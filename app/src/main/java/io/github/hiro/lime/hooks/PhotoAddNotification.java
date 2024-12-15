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

import java.io.File;
import java.util.Arrays;

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

    private void handleNotificationHook(Context context, SQLiteDatabase dbGroups, SQLiteDatabase dbContacts, XC_MethodHook.MethodHookParam param, boolean hasTag) {
        Notification notification = hasTag ? (Notification) param.args[2] : (Notification) param.args[1];
        String title = getNotificationTitle(notification);

        if (title == null) {
           // XposedBridge.log("Notification title is null. Skipping.");
            return;
        }

        // 通知の本文を取得
        String originalText = getNotificationText(notification);
        if (originalText != null && (originalText.contains("写真を送信しました") || originalText.contains("sent a photo"))) { // "写真を送信しました" または "sent a photo" が含まれている場合のみ処理
           // XposedBridge.log("Target notification detected: " + originalText);

            String chatId = resolveChatId(dbGroups, dbContacts, notification);
            if (chatId == null) {
               // XposedBridge.log("Chat ID not found for title: " + title);
                return;
            }

            File latestFile = getLatestMessageFile(chatId);
            if (latestFile == null) {
               // XposedBridge.log("No latest message file found for chat ID: " + chatId);
                return;
            }

            Bitmap bitmap = loadBitmapFromFile(latestFile);
            if (bitmap == null) {
               // XposedBridge.log("Failed to load bitmap from file: " + latestFile.getAbsolutePath());
                return;
            }

            Notification newNotification = createNotificationWithImageFromFile(context, notification, latestFile, originalText);

            if (hasTag) {
                param.args[2] = newNotification;
            } else {
                param.args[1] = newNotification;
            }

            logNotificationDetails("Modified NotificationManager.notify", hasTag ? (int) param.args[1] : -1, newNotification, hasTag ? (String) param.args[0] : null);
        }
    }


    private String resolveChatId(SQLiteDatabase dbGroups, SQLiteDatabase dbContacts, Notification notification) {
        String subText = notification.extras.getString(Notification.EXTRA_SUB_TEXT);
        if (subText != null) {
            String groupId = queryDatabase(dbGroups, "SELECT id FROM groups WHERE name =?", subText);
            if (groupId != null) {
                return groupId;
            }
            String talkId = queryDatabase(dbContacts, "SELECT mid FROM contacts WHERE profile_name =?", subText);
            return talkId;
        }
        return null;
    }

    private File getLatestMessageFile(String chatId) {
        File messagesDir = new File(Environment.getExternalStorageDirectory(),
                "/Android/data/jp.naver.line.android/files/chats/" + chatId + "/messages");
        if (!messagesDir.exists() || !messagesDir.isDirectory()) {
           // XposedBridge.log("Messages directory does not exist: " + messagesDir.getAbsolutePath());
            return null;
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
           // XposedBridge.log("Sleep interrupted: " + e.getMessage());
        }

        File[] files = messagesDir.listFiles((dir, name) -> !name.endsWith(".thumb") && !name.endsWith(".downloading"));
        if (files == null || files.length == 0) {
           // XposedBridge.log("No files found in messages directory: " + messagesDir.getAbsolutePath());
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
       // XposedBridge.log(method + " called. ID: " + id + (tag != null ? ", Tag: " + tag : ""));
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
