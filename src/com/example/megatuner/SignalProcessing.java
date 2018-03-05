package com.example.megatuner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.example.megatuner.Filters.Filter;
import com.example.megatuner.Utils.ArrayIndexComparator;

public class SignalProcessing {
	FFTf fft = new FFTf();

	public short[] soundData;
    public int dataLength;
	int bitRate;
	
	int freqIndexMin;
	int freqIndexMax;
	
	public boolean harmonicSuppression = true;
    public boolean syncByMax = true;
    public boolean frequencyControl = true;
    public boolean useFirst = false;

	public int freqFactor = 4;

    int drawDataIndex = 0;
    TuneSurface.DrawData[] drawData = new TuneSurface.DrawData[2];
    TuneSurface.DrawData currentDrawData;

    float[] filter;
    double pow_12 = Math.pow(2, 1.0/12);

	
	public SignalProcessing(int bitRate)
	{
		this.bitRate = bitRate;

        for(int i = 0; i < drawData.length; i ++)
            drawData[i] = new TuneSurface.DrawData();
	}

	public void CalcFFT()
	{
		if(this.soundData != null)
		{
            fft.AllocateBuffers(dataLength, freqFactor);
            //float fstep = (float)(this.bitRate) / (fft.X.length * freqFactor);
            // Test
			fft.Transform(soundData, dataLength, freqFactor, 1);
			freqIndexMin = 0;
			freqIndexMax = fft.spectrumLength / 2 - 1;
		}
	}
	public void SetRange(int min, int max)
	{
		freqIndexMin = min < 0 ? 0 : min >= fft.spectrumLength / 2 ? fft.spectrumLength / 2 - 1 : min;
		freqIndexMax = max < 0 ? 0 : max >= fft.spectrumLength / 2 ? fft.spectrumLength / 2 - 1 : max;
		if(freqIndexMin > freqIndexMax)
			freqIndexMin = freqIndexMax;

        /*
        if(fft.X != null && rk4 == null)
        {
            float freqStep = (float)(this.bitRate) / (fft.X.length * freqFactor);

            rk4 = new Filter.RK4FilterBand((freqIndexMin  + freqIndexMax) * freqStep / 2,
                    1.0 /this.bitRate, 1, 1);
            filter = new float[fft.X.length];
            rk4.getFilter(filter, freqStep, freqIndexMin, freqIndexMax);//freqIndexMin, freqIndexMax);
        }
        */
    }
	public void CalcMaximums()
	{
		GetMaximums();
	}
	public int getSpectLength()
	{
		return fft.X == null ? 0 : fft.spectrumLength / 2;
	}
    public TuneSurface.DrawData getCurrentDrawData()
    {
        return currentDrawData;
    }
	
	//private float kEqualizer = 1000;
	//private float fEqualizer = 196;


    float[] aveSpectrum;
    int aveCount = 0;

