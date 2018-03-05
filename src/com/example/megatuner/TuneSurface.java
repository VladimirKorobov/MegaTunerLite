package com.example.megatuner;

import java.text.DecimalFormat;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Dialog;
import android.graphics.*;
import android.util.TypedValue;
import android.widget.TextView;
import com.example.megatuner.GLPackage.TuneViewGL;
import com.example.megatuner.Interfaces.TuneBitmap;
import com.example.megatuner.Interfaces.TuneRenderer;
import com.example.megatuner.Interfaces.TuneView;
import com.example.megatuner.draw2DPackage.TuneView2D;

import android.content.Context;

public class TuneSurface {
    static class DrawData
    {
        ReentrantLock lock = new ReentrantLock();
        float[] spectrum;
        short[] soundData;
        int dataLength;
        int freqIndexMin;
        int freqIndexMax;
        float freqMax;
        float freqStep;
    }
	public interface TextBuffer
	{
		void addText(String text, float x, float y);
		void release();
	}

	private class SpectrumSettings
	{
        final float yFactorConst = 0.01f;
		public float yFactor = yFactorConst;
		int left = 0;
		int right = 0x7FFFFFFF;
		int max = 0x7FFFFFFF;
		
		public void CorrectRange()
		{
			if(left < 0) left = 0;
			else if(left > max) left = max;
			if(right < 0) right = 0;
			else if(right > max) right = max;
			if(left > right) left = right;
		}
		public void UpdateMaximum(int max)
		{
			this.max = max;
			CorrectRange();
		}
		public void UpdateScale(float x0, float dx0, float y0, float dy0, float x1, float dx1, float y1, float dy1)
		{
            float dxleft;
            float dxright;

            if(x0 < x1)
            {
                dxleft = dx0;
                dxright = dx1;
            }
            else
            {
                dxleft = dx1;
                dxright = dx0;
            }

            float xFactor = (float)this.max / 2000;
            left = (int)(left - dxleft * xFactor);
            right = (int)(right - dxright * xFactor);
            if(right - left < 10)
                right = left + 10;

            CorrectRange();
		}
		public void UpdatePosition(float dx, float dy)
		{
			if(Math.abs(dx) >= Math.abs(dy) * 0.5f)
			{
                float xFactor = dx * this.max / 2000;

                if(left - xFactor < 0)
                {
                    xFactor = left;
                }
				
				left = (int)(left - xFactor);
				right = (int)(right - xFactor);
				
				if(right > max)
				{
					left -= (right - max);
					right = max;
				}
			}
			else
			{
                if(dy > 0)
                    yFactor *= 0.95;
                else
                    yFactor *= 1.05;

                if(yFactor < yFactorConst * 0.01f)
                    yFactor = yFactorConst * 0.01f;

			}
		}
	}

    private class SignalSettings
	{
		public float xFactor = 1;
		public float yFactor = 0.1f;

		public void UpdateScale(float dx, float dy)
		{
			if(Math.abs(dx) < Math.abs(dy))
			{
                if(dy > 0)
                    yFactor *= 0.95;
                else
                    yFactor *= 1.05;
			}
		}
	}

	public boolean drawTunerFlag = true;
	public boolean drawSignalFlag = false;
	public boolean drawSpectrumFlag = false;
    public boolean drawSignalAndSpectrumFlag = false;

	TuneLayout layoutTune = new TuneLayout(240, 240, 240, 255);
	TuneLayout layoutSignal = new TuneLayout(0, 0, 0, 255);
    TuneLayout layoutSpectrum = new TuneLayout(0, 0, 0, 255);

    TuneLayout layoutTuner = new TuneLayout(240, 240, 240, 255);
    TuneLayout layoutSettings = new TuneLayout(196, 196, 196, 255);

            TuneView view;
	TuneRenderer renderer;
	
	DecimalFormat decFormat = new DecimalFormat("#.##");
	
	public float textSize = 20;
	float lineWidth = 0;
	
	SpectrumSettings spectSettings = new SpectrumSettings();
	SignalSettings signalSettings = new SignalSettings();
	
	TextBuffer octaveTextBuffer;
	float[] octaveLineBuffer;
	float octaveStep;
    ReentrantLock lock = new ReentrantLock();
    public static final int OCTAVE_NONE = -4;
    public static final int OCTAVE_BIG = -1;
    public static final int OCTAVE_SMALL = -0;
    public static final int OCTAVE_1 = 1;
    public static final int OCTAVE_2 = 2;
    public static final int OCTAVE_3 = 3;
    public static final int OCTAVE_4 = 4;
    public static final int OCTAVE_5 = 5;

    public static final int[] octaveIds = new int[] {
            OCTAVE_NONE, OCTAVE_BIG, OCTAVE_SMALL, OCTAVE_1, OCTAVE_2, OCTAVE_3, OCTAVE_4, OCTAVE_5
    };

    int currentOctave = OCTAVE_NONE;
	
	SignalProcessing signal = new SignalProcessing(44100);
    String[] Octaves = {
            "B",
            "S",
            "1",
            "2",
            "3",
            "4",
            "5"
    };

