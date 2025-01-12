package io.github.hiro.lime.hooks;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class PhotoAddNotification implements IHook {

    private static Set<Integer> activeNotificationIds = new HashSet<>();
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.PhotoAddNotification.checked) return;

        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Application appContext = (Application) param.thisObject;

                if (appContext == null) {
                    // XposedBridge.log("Application context is null!");
                    return;
                }

                Context moduleContext;
                try {
                    moduleContext = appContext.createPackageContext(
                            "io.github.hiro.lime", Context.CONTEXT_IGNORE_SECURITY);
                } catch (PackageManager.NameNotFoundException ignored) {
                    // XposedBridge.log("Failed to create package context: " + e.getMessage());
                    return;
                }
                File dbFile1 = appContext.getDatabasePath("naver_line");
                File dbFile2 = appContext.getDatabasePath("contact");

                if (dbFile1.exists() && dbFile2.exists()) {
                    SQLiteDatabase.OpenParams.Builder builder1 = new SQLiteDatabase.OpenParams.Builder();
                    builder1.addOpenFlags(SQLiteDatabase.OPEN_READWRITE);
                    SQLiteDatabase.OpenParams dbParams1 = builder1.build();

                    SQLiteDatabase.OpenParams.Builder builder2 = new SQLiteDatabase.OpenParams.Builder();
                    builder2.addOpenFlags(SQLiteDatabase.OPEN_READWRITE);
                    SQLiteDatabase.OpenParams dbParams2 = builder2.build();

                    SQLiteDatabase db1 = SQLiteDatabase.openDatabase(dbFile1, dbParams1);
                    SQLiteDatabase db2 = SQLiteDatabase.openDatabase(dbFile2, dbParams2);

                    hookNotificationMethods(loadPackageParam, appContext, db1, db2);
                }
            }
        });
    }

    private void hookNotificationMethods(XC_LoadPackage.LoadPackageParam loadPackageParam,
                                         Context context, SQLiteDatabase dbGroups, SQLiteDatabase dbContacts) {

        Class<?> notificationManagerClass = XposedHelpers.findClass(
                "android.app.NotificationManager", loadPackageParam.classLoader
        );
        XposedHelpers.findAndHookMethod(notificationManagerClass, "notify",
                String.class, int.class, Notification.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        String tag = (String) param.args[0];
                        int id = (int) param.args[1];
                        Notification notification = (Notification) param.args[2];

                        if (param.args[0] == null) {
                            param.setResult(null);
                            return;
                        }
                        //logAllNotificationDetails("notify", id, notification, tag);

                        handleNotificationHook(context, dbGroups, dbContacts, param,notification, true);

                    }
                });

    }
                        // param.argsの内容を詳細にログに出力
//                        for (int i = 0; i < param.args.length; i++) {
//                          //  Log.d("NotificationHook", "param.args[" + i + "]: " + param.args[i] + " (type: " + (param.args[i] != null ? param.args[i].getClass().getName() : "null") + ")");
//                        }
//                        // notifyメソッドの引数を動的に判断
//                        String tag = null;
//                        Integer id = null;
//                        Notification notification = null;

