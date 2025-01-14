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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class PhotoAddNotification implements IHook {

    private static final int MAX_RETRIES = 20;

    private static final long RETRY_DELAY = 1000;
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.PhotoAddNotification.checked) return;

        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Application appContext = (Application) param.thisObject;

                if (appContext == null) {
                    ////XposedBridge.log("Application context is null!");
                    return;
                }

                Context moduleContext;
                try {
                    moduleContext = appContext.createPackageContext(
                            "io.github.hiro.lime", Context.CONTEXT_IGNORE_SECURITY);
                } catch (PackageManager.NameNotFoundException ignored) {
                    ////XposedBridge.log("Failed to create package context: " + e.getMessage());
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
                                         Context context, SQLiteDatabase db1, SQLiteDatabase db2) {

        Class<?> notificationManagerClass = XposedHelpers.findClass(
                "android.app.NotificationManager", loadPackageParam.classLoader
        );
        XposedHelpers.findAndHookMethod(notificationManagerClass, "notify",
                String.class, int.class, Notification.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        Notification notification = (Notification) param.args[2];
                        String tag = (String) param.args[0];
                        int ids = (int) param.args[1];
                        logAllNotificationDetails("notify", ids, notification, tag);
                        if (Objects.equals(notification.category, "call")) {
                            return;
                        }
                        if (param.args[0] == null) {
                            param.setResult(null);
                            return;
                        }

                        handleNotificationHook(context, db1, db2, param,notification, true);

                    }
                });

    }
    private static boolean isHandlingNotification = false;
    private static final Set<String> processedNotifications = new HashSet<>();

    private void handleNotificationHook(Context context, SQLiteDatabase db1, SQLiteDatabase db2, XC_MethodHook.MethodHookParam param, Notification notification, boolean hasTag) {

        if (isHandlingNotification) {
            return;
        }

        isHandlingNotification = true;



        try {
           Notification originalNotification = hasTag ? (Notification) param.args[2] : (Notification) param.args[1];
            String title = getNotificationTitle(originalNotification);


            String originalText = getNotificationText(originalNotification);
            Notification newNotification = originalNotification;

            if (title == null) {
                return;
            }

            if (notification.extras != null) {
                Bundle extras = notification.extras;
          //XposedBridge.log("Notification Extras:");
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
             //XposedBridge.log("  " + key + ": " + (value != null ? value.toString() : "null"));
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

                if (extras.containsKey("line.message.id")) {
                    String TalkId = extras.getString("line.message.id");

                    int retryCount = 0;
                    boolean foundValidData = false;
                   //XposedBridge.log(TalkId);
                    while (retryCount < MAX_RETRIES && !foundValidData) {
                        if (TalkId != null) {
                            String chatId = queryDatabase(db1, "SELECT chat_id FROM chat_history WHERE server_id =?", TalkId);
                            String id = queryDatabase(db1, "SELECT id FROM chat_history WHERE server_id =?", TalkId);

                            if (chatId != null && id != null) {
                                foundValidData = true;
                               //XposedBridge.log("Found Chat ID: " + chatId + ", Message ID: " + id);

                                File latestFile = getFileWithId(chatId, id);
                                if (latestFile == null) {
                                   //XposedBridge.log("No file found for Chat ID: " + chatId + " and ID: " + id);
                                    return;
                                }

                                Bitmap bitmap = loadBitmapFromFile(latestFile);
                                if (bitmap == null) {
                                   //XposedBridge.log("Failed to load bitmap from file: " + latestFile.getAbsolutePath());
                                    return;
                                }


                                newNotification = createNotificationWithImageFromFile(context, originalNotification, latestFile, originalText);
                               //XposedBridge.log("Created new notification with image.");


                                param.setResult(null);

                                if (hasTag) {
                                    param.args[2] = newNotification;
                                } else {
                                    param.args[1] = newNotification;
                                }

//
//                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                                if (notificationManager != null) {
//                                    notificationManager.cancel(ids);  // Cancel the original notification
//                                    notificationManager.notify(ids, newNotification);  // Show the new notification with the image
//                                }
                            } else {
                                // Log and retry if either chatId or id is null
                               //XposedBridge.log("Chat ID or Message ID is null, retrying...");
                                retryCount++;
                                try {
                                    Thread.sleep(RETRY_DELAY); // Wait before retrying
                                } catch (InterruptedException e) {
                                   //XposedBridge.log("Retry interrupted.");
                                }
                            }
                        } else {
                           //XposedBridge.log("TalkId is null, retrying...");
                            retryCount++;
                            try {
                                Thread.sleep(RETRY_DELAY); // Wait before retrying
                            } catch (InterruptedException e) {
                               //XposedBridge.log("Retry interrupted.");
                            }
                        }
                    }

                    if (!foundValidData) {
                       //XposedBridge.log("Failed to retrieve valid Chat ID and Message ID after " + MAX_RETRIES + " retries.");
                    }
                }
            }

            param.setResult(null);



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

    private File getFileWithId(String chatId, String id) {
        // Xposedログ: メソッド開始
       //XposedBridge.log("getFileWithId: Searching for file with id: " + id + " in chat: " + chatId);

        File messagesDir = new File(Environment.getExternalStorageDirectory(),
                "/Android/data/jp.naver.line.android/files/chats/" + chatId + "/messages");

        if (!messagesDir.exists() || !messagesDir.isDirectory()) {
           //XposedBridge.log("getFileWithId: Messages directory does not exist or is not a directory.");
            return null;
        }

        // バックアップ用ディレクトリとファイル
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LimeBackup");
        if (!backupDir.exists() && !backupDir.mkdirs()) {
           //XposedBridge.log("getFileWithId: Failed to create backup directory.");
            return null; // ディレクトリ作成失敗
        }

        File waitTimeFile = new File(backupDir, "wait_time.txt");

        long waitTimeMillis = 1000; // デフォルトの待機時間（1秒）
        if (!waitTimeFile.exists()) {
            try (FileWriter writer = new FileWriter(waitTimeFile)) {
                writer.write(String.valueOf(waitTimeMillis)); // デフォルト値を書き込む
               //XposedBridge.log("getFileWithId: Wait time file created with default value: " + waitTimeMillis);
            } catch (IOException e) {
               //XposedBridge.log("getFileWithId: Failed to write wait time file: " + e.getMessage());
                return null; // ファイル作成失敗
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(waitTimeFile))) {
                String line = reader.readLine();
                waitTimeMillis = Long.parseLong(line); // ファイルから待機時間を取得
               //XposedBridge.log("getFileWithId: Wait time read from file: " + waitTimeMillis);
            } catch (IOException | NumberFormatException e) {
               //XposedBridge.log("getFileWithId: Failed to read or parse wait time: " + e.getMessage());
                return null; // 読み込みまたは解析失敗
            }
        }

        // 待機
        try {
           //XposedBridge.log("getFileWithId: Sleeping for " + waitTimeMillis + " milliseconds.");
            Thread.sleep(waitTimeMillis);
        } catch (InterruptedException e) {
           //XposedBridge.log("getFileWithId: Thread interrupted during sleep.");
            return null; // スレッド中断
        }

        // Check each file for the specific id
        File[] files = messagesDir.listFiles((dir, name) -> !name.endsWith(".downloading"));
        if (files == null || files.length == 0) {
           //XposedBridge.log("getFileWithId: No files found in the messages directory.");
            return null;
        }

        // Iterate through the files and find the one containing the id
        for (File file : files) {
           //XposedBridge.log("getFileWithId: Checking file: " + file.getName());
            if (file.getName().contains(id)) {
               //XposedBridge.log("getFileWithId: Found file containing id: " + file.getName());
                return file; // Return the first file containing the id
            }
        }

       //XposedBridge.log("getFileWithId: No file with the specified id found.");
        return null; // No file with the specified id found
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



    private void logAllNotificationDetails(String method, int ids, Notification notification, String tag) {
       XposedBridge.log(method + " called. ID: " + ids + (tag != null ? ", Tag: " + tag : ""));
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
           //XposedBridge.log("No actions found.");
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
