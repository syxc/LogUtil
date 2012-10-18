package org.syxc.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.util.Log;

public class GlobalExceptionHandler implements UncaughtExceptionHandler {

	private static final String TAG = "GlobalExceptionHandler";
	
	@SuppressWarnings("unused")
	private Application mApplication;

	public GlobalExceptionHandler(Application application) {
		this.mApplication = application;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		String info = null;
		ByteArrayOutputStream bos = null;
		PrintStream printStream = null;
		try {
			bos = new ByteArrayOutputStream();
			printStream = new PrintStream(bos);
			ex.printStackTrace(printStream);
			byte[] data = bos.toByteArray();
			info = new String(data);
			data = null;
			LogUtil.trace(Log.ERROR, TAG, info);

			// kill application
		} catch (Exception e) {
			LogUtil.trace(Log.ERROR, TAG, e.getMessage());
			// kill application
		} finally {
			try {
				if (printStream != null) {
					printStream.close();
				}
				if (bos != null) {
					bos.close();
				}
			} catch (Exception e) {
				// kill application
			}
		}
	}
}
