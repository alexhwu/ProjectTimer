package com.thealexwu;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

public class TaskTimerActivity extends Activity {
	// ToggleButton startStopBtn;
	// TextView timeText;
	// TextView taskLabel;

	LayoutParams startStopBtnLayoutParams;
	LayoutParams mtlParams;
	LayoutParams itlParams;
	LayoutParams timeTextParams;
	LayoutParams taskLabelParams;
	LayoutParams llParams;

	TableLayout mainTl;
	TableLayout innerTl;
	TableRow tr;
	LinearLayout ll;

	int viewIdCounter = 0;
	Timer timer = new Timer();
	final Handler handler = new Handler();

	public static final String PREFS_NAME = "MyPrefsFile";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
		int max_count = pref.getInt("max_count", 0);
		
		for(int i=0; i<max_count; i++) {
			int t = pref.getInt("Timer"+i, 0);
		
			String l = pref.getString("Label"+i, "");
			boolean isRunning = pref.getBoolean("IsRunning"+i, false);
			long timestamp = pref.getLong("Timestamp"+i, 0);
			
			addTimerFromStorage(l,t,isRunning, timestamp);
			viewIdCounter++;
		}
		

		ll = (LinearLayout) findViewById(R.id.linearLayout);
		Button newTimerBtn = (Button) findViewById(R.id.newTimerBtn);

		startStopBtnLayoutParams = ((Button) findViewById(R.id.button1)).getLayoutParams();
		timeTextParams = ((TextView) findViewById(R.id.timeText)).getLayoutParams();

		taskLabelParams = ((TextView) findViewById(R.id.taskLabel)).getLayoutParams();
		llParams = ((LinearLayout) findViewById(R.id.linearLayout)).getLayoutParams();
		mtlParams = ((TableLayout) findViewById(R.id.tableLayout)).getLayoutParams();
		itlParams = ((TableLayout) findViewById(R.id.tableLayout2)).getLayoutParams();

		newTimerBtn.setOnClickListener(new View.OnClickListener() {

			private void createTaskTimer(String label) {
				innerTl = new TableLayout(TaskTimerActivity.this);
				innerTl.setLayoutParams(itlParams);

				final TextView taskLabel = new TextView(innerTl.getContext());
				taskLabel.setText(label);
				taskLabel.setLayoutParams(taskLabelParams);
				taskLabel.setTextSize(18);
				taskLabel.setId(20000 + viewIdCounter);
				

				final TextView timeText = new TextView(TaskTimerActivity.this);
				timeText.setText("00:00:00");
				timeText.setLayoutParams(timeTextParams);
				timeText.setTextSize(28);
				timeText.setId(10000 + viewIdCounter);

				TableRow _tr2 = new TableRow(TaskTimerActivity.this);
				_tr2.addView(timeText);
				innerTl.addView(_tr2);

				TableRow _tr3 = new TableRow(TaskTimerActivity.this);
				_tr3.addView(taskLabel);
				innerTl.addView(_tr3);

				// add horizontal line
				ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 1);

				View hrView = new View(TaskTimerActivity.this);
				hrView.setLayoutParams(p);
				hrView.setBackgroundColor(Color.GRAY);
				hrView.getBackground().setAlpha(120);

				innerTl.addView(hrView);

				mainTl = new TableLayout(TaskTimerActivity.this);
				mainTl.setLayoutParams(mtlParams);
				mainTl.setId(viewIdCounter); // set the layout id for reference
											 // later

				//ViewGroup.LayoutParams btnParams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 55);
				final float scale = getResources().getDisplayMetrics().density;
				int pixels = (int) (65 * scale + 0.5f);

				final ToggleButton startStopBtn = new ToggleButton(TaskTimerActivity.this);
				startStopBtn.setText("Start");
				startStopBtn.setTextSize(12);
				startStopBtn.setHeight(pixels);
				startStopBtn.setWidth(pixels);
				startStopBtn.setId(30000+viewIdCounter);

				startStopBtn.setOnClickListener(new View.OnClickListener() {

					TimerTask timerTask = null;
					int counter = 0;

					public void onClick(View v) {

						if (startStopBtn.isChecked()) {
							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
					        SharedPreferences.Editor editor = settings.edit();
					        Calendar cal = Calendar.getInstance();
				        	editor.putLong("StartTimestamp"+viewIdCounter, cal.getTimeInMillis());
				        	
							startStopBtn.setText("Stop");
							timerTask = new TimerTask() {

								public void run() {
									handler.post(new Runnable() {

										public void run() {
											counter++; // seconds
											int hour = counter / 3600;
											int rem = counter % 3600;
											int min = rem / 60;
											int sec = rem % 60;
											
											

											timeText.setText(String.format("%02d:%02d:%02d", hour, min, sec));
											/*
											String counterStr = (hour < 10 ? "0" + hour : hour)
													+ ":"
													+ (min < 10 ? "0" + min : min)
													+ ":"
													+ (sec < 10 ? "0" + sec : sec);
											timeText.setText(counterStr);
											*/
										}
									});
								}
							};

							timer.scheduleAtFixedRate(timerTask, 1000, 1000);

						} else {
							startStopBtn.setText("Start");
							handler.removeCallbacks(timerTask);

							if (timerTask != null) {
								timerTask.cancel();
							}
						}
					}
				});

				TableRow _tr = new TableRow(mainTl.getContext());
				_tr.addView(startStopBtn);
				_tr.addView(innerTl);

				TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
						TableLayout.LayoutParams.FILL_PARENT,
						TableLayout.LayoutParams.FILL_PARENT);
				tableRowParams.setMargins(0, 2, 0, 2);
				_tr.setLayoutParams(tableRowParams);

				mainTl.addView(_tr);

				// add timer and task label to inner table
				ll.addView(mainTl);

				// set long press event
				// startStopBtn.setFocusable(false);
				registerForContextMenu(mainTl);
			}