    private void GetMaximums()
    {
        float tSignal = (float)(dataLength) / (this.bitRate);
        int spectrumLength = fft.spectrumLength / 2;
        
        // Do of Controctave
        float firstDo = 65.406f;

        // Init draw data
        TuneSurface.DrawData data = drawData[drawDataIndex];
        data.lock.lock();
        try
        {
            data.freqIndexMin = freqIndexMin;
            data.freqIndexMax = freqIndexMax;
            data.soundData = soundData;
            data.dataLength = dataLength;
            data.freqStep = (float)(this.bitRate) / (fft.spectrumLength * freqFactor);
            if(data.spectrum == null || data.spectrum.length < spectrumLength)
                data.spectrum = new float[spectrumLength];

            int i;
            // Start from ContrOctave
            int indexMin = freqIndexMin;
            while (indexMin * data.freqStep < firstDo)
                indexMin ++;
            float maxFreq = data.freqIndexMax * data.freqStep;

            // Get global maximum
            float globalMax = 0;

            int freqIndexMaxLocal = syncByMax ? spectrumLength - 1 : freqIndexMax;

            for (i = indexMin; i <= freqIndexMax; i++)
            {
                data.spectrum[i] = (float)(fft.X[i] * fft.X[i] + fft.Y[i] * fft.Y[i]);
                if (data.spectrum[i] > globalMax)
                    globalMax = data.spectrum[i];
             }
            if(syncByMax)
            {
                for (;i <= freqIndexMaxLocal; i++)
                {
                    data.spectrum[i] = (float)(fft.X[i] * fft.X[i] + fft.Y[i] * fft.Y[i]);
                }
            }

            float threshold = globalMax / 200;

            List<Integer> maxIndicesList = new ArrayList<Integer>();
            List<Float> maxFrequencesList = new ArrayList<Float> ();

            float localMax = 0;
            int localMaxIndex = 0;
            boolean bMaxFound = false;

            for (i = indexMin; i <= freqIndexMaxLocal; i ++)
            {
                // Get all maximums that exceed threshold
                if (data.spectrum[i] > threshold)
                {
                    if (data.spectrum[i] > localMax)
                    {
                        if(!bMaxFound)
                            bMaxFound = true;
                        localMax = data.spectrum[i];
                        localMaxIndex = i;
                    }
                }
                else if (bMaxFound == true)
                {
                    maxIndicesList.add(localMaxIndex);
                    bMaxFound = false;
                    localMax = 0;
                }
            }
            if(localMax > 0)
            {
                // Lacal max found, but not added to the list
                maxIndicesList.add(localMaxIndex);
            }

            if(maxIndicesList.size() == 0)
                maxIndicesList.add(freqIndexMax);

            globalMax = 0;
            int globalMaxIndex = 0;

            float[] ampl = new float[1];
            float syncMax = 0;
            float syncFreq = 0;

            for (i = 0; i < maxIndicesList.size(); i++)
            {
                float freq = GetCenterFrequency(
                        data.freqStep, tSignal, maxIndicesList.get(i), data.spectrum, ampl);
                if(freq < maxFreq)
                {
                    maxFrequencesList.add(freq);
                    if (ampl[0] > globalMax)
                    {
                        globalMax = ampl[0];
                        globalMaxIndex = maxFrequencesList.size() - 1;//i
                    }
                }
                else
                {
                    if (ampl[0] > syncMax)
                    {
                        syncMax = ampl[0];
                        syncFreq =  freq;
                    }
                }
            }

            if(maxFrequencesList.size() > 0)
            {
                if(syncMax < globalMax)
                {
                    syncFreq = maxFrequencesList.get(globalMaxIndex);
                }

                if(useFirst)
                {
                    globalMaxIndex = 0;
                }
                else if(harmonicSuppression)
                {
                 // catch harmonies
                    float curF = maxFrequencesList.get(globalMaxIndex);
                    for (i = globalMaxIndex - 1; i >= 0; i--)
                    {
                        float factor = curF / maxFrequencesList.get(i);
                        if(factor > 2 - 0.04)
                        {
                            float fraction = factor - (int)(factor + 0.5);
                            if (fraction * fraction < 0.0016)
                            {
                                globalMaxIndex = i;
                            }
                        }
                    }
                }
                if(syncByMax)
                {
                    //syncFreq  contains a frequency of max harmonic in the spectrum
                    float curFreq = maxFrequencesList.get(globalMaxIndex);
                    if(curFreq > 0 && syncFreq > 0)
                    {
                        double dFactor = (double)syncFreq/curFreq;
                        double dHalfTones = Math.log(dFactor) / Math.log(pow_12);
                        int nHalfTones = (int)dHalfTones;
                        int nFactor = (int)dFactor;
                        if(dFactor - nFactor > 0.5)
                            nFactor ++;
                        if(dHalfTones - nHalfTones > 0.5)
                            nHalfTones ++;
                        // What is the max frequency - harmonic or halftone?
                        if(Math.abs(dFactor - nFactor) < Math.abs(dHalfTones - nHalfTones))
                        {
                            // It's seems to be a harmonic - use harmonic for Sync
                            curFreq = (float)(syncFreq / nFactor);
                        }
                        else
                        {
                            // Use Halftone for Sync
                            curFreq = (float)(syncFreq / Math.pow(pow_12, nHalfTones));
                        }
                        maxFrequencesList.set(globalMaxIndex, curFreq);
                    }
                }

                data.freqMax = maxFrequencesList.get(globalMaxIndex);
            }
            else
                data.freqMax = maxFreq;

            if(frequencyControl && globalMax > 1)
            {
                if(fft.rk4Filter == null)
                {
                    fft.rk4Filter = new Filter.RK4FilterParallel(data.freqMax, 1.0 / this.bitRate, 0.1);
                }
                else
                {
                    fft.rk4Filter.Init(data.freqMax, 1.0 / this.bitRate, 0.1);
                }
                if(syncByMax && data.freqMax != syncFreq)
                {
                    if(fft.rk4FilterMax == null)
                    {
                        fft.rk4FilterMax = new Filter.RK4FilterParallel(syncFreq, 1.0 / this.bitRate, 0.1);
                    }
                    else
                    {
                        fft.rk4FilterMax.Init(syncFreq, 1.0 / this.bitRate, 0.1);
                    }
                }
            }
            else
                fft.rk4Filter = null;

        }
        finally
        {
            data.lock.unlock();
        }
        if(data.freqMax < 0.0001)
            data.freqMax = 0;

        currentDrawData = data;

   }

