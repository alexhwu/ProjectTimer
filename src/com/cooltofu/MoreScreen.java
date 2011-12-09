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
						v.setBackgroundColor(Color.WHITE);
						btn.setTextColor(Color.DKGRAY);
						break;
					case MotionEvent.ACTION_UP:
						v.setBackgroundColor(Color.BLACK);
						btn.setTextColor(Color.LTGRAY);
						break;
				}
				
				return false;
			}
		}); // moreBtn.setOnTouchListener()
	}
}