	private String[] Octave = {
            "Do",
            "Do#",
            "Re",
            "Re#",
            "Mi",
            "Fa",
            "Fa#",
            "Sol",
            "Sol#",
            "La",
            "La#",
            "Si"
	};
	private String[] OctaveG = {
            "C",
            "C#",
            "D",
            "D#",
            "E",
            "F",
            "F#",
            "G",
            "G#",
            "A",
            "A#",
            "B"
	};
    static String[] Octaveg = {
            "c",
            "c#",
            "d",
            "d#",
            "e",
            "f",
            "f#",
            "g",
            "g#",
            "a",
            "a#",
            "b"
    };
	float[] OctRange = {
			// ContrOctave
		    //(float)32.703,
		    // Big Octave
		    (float)65.406,
		    // Small Octave
		    (float)130.81,
		    // Octave 1
		    (float)261.63,
		    // Octave 2
		    (float)523.25,
		    // Octave 3
		    (float)1046.5,
		    // Octave 4
		    (float)2093, 
		    // Octave 5
		    (float)4186,
		    // Octave 6
		    (float)8372
		};
    Context context;
    TunerSettings tunerSettings;

    float width;
    float height;
    float pow_12 = (float)Math.pow(2, 1.0/12);
    int showPressedButtonCount = -1;

    private static class CheckBox
    {
        float x;
        float y;
        String text;
        String[] help;
        float textSize;
        Boolean checked = false;

        static Bitmap checkboxChecked;
        static Bitmap checkboxUnchecked;
        static Bitmap checkboxQuestion;


        public CheckBox(String text, String[] help)
        {
            this.text = text;
            this.help = help;
        }
        public void setCheck(Boolean checked)
        {
            this.checked = checked;
        }
        public Boolean getCheck()
        {
            return this.checked;
        }
        public void draw(TuneRenderer renderer, float x, float y, float textSize, float boxSize)
        {
            this.x = x;
            this.y = y;
            this.textSize = boxSize;

            Bitmap bitmap = (checked) ? checkboxChecked : checkboxUnchecked;
            TuneBitmap scaledBitmap = renderer.createBitmap(bitmap, (int) (boxSize), (int) (boxSize));

            if(scaledBitmap != null) {
                renderer.drawBitmap(x, y, scaledBitmap);
                scaledBitmap.dispose();
            }
            renderer.setColor(0, 0, 0, 255);
            renderer.drawText(text, textSize, x + boxSize + textSize / 2, y + boxSize);
        }
        public void drawHelpButtom(TuneRenderer renderer, float endX, float boxSize)
        {
            TuneBitmap scaledBitmap = renderer.createBitmap(checkboxQuestion, (int) (boxSize), (int) (boxSize));
            if(scaledBitmap != null) {
                renderer.drawBitmap(endX - 1.5f * boxSize, y, scaledBitmap);
                scaledBitmap.dispose();
            }
        }
        public Boolean pressed(float x, float y, float endX)
        {
            RectF r = new RectF(this.x, this.y, endX - 2 * this.textSize, this.y + this.textSize);
            Boolean b = r.contains(x, y);
            return b;

            //return new RectF(this.x, this.y, endX - this.textSize, this.y + this.textSize).contains(x, y);
        }
        public Boolean helpPressed(float x, float y, float endX)
        {
            return new RectF(endX - 1.5f * this.textSize, this.y, endX - 0.5f * this.textSize, this.y + this.textSize).contains(x, y);
        }
    }
    String[] hlpHarmonicSuppression =
    {
            "Harmonic Suppression:",
            "The only lower harmonic of the input signal will be taken into account."
    };
    String[] hlpUseLowerTone =
    {
            "Use Lower Tone:",
            "Tuner will determine the lower tone in the spectrum."
    };
    String[] hlpSyncByMax =
    {
            "Synchronize by max:",
            "Found frequency will be synchronized by the maximum tone in the spectrum.",
            "This may increase a measurement precision of low frequencies.",
            "Should be used together with either 'Harmonic Suppression' or 'Use Lower Tone'."
    };
    String[] hlpAveraging =
    {
            "Averaging:",
            "Tuner will average results of several measurements."
    };
    String[] hlpLockedLoop =
    {
            "Locked loop:",
            "Frequency-locked loop will be used.",
            "This may be useful if the input signal is decreased with time, like a sound of guitar string."
    };

    CheckBox cbHarmonicSuppression = new CheckBox("Harmonic Suppression", hlpHarmonicSuppression);
    CheckBox cbUseLowerTone = new CheckBox("Use Lower Tone", hlpUseLowerTone);
    CheckBox cbSyncByMax = new CheckBox("Sync By Max", hlpSyncByMax);
    CheckBox cbAveraging = new CheckBox("Averaging", hlpAveraging);
    CheckBox cbLockedLoop = new CheckBox("Locked loop", hlpLockedLoop);

    CheckBox activeBox = null;

    public TuneSurface(Context context)
	{
        this.context = context;
        cbHarmonicSuppression.checked = true;
        cbUseLowerTone.checked = false;
        cbSyncByMax.checked = true;
        cbLockedLoop.checked = true;
        cbAveraging.checked = true;
    }

