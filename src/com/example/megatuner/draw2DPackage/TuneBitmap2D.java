package com.example.megatuner.draw2DPackage;

import android.graphics.Bitmap;
import com.example.megatuner.Interfaces.TuneBitmap;

/**
 * Created by Vladimir on 03.08.2015.
 */
public class TuneBitmap2D implements TuneBitmap {
    public Bitmap bitmap;
    public TuneBitmap2D(Bitmap _bitmap){
        bitmap = _bitmap;
    }

    @Override
    public float getWidth() {
        return bitmap.getWidth();
    }

    @Override
    public float getHeight() {
        return bitmap.getHeight();
    }

    @Override
    public void dispose() {
        bitmap.recycle();
    }
}
