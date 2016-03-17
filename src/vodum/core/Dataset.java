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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Dataset {
	
	//---------------------------------------------------------------
	// Instance Variables
	//---------------------------------------------------------------
	
	public Dictionary localDict; // local dictionary	
	public Document[] docs; // a list of documents	
	public int D; // number of documents
	public int W; // number of words
	public int W0; // number of topical words
	public int W1; // number of opinion words
	public Map<Integer, Map<Integer, Boolean>> wordIdPosMap; // key1=wordId, key2=posCategory, value=isWordPos
	
	// map from local coordinates (id) to global ones 
	// null if the global dictionary is not set
	public Map<Integer, Integer> lid2gid; 
	
	// link to a global dictionary (optional), null for train data, not null for test data
	public Dictionary globalDict;	 		
	
	//--------------------------------------------------------------
	// Constructors
	//--------------------------------------------------------------
	
	public Dataset() {
		localDict = new Dictionary();
		D = 0;
		W = 0;
		W0 = 0;
		W1 = 0;
		docs = null;
		wordIdPosMap = new HashMap<Integer, Map<Integer, Boolean>>();
	
		globalDict = null;
		lid2gid = null;
	}
	
	public Dataset(int M) {
		localDict = new Dictionary();
		this.D = M;
		this.W = 0;
		this.W0 = 0;
		this.W1 = 0;
		docs = new Document[M];	
		wordIdPosMap = new HashMap<Integer, Map<Integer, Boolean>>();
		
		globalDict = null;
		lid2gid = null;
	}
	
	public Dataset(int M, Dictionary globalDict) {
		localDict = new Dictionary();	
		this.D = M;
		this.W = 0;
		this.W0 = 0;
		this.W1 = 0;
		docs = new Document[M];
		wordIdPosMap = new HashMap<Integer, Map<Integer, Boolean>>();
		
		this.globalDict = globalDict;
		lid2gid = new HashMap<Integer, Integer>();
	}
	
	//-------------------------------------------------------------
	// Public Instance Methods
	//-------------------------------------------------------------
	
	/**
	 * Set the document at the index idx if idx is greater than 0 and less than D.
	 * @param doc document to be set
	 * @param idx index in the document array
	 */	
	public void setDoc(Document doc, int idx) {
		if (0 <= idx && idx < D) {
			docs[idx] = doc;
		}
	}
	
	/**
	 * Set the document at the index idx if idx is greater than 0 and less than D.
	 * @param str string contains doc
	 * @param idx index in the document array
	 */
	public void setDoc(String str, int idx) {
		if (0 <= idx && idx < D) {
			String[] sentences = str.split("[|]"); // each sentence is separated by a |
			
			Vector<Sentence> sents = new Vector<Sentence>();
			
			for (String sentence : sentences) {
				String[] posWords = sentence.split("[ \\t\\n]");

				Vector<Integer> ids = new Vector<Integer>();
				Vector<Integer> pos = new Vector<Integer>();

				for (String posWord : posWords) {
					String[] posWordSplit = posWord.split("[:]");
					String word = posWordSplit[0];
					int _pos = Integer.parseInt(posWordSplit[1]);
					
					int _id = localDict.word2id.size();

					if (localDict.contains(word)) {		
						_id = localDict.getID(word);
					} else {
						// first time this word occurs at all
						Map<Integer, Boolean> currentWordPosMap = new HashMap<Integer, Boolean>();
						currentWordPosMap.put(0, false); // word with pos category 0 hasn't been seen yet
						currentWordPosMap.put(1, false); // word with pos category 1 hasn't been seen yet
						wordIdPosMap.put(_id, currentWordPosMap);
					}

					if (globalDict != null) {
						// get the global id
						Integer id = globalDict.getID(word);

						if (id != null) {
							localDict.addWord(word);

							lid2gid.put(_id, id);
							ids.add(_id);
							pos.add(_pos);
							
							// updating W0/W1
							if (_pos == 0 && !wordIdPosMap.get(_id).get(0)) {
								// first time this word occurs as a topical word
								W0++;
								
								// word with pos category 0 has now been seen
								wordIdPosMap.get(_id).put(0, true);
							} else if (_pos == 1 && !wordIdPosMap.get(_id).get(1)) {
								// first time this word occurs as an opinion word
								W1++;
								
								// word with pos category 1 has now been seen
								wordIdPosMap.get(_id).put(1, true);
							}
						} else { //not in global dictionary
							// the word will not be considered in the model, remove it
							wordIdPosMap.remove(_id);
						}
					} else {
						localDict.addWord(word);
						ids.add(_id);
						pos.add(_pos);
						
						// updating W0/W1
						if (_pos == 0 && !wordIdPosMap.get(_id).get(0)) {
							// first time this word occurs as a topical word
							W0++;
							
							// word with pos category 0 has now been seen
							wordIdPosMap.get(_id).put(0, true);
						} else if (_pos == 1 && !wordIdPosMap.get(_id).get(1)) {
							// first time this word occurs as an opinion word
							W1++;
							
							// word with pos category 1 has now been seen
							wordIdPosMap.get(_id).put(1, true);
						}
					}
				}

				Sentence sent = new Sentence(ids, pos, sentence);
				sents.add(sent);
			}
			
			Document doc = new Document(sents, str);
			docs[idx] = doc;
			W = localDict.word2id.size();
		}
	}
	
	//---------------------------------------------------------------
	// I/O methods
	//---------------------------------------------------------------
	
	/**
	 * Read a dataset from a stream, create new dictionary.
	 * @return dataset if success and null otherwise
	 */
	public static Dataset readDataSet(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));
			Dataset data = readDataSet(reader);
			reader.close();
			
			return data;
		} catch (Exception e) {
			System.out.println("Read Dataset Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Read a dataset from a file with a preknown vocabulary.
	 * @param filename file from which we read dataset
	 * @param dict the dictionary
	 * @return dataset if success and null otherwise
	 */
	public static Dataset readDataSet(String filename, Dictionary dict) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));
			Dataset data = readDataSet(reader, dict);
			reader.close();
			
			return data;
		} catch (Exception e) {
			System.out.println("Read Dataset Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Read a dataset from a stream, create new dictionary.
	 * @return dataset if success and null otherwise
	 */
	public static Dataset readDataSet(BufferedReader reader) {
		try {
			// read number of documents
			String line;
			line = reader.readLine();
			int D = Integer.parseInt(line);
			
			Dataset data = new Dataset(D);
			for (int d = 0; d < D; ++d) {
				line = reader.readLine();
				
				data.setDoc(line, d);
			}
			
			return data;
		} catch (Exception e) {
			System.out.println("Read Dataset Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Read a dataset from a stream with respect to a specified dictionary.
	 * @param reader stream from which we read dataset
	 * @param dict the dictionary
	 * @return dataset if success and null otherwise
	 */
	public static Dataset readDataSet(BufferedReader reader, Dictionary dict) {
		try {
			// read number of document
			String line;
			line = reader.readLine();
			int D = Integer.parseInt(line);
			
			Dataset data = new Dataset(D, dict);
			for (int d = 0; d < D; ++d) {
				line = reader.readLine();
				
				data.setDoc(line, d);
			}
			
			return data;
		} catch (Exception e) {
			System.out.println("Read Dataset Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Read a dataset from a string, create new dictionary.
	 * @param str String from which we get the dataset, documents are seperated by newline character 
	 * @return dataset if success and null otherwise
	 */
	public static Dataset readDataSet(String[] strs) {
		Dataset data = new Dataset(strs.length);
		
		for (int d = 0 ; d < strs.length; ++d) {
			data.setDoc(strs[d], d);
		}
		
		return data;
	}
	
	/**
	 * Read a dataset from a string with respect to a specified dictionary.
	 * @param str String from which we get the dataset, documents are seperated by newline character	
	 * @param dict the dictionary
	 * @return dataset if success and null otherwise
	 */
	public static Dataset readDataSet(String[] strs, Dictionary dict) {
		Dataset data = new Dataset(strs.length, dict);
		
		for (int d = 0 ; d < strs.length; ++d) {
			data.setDoc(strs[d], d);
		}
		
		return data;
	}
}
