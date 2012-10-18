package org.syxc.util;

import android.os.Environment;

public final class Config {
	
	public static final String STORAGE_PATH = Environment
			.getExternalStorageDirectory().getPath();
	
	public static final String LOG_DIR = STORAGE_PATH + "/xxx/logs/";
}
