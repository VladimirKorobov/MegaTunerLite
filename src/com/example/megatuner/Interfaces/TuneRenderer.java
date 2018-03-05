package com.example.megatuner.Interfaces;

import android.graphics.Bitmap;
import com.example.megatuner.TuneSurface;

public interface TuneRenderer {
	
	void setLineWidth(float lineWidth);
	void setColor(int r, int g, int b, int a);
	void drawLine(float x0, float y0, float x1, float y1);
	void drawLines(float x0, float y0, float[] points);
	void drawRect(float x0, float y0, float x1, float y1);
	void drawText(String text, float size, float x, float y);
    void drawBitmap(float x, float y, TuneBitmap bitmap);
	void drawTextBuffer(TuneSurface.TextBuffer buffer, float size, float x, float y);
    void save();
    void restore();
    void scale(float x, float y);
    void scale(float x, float y, float cx, float cy);
    void setClip(float x, float y, float width, float height);
    void clearClip();
    void rotate(float degrees, float xc, float yc);
    void setAntialias(Boolean antiAlias);
    float getTextWidth(String text, float textSize);
    TuneSurface.TextBuffer createTextBuffer();
    TuneBitmap createBitmap(Bitmap bitmap);
    TuneBitmap createBitmap(Bitmap bitmap, int width, int height);
}
