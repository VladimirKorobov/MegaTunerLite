package com.example.megatuner.GLPackage;

import com.example.megatuner.Interfaces.TuneRenderer;
import com.example.megatuner.TuneSurface;
import com.example.megatuner.Interfaces.TuneView;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class TuneViewGL extends GLSurfaceView implements TuneView {

	float mPreviousX0 = 0;
	float mPreviousY0 = 0;
	
	float mPreviousX1 = 0;
	float mPreviousY1 = 0;
	
	private static final int INVALID_POINTER_ID = -1;
	// Active pointerID
	int mPointer0ID = INVALID_POINTER_ID; 
	// Second pointerID
	int mPointer1ID = INVALID_POINTER_ID;
	
	TuneRendererGL rendererGL;
	TuneSurface surface;
	public TuneViewGL(TuneSurface surface, Context context)
	{
		super(context);
		rendererGL = new TuneRendererGL(surface);
		setRenderer(rendererGL);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		this.surface = surface;
	}
	@Override
	public void invalidateRender() {
		requestRender();
	}
	@Override
	public TuneRenderer getRenderer() {
		return rendererGL;
	}

    @Override
    public void Cleanup() {
        if(rendererGL != null)
        {

        }
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

}
