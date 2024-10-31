package io.github.hiro.lime.hooks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;
import io.github.hiro.lime.hooks.IHook;

public class AutomaticBackup implements IHook {

    private static final String TAG = "AutomaticBackup";
    private ScheduledExecutorService scheduler;
    private static final String PREFS_NAME = "BackupPrefs";
    public static final String KEY_BACKUP_INTERVAL = "backup_interval_in_millis";
    private static final long DAILY_INTERVAL = 24 * 60 * 60 * 1000;

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.AutomaticBackup.checked) return;

        XposedBridge.hookAllMethods(Activity.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                Context appContext = activity.getApplicationContext();
                if (scheduler == null || scheduler.isShutdown()) {
                    startScheduledBackup(appContext);
                }
            }
        });
    }

    private void startScheduledBackup(Context appContext) {
        // 1日ごとにバックアップを行う
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> checkAndBackupChatHistory(appContext), 0, DAILY_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    private void checkAndBackupChatHistory(Context appContext) {
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LimeBackup");
        File[] backupFiles = backupDir.listFiles((dir, name) -> name.startsWith("naver_line_backup_"));

        long currentTime = System.currentTimeMillis();
        long lastBackupTime = 0;

        if (backupFiles != null && backupFiles.length > 0) {
            File latestBackupFile = backupFiles[0];
            for (File file : backupFiles) {
                if (file.lastModified() > latestBackupFile.lastModified()) {
                    latestBackupFile = file;
                }
            }
            lastBackupTime = latestBackupFile.lastModified();
        }

        // 最後のバックアップから指定された間隔が経過しているか確認
        if (currentTime - lastBackupTime >= DAILY_INTERVAL) {
            backupChatHistory(appContext);

        }
    }

    private void backupChatHistory(Context appContext) {
        File originalDbFile = appContext.getDatabasePath("naver_line");
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LimeBackup");
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            return;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File backupFileWithTimestamp = new File(backupDir, "naver_line_backup_" + timeStamp + ".db");

        try (FileChannel source = new FileInputStream(originalDbFile).getChannel()) {
            try (FileChannel destinationWithTimestamp = new FileOutputStream(backupFileWithTimestamp).getChannel()) {
                destinationWithTimestamp.transferFrom(source, 0, source.size());
            }
            showToast(appContext, "自動バックアップが成功しました"); // トーストをUIスレッドで表示

        } catch (IOException e) {
            showToast(appContext, "自動バックアップ中にエラーが発生しました: " + e.getMessage());
        }
    }

    // トーストをUIスレッドで表示するヘルパーメソッド
    private void showToast(final Context context, final String message) {
        new android.os.Handler(context.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }
}