			public void onClick(View v) {

				AlertDialog.Builder alert = new AlertDialog.Builder(TaskTimerActivity.this);
				alert.setTitle("Task Name");
				alert.setMessage("Enter Task Name");

				final EditText input = new EditText(TaskTimerActivity.this);
				input.setSingleLine(); // one line tall
				alert.setView(input);
				alert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								viewIdCounter++;
								createTaskTimer(input.getText().toString());
							}
						});

				alert.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								return;
							}
						});
				alert.show();
			}
		});

	}

	private void addTimerFromStorage(String label, final int seconds, boolean isRunning, long timestamp) {
		ll = (LinearLayout) findViewById(R.id.linearLayout);
		//Button newTimerBtn = (Button) findViewById(R.id.newTimerBtn);

		startStopBtnLayoutParams = ((Button) findViewById(R.id.button1)).getLayoutParams();
		timeTextParams = ((TextView) findViewById(R.id.timeText)).getLayoutParams();

		taskLabelParams = ((TextView) findViewById(R.id.taskLabel)).getLayoutParams();
		llParams = ((LinearLayout) findViewById(R.id.linearLayout)).getLayoutParams();
		mtlParams = ((TableLayout) findViewById(R.id.tableLayout)).getLayoutParams();
		itlParams = ((TableLayout) findViewById(R.id.tableLayout2)).getLayoutParams();

		

	
		innerTl = new TableLayout(TaskTimerActivity.this);
		innerTl.setLayoutParams(itlParams);

		final TextView taskLabel = new TextView(innerTl.getContext());
		taskLabel.setText(label);
		taskLabel.setLayoutParams(taskLabelParams);
		taskLabel.setTextSize(18);
		taskLabel.setId(20000 + viewIdCounter);
		

		final TextView timeText = new TextView(TaskTimerActivity.this);
		timeText.setText("00:00:00");
		timeText.setLayoutParams(timeTextParams);
		timeText.setTextSize(28);
		timeText.setId(10000 + viewIdCounter);

		
		TableRow _tr2 = new TableRow(TaskTimerActivity.this);
		_tr2.addView(timeText);
		innerTl.addView(_tr2);

		TableRow _tr3 = new TableRow(TaskTimerActivity.this);
		_tr3.addView(taskLabel);
		innerTl.addView(_tr3);

		// add horizontal line
		ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 1);

		View hrView = new View(TaskTimerActivity.this);
		hrView.setLayoutParams(p);
		hrView.setBackgroundColor(Color.GRAY);
		hrView.getBackground().setAlpha(120);

		innerTl.addView(hrView);

		mainTl = new TableLayout(TaskTimerActivity.this);
		mainTl.setLayoutParams(mtlParams);
		mainTl.setId(viewIdCounter); // set the layout id for reference
									 // later

		//ViewGroup.LayoutParams btnParams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 55);
		final float scale = getResources().getDisplayMetrics().density;
		int pixels = (int) (65 * scale + 0.5f);

		final ToggleButton startStopBtn = new ToggleButton(TaskTimerActivity.this);
		startStopBtn.setText("Start");
		startStopBtn.setTextSize(12);
		startStopBtn.setHeight(pixels);
		startStopBtn.setWidth(pixels);
		startStopBtn.setId(30000+viewIdCounter);
		
		int _seconds = seconds;
		
		if (isRunning) {
			// calculate the seconds to add since the activity was destroyed
			long now = Calendar.getInstance().getTimeInMillis();
			long elapsed = now - timestamp;  // milliseconds
			_seconds += ((int) (Math.round(elapsed/1000)));
			
		}
		
		final int finalSeconds = _seconds;
		
		// set the initial counter value
		int hour = finalSeconds / 3600;
		int rem = finalSeconds % 3600;
		int min = rem / 60;
		int sec = rem % 60;
		
		
		timeText.setText(String.format("%02d:%02d:%02d", hour, min, sec));
		
		
		startStopBtn.setOnClickListener(new View.OnClickListener() {

			TimerTask timerTask = null;
			int counter = finalSeconds;

			public void onClick(View v) {

				if (startStopBtn.isChecked()) {
					startStopBtn.setText("Stop");
					timerTask = new TimerTask() {

						public void run() {
							handler.post(new Runnable() {

								public void run() {
									counter++; // seconds
									int hour = counter / 3600;
									int rem = counter % 3600;
									int min = rem / 60;
									int sec = rem % 60;
									
									timeText.setText(String.format("%02d:%02d:%02d", hour, min, sec));
								}
							});
						}
					};

					timer.scheduleAtFixedRate(timerTask, 1000, 1000);

				} else {
					startStopBtn.setText("Start");
					handler.removeCallbacks(timerTask);

					if (timerTask != null) {
						timerTask.cancel();
					}
				}
			}
		});

		// start the timer is isRunning == true
		if (isRunning) {
			startStopBtn.performClick();
		}
		
		TableRow _tr = new TableRow(mainTl.getContext());
		_tr.addView(startStopBtn);
		_tr.addView(innerTl);

		TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
				TableLayout.LayoutParams.FILL_PARENT,
				TableLayout.LayoutParams.FILL_PARENT);
		tableRowParams.setMargins(0, 2, 0, 2);
		_tr.setLayoutParams(tableRowParams);

		mainTl.addView(_tr);

		// add timer and task label to inner table
		ll.addView(mainTl);

		// set long press event
		// startStopBtn.setFocusable(false);
		registerForContextMenu(mainTl);
	

		
	
	}
	
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle("Select Option");
		menu.add(0, v.getId(), 0, "Delete Timer");
	}

	public boolean onContextItemSelected(MenuItem item) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		String menuItemTitle = (String) item.getTitle();

		if (menuItemTitle == "Delete Timer") {

			// get the innerTl view
			TableLayout tv = (TableLayout) findViewById(item.getItemId());

			ll.removeView(tv);
			
		}
		return true;
	}

	protected void onSaveInstanceState(Bundle outState) {
		
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
       
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
       
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
        
    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        //Log.d("stop", "stop");
        saveTimers();
    }
    
    private SharedPreferences sharedPref;
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        saveTimers();
        
        // The activity is about to be destroyed.
        timer.cancel();
        
    }
    
    private void saveTimers() {
    	// save timer(s) to preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        
        int max_count = 0;
        
        
        for(int i=0; i<=viewIdCounter; i++) {
        	// if view with the corresponding id is found, then save to pref
        	TableLayout tv = (TableLayout) findViewById(i);
        	
        	if (tv == null) continue; // none found; continue to next iteration
        
        	
        	
        	// table layout found, which means a timer also exists; save the time value
        	TextView timeValue = (TextView) findViewById(10000+i);
        	int secs = convertToSeconds(timeValue.getText().toString());
        	editor.putInt("Timer"+max_count, secs);
        	
        	
        	// save the timer label
        	TextView labelValue = (TextView) findViewById(20000+i);
        	editor.putString("Label"+max_count, labelValue.getText().toString());
        	
        	// save the state of the timer; running or not
        	ToggleButton btn = (ToggleButton) findViewById(30000+i);
        	editor.putBoolean("IsRunning"+max_count, btn.isChecked());
        	
        	// save the timestamp; used for timers that are active when activity is destroyed
        	Calendar cal = Calendar.getInstance();
        	editor.putLong("Timestamp"+max_count, cal.getTimeInMillis());
        	
        	max_count++;
        }
        
        editor.putInt("max_count", max_count);
        editor.commit();
    }
    
    private int convertToSeconds(String t) {
    	// t variable is in the format of: 00:00:00
    	// 								hh:mm:ss
    	int sec = 0;
    	
    	String[] timeStr = t.split(":");
    	if (timeStr[0] != "00") {
    		// convert hours into seconds
    		int h = Integer.parseInt(timeStr[0]) * 60 * 60;
    		sec += h;
    	}
    	
    	if (timeStr[1] != "00") {
    		// convert minutes into seconds
    		int m = Integer.parseInt(timeStr[1]) * 60;
    		sec+= m;
    	}
    	
    	if (timeStr[2] != "00") {
    		// convert minutes into seconds
    		int s = Integer.parseInt(timeStr[2]);
    		sec+= s;
    	}
    	
    	 
    	return sec;
    }
}