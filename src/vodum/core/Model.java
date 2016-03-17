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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import vodum.utils.Pair;
import vodum.utils.Utils;

public class Model {	
	
	//---------------------------------------------------------------
	// Class Variables
	//---------------------------------------------------------------
	
	public static String assignSuffix; // suffix for topic-viewpoint assignment file
	public static String thetaSuffix; // suffix for theta (topic distribution) file
	public static String piSuffix; // suffix for pi (viewpoint distribution) file
	public static String phi0Suffix; // suffix for phi0 (topical word distribution) file
	public static String phi1Suffix; // suffix for phi1 (opinion word distribution) file
	public static String othersSuffix; // suffix for containing other parameters
	public static String twordsSuffix; // suffix for file containing words-per-topic
	public static String vtwordsSuffix; // suffix for file containing words-per-viewpoint/topic
	public static String wordMapSuffix; // suffix for file containing word to id map
	public static String defaultModelName; // default name for the model
	
	//---------------------------------------------------------------
	// Model Parameters and Variables
	//---------------------------------------------------------------	
	
	public String dir; // path of the directory containing the dataset file
	public String dfile; // name of the file containing the dataset
	public String modelName; // name of the model
	public Dataset data; // link to a dataset
	
	public int D; // dataset size (i.e., number of docs)
	public int W; // vocabulary size
	public int W0; // number of topical words in the vocabulary
	public int W1; // number of opinion words in the vocabulary
	public int T; // number of topics
	public int V; // number of viewpoints
	public double perplexity; // perplexity of the model on the dataset
	public double alpha, eta, beta0, beta1; // hyperparameters
	public int nchains; // number of Gibbs sampling chains
	public int niters; // number of Gibbs sampling iterations per chain
	public int savestep; // saving period
	public int topwords; // print out top words
	
	// Estimated/inferred parameters
	public double[][] theta; // theta: viewpoint-specific distributions over topics, size V x T
	public double[] pi; // pi: distribution over viewpoints, size V
	public double[][] phi0; // phi0: topic-specific distributions over words (= topical word distributions), size T x W
	public double[][][] phi1; // phi1: viewpoint-topic-specific distributions over words (= opinion word distributions), size V x T x W
	
	// Variables for sampling
	public int[][] zAssign; // topic assignments for all sentences
	public int[] vAssign; // viewpoint assignments for all documents
	
	protected double[][] nvz; // nvz[i][j]: number of sentences in the collection assigned to viewpoint i and topic j, size V x T
	protected double[] nvzsum; // nvzsum[i]: total number of sentences in the collection assigned to viewpoint i, size V
	
	protected double[][] n0zw; // n0zw[j][k]: number of instances of topical (0) word k assigned to topic j, size T x W
	protected double[] n0zwsum; // n0zwsum[j]: total number of topical (0) words assigned to topic j, size T
	
	protected double[][][] n1vzw; // n1vzw[i][j][k]: number of instances of opinion (1) word k assigned to viewpoint i and topic j, size V x T x W
	protected double[][] n1vzwsum; // n1vzwsum[i][j]: total number of opinion (1) words assigned to viewpoint i and topic j, size V x T
	
	protected double[] nv; // nv[i]: number of documents assigned to viewpoint i, size V
	protected double nvsum; // nvsum: total number of documents, size 1
	
	protected double[] pz;
	protected double[] pv;
	
	// Random number generators
	private Random topicRandomGenerator;
	private Random viewpointRandomGenerator;
	
	//---------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------	

	public Model() {
		setDefaultValues();	
	}
	
