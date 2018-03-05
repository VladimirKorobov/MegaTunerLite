package com.example.megatuner.Audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.example.megatuner.TuneSurface;

import java.util.Random;

public class AudioReceiver{
    final String TAG = "myLogs1";
	public boolean isRunning;
	private Handler handler;
	private int sampleRateInHz;
	private int channelConfig;
	private int audioFormat;
    // Assuming, big enough
	private int bufferSize = 4096 * 8;
    private AudioRecord audioRecord;
	
	private final int BUFFCOUNT = 2;
    private boolean waitAudiThread = false;

	public boolean multicore = false; 

    private double dphase = 0;
    private int nSamples= 6590;
    // 0.15 ms.
    private double aveSignalTime = 0.15;

    public void setWaitAudioThread(boolean wait)
    {
        waitAudiThread = wait;
    }
    public void setCurFreq(double freq) {
        /*
        if(freq > 0.1)
        {
            int nPeriods = (int) (freq * this.aveSignalTime + 0.5);
            double dds = 1;
            int nMinIndex = 0;
            // Find the best match between period count and sample count
            double sampPerPeriod = this.sampleRateInHz / freq;
            for(int i = -5; i <= 5; i ++) {
                double dSamples = (nPeriods + i) * sampPerPeriod;
                nSamples = (int)(dSamples + 0.5);
                double ds = dSamples - nSamples;
                if(ds < 0) ds = -ds;
                if(ds < dds)
                {
                    dds = ds;
                    nMinIndex = i;
                }
            }
            nSamples = (int)((nPeriods + nMinIndex) *  this.sampleRateInHz / freq);
        }
        */
    }

	public AudioReceiver(Handler handler)
	{
		this.sampleRateInHz = 44100;//
		this.channelConfig =  AudioFormat.CHANNEL_IN_MONO;
		this.audioFormat = AudioFormat.ENCODING_PCM_16BIT;
		this.handler = handler;
		
		this.isRunning = false;
		
	}
    public void pause()
    {
        isRunning = false;
    }
    public void start() {
        //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        this.isRunning = true;
        AudioRecord record = null;

        // Buffer size is measured in bytes!!!
        int minBufferSize = AudioRecord.getMinBufferSize(this.sampleRateInHz, this.channelConfig, this.audioFormat);
        this.nSamples = (int)(this.aveSignalTime * this.sampleRateInHz + 0.5);
        this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                this.sampleRateInHz,
                this.channelConfig, this.audioFormat,
                minBufferSize * 10);
        if(this.audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
        {
            System.err.println("getState() != STATE_INITIALIZED");
            return;
        }

        try
        {
            this.audioRecord.startRecording();
        }
        catch(IllegalStateException e)
        {
            e.printStackTrace();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (audioRecord == null)
                    return;
                short[][] audioBuffers = new short[BUFFCOUNT][bufferSize];
                int bufferNumber = 0;
                while(isRunning)
                {
                    int samplesRead;
                    short[] audioBuffer = audioBuffers[bufferNumber];
                    samplesRead = audioRecord.read(audioBuffer, 0, nSamples);
                    if(samplesRead == AudioRecord.ERROR_INVALID_OPERATION)
                    {
                        System.err.println("read() returned ERROR_INVALID_OPERATION");
                        return;
                    }
                    if(samplesRead == AudioRecord.ERROR_BAD_VALUE)
                    {
                        System.err.println("read() returned ERROR_BAD_VALUE");
                        return;
                    }

                    /*
                    double f = 220 ;//+ 5 * Math.sin(d);
                    //d += 0.01;
                    double w = 2*Math.PI*f/sampleRateInHz;

                    //dphase = d;//dphase % (2 *Math.PI);
                    //dphase = 0;
                    for(int i = 0; i < samplesRead; i ++) {
                        audioBuffer[i] = (short) (audioBuffer[i] + Math.sin(dphase) * 1000);
                        dphase += w;
                    }
                    */

                    while(waitAudiThread)
                    {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                        }
                    }
                    // Will be clean in Main activity when ready
                    waitAudiThread = true;
                    // Send message
                    if(handler != null)
                    {
                        Message msg = handler.obtainMessage(1, samplesRead, 0, audioBuffer);
                        handler.sendMessage(msg);
                        bufferNumber = (bufferNumber + 1) % BUFFCOUNT;
                    }
                }
            }
        }).start();




    }

    private void correctSignal(short[] buffer, int sampleCount)
    {
        double f = 80.3;
        double w = 2*Math.PI*f/this.sampleRateInHz;
        dphase = 0;//dphase % (2 *Math.PI);
        for(int i = 0; i < sampleCount; i ++) {
            dphase += w;
            buffer[i] = (short) (buffer[i] + Math.sin(dphase) * 10);
        }
    }
