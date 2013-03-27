package com.cooltofu;

public class TimeUtil {
	private static final String TIME_FORMAT = "%02d:%02d:%02d";
	
	public static String formatTimeTextDisplay(int seconds) {
		int hour = seconds / 3600;
		int rem = seconds % 3600;
		int min = rem / 60;
		int sec = rem % 60;

		return String.format(TIME_FORMAT, hour, min, sec);
	}
	
	public static int convertToSeconds(String t) {
		// t variable is in the format of: 00:00:00
		// hh:mm:ss
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
			sec += m;
		}

		if (timeStr[2] != "00") {
			// convert minutes into seconds
			int s = Integer.parseInt(timeStr[2]);
			sec += s;
		}

		return sec;
	}
}