	/**
	 * Set default values for variables.
	 */
	public void setDefaultValues() {
		assignSuffix = ".assign";
		thetaSuffix = ".theta";
		piSuffix = ".pi";
		phi0Suffix = ".phi0";
		phi1Suffix = ".phi1";
		othersSuffix = ".others";
		twordsSuffix = ".twords";
		vtwordsSuffix = ".vtwords";
		wordMapSuffix = ".wordmap";
		defaultModelName = "model-final";
		
		dir = "./";
		dfile = "trndocs.dat";
		modelName = defaultModelName;	
		
		D = 0;
		W = 0;
		W0 = 0;
		W1 = 0;
		T = 100;
		V = 2;
		perplexity = 0;
		alpha = 50.0/T;
		eta = 0.1;
		beta0 = 0.05;
		beta1 = 0.05;
		nchains = 1;
		niters = 2000;
		
		zAssign = null;
		vAssign = null;
		nvz = null;
		n0zw = null;
		n1vzw = null;
		nv = null;
		nvzsum = null;
		n0zwsum = null;
		n1vzwsum = null;
		
		theta = null;
		pi = null;
		phi0 = null;
		phi1 = null;
		
		topicRandomGenerator = null;
		viewpointRandomGenerator = null;
	}
	
	//---------------------------------------------------------------
	// 	I/O Methods
	//---------------------------------------------------------------
	
