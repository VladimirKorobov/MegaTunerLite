package com.example.megatuner;

import android.graphics.RectF;

public class TuneLayout {

    RectF rect;

    /*
	public float top;
	public float bottom;
	public float right;
	public float left;
	*/
	public float tenthW;
	public float tenthH;
	
	public int red;
	public int green;
	public int blue;
	public int alpha;
	
	public TuneLayout(int r, int g, int b, int a)
	{
		setColor(r, g, b, a);
	}
	
	void setColor(int r, int g, int b, int a)
	{
		red = r;
		green = g;
		blue = b;
		alpha = a;
	}
	void updateLayout(float left, float top, float right, float bottom)
	{
        this.rect = new RectF(left, top, right, bottom);
		this.tenthW = this.rect.width() / 10;
		this.tenthH = this.rect.height() / 10;

	}

}
