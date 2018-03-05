package com.example.megatuner;


import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import com.example.megatuner.Audio.AudioPlayer;
import com.example.megatuner.Audio.AudioReceiver;
import com.example.megatuner.GLPackage.TuneViewGL;
import com.example.megatuner.draw2DPackage.TuneView2D;

public class MainActivity extends Activity {
    final String TAG = "myLogs";
	boolean busyTimer = false;
	AudioReceiver receiver;
	Handler handler;
	boolean isStarted = false;
	private int tunerViewNumber = 0;
    public static String TUNER_PREFERENCES = "TUNER_PREFERENCES";
	
	TuneSurface viewSurface;
	private final Lock lock = new ReentrantLock();

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		// Add view
		LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
		
		viewSurface = new TuneSurface(this);
        loadPreferences();
		viewSurface.initRenrerer2D(this);

		layout.addView((View) viewSurface.view);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		handler = new Handler() {
			@Override
            public void
            handleMessage(Message msg)
			{
				{
					lock.lock();
					try
                    {
                        if(msg.what == 1)
                        {
                            viewSurface.Process((short[])(msg.obj), msg.arg1);
                            receiver.setCurFreq(viewSurface.signal.getCurrentDrawData().freqMax);
                            receiver.setWaitAudioThread(false);
                        }
                    }
                    finally
                    {
                        lock.unlock();
                    }
				}
              }
		};
		receiver = new AudioReceiver(handler);
		int cores = getNumCores();
		receiver.multicore = cores > 1;
		buttonStart_Click(null);
	}
    void Test(short[] data)
    {
        data[0] = 0;
    }
	@Override
	protected void onPause(){
		StopAudioRecieverThread();
		finish();
		super.onPause();
	}

	/*
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
	    super.onConfigurationChanged(newConfig);
	    StopAudioRecieverThread();
	}
	*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	private void StartAudioRecieverThread()
	{
		StopAudioRecieverThread();
        receiver.start();
        //receiver1.start();
	}
	private void StopAudioRecieverThread()
	{
		if(receiver != null)
		{
			receiver.isRunning = false;
			receiver.setWaitAudioThread(false);
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
			}
		}
	}
	public void buttonStart_Click(View view)
	{
		//Button btn = (Button)findViewById(R.id.buttonStart);
		if(!isStarted)
		{
			StartAudioRecieverThread();
			//btn.setText(R.string.btnStop);
			isStarted =true;
		}
		else
		{
			StopAudioRecieverThread();
			//btn.setText(R.string.btnStart);
			isStarted = false;
			//draw2D.invalidate();
		}
	}
	
	public void buttonClose_Click(View v) 
	{
        // TODO Auto-generated method stub
        finish();
	}

    private void savePreferences()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(TUNER_PREFERENCES, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("harmonicSuppression", viewSurface.getHarmonicSuppression());
        editor.putBoolean("lowerTone", viewSurface.getUseFirst());
        editor.putBoolean("syncByMax", viewSurface.getSyncByMax());
        editor.putBoolean("averaging", viewSurface.getAveraging());
        editor.putBoolean("lockedLoop", viewSurface.getFrequencyControl());
        editor.commit();
    }
    private void loadPreferences()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(TUNER_PREFERENCES, Activity.MODE_PRIVATE);
        viewSurface.setHarmonicSuppression(sharedPreferences.getBoolean("harmonicSuppression", viewSurface.getHarmonicSuppression()));
        viewSurface.setUseFirst(sharedPreferences.getBoolean("lowerTone", viewSurface.getUseFirst()));
        viewSurface.setSyncByMax(sharedPreferences.getBoolean("syncByMax", viewSurface.getSyncByMax()));
        viewSurface.setAveraging(sharedPreferences.getBoolean("averaging", viewSurface.getAveraging()));
        viewSurface.setFrequencyControl(sharedPreferences.getBoolean("lockedLoop", viewSurface.getFrequencyControl()));
    }


    @Override
	public void onDestroy() {
        // Save preferences
        savePreferences();

		StopAudioRecieverThread();
		super.onDestroy();
		System.exit(0);
	}
	
	private void setOpenglGraphics()
	{
        viewSurface.lock.lock();
        try
        {
            viewSurface.view.Cleanup();
            LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
            layout.removeViewAt(tunerViewNumber);
            viewSurface.initRenrererGL(this);
            layout.addView((View) viewSurface.view);
        }
        finally
        {
            viewSurface.lock.unlock();
        }
	}
	private void setDraw2dlGraphics()
	{
        viewSurface.lock.lock();
        try
        {
            viewSurface.view.Cleanup();
            LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
            layout.removeViewAt(tunerViewNumber);
            viewSurface.initRenrerer2D(this);
            layout.addView((View) viewSurface.view);
        }
        finally
        {
            viewSurface.lock.unlock();
        }
    }
    private void setHarmonicSuppression()
    {
        boolean suppression = !viewSurface.getHarmonicSuppression();
        viewSurface.setHarmonicSuppression(suppression);
        // Either useFirst or harmonicSupression is available at once
        if(suppression)
            viewSurface.setUseFirst(false);
    }
    private void setUseFirstHarmonic()
    {
        boolean useFirst = !viewSurface.getUseFirst();
        viewSurface.setUseFirst(useFirst);
        // Either useFirst or harmonicSupression is available at once
        if(useFirst)
            viewSurface.setHarmonicSuppression(false);
    }

    private void setAveraging()
    {
        boolean averaging = viewSurface.getAveraging();
        viewSurface.setAveraging(!averaging);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.action_opengl:
			setOpenglGraphics();
			return true;
		case R.id.action_draw2d:
			setDraw2dlGraphics();
			return true;
		case R.id.action_exit:
			finish();
            return true;
        case R.id.action_showtuner:
            viewSurface.drawTunerFlag = true;
            viewSurface.drawSignalFlag = false;
            viewSurface.drawSpectrumFlag = false;
            viewSurface.drawSignalAndSpectrumFlag = false;
            viewSurface.updateLayouts();
            return true;
		case R.id.action_showsignal:
            viewSurface.drawTunerFlag = false;
            viewSurface.drawSignalFlag = true;
            viewSurface.drawSpectrumFlag = false;
            viewSurface.drawSignalAndSpectrumFlag = false;
            viewSurface.updateLayouts();
			return true;
		case R.id.action_showspectrum:
            viewSurface.drawTunerFlag = false;
            viewSurface.drawSignalFlag = false;
            viewSurface.drawSpectrumFlag = true;
            viewSurface.drawSignalAndSpectrumFlag = false;
            viewSurface.updateLayouts();
			return true;
		case R.id.action_shownothing:
            viewSurface.drawTunerFlag = false;
			viewSurface.drawSignalFlag = false;
			viewSurface.drawSpectrumFlag = false;
            viewSurface.drawSignalAndSpectrumFlag = true;
            viewSurface.updateLayouts();
			return true;
		case R.id.harmonic_suppression:
            setHarmonicSuppression();
			return true;
        case R.id.averaging:
            setAveraging();
            return true;
        case R.id.use_first_harmonic:
            setUseFirstHarmonic();
            return true;
        case R.id.sync_by_max:
            boolean syncByMax = !viewSurface.getSyncByMax();
            viewSurface.setSyncByMax(syncByMax);
            return true;
        case R.id.frequency_control:
            boolean frequency_control = !viewSurface.getFrequencyControl();
            viewSurface.setFrequencyControl(frequency_control);
            return true;
        case R.id.octNone:
            viewSurface.setOctave(TuneSurface.OCTAVE_NONE);
            return true;
        case R.id.octBig:
            viewSurface.setOctave(TuneSurface.OCTAVE_BIG);
            return true;
        case R.id.octSmall:
            viewSurface.setOctave(TuneSurface.OCTAVE_SMALL);
            return true;
        case R.id.oct1:
            viewSurface.setOctave(TuneSurface.OCTAVE_1);
            return true;
        case R.id.oct2:
            viewSurface.setOctave(TuneSurface.OCTAVE_2);
            return true;
        case R.id.oct3:
            viewSurface.setOctave(TuneSurface.OCTAVE_3);
            return true;
        case R.id.oct4:
            viewSurface.setOctave(TuneSurface.OCTAVE_4);
            return true;
        case R.id.oct5:
            viewSurface.setOctave(TuneSurface.OCTAVE_5);
            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    int[] menuOctaveIds = new int[] {
            R.id.octNone,
            R.id.octBig,
            R.id.octSmall,
            R.id.oct1,
            R.id.oct2,
            R.id.oct3,
            R.id.oct4,
            R.id.oct5
    };

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean checked = viewSurface.getHarmonicSuppression();
        MenuItem mi = menu.findItem(R.id.harmonic_suppression);
        mi.setChecked(checked);

        mi = menu.findItem(R.id.averaging);
        mi.setChecked(viewSurface.getAveraging());

        mi = menu.findItem(R.id.use_first_harmonic);
        mi.setChecked(viewSurface.getUseFirst());

        mi = menu.findItem(R.id.sync_by_max);
        mi.setChecked(viewSurface.getSyncByMax());

        mi = menu.findItem(R.id.frequency_control);
        mi.setChecked(viewSurface.getFrequencyControl());

        mi = menu.findItem(R.id.action_showtuner);
        mi.setChecked(viewSurface.drawTunerFlag);

        mi = menu.findItem(R.id.action_showsignal);
        mi.setChecked(viewSurface.drawSignalFlag);

        mi = menu.findItem(R.id.action_showspectrum);
        mi.setChecked(viewSurface.drawSpectrumFlag);

        mi = menu.findItem(R.id.action_shownothing);
        mi.setChecked(viewSurface.drawSignalAndSpectrumFlag);

        mi = menu.findItem(R.id.action_opengl);
        mi.setChecked(viewSurface.view instanceof TuneViewGL);

        mi = menu.findItem(R.id.action_draw2d);
        mi.setChecked(viewSurface.view instanceof TuneView2D);

        int curOctave = viewSurface.getOctave();
        for(int i = 0; i < menuOctaveIds.length; i ++)
        {
            mi = menu.findItem(menuOctaveIds[i]);
            boolean check = curOctave == TuneSurface.octaveIds[i];
            mi.setChecked(check);
            /*
            boolean isChecked =mi.isChecked();
            if(isChecked == true)
                mi.setChecked(true);
                */
        }
	    return true;
	}
	/**
	 * Gets the number of cores available in this device, across all processors.
	 * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
	 * @return The number of cores, or 1 if failed to get result
	 */
	private int getNumCores() {
	    //Private Class to display only CPU devices in the directory listing
	    class CpuFilter implements FileFilter {

			@Override
			public boolean accept(File pathname) {
				//Check if filename is "cpu", followed by a single digit number
	            if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
	                return true;
	            }
	            return false;
			}      
	    }

	    try {
	        //Get directory containing CPU info
	        File dir = new File("/sys/devices/system/cpu/");
	        //Filter to only list the devices we care about
	        File[] files = dir.listFiles(new CpuFilter());
	        //Return the number of cores (virtual CPU devices)
	        return files.length;
	    } catch(Exception e) {
	        //Default to return 1 core
	        return 1;
	    }
	}
    private HashMap<String, AudioPlayer.PlayThread> threadMap = new HashMap<String, AudioPlayer.PlayThread>();
    public void Play(Activity activity, String note, double frequency)
    {
        if(!threadMap.containsKey(note))
        {
            AudioPlayer.PlayThread thread = new AudioPlayer.PlayThread(activity, frequency);
            thread.start();
            threadMap.put(note, thread);
        }
    }
    public void Stop(String note)
    {
        AudioPlayer.PlayThread thread = threadMap.get(note);
        if (thread != null)
        {
            thread.requestStop();
            threadMap.remove(note);
        }
    }
}
