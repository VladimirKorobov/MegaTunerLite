package com.example.megatuner;

import android.content.Context;
import android.graphics.*;
import com.example.megatuner.Interfaces.TuneBitmap;
import com.example.megatuner.Interfaces.TuneRenderer;

/**
 * Created with IntelliJ IDEA.
 * User: Vladimir-Desktop
 * Date: 22.10.13
 * Time: 11:14
 * To change this template use File | Settings | File Templates.
 */
class TunerSettings
{
    Bitmap bitmapOneOctave;
    Bitmap bitmapAllOctaves;
    Bitmap bitmapAllTone;
    Bitmap bitmapOctaveTone;
    Bitmap bitmapPointer;
    //Bitmap piano;
    PointF[] octavesScaleHits;
    PointF[] octaveOneScaleHits;
    PointF[] buttonHits;

    float widthAllToneScale;
    float widthOctaveToneScale;
    int countAllToneScale;
    int countOctaveToneScale;


    float centerX;
    float centerY;
    float circleStep;

    RectF freqBox;
    RectF octaveBox;
    RectF tunerBox;

    float width;
    float tunerHeight;
    float pianoHeight;
    float textSize;
    Context context;
    TuneSurface tuneSurface;
    // RGBA!!!
    int[] colorToneBox = new int[] {64, 128, 255, 255};

    boolean backButtonPressed = false;

    public TunerSettings(Context context, TuneSurface tuneSurface, float width, float height, float textSize)
    {
        this.width = Math.min(width, height);

        this.textSize = textSize;

        centerX = this.width / 2;
        centerY = centerX;
        circleStep = this.width / 8;

        float boxHeight = 1.2f * textSize;
        freqBox = new RectF(centerX - circleStep, centerY + 1.5f * circleStep, centerX + circleStep,
                centerY + 1.5f * circleStep + boxHeight);
        octaveBox = new RectF(centerX - circleStep, centerY - 1.5f * circleStep - boxHeight,
                centerX + circleStep, centerY - 1.5f * circleStep);

        float indent = 0;//circleStep / 16;
        //boxHeight = (height - freqBox.bottom) / 2;
        float minDim = Math.min(width, height);
        tunerBox = new RectF(indent, freqBox.bottom + indent, minDim - indent, freqBox.bottom + indent + circleStep );
        this.tunerHeight = tunerBox.bottom + indent;
        this.pianoHeight = height - this.tunerHeight;

        this.context = context;
        this.tuneSurface = tuneSurface;


        octavesScaleHits = new PointF[this.tuneSurface.Octaves.length];
        bitmapAllOctaves = buildTunerBitmap(this.tuneSurface.Octaves, 25, octavesScaleHits, false);
        octaveOneScaleHits = new PointF[this.tuneSurface.Octaveg.length];
        bitmapOneOctave = buildTunerBitmap(this.tuneSurface.Octaveg, 19, octaveOneScaleHits, true);

        // Setup piano output
        /*
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.setMode(AudioManager.MODE_IN_CALL);
        am.setSpeakerphoneOn(true);
        */

        //piano = createPianoBitmap();
    }

    public void drawOctaveToneBitmap(TuneRenderer renderer, float xOffset, float x, float y)
    {
        TuneBitmap bitmap = renderer.createBitmap(bitmapOctaveTone);
        renderer.drawBitmap(x, y, bitmap);
        bitmap.dispose();
    }
    public void drawAllToneBitmap(TuneRenderer renderer, float x, float y)
    {
        TuneBitmap bitmap = renderer.createBitmap(bitmapAllTone);
        renderer.drawBitmap(x, y, bitmap);
        bitmap.dispose();
    }

    private void drawRotatedText(Canvas canvas, Paint paint, String text, float x, float y, float cx, float cy, float degrees)
    {
        canvas.save();
        canvas.rotate(degrees, cx, cy);
        canvas.drawText(text, x, y, paint);
        canvas.restore();
    }
    private void drawRotatedLine(Canvas canvas, Paint paint, float x1, float y1, float x2, float y2, float cx, float cy, float degrees)
    {
        canvas.save();
        canvas.rotate(degrees, cx, cy);
        canvas.drawLine(x1, y1, x2, y2, paint);
        canvas.restore();
    }

