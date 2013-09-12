package org.syxc.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

/**
 * A custom Android log class
 * @author syxc
 */
public final class LogUtil {

	public static boolean logoff = false; // Log switch open, development, released when closed(LogCat)
	public static int level = Log.ERROR; // Write file level
	
	/**
	 * Custom Log output style
	 * @param type Log type
	 * @param tag TAG
	 * @param msg Log message
	 */
	public static void trace(int type, String tag, String msg) {
		// LogCat
		if (logoff) { 
			switch (type) {
			case Log.VERBOSE:
				Log.v(tag, msg);
				break;
			case Log.DEBUG:
				Log.d(tag, msg);
				break;
			case Log.INFO:
				Log.i(tag, msg);
				break;
			case Log.WARN:
				Log.w(tag, msg);
				break;
			case Log.ERROR:
				Log.e(tag, msg);
				break;
			}
		}
		// Write to file
		if (type >= level) {
			writeLog(type, msg);
		}
	}
	
	/**
	 * Write log file to the SDcard
	 * @param type
	 * @param msg
	 */
	private static void writeLog(int type, String msg) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
		try {
			final HashMap<Integer, String> logMap = new HashMap<Integer, String>();
			logMap.put(Log.VERBOSE, " VERBOSE ");
			logMap.put(Log.DEBUG, " DEBUG ");
			logMap.put(Log.INFO, " INFO ");
			logMap.put(Log.WARN, " WARN ");
			logMap.put(Log.ERROR, " ERROR ");
			
			final StackTraceElement tag = new Throwable().fillInStackTrace().getStackTrace()[2];
			
			msg = new StringBuilder().append("\r\n")
					.append(getDateFormat(DateFormater.SS.getValue()))
					.append(logMap.get(type)).append(tag.getClassName())
					.append(" - ").append(tag.getMethodName()).append("(): ")
					.append(msg).toString();

			final String fileName = new StringBuffer().append("test-")
					.append(getDateFormat(DateFormater.DD.getValue()))
					.append(".log").toString();
			
			recordLog(Config.LOG_DIR, fileName, msg, true);
		} catch (Exception e) {
			LogUtil.trace(Log.ERROR, "LogUtil: ", e.getMessage());
		}
	}
	
	/**
	 * Write log
	 * @param logDir Log path to save
	 * @param fileName 
	 * @param msg Log content
	 * @param bool Save as type, false override save, true before file add save
	 */
	private static void recordLog(String logDir, String fileName, String msg, boolean bool) {
		try {
			createDir(logDir);
			final File saveFile = new File(new StringBuffer().append(logDir)
					.append("/").append(fileName).toString());
			if (!bool && saveFile.exists()) {
				saveFile.delete();
				saveFile.createNewFile();
				final FileOutputStream fos = new FileOutputStream(saveFile, bool);
				fos.write(msg.getBytes());
				fos.close();
			} else if (bool && saveFile.exists()) {
				final FileOutputStream fos = new FileOutputStream(saveFile, bool);
				fos.write(msg.getBytes());
				fos.close();
			} else if (bool && !saveFile.exists()) {
				saveFile.createNewFile();
				final FileOutputStream fos = new FileOutputStream(saveFile, bool);
				fos.write(msg.getBytes());
				fos.close();
			}
		} catch (IOException e) {
			recordLog(logDir, fileName, msg, bool);
		}
	}
	
	public static void processGlobalException(Application application) {
		if (application != null) {
			GlobalExceptionHandler handler = new GlobalExceptionHandler(application);
			Thread.setDefaultUncaughtExceptionHandler(handler);
		}
	}

	private static String getDateFormat(String pattern) {
		final DateFormat format = new SimpleDateFormat(pattern);
		return format.format(new Date());
	}
	
	private static File createDir(String dir) {
		final File file = new File(dir);
		if (!file.exists()) { 
			file.mkdirs();
		}
		return file;
	}
}
