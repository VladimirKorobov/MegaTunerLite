package com.example.megatuner.GLPackage;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLUtils;
import com.example.megatuner.Interfaces.TuneBitmap;
import com.example.megatuner.draw2DPackage.TuneBitmap2D;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Vladimir on 03.08.2015.
 */
public class TuneBitmapGL implements TuneBitmap {

    private GL10 gl;
    private float width;
    private float height;
    public int textureWidth;
    public int textureHeight;
    public int[] textureId = new int[1];
    public Bitmap bitmap;
    public TuneBitmapGL(Bitmap _bitmap, GL10 _gl){
        bitmap = _bitmap;

        gl = _gl;
        _gl.glEnable(GL10.GL_TEXTURE_2D);
        _gl.glGenTextures(1, textureId, 0);

        //...and bind it to our array
        _gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId[0]);
        //Create  Filtered Texture
        //_gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        //Use the Android GLUtils to specify a two-dimensional texture image from our bitmap

        textureWidth = 1;
        while(textureWidth < bitmap.getWidth())
            textureWidth <<= 1;
        textureHeight = 1;
        while(textureHeight < bitmap.getHeight())
            textureHeight <<= 1;
        if(textureWidth != bitmap.getWidth() || textureHeight != bitmap.getHeight()) {
            Bitmap image = Bitmap.createBitmap(textureWidth, textureHeight, bitmap.getConfig());
            Canvas canvas = new Canvas(image);
            canvas.drawBitmap(bitmap, 0, 0, null);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, image, 0);
            int error = _gl.glGetError();
            if(error != 0)
                error = 0;

            image.recycle();
        }
        else
        {
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, bitmap, 0);
            int error = _gl.glGetError();
            if(error != 0)
                error = 0;
        }
        _gl.glDisable(GL10.GL_TEXTURE_2D);
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
        gl.glDeleteTextures(1, textureId, 0);
    }
}
