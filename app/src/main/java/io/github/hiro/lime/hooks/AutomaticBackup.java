package io.github.hiro.lime.hooks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;;
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
                if ("バックアップを開始".equals(text)) {
                    backupChatHistory(((Activity) activity).getApplicationContext());
                }
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

        private void showToast(final Context context, final String message) {
            new android.os.Handler(context.getMainLooper()).post(() ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            );
        }
    }
