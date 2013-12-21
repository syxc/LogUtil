package org.syxc.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * A custom Android log class
 *
 * @author syxc
 */
public final class Logger {

    private static final String TAG = "Logger";

    private static FileChannel mChannel;
    private static final String LINE_SEPARATOR;
    private static int BUFFER_SIZE;
    private static final String LOG_PREFIX;

    private static final boolean DEBUG;
    private static int level;

    static {
        mChannel = null;
        LINE_SEPARATOR = System.getProperty("line.separator");
        BUFFER_SIZE = Config.BUFFER_SIZE;
        LOG_PREFIX = Config.LOG_PREFIX;
        DEBUG = Config.DEBUG;
        level = Config.LOG_LEVEL;
    }


    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
        if (Log.VERBOSE >= level) {
            writeLog(Log.VERBOSE, msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.v(tag, msg, tr);
        }
        if (Log.VERBOSE >= level) {
            writeLog(Log.VERBOSE, msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
        if (Log.DEBUG >= level) {
            writeLog(Log.DEBUG, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.d(tag, msg, tr);
        }
        if (Log.DEBUG >= level) {
            writeLog(Log.DEBUG, msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
        if (Log.INFO >= level) {
            writeLog(Log.INFO, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.i(tag, msg, tr);
        }
        if (Log.INFO >= level) {
            writeLog(Log.INFO, msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
        if (Log.WARN >= level) {
            writeLog(Log.WARN, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.w(tag, msg, tr);
        }
        if (Log.WARN >= level) {
            writeLog(Log.WARN, msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
        if (Log.ERROR >= level) {
            writeLog(Log.ERROR, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.e(tag, msg, tr);
        }
        if (Log.ERROR >= level) {
            writeLog(Log.ERROR, msg + '\n' + getStackTraceString(tr));
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
     * Write log file to the sdcard
     *
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

            msg = new StringBuilder()
                    .append(getDateFormat(DateFormater.SS.getValue()))
                    .append(logMap.get(type)).append(tag.getClassName())
                    .append(" - ").append(tag.getMethodName()).append("(): ")
                    .append(msg).toString();

            final String fileName = new StringBuffer()
                    .append(LOG_PREFIX)
                    .append(getDateFormat(DateFormater.DD.getValue()))
                    .append(".log").toString();

            recordLog(Config.LOG_DIR, fileName, msg, true);

        } catch (Exception e) {
            Logger.d(TAG, e.getMessage());
        }
    }

    /**
     * Write log
     *
     * @param logDir   Log path to save
     * @param fileName
     * @param msg      Log content
     * @param bool     Save as type, false override save, true before file add save
     */
    private static void recordLog(String logDir, String fileName, String msg, boolean bool) {
        try {
            if (!createDirectory(logDir)) {
                Logger.d(TAG, "Create directory fail!!!");
                return;
            }

            final File saveFile = new File(new StringBuffer()
                    .append(logDir)
                    .append(File.separator)
                    .append(fileName).toString());

            if (!bool && saveFile.exists()) {
                saveFile.delete();
                saveFile.createNewFile();
                write(saveFile, msg, bool);
            } else if (bool && saveFile.exists()) {
                write(saveFile, msg, bool);
            } else if (bool && !saveFile.exists()) {
                saveFile.createNewFile();
                write(saveFile, msg, bool);
            }
        } catch (IOException e) {
            Logger.d(TAG, "recordLog fail!!!");
        }
    }

    private static String getDateFormat(String pattern) {
        final DateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }

    /**
     * Create directory
     *
     * @param dir file path
     * @return true: success  false: fail
     */
    private static boolean createDirectory(String dir) {
        final File file = new File(dir);
        boolean flag = false;
        if (!file.exists()) {
            flag = file.mkdirs();
        }
        return flag;
    }

    /**
     * Write msg to file
     *
     * @param file
     * @param msg
     * @param append
     */
    private static void write(final File file, String msg, boolean append) {
        FileOutputStream output = null;
        ByteBuffer byteBuffer = null;
        try {
            output = new FileOutputStream(file, append);

            // set up our output channel and byte buffer
            mChannel = output.getChannel();
            byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);

            if (mChannel != null) {
                byteBuffer.put(msg.getBytes());
                byteBuffer.put(LINE_SEPARATOR.getBytes());
                byteBuffer.flip();

                mChannel.write(byteBuffer);
                mChannel.force(true);
            }
        } catch (IOException e) {
            Logger.d(TAG, "Exception writing cache " + file.getName(), e);
        } finally {
            if (byteBuffer != null) {
                byteBuffer.clear();
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    Logger.d(TAG, "Exception closing stream: ", e);
                }
            }
        }
    }

}
