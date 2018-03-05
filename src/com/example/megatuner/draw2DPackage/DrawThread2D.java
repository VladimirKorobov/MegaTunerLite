package com.example.megatuner.draw2DPackage;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created with IntelliJ IDEA.
 * User: Vladimir-Desktop
 * Date: 04.10.13
 * Time: 8:47
 * To change this template use File | Settings | File Templates.
 */
public class DrawThread2D extends Thread {
    private boolean runFlag = false;
    public boolean drawFlag = false;
    private SurfaceHolder surfaceHolder;
    TuneView2D view;
    public DrawThread2D(SurfaceHolder surfaceHolder, TuneView2D view)
    {
        this.surfaceHolder = surfaceHolder;
        this.view = view;
    }
    public void setRunning(boolean run) {
        runFlag = run;
    }

    @Override
    public void run() {
        Canvas canvas;
        while (runFlag) {
            canvas = null;
            if(drawFlag)
            {
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                        view.onDraw(canvas);
                    }
                }
                finally
                {
                    if(canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    drawFlag = false;
                }
            }
            else
            {
                try {
                    Thread.sleep(2);
                }
                catch (InterruptedException e) {

                }
            }
        }
    }
}
