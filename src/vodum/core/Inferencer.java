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
import java.util.HashMap;
import java.util.Map;

public class Inferencer {	
	public Model trnModel; // training model
	public Dictionary globalDict;
	private CmdOption option;
	private Model newModel; // test model
	
	//-----------------------------------------------------
	// Init method
	//-----------------------------------------------------
	
	public boolean init(CmdOption option) {
		this.option = option;
		trnModel = new Model();
		
		if (!trnModel.initEstimatedModel(option))
			return false;		
		
		globalDict = trnModel.data.localDict;
		
		return true;
	}
	
	/**
	 * Infer new model using data from a specified dataset.
	 * @param newData data on which we want to do inference
	 * @return the new model
	 */
	public Model inference(Dataset newData) {
		System.out.println("init new model");
		newModel = new Model();		

		newModel.initNewModel(option, newData, trnModel);	

		System.out.println("Sampling " + newModel.niters + " iterations!");

		for (int currentIter = 1; currentIter <= newModel.niters; currentIter++) {
			System.out.println("Iteration " + currentIter + "...");

			for (int d = 0; d < newModel.D; d++) {
				for (int m = 0; m < newModel.data.docs[d].length; m++) {
					// sample from p(z[d][m] | v, z_-[d][m], w, x)
					int topic = infZSampling(d, m);
					newModel.zAssign[d][m] = topic;
				} // end for each sentence
				
				// sample from p(v[d] | v_-[d], z, w, x)
				int viewpoint = infVSampling(d);
				newModel.vAssign[d] = viewpoint;
			} // end for each new document

		} // end iterations

		System.out.println("Gibbs sampling for inference completed!");

		computeNewTheta();
		computeNewPi();
		computeNewPhi0();
		computeNewPhi1();
		newModel.computePerplexity();

		return this.newModel;
	}
	
	/**
	 * Infer new model using data from a specified string array.
	 * @param strs data on which we want to do inference
	 * @return the new model
	 */
	public Model inference(String[] strs) {
		Dataset dataset = Dataset.readDataSet(strs, globalDict);
		
		return inference(dataset);
	}
	
	/**
	 * Infer new model using data from a dataset specified in the option.
	 * @return the new model
	 */
	public Model inference() {	
		newModel = new Model();
		if (!newModel.initNewModel(option, trnModel)) return null;
		
		System.out.println("Sampling " + newModel.niters + " iterations!");		

		for (int currentIter = 1; currentIter <= newModel.niters; currentIter++) {
			System.out.println("Iteration " + currentIter + "...");

			for (int d = 0; d < newModel.D; d++) {
				// sample from p(v[d] | v_-d, z, s, w, p)
				int viewpoint = infVSampling(d);
				newModel.vAssign[d] = viewpoint;

				for (int m = 0; m < newModel.data.docs[d].length; m++) {
					// sample from p(z[d][m] | v, z_-(d,m), w, p)
					int topic = infZSampling(d, m);
					newModel.zAssign[d][m] = topic;
				} // end for each sentence
			} //end for each new document
		} // end iterations
		
		System.out.println("Gibbs sampling for inference completed!");		
		System.out.println("Saving the inference outputs!");
		
		computeNewTheta();
		computeNewPi();
		computeNewPhi0();
		computeNewPhi1();
		newModel.computePerplexity();
		newModel.saveModel(trnModel.modelName + "-inference");
		newModel.data.localDict.writeWordMap(option.dir + File.separator + trnModel.modelName + "-inference" + Model.wordMapSuffix);
		
		return newModel;
	}
	