    public void setHarmonicSuppression(boolean enable)
    {
        cbHarmonicSuppression.checked = enable;
    }
    public boolean getHarmonicSuppression()
    {
        return cbHarmonicSuppression.checked;
    }
    public void setUseFirst(boolean useFirst)
    {
        cbUseLowerTone.checked = useFirst;
    }
    public boolean getUseFirst()
    {
        return cbUseLowerTone.checked;
    }
    public void setSyncByMax(boolean syncByMax)
    {
        cbSyncByMax.checked = syncByMax;
    }
    public boolean getSyncByMax()
    {
        return cbSyncByMax.checked;
    }
    public void setFrequencyControl(boolean frequencyControl)
    {
        cbLockedLoop.checked = frequencyControl;
    }
    public boolean getFrequencyControl()
    {
        return cbLockedLoop.checked;
    }
    public void setAveraging(boolean averaging)
    {
        cbAveraging.checked = averaging;
    }
    public boolean getAveraging()
    {
        return cbAveraging.checked;
    }

    private int getOctaveIndex(int octave)
    {
        switch(octave)
        {
            case OCTAVE_BIG:
                return 0;
            case OCTAVE_SMALL:
                return 1;
            case OCTAVE_1:
                return 2;
            case OCTAVE_2:
                return 3;
            case OCTAVE_3:
                return 4;
            case OCTAVE_4:
                return 5;
            case OCTAVE_5:
                return 6;
            default:
                return -1;
        }
    }
    public int getOctave() {
        return currentOctave;
    }
    public void setOctave(int octave)
    {
        signal.currentDrawData.lock.lock();
        try
        {
            int octaveIndex = getOctaveIndex(octave);
            if(octaveIndex < 0)
            {
                spectSettings.left = 0;
                spectSettings.right = spectSettings.max;
            }
            else
            {
                spectSettings.left = (int)(OctRange[octaveIndex] / signal.currentDrawData.freqStep / pow_12);
                spectSettings.right = (int)(OctRange[octaveIndex] * 2 / signal.currentDrawData.freqStep * pow_12 + 0.5);
            }
            currentOctave = octave;
        }
        finally {
            UpdateSignalRange();
            signal.currentDrawData.lock.unlock();

        }
    }
    private int getOctaveId(int octaveIndex)
    {
        switch(octaveIndex)
        {
            case 0:
                return OCTAVE_BIG;
            case 1:
                return OCTAVE_SMALL;
            case 2:
                return OCTAVE_1;
            case 3:
                return OCTAVE_2;
            case 4:
                return OCTAVE_3;
            case 5:
                return OCTAVE_4;
            case 6:
                return OCTAVE_5;
            default:
                return OCTAVE_NONE;
        }
    }


	public void initRenrererGL(Context context)
	{
		TuneViewGL surfaceview = new TuneViewGL(this, context);
		renderer = surfaceview.getRenderer();
		renderer.setLineWidth(lineWidth);
		view = surfaceview;
	}
    public void Process1() {

    }
	public void Process(short[] soundData, int dataLength)
	{
        signal.harmonicSuppression = cbHarmonicSuppression.checked;
        signal.useFirst = cbUseLowerTone.checked;
        signal.syncByMax = cbSyncByMax.checked;
        signal.frequencyControl = cbLockedLoop.checked;

        signal.soundData = soundData;
        signal.dataLength = dataLength;
		signal.CalcFFT();
        UpdateSignalRange();
		signal.CalcMaximums();
		invalidateRender();
	}
	public void initRenrerer2D(Context context)
	{
		TuneView2D surfaceview = new TuneView2D(this, context); 
		renderer = surfaceview.getRenderer();
		renderer.setLineWidth(lineWidth);
		view = surfaceview;
	}
    public void updateLayouts()
    {
        updateLayouts(this.width, this.height);
    }

