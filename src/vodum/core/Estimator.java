/*
 * Copyright (C) 2016 by
 * 
 *  Thibaut Thonet
 *  thibaut.thonet@irit.fr
 *  Institut de Recherche en Informatique de Toulouse (IRIT)
 *  University of Toulouse, Toulouse
 * 
 * This file is part of VODUM.
 *
 * VODUM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VODUM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VODUM. If not, see <http://www.gnu.org/licenses/>
 */

/*
 * Copyright (C) 2007 by
 * 
 * 	Xuan-Hieu Phan
 *	hieuxuan@ecei.tohoku.ac.jp or pxhieu@gmail.com
 * 	Graduate School of Information Sciences
 * 	Tohoku University
 * 
 *  Cam-Tu Nguyen
 *  ncamtu@gmail.com
 *  College of Technology
 *  Vietnam National University, Hanoi
 *
 * JGibbsLDA is a free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * JGibbsLDA is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGibbsLDA; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package vodum.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import vodum.utils.Utils;

public class Estimator {
	
	protected Model trnModel; // output model
	CmdOption option;
	
	public boolean init(CmdOption option) throws FileNotFoundException, UnsupportedEncodingException{
		this.option = option;
		trnModel = new Model();

		if (!trnModel.initNewModel(option)) {
			return false;
		}

		return true;
	}
	
	public void estimate() throws FileNotFoundException, UnsupportedEncodingException {
		System.out.println("Sampling " + trnModel.nchains + " chains of " + trnModel.niters + " iterations!");
		
		for (int currentChain = 1; currentChain <= trnModel.nchains; currentChain++) {
			for (int currentIter = 1; currentIter <= trnModel.niters; currentIter++) {
				System.out.println("Chain " + currentChain + ", Iteration " + currentIter + "...");

				for (int d = 0; d < trnModel.D; d++) {
					for (int m = 0; m < trnModel.data.docs[d].length; m++) {
						// sample from p(z[d][m] | v, z_-[d][m], w, x)
						int topic = zSampling(d, m);
						trnModel.zAssign[d][m] = topic;
					} // end for each sentence
					
					// sample from p(v[d] | v_-[d], z, w, x)
					int viewpoint = vSampling(d);
					trnModel.vAssign[d] = viewpoint;
				} // end for each document

				if (option.savestep > 0) {
					// save the model if the iterations are a multiple of savestep
					// and if it's not the final iteration (the final model will
					// be saved later)
					if ((currentIter % option.savestep == 0) && (currentIter != trnModel.niters)) {
						System.out.println("Saving the model for chain " + currentChain + " at iteration " + currentIter + "...");
						computeTheta();
						computePi();
						computePhi0();
						computePhi1();
						trnModel.computePerplexity();
						trnModel.saveModel("model-" + Utils.zeroPad(currentChain, 2) + "-" + Utils.zeroPad(currentIter, 5));
					}
				}
			} // end iterations per chain
			
			System.out.println("Saving the final model for chain " + currentChain + "!");
			computeTheta();
			computePi();
			computePhi0();
			computePhi1();
			trnModel.computePerplexity();
			trnModel.saveModel("model-" + Utils.zeroPad(currentChain, 2) + "-final");
			trnModel.data.localDict.writeWordMap(option.dir + File.separator + "model-" + Utils.zeroPad(currentChain, 2) + "-final" + Model.wordMapSuffix);
			
			if (currentChain < trnModel.nchains) {
				trnModel.initNewModel(option); // reinitialize trnModel for the next chain
			}
		} // end chains
		System.out.println("Gibbs sampling completed!\n");
	}
	
	/**
	 * Do viewpoint sampling.
	 * @param d document index
	 * @return viewpoint id
	 */
	public int vSampling(int d) {
		// remove v[d] from the count variables
		int viewpoint = trnModel.vAssign[d];

		// changing the viewpoint assignment of the current document
		// modifies n1vzw, n1vzwsum, nvz, nvzsum and nv depending on
		// the word occurrences in the document
		for (int m = 0; m < trnModel.data.docs[d].length; m++) {
			int topic = trnModel.zAssign[d][m];
			
			Sentence sentence = trnModel.data.docs[d].sentences[m];	
			for (int k : sentence.n1Map.keySet()) {
				// opinion word
				int kcount = sentence.n1Map.get(k); // number of occurrences of k in the current sentence
				trnModel.n1vzw[viewpoint][topic][k] -= kcount;
				trnModel.n1vzwsum[viewpoint][topic] -= kcount;
			}
			
			trnModel.nvz[viewpoint][topic] -= 1;
			trnModel.nvzsum[viewpoint] -= 1;
		}
		trnModel.nv[viewpoint] -= 1;
		trnModel.nvsum -= 1;
		
		// currentN1vzw counts the number of occurrences for each
		// opinion word and their assigned topic in the current
		// document, and currentNvz counts the number of sentences
		// assigned to each topic in the current document.
		Map<Integer, Map<Integer, Integer>> currentN1vzw = new HashMap<Integer, Map<Integer, Integer>>(); // key1=topicId, key2=wordId, val=wordCount
		Map<Integer, Integer> currentNvz = new HashMap<Integer, Integer>(); // key=topicId, val=sentenceCount
		for (int m = 0; m < trnModel.data.docs[d].length; m++) {
			int topic = trnModel.zAssign[d][m];
			
			if (!currentN1vzw.containsKey(topic)) {
				// this topic hasn't been seen in the document yet
				currentN1vzw.put(topic, new HashMap<Integer, Integer>());
			}
			Map<Integer, Integer> currentN1vzwTopic = currentN1vzw.get(topic);
			
			Sentence sentence = trnModel.data.docs[d].sentences[m];
			for (int k : sentence.n1Map.keySet()) {
				// opinion word
				int kcount = sentence.n1Map.get(k); // number of occurrences of k as an opinion word in the current sentence
				if (!currentN1vzwTopic.containsKey(k)) {
					// this word for this topic hasn't been seen in
					// the document yet
					currentN1vzwTopic.put(k, kcount);
				} else {
					// this word for this topic has already been
					// seen in the document
					currentN1vzwTopic.put(k, kcount + currentN1vzwTopic.get(k));
				}
			}
			
			if (!currentNvz.containsKey(topic)) {
				// this topic hasn't been seen in the document yet
				currentNvz.put(topic, 1);
			} else {
				// this topic has already been seen in the document
				currentNvz.put(topic, 1 + currentNvz.get(topic));
			}
		}
		
		double Veta = trnModel.V*trnModel.eta;
		double Talpha = trnModel.T*trnModel.alpha;
		double W1beta1 = trnModel.W1*trnModel.beta1;
		
		// do multinominal sampling via cumulative method
		
		// log probabilities are used instead of normal
		// probabilities in order to avoid that probabilities
		// undergo underflow (resulting in them being approximated
		// to 0)
		double[] logP = new double[trnModel.V];
		
		// maxLogP will be used to normalize the probabilities
		double maxLogP = Double.NEGATIVE_INFINITY;
				
		// calculate probabilities for each viewpoint
		for (int i = 0; i < trnModel.V; i++) {
			logP[i] = Math.log(trnModel.eta + trnModel.nv[i]) - Math.log(Veta + trnModel.nvsum);
			
			double totalCount1 = 0;
			for (int j : currentNvz.keySet()) {
				for (double count = 0; count < currentNvz.get(j); count++) {
					logP[i] += Math.log(trnModel.alpha + trnModel.nvz[i][j] + count) - Math.log(Talpha + trnModel.nvzsum[i] + totalCount1);		
					totalCount1++;
				}
			}
			
			for (int j : currentN1vzw.keySet()) {
				Map<Integer, Integer> currentN1vzwTopic = currentN1vzw.get(j);

				double totalCount2 = 0;
				for (int k : currentN1vzwTopic.keySet()) {
					for (double count = 0; count < currentN1vzwTopic.get(k); count++) {
						logP[i] += Math.log(trnModel.beta1 + trnModel.n1vzw[i][j][k] + count) - Math.log(W1beta1 + trnModel.n1vzwsum[i][j] + totalCount2);		
						totalCount2++;
					}
				}
			}
			
			if (logP[i] > maxLogP) {
				maxLogP = logP[i];
			}
		}
		
		// normalize probabilities
		for (int i = 0; i < trnModel.V; i++) {
			trnModel.pv[i] = Math.exp(logP[i] - maxLogP);
		}
		
		// cumulate multinomial parameters
		for (int i = 1; i < trnModel.V; i++) {
			trnModel.pv[i] += trnModel.pv[i - 1];
		}
		
		// scaled sample
		double scaledRand = Math.random()*trnModel.pv[trnModel.V - 1];
		
		// sample viewpoint w.r.t distribution pv
		for (viewpoint = 0; viewpoint < trnModel.V; viewpoint++) {
			if (trnModel.pv[viewpoint] > scaledRand) {
				break;
			}
		}
		
		// add newly estimated v[d] to count variables
		for (int m = 0; m < trnModel.data.docs[d].length; m++) {
			int topic = trnModel.zAssign[d][m];
			
			Sentence sentence = trnModel.data.docs[d].sentences[m];
			for (int k : sentence.n1Map.keySet()) {
				// opinion word
				int kcount = sentence.n1Map.get(k); // number of occurrences of k in the current sentence
				trnModel.n1vzw[viewpoint][topic][k] += kcount;
				trnModel.n1vzwsum[viewpoint][topic] += kcount;
			}
			
			trnModel.nvz[viewpoint][topic] += 1;
			trnModel.nvzsum[viewpoint] += 1;
		}
		trnModel.nv[viewpoint] += 1;
		trnModel.nvsum += 1;
		
		return viewpoint;
	}
	
	/**
	 * Do topic sampling.
	 * @param d document index
	 * @param m sentence index
	 * @return topic id
	 */
	public int zSampling(int d, int m) {
		// remove z[d][m] from the count variables
		int topic = trnModel.zAssign[d][m];
		int viewpoint = trnModel.vAssign[d];
		Sentence sentence = trnModel.data.docs[d].sentences[m];
		
		// changing the topic assignment of the current sentence
		// modifies n0zw, n0zwsum, n1vzw, n1vzwsum and nvz depending on
		// the word occurrences in the sentence
		for (int k : sentence.n0Map.keySet()) {
			// topical word
			int kcount = sentence.n0Map.get(k); // number of occurrences of k in the current sentence
			trnModel.n0zw[topic][k] -= kcount;
			trnModel.n0zwsum[topic] -= kcount;
		}
		for (int k : sentence.n1Map.keySet()) {
			// opinion word
			int kcount = sentence.n1Map.get(k); // number of occurrences of k in the current sentence
			trnModel.n1vzw[viewpoint][topic][k] -= kcount;
			trnModel.n1vzwsum[viewpoint][topic] -= kcount;
		}
		trnModel.nvz[viewpoint][topic] -= 1;
		trnModel.nvzsum[viewpoint] -= 1;
		
		double Talpha = trnModel.T*trnModel.alpha;
		double W0beta0 = trnModel.W0*trnModel.beta0;
		double W1beta1 = trnModel.W1*trnModel.beta1;
		
		// do multinominal sampling via cumulative method
		
		// log probabilities are used instead of normal
		// probabilities in order to avoid that probabilities
		// undergo underflow (resulting in them being approximated
		// to 0)
		double[] logP = new double[trnModel.T];
		
		// maxLogP will be used to normalize the probabilities
		double maxLogP = Double.NEGATIVE_INFINITY;
		
		// calculate probabilities for each topic
		for (int j = 0; j < trnModel.T; j++) {
			logP[j] = Math.log(trnModel.alpha + trnModel.nvz[viewpoint][j]) - Math.log(Talpha + trnModel.nvzsum[viewpoint]);
			
			double totalCount1 = 0;
			for (int k : sentence.n0Map.keySet()) {
				for (double count = 0; count < sentence.n0Map.get(k); count++) {
					logP[j] += Math.log(trnModel.beta0 + trnModel.n0zw[j][k] + count) - Math.log(W0beta0 + trnModel.n0zwsum[j] + totalCount1);		
					totalCount1++;
				}
			}
			
			double totalCount2 = 0;
			for (int k : sentence.n1Map.keySet()) {
				for (double count = 0; count < sentence.n1Map.get(k); count++) {
					logP[j] += Math.log(trnModel.beta1 + trnModel.n1vzw[viewpoint][j][k] + count) - Math.log(W1beta1 + trnModel.n1vzwsum[viewpoint][j] + totalCount2);		
					totalCount2++;
				}
			}
			
			if (logP[j] > maxLogP) {
				maxLogP = logP[j];
			}
		}
		
		// normalize probabilities
		for (int j = 0; j < trnModel.T; j++) {
			trnModel.pz[j] = Math.exp(logP[j] - maxLogP);
		}
		
		// cumulate multinomial parameters
		for (int j = 1; j < trnModel.T; j++) {
			trnModel.pz[j] += trnModel.pz[j - 1];
		}
		
		// scaled sample
		double scaledRand = Math.random()*trnModel.pz[trnModel.T - 1];
		
		// sample topic w.r.t distribution pz
		for (topic = 0; topic < trnModel.T; topic++) {
			if (trnModel.pz[topic] > scaledRand) {
				break;
			}
		}
		
		// add newly estimated z[d][m] to count variables
		for (int k : sentence.n0Map.keySet()) {
			// topical word
			int kcount = sentence.n0Map.get(k);
			trnModel.n0zw[topic][k] += kcount;
			trnModel.n0zwsum[topic] += kcount;
		}
		for (int k : sentence.n1Map.keySet()) {
			// opinion word
			int kcount = sentence.n1Map.get(k);
			trnModel.n1vzw[viewpoint][topic][k] += kcount;
			trnModel.n1vzwsum[viewpoint][topic] += kcount;
		}
		trnModel.nvz[viewpoint][topic] += 1;
		trnModel.nvzsum[viewpoint] += 1;
		
 		return topic;
	}
	
	public void computeTheta() {
		for (int i = 0; i < trnModel.V; i++) {
			for (int j = 0; j < trnModel.T; j++) {
				trnModel.theta[i][j] = (trnModel.nvz[i][j] + trnModel.alpha)/(trnModel.nvzsum[i] + trnModel.T*trnModel.alpha);
			}
		}
	}
	
	public void computePi() {
		for (int i = 0; i < trnModel.V; i++) {
			trnModel.pi[i] = (trnModel.nv[i] + trnModel.eta)/(trnModel.nvsum + trnModel.V*trnModel.eta);
		}
	}
	
	public void computePhi0() {	
		for (int j = 0; j < trnModel.T; j++) {
			for (int k = 0; k < trnModel.W; k++) {
				if (trnModel.data.wordIdPosMap.get(k).get(0)) {
					trnModel.phi0[j][k] = (trnModel.n0zw[j][k] + trnModel.beta0)/(trnModel.n0zwsum[j] + trnModel.W0*trnModel.beta0);
				} else {
					trnModel.phi0[j][k] = 0;
				}
			}
		}
	}
	
	public void computePhi1() {
		for (int i = 0; i < trnModel.V; i++) {
			for (int j = 0; j < trnModel.T; j++) {
				for (int k = 0; k < trnModel.W; k++) {
					if (trnModel.data.wordIdPosMap.get(k).get(1)) {
						trnModel.phi1[i][j][k] = (trnModel.n1vzw[i][j][k] + trnModel.beta1)/(trnModel.n1vzwsum[i][j] + trnModel.W1*trnModel.beta1);
					} else {
						trnModel.phi1[i][j][k] = 0;
					}
				}
			}
		}
	}
}
