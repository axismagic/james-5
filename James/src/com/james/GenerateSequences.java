package com.james;

import java.util.ArrayList;
import java.util.List;

public class GenerateSequences {
	// public static int numOfSeqs = 3;
	public static final int minNum = 1;
	public static final int maxNum = 45;

	public static List<List<Integer>> generateSequences(int numOfSeqs) {
		List<List<Integer>> sequences = new ArrayList<List<Integer>>();

		for (int startNum = minNum; startNum <= maxNum - numOfSeqs + 1; startNum++) {
			List<Integer> nums = new ArrayList<Integer>();
			nums.add(new Integer(startNum));
			for (int i = 1; i < numOfSeqs; i++) {
				nums.add(new Integer(startNum + i));
			}
			sequences.add(nums);
		}
		
		return sequences;
	}

	public static void main(String[] args) {

	}
}
