package main;

import summarization.kwplus.SummarizationKWPlus;
import util.Parameter;

public class SummarizationKWPlusMain {
	
	public static String DATASET, RESULT;
	
	public static void main(String[] args) {
		
		if (args.length != 6) {
			System.out.println("kw+ : error args");
			System.exit(0);
		}

		Parameter.W = Integer.parseInt(args[1]);
		Parameter.L = Parameter.W / 100;
		Parameter.T = Parameter.W / 10000;
		
		Parameter.d = Integer.parseInt(args[2]);
		Parameter.c = Double.parseDouble(args[3]);
		
		Parameter.EPSILON = Double.parseDouble(args[4]);
		Parameter.BETA = Double.parseDouble(args[5]);

		DATASET = args[0];
		RESULT = args[0] + "-" + "KWPLUS" + "-" + Parameter.W + "-" + Parameter.d + "-" + Parameter.c + "-" + Parameter.EPSILON + "-" + Parameter.BETA + ".csv";

		Parameter.BASE = 1 + Parameter.d;
		Parameter.LOG_BASE = 1.0d + Parameter.EPSILON;
		
		SummarizationKWPlus windowOpt = new SummarizationKWPlus();
		windowOpt.streaming();
	}

}