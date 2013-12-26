package org.syxc.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Locale;

/**
 * {@link UncaughtExceptionHandler}  send an e-mail with
 * some debug information to the developer.
 * <p/>
 * In the Activity of onCreate calling methods:
 * <p/>
 * CrashHandler crashHandler = CrashHandler.getInstance();
 * crashHandler.init(this);
 */
public class CrashHandler implements UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";

    private static final String RECIPIENT = "yourname@gmail.com";

    private static CrashHandler instance;
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            Date curDate = new Date();

            StringBuilder report = new StringBuilder();
            report.append("Error Report collected on : ").append(curDate.toString()).append('\n').append('\n');
            report.append("Infomations: ").append('\n');
            addInformation(report);
            report.append('\n').append('\n');
            report.append("Stack:\n");

            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);

            e.printStackTrace(printWriter);
            report.append(result.toString());
            printWriter.close();
            report.append('\n');
            report.append("****  End of current Report ***");

            Logger.e(TAG, "Error while sendErrorMail " + report);

            sendErrorMail(report);
        } catch (Throwable ignore) {
            Logger.e(TAG, "Error while sending error e-mail", ignore);
        }
    }

    private StatFs getStatFs() {
        File path = Environment.getDataDirectory();
        return new StatFs(path.getPath());
    }

    private long getAvailableInternalMemorySize(StatFs stat) {
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    private long getTotalInternalMemorySize(StatFs stat) {
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    private void addInformation(StringBuilder message) {
        message.append("Locale: ").append(Locale.getDefault()).append('\n');
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi;
            pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            message.append("Version: ").append(pi.versionName).append('\n');
            message.append("Package: ").append(pi.packageName).append('\n');
        } catch (Exception e) {
            Logger.e("CustomExceptionHandler", "Error", e);
            message.append("Could not get Version information for ").append(mContext.getPackageName());
        }
        message.append("Phone Model: ").append(Build.MODEL).append('\n');
        message.append("Android Version: ").append(Build.VERSION.RELEASE).append('\n');
        message.append("Board: ").append(Build.BOARD).append('\n');
        message.append("Brand: ").append(Build.BRAND).append('\n');
        message.append("Device: ").append(Build.DEVICE).append('\n');
        message.append("Host: ").append(Build.HOST).append('\n');
        message.append("ID: ").append(Build.ID).append('\n');
        message.append("Model: ").append(Build.MODEL).append('\n');
        message.append("Product: ").append(Build.PRODUCT).append('\n');
        message.append("Type: ").append(Build.TYPE).append('\n');

        StatFs stat = getStatFs();

        message.append("Total Internal memory: ")
                .append(getTotalInternalMemorySize(stat))
                .append('\n');
        message.append("Available Internal memory: ")
                .append(getAvailableInternalMemorySize(stat))
                .append('\n');
    }

    /**
     * This method for call alert dialog when application crashed!
     *
     * @param errorContent
     */
    private void sendErrorMail(final StringBuilder errorContent) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        new Thread() {

            @Override
            public void run() {
                Looper.prepare();

                builder.setTitle("Sorry...!");
                builder.create();
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                builder.setPositiveButton("Report", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);

                        String subject = "Your App crashed! Fix it!";
                        StringBuilder body = new StringBuilder("Yoddle");
                        body.append('\n').append('\n');
                        body.append(errorContent).append('\n').append('\n');

                        sendIntent.setType("message/rfc822");
                        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{RECIPIENT});
                        sendIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                        sendIntent.setType("message/rfc822");

                        mContext.startActivity(Intent.createChooser(sendIntent, "Error Report"));

                        System.exit(0);
                    }
                });
                builder.setMessage("Unfortunately, This application has stopped!");
                builder.show();

                Looper.loop();
            }
        }.start();
    }

}
