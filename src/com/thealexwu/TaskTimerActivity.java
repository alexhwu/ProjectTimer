package com.thealexwu;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

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

				final TextView timeText = new TextView(TaskTimerActivity.this);
				timeText.setText("00:00:00");
				timeText.setLayoutParams(timeTextParams);
				timeText.setTextSize(28);

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

				ViewGroup.LayoutParams btnParams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 55);
				final float scale = getResources().getDisplayMetrics().density;
				int pixels = (int) (65 * scale + 0.5f);

				final ToggleButton startStopBtn = new ToggleButton(TaskTimerActivity.this);
				startStopBtn.setText("Start");
				startStopBtn.setTextSize(12);
				startStopBtn.setHeight(pixels);
				startStopBtn.setWidth(pixels);

				startStopBtn.setOnClickListener(new View.OnClickListener() {

					TimerTask timerTask = null;
					int counter = 0;

					public void onClick(View v) {

						if (startStopBtn.isChecked()) {
							startStopBtn.setText("Stop");
							timerTask = new TimerTask() {

								public void run() {
									handler.post(new Runnable() {

										public void run() {
											counter++; // seconds
											int hour = (int) counter / 6000;
											int min = (int) counter / 60;
											int sec = counter % 60;

											String counterStr = (hour < 10 ? "0" + hour : hour)
													+ ":"
													+ (min < 10 ? "0" + min : min)
													+ ":"
													+ (sec < 10 ? "0" + sec : sec);
											timeText.setText(counterStr);

										}
									});
								}
							};

							timer.schedule(timerTask, 0, 1000);

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
}