package com.example.megatuner;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Bitmap;

public class BitmapContainer {
	List<Bitmap> bitmaps = new ArrayList<Bitmap>();
	Point2DContainer points = new Point2DContainer();
	
	public void addBitmap(float x, float y, Bitmap bitmap)
	{
		points.addPoint(x, y);
		bitmaps.add(bitmap);
	}
	public float[] getPoints()
	{
		return points.getPoints();
	}
	public Bitmap[] getBitmaps()
	{
		Bitmap[] bitmapsArray = new Bitmap[bitmaps.size()];
		bitmaps.toArray(bitmapsArray);
		return bitmapsArray;
	}
	
	public void clear()
	{
		bitmaps.clear();
		points.clear();
	}
}
