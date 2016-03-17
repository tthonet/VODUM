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

import org.kohsuke.args4j.*;

public class VODUM {
	
	public static void main(String args[]) {
		CmdOption option = new CmdOption();
		CmdLineParser parser = new CmdLineParser(option);
		
		try {
			if (args.length == 0) {
				showHelp(parser);
				return;
			}
			
			parser.parseArgument(args);
			
			if (option.est) {
				Estimator estimator = new Estimator();
				estimator.init(option);
				estimator.estimate();
			} else if (option.inf) {
				Inferencer inferencer = new Inferencer();
				inferencer.init(option);
				inferencer.inference();
			}
		} catch (CmdLineException cle) {
			System.out.println("Command line error: " + cle.getMessage());
			showHelp(parser);
			return;
		} catch (Exception e) {
			System.out.println("Error in main: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	public static void showHelp(CmdLineParser parser) {
		System.out.println("VODUM [options...] [arguments...]");
		parser.printUsage(System.out);
	}
	
}
