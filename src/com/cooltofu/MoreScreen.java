package com.cooltofu;


import android.app.Activity;
import android.os.Bundle;
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
		
	}

}