    float Parabola(float f1, float A1, float f2, float A2, float f3, float A3, float[] A)
    {
        double a = (A3 - (f3*(A2 - A1) + f2 * A1 - f1*A2) / (f2 - f1)) / (f3 * (f3 - f1 - f2) + f1 * f2);
        double b = (A2 - A1) / (f2 - f1) - a * (f1 + f2);
        double c = (f2 * A1 - f1 * A2) / (f2 - f1) + a * f1 * f2;
        double f0;
        if (b != 0)
            f0 = -b / 2 / a;
        else
            f0 = 0;
        A[0] = (float)(c - a * f0 * f0);
        return (float)f0;
    }

    float Gauss1(float f1, float A1, float f2, float A2, float f3, float A3, float[] A)
    {
        /*
        A1 = (float)Math.sqrt(A1);
        A2 = (float)Math.sqrt(A2);
        A3 = (float)Math.sqrt(A3);
        */

        return Parabola(f1, A1, f2, A2, f3, A3, A);
        /*
        double f0, b;
        //A3 *= 0.975;
        double lnA1A2 = Math.log(A1 / A2);
        double lnA3A2 = Math.log(A3 / A2);

        if(lnA1A2 == 0)
        {
            f0 = (f1 + f2) / 2;
            b = f2*f2 - f3*f3 + 2 * f0 * (f3 - f2) / lnA3A2;
        }
        else
        {
            double ln = (lnA3A2 / lnA1A2);
            double numerator = (f2 * f2 - f1 * f1) * ln - f2 * f2 + f3 * f3;
            double denominator = (f3 - f2 - (f1 - f2) * ln) * 2;
            f0 = numerator / denominator;
            b = (f2*f2 - f1*f1 + 2 * f0 * (f1 - f2)) / lnA1A2;
        }
        if(b == 0)     // Strange case...
            A[0] = A2;
        else
            A[0] = (float)(A2 * Math.exp((f2 - f0)*(f2 - f0) / b));

        return (float)f0;
        */
    }

    float Gauss(float f1, float A1, float f2, float A2, float f3, float A3)
    {
        float ln = (float)(Math.log(A3 / A2) / Math.log(A1 / A2));

        float numerator = (f2 * f2 - f1 * f1) * ln - f2 * f2 + f3 * f3;
        float denominator = (f3 - f2 - (f1 - f2) * ln) * 2;
        return numerator / denominator;
    }
    private float GetCenterFrequency(float f0, float tSignal, int freqNumber, float[] abs2, float[] ampl)
    {
        int freqNumber0 = freqNumber - 1;
        int freqNumber2 = freqNumber + 1;
        if(freqNumber0 < 0) freqNumber0 = 0;
        if(freqNumber2 >= abs2.length) freqNumber2 = abs2.length - 1;

        float A1 = abs2[freqNumber0];
        float A2 = abs2[freqNumber];
        float A3 = abs2[freqNumber2];

        if (A1 == A3)
        {
        	ampl[0] = (float)A2;
            return (float)f0 * freqNumber;
        }
        if (A2 == 0)
        {
            A2 = 0;
            return 0;
        }

        float f1 = f0 * (freqNumber0);
        float f3 = f0 * (freqNumber2);
        float f2 = f0 * freqNumber;


        //float f = Gauss(f1, A1, f2, A2, f3, A3);
        float f = Gauss1(f1, A1, f2, A2, f3, A3, ampl);
        if(f > f3 || f < f1)
        {
        	f = 0;
        	ampl[0] = 0;
        }
        else
        {
        	;//ampl[0] = (float)FFTf.SingleDFTOpt(soundData, f, tSignal);
        }
        return f;
    }
    private boolean FindFrequency(float freq, float freqStep, float[] abs2, float[] ampl)
    {
        // Get average spect value in the range of freq
        int index = (int)(freq / freqStep);
        float ave = 0;
        for(int i = index - 5; i <= index + 5; i ++)
            ave += abs2[i];
        ave /= 11;

        float tSignal = (float)soundData.length / bitRate;
        ampl[0] = (float)FFTf.SingleDFTOpt(soundData, freq, tSignal);
        //float a2 = (float)FFTf.SingleDFTOpt(soundData, freq * 1.03f, tSignal);
        return ampl[0] > ave * 4f;
    }

}