	/**
	 * Do viewpoint sampling for inference.
	 * @param d document index
	 * @return viewpoint id
	 */
	public int infVSampling(int d) {
		// remove v[d] from the count variable
		int viewpoint = newModel.vAssign[d];

		// changing the viewpoint assignment of the current document
		// modifies n1vzw, n1vzwsum, nvz, nvzsum and nv depending on
		// the word occurrences in the document
		for (int m = 0; m < newModel.data.docs[d].length; m++) {
			int topic = newModel.zAssign[d][m];
			
			Sentence sentence = newModel.data.docs[d].sentences[m];	
			for (int k : sentence.n1Map.keySet()) {
				// opinion word
				int kcount = sentence.n1Map.get(k); // number of occurrences of k in the current sentence
				newModel.n1vzw[viewpoint][topic][k] -= kcount;
				newModel.n1vzwsum[viewpoint][topic] -= kcount;
			}
			
			newModel.nvz[viewpoint][topic] -= 1;
			newModel.nvzsum[viewpoint] -= 1;
		}
		newModel.nv[viewpoint] -= 1;
		newModel.nvsum -= 1;
		
		// currentN1vzw counts the number of occurrences for each
		// opinion word and their assigned topic in the current
		// document, and currentNvz counts the number of sentences
		// assigned to each topic in the current document.
		Map<Integer, Map<Integer, Integer>> currentN1vzw = new HashMap<Integer, Map<Integer, Integer>>(); // key1=topicId, key2=wordId, val=wordCount
		Map<Integer, Integer> currentNvz = new HashMap<Integer, Integer>(); // key=topicId, val=sentenceCount
		for (int m = 0; m < newModel.data.docs[d].length; m++) {
			int topic = newModel.zAssign[d][m];
			
			if (!currentN1vzw.containsKey(topic)) {
				// this topic hasn't been seen in the document yet
				currentN1vzw.put(topic, new HashMap<Integer, Integer>());
			}
			Map<Integer, Integer> currentNz1vwTopic = currentN1vzw.get(topic);
			
			Sentence sentence = newModel.data.docs[d].sentences[m];
			for (int k : sentence.n1Map.keySet()) {
				// opinion word
				int kcount = sentence.n1Map.get(k); // number of occurrences of k in the current sentence
				if (!currentNz1vwTopic.containsKey(k)) {
					// this word for this topic hasn't been seen in
					// the document yet
					currentNz1vwTopic.put(k, kcount);
				} else {
					// this word for this topic has already been
					// seen in the document
					currentNz1vwTopic.put(k, kcount + currentNz1vwTopic.get(k));
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
		
		double Veta = newModel.V * newModel.eta;
		double Talpha = newModel.T * newModel.alpha;
		double W1beta1 = trnModel.W1 * newModel.beta1;
		
		// do multinominal sampling via cumulative method
		
		// log probabilities are used instead of normal
		// probabilities in order to avoid that probabilities
		// undergo underflow (resulting in them being approximated
		// to 0)
		double[] logP = new double[newModel.V];

		// maxLogP will be used to normalize the probabilities
		double maxLogP = Double.NEGATIVE_INFINITY;

		// calculate probabilities for each viewpoint
		for (int i = 0; i < newModel.V; i++) {
			logP[i] = Math.log(newModel.eta + trnModel.nv[i] + newModel.nv[i]) - Math.log(Veta + trnModel.nvsum + newModel.nvsum);

			int totalCount1 = 0;
			for (int j : currentNvz.keySet()) {
				for (int count = 0; count < currentNvz.get(j); count++) {
					logP[i] += Math.log(newModel.alpha + trnModel.nvz[i][j] + newModel.nvz[i][j] + count) - Math.log(Talpha + trnModel.nvzsum[i] + newModel.nvzsum[i] + totalCount1);		
					totalCount1++;
				}
			}

			for (int j : currentN1vzw.keySet()) {
				Map<Integer, Integer> currentNz1vwTopic = currentN1vzw.get(j);

				int totalCount2 = 0;
				for (int _k : currentNz1vwTopic.keySet()) {
					int k = newModel.data.lid2gid.get(_k);

					for (int count = 0; count < currentNz1vwTopic.get(_k); count++) {
						logP[i] += Math.log(newModel.beta1 + trnModel.n1vzw[i][j][k] + newModel.n1vzw[i][j][_k] + count) - Math.log(W1beta1 + trnModel.n1vzwsum[i][j] + newModel.n1vzwsum[i][j] + totalCount2);		
						totalCount2++;
					}
				}
			}

			if (logP[i] > maxLogP) {
				maxLogP = logP[i];
			}
		}

		// normalize probabilities
		for (int i = 0; i < newModel.V; i++) {
			newModel.pv[i] = Math.exp(logP[i] - maxLogP);
		}

		// cumulate multinomial parameters
		for (int i = 1; i < newModel.V; i++) {
			newModel.pv[i] += newModel.pz[i - 1];
		}
		
		// scaled sample
		double u = Math.random() * newModel.pv[newModel.V - 1];
		
		for (viewpoint = 0; viewpoint < newModel.V; viewpoint++) {
			if (newModel.pv[viewpoint] > u) //sample topic w.r.t distribution pz
				break;
		}
		
		// add newly estimated v[d] to count variables
		for (int m = 0; m < newModel.data.docs[d].length; m++) {
			int topic = newModel.zAssign[d][m];
			
			Sentence sentence = newModel.data.docs[d].sentences[m];
			for (int k : sentence.n1Map.keySet()) {
				// opinion word
				int kcount = sentence.n1Map.get(k); // number of occurrences of k in the current sentence
				newModel.n1vzw[viewpoint][topic][k] += kcount;
				newModel.n1vzwsum[viewpoint][topic] += kcount;
			}
			
			newModel.nvz[viewpoint][topic] += 1;
			newModel.nvzsum[viewpoint] += 1;
		}
		newModel.nv[viewpoint] += 1;
		newModel.nvsum += 1;
		
		return viewpoint;
	}
	
	/**
	 * Do topic sampling for inference.
	 * @param d document index
	 * @param m sentence index
	 * @return topic id
	 */
	public int infZSampling(int d, int m) {
		// remove z[d][m] from the count variable
		int topic = newModel.zAssign[d][m];
		int viewpoint = newModel.vAssign[d];
		Sentence sentence = newModel.data.docs[d].sentences[m];
		
		// changing the topic assignment of the current sentence
		// modifies n0zw, n0zwsum, n1vzw and n1vzwsum depending on
		// the word occurrences in the sentence
		for (int k : sentence.n0Map.keySet()) {
			// topical word
			int kcount = sentence.n0Map.get(k); // number of occurrences of k in the current sentence
			newModel.n0zw[topic][k] -= kcount;
			newModel.n0zwsum[topic] -= kcount;
		}
		for (int k : sentence.n1Map.keySet()) {
			// opinion word
			int kcount = sentence.n1Map.get(k); // number of occurrences of k in the current sentence
			newModel.n1vzw[viewpoint][topic][k] -= kcount;
			newModel.n1vzwsum[viewpoint][topic] -= kcount;
		}
		newModel.nvz[viewpoint][topic] -= 1;
		newModel.nvzsum[viewpoint] -= 1;
		
		double Talpha = newModel.T * newModel.alpha;
		double W0beta0 = trnModel.W0 * newModel.beta0;
		double W1beta1 = trnModel.W1 * newModel.beta1; 

		// do multinominal sampling via cumulative method

		// log probabilities are used instead of normal
		// probabilities in order to avoid that probabilities
		// undergo underflow (resulting in them being approximated
		// to 0)
		double[] logP = new double[newModel.T];

		// maxLogP will be used to normalize the probabilities
		double maxLogP = Double.NEGATIVE_INFINITY;
		
		// calculate probabilities for each topic
		for (int j = 0; j < newModel.T; j++) {
			logP[j] = Math.log(newModel.alpha + trnModel.nvz[viewpoint][j] + newModel.nvz[viewpoint][j]) - Math.log(Talpha + trnModel.nvzsum[viewpoint] + newModel.nvzsum[viewpoint]);
			
			int totalCount1 = 0;
			for (int _k : sentence.n0Map.keySet()) {
				int k = newModel.data.lid2gid.get(_k);
				
				for (int count = 0; count < sentence.n0Map.get(_k); count++) {
					logP[j] += Math.log(newModel.beta0 + trnModel.n0zw[j][k] + newModel.n0zw[j][_k] + count) - Math.log(W0beta0 + trnModel.n0zwsum[j] + newModel.n0zwsum[j] + totalCount1);		
					totalCount1++;
				}
			}
			
			int totalCount2 = 0;
			for (int _k : sentence.n1Map.keySet()) {
				int k = newModel.data.lid2gid.get(_k);
				
				for (int count = 0; count < sentence.n1Map.get(_k); count++) {
					logP[j] += Math.log(newModel.beta1 + trnModel.n1vzw[viewpoint][j][k] + newModel.n1vzw[viewpoint][j][_k] + count) - Math.log(W1beta1 + trnModel.n1vzwsum[viewpoint][j] + newModel.n1vzwsum[viewpoint][j] + totalCount2);		
					totalCount2++;
				}
			}
			
			if (logP[j] > maxLogP) {
				maxLogP = logP[j];
			}
		}
		
		// normalize probabilities
		for (int j = 0; j < newModel.T; j++) {
			newModel.pz[j] = Math.exp(logP[j] - maxLogP);
		}
		
		// cumulate multinomial parameters
		for (int j = 1; j < newModel.T; j++) {
			newModel.pz[j] += newModel.pz[j - 1];
		}
		
		// scaled sample because of unnormalized pz[]
		double u = Math.random() * newModel.pz[newModel.T - 1];
		
		for (topic = 0; topic < newModel.T; topic++) {
			if (newModel.pz[topic] > u) //sample topic w.r.t distribution pz
				break;
		}
		
		// add newly estimated z[d][m] to count variables
		for (int k : sentence.n0Map.keySet()) {
			// topical word
			int kcount = sentence.n0Map.get(k);
			newModel.n0zw[topic][k] += kcount;
			newModel.n0zwsum[topic] += kcount;
		}
		for (int k : sentence.n1Map.keySet()) {
			// opinion word
			int kcount = sentence.n1Map.get(k); // number of occurrences of k in the current sentence
			newModel.n1vzw[viewpoint][topic][k] += kcount;
			newModel.n1vzwsum[viewpoint][topic] += kcount;
		}
		newModel.nvz[viewpoint][topic] += 1;
		newModel.nvzsum[viewpoint] += 1;
		
 		return topic;
	}
	
	protected void computeNewTheta() {
		for (int i = 0; i < newModel.V; i++) {
			for (int j = 0; j < newModel.T; j++) {
				newModel.theta[i][j] = (trnModel.nvz[i][j] + newModel.nvz[i][j] + newModel.alpha) / (trnModel.nvzsum[i] + newModel.nvzsum[i] + newModel.T * newModel.alpha);
			} // end for each topic
		} // end for each viewpoint
	}
	
	protected void computeNewPi() {
		for (int i = 0; i < newModel.V; i++) {
			newModel.pi[i] = (trnModel.nv[i] + newModel.nv[i] + newModel.eta) / (trnModel.nvsum + newModel.nvsum + newModel.V * newModel.eta);
		} // end for each viewpoint
	}
	
	protected void computeNewPhi0() {
		for (int j = 0; j < newModel.T; j++) {
			for (int _k = 0; _k < newModel.W; _k++) {
				Integer k = newModel.data.lid2gid.get(_k);

				if (k != null) {
					if (newModel.data.wordIdPosMap.get(_k).get(0) || trnModel.data.wordIdPosMap.get(k).get(0)) {
						newModel.phi0[j][_k] = (trnModel.n0zw[j][k] + newModel.n0zw[j][_k] + newModel.beta0) / (trnModel.n0zwsum[j] + newModel.n0zwsum[j] + trnModel.W0 * newModel.beta0);
					} else {
						newModel.phi0[j][_k] = 0;
					}
				}
			} // end for each word
		} // end for each topic
	}
	
	protected void computeNewPhi1() {
		for (int i = 0; i < newModel.V; i++) {
			for (int j = 0; j < newModel.T; j++) {
				for (int _k = 0; _k < newModel.W; _k++) {
					Integer k = newModel.data.lid2gid.get(_k);
					
					if (k != null) {
						if (newModel.data.wordIdPosMap.get(_k).get(1) || trnModel.data.wordIdPosMap.get(k).get(1)) {
							newModel.phi1[i][j][_k] = (trnModel.n1vzw[i][j][k] + newModel.n1vzw[i][j][_k] + newModel.beta1) / (trnModel.n1vzwsum[i][j] + newModel.n1vzwsum[i][j] + trnModel.W1 * newModel.beta1);
						} else {
							newModel.phi1[i][j][_k] = 0;
						}
					}
				} // end for each word
			} // end for each topic
		} // end for each viewpoint
	}
}