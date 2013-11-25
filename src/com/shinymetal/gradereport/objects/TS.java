package com.shinymetal.gradereport.objects;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TS {
	private long startDate;
	private long startNanoseconds;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

	private TS() {
		this.startDate = System.currentTimeMillis();
		this.startNanoseconds = System.nanoTime();
	}

	private String getTimestamp() {
		long microSeconds = (System.nanoTime() - this.startNanoseconds) / 1000;
		long date = this.startDate + (microSeconds / 1000);
		return dateFormat.format(date)
				+ String.format("%03d", microSeconds % 1000) + ": ";
	}
	
	public static String get () {
		
		final TS ts = new TS();
		return ts.getTimestamp();		
	}
}