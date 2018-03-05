package com.example.megatuner;

import com.example.megatuner.Filters.Filter;

import java.util.Arrays;
import java.util.Random;

public class FFTf {
	// This computes an in-place complex-to-complex FFT 
    // x and y are the real and imaginary arrays of 2^m points.
    // dir =  1 gives forward transform
    // dir = -1 gives reverse transform 
    // Taken from http://paulbourke.net/miscellaneous/dft/
	
	public float[] X;
	public float[] Y;
	
	private static float[] _sin;
    private static float[] _cos;
    static Random random = new Random();
    Filter.RK4 rk4Filter;
    Filter.RK4 rk4FilterMax;
    float[] filtered = null;
    public int spectrumLength;

    private static void InitCos()
    {
        if (_cos == null)
        {
            _cos = new float[1024];
            _sin = new float[1024];

            int i;
            float dPI = (float)Math.PI * 2 / _cos.length;
            for ( i = 0; i < _cos.length; i++)
            {
                _cos[i] = (float)Math.cos(i * dPI);
            }
            for (i = 0; i < _cos.length; i++)
            {
                _sin[i] = (float)Math.sin(i * dPI);
            }
        }
    }

    private static float Cos(float arg)
    {
        float dIndex = (float)(arg / (Math.PI * 2) * _cos.length);
        int iIndex = (int)dIndex;
        int index = iIndex & (_cos.length - 1);
        if (index == _cos.length - 1)
            return _cos[_cos.length - 1];

        float fraction = dIndex - iIndex;
        return _cos[index] * (1 - fraction) + _cos[index + 1] * fraction;
    }
    private static float Sin(float arg)
    {
        float dIndex = (float)(arg / (Math.PI * 2) * _cos.length);
        int iIndex = (int)dIndex;
        int index = iIndex & (_cos.length - 1);
        if (index == _sin.length - 1)
            return _sin[_sin.length - 1];

        float fraction = dIndex - iIndex;
        return _sin[index] * (1 - fraction) + _sin[index + 1] * fraction;
    }

    public int getSpectrumSize(int dataLength, int freqFactor)
    {
        int N = 1;
        while (N < dataLength)// / freqFactor)
        {
            N <<= 1;
        }
        //N <<= 1;
        return N;
    }

    public void AllocateBuffers(int dataLength, int freqFactor)
    {
        spectrumLength = getSpectrumSize(dataLength, freqFactor);

        if(X == null || X.length < spectrumLength) {
            X = new float[spectrumLength];
            Y = new float[spectrumLength];
        }
        Arrays.fill(X, 0, spectrumLength - 1, 0);
        Arrays.fill(Y, 0, spectrumLength - 1, 0);
    }


    //private short[] data1;

	public void Transform(short[] data, int dataLength, int freqFactor, int dir)
	{
        AllocateBuffers(dataLength, freqFactor);

        int i = 0;
        int j = 0;

        if (rk4Filter != null) {
            //if(filtered == null)
            //filtered = new float[(dataLength + 1) / 2];
            // filtered = new float[dataLength * 2];
            for (i = 0; i < dataLength; i += freqFactor) {
                //filtered[j ++] = (float)rk4Filter.Next(data, i);
                X[j++] = (float) rk4Filter.Next(data, i);
            }
            /*
            if(rk4FilterMax != null)
            {
                j = 0;
                for(i = 0; i < dataLength; i ++)
                    filtered[j ++] += (float)rk4FilterMax.Next(data, i);
            }
            */

            //freqFactor /= 2;
            //for(i = 0, j = 0; i < dataLength; i += freqFactor, j ++)
            /*
            for(i = 0; i < j; i ++)
            {
                X[i] = filtered[i];
            }
            */
        } else {
            //filtered = null;
            // int k = 0;
            for (; i < dataLength; i += freqFactor, j++) {
                X[j] = data[i];
            }
        }

		//Transform(X, Y, pow2, dir, data.length / freqFactor);
		Transform1(X, Y, spectrumLength, -dir, dataLength / freqFactor);
	}
	

