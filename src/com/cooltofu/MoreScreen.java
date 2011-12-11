package com.cooltofu;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ScrollView;

public class MoreScreen extends Activity implements OnClickListener {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.more);
		
		final Button moreBtn = (Button) findViewById(R.id.moreBtn);
		moreBtn.setOnTouchListener(new View.OnTouchListener() {
				
			public boolean onTouch(View v,MotionEvent evt) {
				
				switch(evt.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundColor(Color.WHITE);
						moreBtn.setTextColor(Color.DKGRAY);
						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundColor(Color.BLACK);
						moreBtn.setTextColor(Color.LTGRAY);
						break;
					case MotionEvent.ACTION_CANCEL:
						v.setBackgroundColor(Color.BLACK);
						((Button) v).setTextColor(Color.LTGRAY);
						break;
				}
				
				return false;
			}
		}); // moreBtn.setOnTouchListener()
		
		final Button btn = (Button) findViewById(R.id.moreScreenOkBtn);
		btn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				finish();
			}
			
		});
		
		btn.setOnTouchListener(new View.OnTouchListener() {
			
			public boolean onTouch(View v,MotionEvent evt) {
				
				switch(evt.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundColor(Color.WHITE);
						btn.setTextColor(Color.DKGRAY);
						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundColor(Color.BLACK);
						btn.setTextColor(Color.LTGRAY);
						break;
					case MotionEvent.ACTION_CANCEL:
						v.setBackgroundColor(Color.BLACK);
						((Button) v).setTextColor(Color.LTGRAY);
						break;
				}
				
				return false;
			}
		}); // more screen ok btn.setOnTouchListener()
		
		
		final Button timerBtn = (Button) findViewById(R.id.timerBtn);
		timerBtn.setOnTouchListener(new View.OnTouchListener() {
				
			public boolean onTouch(View v,MotionEvent evt) {
				
				switch(evt.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundColor(Color.WHITE);
						timerBtn.setTextColor(Color.DKGRAY);
						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundColor(Color.BLACK);
						timerBtn.setTextColor(Color.LTGRAY);
						break;
					case MotionEvent.ACTION_CANCEL:
						v.setBackgroundColor(Color.BLACK);
						((Button) v).setTextColor(Color.LTGRAY);
						break;
				}
				
				return false;
			}
		}); // moreBtn.setOnTouchListener()
		
		timerBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//Intent i = new Intent();
				//i.setClass(MoreScreen.this, TaskTimerActivity.class);
				//startActivity(i);
				finish();
			}
		
		});
		
		
		// handle swipe gestures
		gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        
        ScrollView sv = (ScrollView) findViewById(R.id.scrollView1);
        sv.setOnClickListener(MoreScreen.this);
        sv.setOnTouchListener(gestureListener);
	}
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
    
	class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(MoreScreen.this, "Left Swipe", Toast.LENGTH_SHORT).show();
                	// do nothing
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(MoreScreen.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                	MoreScreen.this.finish();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

    }

	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
