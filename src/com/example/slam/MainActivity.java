package com.example.slam;

import java.io.File;
import java.io.PrintWriter;

import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private final int POLLING_INTERVAL = 500;
	
	private boolean alarmSet;
	private Intent intent;
	private PendingIntent sender;
	private AlarmManager am;
	
	public static MainActivity mThis = null;
	public static int markupNum;
	public static long startTime;
	public Context context;
	
	public static final String file_name = "records.txt";
	public static final File sdCard = Environment.getExternalStorageDirectory();
	public static final File recordfile = new File(sdCard.getAbsolutePath(),file_name);
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this.getApplicationContext();
		// reset from saved instance
		if(savedInstanceState != null){
			alarmSet = savedInstanceState.getBoolean("alarm");
			markupNum = savedInstanceState.getInt("marknumber");
			startTime = savedInstanceState.getLong("starttime");
			((TextView)findViewById(R.id.wfinfo)).setText(savedInstanceState.getString("info"));
			
			String lastState = (alarmSet ? "Stop" : "Start");
			((Button)findViewById(R.id.toggle_button)).setText(lastState);
		}
		else{
			alarmSet = false;
			markupNum = 0;
			startTime = 0;
		}
		
		intent = new Intent(this, PollReceiver.class);
		sender = PendingIntent.getBroadcast(
				this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
		final Button toggleRecording = (Button)findViewById(R.id.toggle_button);
		final Button markup = (Button)findViewById(R.id.markup_button);
		final Button clearData = (Button)findViewById(R.id.clear_button);
		
		toggleRecording.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i("PollReceiver","Alarm state: " + alarmSet);
		
				if(alarmSet){
					stopAlarm();
					toggleRecording.setText("Start");
				}
				else{
					startAlarm();
					toggleRecording.setText("Stop");
				}
			}
		});
		
		markup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				markupNum++;
			}
		});
		
		clearData.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				AlertDialog.Builder adb = new AlertDialog.Builder(arg0.getContext());
				adb.setTitle("Erase Records?");
				adb.setMessage("This action cannot be undone.")
				.setCancelable(false)
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				})
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						try{
							PrintWriter writer = new PrintWriter(recordfile);
							writer.print("");
							writer.close();
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				});
				
				AlertDialog warn = adb.create();
				warn.show();
				
			}
		});
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle savedBundle){
		super.onSaveInstanceState(savedBundle);
		
		savedBundle.putBoolean("alarm", alarmSet);
		savedBundle.putInt("marknumber", markupNum);
		savedBundle.putLong("starttime", startTime);
		
		savedBundle.putString("info", ((TextView)findViewById(R.id.wfinfo)).getText().toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(isFinishing()){
			stopAlarm();
		}
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    mThis = this;
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    mThis = null;
	}
	
	public void startAlarm(){
		markupNum = 0;
		startTime = System.currentTimeMillis();
		long firstTime = SystemClock.elapsedRealtime() + POLLING_INTERVAL;
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstTime, POLLING_INTERVAL, sender);
		alarmSet = true;
	}
	
	public void stopAlarm(){
		am.cancel(sender);
		alarmSet = false;
	}
}
