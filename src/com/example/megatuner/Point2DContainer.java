package com.example.megatuner;

import java.util.ArrayList;
import java.util.List;

public class Point2DContainer {
	protected List<Float> pointList = new ArrayList<Float>();
	public void addPoint(float x0, float y0)
	{
		pointList.add(x0);
		pointList.add(y0);
	}
	
	public void addPoints(float[] points)
	{
		for(int i = 0; i < points.length; i ++)
			pointList.add(points[i]);
	}
	public float[] getPoints()
	{
		Object[] pointsObj = pointList.toArray();
		float[] res = new float[pointsObj.length];
		for(int i = 0; i < res.length; i ++)
		{
			res[i] = ((Float)pointsObj[i]).floatValue();
		}
		return res;
	}
	public void clear()
	{
		pointList.clear();
	}

}