    private void drawRotatedLine(TuneRenderer renderer, float x1, float y1, float x2, float y2, float cx, float cy, float degrees)
    {
        renderer.save();
        renderer.rotate(degrees, cx, cy);
        renderer.drawLine(x1, y1, x2, y2);
        renderer.restore();
    }

    private void DrawEmbossedRect(Canvas canvas, Paint paint, RectF rect, float lineWidth, int[] fillColor, Boolean convex)
    {
        if(fillColor != null)
        {
            paint.setARGB(fillColor[0], fillColor[1], fillColor[2], fillColor[3]);

            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect, paint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);


        if(!convex)
            paint.setARGB(128, 192, 192, 192);
        else
            paint.setARGB(128, 0, 0, 0);

        canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, paint);
        canvas.drawLine(rect.right, rect.bottom, rect.left, rect.bottom, paint);

        if(convex)
            paint.setARGB(128, 192, 192, 192);
        else
            paint.setARGB(128, 0, 0, 0);

        canvas.drawLine(rect.left, rect.bottom, rect.left, rect.top, paint);
        canvas.drawLine(rect.left, rect.top, rect.right, rect.top, paint);
    }
    private Bitmap createToneBitmap_impl1(int bitmapWidth, int bitmapHeight, int scaleCount)
    {
        Bitmap bitmap =  Bitmap.createBitmap(bitmapWidth * 12, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        //paint.setAntiAlias(true);
        paint.setARGB(0, 0, 0, 0);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);

        float scaleRange = (float)bitmapWidth / scaleCount;
        paint.setARGB(255, 0, 0, 0);
        float lineOffset = 1;
        float textSize = bitmapHeight / 2;
        paint.setTextSize(textSize);
        for(int j = 0; j < 2; j ++)
        {
            for(int k = 0; k < TuneSurface.Octaveg.length; k ++) {
                paint.setStyle(Paint.Style.FILL);
                canvas.drawText(TuneSurface.Octaveg[k], lineOffset + 4, textSize * 1.2f, paint);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(lineOffset, 0, lineOffset, bitmapHeight, paint);

                for (int i = 1; i < scaleCount; i++) {
                    float offsetX = i * scaleRange + lineOffset;
                    canvas.drawLine(offsetX, 0, offsetX, bitmapHeight / 8, paint);
                }
                lineOffset += scaleCount * scaleRange;
            }

            paint.setARGB(colorToneBox[3], colorToneBox[0], colorToneBox[1], colorToneBox[2]);
            lineOffset = 0;
        }

        return bitmap;
    }
    private Bitmap createToneBitmap(int bitmapWidth, int bitmapHeight, int scaleCount)
    {
        Bitmap bitmap =  Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        //paint.setAntiAlias(true);
        paint.setARGB(0, 0, 0, 0);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);

        float scaleRange = (float)bitmap.getWidth() / scaleCount;
        paint.setARGB(255, 0, 0, 0);
        float lineOffset = 1;
        for(int j = 0; j < 2; j ++)
        {
            canvas.drawLine(lineOffset, 0, lineOffset, bitmapHeight, paint);
            for(int i = 1; i < scaleCount; i ++)
            {
                float offsetX = i * scaleRange + lineOffset;
                canvas.drawLine(offsetX, 0, offsetX, bitmapHeight / 8, paint);
            }

            paint.setARGB(colorToneBox[3], colorToneBox[0], colorToneBox[1], colorToneBox[2]);
            lineOffset = 0;
        }

        return bitmap;
    }

    private void drawBackButton(Canvas canvas, boolean pressed)
    {
        // Draw back knob
        Bitmap image = BitmapFactory.decodeResource(context.getResources(),
                (pressed) ? R.drawable.tbbackpressed0 : R.drawable.tbbackreleased0);
        RectF buttonRect = new RectF(0, 0,circleStep, circleStep / 1.7f);
        buttonRect.offset(circleStep / 4, circleStep / 4);

        if(buttonHits == null)
        {
            buttonHits = new PointF[1];
            buttonHits[0] = new PointF(buttonRect.left + buttonRect.width() / 2,
                    buttonRect.top + buttonRect.height() / 2);
        }

        Rect imageRect = new Rect(0, 0, image.getWidth() - 1, image.getHeight() - 1);

        canvas.drawBitmap(image, imageRect, buttonRect, new Paint(Paint.FILTER_BITMAP_FLAG));
        image.recycle();

    }
    public void backButtonUpdate(boolean pressed)
    {

        if(pressed && !backButtonPressed)
        {
            Canvas canvas = new Canvas(bitmapOneOctave);
            drawBackButton(canvas, pressed);
            backButtonPressed = true;
        }
        else if(!pressed && backButtonPressed)
        {
            Canvas canvas = new Canvas(bitmapOneOctave);
            drawBackButton(canvas, pressed);
            backButtonPressed = false;
        }
    }

    private Bitmap buildTunerBitmap(String[] scaleValues, float angleStep, PointF[] scaleCenters, Boolean oneOctave)
    {

        float angle = -(angleStep * (scaleValues.length + 2) - angleStep) / 2;
        int i;

        Bitmap bitmap =  Bitmap.createBitmap((int)this.width, (int)this.tunerHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAntiAlias(true);
        //paint.setARGB(255, 196, 196, 196);
        //canvas.drawPaint(paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(circleStep / 16);

        // Draw background
        Bitmap background = BitmapFactory.decodeResource(context.getResources(), R.drawable.plastic3);
        RectF tunerRect = new RectF(0, 0, this.width, this.tunerHeight);
        Rect backgroundRect = new Rect(0, 0, background.getWidth() - 1, background.getHeight() - 1);
        canvas.drawBitmap(background, backgroundRect, tunerRect, null);
        DrawEmbossedRect(canvas, paint, tunerRect, circleStep / 16, null, true);

        paint.setARGB(255, 0, 0, 0);

        RectF arcBlackRect = new RectF(
                centerX + 2 - 3f * circleStep,
                centerY - 3f * circleStep,
                centerX + 2 + 3f * circleStep,
                centerY + 3f * circleStep);

        //Draw frequency lines with black
        canvas.drawArc(arcBlackRect, -angle - 90, 2 * angle, false, paint);
        canvas.drawCircle(centerX + 2, centerY, 1 * circleStep, paint);
        for(i = 0; i < scaleValues.length + 2; i ++)
            drawRotatedLine(canvas, paint, centerX + 2, centerY - 3 * circleStep,
                    centerX + 2, centerY - (2.8f * circleStep),
                    centerX, centerY, angle + i * angleStep);

        //Draw frequency text with black
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize);

        // Start and end scale lines will be without text
        // Coordinates of scale to touch
        float scaleXCoord = 0;
        float scaleYCoord = -3 * circleStep - textSize * 0.75f;

        // Prepare octave buttons
        Bitmap octaveButton = null;
        Rect octaveBitmapRect = null;
        RectF octaveButtonRect = null;
        if(!oneOctave)
        {
            octaveButton = BitmapFactory.decodeResource(context.getResources(), R.drawable.octavebutton);
            octaveBitmapRect = new Rect(0, 0, octaveButton.getWidth(), octaveButton.getHeight());
            float buttonDim = textSize / 1.5f;
            octaveButtonRect = new RectF(-buttonDim, -buttonDim, buttonDim, buttonDim);
       }


        for(i = 0; i < scaleValues.length; i ++)
        {
            float curAngle = angle + (i + 1) * angleStep;

            float cos = (float)Math.cos(curAngle * Math.PI / 180);
            float sin = (float)Math.sin(curAngle * Math.PI / 180);
            float x = cos * scaleXCoord - sin * scaleYCoord + centerX;
            float y = sin * scaleXCoord + cos * scaleYCoord + centerY;
            scaleCenters[i] = new PointF(x, y);

            if(!oneOctave)
            {
                RectF rect = new RectF(octaveButtonRect);
                rect.offset(scaleCenters[i].x, scaleCenters[i].y);
                canvas.drawBitmap(octaveButton, octaveBitmapRect, rect, paint);
            }

            float textWidth = paint.measureText(scaleValues[i]) / 2;
            drawRotatedText(canvas, paint, scaleValues[i], centerX-textWidth + 2, circleStep - textSize * 0.5f, centerX, centerY,
                    curAngle);
        }
        if(octaveButton != null)
            octaveButton.recycle();

        // Draw tune knob
        Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.tunerknob);
        RectF buttonRect = new RectF(centerX - circleStep-1, centerY - circleStep-1, centerX + circleStep+1, centerY + circleStep+1);
        Rect imageRect = new Rect(0, 0, image.getWidth() - 1, image.getHeight() - 1);

        paint.setARGB(255, 0, 0, 0);
        canvas.drawBitmap(image, imageRect, buttonRect, paint);
        image.recycle();

        if(oneOctave)
        {
            drawBackButton(canvas, false);
        }


        paint.setARGB(196, 64, 255, 64);
        paint.setStyle(Paint.Style.STROKE);

        RectF arcColorRect = new RectF(
                centerX - 3f * circleStep,
                centerY - 3f * circleStep,
                centerX + 3f * circleStep,
                centerY + 3f * circleStep);

        //Draw frequency lines with color
        canvas.drawArc(arcColorRect, -angle - 90 + 0.5f, 2 * angle - 1, false, paint);
        //canvas.drawCircle(centerX, centerY, 1 * circleStep, paint);

        for(i = 0; i < scaleValues.length + 2; i ++)
            drawRotatedLine(canvas, paint, centerX, centerY - 3 * circleStep,
                    centerX, centerY - (2.8f * circleStep),
                    centerX, centerY, angle + i * angleStep);

        //Draw frequency text with color
        paint.setStyle(Paint.Style.FILL);

        for(i = 0; i < scaleValues.length; i ++)
        {
            float textWidth = paint.measureText(scaleValues[i]) / 2;
            drawRotatedText(canvas, paint, scaleValues[i], centerX-textWidth, circleStep -textSize * 0.5f, centerX, centerY,
                    angle + (i + 1) * angleStep);
        }

        // Precise tuner rect
        DrawEmbossedRect(canvas, paint, tunerBox, circleStep / 32, new int[] {255, 64, 64, 64}, false);

        widthAllToneScale = tunerBox.width() / 5;
        widthOctaveToneScale = tunerBox.width() / 3;
        countAllToneScale = 10;
        countOctaveToneScale = 10;


        //bitmapAllTone = createToneBitmap((int)(widthAllToneScale), (int)(tunerBox.height()), countAllToneScale);
        bitmapAllTone = createToneBitmap_impl1((int)(widthAllToneScale), (int)(tunerBox.height()), countAllToneScale);
        bitmapOctaveTone = createToneBitmap_impl1((int)(widthOctaveToneScale), (int)(tunerBox.height()), countOctaveToneScale);

        return bitmap;
    }
    public void createPointerBitmap()
    {
        if(bitmapPointer == null)
        {
            bitmapPointer = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
            Canvas pointerCanvas = new Canvas(bitmapPointer);
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
            float cx = bitmapPointer.getWidth()/2;
            float cy = 0;
            float dy = bitmapPointer.getHeight();
            float dx = cx;

            paint.setStyle(Paint.Style.FILL);
            Path path = new Path();
            path.moveTo(cx - dx/2, cy);
            path.lineTo(cx, cy + dy);
            path.lineTo(cx + dx/2, cy);
            path.close();
            path.offset(3, 0);
            paint.setARGB(255, 0, 0, 0);
            pointerCanvas.drawPath(path, paint);
            path.offset(-3, 0);
            paint.setARGB(255, 192, 0, 0);
            pointerCanvas.drawPath(path, paint);
        }
    }
}