//                        if (param.args.length == 2) {
//                            // notify(int id, Notification notification)
//                            if (param.args[0] instanceof Integer && param.args[1] instanceof Notification) {
//                                id = (Integer) param.args[0];
//                                notification = (Notification) param.args[1];
//                            }
//                        } else if (param.args.length == 3) {
//                            // notify(String tag, int id, Notification notification)
//                            if (param.args[0] instanceof String && param.args[1] instanceof Integer && param.args[2] instanceof Notification) {
//                                tag = (String) param.args[0];
//                                id = (Integer) param.args[1];
//                                notification = (Notification) param.args[2];
//                            }
//                        } else {
//                            Log.e("NotificationHook", "Unexpected number of arguments: " + param.args.length);
//                            return;
//                        }
//
//                        // idまたはnotificationがnullの場合の処理
//                        if (id == null || notification == null) {
//                            // Log.e("NotificationHook", "Invalid arguments: id=" + id + ", notification=" + notification);
//                            return;
//                        }
//
//                        // ログに出力
//                        Log.d("NotificationHook", "notify called with tag: " + tag + ", id: " + id);
//                        Log.d("NotificationHook", "Notification title: " + getNotificationTitle(notification));
//                        Log.d("NotificationHook", "Notification text: " + getNotificationText(notification));





    private static boolean isHandlingNotification = false;


    private static final Set<String> processedNotifications = new HashSet<>();

    private void handleNotificationHook(Context context, SQLiteDatabase dbGroups, SQLiteDatabase dbContacts, XC_MethodHook.MethodHookParam param, Notification notification, boolean hasTag) {

        if (isHandlingNotification) {
            return;
        }

        isHandlingNotification = true;

        

        try {
           Notification originalNotification = hasTag ? (Notification) param.args[2] : (Notification) param.args[1];
            String title = getNotificationTitle(originalNotification);


String originalText = getNotificationText(originalNotification);
            Notification newNotification = originalNotification;

            if (originalText.contains("LINE音声通話を着信中") ||
                    originalText.contains("Incoming LINE voice call") ||
                    originalText.contains("LINE語音通話來電中")) {
                return;
            }




 
            if (title == null) {
                return;
            }

            if (notification.extras != null) {
                Bundle extras = notification.extras;
//                XposedBridge.log("Notification Extras:");
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                   // XposedBridge.log("  " + key + ": " + (value != null ? value.toString() : "null"));
                }
                if (extras.containsKey("line.sticker.url")) {
                    String stickerUrl = extras.getString("line.sticker.url");
                    if (stickerUrl != null) {
                        Bitmap stickerBitmap = downloadBitmapFromUrl(stickerUrl);
                        if (stickerBitmap != null) {
                            newNotification = createNotificationWithImageFromBitmap(context, originalNotification, stickerBitmap, originalText);
                        }
                    }
                }
            }

            if (originalText != null && (originalText.contains("写真を送信しました") || originalText.contains("sent a photo") || originalText.contains("傳送了照片"))) {

                Bundle extras = notification.extras;
                // Check if line.sticker.url exists in extras
                if (extras.containsKey("line.chat.id")) {
                    String chatId = extras.getString("line.chat.id");
                    if (chatId != null) {

                        File latestFile = getLatestMessageFile(chatId);
                        if (latestFile == null) {
                            return;
                        }

                        Bitmap bitmap = loadBitmapFromFile(latestFile);
                        if (bitmap == null) {
                            return;
                        }

                        newNotification = createNotificationWithImageFromFile(context, originalNotification, latestFile, originalText);

                        param.setResult(null);

                        if (hasTag) {
                            param.args[2] = newNotification;
                        } else {
                            param.args[1] = newNotification;
                        }
                    }
                }
            }

            int randomNotificationId = (int) System.currentTimeMillis();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                if (hasTag) {
                    String tag = (String) param.args[0];
                    notificationManager.notify(tag, randomNotificationId, newNotification);
                } else {
                    notificationManager.notify(randomNotificationId, newNotification);
                }
            }

            param.setResult(null);

        } finally {
            isHandlingNotification = false;
        }
    }
    private Bitmap loadBitmapFromFile(File file) {
        if (!file.exists()) {
            return null;
        }
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    private Notification createNotificationWithImageFromFile(Context context, Notification original, File imageFile, String originalText) {
        Bitmap bitmap = loadBitmapFromFile(imageFile);
        if (bitmap == null) {
            return original;
        }
        Notification.Builder builder = Notification.Builder.recoverBuilder(context, original)
                .setStyle(new Notification.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(bitmap)
                        .setSummaryText(originalText));
        return builder.build();

    }


    private Bitmap downloadBitmapFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private Notification createNotificationWithImageFromBitmap(Context context, Notification original, Bitmap bitmap, String originalText) {
        Notification.BigPictureStyle bigPictureStyle = new Notification.BigPictureStyle()
                .bigPicture(bitmap) // メインの画像
                .bigLargeIcon(bitmap) // 画像表示領域を広げる
                .setSummaryText(originalText); // テキストを設定

        Notification.Builder builder = Notification.Builder.recoverBuilder(context, original)
                .setStyle(bigPictureStyle); // BigPictureStyle を適用

        return builder.build();
    }

    private File getLatestMessageFile(String chatId) {
        File messagesDir = new File(Environment.getExternalStorageDirectory(),
                "/Android/data/jp.naver.line.android/files/chats/" + chatId + "/messages");
        if (!messagesDir.exists() || !messagesDir.isDirectory()) {
            return null;
        }

        // バックアップ用ディレクトリとファイル
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LimeBackup");
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            return null; // ディレクトリ作成失敗
        }
        File waitTimeFile = new File(backupDir, "wait_time.txt");

        long waitTimeMillis = 1000; // デフォルトの待機時間（1秒）
        if (!waitTimeFile.exists()) {
            try (FileWriter writer = new FileWriter(waitTimeFile)) {
                writer.write(String.valueOf(waitTimeMillis)); // デフォルト値を書き込む
            } catch (IOException e) {
                return null; // ファイル作成失敗
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(waitTimeFile))) {
                String line = reader.readLine();
                waitTimeMillis = Long.parseLong(line); // ファイルから待機時間を取得
            } catch (IOException | NumberFormatException e) {
                return null; // 読み込みまたは解析失敗
            }
        }

        // 待機
        try {
            Thread.sleep(waitTimeMillis);
        } catch (InterruptedException e) {
            return null; // スレッド中断
        }

        File[] files = messagesDir.listFiles((dir, name) ->  !name.endsWith(".downloading"));
        if (files == null || files.length == 0) {
            return null;
        }

        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        return files[0];
    }


    private String queryDatabase(SQLiteDatabase db, String query, String... selectionArgs) {
        Cursor cursor = db.rawQuery(query, selectionArgs);
        String result = null;
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }
    private String getNotificationTitle(Notification notification) {
        if (notification.extras != null) {
            return notification.extras.getString(Notification.EXTRA_TITLE);
        }
        return null;
    }

    private String getNotificationText(Notification notification) {
        if (notification.extras != null) {
            return notification.extras.getString(Notification.EXTRA_TEXT);
        }
        return null;
    }



    private void logAllNotificationDetails(String method, int id, Notification notification, String tag) {
        XposedBridge.log(method + " called. ID: " + id + (tag != null ? ", Tag: " + tag : ""));
        XposedBridge.log("Notification Icon: " + notification.icon);
        XposedBridge.log("Notification When: " + notification.when);
        XposedBridge.log("Notification Flags: " + notification.flags);
        XposedBridge.log("Notification Priority: " + notification.priority);
        XposedBridge.log("Notification Category: " + notification.category);
        if (notification.extras != null) {
            Bundle extras = notification.extras;
            XposedBridge.log("Notification Extras:");
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                XposedBridge.log("  " + key + ": " + (value != null ? value.toString() : "null"));
            }
        } else {
            XposedBridge.log("Notification has no extras.");
        }

        if (notification.actions != null) {
            XposedBridge.log("Notification Actions:");
            for (int i = 0; i < notification.actions.length; i++) {
                Notification.Action action = notification.actions[i];
                XposedBridge.log("  Action " + i + ": " +
                        "Title=" + action.title +
                        ", Intent=" + action.actionIntent);
            }
        } else {
            XposedBridge.log("No actions found.");
        }

        // その他の情報
        XposedBridge.log("Notification Visibility: " + notification.visibility);
        XposedBridge.log("Notification Color: " + notification.color);
        XposedBridge.log("Notification Group: " + notification.getGroup());
        XposedBridge.log("Notification SortKey: " + notification.getSortKey());
        XposedBridge.log("Notification Sound: " + notification.sound);
        XposedBridge.log("Notification Vibrate: " + (notification.vibrate != null ? "Yes" : "No"));
    }



}
