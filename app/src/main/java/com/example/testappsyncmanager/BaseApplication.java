package com.example.testappsyncmanager;

import android.app.Application;

import com.example.testappsyncmanager.utilities.PreferenceUtils;
import com.google.firebase.FirebaseApp;
import com.sendbird.android.SendBird;

public class BaseApplication extends Application {
	//private static final String APP_ID = "361BACE2-A48A-4E5D-BDBF-AFD544877AE4";
	private static final String APP_ID = "31936A04-F28A-40A1-BDB1-598E7CA2D74C";

	@Override
	public void onCreate() {
		super.onCreate();
		FirebaseApp.initializeApp(this);
		PreferenceUtils.init(getApplicationContext());
		SendBird.init(APP_ID, getApplicationContext());
	}
}
