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

package vodum.utils;

public class Utils {
	
	/**
	 * Pad an integer with zeroes to put it into the desired width.
	 * @param number the number to pad
	 * @param width the width of the padded number
	 * @return the string of the padded number
	 */
	public static String zeroPad(int number, int width) {
	      StringBuffer result = new StringBuffer("");
	      for (int i = 0; i < width - Integer.toString(number).length(); i++) {
	         result.append("0");
	      }
	      result.append(Integer.toString(number));
	     
	      return result.toString();
	}
	
	/**
	 * Compute the log of the sum of a array of doubles expressed in the log space.
	 * 
	 * @param y array of logs, such that y[i] = log(x[i])
	 * @return the double log(x[0] + ... + x[n-1]) (= yMax + log(exp(y[0] - yMax) + ... + exp(y[n-1] - yMax)))
	 */
	public static double logSum(double[] y) {
		double yMax = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < y.length; i++) {
			if (y[i] > yMax) {
				yMax = y[i];
			}
		}
		
		double logArg = 0;
		for (int i = 0; i < y.length; i++) {
			logArg += Math.exp(y[i] - yMax);
		}
		
		return yMax + Math.log(logArg);
	}
}
