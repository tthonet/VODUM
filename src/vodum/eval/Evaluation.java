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

package vodum.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

public class Evaluation {
	
	private Map<String, List<Integer>> assignMap; // key = sample name, value = per document viewpoint assignment
	private Map<String, Double> perplexityMap; // key = sample name, value = perplexity
	private List<Integer> groundTruth; // list of the ground truth per document viewpoint assignments
	private PrintWriter writer; // log file writer
	private double docCount; // number of documents in the collection
	
	public Evaluation(PrintWriter writer) {
		assignMap = new LinkedHashMap<String, List<Integer>>();
		perplexityMap = new LinkedHashMap<String, Double>();
		groundTruth = new ArrayList<Integer>();
		this.writer = writer;
		docCount = 0;
	}
	
	/**
	 * Load viewpoint assignments and perplexity of the samples, and save them into
	 * assignMap and perplexityMap.
	 */
	public void loadSamples(String sampleDirPath) throws IOException {
		File sampleDir = new File(sampleDirPath);
		File[] sampleFiles = sampleDir.listFiles();
		
		boolean firstIteration = true;

		for (File sampleFile : sampleFiles) {
			String sampleFileName = FilenameUtils.getBaseName(sampleFile.getName());
			String sampleFileExtension = FilenameUtils.getExtension(sampleFile.getName());

			if (sampleFileExtension.equals("assign")) {
				// this file contains the assignments
				BufferedReader assignBr = new BufferedReader(new FileReader(sampleFile));

				List<Integer> sampleAssignList = new ArrayList<Integer>();

				try {
					String assignLine = assignBr.readLine();

					while (assignLine != null) {
						int viewpoint = Integer.parseInt(assignLine.split("[| ]")[0]); // the viewpoint is the first character of the line, separated from the rest of the line with |

						sampleAssignList.add(viewpoint);

						assignLine = assignBr.readLine();

						if (firstIteration) {
							docCount++;
						}
					}

					if (firstIteration) {
						firstIteration = false; // first iteration is finished
					}
				} finally {
					assignBr.close();
				}

				assignMap.put(sampleFileName, sampleAssignList);
			} else if (sampleFileExtension.equals("others")) {
				// this file contains the perplexity score of the sample
				BufferedReader othersBr = new BufferedReader(new FileReader(sampleFile));

				try {
					String othersLine = othersBr.readLine();

					while (othersLine != null) {
						String[] othersLineArray = othersLine.split("[=]"); // variables' name and value are separated by =

						if (othersLineArray[0].equals("perplexity")) {
							// this line contains the perplexity of the sample
							double perplexity = Double.parseDouble(othersLineArray[1]);
							perplexityMap.put(sampleFileName, perplexity);
						}

						othersLine = othersBr.readLine();
					}
				} finally {
					othersBr.close();
				}
			}
		}
	}
	
	/**
	 * Load the per document viewpoint assignments from the ground truth file
	 */
	public void loadGroundTruth(String groundTruthFilePath) throws IOException {
		BufferedReader groundTruthBr = new BufferedReader(new FileReader(groundTruthFilePath));
		
		try {
	        String groundTruthLine = groundTruthBr.readLine();

	        while (groundTruthLine != null) {
        		int groundTruthViewpoint = Integer.parseInt(groundTruthLine);
        		
        		groundTruth.add(groundTruthViewpoint);
        		
        		groundTruthLine = groundTruthBr.readLine();
	        }
	    } finally {
	    	groundTruthBr.close();
	    }
	}
	
	/**
	 * Evaluate the average accuracy on per document viewpoint assignments
	 * for all samples.
	 */
	public void evaluateAll(boolean verbose) {
		
		double perplexitySum = 0;
		double accuracySum = 0;
		double sampleCount = 0;
		double perplexityMin = Double.POSITIVE_INFINITY;
		double perplexityMax = 0;
		double accuracyMin = 1;
		double accuracyMax = 0;
		
		for (String sampleName : assignMap.keySet()) {
			if (verbose) {
				writer.print(sampleName + ": ");
			}
			
			// evaluating perplexity
			double samplePerplexity = perplexityMap.get(sampleName);
			if (samplePerplexity > perplexityMax) {
				perplexityMax = samplePerplexity;
			}
			if (samplePerplexity < perplexityMin) {
				perplexityMin = samplePerplexity;
			}
			perplexitySum += samplePerplexity;
			if (verbose) {
				writer.print("perplexity: " + samplePerplexity + "; ");
			}
			
			// evaluating accuracy
			double sampleAccuracy = evaluate(sampleName);
			if (sampleAccuracy > accuracyMax) {
				accuracyMax = sampleAccuracy;
			}
			if (sampleAccuracy < accuracyMin) {
				accuracyMin = sampleAccuracy;
			}
			accuracySum += sampleAccuracy;
			if (verbose) {
				writer.println("accuracy: " + sampleAccuracy);
			}
			
			sampleCount++;
		}
		
		writer.println("Minimal Perplexity: " + perplexityMin);
		writer.println("Maximal Perplexity: " + perplexityMax);
		writer.println("Average Perplexity: " + perplexitySum/sampleCount);
		writer.println("Minimal Accuracy: " + accuracyMin);
		writer.println("Maximal Accuracy: " + accuracyMax);
		writer.println("Average Accuracy: " + accuracySum/sampleCount);
	}
	
	/**
	 * Evaluate the accuracy on per document viewpoint assignments
	 * for a given sample.
	 */
	public double evaluate(String sampleName) {
		
		double accuracy = 0;

		List<Integer> sampleAssignList = assignMap.get(sampleName);

		double difference = 0;

		for (int i = 0; i < sampleAssignList.size(); i++) {
			int trueViewpoint = groundTruth.get(i);
			int modelViewpoint = sampleAssignList.get(i);
			difference += (trueViewpoint == modelViewpoint ? 0 : 1);
		}

		accuracy = Math.max(difference, docCount - difference)/docCount;
		
		return accuracy;
	}

	public static void main(String[] args) throws IOException {
		String groundTruthFilePath = args[0];
		String sampleDirPath = args[1];
		String logFilePath = args[2];
		boolean verbose = true;
		
		File logFile = new File(logFilePath);
		if (logFile.getParentFile() != null) {
			logFile.getParentFile().mkdirs();
		}
		logFile.createNewFile();
		
		PrintWriter writer = new PrintWriter(logFile, "UTF-8");
		
		Evaluation sampleSummarizer = new Evaluation(writer);
		sampleSummarizer.loadSamples(sampleDirPath);
		sampleSummarizer.loadGroundTruth(groundTruthFilePath);
		sampleSummarizer.evaluateAll(verbose);
		
		writer.close();
	}
}
