package io.github.hiro.lime.hooks;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;


public class ReadChecker implements IHook {
    private SQLiteDatabase limeDatabase;
    private SQLiteDatabase db3 = null; 
    private SQLiteDatabase db4 = null;
    private boolean shouldHookOnCreate = false;
    private String currentGroupId = null;


    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.ReadChecker.checked) return;
        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application appContext = (Application) param.thisObject;


                if (appContext == null) {
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


                    db3 = SQLiteDatabase.openDatabase(dbFile3, dbParams1);
                    db4 = SQLiteDatabase.openDatabase(dbFile4, dbParams2);


                    initializeLimeDatabase(appContext);
                    Catcha(loadPackageParam, db3, db4); 
                }
            }
        });


        Class<?> chatHistoryRequestClass = XposedHelpers.findClass("com.linecorp.line.chat.request.ChatHistoryRequest", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(chatHistoryRequestClass, "getChatId", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String chatId = (String) param.getResult();
                //XposedBridge.log(chatId);
                if (isGroupExists(chatId)) {
                    shouldHookOnCreate = true;
                    currentGroupId = chatId;
                } else {
                    shouldHookOnCreate = false;
                    currentGroupId = null;
                }
            }
        });


        Class<?> chatHistoryActivityClass = XposedHelpers.findClass("jp.naver.line.android.activity.chathistory.ChatHistoryActivity", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(chatHistoryActivityClass, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (shouldHookOnCreate && currentGroupId != null) {
                
                    if (!isNoGroup(currentGroupId)) {
                        Activity activity = (Activity) param.thisObject;
                        addButton(activity);
                    }
                }
            }
        });


    }

    private boolean isGroupExists(String groupId) {
        if (limeDatabase == null) {
            XposedBridge.log("Database is not initialized.");
            return false;
        }


        String query = "SELECT 1 FROM group_messages WHERE group_id = ?";
        Cursor cursor = limeDatabase.rawQuery(query, new String[]{groupId});
        boolean exists = cursor.moveToFirst();
        cursor.close();


        return exists;
    }

    private boolean isNoGroup(String groupId) {
        if (limeDatabase == null) {
            XposedBridge.log("Database is not initialized.");
            return true;
        }


        String query = "SELECT group_name FROM group_messages WHERE group_id = ?";
        Cursor cursor = limeDatabase.rawQuery(query, new String[]{groupId});

        boolean noGroup = true;

        if (cursor.moveToFirst()) {
            String groupName = cursor.getString(cursor.getColumnIndex("group_name"));
            noGroup = groupName == null || groupName.isEmpty();
        }

        cursor.close();
        return noGroup;
    }

    private void addButton(Activity activity) {
        Button button = new Button(activity);
        button.setText("既読データ表示");


        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        frameParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        frameParams.topMargin = 150;
        button.setLayoutParams(frameParams);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentGroupId != null) {
                    showDataForGroupId(activity, currentGroupId);
                }
            }
        });


        ViewGroup layout = activity.findViewById(android.R.id.content);
        layout.addView(button);
    }

    private void showDataForGroupId(Activity activity, String groupId) {
        if (limeDatabase == null) {
            return;
        }

        String query = "SELECT server_id, content, created_time FROM group_messages WHERE group_id=? ORDER BY created_time ASC";
        Cursor cursor = limeDatabase.rawQuery(query, new String[]{groupId});

        Map<String, DataItem> dataItemMap = new HashMap<>();

        while (cursor.moveToNext()) {
            String serverId = cursor.getString(0);
            String content = cursor.getString(1);
            String createdTime = cursor.getString(2);

            List<String> talkNameList = getTalkNamesForServerId(serverId);

            if (dataItemMap.containsKey(serverId)) {
                DataItem existingItem = dataItemMap.get(serverId);
                existingItem.talkNames.addAll(talkNameList);
            } else {
                DataItem dataItem = new DataItem(serverId, content, createdTime);
                dataItem.talkNames.addAll(talkNameList);
                dataItemMap.put(serverId, dataItem);
            }
        }
        cursor.close();

        List<DataItem> sortedDataItems = new ArrayList<>(dataItemMap.values());
        Collections.sort(sortedDataItems, Comparator.comparing(item -> item.createdTime));

        StringBuilder resultBuilder = new StringBuilder();
        for (DataItem item : sortedDataItems) {
            resultBuilder.append("Content: ").append(item.content != null ? item.content : "Media").append("\n");
            resultBuilder.append("Created Time: ").append(item.createdTime).append("\n");

            if (!item.talkNames.isEmpty()) {
                resultBuilder.append("既読者 (").append(item.talkNames.size()).append("):\n");
                for (String talkName : item.talkNames) {
                    resultBuilder.append("- ").append(talkName).append("\n");
                }
            } else {
                resultBuilder.append("No talk names found.\n");
            }
            resultBuilder.append("\n");
        }

        TextView textView = new TextView(activity);
        textView.setText(resultBuilder.toString());
        textView.setPadding(20, 20, 20, 20);

        ScrollView scrollView = new ScrollView(activity);
        scrollView.addView(textView);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("READ Data");
        builder.setView(scrollView);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private static class DataItem {
        String serverId;
        String content;
        String createdTime;
        Set<String> talkNames;

        DataItem(String serverId, String content, String createdTime) {
            this.serverId = serverId;
            this.content = content;
            this.createdTime = createdTime;
            this.talkNames = new HashSet<>();
        }
    }


    private List<String> getTalkNamesForServerId(String serverId) {
        List<String> talkNames = new ArrayList<>();
        if (limeDatabase == null) {
            return talkNames;
        }
        String query = "SELECT DISTINCT talk_name FROM group_messages WHERE server_id=?";
        Cursor cursor = limeDatabase.rawQuery(query, new String[]{serverId});

        while (cursor.moveToNext()) {
            String talkName = cursor.getString(0);
            if (talkName != null) {
                talkNames.add(talkName);
            }
        }
        cursor.close();
        return talkNames;
    }


    private void Catcha(XC_LoadPackage.LoadPackageParam loadPackageParam, SQLiteDatabase db3, SQLiteDatabase db4) {
        try {
            XposedBridge.hookAllMethods(
                    loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                    Constants.RESPONSE_HOOK.methodName,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String paramValue = param.args[1].toString();
                           // XposedBridge.log(paramValue);


                            if (paramValue != null && paramValue.contains("type:NOTIFIED_READ_MESSAGE")) {
                                fetchDataAndSave(db3, db4, paramValue); // db3とdb4を渡す
                            } else {
                                Log.e("ReadChecker", "paramValue is null or does not contain 'type:NOTIFIED_READ_MESSAGE'");
                            }
                        }
                    }
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private void fetchDataAndSave(SQLiteDatabase db3, SQLiteDatabase db4, String paramValue) {

        String serverId = extractServerId(paramValue);
        String checkedUser = extractCheckedUser(paramValue);


        if (serverId == null ||  checkedUser == null) {
            // XposedBridge.log("Missing parameters: serverId=" + serverId + ", groupId=" + groupId + ", checkedUser=" + checkedUser);
            return;
        }
        String groupId = queryDatabase(db3, "SELECT chat_id FROM chat_history WHERE server_id=?", serverId);
        String groupName = queryDatabase(db3, "SELECT name FROM groups WHERE id=?", groupId);
        if (groupName == null) {
            return;
        }

        String content = queryDatabase(db3, "SELECT content FROM chat_history WHERE server_id=?", serverId);

        String talkName = queryDatabase(db4, "SELECT profile_name FROM contacts WHERE mid=?", checkedUser);
        String timeEpochStr = queryDatabase(db3, "SELECT created_time FROM chat_history WHERE server_id=?", serverId);
        String timeFormatted = formatMessageTime(timeEpochStr);


        saveData(groupId, serverId, checkedUser, groupName, content, talkName, timeFormatted);
    }


    private String formatMessageTime(String timeEpochStr) {
        if (timeEpochStr == null) return null;
        long timeEpoch = Long.parseLong(timeEpochStr);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timeEpoch));
    }


    private String extractGroupId(String paramValue) {
        Pattern pattern = Pattern.compile("param1:([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(paramValue);
        return matcher.find() ? matcher.group(1) : null;
    }


    private String extractServerId(String paramValue) {
        Pattern pattern = Pattern.compile("param3:([0-9]+)");
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
                "group_name TEXT," +
                "content TEXT," +
                "talk_name TEXT," +
                "created_time TEXT," +
                "PRIMARY KEY (group_id, server_id, checked_user)" +
                ");";


        limeDatabase.execSQL(createGroupTable);
        XposedBridge.log("Database initialized and group_messages table created.");
    }




    private void saveData(String groupId, String serverId, String checkedUser, String groupName, String content, String talkName, String createdTime) {
        if (limeDatabase == null) {


            return;
        }




        String checkQuery = "SELECT COUNT(*) FROM group_messages WHERE server_id=? AND checked_user=?";
        Cursor cursor = limeDatabase.rawQuery(checkQuery, new String[]{serverId, checkedUser});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();



        if (count > 0) {
            //XposedBridge.log("Data already exists for Server_Id: " + serverId + ", Checked_user: " + checkedUser + ". Skipping save.");
            return;
        }


        String insertOrUpdateQuery = "INSERT INTO group_messages (group_id, server_id, checked_user, group_name, content, talk_name, created_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);"; // created_timeを追加


        limeDatabase.execSQL(insertOrUpdateQuery, new Object[]{groupId, serverId, checkedUser, groupName, content, talkName, createdTime}); // created_timeも追加


      //  XposedBridge.log("Saved to DB: Group_Id: " + groupId + ", Server_id: " + serverId + ", Checked_user: " + checkedUser +
        //        ", Group_Name: " + groupName + ", Content: " + content + ", Talk_Name: " + talkName + ", Created_Time: " + createdTime);
    }


}