    private static void Transform(float[] x, float[] y, int m, int dir, int norm)
    {
        int n, i, i1, j, k, i2, l, l1, l2;
        float c1, c2, tx, ty, t1, t2, u1, u2, z;

        /* Calculate the number of points */
        n = 1;
        for (i = 0; i < m; i++)
            n *= 2;

        /* Do the bit reversal */
        i2 = n >> 1;
        j = 0;
        for (i = 0; i < n - 1; i++)
        {
            if (i < j)
            {
                tx = x[i];
                ty = y[i];
                x[i] = x[j];
                y[i] = y[j];
                x[j] = tx;
                y[j] = ty;
            }
            k = i2;
            while (k <= j)
            {
                j -= k;
                k >>= 1;
            }
            j += k;
        }

        /* Compute the FFT */
        c1 = -1.0f;
        c2 = 0.0f;
        l2 = 1;
        for (l = 0; l < m; l++)
        {
            l1 = l2;
            l2 <<= 1;
            u1 = 1.0f;
            u2 = 0.0f;
            for (j = 0; j < l1; j++)
            {
                for (i = j; i < n; i += l2)
                {
                    i1 = i + l1;
                    t1 = u1 * x[i1] - u2 * y[i1];
                    t2 = u1 * y[i1] + u2 * x[i1];
                    x[i1] = x[i] - t1;
                    y[i1] = y[i] - t2;
                    x[i] += t1;
                    y[i] += t2;
                }
                z = u1 * c1 - u2 * c2;
                u2 = u1 * c2 + u2 * c1;
                u1 = z;
            }
            c2 = (float)Math.sqrt((1.0 - c1) / 2.0);
            if (dir == 1)
                c2 = -c2;
            c1 = (float)Math.sqrt((1.0 + c1) / 2.0);
        }

        /* Scaling for forward transform */
        if (dir == 1)
        {
            for (i = 0; i < n; i++)
            {
                /*
                x[i] /= n;
                y[i] /= n;
                 * */
                x[i] /= norm;
                y[i] /= norm;
            }
        }
    }
    
    public static void Transform1(float[] x, float[] y, int N, int I, int factor)
    {
    	InitCos();
        float c, s, t1, t2, t3, t4, u1, u2, u3;
        int i, j, p, l, L, M, M1, K;
        L = N;
        M = N / 2;
        M1 = N - 1;
        while (L >= 2)
        {
            l = L / 2; u1 = 1.0f; u2 = 0.0f; t1 = (float)Math.PI / (float)l;
            c = (float)Cos(t1); s = (-1) * I * (float)Sin(t1);
            j = 0;
            for (; j < l; j++)
            {
                for (i = j; i < N; i += L)
                {
                    p = i + l;
                    t1 = x[i] + x[p];
                    t2 = y[i] + y[p];
                    t3 = x[i] - x[p];
                    t4 = y[i] - y[p];
                    x[p] = t3 * u1 - t4 * u2;
                    y[p] = t4 * u1 + t3 * u2;
                    x[i] = t1; y[i] = t2;
                }
                u3 = u1 * c - u2 * s;
                u2 = u2 * c + u1 * s; u1 = u3;
            }
            L /= 2;
        }
        j = 0;
        for (i = 0; i < M1; i++)
        {
            if (i > j)
            {
                t1 = x[j]; t2 = y[j];
                x[j] = x[i]; y[j] = y[i];
                x[i] = t1; y[i] = t2;
            }
            K = M;
            while (j >= K)
            {
                j -= K; K /= 2;
            }
            j += K;
        }
        if (I < 1)
        {
        	float k = (factor == 0) ? 1.0f / N : 1.0f / factor;
            for (i = 0; i < N; i++)
            {
                x[i] *= k;
                y[i] *= k;
            }
        }
    }

    private static int intPhase = 0;
    private static float floatPhase = 0;
    private static float randomPhase = 0;

    public static float SingleDFTOpt(short[] x, float f, float tSignal)
    {
    	InitCos();
        // tSignal - signal duration
    	float cr = 0;
    	float ci = 0;

    	float dt = (float)(2 * Math.PI) * f * tSignal / x.length * 2;
        float phase = 0;

        for (int i = 0; i < x.length; i += 2, phase += dt)
        {
            cr += x[i] * Cos(phase); //Math.cos(t);
            ci -= x[i] * Sin(phase);////Math.sin(t);
        }

        cr /= (x.length / 2);
        ci /= (x.length / 2);

        float res = (float)(cr * cr + ci * ci);
        return res;
    }

    /*
    public static float SingleDFT(short[] x, float f, float tSignal)
    {
    	InitCos();
        // tSignal - signal duration
    	float cr = 0;
    	float ci = 0;
    	float dt = (float)(2 * Math.PI) * f * tSignal / x.length;
    	float t = phase * dt;
        for (int i = 0; i < x.length; i++, t += dt, phase ++)
        {
            cr += x[i] * Cos(t); //Math.cos(t);
            ci -= x[i] * Sin(t);////Math.sin(t);
        }

        cr /= x.length;
        ci /= x.length;

        float res = (float)(cr * cr + ci * ci);
        return res;
    }
    */

}
