package com.thealexwu;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class TaskTimerActivity extends Activity {
    //ToggleButton startStopBtn;
    //TextView timeText;
    //TextView taskLabel;
    
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
   
    Timer timer = new Timer();
    final Handler handler = new Handler();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        ll = (LinearLayout) findViewById(R.id.linearLayout);
        Button btn = (Button) findViewById(R.id.newTimerBtn);
       
        startStopBtnLayoutParams = ((Button) findViewById(R.id.button1)).getLayoutParams();
        timeTextParams = ((TextView) findViewById(R.id.timeText)).getLayoutParams();
        
        taskLabelParams = ((TextView) findViewById(R.id.taskLabel)).getLayoutParams();
        llParams = ((LinearLayout) findViewById(R.id.linearLayout)).getLayoutParams();
        mtlParams = ((TableLayout) findViewById(R.id.tableLayout)).getLayoutParams();
        itlParams = ((TableLayout) findViewById(R.id.tableLayout2)).getLayoutParams();
        
        btn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				 innerTl = new TableLayout(v.getContext());
				 innerTl.setLayoutParams(itlParams);
				 
				 final TextView timeText = new TextView(v.getContext());
				 timeText.setText("00:00:00");
				 timeText.setLayoutParams(timeTextParams);
				 timeText.setTextSize(28);
				 
				 TableRow _tr2 = new TableRow(v.getContext());
				 _tr2.addView(timeText);
				 innerTl.addView(_tr2);
				 
				 TextView taskLabel = new TextView(innerTl.getContext());
				 taskLabel.setText("New task here");
				 taskLabel.setLayoutParams(taskLabelParams);
				 taskLabel.setTextSize(18);
				 
				 TableRow _tr3 = new TableRow(v.getContext());
				 _tr3.addView(taskLabel);
				 innerTl.addView(_tr3);
				 
				 // add horizontal line
				 ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 1);
				 
				 View hrView = new View(v.getContext());
				 hrView.setLayoutParams(p);
				 hrView.setBackgroundColor(Color.GRAY);
				 hrView.getBackground().setAlpha(120);
				
				 innerTl.addView(hrView);
				 
				 mainTl = new TableLayout(v.getContext());
				 mainTl.setLayoutParams(mtlParams);
				 
				 ViewGroup.LayoutParams btnParams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 55);
				 final float scale = getResources().getDisplayMetrics().density;
				 int pixels = (int) (65 * scale + 0.5f);
				    
				 final ToggleButton startStopBtn = new ToggleButton(v.getContext()); 
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
											
											
											String counterStr = (hour < 10 ? "0"+hour : hour) + ":" + (min < 10 ? "0"+min : min) + ":" + (sec < 10 ? "0"+sec : sec);
											timeText.setText(counterStr);
											//Toast toast = Toast.makeText(TaskTimerActivity.this, counterStr, Toast.LENGTH_SHORT);
											//toast.show();
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
						 TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT);
				 tableRowParams.setMargins(0, 2,0,2);
				 _tr.setLayoutParams(tableRowParams);
				 
				 
				 mainTl.addView(_tr);
				
				 // add timer and task label to inner table
				 ll.addView(mainTl);
					 
				 
			}
		});
		
		
    }
    
    
    
}