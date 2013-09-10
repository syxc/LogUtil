package org.syxc.test;
import org.syxc.util.LogUtil;

import android.app.Application;
import android.util.Log;

public class App extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.logoff = true;
		LogUtil.level = Log.VERBOSE;
	}
}