/*
	@Override
    public void run()
    {
        //super.run();
		// Priority for audio
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		this.isRunning = true;
		AudioRecord record = null;

        // Buffer size is measured in bytes!!!
        int minBufferSize = AudioRecord.getMinBufferSize(this.sampleRateInHz, this.channelConfig, this.audioFormat);
		this.bufferSize = minBufferSize * 2;//4096 * 2;//minBufferSize / 2 + minBufferSize / 4;
		short[][] buffers;
        //this.bufferSize = -1;
		if(this.bufferSize < 0)
		{
			// Emulator
			buffers = new short[BUFFCOUNT][this.bufferSize];
		}
		else
		{
			// cycle buffer
			buffers = new short[BUFFCOUNT][this.bufferSize];
			//buffers = new short[BUFFCOUNT][this.bufferSize];
			record = new AudioRecord(MediaRecorder.AudioSource.MIC,
					this.sampleRateInHz, 
					this.channelConfig, this.audioFormat,
                    minBufferSize * 10);
			if(record.getState() != AudioRecord.STATE_INITIALIZED)
	        {
	            System.err.println("getState() != STATE_INITIALIZED");
	            return;
	        }
			try
			{
				 record.startRecording();
	        }
	        catch(IllegalStateException e)
	        {
	            e.printStackTrace();
	            return;
	        }		
		}
		int bufferNumber = 0;
        //this.bufferSize = 0;

		while(this.isRunning)
		{
			int samplesRead;
			if(this.bufferSize > 0)
			{
                Log.d(TAG, "record.read");
				samplesRead = record.read(buffers[bufferNumber], 0, buffers[bufferNumber].length);

                Log.d(TAG, "samplesRead == AudioRecord.ERROR_INVALID_OPERATION");
				if(samplesRead == AudioRecord.ERROR_INVALID_OPERATION)
	            {
	                System.err.println("read() returned ERROR_INVALID_OPERATION");
	                return;
	            }
				if(samplesRead == AudioRecord.ERROR_BAD_VALUE)
	            {
	                System.err.println("read() returned ERROR_BAD_VALUE");
	                return;
	            }
 			}
			else
			{
				// Emulator
				short[] buffer = buffers[bufferNumber];
				samplesRead = buffer.length;
                //generateTriangle2(buffer, 82.1);

				double f = 80.3;
				double w = 2*Math.PI*f/this.sampleRateInHz;// * (1 + 0.8 * Math.sin((float)phase / 50000));
                //dphase = random.nextDouble() % (2 * Math.PI);//dphase % (2 * Math.PI);
                //dphase = Math.PI / 3;

                dphase = 0;//dphase % (2 *Math.PI);
                for(int i = 0; i < samplesRead; i ++)
				{
                    //if(phase < 0)
                    //    phase = 0;
                    dphase += w;
					double arg = dphase;//w * (phase ++);
					//double s = Math.sin(arg) * 10 + Math.sin(2 * arg) * 10;
					//double s = Math.sin(arg) * 10 + Math.sin(1.5 * arg ) * 9 + Math.sin(1.7  * arg) * 8 ;
					double s = Math.sin(2 * arg) * 1000 + Math.sin(arg) * 100;// +  Math.sin(arg/2) * 200;
                    //double s = Math.sin(arg) * 1000;

					buffer[i] = (short)(s > 0 ? s + 0.5 : s - 0.5);
				}

				try
				{
					Thread.sleep(150);
				} 
				catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
				}
			}
            while(waitAudiThread)
            {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
            // Will be clean in Main activity when ready
            waitAudiThread = true;
            // Send message
            if(this.handler != null)
            {
                Message msg = this.handler.obtainMessage(3333, samplesRead, 0, buffers[bufferNumber]);
                this.handler.sendMessage(msg);
            }
            // Update buffer number
			//bufferNumber = (bufferNumber + 1) % BUFFCOUNT;

			if(!multicore)
			{
				int waitIndex = 0;
				// Wait processing
				while(waitAudiThread && waitIndex ++ < 20)
				{
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
					}
				}
				//wait = false;
			}

		}
		// Stop thread
		try
        {
            try
            {
            	if(record != null)
            		record.stop();
            }
            catch(IllegalStateException e)
            {
                return;
            }
        }
        finally
        {
            // Release resources
        	if(record != null)
        	{
        		record.release();
        	}
        }
    }
    private void generateTriangle2(short[] buffer, double frequency)
    {
        int i = 0;
        double ampl = Short.MAX_VALUE / 4;

        double dp = 2 * Math.PI * frequency / sampleRateInHz;
        double k = 2 * ampl / Math.PI;

        for(;;)
        {
            dphase %= (2 * Math.PI);
            while(dphase < Math.PI)
            {
                buffer[i++] = (short)(dphase * k - ampl);
                dphase += dp;
                if(i == buffer.length)
                    return;

            }
            while(dphase < 2 * Math.PI)
            {
                buffer[i++] = (short)(-dphase * k + 3 *  ampl);
                dphase += dp;
                if(i == buffer.length)
                    return;
            }
        }
    }
    */
}