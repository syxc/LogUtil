package org.syxc.util;

import android.app.Application;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;


public class CrashHandler implements UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";

    private Application mApplication;
    private UncaughtExceptionHandler mDefaultHandler;
    private boolean isWrite;

    public CrashHandler(Application application, boolean write) {
        this.mApplication = application;
        this.isWrite = write;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (isWrite) {
            writeExceptionHandler(ex);
        }
        mDefaultHandler.uncaughtException(thread, ex);
    }

    private void writeExceptionHandler(Throwable ex) {
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
            Logger.e(TAG, info);
            // kill application
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
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
