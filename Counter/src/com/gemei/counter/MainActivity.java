package com.gemei.counter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity 
{	
	private SoundPool spool; //Make soundPool object called "spool"
	private PowerManager myPowermanager;
	private PowerManager.WakeLock CPUWL;
	private int upClick;
	private int downClick;
	private int reset;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//No Title Bar
		setContentView(R.layout.activity_main);
		
		myPowermanager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        CPUWL = myPowermanager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "CPU_WAKE_LOCK");
        
		//Use the audio manger and import the audio files
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		/*make a new soundPool object with maximum 10 sound flies and 100% sound
		  quality that use audio manger API to stream the sound file*/
        spool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        //Loading the upClick, downClick and reset button sound into the soundPool object spool
        upClick = spool.load(this, R.raw.upclick, upClick);
        downClick = spool.load(this, R.raw.downclick, downClick);
        reset = spool.load(this, R.raw.reset, reset);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
	    menu.findItem(R.id.menu_stayAwake).setChecked(getCheckBoxValue("screenLock"));

	    return super.onPrepareOptionsMenu(menu);
	}
	
	@SuppressLint("Wakelock")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    // Handle item selection
	    switch (item.getItemId()) 
	    {
	    	//If reset count item selected do that
	        case R.id.menu_resetButton:
	        	//go to resetCounter method
	        	savePreResetValue(getCounterValue());//Get the last counter value right before reseting it
	        	resetCounter();
	        	playResetClick();
	            return true;
		    //If reset count item selected do that
	        case R.id.menu_lastValue:
	        	//go to send the value of last count to sandValue method
	        	showValue(getPreResetValue());
	            return true;
	        //If help item selected do that
	        case R.id.menu_settings:
	        	//Start the help activity
	        	showHelp();
	            return true;
	        case R.id.menu_stayAwake:
	        	//Screen Wake Lock CheckBox
	        	if (item.isChecked()==true)
	        	{
	        		item.setChecked(false);
	        		saveCheckBoxValue(false, "screenLock");
	            	if(CPUWL.isHeld()==true)
	            		CPUWL.release();
	        	}	
	            else 
	            {
	            	item.setChecked(true);
	            	saveCheckBoxValue(true, "screenLock");
	            	if(CPUWL.isHeld()==false)
	            		CPUWL.acquire();
	            }
	            	
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@SuppressLint("Wakelock")
	private void screenLockChecker()
	{
		if(getCheckBoxValue("screenLock")==true)
		{
			if(CPUWL.isHeld()==false)
				CPUWL.acquire();
		}
	}
	
	private void showHelp()
	{
		/*Make A New Alert Dialog*/
		AlertDialog.Builder helpDialog = new AlertDialog.Builder(this);
		helpDialog.setTitle("Help!");
		TextView myMsg = new TextView(this);
		myMsg.setTextSize(18);
		myMsg.setText("Use the rocker buttons to increase and decrease the count.");
		myMsg.setPadding(10, 8, 10, 8);
		//myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		
		helpDialog.setView(myMsg).setNegativeButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// User cancelled the dialog
			}
		});
		
		helpDialog.show();
	}
	
	//send value of last count to activity LastValue
	private void showValue(int value)
	{
        /*Make A New Alert Dialog*/
		String toString = String.valueOf(value);
		AlertDialog.Builder lastValueDialog = new AlertDialog.Builder(this);
		lastValueDialog.setTitle("Last Counter Value");
		TextView myMsg = new TextView(this);
		myMsg.setTextSize(30);
		myMsg.setText(toString);
		myMsg.setPadding(10, 10, 10, 10);
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		
		lastValueDialog.setView(myMsg).setNegativeButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// User cancelled the dialog
			}
		});
		
		lastValueDialog.show();
	}
	
	//Trace the volume buttons actions
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) 
	{
	    int action = event.getAction();
	    int keyCode = event.getKeyCode();
	    
	        switch (keyCode) 
	        {
	        //When clicking the upper volume button
	        case KeyEvent.KEYCODE_VOLUME_UP:
	            if (action == KeyEvent.ACTION_UP) 
	            {
	            	   	
	            	//play up click sound
	            	playUpClick();
	            	
	            	//Get text view from the xml file
	            	TextView countView = (TextView) findViewById(R.id.text_count);
	        		
	            	//Get the text from the text view file in the xml and convert it to string
	        		String toString = countView.getText().toString();
	        		//Converting the String value to int value
	        		int toInt = Integer.parseInt(toString);
	        		toInt++;
	        		//Converting int value to String value to add to the text view
	        		toString = String.valueOf(toInt);
	        		countView.setText(toString);//text views only take string values
	        		
	        		saveCounterValue(toInt);//call saveCounter method and give the value toInt
	            }
	            return true;
	            
	        //When clicking the down volume button
	        case KeyEvent.KEYCODE_VOLUME_DOWN:
	            if (action == KeyEvent.ACTION_DOWN) 
	            {	
	            	//play down click sound
	            	playDownClick();
	            	
	            	//Get text view from the xml file
	            	TextView countView = (TextView) findViewById(R.id.text_count);
	        		
	            	//Get the text from the text view file in the xml and convert it to string
	        		String toString = countView.getText().toString();
	        		//Converting the String value to int value
	        		int toInt = Integer.parseInt(toString);
	        		toInt--;
	        		//Converting int value to String value to add to the text view
	        		toString = String.valueOf(toInt);
	        		countView.setText(toString);//text views only take string values
	        		
	        		saveCounterValue(toInt);//call saveCounter method and give the value toInt
	            }
	            return true;
	            
	        default:
	            return super.dispatchKeyEvent(event);//Releasing volume buttons
	        }
	    }
	
	//rest the counter value on click
	private void resetCounter()
	{
		TextView countView = (TextView) findViewById(R.id.text_count);
		//Set counter value to 0
		countView.setText("0");
		saveCounterValue(0);//call saveCounter method and give the value 0
	}
	
	//up click sound method
	private void playUpClick()
	{
		//using the audio manger API
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //Setting the volume value as a float value
        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        /*play the "upClick" sound with volume value on left and right with
          priority value "1" and looping 0 times and setting the float value to one decimal point*/
        spool.play(upClick, volume, volume, 1, 0, 1f);
    }
	
	//down click sound method
	private void playDownClick()
	{
		//using the audio manger API
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //Setting the volume value as a float value
        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        /*play the "upClick" sound with volume value on left and right with
          priority value "1" and looping 0 times and setting the float value to one decimal point*/
        spool.play(downClick, volume, volume, 1, 0, 1f);
    }
	
	//reset click sound method
	private void playResetClick()
	{
		//using the audio manger API
	    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
	    //Setting the volume value as a float value
	    float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	    /*play the "reset" sound with volume value on left and right with
	      priority value "1" and looping 0 times and setting the float value to one decimal point*/
	    spool.play(reset, volume, volume, 1, 0, 1f);
	 }
	
	//method to save counter value
	private void saveCounterValue(int value)
	{
		//Saving the counter value from the shared preferences
	    SharedPreferences myPrefs = getSharedPreferences("counterValue", MODE_PRIVATE);
	    //Make editor object to write to the shared preferences
	    SharedPreferences.Editor editor = myPrefs.edit();
	    //Saving the counter value under the name of "savedCounterValue"
		editor.putInt("savedCounterValue", value);
		//Commit the action and save the value
		editor.commit();
	}
	
	//method to get counter saved value
	private int getCounterValue()
	{
		//Retrieving the counter value from the shared preferences
		SharedPreferences myPrefs = getSharedPreferences("counterValue", MODE_PRIVATE);
		int intCountrValue = myPrefs.getInt("savedCounterValue", 0);
		return intCountrValue;
	}
	
	//Method to save the last counter value before reset
	private void savePreResetValue(int value)
	{
		//Saving the counter value from the shared preferences
	    SharedPreferences myPrefs = getSharedPreferences("preResetValue", MODE_PRIVATE);
	    //Make editor object to write to the shared preferences
	    SharedPreferences.Editor editor = myPrefs.edit();
	    //Saving the counter value under the name of "preResetValue"
		editor.putInt("preResetValue", value);
		//Commit the action and save the value
		editor.commit();
	}
	
	//Method to get the last counter value before reset
	private int getPreResetValue()
	{
		//Retrieving the counter value from the shared preferences
		SharedPreferences myPrefs = getSharedPreferences("preResetValue", MODE_PRIVATE);
		int intCountrValue = myPrefs.getInt("preResetValue", 0);
		return intCountrValue;
	}
	

	//Method to save the last CheckBox status
	private void saveCheckBoxValue(boolean value, String name)
	{
		//Saving the CheckBox value from the shared preferences
		SharedPreferences myPrefs = getSharedPreferences(name, MODE_PRIVATE);
		//Make editor object to write to the shared preferences
		SharedPreferences.Editor editor = myPrefs.edit();
		//Saving the CheckBox value under the name of "preResetValue"
		editor.putBoolean("Saved"+name, value);
		//Commit the action and save the value
		editor.commit();
	}
			
	//Method to get the last CheckBox status before reset
	private boolean getCheckBoxValue(String name)
	{
		//Retrieving the CheckBox value from the shared preferences
		SharedPreferences myPrefs = getSharedPreferences(name, MODE_PRIVATE);
		boolean CheckBoxstatus = myPrefs.getBoolean("Saved"+name, false);
		return CheckBoxstatus;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();//Always call the super.onResume(); when overriding onResume() method
		
		screenLockChecker();
		
		//Retrieving the count value from the getCounterValue() method and convert it to string
		String toString = String.valueOf(getCounterValue());
		TextView countView = (TextView) findViewById(R.id.text_count);
		countView.setText(toString);
	}
	
	@Override()
	public void onPause()
	{
		super.onPause();
		if(CPUWL.isHeld()==true)
			CPUWL.release();
	}
}
