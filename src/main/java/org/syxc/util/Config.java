package org.syxc.util;

import android.os.Environment;
import android.util.Log;

/**
 * 日志工具类的配置信息
 *
 * @author syxc
 */
public final class Config {

    private static final String DOWNLOADS_PATH;

    // Log switch open, development, released when closed(LogCat)
    public static final boolean DEBUG;

    // Write file level
    public static final int LOG_LEVEL;

    public static final int BUFFER_SIZE;

    protected static final String LOG_PREFIX;
    protected static final String LOG_DIR;

    static {
        DOWNLOADS_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        DEBUG = false;
        LOG_LEVEL = Log.ERROR;
        BUFFER_SIZE = 1024;
        LOG_PREFIX = setLogPrefix("");
        LOG_DIR = setLogPath("");
    }

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
