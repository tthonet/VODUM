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

package vodum.utils;

@SuppressWarnings({"rawtypes","unchecked"})
public class Pair implements Comparable<Pair> {
	public Object first;
	public Comparable second;
	public static boolean naturalOrder = false;
	
	public Pair(Object k, Comparable v) {
		first = k;
		second = v;		
	}
	
	public Pair(Object k, Comparable v, boolean naturalOrder) {
		first = k;
		second = v;
		Pair.naturalOrder = naturalOrder; 
	}
	
	public int compareTo(Pair p) {
		if (naturalOrder) {
			return this.second.compareTo(p.second);
		} else {
			return -this.second.compareTo(p.second);
		}
	}
}

