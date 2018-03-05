package com.example.megatuner.Audio;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Vladimir-Desktop
 * Date: 26.10.13
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class AudioPlayer {
    public static class PlayThread extends Thread {
        boolean stop = false;
        AudioTrack audioTrack;
        double frequency = 0;
        double phase = 0;
        int sampleRate = 22050;
        int ramp;
        short ampl = 8000 ;


        public PlayThread(Activity activity, double frequency)
        {
            super();
            /*
            AudioManager aM = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            aM.setMode(AudioManager.MODE_IN_CALL);
            aM.setSpeakerphoneOn(false);
            */

            this.frequency = frequency;
        }
        public void run()
        {
            try
            {
                int bufferSize = 4096;
                short [] buffer = new short [bufferSize];
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                        AudioTrack.MODE_STREAM);

                audioTrack.play();
                int age = PLAY_AGE_CONTINUE;
                ramp = 0;//bufferSize / 2;
                // Get signal

                while(!stop)
                {
                    generateSignal(buffer, age);
                    audioTrack.write(buffer, 0, buffer.length);
                    age = PLAY_AGE_CONTINUE;
                }
                /*
                Arrays.fill(buffer, 0, buffer.length - 1, (short) 0);
                age = PLAY_AGE_END;
                ramp = bufferSize / 2;
                generateSignal(buffer, age);
                audioTrack.write(buffer, 0, ramp);
                */

                audioTrack.stop();
                audioTrack.release();
            }
            catch(Exception ex)
            {

            }
        }
        public synchronized void requestStop()
        {
            stop = true;
        }
        final int PLAY_AGE_START = 0;
        final int PLAY_AGE_CONTINUE = 1;
        final int PLAY_AGE_END = 2;

        private void generateSignal(short[] buffer, int age)
        {
            //generateSin(buffer, age);
            //generateSquare(buffer, age);
            //generateTriangle1(buffer, age);
            generateTriangle2(buffer, age);
        }

        private void generateSin(short[] buffer, int age)
        {
            int i = 0;
            double ampl1 = frequency > 440 ? ampl / 2 : (880 - frequency) / 440 * ampl / 2;

            float dp = (float)(2 * Math.PI * frequency / sampleRate);
            /*
            //phase %= (float)(2 * Math.PI);
            if(age == PLAY_AGE_END)
            {
                for(; i < ramp; i ++, phase += dp)
                {
                    if(phase >= (float)(2 * Math.PI))
                        phase -= (float)(2 * Math.PI);

                    buffer[i] = (short)(FloatMath.sin(phase) * ampl1 * (ramp - i) / ramp);
                }
            }
            else
            {
                if(age == PLAY_AGE_START )
                    for(; i < ramp; i ++, phase += dp)
                    {
                        if(phase >= (float)(2 * Math.PI))
                            phase -= (float)(2 * Math.PI);
                        buffer[i] = (short)(FloatMath.sin(phase) * ampl1 * (i / ramp));
                    }

                for(;i < buffer.length; i ++, phase += dp)
                {
                    buffer[i] = (short)(FloatMath.sin(phase) * ampl1);
                }
            }
            */
            for(;i < buffer.length; i ++, phase += dp)
            {
                //if(phaseSin >= (float)(2 * Math.PI))
                //    phaseSin -= (float)(2 * Math.PI);
                buffer[i] = (short)(Math.sin((float)phase) * ampl1);
            }
        }

        private void generateSquare(short[] buffer, int age)
        {
            int i = 0;

            double dp = 2 * Math.PI * frequency / sampleRate;

            for(;;)
            {
                phase %= (2 * Math.PI);
                while(phase < Math.PI)
                {
                    buffer[i++] = ampl;
                    if(i == buffer.length)
                        return;
                    phase += dp;
                }
                while(phase < 2 * Math.PI)
                {
                    buffer[i++] = (short)-ampl;
                    if(i == buffer.length)
                        return;
                    phase += dp;
                }
            }
        }
        private void generateTriangle1(short[] buffer, int age)
        {
            int i = 0;

            double dp = 2 * Math.PI * frequency / sampleRate;
            double k = ampl / Math.PI;

            for(;;)
            {
                phase %= (2 * Math.PI);
                while(phase < 2 * Math.PI)
                {
                    buffer[i++] = (short)(phase * k - ampl);
                    if(i == buffer.length)
                        return;
                    phase += dp;
                }
            }
        }
        private void generateTriangle2(short[] buffer, int age)
        {
            int i = 0;

            double dp = 2 * Math.PI * frequency / sampleRate;
            double k = 2 * ampl / Math.PI;

            for(;;)
            {
                phase %= (2 * Math.PI);
                while(phase < Math.PI)
                {
                    buffer[i++] = (short)(phase * k - ampl);
                    phase += dp;

                    if(i == buffer.length)
                        return;
                }
                while(phase < 2 * Math.PI)
                {
                    buffer[i++] = (short)(-phase * k + 3 *  ampl);
                    phase += dp;

                    if(i == buffer.length)
                        return;
                }
            }
        }
    }
                  /*
    private HashMap<String, PlayThread> threadMap = new HashMap<String, PlayThread>();
    public void Play(Activity activity, String note, double frequency)
    {
        if(!threadMap.containsKey(note))
        {
            PlayThread thread = new PlayThread(activity, frequency);
            thread.start();
            threadMap.put(note, thread);
        }
    }
    public void Stop(String note)
    {
        PlayThread thread = threadMap.get(note);
        if (thread != null)
        {
            thread.requestStop();
            threadMap.remove(note);
        }
    }
    */
}
