package com.example.megatuner.draw2DPackage;


import java.util.ArrayList;

import android.graphics.*;
import com.example.megatuner.Interfaces.TuneBitmap;
import com.example.megatuner.Point2DContainer;
import com.example.megatuner.Interfaces.TuneRenderer;
import com.example.megatuner.TuneSurface.TextBuffer;
import com.example.megatuner.TuneSurface;

import android.content.Context;

public class TuneRenderer2D implements TuneRenderer {

    private static float nativeTextSize = 50;

	private class TextBuffer2D  implements TuneSurface.TextBuffer
	{
		Point2DContainer textPoints = new Point2DContainer();
		//List<String> textString = new ArrayList<String>();
        private ArrayList<Path> textPaths = new ArrayList<Path>();

		

		@Override
		public void addText(String text, float x, float y) {
			textPoints.addPoint(x,  y);
			//textString.add(text);
            Path path = new Path();
            paint.setTextSize(nativeTextSize);
            paint.getTextPath(text, 0, text.length(), 0, 0, path);
            path.close();
            textPaths.add(path);
		}

		@Override
		public void release() {
            textPaths.clear();
			textPoints.clear();
			//textString.clear();
		}
		
	}
	Context context;
	Paint paint = new Paint();
	Canvas canvas;

	public TuneRenderer2D(Context context)
	{
		this.context = context;
	}
	@Override
	public void setColor(int r, int g, int b, int a) {
		paint.setColor((a << 24) + (r << 16) + (g << 8) + (b));

	}

	@Override
	public void drawLine(float x0, float y0, float x1, float y1) {
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawLine(x0, y0, x1, y1, paint);
	}

	@Override
	public void drawLines(float x0, float y0, float[] points) {
		paint.setStyle(Paint.Style.STROKE);
		for(int i = 0; i < points.length; i += 4)
		{
			canvas.drawLine(x0 + points[i], y0 + points[i + 1], x0 + points[i + 2], y0 + points[i + 3], paint);
		}
	}

	@Override
	public void drawRect(float x0, float y0, float x1, float y1) {
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(x0, y0, x1, y1, paint);
	}

	@Override
	public void drawText(String text, float size, float x, float y) {
		paint.setStyle(Paint.Style.FILL);
		paint.setTextSize(size);
		canvas.drawText(text, x, y, paint);
	}

    @Override
    public void drawBitmap(float x, float y, TuneBitmap bitmap) {
        canvas.drawBitmap(((TuneBitmap2D)bitmap).bitmap, x, y, null);
    }

    @Override
	public TextBuffer createTextBuffer() {
		return new TextBuffer2D();
	}

    @Override
    public TuneBitmap createBitmap(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
        return new TuneBitmap2D(newBitmap);
    }

    @Override
    public TuneBitmap createBitmap(Bitmap bitmap, int width, int height) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return new TuneBitmap2D(newBitmap);
    }

    @Override
	public void setLineWidth(float lineWidth) {
		paint.setStrokeWidth(lineWidth);
	}
	@Override
	public void drawTextBuffer(TextBuffer buffer, float size, float x, float y) {
		
		TextBuffer2D buffer2D = (TextBuffer2D)buffer;
		
		//paint.setTextSize(size);
		paint.setStyle(Paint.Style.FILL);

        float scale = size / nativeTextSize;
        float[] coord = buffer2D.textPoints.getPoints();

        for(int i = 0, j = 0; i < buffer2D.textPaths.size(); i ++, j += 2)
        {
            canvas.save();
            Path path = buffer2D.textPaths.get(i);


            canvas.translate(x + coord[j], y + coord[j + 1]);
            canvas.scale(scale, scale);


            canvas.drawPath(path, paint);
            canvas.restore();
        }

        /*
		float[] coord = buffer2D.textPoints.getPoints(); 
		
		for(int i = 0, j = 0; i < buffer2D.textString.size(); i ++, j += 2)
		{
			canvas.drawText(buffer2D.textString.get(i), x + coord[j], y + coord[j + 1], paint);
		}
		*/
		
	}

    @Override
    public void save() {
        canvas.save();
    }

    @Override
    public void restore() {
        canvas.restore();
    }

    @Override
    public void scale(float x, float y) {
        canvas.scale(x, y);
    }

    @Override
    public void scale(float x, float y, float cx, float cy) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setClip(float x, float y, float width, float height) {
        canvas.clipRect(x, y, x + width - 1, y + height - 1);
    }

    @Override
    public void clearClip() {
        canvas.clipRect((RectF)null);
    }

    @Override
    public void rotate(float degrees, float xc, float yc) {
        canvas.rotate(degrees, xc, yc);
    }

    @Override
    public void setAntialias(Boolean antiAlias) {
        paint.setAntiAlias(antiAlias);
    }

    @Override
    public float getTextWidth(String text, float textSize) {
        paint.setTextSize(textSize);
        return paint.measureText(text);
    }

}
