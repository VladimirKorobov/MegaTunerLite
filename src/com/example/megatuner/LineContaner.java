package com.example.megatuner;



public class LineContaner extends Point2DContainer {
	public void addLine(float x0, float y0, float x1, float y1)
	{
		addPoint(x0, y0);
		addPoint(x1, y1);
	}
}
