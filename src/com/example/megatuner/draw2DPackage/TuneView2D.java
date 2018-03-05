package com.example.megatuner.draw2DPackage;


import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.example.megatuner.Interfaces.TuneRenderer;
import com.example.megatuner.TuneSurface;
import com.example.megatuner.Interfaces.TuneView;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;


public class TuneView2D extends SurfaceView implements TuneView, SurfaceHolder.Callback{

	Context context;
	TuneRenderer2D renderer;
	TuneSurface surface;
	
	float mPreviousX0 = 0;
	float mPreviousY0 = 0;
	
	float mPreviousX1 = 0;
	float mPreviousY1 = 0;
	
	private static final int INVALID_POINTER_ID = -1;
	// Active pointerID
	int mPointer0ID = INVALID_POINTER_ID; 
	// Second pointerID
	int mPointer1ID = INVALID_POINTER_ID;

    private DrawThread2D drawThread;

	public TuneView2D(TuneSurface surface, Context context)
	{
		super(context);
		this.surface = surface;  
		renderer = new TuneRenderer2D(context);
        getHolder().addCallback(this);
    }
	@Override
	public void invalidateRender() {
		//invalidate();
        //if(drawThread != null)
        //    drawThread.drawFlag = true;

        Canvas canvas = null;
        SurfaceHolder surfaceHolder = null;
        try {
            surfaceHolder = getHolder();
            if(surfaceHolder != null) {
                canvas = surfaceHolder.lockCanvas(null);
                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        onDraw(canvas);
                    }
                }
            }
        } catch (Exception e) {
            // если не получилось, то будем пытаться еще и еще
        }
        finally
        {
            if(canvas != null)
                surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

	@Override
	public TuneRenderer getRenderer() {
		return renderer;
	}

    @Override
    public void Cleanup() {
        /*
        if(!drawThread.isInterrupted())
        {
            boolean retry = true;
            // завершаем работу потока
            drawThread.setRunning(false);
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // если не получилось, то будем пытаться еще и еще
                }
            }
        }
        */
    }

    @Override
	protected void onDraw(Canvas canvas)
	{
        if(renderer != null) {
            renderer.canvas = canvas;
            surface.draw();
        }
		
	}
	@Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld)
    {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		surface.updateLayouts(xNew, yNew);
    }

	@Override
	public boolean onTouchEvent(MotionEvent m) {
		

		float x0 = m.getX();
		float y0 = m.getY();
		
	    switch (m.getActionMasked()) {
	    	case MotionEvent.ACTION_DOWN:
	    		mPointer0ID = m.getPointerId(0);
	    		mPreviousX0 = x0;
	    		mPreviousY0 = y0;
                this.surface.TouchDownProcessing(x0, y0);
	    		break;
	    	case MotionEvent.ACTION_POINTER_DOWN:
	    		if(mPointer1ID == INVALID_POINTER_ID)
	    		{
	    			int index = m.getActionIndex();
		    		mPointer1ID = m.getPointerId(index);
		    		mPreviousX1 = m.getX(index);
		    		mPreviousY1 = m.getY(index);
	    		}
	    		break;

	    	case MotionEvent.ACTION_MOVE:
	    		if(mPointer0ID != INVALID_POINTER_ID)
	    		{
		    		float dx0 = x0 - mPreviousX0;
		    		float dy0 = y0 - mPreviousY0;
		    		
		            if(mPointer1ID != INVALID_POINTER_ID)
		            {
		            	int index = m.findPointerIndex(mPointer1ID);
		            	if(index >= 0)
		            	{
			            	float x1 = m.getX(index);
			            	float y1 = m.getY(index);

                            this.surface.TouchScaleProcessing(
                                    mPreviousX0,
                                    dx0,
                                    mPreviousY0,
                                    dy0,
                                    mPreviousX1,
                                    x1 - mPreviousX1,
                                    mPreviousY1,
                                    y1 - mPreviousY1);

		            		mPreviousX1 = x1;
		            		mPreviousY1 = y1;
		            	}
		            }
		            else
		            {
		            	this.surface.TouchMoveProcessing(x0, y0, dx0,  dy0);
		            }
		            mPreviousX0 = x0;
	        		mPreviousY0 = y0;
	    		}
	            break;
	    	case MotionEvent.ACTION_UP:
	    		mPointer0ID = INVALID_POINTER_ID;
                this.surface.TouchUpProcessing(x0, y0);
                break;
	    	case MotionEvent.ACTION_POINTER_UP:
	    		if(mPointer1ID != INVALID_POINTER_ID)
	            {
	    			mPointer1ID = INVALID_POINTER_ID;
	    			int index = m.getActionIndex();
		    		if(mPointer1ID == m.getPointerId(index))
		    			mPointer1ID = INVALID_POINTER_ID;
	            }
	    		break;
	    }

		return true;
	}

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //drawThread = new DrawThread2D(getHolder(), this);
        //drawThread.setRunning(true);
        //drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Cleanup();
    }
}
