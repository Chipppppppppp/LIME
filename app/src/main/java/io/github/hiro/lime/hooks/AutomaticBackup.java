package io.github.hiro.lime.hooks;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;
import io.github.hiro.lime.R;

public class AutomaticBackup implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        XposedHelpers.findAndHookMethod("jp.naver.line.android.activity.schemeservice.LineSchemeServiceActivity",
                loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Context moduleContext = AndroidAppHelper.currentApplication().createPackageContext(
                                "io.github.hiro.lime", Context.CONTEXT_IGNORE_SECURITY);
                        Intent intent = ((Activity) param.thisObject).getIntent();
                        handleIntent(intent, param.thisObject,moduleContext);
                    }
                });
    }

    private void handleIntent(Intent intent, Object activity,Context moduleContext) {
        if (intent != null) {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);

            if (moduleContext.getResources().getString(R.string.Talk_Back_up).equals(text)) {
                backupChatHistory(((Activity) activity).getApplicationContext(),moduleContext);
            }
            if (moduleContext.getResources().getString(R.string.Talk_Picture_Back_up).equals(text)) {
                backupChatsFolder(((Activity) activity).getApplicationContext(),moduleContext);
            }

            if (moduleContext.getResources().getString(R.string.BackUp_Stat).equals(text)) {
                backupChatHistory(((Activity) activity).getApplicationContext(),moduleContext);
                backupChatsFolder(((Activity) activity).getApplicationContext(),moduleContext);
            }
        }
    }


    private void backupChatHistory(Context appContext,Context moduleContext) {
        File originalDbFile = appContext.getDatabasePath("naver_line");
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LimeBackup");
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            return;
        }
        File backupFileWithTimestamp = new File(backupDir, "naver_line_backup" + ".db");

        try (FileChannel source = new FileInputStream(originalDbFile).getChannel()) {
            try (FileChannel destinationWithTimestamp = new FileOutputStream(backupFileWithTimestamp).getChannel()) {
                destinationWithTimestamp.transferFrom(source, 0, source.size());
            }
            showToast(appContext,moduleContext.getResources().getString(R.string.Talk_Auto_Back_up_Success)); // トーストをUIスレッドで表示

        } catch (IOException ignored) {
            showToast(appContext, moduleContext.getResources().getString(R.string.Talk_Auto_Back_up_Error));
        }
    }
    private void backupChatsFolder(Context context,Context moduleContext) {
        File originalChatsDir = new File(Environment.getExternalStorageDirectory(), "Android/data/jp.naver.line.android/files/chats");
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LimeBackup");

        if (!backupDir.exists() && !backupDir.mkdirs()) {
            return;
        }
        File backupChatsDir = new File(backupDir, "chats_backup");
        if (!backupChatsDir.exists() && !backupChatsDir.mkdirs()) {
            return;
        }
        try {
            copyDirectory(originalChatsDir, backupChatsDir);

            Toast.makeText(context,moduleContext.getResources().getString(R.string.Talk_Picture_Back_up_Success), Toast.LENGTH_SHORT).show();
        } catch (IOException ignored) {
            Toast.makeText(context, moduleContext.getResources().getString(R.string.Talk_Picture_Back_up_Error), Toast.LENGTH_SHORT).show();
        }
    }

    private void copyDirectory(File sourceDir, File destDir) throws IOException {
        if (!sourceDir.exists()) {
            throw new IOException("Source directory does not exist: " + sourceDir.getAbsolutePath());
        }

        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                File destFile = new File(destDir, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, destFile);
                } else {
                    copyFile(file, destFile);
                }
            }
        }
    }
    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (destFile.exists()) {
            destFile.delete();
        }
        try (FileChannel sourceChannel = new FileInputStream(sourceFile).getChannel();
             FileChannel destChannel = new FileOutputStream(destFile).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }
    private void showToast(final Context context, final String message) {
        new android.os.Handler(context.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }
}
