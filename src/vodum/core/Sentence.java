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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Sentence {
	
	//----------------------------------------------------
	// Instance Variables
	//----------------------------------------------------
	
	public int [] words;
	public int [] pos; // part-of-speech category (0 or 1) of the corresponding words
	public String rawStr;
	public int length;
	public Map<Integer, Integer> n0Map; // topical word count map, key=wordId, value=wordCount
	public Map<Integer, Integer> n1Map; // opinion word count map, key=wordId, value=wordCount
	
	//----------------------------------------------------
	// Constructors
	//----------------------------------------------------
	
	public Sentence(){
		length = 0;
		rawStr = "";
		words = null;
		pos = null;
		n0Map = null;
		n1Map = null;
	}
	
	public Sentence(int length){
		this.length = length;
		rawStr = "";
		words = new int[length];
		pos = new int[length];
		n0Map = null;
		n1Map = null;
	}
	
	public Sentence(int length, int [] words, int [] pos){
		this.length = length;
		rawStr = "";
		this.words = new int[length];
		this.pos = new int[length];
		n0Map = new HashMap<Integer, Integer>();
		n1Map = new HashMap<Integer, Integer>();
		
		for (int i = 0 ; i < length; ++i){
			this.words[i] = words[i];
			this.pos[i] = pos[i];
			if (this.pos[i] == 0) {
				if (!n0Map.containsKey(words[i])) {
					// first time seeing this word in the sentence
					n0Map.put(words[i], 1);
				} else {
					// this word has already been seen
					n0Map.put(words[i], n0Map.get(words[i]) + 1);
				}
			} else if (this.pos[i] == 1) {
				if (!n1Map.containsKey(words[i])) {
					// first time seeing this word in the sentence
					n1Map.put(words[i], 1);
				} else {
					// this word has already been seen
					n1Map.put(words[i], n1Map.get(words[i]) + 1);
				}
			} 
		}
	}
	
	public Sentence(int length, int [] words, int [] pos, String rawStr){
		this.length = length;
		this.rawStr = rawStr;
		this.words = new int[length];
		this.pos = new int[length];
		n0Map = new HashMap<Integer, Integer>();
		n1Map = new HashMap<Integer, Integer>();
		
		for (int i =0 ; i < length; ++i){
			this.words[i] = words[i];
			this.pos[i] = pos[i];
			if (this.pos[i] == 0) {
				if (!n0Map.containsKey(this.words[i])) {
					// first time seeing this word in the sentence
					n0Map.put(this.words[i], 1);
				} else {
					// this word has already been seen
					n0Map.put(this.words[i], n0Map.get(this.words[i]) + 1);
				}
			} else if (this.pos[i] == 1) {
				if (!n1Map.containsKey(this.words[i])) {
					// first time seeing this word in the sentence
					n1Map.put(this.words[i], 1);
				} else {
					// this word has already been seen
					n1Map.put(this.words[i], n1Map.get(this.words[i]) + 1);
				}
			} 
		}
	}
	
	public Sentence(Vector<Integer> words, Vector<Integer> pos){
		this.length = words.size();
		rawStr = "";
		this.words = new int[length];
		this.pos = new int[length];
		n0Map = new HashMap<Integer, Integer>();
		n1Map = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < length; i++){
			this.words[i] = words.get(i);
			this.pos[i] = pos.get(i);
			if (pos.get(i) == 0) {
				if (!n0Map.containsKey(this.words[i])) {
					// first time seeing this word in the sentence
					n0Map.put(this.words[i], 1);
				} else {
					// this word has already been seen
					n0Map.put(this.words[i], n0Map.get(this.words[i]) + 1);
				}
			} else if (pos.get(i) == 1) {
				if (!n1Map.containsKey(this.words[i])) {
					// first time seeing this word in the sentence
					n1Map.put(this.words[i], 1);
				} else {
					// this word has already been seen
					n1Map.put(this.words[i], n1Map.get(this.words[i]) + 1);
				}
			} 
		}
	}
	
	public Sentence(Vector<Integer> words, Vector<Integer> pos, String rawStr){
		this.length = words.size();
		this.rawStr = rawStr;
		this.words = new int[length];
		this.pos = new int[length];
		n0Map = new HashMap<Integer, Integer>();
		n1Map = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < length; ++i){
			this.words[i] = words.get(i);
			this.pos[i] = pos.get(i);
			if (this.pos[i] == 0) {
				if (!n0Map.containsKey(this.words[i])) {
					// first time seeing this word in the sentence
					n0Map.put(this.words[i], 1);
				} else {
					// this word has already been seen
					n0Map.put(this.words[i], n0Map.get(this.words[i]) + 1);
				}
			} else if (this.pos[i] == 1) {
				if (!n1Map.containsKey(this.words[i])) {
					// first time seeing this word in the sentence
					n1Map.put(this.words[i], 1);
				} else {
					// this word has already been seen
					n1Map.put(this.words[i], n1Map.get(this.words[i]) + 1);
				}
			} 
		}
	}
}
