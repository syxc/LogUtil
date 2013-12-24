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
    private static final String DOWNLOADS_PATH;

    // Log switch open, development, released when closed(LogCat)
    public static boolean DEBUG;

    // 日志的写入模式，是否重写
    public static boolean APPEND;

    // Write file level
    public static int LOG_LEVEL;

    public static int BUFFER_SIZE;

    protected static final String LOG_PREFIX;
    protected static final String LOG_DIR;

    static {
        mChannel = null;
        LINE_SEPARATOR = System.getProperty("line.separator");
        DOWNLOADS_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        DEBUG = true;
        APPEND = true;
        LOG_LEVEL = Log.ERROR;
        BUFFER_SIZE = 1024;
        LOG_PREFIX = setLogPrefix("");
        LOG_DIR = setLogPath("");
    }


    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
        if (Log.VERBOSE >= LOG_LEVEL) {
            writeLog(Log.VERBOSE, msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.v(tag, msg, tr);
        }
        if (Log.VERBOSE >= LOG_LEVEL) {
            writeLog(Log.VERBOSE, msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
        if (Log.DEBUG >= LOG_LEVEL) {
            writeLog(Log.DEBUG, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.d(tag, msg, tr);
        }
        if (Log.DEBUG >= LOG_LEVEL) {
            writeLog(Log.DEBUG, msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
        if (Log.INFO >= LOG_LEVEL) {
            writeLog(Log.INFO, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.i(tag, msg, tr);
        }
        if (Log.INFO >= LOG_LEVEL) {
            writeLog(Log.INFO, msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
        if (Log.WARN >= LOG_LEVEL) {
            writeLog(Log.WARN, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.w(tag, msg, tr);
        }
        if (Log.WARN >= LOG_LEVEL) {
            writeLog(Log.WARN, msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
        if (Log.ERROR >= LOG_LEVEL) {
            writeLog(Log.ERROR, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.e(tag, msg, tr);
        }
        if (Log.ERROR >= LOG_LEVEL) {
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

            recordLog(LOG_DIR, fileName, msg, APPEND);

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
     * @param append   Save as type, false override save, true before file add save
     */
    private static void recordLog(String logDir, String fileName, String msg, boolean append) {
        try {
            if (!createDirectory(logDir)) {
                Logger.d(TAG, "Create directory fail!!!");
                return;
            }

            final File saveFile = new File(new StringBuffer()
                    .append(logDir)
                    .append(File.separator)
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
            if (mChannel != null) {
                try {
                    mChannel.close();
                } catch (IOException e) {
                    Logger.d(TAG, "Exception closing mChannel: ", e);
                }
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
        String str;
        if (prefix.length() == 0) {
            str = "logger-";
        } else {
            str = new StringBuilder().append(prefix).append("-").toString();
        }
        return str;
    }

    /**
     * 设置日志文件存放路径
     *
     * @param subPath 子路径("/Downloads/subPath")
     * @return 日志文件路径
     */
    public static String setLogPath(final String subPath) {
        String path;
        if (subPath.length() == 0) {
            path = new StringBuilder().append(DOWNLOADS_PATH).append("/logs").toString();
        } else {
            path = new StringBuilder().append(DOWNLOADS_PATH).append(subPath).toString();
        }
        return path;
    }

}