	/**
	 * Read others file to get parameters.
	 */
	protected boolean readOthersFile(String otherFile) {
		// open file <model>.others to read
		try {
			BufferedReader reader = new BufferedReader(new FileReader(otherFile));
			String line;
			while ((line = reader.readLine()) != null) {
				StringTokenizer tknr = new StringTokenizer(line,"= \t\r\n");
				
				int count = tknr.countTokens();
				if (count != 2) {
					continue;
				}
					
				String optstr = tknr.nextToken();
				String optval = tknr.nextToken();
				
				if (optstr.equalsIgnoreCase("alpha")) {
					alpha = Double.parseDouble(optval);					
				} else if (optstr.equalsIgnoreCase("beta0")) {
					beta0 = Double.parseDouble(optval);					
				} else if (optstr.equalsIgnoreCase("beta1")) {
					beta1 = Double.parseDouble(optval);					
				} else if (optstr.equalsIgnoreCase("eta")) {
					eta = Double.parseDouble(optval);
				} else if (optstr.equalsIgnoreCase("ntopics")) {
					T = Integer.parseInt(optval);
				} else if (optstr.equalsIgnoreCase("nviews")) {
					V = Integer.parseInt(optval);
				} else if (optstr.equalsIgnoreCase("nwords")) {
					W = Integer.parseInt(optval);
				} else if (optstr.equalsIgnoreCase("ntopwords")) {
					W0 = Integer.parseInt(optval);
				} else if (optstr.equalsIgnoreCase("nopwords")) {
					W1 = Integer.parseInt(optval);
				} else if (optstr.equalsIgnoreCase("ndocs")) {
					D = Integer.parseInt(optval);
				}
			}
			
			reader.close();
			
		} catch (Exception e) {
			System.out.println("Error while reading others file: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 *  Read the file containing the variable assignments.
	 */
	protected boolean readAssignFile(String assignFile) {
		// open file <model>.assign to read
		try {
			int d,m;
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(assignFile), "UTF-8"));
			
			try {
				String line;
				zAssign = new int[D][];
				vAssign = new int[D];
				data = new Dataset(D);
				data.W = W;
				data.W0 = W0;
				data.W1 = W1;
				data.wordIdPosMap = new HashMap<Integer, Map<Integer, Boolean>>();
			
				for (d = 0; d < D; d++) {
					// one document per line
					line = reader.readLine();
					StringTokenizer tknr = new StringTokenizer(line, "|"); // each line contains first the viewpoint assignment of the document, then |, and then all the sentences separated by |
					
					int length = tknr.countTokens() - 1;
					
					// the first token is the viewpoint of document d
					int viewpoint = Integer.parseInt(tknr.nextToken());
					
					// assign values for v
					vAssign[d] = viewpoint;
					
					Vector<Sentence> sentences = new Vector<Sentence>();
					
					// the next tokens are the topic assignment for each sentence m
					for (m = 0; m < length; m++) {
						zAssign[d] = new int[length];
						
						Vector<Integer> words = new Vector<Integer>();
						Vector<Integer> pos = new Vector<Integer>();
						
						String token = tknr.nextToken();
						
						// the topic of the sentence and the words are separated by ;
						StringTokenizer tknr2 = new StringTokenizer(token, ";");
						if (tknr2.countTokens() != 2) {
							System.out.println("Invalid sentence-topic assignment line\n");
							return false;
						}
						
						String[] posWords = tknr2.nextToken().split("[ \\t\\n]"); // the first token contains posWords, separated by spaces
						String topicId = tknr2.nextToken(); // the second token contains topicId
						zAssign[d][m] = Integer.parseInt(topicId);
						
						for (String posWord : posWords) {
							// each posWord is made of a word and its part-of-speech,
							// separated by :
							String[] posWordSplit = posWord.split("[:]");
							int wordId = Integer.parseInt(posWordSplit[0]);
							int _pos = Integer.parseInt(posWordSplit[1]);
							
							// check if wordId is in the wordIdPosMap
							if (!data.wordIdPosMap.containsKey(wordId)) {
								// first time this word occurs at all
								Map<Integer, Boolean> currentWordPosMap = new HashMap<Integer, Boolean>();
								currentWordPosMap.put(0, false); // word with pos category 0 hasn't been seen yet
								currentWordPosMap.put(1, false); // word with pos category 1 hasn't been seen yet
								data.wordIdPosMap.put(wordId, currentWordPosMap);
							}
							
							// check the pos category of wordId
							if (_pos == 0 && !data.wordIdPosMap.get(wordId).get(0)) {
								// first time this word occurs as a topical word
								// word with pos category 0 has now been seen
								data.wordIdPosMap.get(wordId).put(0, true);
							} else if (_pos == 1 && !data.wordIdPosMap.get(wordId).get(1)) {
								// first time this word occurs as an opinion word
								// word with pos category 1 has now been seen
								data.wordIdPosMap.get(wordId).put(1, true);
							}
							
							words.add(wordId);
							pos.add(_pos);
						}
						Sentence sentence = new Sentence(words, pos);
						sentences.add(sentence);
					}
					
					// allocate and add new document to the corpus
					Document doc = new Document(sentences);
					data.setDoc(doc, d);
					
				}// end for each doc
			
			} finally {
				reader.close();
			}
			
		} catch (Exception e) {
			System.out.println("Error while loading model: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Load saved model.
	 */
	public boolean loadModel() {
		if (!readOthersFile(dir + File.separator + modelName + othersSuffix)) {
			return false;
		}
		
		if (!readAssignFile(dir + File.separator + modelName + assignSuffix)) {
			return false;
		}
		
		// read dictionary
		Dictionary dict = new Dictionary();
		if (!dict.readWordMap(dir + File.separator + modelName + wordMapSuffix)) {
			return false;
		}
			
		data.localDict = dict;
		
		return true;
	}
	
	/**
	 * Save viewpoint and topic assignments for this model.
	 */
	public boolean saveModelAssign(String filename) {
		int d, m, n;
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			// write docs with viewpoint assignments and topic assignments for sentences
			for (d = 0; d < data.D; d++) {
				writer.write(vAssign[d] + "|");
				for (m = 0; m < data.docs[d].length; ++m) {
					for (n = 0; n < data.docs[d].sentences[m].length; n++) {
						String space = (n == data.docs[d].sentences[m].length - 1 ? "" : " ");
						int word = data.docs[d].sentences[m].words[n];
						int pos = data.docs[d].sentences[m].pos[n];
						writer.write(word + ":" + pos + space);
					}
					String pipe = (m == data.docs[d].length - 1 ? "" : "|");
					writer.write(";" + zAssign[d][m] + pipe);
				}
				writer.write("\n");
			}
				
			writer.close();
			
		} catch (Exception e) {
			System.out.println("Error while saving model assign: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Save theta (topic distribution) for this model.
	 */
	public boolean saveModelTheta(String filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			for (int i = 0; i < V; i++) {
				for (int j = 0; j < T; j++) {
					writer.write(theta[i][j] + " ");
				}
				writer.write("\n");
			}
			
			writer.close();
			
		} catch (Exception e) {
			System.out.println("Error while saving the topic distribution file for this model: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Save pi (viewpoint distribution) for this model.
	 */
	public boolean saveModelPi(String filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			for (int i = 0; i < V; i++) {
				writer.write(pi[i] + " ");
			}
			
			writer.close();
			
		} catch (Exception e) {
			System.out.println("Error while saving the viewpoint distribution file for this model: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Save phi0 (topical word distribution) for this model.
	 */
	public boolean saveModelPhi0(String filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			for (int j = 0; j < T; j++) {
				for (int k = 0; k < W; k++) {
					writer.write(phi0[j][k] + " ");
				}
				writer.write("\n");
			}
				
			writer.close();
			
		} catch (Exception e) {
			System.out.println("Error while saving the topical word distribution file for this model: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Save phi1 (opinion word distribution) for this model.
	 */
	public boolean saveModelPhi1(String filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			for (int i = 0; i < V; i++) {
				for (int j = 0; j < T; j++) {
					for (int k = 0; k < W; k++) {
						writer.write(phi1[i][j][k] + " ");
					}
					writer.write("\n");
				}
				writer.write("\n");
			}
				
			writer.close();
			
		} catch (Exception e) {
			System.out.println("Error while saving the opinion word distribution file for this model: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Save other information of this model.
	 */
	public boolean saveModelOthers(String filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			writer.write("alpha=" + alpha + "\n");
			writer.write("beta0=" + beta0 + "\n");
			writer.write("beta1=" + beta1 + "\n");
			writer.write("eta=" + eta + "\n");
			writer.write("ntopics=" + T + "\n");
			writer.write("nviews=" + V + "\n");
			writer.write("ndocs=" + D + "\n");
			writer.write("nwords=" + W + "\n");
			writer.write("ntopwords=" + W0 + "\n");
			writer.write("nopwords=" + W1 + "\n");
			writer.write("perplexity=" + perplexity + "\n");
			
			writer.close();
			
		} catch(Exception e) {
			System.out.println("Error while saving model others:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Save the most likely topical words for each topic.
	 */
	public boolean saveModelTwords(String filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF-8"));
			
			if (topwords > W) {
				topwords = W;
			}
			
			for (int j = 0; j < T; j++) {
				List<Pair> wordsProbsList = new ArrayList<Pair>();
				
				for (int k = 0; k < W; k++) {
					Pair p = new Pair(k, phi0[j][k], false);

					wordsProbsList.add(p);
				} // end for each word

				// print topic			
				writer.write("Topic " + j + ":\n");
				Collections.sort(wordsProbsList);
				
				Set<String> wordList = new HashSet<String>();			
				for (int t = 0; t < wordsProbsList.size() && wordList.size() < topwords; t++) {
					if (data.localDict.contains((Integer)wordsProbsList.get(t).first)) {
						String word = data.localDict.getWord((Integer)wordsProbsList.get(t).first);
						
						if (!wordList.contains(word)) {
							writer.write("\t" + word + " " + wordsProbsList.get(t).second + "\n");
							wordList.add(word);
						}
					}
				}
			} // end for each topic			
						
			writer.close();
			
		} catch(Exception e) {
			System.out.println("Error while saving model twords: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Save the most likely opinion words for each viewpoint and each topic.
	 */
	public boolean saveModelVTwords(String filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF-8"));
			
			if (topwords > W) {
				topwords = W;
			}
			
			for (int i = 0; i < V; i++) {
				for (int j = 0; j < T; j++) {
					List<Pair> wordsProbsList = new ArrayList<Pair>();
					
					for (int k = 0; k < W; k++) {
						Pair p = new Pair(k, phi1[i][j][k], false);
						
						wordsProbsList.add(p);
					} // end for each word
					
					// print viewpoint and topic				
					writer.write("Viewpoint " + i + ", Topic " + j + ":\n");
					Collections.sort(wordsProbsList);
					
					for (int t = 0; t < topwords; t++) {
						if (data.localDict.contains((Integer)wordsProbsList.get(t).first)) {
							String word = data.localDict.getWord((Integer)wordsProbsList.get(t).first);
							
							writer.write("\t" + word + " " + wordsProbsList.get(t).second + "\n");							
						}
					}
				} // end for each topic
			} // end for each viewpoint			
						
			writer.close();
			
		} catch(Exception e) {
			System.out.println("Error while saving model vtwords: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Save the model.
	 */
	public boolean saveModel(String modelName) {
		if (!saveModelAssign(dir + File.separator + modelName + assignSuffix)) {
			return false;
		}
		
		if (!saveModelOthers(dir + File.separator + modelName + othersSuffix)) {			
			return false;
		}
		
		if (!saveModelTheta(dir + File.separator + modelName + thetaSuffix)) {
			return false;
		}
		
		if (!saveModelPi(dir + File.separator + modelName + piSuffix)) {
			return false;
		}
		
		if (!saveModelPhi0(dir + File.separator + modelName + phi0Suffix)) {
			return false;
		}
		
		if (!saveModelPhi1(dir + File.separator + modelName + phi1Suffix)) {
			return false;
		}
		
		if (topwords > 0) {		
			if (!saveModelTwords(dir + File.separator + modelName + twordsSuffix)) {
				return false;
			}
			
			if (!saveModelVTwords(dir + File.separator + modelName + vtwordsSuffix)) {
				return false;
			}
		}
		
		return true;
	}
	
	//---------------------------------------------------------------
	// Init Methods
	//---------------------------------------------------------------
	
	/**
	 * Initialize the model.
	 */
	protected boolean init(CmdOption option) {		
		if (option == null) {
			return false;
		}
		
		dir = option.dir;
		if (dir.endsWith(File.separator)) {
			dir = dir.substring(0, dir.length() - 1);
		}
		
		modelName = option.modelName;
		T = option.T;
		V = option.V;
		
		alpha = option.alpha;
		if (alpha < 0.0) {
			alpha = 50.0/T;
		}
		
		if (option.beta0 >= 0) {
			beta0 = option.beta0;
		}
		
		if (option.beta1 >= 0) {
			beta1 = option.beta1;
		}
		
		if (option.eta >= 0) {
			eta = option.eta;
		}
		
		nchains = option.nchains;
		niters = option.niters;
		
		dfile = option.dfile;
		topwords = option.topwords;
		
		return true;
	}
	
	/**
	 * Init parameters for estimation.
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public boolean initNewModel(CmdOption option) throws FileNotFoundException, UnsupportedEncodingException{
		if (!init(option)) {
			return false;
		}
		
		int d, m, n, i, j, k;
		pz = new double[T];
		pv = new double[V];
		
		data = Dataset.readDataSet(dir + File.separator + dfile);
		if (data == null) {
			System.out.println("Fail to read training data!\n");
			return false;
		}
		
		// assign values for variables		
		D = data.D;
		W = data.W;
		W0 = data.W0;
		W1 = data.W1;
		dir = option.dir;
		savestep = option.savestep;

		// V, T: from command line or default value
		// alpha, beta0, beta1, eta: from command line or default values
		// nchains, niters, savestep: from command line or default values

		nvz = new double[V][T];
		for (i = 0; i < V; i++) {
			for (j = 0; j < T; j++) {
				nvz[i][j] = 0;
			}
		}
		
		n0zw = new double[T][W];
		for (j = 0; j < T; j++) {
			for (k = 0; k < W; k++) {
				n0zw[j][k] = 0;
			}
		}
		
		n1vzw = new double[V][T][W];
		for (i = 0; i < V; i++) {
			for (j = 0; j < T; j++) {
				for (k = 0; k < W; k++) {
					n1vzw[i][j][k] = 0;
				}
			}
		}
		
		nv = new double[V];
		for (i = 0; i < V; i++) {
			nv[i] = 0;
		}
		
		nvzsum = new double[V];
		for (i = 0; i < V; i++) {
			nvzsum[i] = 0;
		}
		
		n0zwsum = new double[T];
		for (j = 0; j < T; j++) {
			n0zwsum[j] = 0;
		}
		
		n1vzwsum = new double[V][T];
		for (i = 0; i < V; i++) {
			for (j = 0; j < T; j++) {
				n1vzwsum[i][j] = 0;
			}
		}
		
		nvsum = 0;
		
		topicRandomGenerator = new Random();
		viewpointRandomGenerator = new Random();
		
		zAssign = new int[D][];
		vAssign = new int[D];
		
		// initialize vAssign for each document d
		for (d = 0; d < data.D; d++) {
			int M = data.docs[d].length; // number of sentences in document d
			
			// choose a random viewpoint for document d
			int viewpoint = viewpointRandomGenerator.nextInt(V);
			vAssign[d] = viewpoint;
			
			// increment the number of documents assigned to viewpoint
			nv[viewpoint] += 1;
			
			// increment the total number of documents
			nvsum += 1;
			
			zAssign[d] = new int[M];
			
			// initialize zAssign for each sentence m in document d
			for (m = 0; m < M; m++) {
				int N = data.docs[d].sentences[m].length; // number of words in sentence m
				
				// choose a random topic for sentence m
				int topic = topicRandomGenerator.nextInt(T);
				zAssign[d][m] = topic;
				
				for (n = 0; n < N; n++) {
					int word = data.docs[d].sentences[m].words[n];
					int pos = data.docs[d].sentences[m].pos[n];
					
					if (pos == 0) {
						// topical word
						
						// increment the number of topical instances of word assigned to topic
						n0zw[topic][word] += 1;
						
						// increment the total number of topical words assigned to topic
						n0zwsum[topic] += 1;
					} else if (pos == 1) {
						// opinion word
						
						// increment the number of opinion instances of word assigned to viewpoint and topic
						n1vzw[viewpoint][topic][word] += 1;
						
						// increment the total number of opinion words assigned to viewpoint and topic
						n1vzwsum[viewpoint][topic] += 1;
					}
				}
				
				// increment the number of sentences in the collection assigned to viewpoint and topic
				nvz[viewpoint][topic] += 1;
				
				// increment the total number of sentences in the collection assigned to viewpoint
				nvzsum[viewpoint] += 1;
			}
		}
		
		theta = new double[V][T];
		pi = new double[V];
		phi0 = new double[T][W];
		phi1 = new double[V][T][W];
		
		return true;
	}

	/**
	 * Init parameters for inference.
	 * @param newData the dataSet for which we do inference
	 */
	public boolean initNewModel(CmdOption option, Dataset newData, Model trnModel) {
		if (!init(option)) {
			return false;
		}
		
		int d, m, n, i, j, k;
		
		T = trnModel.T;
		V = trnModel.V;
		alpha = trnModel.alpha;
		beta0 = trnModel.beta0;
		beta1 = trnModel.beta1;
		eta = trnModel.eta;
		
		pz = new double[T];
		System.out.println("T:" + T);
		pv = new double[V];
		System.out.println("V:" + V);
		
		data = newData;
		
		// assign values for variables		
		D = data.D;
		W = data.W;
		W0 = data.W0;
		W1 = data.W1;
		dir = option.dir;
		savestep = option.savestep;
		System.out.println("D:" + D);
		System.out.println("W:" + W);
		System.out.println("W0:" + W0);
		System.out.println("W1:" + W1);

		// T: from command line or default value
		// alpha, beta0, beta1, eta: from command line or default values
		// nchains, niters, savestep: from command line or default values

		nvz = new double[V][T];
		for (i = 0; i < V; i++) {
			for (j = 0; j < T; j++) {
				nvz[i][j] = 0;
			}
		}
		
		n0zw = new double[T][W];
		for (j = 0; j < T; j++) {
			for (k = 0; k < W; k++) {
				n0zw[j][k] = 0;
			}
		}
		
		n1vzw = new double[V][T][W];
		for (i = 0; i < V; i++) {
			for (j = 0; j < T; j++) {
				for (k = 0; k < W; k++) {
					n1vzw[i][j][k] = 0;
				}
			}
		}
		
		nv = new double[V];
		for (i = 0; i < V; i++) {
			nv[i] = 0;
		}
		
		nvzsum = new double[V];
		for (i = 0; i < V; i++) {
			nvzsum[i] = 0;
		}
		
		n0zwsum = new double[T];
		for (j = 0; j < T; j++) {
			n0zwsum[j] = 0;
		}
		
		n1vzwsum = new double[V][T];
		for (i = 0; i < V; i++) {
			for (j = 0; j < T; j++) {
				n1vzwsum[i][j] = 0;
			}
		}
		
		nvsum = 0;
		
		topicRandomGenerator = new Random();
		viewpointRandomGenerator = new Random();
		
		zAssign = new int[D][];
		vAssign = new int[D];
		
		// initialize vAssign for each document d
		for (d = 0; d < data.D; d++) {
			int M = data.docs[d].length; // number of sentences
			zAssign[d] = new int[M];
			
			// choose a random viewpoint for document d
			int viewpoint = viewpointRandomGenerator.nextInt(V);
			vAssign[d] = viewpoint;
			
			// increment the number of documents assigned to viewpoint
			nv[viewpoint] += 1;

			// increment the total number of documents
			nvsum += 1;
			
			// initialize zAssign for each sentence m in document d
			for (m = 0; m < M; m++) {
				int N = data.docs[d].sentences[m].length; // number of words in the sentence m
				
				// choose a random topic for sentence m
				int topic = topicRandomGenerator.nextInt(T);
				zAssign[d][m] = topic;
				
				for (n = 0; n < N; n++) {
					int word = data.docs[d].sentences[m].words[n];
					int pos = data.docs[d].sentences[m].pos[n];
					
					if (pos == 0) {
						// topical word
						
						// increment the number of topical instances of word assigned to topic
						n0zw[topic][word] += 1;
						
						// increment the total number of topical words assigned to topic
						n0zwsum[topic] += 1;
					} else if (pos == 1) {
						// opinion word
						
						// increment the number of opinion instances of word assigned to viewpoint and topic
						n1vzw[viewpoint][topic][word] += 1;
						
						// increment the total number of opinion words assigned to viewpoint and topic
						n1vzwsum[viewpoint][topic] += 1;
					}
				}
				
				// increment the number of sentences in the collection assigned to viewpoint and topic
				nvz[viewpoint][topic] += 1;
				
				// increment the total number of sentences in the collection assigned to viewpoint
				nvzsum[viewpoint] += 1;
			}
		}
		
		theta = new double[V][T];
		pi = new double[V];
		phi0 = new double[T][W];
		phi1 = new double[V][T][W];
		
		return true;
	}
	
	/**
	 * Init parameters for inference.
	 * Reading new dataset from file.
	 */
	public boolean initNewModel(CmdOption option, Model trnModel) {
		if (!init(option))
			return false;
		
		Dataset dataset = Dataset.readDataSet(dir + File.separator + dfile, trnModel.data.localDict);
		if (dataset == null) {
			System.out.println("Fail to read dataset!\n");
			return false;
		}
		
		return initNewModel(option, dataset, trnModel);
	}
	
	/**
	 * Init parameters for later inference.
	 */
	public boolean initEstimatedModel(CmdOption option) {
		if (!init(option))
			return false;
		
		int d, m, n, i, j, k;
		
		pz = new double[T];
		pv = new double[V];
		
		// load model, i.e., read z, v and trndata
		if (!loadModel()) {
			System.out.println("Fail to load the assignment file of the model!\n");
			return false;
		}
		
		System.out.println("Model loaded:");
		System.out.println("\talpha:" + alpha);
		System.out.println("\tbeta0:" + beta0);
		System.out.println("\tbeta1:" + beta1);
		System.out.println("\teta:" + eta);
		System.out.println("\tD:" + D);
		System.out.println("\tW:" + W);
		System.out.println("\tW0:" + W0);	
		System.out.println("\tW1:" + W1);	
		
		nvz = new double[V][T];
		for (i = 0; i < V; i++) {
			for (j = 0; j < T; j++) {
				nvz[i][j] = 0;
			}
		}
		
		n0zw = new double[T][W];
		for (j = 0; j < T; j++) {
			for (k = 0; k < W; k++) {
				n0zw[j][k] = 0;
			}
		}
		
		n1vzw = new double[V][T][W];
		for (i = 0; i < V; i++) {
			for (j = 0; j < T; j++) {
				for (k = 0; k < W; k++) {
					n1vzw[i][j][k] = 0;
				}
			}
		}
		
		nv = new double[V];
		for (i = 0; i < V; i++) {
			nv[i] = 0;
		}
		
		nvzsum = new double[V];
		for (i = 0; i < V; i++) {
			nvzsum[i] = 0;
		}
		
		n0zwsum = new double[T];
		for (j = 0; j < T; j++) {
			n0zwsum[j] = 0;
		}
		
		n1vzwsum = new double[V][T];
		for (i = 0; i < V; i++) {
			for (j = 0; j < T; j++) {
				n1vzwsum[i][j] = 0;
			}
		}
		
		nvsum = 0;
		
		// initialize count variables
		for (d = 0; d < data.D; d++) {
			int M = data.docs[d].length; // number of sentences
			
			int viewpoint = vAssign[d];
			
			// increment the number of documents assigned to viewpoint
			nv[viewpoint] += 1;

			// increment the total number of documents
			nvsum += 1;
			
			for (m = 0; m < M; m++) {
				int N = data.docs[d].sentences[m].length; // number of words in the sentence m
				
				int topic = zAssign[d][m];
				
				for (n = 0; n < N; n++) {
					int word = data.docs[d].sentences[m].words[n];
					int pos = data.docs[d].sentences[m].pos[n];
					
					if (pos == 0) {
						// topical word
						
						// increment the number of topical instances of word assigned to topic
						n0zw[topic][word] += 1;
						
						// increment the total number of topical words assigned to topic
						n0zwsum[topic] += 1;
					} else if (pos == 1) {
						// opinion word
						
						// increment the number of opinion instances of word assigned to viewpoint and topic
						n1vzw[viewpoint][topic][word] += 1;
						
						// increment the total number of opinion words assigned to viewpoint and topic
						n1vzwsum[viewpoint][topic] += 1;
					}
				}
				
				// increment the number of sentences in the collection assigned to viewpoint and topic
				nvz[viewpoint][topic] += 1;
				
				// increment the total number of sentences in the collection assigned to viewpoint
				nvzsum[viewpoint] += 1;
			}
		}
	 
		theta = new double[V][T];
		pi = new double[V];
		phi0 = new double[T][W];
		phi1 = new double[V][T][W];
		dir = option.dir;
		savestep = option.savestep;
	 
		return true;
	}
	
	/**
	 * Compute the perplexity of the model
	 */
	public void computePerplexity() {
		double logP = 0;
		
		for (int d = 0; d < D; d++) {
			Document document = data.docs[d];
			double[] logPv = new double[V];
			
			for (int i = 0; i < V; i++) {
				logPv[i] = 0;
				
				for (int m = 0; m < document.length; m++) {
					Sentence sentence = data.docs[d].sentences[m];
					double[] logPz = new double[T];
					
					for (int j = 0; j < T; j++) {
						logPz[j] = 0;
						
						for (int n = 0; n < sentence.length; n++) {
							int word = sentence.words[n];
							int pos = sentence.pos[n];
							
							if (pos == 0) {
								logPz[j] += Math.log(phi0[j][word]);
							} else if (pos == 1) {
								logPz[j] += Math.log(phi1[i][j][word]);
							}
						} // end for each word
						
						logPz[j] += Math.log(theta[i][j]);
					} // end for each topic
					
					logPv[i] += Utils.logSum(logPz);
				} // end for each sentence
				
				logPv[i] += Math.log(pi[i]);
			} // end for each viewpoint
			
			logP += Utils.logSum(logPv);
		} // end for each document
		
		double N = 0;
		
		for (int d = 0; d < D; d++) {
			Document document = data.docs[d];
			for (int m = 0; m < document.length; m++) {
				Sentence sentence = data.docs[d].sentences[m];
				N += sentence.length;
			} // end for each sentence
		} // end for each document
		
		perplexity = Math.exp(-logP/N);
	}
}
