package com.james;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SumOfIntegers {

	public static int sum(List<Integer> a) {
		int sum = 0;
		for (Integer b : a) {
			sum += b;
		}
		return sum;
	}

	public static void main(String[] args) {
		List<Integer> demoList = new ArrayList<Integer>();
		Integer[] test = {2,3,4,5};
		demoList.addAll(Arrays.asList(test));

		System.out.println(sum(demoList));
	}
}
