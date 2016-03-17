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

import java.util.Vector;

public class Document {

	//----------------------------------------------------
	//Instance Variables
	//----------------------------------------------------
	public Sentence[] sentences;
	public String rawStr;
	public int length;
	
	//----------------------------------------------------
	//Constructors
	//----------------------------------------------------
	public Document() {
		sentences = null;
		rawStr = "";
		length = 0;
	}
	
	public Document(int length) {
		this.length = length;
		rawStr = "";
		sentences = new Sentence[length];
	}
	
	public Document(int length, Sentence[] sentences) {
		this.length = length;
		rawStr = "";
		
		this.sentences = new Sentence[length];
		for (int i = 0; i < length; ++i) {
			this.sentences[i] = sentences[i];
		}
	}
	
	public Document(int length, Sentence[] sentences, String rawStr) {
		this.length = length;
		this.rawStr = rawStr;
		
		this.sentences = new Sentence[length];
		for (int i = 0; i < length; ++i) {
			this.sentences[i] = sentences[i];
		}
	}
	
	public Document(Vector<Sentence> doc) {
		this.length = doc.size();
		rawStr = "";
		this.sentences = new Sentence[length];
		for (int i = 0; i < length; i++) {
			this.sentences[i] = doc.get(i);
		}
	}
	
	public Document(Vector<Sentence> doc, String rawStr) {
		this.length = doc.size();
		this.rawStr = rawStr;
		this.sentences = new Sentence[length];
		for (int i = 0; i < length; ++i) {
			this.sentences[i] = doc.get(i);
		}
	}
}
