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
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class PhotoAddNotification implements IHook {
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
        XposedHelpers.findAndHookMethod(NotificationManager.class, "notify",
                int.class, Notification.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        handleNotificationHook(context, dbGroups, dbContacts, param, false);
                    }
                });

        XposedHelpers.findAndHookMethod(NotificationManager.class, "notify",
                String.class, int.class, Notification.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        handleNotificationHook(context, dbGroups, dbContacts, param, true);
                    }
                });
    }

    private static boolean isHandlingNotification = false;

    private void handleNotificationHook(Context context, SQLiteDatabase dbGroups, SQLiteDatabase dbContacts, XC_MethodHook.MethodHookParam param, boolean hasTag) {

        if (isHandlingNotification) {
            return;
        }

        isHandlingNotification = true;

        try {
            Notification originalNotification = hasTag ? (Notification) param.args[2] : (Notification) param.args[1];
            String title = getNotificationTitle(originalNotification);

            if (title == null) {
                return;
            }

            String originalText = getNotificationText(originalNotification);
            Notification newNotification = originalNotification;
            if (originalText.contains("LINE音声通話を着信中") ||
                    originalText.contains("Incoming LINE voice call") ||
                    originalText.contains("LINE語音通話來電中")) {
                return;
            }
            if (originalText != null &&
                    (originalText.contains("写真を送信しました") ||
                            originalText.contains("sent a photo") ||
                            originalText.contains("傳送了照片"))) {

                String chatId = resolveChatId(dbGroups, dbContacts, originalNotification);
                if (chatId == null) {
                    return;
                }

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


    private String resolveChatId(SQLiteDatabase dbGroups, SQLiteDatabase dbContacts, Notification notification) {
        Set<String> keys = notification.extras.keySet();
      //  Log.d("NotificationKeys", "Notification Extras Keys: " + keys);

        String title = notification.extras.getString(Notification.EXTRA_TITLE);
        String text = notification.extras.getString(Notification.EXTRA_TEXT);
        String subText = notification.extras.getString(Notification.EXTRA_SUB_TEXT);

      /*  Log.d("ResolveChatId", "Notification Title: " + title);
        Log.d("ResolveChatId", "Notification Text: " + text);
        Log.d("ResolveChatId", "Notification SubText: " + subText);
*/
        if (subText != null) {
            String groupId = queryDatabase(dbGroups, "SELECT id FROM groups WHERE name =?", subText);
            if (groupId != null) {
                return groupId;
            }
            String talkId = queryDatabase(dbContacts, "SELECT mid FROM contacts WHERE profile_name =?", subText);
            if (talkId != null) {
            }
            return talkId;
        } else {

            // Use title if subText is null
            String groupId = queryDatabase(dbGroups, "SELECT id FROM groups WHERE name =?", title);
            if (groupId != null) {

                return groupId;
            }
            String talkId = queryDatabase(dbContacts, "SELECT mid FROM contacts WHERE profile_name =?", title);
            if (talkId != null) {
            }
            return talkId;
        }
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
                        .setSummaryText(originalText));
        return builder.build();
    }

    private void logNotificationDetails(String method, int id, Notification notification, String tag) {
      //  XposedBridge.log(method + " called. ID: " + id + (tag != null ? ", Tag: " + tag : ""));
        if (notification.extras != null) {
            String title = notification.extras.getString(Notification.EXTRA_TITLE);
            String text = notification.extras.getString(Notification.EXTRA_TEXT);
            // XposedBridge.log("Notification Title: " + (title != null ? title : "No Title"));
            // XposedBridge.log("Notification Text: " + (text != null ? text : "No Text"));
        } else {
            // XposedBridge.log("Notification has no extras.");
        }
    }
}
