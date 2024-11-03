package io.github.hiro.lime.hooks;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;;
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

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;
import io.github.hiro.lime.hooks.IHook;

public class AutomaticBackup implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        XposedHelpers.findAndHookMethod("jp.naver.line.android.activity.schemeservice.LineSchemeServiceActivity",
                loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Intent intent = ((Activity) param.thisObject).getIntent();
                        XposedBridge.log("onCreate Intent action: " + intent.getAction());
                        handleIntent(intent, param.thisObject);
                    }
                });
    }
    private void handleIntent(Intent intent, Object activity) {
        if (intent != null) {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            if ("トーク履歴のバックアップを開始".equals(text)) {
                backupChatHistory(((Activity) activity).getApplicationContext());
            }
            if ("トーク画像フォルダのバックアップを開始".equals(text)) {
                backupChatsFolder(((Activity) activity).getApplicationContext());
            }
            if ("バックアップを開始".equals(text)) {
                backupChatHistory(((Activity) activity).getApplicationContext());
                backupChatsFolder(((Activity) activity).getApplicationContext());
            }
        }
    }



    private void backupChatHistory(Context appContext) {
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
            showToast(appContext, "自動バックアップが成功しました"); // トーストをUIスレッドで表示

        } catch (IOException e) {
            showToast(appContext, "自動バックアップ中にエラーが発生しました: " + e.getMessage());
        }
    }
    private void backupChatsFolder(Context context) {
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
            Toast.makeText(context, "トーク画像フォルダのバックアップが成功しました", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "トーク画像フォルダのバックアップ中にエラーが発生しました", Toast.LENGTH_SHORT).show();
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