    public void updateLayouts(float width, float height)
	{
        if(width > height)
        {
            if(width / height < 1.4)
                height = width / 1.4f;
            //((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else
        {
            if(height / width  < 1.4)
                width = height / 1.4f;
            //((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

		textSize = (width + height) / 2 / 20;

        layoutTuner.updateLayout(0, 0, Math.min(width, height),Math.min(width, height));

		layoutTune.updateLayout(0, 0, width, height / 8);
        if(drawSignalAndSpectrumFlag) {
            layoutSpectrum.updateLayout(0, layoutTune.rect.bottom, width, layoutTune.rect.bottom + height * 5 / 12);
            layoutSignal.updateLayout(0, layoutSpectrum.rect.bottom, width, height);
        }
        else if(drawSignalFlag) {
            layoutSignal.updateLayout(0, layoutTune.rect.bottom, width, height);
        }
        else if(drawSpectrumFlag) {
            layoutSpectrum.updateLayout(0, layoutTune.rect.bottom, width, height);
        }

		lineWidth = width / 400;
		if(lineWidth < 1) lineWidth = 1;

		renderer.setLineWidth(lineWidth);
		
		octaveStep = layoutTune.rect.width() / 5;
		createOctaveTextBuffer();
		createOctaveLineBuffer();

        if(tunerSettings == null)
        {
            tunerSettings = new TunerSettings(context, this, width, height, textSize);
        }

        layoutSettings.updateLayout(0, tunerSettings.tunerHeight, width, height);
        this.width = width;
        this.height = height;
	}
	
	public void invalidateRender()
	{
		view.invalidateRender();
	}
	
	void clearLayout(TuneLayout layout)
	{
        if (layout != null) {
            renderer.setColor(layout.red, layout.green, layout.blue, layout.alpha);
            renderer.drawRect(layout.rect.left, layout.rect.top, layout.rect.right + 1, layout.rect.bottom + 1);
        } else {
            renderer.setColor(0, 0, 0, 255);
            renderer.drawRect(0, 0, width, height);
        }
	}
	
	public void UpdateSignalRange()
	{
		spectSettings.UpdateMaximum(signal.getSpectLength() - 1);
		signal.SetRange(spectSettings.left, spectSettings.right);
	}

	
	public void draw()
	{
        DrawData drawData = this.signal.getCurrentDrawData();
        if (drawData != null) {
            drawData.lock.lock();
            try {
                if (drawTunerFlag) {
                    clearLayout(null);
                    drawTuner(drawData);
                    clearLayout(layoutSettings);
                    drawSettings();
                } else {
                    clearLayout(layoutTune);
                    drawTune(drawData);
                    if (drawSignalFlag) {
                        clearLayout(layoutSignal);
                        drawSignal(drawData);
                    }
                    else if (drawSpectrumFlag) {
                        clearLayout(layoutSpectrum);
                        drawSpectrum(drawData);
                    }
                    else
                    {
                        clearLayout(layoutSignal);
                        clearLayout(layoutSpectrum);
                        drawSignal(drawData);
                        drawSpectrum(drawData);
                    }
                }
            } finally {
                drawData.lock.unlock();
            }
        }
	}
	// Touch processing
    //////////////////////////////////////////////////////////////////////////////////////////////
	public void TouchScaleProcessing(float x0, float dx0, float y0, float dy0, float x1, float dx1, float y1, float dy1)
	{
		if(drawSpectrumFlag || drawSignalAndSpectrumFlag)
		{
            if(layoutSpectrum.rect.contains(x0, y0))
            {
                spectSettings.UpdateScale(x0, dx0, y0, dy0, x1, dx1, y1, dy1);
                UpdateSignalRange();
            }
		}
	}
	
	public void TouchMoveProcessing(float x0, float y0, float dx, float dy)
	{
		if(drawSpectrumFlag || drawSignalAndSpectrumFlag)
		{
            if(layoutSpectrum.rect.contains(x0, y0))
            {
			    spectSettings.UpdatePosition(dx, dy);
            }
		}
		if(drawSignalFlag || drawSignalAndSpectrumFlag)
		{
            if(layoutSignal.rect.contains(x0, y0))
            {
			    signalSettings.UpdateScale(dx, dy);
            }
		}
	}

    public void TouchUpProcessing(float x, float y)
    {

    }

    public void TouchDownProcessing(float x, float y)
    {
        if(!drawTunerFlag)
            return;
        if(SettingsTouch(x, y, tunerSettings.width))
            return;

        if(tunerSettings != null)
        {
            float  rPow2 = layoutTuner.rect.width() / 16;
            rPow2 *= rPow2;
            if(drawTunerFlag && currentOctave == OCTAVE_NONE)
            {
                if(tunerSettings.octavesScaleHits != null)
                {
                    // Get octave scale
                    for(int i = 0; i < tunerSettings.octavesScaleHits.length; i ++)
                    {
                        float dx = tunerSettings.octavesScaleHits[i].x - x;
                        float dy = tunerSettings.octavesScaleHits[i].y - y;
                        if(dx*dx + dy*dy < rPow2)
                        {
                            // Octave found
                            int octaveId = getOctaveId(i);
                            tunerSettings.backButtonUpdate(false);
                            setOctave(octaveId);
                            break;
                        }
                    }
                }
            }
            else // Back button may be pressed
            {
                if(tunerSettings.buttonHits != null)
                {
                    float dx = tunerSettings.buttonHits[0].x - x;
                    float dy = tunerSettings.buttonHits[0].y - y;
                    if(dx*dx + dy*dy < rPow2)
                    {
                        // button pressed
                        tunerSettings.backButtonUpdate(true);
                        showPressedButtonCount = 1;

                        //setOctave(OCTAVE_NONE);
                    }
                }
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
	private void createOctaveTextBuffer()
	{
		octaveTextBuffer = renderer.createTextBuffer();
		float x0 = layoutTune.tenthW / 5;
        for (int i = 0; i < Octave.length; i++, x0 += octaveStep)
        {
        	octaveTextBuffer.addText(OctaveG[i], x0, 0);
        }
	}
	private void createOctaveLineBuffer()
	{
		octaveLineBuffer = new float[Octave.length * 4];
		float x0 = 0;
        for (int i = 0, j = 0; i < Octave.length; i++, x0 += octaveStep)
        {
        	octaveLineBuffer[j++] = x0; //x0
        	octaveLineBuffer[j++] = 2 * layoutTune.rect.height() / 3; // y0
        	octaveLineBuffer[j++] = x0; //x1
        	octaveLineBuffer[j++] = layoutTune.rect.height() / 3; // y1
        }
		
	}
	private void drawOctave(float x, float y, int oct)
	{
		int i;
		renderer.drawLines(x, y, octaveLineBuffer);

        // Octave text
        renderer.drawTextBuffer(octaveTextBuffer, textSize, x,  y + textSize * 2);
	}

    private String getOctaveName(int octave)
    {
        String octaveName =
                (octave == 0) ? "Big" :
                        (octave == 1) ? "Small" : String.valueOf(octave - 1);
        return octaveName;
    }

    float freq_0;
    float freq_1;
    float freq_2;
    float freq_3;
    float freq_4;

    private float getActualFrequency(float freqMax)
    {
        float freq;
        if(cbAveraging.checked)
        {
            freq = (freq_0 + freq_1 + freqMax) / 3;
            freq_0 = freq_1;
            freq_1 = freqMax;
            //freq_2 = drawData.freqMax;
            /*

            freq_2 = freq_3;
            freq_3 = freq_4;
            freq_4 = drawData.freqMax;
            */
        }
        else
        {
            freq = freqMax;
        }
        return freq;
    }

	private void drawTune(DrawData drawData)
	{
		if(drawData == null || drawData.spectrum == null)
			return;

        float freq = getActualFrequency(drawData.freqMax);
        RectF tuneRect = new RectF(layoutTune.rect.left, layoutTune.rect.top, layoutTune.rect.width(), layoutTune.rect.height() / 2);
        tuneRect.offset(0, textSize * 1.1f );
        drawPreciseTuner_impl1(tuneRect, freq, -1);

        // Frequency value
        String text = decFormat.format(freq);
        float centerX = (layoutTune.rect.left + layoutTune.rect.right) / 2;
        float textOffset = textSize / 4;
        renderer.setColor(64, 128, 255, 255);
        renderer.drawText(text, textSize, centerX + textOffset, layoutTune.rect.top + textSize);
        // Draw pointrer
        tuneRect.set(0, 0, layoutTune.tenthW / 2, textSize * 1.1f);
        tuneRect.offset(layoutTune.rect.width() / 2, 0);
        drawPointer(tuneRect);
	}

	private void drawSignal(DrawData drawData)
	{
		if(drawData == null || drawData.soundData == null)
			return;

		int count = Math.min(signal.soundData.length,  (int)layoutSignal.rect.width());
		
		float yMin = layoutSignal.rect.top;
		float yMax = layoutSignal.rect.bottom;
		
		float ycenter = (yMax + yMin) / 2;
		float factor = 0.1f;
		
		float starty = ycenter - (float)(drawData.soundData[0] * factor);
		if(starty < yMin) starty = yMin;
		else if(starty > yMax) starty = yMax;
		
		renderer.setColor(128, 255, 128, 255);
        renderer.drawLine(0, ycenter, layoutSignal.rect.width(), ycenter);

		for(int i = 1; i < count; i ++)
		{
			int dataIndex = (int)(i * signalSettings.xFactor);
			if(dataIndex >= drawData.soundData.length)
				break;
			float endy = ycenter - (float)(drawData.soundData[dataIndex] * signalSettings.yFactor);
			if(endy < yMin) endy = yMin;
			else if(endy > yMax) endy = yMax;
			renderer.drawLine(i - 1, starty, i, endy);
			starty = endy;
		}
	}
	private void drawSpectrumAxis(float x0, float y0, float x1, int stepCount, DrawData drawData)
	{
		renderer.drawLine(x0, y0, x1, y0);
		float stepLength = (float)(x1 - x0) / stepCount;
		
		float fmin = drawData.freqStep * drawData.freqIndexMin;
		float fmax = drawData.freqStep * drawData.freqIndexMax;
		
		float df = (float)((fmax - fmin) / stepCount);
        float f = (float) fmin;
        for (float x = x0; x <= x1; x += stepLength, f += df)
        {
        	renderer.drawLine(x, y0, x, y0 - 20);
        	String text = decFormat.format(f);
        	renderer.drawText(text, 2 * textSize / 3, x + 5, y0 - 5);
        }
	}
	private void drawSpectrum(DrawData drawData)
	{
		if(drawData  == null || drawData.spectrum == null)
			return;

		int spectSize = drawData.freqIndexMax - drawData.freqIndexMin + 1;
		float xFactor = layoutSpectrum.rect.width() / (float)spectSize;
		float ymax = layoutSpectrum.rect.bottom - layoutSpectrum.tenthH;
		int i;
		renderer.setColor(128, 128, 255, 255);
        //for(int j = 0; j < 100; j ++)
        int xIndexPrev = -1;
		for(i = drawData.freqIndexMin; i <= drawData.freqIndexMax; i ++)
		{
			int xIndex = (int)((i - drawData.freqIndexMin) * xFactor + 0.5);
            if(xIndex > xIndexPrev)
            {
                float yIndex = (float)(drawData.spectrum[i] * spectSettings.yFactor);
                if(yIndex >= 1f)
                {
                    yIndex = ymax - yIndex;
                    if(yIndex < layoutSpectrum.rect.top)
                        yIndex = layoutSpectrum.rect.top;
                    renderer.drawLine(xIndex, ymax, xIndex, yIndex);
                }
                xIndexPrev = xIndex;
            }
		}

		drawSpectrumAxis(layoutSpectrum.rect.left, layoutSpectrum.rect.bottom, layoutSpectrum.rect.right, 5, drawData);
	}
    private void drawRotatedLine(float x1, float y1, float x2, float y2, float cx, float cy, float degrees)
    {
        renderer.save();
        renderer.rotate(degrees, cx, cy);
        renderer.drawLine(x1, y1, x2, y2);
        renderer.restore();
    }

    private int getOctave(float freq)
    {
        int octave;
        for (octave = 0; octave < OctRange.length - 1; octave++)
        {
            if (freq >= OctRange[octave] && freq < OctRange[octave + 1])
                break;
        }
        if(octave > 4)
            octave = 0;
        return octave;
    }

    //private Bitmap createPresizeBitmap1(RectF rect, float freq, int octave)
    //tonePos = (tonePos + i) * bitmap.getWidth() / 12;
    private void drawPreciseTuner_impl1(RectF rect, float freq, int octave)
    {
        int i;
        if(octave < 0)
        {
            octave =  getOctave(freq);
        }
        float centerX = (rect.left + rect.right) / 2;
        // Find position inside two tones
        float tone = OctRange[octave] / 2;
        if(freq >= tone)
        {
            for(i = 0;  freq >= tone; tone *= pow_12, i ++);
            i--;
            i %= Octaveg.length;
            float toneFactor = freq / (tone / pow_12);
            float tonePos = (float)(Math.log(toneFactor) / Math.log(pow_12));
            Bitmap bitmap;

            if(currentOctave == OCTAVE_NONE)
            {
                tonePos = (tonePos + i) * tunerSettings.widthAllToneScale ;
                bitmap = tunerSettings.bitmapAllTone;
            }
            else
            {
                tonePos = (tonePos + i) * tunerSettings.widthOctaveToneScale;
                bitmap = tunerSettings.bitmapOctaveTone;
            }
            Rect bitmapRect = new Rect(0, 0, bitmap.getWidth() - 1, bitmap.getHeight() -1);

            float bitmapYPos = rect.top;// + tunerSettings.circleStep / 4;
            float bitmapXPos = centerX - tonePos;
            float bitmapWidth =  bitmap.getWidth();

            RectF toneRect = new RectF(bitmapXPos,
                    bitmapYPos, bitmapXPos + bitmapWidth, rect.bottom);

            float textOffset = textSize / 4;

            TuneBitmap tuneBitmap = renderer.createBitmap(bitmap);

            renderer.drawBitmap(toneRect.left, toneRect.top, tuneBitmap);
            if(bitmapXPos + bitmapWidth < rect.right) {
                toneRect.offset(bitmapWidth, 0);
                renderer.drawBitmap(toneRect.left, toneRect.top, tuneBitmap);
            }
            else if(bitmapXPos > 0)
            {
                toneRect.offset(-bitmapWidth, 0);
                renderer.drawBitmap(toneRect.left, toneRect.top, tuneBitmap);
            }
            tuneBitmap.dispose();
        }
    }


    private void drawPreciseTuner(RectF rect, float freq, int octave)
    {
        int i;
        if(octave < 0)
        {
            octave =  getOctave(freq);
        }
        float centerX = (rect.left + rect.right) / 2;
        // Find position inside two tones
        float tone = OctRange[octave] / 2;
        if(freq >= tone)
        {
            for(i = 0;  freq >= tone; tone *= pow_12, i ++);
            i--;
            i %= Octaveg.length;
            float toneFactor = freq / (tone / pow_12);
            float tonePos = (float)(Math.log(toneFactor) / Math.log(pow_12));
            Bitmap bitmap;

            if(currentOctave == OCTAVE_NONE)
            {
                tonePos = tonePos * tunerSettings.widthAllToneScale;
                bitmap = tunerSettings.bitmapAllTone;
            }
            else
            {
                tonePos = tonePos * tunerSettings.widthOctaveToneScale;
                bitmap = tunerSettings.bitmapOctaveTone;
            }
            Rect bitmapRect = new Rect(0, 0, bitmap.getWidth() - 1, bitmap.getHeight() -1);

            float bitmapYPos = rect.top;// + tunerSettings.circleStep / 4;
            float bitmapXPos = centerX - tonePos;
            float bitmapWidth =  bitmap.getWidth();

            int leftBitmapCount = (int)((bitmapXPos - rect.left + bitmapWidth) / bitmapWidth);
            int rightBitmapCount = (int)((rect.right - bitmapXPos + bitmapWidth) / bitmapWidth);
            int bitmapCount = leftBitmapCount + rightBitmapCount;
            float leftBitmapPos = bitmapXPos - leftBitmapCount * bitmapWidth;
            int octaveIndex = i - leftBitmapCount;
            while(octaveIndex < 0) octaveIndex += Octaveg.length;

            // Draw tuner to bitmap
            Bitmap image = Bitmap.createBitmap((int) rect.width(), (int) rect.height(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);
            Paint paint = new Paint();//Paint.FILTER_BITMAP_FLAG);

            int[] c = tunerSettings.colorToneBox;
            paint.setARGB(c[3], c[0], c[1], c[2]);
            paint.setTextSize(textSize);

            RectF toneRect = new RectF(leftBitmapPos - rect.left,
                    bitmapYPos - rect.top, leftBitmapPos - rect.left + bitmapWidth, rect.bottom - rect.top);

            float textOffset = textSize / 4;

            for(i = 0; i < bitmapCount; i ++, leftBitmapPos += bitmapWidth, octaveIndex ++)
            {
                canvas.drawBitmap(bitmap, bitmapRect, toneRect, paint);
                toneRect.offset(bitmapWidth, 0);

                if(octaveIndex >= Octaveg.length)
                    octaveIndex -= Octaveg.length;
                //renderer.drawText(Octaveg[octaveIndex], textSize * 2 / 3, leftBitmapPos + 3, textYPos);
                canvas.drawText(Octaveg[octaveIndex], leftBitmapPos - rect.left + textOffset,
                        rect.bottom - rect.top - textOffset, paint);
            }
            TuneBitmap tuneBitmap = renderer.createBitmap(image);
            renderer.drawBitmap(rect.left, rect.top, tuneBitmap);
            tuneBitmap.dispose();
            image.recycle();

            //renderer.clearClip();
        }
    }

    private void drawTuner(DrawData drawData)
    {
        if(drawData == null || drawData.spectrum == null || tunerSettings == null)
            return;
        // Now we have several maximums - one for each octave.
        // Choose the greater one

        if(showPressedButtonCount > 0)
            showPressedButtonCount --;
        else if(showPressedButtonCount == 0)
        {
            showPressedButtonCount = -1;
            setOctave(OCTAVE_NONE);
        }

        float freq = getActualFrequency(drawData.freqMax);

        // Get octave corresponding to the found frequency
        int octave;
        int i;
        String[] scaleValues;
        float angleStep;
        float factor;
        float range;
        if(currentOctave == OCTAVE_NONE)
        {
            octave = getOctave(freq);
            scaleValues = Octaves;
            angleStep = 25;
            // Use 7 octaves - from Big to 5
            factor = (float)(freq / (OctRange[0]));
            range = OctRange[Octaves.length - 1] * 2 / (OctRange[0]);
        }
        else
        {
            octave = getOctaveIndex(currentOctave);
            scaleValues = Octaveg;
            angleStep = 19;
            factor = freq / OctRange[octave];
            range = 2;
        }

        float angle = -(angleStep * (scaleValues.length + 2)- angleStep) / 2;

        // Position of frequency inside the octave
        float posD = (float)(Math.log(factor) / Math.log(range) * angleStep * scaleValues.length);

        // Get marker angle
        float markerAngle = angle + angleStep + posD;
        if(markerAngle < angle) markerAngle = angle;
        else if(markerAngle > -angle) markerAngle = -angle;

        if(currentOctave == OCTAVE_NONE)
        {
            TuneBitmap bitmap = renderer.createBitmap(tunerSettings.bitmapAllOctaves);
            renderer.drawBitmap(0, 0, bitmap);
            bitmap.dispose();
        }
        else
        {
            TuneBitmap bitmap = renderer.createBitmap(tunerSettings.bitmapOneOctave);
            renderer.drawBitmap(0, 0, bitmap);
            bitmap.dispose();
        }
        renderer.setAntialias(true);

         // Octave
        if(currentOctave != OCTAVE_NONE)
        {
            float textWidth = renderer.getTextWidth(Octaves[octave], textSize);
            float textPos = (tunerSettings.octaveBox.left + tunerSettings.octaveBox.right - textWidth) / 2;

            renderer.setColor(0, 0, 0, 255);
            renderer.drawText(Octaves[octave], textSize, textPos + 2,
                    tunerSettings.octaveBox.bottom - 5 + 3);
            renderer.setColor(64, 128, 255, 255);

            renderer.drawText(Octaves[octave], textSize, textPos,
                    tunerSettings.octaveBox.bottom - 5);
        }

        // Frequency value
        String text = decFormat.format(freq);
        float textWidth = renderer.getTextWidth(text, textSize);
        float textPos = (tunerSettings.freqBox.left + tunerSettings.freqBox.right - textWidth) / 2;
        renderer.setColor(0, 0, 0, 255);
        renderer.drawText(text, textSize, textPos + 3,
                tunerSettings.freqBox.bottom - 5 + 3);
        renderer.setColor(64, 128, 255, 255);
        renderer.drawText(text, textSize, textPos, tunerSettings.freqBox.bottom - 5);

        // Draw pointer
        renderer.setLineWidth(tunerSettings.circleStep / 10);
        renderer.setColor(255, 0, 0, 128);

        drawRotatedLine(tunerSettings.centerX, tunerSettings.centerY - tunerSettings.circleStep,
                tunerSettings.centerX, tunerSettings.centerY - 3 * tunerSettings.circleStep,
                tunerSettings.centerX, tunerSettings.centerY, markerAngle);
        renderer.setLineWidth(tunerSettings.circleStep / 20);
        renderer.setColor(0, 0, 0, 255);
        drawRotatedLine(tunerSettings.centerX + tunerSettings.circleStep / 20, tunerSettings.centerY - tunerSettings.circleStep,
                tunerSettings.centerX + tunerSettings.circleStep / 20, tunerSettings.centerY - 3 * tunerSettings.circleStep,
                tunerSettings.centerX, tunerSettings.centerY, markerAngle);

        RectF tunerRect = new RectF(
                0,
                tunerSettings.tunerBox.top + tunerSettings.circleStep / 4,
                tunerSettings.tunerBox.right,
                tunerSettings.tunerBox.bottom);

        //drawPreciseTuner(tunerRect, freq, octave);
        drawPreciseTuner_impl1(tunerRect, freq, octave);

        RectF pointerRect = new RectF(0, 0, tunerSettings.circleStep / 4 , tunerSettings.circleStep / 2);
        pointerRect.offset(tunerSettings.centerX, tunerSettings.tunerBox.top);

        drawPointer(pointerRect);

    }
    public void drawPointer(RectF rect)
    {
        tunerSettings.createPointerBitmap();
        TuneBitmap scaledBitmap = renderer.createBitmap(tunerSettings.bitmapPointer, (int) (rect.width() + 0.5f), (int) (rect.height() + 0.5f));
        if(scaledBitmap != null) {
            renderer.drawBitmap(rect.left - scaledBitmap.getWidth() / 2 + 1, rect.top, scaledBitmap);
            scaledBitmap.dispose();
        }
    }

    private RectF harmonicSuppressionRect = new RectF();
    private RectF useLowerToneRect = new RectF();
    private RectF syncByMaxRect = new RectF();
    private RectF averagingRect = new RectF();
    private RectF lockedLoopRect = new RectF();

    private void InitRect(RectF rect, float left, float top, float right, float bottom)
    {
        rect.left = left;
        rect.top = top;
        rect.right = right;
        rect.bottom = bottom;
    }

    private void drawSettings()
    {
        if(CheckBox.checkboxChecked == null)
            CheckBox.checkboxChecked = BitmapFactory.decodeResource(context.getResources(), R.drawable.checked);
        if(CheckBox.checkboxUnchecked == null)
            CheckBox.checkboxUnchecked = BitmapFactory.decodeResource(context.getResources(), R.drawable.unchecked);
        if(CheckBox.checkboxQuestion == null)
            CheckBox.checkboxQuestion = BitmapFactory.decodeResource(context.getResources(), R.drawable.question);

        //float wh_factor = layoutSettings.rect.width() / layoutSettings.rect.height();
        //float textSize = (float)(layoutSettings.rect.height() / 7 / wh_factor);

        float boxSize = (float)(layoutSettings.rect.height()) / 8;
        float textSize = (float)((layoutSettings.rect.width() - 4 * boxSize) / 10);

        renderer.setColor(0, 0, 0, 255);
        float yPos = layoutSettings.rect.top + textSize / 2;
        float xPos = textSize / 2;
        float xEnd = layoutSettings.rect.right;// - 2 * textSize;
        float yOffset = (float)(1.5 * boxSize);

        if(activeBox != null && activeBox.help != null)
        {
            for(int i = 0; i < activeBox.help.length; i ++)
                renderer.drawText(activeBox.help[i], textSize, xPos, yPos);
                yPos += 1.5 * textSize;
        }
        else
        {
            //textSize /= 1.5;

            cbHarmonicSuppression.draw(renderer, xPos, yPos, textSize, boxSize);
            cbHarmonicSuppression.drawHelpButtom(renderer, xEnd, boxSize);
            yPos += yOffset;
            cbUseLowerTone.draw(renderer, xPos, yPos, textSize, boxSize);
            cbUseLowerTone.drawHelpButtom(renderer, xEnd, boxSize);
            yPos += yOffset;
            cbSyncByMax.draw(renderer, xPos, yPos, textSize, boxSize);
            cbSyncByMax.drawHelpButtom(renderer, xEnd, boxSize);
            yPos += yOffset;
            cbAveraging.draw(renderer, xPos, yPos, textSize, boxSize);
            cbAveraging.drawHelpButtom(renderer, xEnd, boxSize);
            yPos += yOffset;
            cbLockedLoop.draw(renderer, xPos, yPos, textSize, boxSize);
            cbLockedLoop.drawHelpButtom(renderer, xEnd, boxSize);
        }
    }

    private void showDialog(String[] message)
    {
        //create a Dialog instance using the context
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_layout);//popup view is the layout you created
        dialog.setTitle(message[0]);
        TextView txt = (TextView)dialog.findViewById(R.id.textView);
        String msg = message[1];
        for(int i = 2; i < message.length; i ++)
        {
            msg += "\n";
            msg += message[i];
        }

        // Set text size equal to title size
        dialog.show();
        TextView title = (TextView)dialog.findViewById(android.R.id.title);
        float titleSize = title.getTextSize();

        //((TextView)dialog.findViewById(android.R.id.title)).setTextSize(titleSize / 2);
        //titleSize = ((TextView)dialog.findViewById(android.R.id.title)).getTextSize();
        //((TextView)dialog.findViewById(android.R.id.title)).setTextSize(titleSize);

        txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);
        txt.setText(msg);
    }


    private Boolean SettingsTouch(float x, float y, float endX)
    {
        Boolean res = false;
        if(layoutSettings.rect.contains(x, y))
        {
            res = true;
            if(cbHarmonicSuppression.pressed(x, y, endX))
            {
                cbHarmonicSuppression.checked = !cbHarmonicSuppression.checked;
                cbUseLowerTone.checked = false;
            }
            else if(cbUseLowerTone.pressed(x, y, endX))
            {
                cbUseLowerTone.checked = !cbUseLowerTone.checked;
                cbHarmonicSuppression.checked = false;
            }
            else if(cbSyncByMax.pressed(x, y, endX))
            {
                cbSyncByMax.checked = !cbSyncByMax.checked;
            }
            else if(cbAveraging.pressed(x, y, endX))
            {
                cbAveraging.checked = !cbAveraging.checked;
            }
            else if(cbLockedLoop.pressed(x, y, endX))
            {
                cbLockedLoop.checked = !cbLockedLoop.checked;
            }
            else if(cbHarmonicSuppression.helpPressed(x, y, endX))
            {
                showDialog(cbHarmonicSuppression.help);
            }
            else if(cbUseLowerTone.helpPressed(x, y, endX))
            {
                showDialog(cbUseLowerTone.help);
            }
            else if(cbSyncByMax.helpPressed(x, y, endX))
            {
                showDialog(cbSyncByMax.help);
            }
            else if(cbAveraging.helpPressed(x, y, endX))
            {
                showDialog(cbAveraging.help);
            }
            else if(cbLockedLoop.helpPressed(x, y, endX))
            {
                showDialog(cbLockedLoop.help);
            }
            else
                res = false;
        }
        return res;
    }
}
