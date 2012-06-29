package com.cooltofu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cooltofu.db.TimerDbAdapter;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class ProjectTimerActivity extends Activity {
	private final static int TIME_ID_PREFIX = 10000;
	private final static int TASK_LABEL_ID_PREFIX = 20000;
	private final static int START_STOP_ID_PREFIX = 30000;
	private final static int EMAIL_TIMERS_MENU_ID = 40000;
	private final static int DELETE_ALL_TIMERS_MENU_ID = 50000;
	private final static int RESET_ALL_TIMERS_MENU_ID = 60000;
	
	private static final String TIME_FORMAT = "%02d:%02d:%02d";
	final static String ALERT_NEW_TIMER_TITLE = "Add New Timer";
	final static String ALERT_NEW_TIMER_MSG = "Enter Timer Label";
	final static String OK_BTN_STRING = "Ok";
	final static String CANCEL_BTN_STRING = "Cancel";
	final static String SAVE_BTN_STRING = "Save";
	static final String TOGGLE_BTN_ON_LABEL = "ON";
	static final String TOGGLE_BTN_OFF_LABEL = "OFF";
	static final String TIMER_TAG = "Timer";
	static final String CONTEXT_MENU_HEADER_TITLE = "Select Option";
	static final String CONTEXT_MENU_EDIT_TIME = "Edit Time";
	static final String CONTEXT_MENU_EDIT_LABEL = "Edit Label";
	static final String CONTEXT_MENU_EDIT_NOTE = "Edit Note";
	static final String CONTEXT_MENU_DELETE_TIMER = "Delete Timer";
	static final String CONTEXT_MENU_RESET_TIMER = "Reset Timer";
	static final String CONTEXT_MENU_RESET_ALL_TIMERS = "Reset All Timers";
	static final String CONTEXT_MENU_DELETE_ALL_TIMERS = "Delete All Timers";
	static final String CONTEXT_MENU_EMAIL_TIMERS = "Email Timers";
	
	static final String nl = "\n";
	static final String DATA_FILE_NAME = "timers.csv";
	static final String EMAIL_TYPE = "text/csv";
	static final String EMAIL_SUBJECT = "Trial Version: Project Time Data";
	static final String EMAIL_BODY = "Data from the Project Timer app by CoolTofu.com.";
	static final String INTENT_CHOOSER_TITLE = "Send Mail";
	final int repeatSpeed = 120; // how fast to repeat the action for increment/decrement time
	final int PRESS_DELAY = 200; // delay on press event for time editing
	final static String PREF_NAME = "MY_PREF";
	final static String SAMPLE_PROJECT_LABEL = "Sample Project";
	
	static List<Integer> timerIds = new ArrayList<Integer>();
	
	
	static String editedHour;
	static String editedMinute;
	static String editedSecond;
	
	static LayoutParams startStopBtnLayoutParams;
	static LayoutParams mtlParams;
	static LayoutParams itlParams;
	static LayoutParams timeTextParams;
	static LayoutParams taskLabelParams;
	static LayoutParams llParams;
	static TableLayout.LayoutParams tableRowParams;
	
	static TableLayout mainTl;
	static TableLayout innerTl;
	static TableRow tr;
	static LinearLayout ll;
	static RelativeLayout sv;
	
	Timer timer = new Timer();
	static Handler handler = new Handler();

	private static TimerDbAdapter db;
	private static Cursor cursor;
	private static int timerId;
	
	private static Button newTimerBtn;
	private static AlertDialog.Builder alert;
	private static Button moreBtn;
	private static Button optionBtn;
	private static TimerTask totalTimerTask;
	
	
	// add horizontal line
	final static ViewGroup.LayoutParams hrParams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 1);
	
	static float scale;// = getResources().getDisplayMetrics().density;
	static int pixels;// = (int) (65 * scale + 0.5f);
	
	
	static View hrView;
	
	static StringBuffer headerBuf;
	static StringBuffer rowBuf;
	
	static File f;
	static File sdcard;
	static FileWriter writer;
	
	static int timerCount = 0;
	static int MAX_TIMER_COUNT = 0; // limit for trial version
	private AdView adView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// end user license agreement
		Eula.show(this);
		
		setContentView(R.layout.main);
		
		startStopBtnLayoutParams = ((Button) findViewById(R.id.button1)).getLayoutParams();
		timeTextParams = ((TextView) findViewById(R.id.timeText)).getLayoutParams();
		taskLabelParams = ((TextView) findViewById(R.id.taskLabel)).getLayoutParams();
		llParams = ((LinearLayout) findViewById(R.id.linearLayout)).getLayoutParams();
		mtlParams = ((TableLayout) findViewById(R.id.tableLayout)).getLayoutParams();
		itlParams = ((TableLayout) findViewById(R.id.tableLayout2)).getLayoutParams();
		ll = (LinearLayout) findViewById(R.id.linearLayout);
		
		// get timers from db
		db = new TimerDbAdapter(this);
		db.open();
		
		cursor = db.fetchAllTimers();
		startManagingCursor(cursor);
		
		if (cursor != null) {
			
			timerIds.clear();
			int seconds = 0;
			long timestamp = 0;
			long now = Calendar.getInstance().getTimeInMillis();
			long elapsed = 0;
			boolean isTimerOn;
			
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false) {
				// KEY_ROWID, KEY_LABEL, KEY_SECONDS, KEY_IS_ON
				timerId = cursor.getInt(0);
				seconds = cursor.getInt(2);
				timestamp = cursor.getLong(3); // milliseconds
				isTimerOn = (cursor.getInt(4) == 1) ? true : false;
				
				// add timestamp value to seconds if needed
				// calculate the seconds to add since the activity was destroyed
				if (isTimerOn && timestamp > 0) {
					elapsed = now - timestamp;  // milliseconds
					seconds += ((int) (Math.round(elapsed/1000 + .5))); // add .5 to lessen the lost milliseconds
				}
				
				createTaskTimer(cursor.getInt(0), cursor.getString(1), seconds, isTimerOn);
				timerIds.add(timerId);
				cursor.moveToNext();
				
				timerCount++;
			}// while cursor
		}// if cursor != null
		

		
		newTimerBtn = (Button) findViewById(R.id.newTimerBtn);
		

		
		

		newTimerBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (MAX_TIMER_COUNT > 0 && timerCount >= MAX_TIMER_COUNT) {
					// alert trial version
					alert = new AlertDialog.Builder(ProjectTimerActivity.this);
					alert.setTitle("Trial Version");
					alert.setMessage("Please purchase the app for unlimited timers.");
					
					
					alert.setPositiveButton(OK_BTN_STRING,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								return;
							}
						});
					alert.show();
				} else {
				
				
					timerCount++;
					
					alert = new AlertDialog.Builder(ProjectTimerActivity.this);
					alert.setTitle(ALERT_NEW_TIMER_TITLE);
					alert.setMessage(ALERT_NEW_TIMER_MSG);
						
					final EditText input = new EditText(ProjectTimerActivity.this);
					input.setSingleLine(); // one line tall
					input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
					alert.setView(input);
					
					alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
						public void onCancel(DialogInterface arg0) {
							return;
						}
					});
					
					alert.setPositiveButton(OK_BTN_STRING,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									final String label = input.getText().toString();
									
									// create db entry for new timer
									timerId = (int) db.createTimer(label, 0, 0, false);
									
									if (timerId == -1) {
										// db error
										// TODO: handle error
									}
									
									createTaskTimer(timerId, label, 0, false);
									timerIds.add(new Integer(timerId));
									
								}
							});
	
					alert.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									return;
								}
							});
					alert.show();
				}
			}
		});

		
		
		//-------------------------------
		// More button actions
		moreBtn = (Button) findViewById(R.id.moreBtn);
		
		
		moreBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent();
				i.setClass(ProjectTimerActivity.this, MoreScreen.class);
				startActivity(i);
			}
		});
		
		
        
    	//-------------------------------
		// Options button actions
		optionBtn = (Button) findViewById(R.id.optionBtn);
		
		
		optionBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				openContextMenu(v);
			}
		});
		registerForContextMenu(optionBtn);
		
    	
			
    	
    	// if on first access, add a sample timer to the window
    	SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);
    	boolean isAccessed = settings.getBoolean("isAccessed", false);
    	if (isAccessed == false) {
    		timerId = (int) db.createTimer(SAMPLE_PROJECT_LABEL, 0, 0, false);
			
			if (timerId == -1) {
				// db error
				// TODO: handle error
			}
			
			createTaskTimer(timerId, SAMPLE_PROJECT_LABEL, 5025, false);
			timerIds.add(timerId);
			
			
	    	SharedPreferences.Editor editor = settings.edit();
	    	editor.putBoolean("isAccessed", true);
	    	editor.commit(); 
    	}
    	
    	
    	
    	// Create the adView
        adView = new AdView(this, AdSize.BANNER, "a14edeef620bb3a");

        
        // Lookup your LinearLayout assuming it�s been given
        // the attribute android:id="@+id/mainLayout"
        LinearLayout layout = (LinearLayout)findViewById(R.id.ads);

        // Add the adView to it
        layout.addView(adView);

        AdRequest adRequest = new AdRequest();
        

        // Initiate a generic request to load it with an ad
        adView.loadAd(adRequest);

	}//onCreate

	
	
	private void createTaskTimer(int timerId, String label, final int seconds, boolean isOn) {
	
		innerTl = new TableLayout(ProjectTimerActivity.this);
		innerTl.setLayoutParams(itlParams);
		String trimmedLabel = (label.trim().equals("")) ? " " : label.trim();
		final TextView taskLabel = new TextView(innerTl.getContext());
		taskLabel.setText(trimmedLabel);
		taskLabel.setLayoutParams(taskLabelParams);
		taskLabel.setTextSize(18);
		taskLabel.setId(TASK_LABEL_ID_PREFIX + timerId);
		taskLabel.setTextColor(Color.LTGRAY);

		final TextView timeText = new TextView(ProjectTimerActivity.this);
		timeText.setText(formatTimeTextDisplay(seconds));
		timeText.setLayoutParams(timeTextParams);
		timeText.setTextSize(28);
		timeText.setId(TIME_ID_PREFIX + timerId);
		timeText.setTextColor(Color.LTGRAY);
		
		tr = new TableRow(ProjectTimerActivity.this);
		tr.addView(timeText);
		innerTl.addView(tr);

		tr = new TableRow(ProjectTimerActivity.this);
		tr.addView(taskLabel);
		innerTl.addView(tr);

		

		hrView = new View(ProjectTimerActivity.this);
		hrView.setLayoutParams(hrParams);
		hrView.setBackgroundColor(Color.GRAY);
		hrView.getBackground().setAlpha(120);
		//innerTl.addView(hrView);
		
		
		mainTl = new TableLayout(ProjectTimerActivity.this);
		mainTl.setLayoutParams(mtlParams);
		mainTl.setId(timerId); // set the layout id for reference later
		mainTl.setPadding(0, 10, 0, 0);
		mainTl.setTag(TIMER_TAG);

		scale = getResources().getDisplayMetrics().density;
		pixels = (int) (65 * scale + 0.5f);

		final ToggleButton startStopBtn = new ToggleButton(ProjectTimerActivity.this);
		startStopBtn.setText(TOGGLE_BTN_OFF_LABEL);
		startStopBtn.setTextSize(12);
		startStopBtn.setHeight(pixels);
		startStopBtn.setWidth(pixels);
		startStopBtn.setId(START_STOP_ID_PREFIX + timerId);
		startStopBtn.setBackgroundColor(Color.BLACK);
		startStopBtn.setTextColor(R.color.text_color);
		startStopBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_toggle_off));
		
		
		startStopBtn.setOnClickListener(new View.OnClickListener() {

			TimerTask timerTask;
			int counter = 0;

			
			public void onClick(View v) {

				if (startStopBtn.isChecked()) {
					
		        	startStopBtn.setText(TOGGLE_BTN_ON_LABEL);
		        	startStopBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_toggle_on));
		        
					
					timerTask = new TimerTask() {
						
						public void run() {
							
							handler.post(new Runnable() {

								public void run() {
									counter = convertToSeconds(timeText.getText().toString());
									timeText.setText(formatTimeTextDisplay(counter));
									counter++;
									timeText.setText(formatTimeTextDisplay(counter));
								}
							});
							
						}
					};
					
					
					timer.scheduleAtFixedRate(timerTask, 1000, 1000);
				
				} else {
					startStopBtn.setText(TOGGLE_BTN_OFF_LABEL);
					startStopBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_toggle_off));
					handler.removeCallbacks(timerTask);

					if (timerTask != null) {
						timerTask.cancel();
					}
				}
			}
		});

		// start the timer if isOn == true
		if (isOn) {
			startStopBtn.performClick();
		}
		
		tr = new TableRow(mainTl.getContext());
		tr.addView(startStopBtn);
		tr.addView(innerTl);
		

		tableRowParams = new TableLayout.LayoutParams(
				TableLayout.LayoutParams.FILL_PARENT,
				TableLayout.LayoutParams.FILL_PARENT);
		tableRowParams.setMargins(0, 2, 0, 2);
		tr.setLayoutParams(tableRowParams);

		mainTl.addView(tr);

		// add timer and task label to inner table
		ll.addView(mainTl);

		mainTl.addView(hrView);

		
		innerTl.setOnTouchListener(new View.OnTouchListener() {
			
			public boolean onTouch(View v,MotionEvent evt) {
				
				
				switch(evt.getAction()) {
						
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundColor(Color.WHITE);
						taskLabel.setTextColor(Color.DKGRAY);
						timeText.setTextColor(Color.DKGRAY);
						break;
					
					case MotionEvent.ACTION_UP:
						v.setBackgroundColor(Color.BLACK);
						taskLabel.setTextColor(Color.LTGRAY);
						timeText.setTextColor(Color.LTGRAY);
						break;
						
					case MotionEvent.ACTION_CANCEL:
						v.setBackgroundColor(Color.BLACK);
						taskLabel.setTextColor(Color.LTGRAY);
						timeText.setTextColor(Color.LTGRAY);
						break;
				}
				
				
				return false;
			}
		});

		
		innerTl.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				openContextMenu(v);
				return true;
			}
		});
		
		
		// set long press event
		registerForContextMenu(mainTl);
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, EMAIL_TIMERS_MENU_ID, 0, "Email Timers");
		menu.add(1, RESET_ALL_TIMERS_MENU_ID, 1, "Reset All Timers");
		menu.add(1, DELETE_ALL_TIMERS_MENU_ID, 2, "Delete All Timers");
		return true;
	}
	
	// long press context menu
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		
		if (v.getTag() == TIMER_TAG) {
			menu.setHeaderTitle(CONTEXT_MENU_HEADER_TITLE);
			menu.add(0, (v.getId()), 0, CONTEXT_MENU_EDIT_TIME);
			menu.add(0, (v.getId()), 1, CONTEXT_MENU_EDIT_LABEL);
			menu.add(0, (v.getId()), 2, CONTEXT_MENU_EDIT_NOTE);
			menu.add(0, (v.getId()), 3, CONTEXT_MENU_RESET_TIMER);
			menu.add(0, v.getId(), 4, CONTEXT_MENU_DELETE_TIMER);
		} else {
			// options button
			menu.setHeaderTitle(CONTEXT_MENU_HEADER_TITLE);
			menu.add(1, 1, 0, CONTEXT_MENU_EMAIL_TIMERS);
			menu.add(1, 2, 1, CONTEXT_MENU_RESET_ALL_TIMERS);
			menu.add(1, 3, 2, CONTEXT_MENU_DELETE_ALL_TIMERS);
		}
	}
	
	private static String escapeQuote(String s) {
		return s.replaceAll("\"", "\"\"");
	}
	
	private void emailTimers() {
		//
		// create the csv file
		headerBuf = new StringBuffer("Date,Project,Time,Note");
		rowBuf = new StringBuffer();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date = new Date();
		String now = dateFormat.format(date);
		
		int len = timerIds.size();
    	
        
        
        if (len > 0) {
			int id = 0;
			
			TextView timeValue;
			TextView labelValue;
			String label;
			String note = "";
			
			for (int i = 0; i < len; i++) {
				
				id = (Integer) timerIds.get(i);
				
				TableLayout tv = (TableLayout) findViewById(id);
	        	
	        	if (tv == null) continue; // none found; continue to next iteration
	        
	        	
	        	cursor = db.getNote(id);
	        	startManagingCursor(cursor);
	        	
	        	if (cursor != null)
	        		note = cursor.getString(0);
	        	
	        	// table layout found, which means a timer also exists; save the time value
	        	timeValue = (TextView) findViewById(TIME_ID_PREFIX+id);
	        	
	        	// save the timer label
	        	labelValue = (TextView) findViewById(TASK_LABEL_ID_PREFIX+id);
	        	label = labelValue.getText().toString();
	        	
	        	rowBuf.append("\""+ escapeQuote(now) +"\",");
        		rowBuf.append("\""+ escapeQuote(label) +"\",");
        		rowBuf.append("\""+ timeValue.getText().toString() + "\",");
        		rowBuf.append("\""+ escapeQuote(note) + "\"");
        		
        		if (i < (len-1))
        			rowBuf.append(nl);
	        	
			}
		}
        
	
		
		
		try {
			sdcard = new File(Environment.getExternalStorageDirectory() + "/data/com/cooltofu/projecttimer/");
			sdcard.mkdirs();
			
			f = new File(sdcard, DATA_FILE_NAME);
			writer = new FileWriter(f);
			writer.write(headerBuf.toString() + nl);
			writer.write(rowBuf.toString());
			writer.flush();
			
		} catch (FileNotFoundException ex) {
			Log.e("file not found", ex.getMessage());
			Toast.makeText(this, "Can't write to external SD card.", Toast.LENGTH_LONG).show();
			
		} catch (IOException ioex) {
			Log.e("io ex: ", ioex.getMessage());
			Toast.makeText(this, "Can't write to external SD card.", Toast.LENGTH_LONG).show();
			
		} finally {
			try {
				if (writer != null)
					writer.close();
				
				
			} catch (IOException ioe) {
				Log.e("finally. IO Exc: ", ioe.getMessage());
			}
		}
		
		
		// send the email with the file attachment
		if (f.exists() && f.canRead()) {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType(EMAIL_TYPE);
			i.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
			i.putExtra(Intent.EXTRA_TEXT, EMAIL_BODY);
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
			
			startActivity(Intent.createChooser(i, INTENT_CHOOSER_TITLE));
		} else {
			Toast.makeText(this, "Can't send email.", Toast.LENGTH_LONG).show();
		}
		
		
	}
	
	private void confirmResetAllTimers() {
		alert = new AlertDialog.Builder(ProjectTimerActivity.this);
		alert.setTitle("Reset all Timers?");
	 	
		alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

			public void onCancel(DialogInterface arg0) {
				
				return;
			}
			
		});
		alert.setPositiveButton(OK_BTN_STRING,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						resetAllTimers();

					}
				});

		alert.setNegativeButton(CANCEL_BTN_STRING,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog,int which) {
						return;
					}
				});
		alert.show();
	}
	
	private void confirmDeleteAllTimers() {
		alert = new AlertDialog.Builder(ProjectTimerActivity.this);
		alert.setTitle("Delete all Timers?");
	 	
		alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

			public void onCancel(DialogInterface arg0) {
				
				return;
			}
			
		});
		alert.setPositiveButton(OK_BTN_STRING,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						deleteAllTimers();

					}
				});

		alert.setNegativeButton(CANCEL_BTN_STRING,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog,int which) {
						return;
					}
				});
		alert.show();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case EMAIL_TIMERS_MENU_ID:
				emailTimers();
				return true;
			case RESET_ALL_TIMERS_MENU_ID:
				confirmResetAllTimers();
				return true;	
			case DELETE_ALL_TIMERS_MENU_ID:
				confirmDeleteAllTimers();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	
	public boolean onContextItemSelected(final MenuItem item) {

		String menuItemTitle = (String) item.getTitle();
		
		
		if (menuItemTitle == CONTEXT_MENU_EMAIL_TIMERS) {
			emailTimers();
		} else if (menuItemTitle == CONTEXT_MENU_RESET_ALL_TIMERS) {
			confirmResetAllTimers();
			
		} else if (menuItemTitle == CONTEXT_MENU_DELETE_ALL_TIMERS) {
			confirmDeleteAllTimers();
		} else if (menuItemTitle == CONTEXT_MENU_RESET_TIMER) {
			final TextView textView = (TextView) findViewById(TASK_LABEL_ID_PREFIX + item.getItemId());
			
			alert = new AlertDialog.Builder(ProjectTimerActivity.this);
			alert.setTitle("Reset " + textView.getText().toString() + "?");
		 	
			alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface arg0) {
					return;
				}
			});
			alert.setPositiveButton(OK_BTN_STRING,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// get the innerTl view
							//TableLayout tv = (TableLayout) findViewById(item.getItemId());
							
				        	// table layout found, which means a timer also exists; reset the time value
				        	TextView timeValue = (TextView) findViewById(TIME_ID_PREFIX+item.getItemId());
				        	timeValue.setText(formatTimeTextDisplay(0));
						}
					});

			alert.setNegativeButton(CANCEL_BTN_STRING,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog,int which) {
							return;
						}
					});
			alert.show();
			
		} else if (menuItemTitle == CONTEXT_MENU_DELETE_TIMER) {
			final TextView textView = (TextView) findViewById(TASK_LABEL_ID_PREFIX + item.getItemId());
			
			alert = new AlertDialog.Builder(ProjectTimerActivity.this);
			alert.setTitle("Delete " + textView.getText().toString() + "?");
		 	
			alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface arg0) {
					return;
				}
			});
			alert.setPositiveButton(OK_BTN_STRING,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// get the innerTl view
							TableLayout tv = (TableLayout) findViewById(item.getItemId());
							ll.removeView(tv);
							timerId = (int) item.getItemId();
							
							db.deleteTimer(timerId);
							
							timerIds.remove(timerIds.indexOf(new Integer(timerId)));
							timerCount--;
						}
					});

			alert.setNegativeButton(CANCEL_BTN_STRING,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog,int which) {
							return;
						}
					});
			alert.show();
			//isDialogShowing = true;
			
		} else if (menuItemTitle == CONTEXT_MENU_EDIT_LABEL) {
			final TextView textView = (TextView) findViewById(TASK_LABEL_ID_PREFIX + item.getItemId());
			
			alert = new AlertDialog.Builder(ProjectTimerActivity.this);
			alert.setTitle(CONTEXT_MENU_EDIT_LABEL);

			final EditText input = new EditText(ProjectTimerActivity.this);
			input.setSingleLine(); // one line tall
			input.setText(textView.getText().toString());
			input.setSelection(input.getText().length());
			alert.setView(input);
			
			alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface arg0) {
					return;
				}
				
			});
			
			alert.setPositiveButton(OK_BTN_STRING,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							textView.setText(input.getText().toString().trim());
						}
					});

			alert.setNegativeButton(CANCEL_BTN_STRING,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog,int which) {
							return;
						}
					});
			alert.show();
		} else if (menuItemTitle == CONTEXT_MENU_EDIT_NOTE) {
			//final TextView textView = (TextView) findViewById(TASK_NOTE_ID_PREFIX + item.getItemId());
			final TextView textView = (TextView) findViewById(TASK_LABEL_ID_PREFIX + item.getItemId());
			final int id = item.getItemId();
			
			alert = new AlertDialog.Builder(ProjectTimerActivity.this);
			alert.setTitle("Note for " + textView.getText().toString());

			final EditText input = new EditText(ProjectTimerActivity.this);
			input.setGravity(Gravity.TOP);
			input.setLines(5); // one line tall
			
			
			// get the note from the DB if available
			cursor = db.getNote(id);
			startManagingCursor(cursor);
			String note = "";
			
			if (cursor != null) {
				note = cursor.getString(0);
				input.setText(note);
			}
			
			input.setSelection(input.getText().length());
			alert.setView(input);
			
			alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface arg0) {
					return;
				}
				
			});
			
			alert.setPositiveButton(SAVE_BTN_STRING,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							if (db.updateNote(id, input.getText().toString().trim()))
								Toast.makeText(ProjectTimerActivity.this, "Your note is saved.", Toast.LENGTH_SHORT).show();
							else
								Toast.makeText(ProjectTimerActivity.this, "Could not save your note.", Toast.LENGTH_LONG).show();
							
							return;
						}
					});

			alert.setNegativeButton(CANCEL_BTN_STRING,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog,int which) {
							return;
						}
					});
			alert.show();
		} else if (menuItemTitle == CONTEXT_MENU_EDIT_TIME) {
			final TextView timeText = (TextView) findViewById(TIME_ID_PREFIX + item.getItemId());
			final TextView taskLabel = (TextView) findViewById(TASK_LABEL_ID_PREFIX + item.getItemId());
			final ToggleButton startStopBtn = (ToggleButton) findViewById(START_STOP_ID_PREFIX + item.getItemId());
			final boolean isOn = startStopBtn.isChecked();
			
			// if timer is running, stop until finished editing time
			if (isOn)
				startStopBtn.performClick();
			
			String[] timeArray = timeText.getText().toString().split(":");
			
			
			final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View editTimeView = inflater.inflate(R.layout.edit_time, null);
			
			final TextView hourText = (TextView) editTimeView.findViewById(R.id.editHourText);
			final String hour = timeArray[0];
			hourText.setText(hour);
			
			TextView minuteText = (TextView) editTimeView.findViewById(R.id.editMinuteText);
			String minute = timeArray[1];
			minuteText.setText(minute);
			
			TextView secondText = (TextView) editTimeView.findViewById(R.id.editSecondText);
			String second = timeArray[2];
			secondText.setText(second);
			
			
			final Dialog dialog = new Dialog(this);
			dialog.setTitle("Edit Time for " + taskLabel.getText().toString());
			dialog.setContentView(editTimeView);
			dialog.show();
			
			
			final Runnable onPressedIncrementHour = new Runnable() {
				public void run() {
					
					final TextView hText = (TextView) editTimeView.findViewById(R.id.editHourText);
					int h = Integer.parseInt(hText.getText().toString());
					hText.setText(formatDoubleDigit(incrementHour(h)));
					handler.postAtTime(this, SystemClock.uptimeMillis()+repeatSpeed);
				}
			};
			final Runnable onPressedDecrementHour = new Runnable() {
				public void run() {
					
					final TextView hText = (TextView) editTimeView.findViewById(R.id.editHourText);
					int h = Integer.parseInt(hText.getText().toString());
					hText.setText(formatDoubleDigit(decrementHour(h)));
					handler.postAtTime(this, SystemClock.uptimeMillis()+repeatSpeed);
				}
			};
			
			
			
			final Runnable onPressedIncrementMinute = new Runnable() {
				public void run() {
					
					final TextView mText = (TextView) editTimeView.findViewById(R.id.editMinuteText);
					int m = Integer.parseInt(mText.getText().toString());
					mText.setText(formatDoubleDigit(incrementMinuteSecond(m)));
					handler.postAtTime(this, SystemClock.uptimeMillis()+repeatSpeed);
				}
			};
			final Runnable onPressedDecrementMinute = new Runnable() {
				public void run() {
					
					final TextView mText = (TextView) editTimeView.findViewById(R.id.editMinuteText);
					int m = Integer.parseInt(mText.getText().toString());
					mText.setText(formatDoubleDigit(decrementMinuteSecond(m)));
					handler.postAtTime(this, SystemClock.uptimeMillis()+repeatSpeed);
				}
			};
			
			
			final Runnable onPressedIncrementSecond = new Runnable() {
				public void run() {
					final TextView sText = (TextView) editTimeView.findViewById(R.id.editSecondText);
					int s = Integer.parseInt(sText.getText().toString());
					sText.setText(formatDoubleDigit(incrementMinuteSecond(s)));
					handler.postAtTime(this, SystemClock.uptimeMillis()+repeatSpeed);
				}
			};
			final Runnable onPressedDecrementSecond = new Runnable() {
				public void run() {
					final TextView sText = (TextView) editTimeView.findViewById(R.id.editSecondText);
					int s = Integer.parseInt(sText.getText().toString());
					sText.setText(formatDoubleDigit(decrementMinuteSecond(s)));
					handler.postAtTime(this, SystemClock.uptimeMillis()+repeatSpeed);
				}
			};
			//------------------------------------------------------------------------
			// set the click function for the increment buttons
			Button hourUpBtn = (Button) editTimeView.findViewById(R.id.editHourUpBtn);
			hourUpBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					
					final TextView hText = (TextView) editTimeView.findViewById(R.id.editHourText);
					int h = Integer.parseInt(hText.getText().toString());
					hText.setText(formatDoubleDigit(incrementHour(h)));
				}
			});
			
			hourUpBtn.setOnTouchListener(new View.OnTouchListener() {
				
				public final boolean onTouch(View v, MotionEvent event) {
					
					
					switch(event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							handler.removeCallbacks(onPressedIncrementHour);
							handler.postAtTime(onPressedIncrementHour, SystemClock.uptimeMillis()+repeatSpeed+PRESS_DELAY);
							break;
						case MotionEvent.ACTION_UP:
							handler.removeCallbacks(onPressedIncrementHour);
							break;
					}
					return false;
				}
				
			});
			
			
			
			Button minuteUpBtn = (Button) editTimeView.findViewById(R.id.editMinuteUpBtn);
			minuteUpBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					final TextView mText = (TextView) editTimeView.findViewById(R.id.editMinuteText);
					int m = Integer.parseInt(mText.getText().toString());
					mText.setText(formatDoubleDigit(incrementMinuteSecond(m)));
				}
			});
			minuteUpBtn.setOnTouchListener(new View.OnTouchListener() {
				
				public final boolean onTouch(View v, MotionEvent event) {
					
					switch(event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							handler.removeCallbacks(onPressedIncrementMinute);
							handler.postAtTime(onPressedIncrementMinute, SystemClock.uptimeMillis()+repeatSpeed+PRESS_DELAY);
							break;
						case MotionEvent.ACTION_UP:
							handler.removeCallbacks(onPressedIncrementMinute);
							break;
					}
					return false;
				}
				
			});

			
			Button secondUpBtn = (Button) editTimeView.findViewById(R.id.editSecondUpBtn);
			secondUpBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					final TextView sText = (TextView) editTimeView.findViewById(R.id.editSecondText);
					int s = Integer.parseInt(sText.getText().toString());
					sText.setText(formatDoubleDigit(incrementMinuteSecond(s)));
				}
			});
			secondUpBtn.setOnTouchListener(new View.OnTouchListener() {
				
				public final boolean onTouch(View v, MotionEvent event) {
					
					switch(event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							handler.removeCallbacks(onPressedIncrementSecond);
							handler.postAtTime(onPressedIncrementSecond, SystemClock.uptimeMillis()+repeatSpeed+PRESS_DELAY);
							break;
						case MotionEvent.ACTION_UP:
							handler.removeCallbacks(onPressedIncrementSecond);
							break;
					}
					return false;
				}
				
			});
			
			//------------------------------------------------------------------------
			// set the click function for the decrement buttons
			Button hourDownBtn = (Button) editTimeView.findViewById(R.id.editHourDownBtn);
			hourDownBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					final TextView hText = (TextView) editTimeView.findViewById(R.id.editHourText);
					int h = Integer.parseInt(hText.getText().toString());
					hText.setText(formatDoubleDigit(decrementHour(h)));
				}
			});
			hourDownBtn.setOnTouchListener(new View.OnTouchListener() {
				
				public final boolean onTouch(View v, MotionEvent event) {
					
					switch(event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							handler.removeCallbacks(onPressedDecrementHour);
							handler.postAtTime(onPressedDecrementHour, SystemClock.uptimeMillis()+repeatSpeed+PRESS_DELAY);
							break;
						case MotionEvent.ACTION_UP:
							handler.removeCallbacks(onPressedDecrementHour);
							break;
					}
					return false;
				}
				
			});

			Button minuteDownBtn = (Button) editTimeView.findViewById(R.id.editMinuteDownBtn);
			minuteDownBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					final TextView mText = (TextView) editTimeView.findViewById(R.id.editMinuteText);
					int m = Integer.parseInt(mText.getText().toString());
					mText.setText(formatDoubleDigit(decrementMinuteSecond(m)));
				}
			});
			minuteDownBtn.setOnTouchListener(new View.OnTouchListener() {
				
				public final boolean onTouch(View v, MotionEvent event) {
					
					switch(event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							handler.removeCallbacks(onPressedDecrementMinute);
							handler.postAtTime(onPressedDecrementMinute, SystemClock.uptimeMillis()+repeatSpeed+PRESS_DELAY);
							break;
						case MotionEvent.ACTION_UP:
							handler.removeCallbacks(onPressedDecrementMinute);
							break;
					}
					return false;
				}
			});

			Button secondDownBtn = (Button) editTimeView.findViewById(R.id.editSecondDownBtn);
			secondDownBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					final TextView sText = (TextView) editTimeView.findViewById(R.id.editSecondText);
					int s = Integer.parseInt(sText.getText().toString());
					sText.setText(formatDoubleDigit(decrementMinuteSecond(s)));
				}
			});
			secondDownBtn.setOnTouchListener(new View.OnTouchListener() {
				
				public final boolean onTouch(View v, MotionEvent event) {
					
					switch(event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							handler.removeCallbacks(onPressedDecrementSecond);
							handler.postAtTime(onPressedDecrementSecond, SystemClock.uptimeMillis()+repeatSpeed+PRESS_DELAY);
							break;
						case MotionEvent.ACTION_UP:
							handler.removeCallbacks(onPressedDecrementSecond);
							break;
					}
					return false;
				}
				
			});
			
			
			Button editTimeOkBtn = (Button) editTimeView.findViewById(R.id.editTimeOkBtn);
			editTimeOkBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					
					
					final TextView hText = (TextView) editTimeView.findViewById(R.id.editHourText);
					int h = Integer.parseInt(hText.getText().toString());
					
					final TextView mText = (TextView) editTimeView.findViewById(R.id.editMinuteText);
					int m = Integer.parseInt(mText.getText().toString());
					
					final TextView sText = (TextView) editTimeView.findViewById(R.id.editSecondText);
					int s = Integer.parseInt(sText.getText().toString());
					
					timeText.setText(formatDoubleDigit(h) + ":" + formatDoubleDigit(m) + ":" + formatDoubleDigit(s));
					
					// restart the timer
					if (isOn)
						startStopBtn.performClick();
					
					dialog.dismiss();
					
				}
			});
			
			Button editTimeCancelBtn = (Button) editTimeView.findViewById(R.id.editTimeCancelBtn);
			editTimeCancelBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// restart the timer
					if (isOn)
						startStopBtn.performClick();
					
					dialog.dismiss();
					
				}
			});
			
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
      
        if (!db.isOpen())
			db.open();
        
        // calculate total if there are timers
    	
    	final TextView totalTextView = (TextView) findViewById(R.id.sumText);
    	
    	totalTimerTask = new TimerTask() {
    		int seconds = 0;
    		int len = 0;
    		TableLayout tv;
    		TextView timeValue;
    		
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						seconds = 0;
						len = timerIds.size();
						
						for (int i = 0; i < len; i++) {
							// KEY_ROWID, KEY_LABEL, KEY_SECONDS, KEY_IS_ON
							int id = (Integer) timerIds.get(i);
							tv = (TableLayout) findViewById(id);
							
				        	if (tv == null) continue; // none found; continue to next iteration
				        	
				        	// table layout found, which means a timer also exists; save the time value
				        	timeValue = (TextView) findViewById(TIME_ID_PREFIX+id);
				        	seconds += convertToSeconds(timeValue.getText().toString());
						}	
						
						if (seconds == 0)
							totalTextView.setText("00:00:00");
						else
							totalTextView.setText(formatTimeTextDisplay(seconds));
					}
				}); 
			}
    	};
    	timer.scheduleAtFixedRate(totalTimerTask, 0, 200);
        
    }
    @Override
    protected void onPause() {
        super.onPause();
        
        // Another activity is taking focus (this activity is about to be "paused").
        saveTimers();
        totalTimerTask.cancel();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	adView.destroy();
    	
        // The activity is about to be destroyed.
        timer.cancel();
        
        if (cursor != null)
        	cursor.close();
        
        if (db != null)
        	db.close();
     
        super.onDestroy();
    }
    
    private void saveTimers() {
    	
        int len = timerIds.size();
        
        TableLayout tv;
        int id = 0;
        TextView timeValue;
        int seconds = 0;
        TextView labelValue;
        String label;
        ToggleButton btn;
        boolean isOn;
        long timestamp = 0;
        
        Calendar cal = Calendar.getInstance();
        
		for (int i=0; i < len; i++) {
			// KEY_ROWID, KEY_LABEL, KEY_SECONDS, KEY_IS_ON
			id = (Integer) timerIds.get(i);
			
			tv = (TableLayout) findViewById(id);
        	
        	if (tv == null) continue; // none found; continue to next iteration
        
        	
        	
        	// table layout found, which means a timer also exists; save the time value
        	timeValue = (TextView) findViewById(TIME_ID_PREFIX+id);
        	seconds = convertToSeconds(timeValue.getText().toString());
        	
        	
        	// save the timer label
        	labelValue = (TextView) findViewById(TASK_LABEL_ID_PREFIX+id);
        	label = labelValue.getText().toString();
        	
        	
        	// save the state of the timer; running or not
        	btn = (ToggleButton) findViewById(START_STOP_ID_PREFIX+id);
        	isOn = btn.isChecked();
        	
        	
        	// save the timestamp; used for timers that are active when activity is destroyed
        	
        	timestamp = cal.getTimeInMillis();
        	
        	
        	db.updateTimer(id, label, seconds, timestamp, isOn);
        	
		}
	
        
        
        
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
    
    private String formatDoubleDigit(int num) {
    	if (num < 10)
    		return "0"+num;
    	
    	return num+"";
    }
    
    private String formatTimeTextDisplay(int seconds) {
    	int hour = seconds / 3600;
		int rem = seconds % 3600;
		int min = rem / 60;
		int sec = rem % 60;
		
		return String.format(TIME_FORMAT, hour, min, sec);
    }
    
    private int incrementHour(int num) {
    	
    	return ++num;
    }
    
    private int incrementMinuteSecond(int num) {
    	if (num + 1 > 59)
    		return 0;
    	
    	return ++num;
    }
    
    private int decrementHour(int num) {
    	if (num - 1 < 0)
    		return 0;
    	
    	return --num;
    }
    
    private int decrementMinuteSecond(int num) {
    	if (num - 1 < 0)
    		return 59;
    	
    	return --num;
    }
    
    private void resetAllTimers() {
		
		int len = timerIds.size();
		
		if (len > 0) {
			int id = 0;
			TableLayout tv;
			TextView timeValue;
			
			for (int i = 0; i < len; i++) {
				
				id = (Integer) timerIds.get(i);
				
				tv = (TableLayout) findViewById(id);
	        	
	        	if (tv == null) continue; // none found; continue to next iteration
	        
	        	
	        	// table layout found, which means a timer also exists; reset the time value
	        	timeValue = (TextView) findViewById(TIME_ID_PREFIX+id);
	        	timeValue.setText(formatTimeTextDisplay(0));
			}
			
			saveTimers();
		}
			
		
	}
	
	private void deleteAllTimers() {
		
		int len = timerIds.size();
		int id = 0;
		TableLayout tv;
		
		for (int i = 0; i < len; i++) {
			// KEY_ROWID, KEY_LABEL, KEY_SECONDS, KEY_IS_ON
			id = (Integer) timerIds.get(i);
			tv = (TableLayout) findViewById(id);
			ll.removeView(tv);
			
			db.deleteTimer(id);
		}
		timerIds.clear();	
		timerCount = 0;
	}
	
}