package com.cooltofu;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.widget.TextView;

public class MyTimer {
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture handle;
	private TextView timeTextView;
	private TextView labelTextView;
	private String label;
	private int seconds;
	private int timerId;
	private boolean timerActive;
	
	public MyTimer(int timerId, TextView labelTextView, TextView timeTextView, String label, int initialSeconds) {
		this.timerId = timerId;
		this.labelTextView = labelTextView;
		this.timeTextView = timeTextView;
		this.label = label;
		this.seconds = initialSeconds;
		init();
	}
	
	private void init() {
		timeTextView.post(new Runnable() {
			public void run() {
				timeTextView.setText(TimeUtil.formatTimeTextDisplay(seconds));
			}
		});
		
		labelTextView.post(new Runnable() {
			public void run() {
				labelTextView.setText(label);
			}
		});
	}
	
	public void startTimer() {
		final Runnable increment = new Runnable() {
			public void run() {
				timeTextView.post(new Runnable() {
					public void run() {
						timeTextView.setText(TimeUtil.formatTimeTextDisplay(seconds += 1));
					}
				});
			}
		};
		
		handle = scheduler.scheduleAtFixedRate(increment,  1, 1, TimeUnit.SECONDS);
		timerActive = true;
	}
	
	public void stopTimer() {
		if (handle == null)
			return;
		
		handle.cancel(true);
		timerActive = false;
	}
	
	
	
	public boolean isActive() {
		return timerActive;
	}
	
	public int getSeconds() {
		return seconds;
	}
	
	public void setSeconds(final int seconds) {
		this.seconds = seconds;
		timeTextView.post(new Runnable() {
			public void run() {
				timeTextView.setText(TimeUtil.formatTimeTextDisplay(seconds));
			}
		});
	}

	public int getTimerId() {
		return timerId;
	}
	
	public void setLabel(final String label) {
		this.label = label;
		
		labelTextView.post(new Runnable() {
			public void run() {
				labelTextView.setText(label);
			}
		});
	}
	
	public String getLabel() {
		return label;
	}
	
	public void reset() {
		seconds = 0;
		
		timeTextView.post(new Runnable() {
			public void run() {
				timeTextView.setText(TimeUtil.formatTimeTextDisplay(seconds));
			}
		});
	}	
}