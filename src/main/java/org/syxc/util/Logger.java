package org.syxc.util;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

//import org.syxc.util.GlobalExceptionHandler.UncaughtException;

/**
 * A custom Android log class
 *
 * @author syxc
 */
public final class Logger {

    private static final String TAG = "Logger";

    private static final String DOWNLOADS_PATH;

    protected static final String LOG_PREFIX;
    protected static final String LOG_DIR;

    public static boolean debug = false; // Log switch open, development, released when closed(LogCat)
    public static int level = Log.ERROR; // Write file level


    static {
        DOWNLOADS_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        LOG_PREFIX = setLogPrefix("");
        LOG_DIR = setLogPath("");
    }


    public static void v(String tag, String msg) {
        trace(Log.VERBOSE, tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        trace(Log.VERBOSE, tag, msg, tr);
    }

    public static void d(String tag, String msg) {
        trace(Log.DEBUG, tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        trace(Log.DEBUG, tag, msg, tr);
    }

    public static void i(String tag, String msg) {
        trace(Log.INFO, tag, msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        trace(Log.INFO, tag, msg, tr);
    }

    public static void w(String tag, String msg) {
        trace(Log.WARN, tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        trace(Log.WARN, tag, msg, tr);
    }

    public static void e(String tag, String msg) {
        trace(Log.ERROR, tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        trace(Log.ERROR, tag, msg, tr);
    }


    /**
     * Custom Log output style
     *
     * @param type Log type
     * @param tag  TAG
     * @param msg  Log message
     */
    private static void trace(final int type, String tag, final String msg) {
        // LogCat
        if (debug) {
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
     * Custom Log output style
     *
     * @param type
     * @param tag
     * @param msg
     * @param tr
     */
    private static void trace(final int type, final String tag,
                              final String msg, final Throwable tr) {
        // LogCat
        if (debug) {
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
            writeLog(type, msg + '\n' + getStackTraceString(tr));
        }
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     *
     * @param tr An exception to log
     */
    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, 256);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * Write log file to the SDCard
     *
     * @param type
     * @param msg
     */
    private static void writeLog(int type, String msg) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        try {
            SparseArray<String> logMap = new SparseArray<String>();
            logMap.put(Log.VERBOSE, " VERBOSE ");
            logMap.put(Log.DEBUG, " DEBUG ");
            logMap.put(Log.INFO, " INFO ");
            logMap.put(Log.WARN, " WARN ");
            logMap.put(Log.ERROR, " ERROR ");

            final StackTraceElement tag = new Throwable().fillInStackTrace().getStackTrace()[2];

            msg = new StringBuilder()
                    .append("\r\n")
                    .append(getDateFormat(DateFormater.SS.getValue()))
                    .append(logMap.get(type)).append(tag.getClassName())
                    .append(" - ").append(tag.getMethodName()).append("(): ")
                    .append(msg).toString();

            final String fileName = new StringBuffer()
                    .append(LOG_PREFIX)
                    .append(getDateFormat(DateFormater.DD.getValue()))
                    .append(".log").toString();

            recordLog(LOG_DIR, fileName, msg, true);
        } catch (Exception e) {
            Logger.e("Logger: ", e.getMessage());
        }
    }

    /**
     * Write log
     *
     * @param logDir   Log path to save
     * @param fileName
     * @param msg      Log content
     * @param append   Save as type, false override save, true before file add save
     */
    private static void recordLog(String logDir, String fileName, String msg, boolean append) {
        try {
            createDir(logDir);

            final File saveFile = new File(new StringBuffer()
                    .append(logDir)
                    .append("/")
                    .append(fileName).toString());

            if (!append && saveFile.exists()) {
                saveFile.delete();
                saveFile.createNewFile();
                write(saveFile, msg, append);
            } else if (append && saveFile.exists()) {
                write(saveFile, msg, append);
            } else if (append && !saveFile.exists()) {
                saveFile.createNewFile();
                write(saveFile, msg, append);
            } else if (!append && !saveFile.exists()) {
                saveFile.createNewFile();
                write(saveFile, msg, append);
            }
        } catch (IOException e) {
            recordLog(logDir, fileName, msg, append);
        }
    }

    public static void processGlobalException(Application application, boolean isWriteIntoFile) {
        if (application != null) {
            CrashHandler handler = new CrashHandler(application, isWriteIntoFile);
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

    /**
     * Write msg to file
     *
     * @param file
     * @param msg
     * @param append
     */
    private static void write(final File file, final String msg, final boolean append) {

        new SafeAsyncTask<Void>() {

            @Override
            public Void call() throws Exception {
                final FileOutputStream fos;
                try {
                    fos = new FileOutputStream(file, append);
                    try {
                        fos.write(msg.getBytes());
                    } catch (IOException e) {
                        Logger.e(TAG, "write fail!!!", e);
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                Logger.d(TAG, "Exception closing stream: ", e);
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    Logger.e(TAG, "write fail!!!", e);
                }

                return null;
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);
                //if (e instanceof OperationCanceledException) {
                //}
                Logger.d(TAG, "Log write fail!", e);
            }

            @Override
            protected void onSuccess(Void hasAuthenticated) throws Exception {
                super.onSuccess(hasAuthenticated);
                Logger.d(TAG, "Log written!");
            }
        }.execute();
    }


    // -----------------------------------
    // Logger config methods
    // -----------------------------------

    /**
     * 设置日志文件名前缀
     *
     * @param prefix (prefix-20121212.log)
     * @return
     */
    public static String setLogPrefix(final String prefix) {
        return (prefix.length() == 0) ? "logger-" : prefix + "-";
    }

    /**
     * 设置日志文件存放路径
     *
     * @param subPath 子路径("/Downloads/subPath")
     * @return
     */
    public static String setLogPath(final String subPath) {
        return subPath.length() == 0 ? DOWNLOADS_PATH + "/logs" : subPath;
    }

}