package com.cooltofu;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;



public class MoreScreen extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.more);
		
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
						setButtonEffect(btn, MotionEvent.ACTION_DOWN);
						break;
					case MotionEvent.ACTION_UP:
						setButtonEffect(btn, MotionEvent.ACTION_UP);
						break;
					case MotionEvent.ACTION_CANCEL:
						setButtonEffect(btn, MotionEvent.ACTION_UP);
						break;
				}
				
				return false;
			}
		}); // more screen ok btn.setOnTouchListener()
		
		
		
	}
	
	
	private void setButtonEffect(Button btn, int action) {
		if (action == MotionEvent.ACTION_DOWN) {
			btn.setBackgroundColor(getResources().getColor(R.color.menu_down_bg));
			btn.setTextColor(Color.DKGRAY);
		} else {
			//btn.getBackground().setColorFilter(Color.parseColor(R.color.menu_bg));
			btn.setBackgroundColor(getResources().getColor(R.color.menu_bg));
			btn.setTextColor(Color.LTGRAY);
		}
		
	}
}
