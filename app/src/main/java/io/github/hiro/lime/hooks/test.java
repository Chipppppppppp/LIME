package io.github.hiro.lime.hooks;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;


import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class test implements IHook {
    private SQLiteDatabase limeDatabase;
    private SQLiteDatabase db3 = null; // クラスフィールドとして宣言
    private SQLiteDatabase db4 = null; // クラスフィールドとして宣言

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application appContext = (Application) param.thisObject;

                if (appContext == null) {
                    XposedBridge.log("appContext is null!");
                    return;
                }

                File dbFile3 = appContext.getDatabasePath("naver_line");
                File dbFile4 = appContext.getDatabasePath("contact");

                if (dbFile3.exists() && dbFile4.exists()) {
                    SQLiteDatabase.OpenParams.Builder builder1 = new SQLiteDatabase.OpenParams.Builder();
                    builder1.addOpenFlags(SQLiteDatabase.OPEN_READWRITE);
                    SQLiteDatabase.OpenParams dbParams1 = builder1.build();

                    SQLiteDatabase.OpenParams.Builder builder2 = new SQLiteDatabase.OpenParams.Builder();
                    builder2.addOpenFlags(SQLiteDatabase.OPEN_READWRITE);
                    SQLiteDatabase.OpenParams dbParams2 = builder2.build();

                    db3 = SQLiteDatabase.openDatabase(dbFile3, dbParams1); // フィールドに代入
                    db4 = SQLiteDatabase.openDatabase(dbFile4, dbParams2); // フィールドに代入

                    // データベースの初期化
                    initializeLimeDatabase(appContext);

                    // データの取得
                    Catcha(loadPackageParam, db3, db4); // ここでフィールドを使って呼び出す
                }
            }
        });
    }

    private void Catcha(XC_LoadPackage.LoadPackageParam loadPackageParam, SQLiteDatabase db3, SQLiteDatabase db4) {
        try {
            XposedBridge.hookAllMethods(
                    loadPackageParam.classLoader.loadClass(Constants.REQUEST_HOOK.className),
                    Constants.REQUEST_HOOK.methodName,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String paramValue = param.args[1].toString();
                            if (paramValue.contains("type:SEND_CHAT_CHECKED")) {
                                // Fetch data and save it to the database
                                fetchDataAndSave(db3, db4, paramValue); // db3とdb4を渡す
                            }
                        }
                    }
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void fetchDataAndSave(SQLiteDatabase db3, SQLiteDatabase db4, String paramValue) {
        String groupId = extractGroupId(paramValue);
        String serverId = extractServerId(paramValue);
        String checkedUser = extractCheckedUser(paramValue);

        if (serverId == null || groupId == null || checkedUser == null) {
            XposedBridge.log("Missing parameters: serverId=" + serverId + ", groupId=" + groupId + ", checkedUser=" + checkedUser);
            return; // 必要なパラメータが不足している場合、処理を中断
        }

        // データベースから情報を取得
        String content = queryDatabase(db3, "SELECT content FROM chat_history WHERE server_id=?", serverId);
        String groupName = queryDatabase(db3, "SELECT name FROM groups WHERE id=?", groupId);
        String talkName = queryDatabase(db4, "SELECT profile_name FROM contacts WHERE mid=?", checkedUser);

        // 取得した情報をログに出力または適切に保存
        if (content != null) {
            XposedBridge.log("Content: " + content);
        } else {
            XposedBridge.log("No content found for serverId: " + serverId);
        }

        if (groupName != null) {
            XposedBridge.log("Group Name: " + groupName);
        } else {
            XposedBridge.log("No group found for groupId: " + groupId);
        }

        if (talkName != null) {
            XposedBridge.log("Talk Name: " + talkName);
        } else {
            XposedBridge.log("No talk name found for checkedUser: " + checkedUser);
        }

        // データベースに保存
        saveData(groupId, serverId, checkedUser);
    }

    private String extractGroupId(String paramValue) {
        Pattern pattern = Pattern.compile("to:([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(paramValue);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractServerId(String paramValue) {
        Pattern pattern = Pattern.compile("id:([0-9]+)");
        Matcher matcher = pattern.matcher(paramValue);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractCheckedUser(String paramValue) {
        Pattern pattern = Pattern.compile("param2:([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(paramValue);
        return matcher.find() ? matcher.group(1) : null;
    }


    private String queryDatabase(SQLiteDatabase db, String query, String... selectionArgs) {
        if (db == null) {
            XposedBridge.log("Database is not initialized.");
            return null;
        }
        Cursor cursor = db.rawQuery(query, selectionArgs);
        String result = null;
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }


    private void initializeLimeDatabase(Context context) {
        File dbFile = new File(context.getFilesDir(), "lime_data.db");
        limeDatabase = SQLiteDatabase.openOrCreateDatabase(dbFile, null);

        String createGroupTable = "CREATE TABLE IF NOT EXISTS group_messages (" +
                "group_id TEXT NOT NULL," +
                "server_id TEXT NOT NULL," +
                "checked_user TEXT," +
                "PRIMARY KEY (group_id, server_id, checked_user)" +
                ");";

        limeDatabase.execSQL(createGroupTable);
        XposedBridge.log("Database initialized and group_messages table created.");
    }


    private void saveData(String groupId, String serverId, String checkedUser) {
        if (limeDatabase == null) {
            XposedBridge.log("Database is not initialized.");
            return;
        }

        String insertOrUpdateQuery = "INSERT OR REPLACE INTO group_messages (group_id, server_id, checked_user) " +
                "VALUES (?, ?, ?);";

        limeDatabase.execSQL(insertOrUpdateQuery, new Object[]{groupId, serverId, checkedUser});

        XposedBridge.log("Saved to DB: Group_Id: " + groupId + ", Server_id: " + serverId + ", Checked_user: " + checkedUser);
    }
}
