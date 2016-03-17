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

public class CmdOption {
	
	@Option(name="-est", usage="Specify whether we want to estimate the model from scratch")
	public boolean est = false;
	
	@Option(name="-inf", usage="Specify whether we want to do inference")
	public boolean inf = true;
	
	@Option(name="-dir", usage="Specify directory")
	public String dir = "";
	
	@Option(name="-dfile", usage="Specify data file")
	public String dfile = "";
	
	@Option(name="-model", usage="Specify the model name")
	public String modelName = "";
	
	@Option(name="-alpha", usage="Specify alpha")
	public double alpha = -1.0;
	
	@Option(name="-beta0", usage="Specify beta0")
	public double beta0 = -1.0;
	
	@Option(name="-beta1", usage="Specify beta1")
	public double beta1 = -1.0;
	
	@Option(name="-eta", usage="Specify eta")
	public double eta = -1.0;
	
	@Option(name="-ntopics", usage="Specify the number of topics")
	public int T = 100;
	
	@Option(name="-nviews", usage="Specify the number of viewpoints")
	public int V = 2;
	
	@Option(name="-nchains", usage="Specify the number of chains")
	public int nchains = 1;
	
	@Option(name="-niters", usage="Specify the number of iterations per chain")
	public int niters = 1000;
	
	@Option(name="-savestep", usage="Specify the number of steps to save the model since the last save")
	public int savestep = 100;
	
	@Option(name="-topwords", usage="Specify the number of most likely (top) words to be printed")
	public int topwords = 100;
	
	@Option(name="-withrawdata", usage="Specify whether we include raw data in the input")
	public boolean withrawdata = false;
}
