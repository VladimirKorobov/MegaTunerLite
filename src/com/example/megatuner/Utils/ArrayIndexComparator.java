package com.example.megatuner.Utils;

import java.util.Comparator;

public class ArrayIndexComparator implements Comparator<Integer> {

	private float[] floatArray;
	public ArrayIndexComparator(float[] floatArray)
	{
		this.floatArray = floatArray;
	}
	@Override
	public int compare(Integer lhs, Integer rhs) 
	{
		return  floatArray[lhs] > floatArray[rhs] ? -1 : floatArray[lhs] == floatArray[rhs] ? 0 : 1;  
	}
}
