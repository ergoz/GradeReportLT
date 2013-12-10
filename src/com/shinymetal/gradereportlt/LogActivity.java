package com.shinymetal.gradereportlt;

import com.shinymetal.gradereport.utils.NetLogger;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class LogActivity extends Activity {

	private ListView lvData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_log);

		NetLogger.trim(86400);

		lvData = (ListView) findViewById(R.id.lvData);
		lvData.setAdapter(new LogListAdapter(NetLogger.getCursorAll()));
	}
}
