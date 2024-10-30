package io.github.hiro.lime.hooks;

import android.app.AlertDialog;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;
import io.github.hiro.lime.R;

public class UnsentRec implements IHook {


    public static final String Main_file = "UNSENT_REC.txt";
    public static final String Main_backup = "BackUpFile.txt";

    SQLiteDatabase db1 = null;
    SQLiteDatabase db2 = null;

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!limeOptions.preventUnsendMessage.checked) return;
        XposedBridge.hookAllConstructors(
                loadPackageParam.classLoader.loadClass("jp.naver.line.android.common.view.listview.PopupListView"),
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        ViewGroup viewGroup = (ViewGroup) param.thisObject;
                        Context appContext = viewGroup.getContext();
                        Context context = viewGroup.getContext();


                        Context moduleContext = AndroidAppHelper.currentApplication().createPackageContext(
                                "io.github.hiro.lime", Context.CONTEXT_IGNORE_SECURITY);

                        RelativeLayout container = new RelativeLayout(appContext);
                        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        container.setLayoutParams(containerParams);



                        Button openFileButton = new Button(appContext);
                        openFileButton.setText(moduleContext.getResources().getString(R.string.confirm_messages));


                        openFileButton.setTextSize(12);
                        openFileButton.setTextColor(Color.BLACK);
                        openFileButton.setId(View.generateViewId());

                        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        container.addView(openFileButton, buttonParams);

                        Button clearFileButton = new Button(appContext);
                        clearFileButton.setText(moduleContext.getResources().getString(R.string.delete_messages));
                        clearFileButton.setTextSize(12);
                        clearFileButton.setTextColor(Color.RED);
                        clearFileButton.setId(View.generateViewId());

                        RelativeLayout.LayoutParams clearButtonParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        clearButtonParams.addRule(RelativeLayout.BELOW, openFileButton.getId());
                        clearButtonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        container.addView(clearFileButton, clearButtonParams);


                        openFileButton.setOnClickListener(v -> {
                            File backupFile = new File(appContext.getFilesDir(), Main_backup);
                            if (!backupFile.exists()) {
                                try {
                                    backupFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(appContext, moduleContext.getResources().getString(R.string.file_creation_failed), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            if (backupFile.length() > 0) {
                                try {
                                    StringBuilder output = new StringBuilder();
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(backupFile)));
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        output.append(line).append("\n");
                                    }                                  // カスタムViewを作成
                                    HorizontalScrollView horizontalScrollView = new HorizontalScrollView(context); // 横スクロール用のScrollView
                                    ScrollView verticalScrollView = new ScrollView(context); // 縦スクロール用のScrollView

                                    TextView textView = new TextView(context);
                                    textView.setText(output.toString());

                                    // TextViewの設定（改行を防ぎ、横スクロールをサポート）
                                    textView.setMaxLines(Integer.MAX_VALUE); // 最大行数を設定（実質無制限）
                                    textView.setHorizontallyScrolling(true); // 横スクロールを有効にする
                                    textView.setHorizontalScrollBarEnabled(true); // 横スクロールバーを表示する

                                    // スクロールビューにTextViewを追加
                                    horizontalScrollView.addView(textView); // 横スクロールをサポートするScrollViewにTextViewを追加
                                    verticalScrollView.addView(horizontalScrollView); // 縦スクロールをサポートするScrollViewに横ScrollViewを追加
                                    reader.close();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
                                    builder.setTitle(moduleContext.getResources().getString(R.string.backup))
                                            .setMessage(output.toString())
                                            .setPositiveButton("OK", null)
                                            .create()
                                            .show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(appContext, moduleContext.getResources().getString(R.string.read_BackUpFile_failed), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(appContext, moduleContext.getResources().getString(R.string.no_backup_found), Toast.LENGTH_SHORT).show();
                            }
                        });

                        clearFileButton.setOnClickListener(v -> {
                            new AlertDialog.Builder(appContext)
                                    .setTitle(moduleContext.getResources().getString(R.string.check))
                                    .setMessage(moduleContext.getResources().getString(R.string.really_delete))
                                    .setPositiveButton(moduleContext.getResources().getString(R.string.yes), (dialog, which) -> {
                                        File backupFile = new File(appContext.getFilesDir(), Main_backup);
                                        if (backupFile.exists()) {
                                            try {
                                                BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile));
                                                writer.write("");
                                                writer.close();
                                                Toast.makeText(appContext, moduleContext.getResources().getString(R.string.file_content_deleted), Toast.LENGTH_SHORT).show();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                Toast.makeText(appContext, moduleContext.getResources().getString(R.string.file_delete_failed), Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(appContext, moduleContext.getResources().getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .setNegativeButton(moduleContext.getResources().getString(R.string.no), null)
                                    .create()
                                    .show();
                        });


                        ((ListView) viewGroup.getChildAt(0)).addFooterView(container);
                    }
                }
        );



        XposedHelpers.findAndHookMethod(


                "com.linecorp.line.chatlist.view.fragment.ChatListPageFragment",
                loadPackageParam.classLoader, "onCreateView",
                LayoutInflater.class, ViewGroup.class, android.os.Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Context moduleContext = AndroidAppHelper.currentApplication().createPackageContext(
                                "io.github.hiro.lime", Context.CONTEXT_IGNORE_SECURITY);

                        View rootView = (View) param.getResult();
                        Context context = rootView.getContext();


                        File originalFile = new File(context.getFilesDir(), Main_file);
                        if (!originalFile.exists()) {
                            try {
                                originalFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(context,  moduleContext.getResources().getString(R.string.file_creation_failed), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        if (originalFile.length() > 0) {
                            int lineCount = countLinesInFile(originalFile);

                            if (lineCount > 0) {
                                Button button = new Button(context);
                                button.setText(Integer.toString(lineCount));
                                int buttonId = View.generateViewId();
                                button.setId(buttonId);  // IDを設定
                                RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                button.setLayoutParams(buttonParams);

                                // SharedPreferencesでフラグの状態を取得
                                SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                                boolean messageShown = prefs.getBoolean("messageShown", false); // デフォルトはfalse

                                button.setOnClickListener(v -> {
                                    try {
                                        StringBuilder output = new StringBuilder();
                                        StringBuilder updatedContent = new StringBuilder();
                                        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(originalFile)));
                                        String line;
                                        boolean showToast = false;  // トーストを表示するかどうか

                                        while ((line = reader.readLine()) != null) {
                                            if (line.contains("No content") || line.contains("No name")) {
                                                // メッセージが表示されていない場合にだけ表示
                                                if (!messageShown) {
                                                    showToast = true;
                                                    // フラグをSharedPreferencesに保存
                                                    SharedPreferences.Editor editor = prefs.edit();
                                                    editor.putBoolean("messageShown", true);
                                                    editor.apply();
                                                }
                                                // 取得できなかったメッセージは表示せず、削除のため更新しない
                                                continue;
                                            }
                                            output.append(line).append("\n");
                                            updatedContent.append(line).append("\n"); // 有効な行は保持
                                        }
                                        reader.close();

                                        // トーストメッセージの表示
                                        if (showToast) {
                                            Toast.makeText(context, moduleContext.getResources().getString(R.string.no_get_restart_app), Toast.LENGTH_SHORT).show();
                                        }



                                        // カスタムViewを作成
                                        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(context); // 横スクロール用のScrollView
                                        ScrollView verticalScrollView = new ScrollView(context); // 縦スクロール用のScrollView

                                        TextView textView = new TextView(context);
                                        textView.setText(output.toString());

// TextViewの設定（改行を防ぎ、横スクロールをサポート）
                                        textView.setMaxLines(Integer.MAX_VALUE); // 最大行数を設定（実質無制限）
                                        textView.setHorizontallyScrolling(true); // 横スクロールを有効にする
                                        textView.setHorizontalScrollBarEnabled(true); // 横スクロールバーを表示する

// スクロールビューにTextViewを追加
                                        horizontalScrollView.addView(textView); // 横スクロールをサポートするScrollViewにTextViewを追加
                                        verticalScrollView.addView(horizontalScrollView); // 縦スクロールをサポートするScrollViewに横ScrollViewを追加

// AlertDialogを作成
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle( moduleContext.getResources().getString(R.string.deleted_messages))
                                                .setView(verticalScrollView)  // カスタムビューをセット
                                                .setPositiveButton("ok", (dialog, which) -> {
                                                    try {
                                                        File backupFile = new File(context.getFilesDir(), Main_backup);
                                                        BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile, true));
                                                        writer.write(output.toString());
                                                        writer.close();

                                                        // originalFileの内容をクリアしてバックアップ
                                                        BufferedWriter clearWriter = new BufferedWriter(new FileWriter(originalFile));
                                                        clearWriter.close();

                                                        Toast.makeText(context,
                                                                moduleContext.getResources().getString(R.string.content_moved_to_backup),Toast.LENGTH_SHORT).show();

                                                        // フラグをリセット
                                                        SharedPreferences.Editor editor = prefs.edit();
                                                        editor.putBoolean("messageShown", false);
                                                        editor.apply();

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                        Toast.makeText(context,  moduleContext.getResources().getString(R.string.file_move_failed), Toast.LENGTH_SHORT).show();
                                                    }

                                                    // ボタンの親からボタンを削除
                                                    if (button.getParent() instanceof ViewGroup) {
                                                        ViewGroup parent = (ViewGroup) button.getParent();
                                                        parent.removeView(button);
                                                    }
                                                })
                                                .create()
                                                .show();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Toast.makeText(context,  moduleContext.getResources().getString(R.string.read_BackUpFile_failed), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                if (rootView instanceof ViewGroup) {
                                    ViewGroup viewGroup = (ViewGroup) rootView;
                                    viewGroup.addView(button);

                                    View existingButton = viewGroup.findViewById(buttonId);
                                    if (existingButton != null) {

                                    }
                                }
                            }
                        }
                    }
                }
        );



        XposedBridge.hookAllMethods(Application.class, "onCreate", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application appContext = (Application) param.thisObject;


                if (appContext == null) {
                    XposedBridge.log("appContext is null!");
                    return;
                }


                Context moduleContext;
                try {
                    moduleContext = appContext.createPackageContext(
                            "io.github.hiro.lime", Context.CONTEXT_IGNORE_SECURITY);
                } catch (PackageManager.NameNotFoundException e) {
                    XposedBridge.log("Failed to create package context: " + e.getMessage());
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


                    hookMessageDeletion(loadPackageParam, appContext,db1, db2);
                    resolveUnresolvedIds(loadPackageParam, appContext,db1, db2,moduleContext);
                }
            }
        });

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

    private int countLinesInFile(File file) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!(line.contains("No content") || line.contains("No name"))) {
                    count++; // 条件に合致しない行だけをカウント
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }
    private void hookMessageDeletion(XC_LoadPackage.LoadPackageParam loadPackageParam, Context context, SQLiteDatabase db1, SQLiteDatabase db2) {
        try {
            XposedBridge.hookAllMethods(
            loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                    Constants.RESPONSE_HOOK.methodName,

                    new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String paramValue = param.args[1].toString();
                            if (paramValue.contains("type:NOTIFIED_DESTROY_MESSAGE,")) {


                                Context moduleContext = AndroidAppHelper.currentApplication().createPackageContext(
                                        "io.github.hiro.lime", Context.CONTEXT_IGNORE_SECURITY);



                                processMessage(paramValue, moduleContext, db1, db2,context);
                            }
                        }
                    });
        } catch (ClassNotFoundException e) {
            XposedBridge.log("Class not found: " + e.getMessage());
        }
    }

    private void processMessage(String paramValue, Context moduleContext, SQLiteDatabase db1, SQLiteDatabase db2, Context context) {
        String unresolvedFilePath = context.getFilesDir() + "/UnresolvedIds.txt";

        String[] operations = paramValue.split("Operation\\(");
        for (String operation : operations) {
            if (operation.trim().isEmpty()) continue;
            String revision = null;
            String createdTime = null;
            String type = null;
            String from = null;
            String to = null;
            String param12 = null;
            String param22 = null;
            String operationContent = null;
            String serverId = null;
            String talkId = null;

            String[] parts = operation.split(",");
            for (String part : parts) {
                part = part.trim();
                if (part.startsWith("param1:")) {
                    talkId = part.substring("param1:".length()).trim();
                } else if (part.startsWith("param2:")) {
                    serverId = part.substring("param2:".length()).trim();
                } else if (part.startsWith("revision:")) {
                    revision = part.substring("revision:".length()).trim();
                } else if (part.startsWith("createdTime:")) {
                    createdTime = part.substring("createdTime:".length()).trim();
                } else if (part.startsWith("type:")) {
                    type = part.substring("type:".length()).trim();
                } else if (part.startsWith("from:")) {
                    from = part.substring("from:".length()).trim();
                } else if (part.startsWith("to:")) {
                    to = part.substring("to:".length()).trim();
                } else if (part.startsWith("contentMetadata:")) {
                    param12 = part.substring("contentMetadata:".length()).trim();
                } else if (part.startsWith("operationContent:")) {
                    operationContent = part.substring("operationContent:".length()).trim();
                }
            }

            if (serverId != null && talkId != null) {
                String content = queryDatabase(db1, "SELECT content FROM chat_history WHERE server_id=?", serverId);
                String imageCheck = queryDatabase(db1, "SELECT attachement_image FROM chat_history WHERE server_id=?", serverId);
                String timeEpochStr = queryDatabase(db1, "SELECT created_time FROM chat_history WHERE server_id=?", serverId);
                String timeFormatted = formatMessageTime(timeEpochStr);
                String groupName = queryDatabase(db1, "SELECT name FROM groups WHERE id=?", talkId);
                String media = queryDatabase(db1, "SELECT attachement_type FROM chat_history WHERE server_id=?", serverId);
                String talkName = queryDatabase(db2, "SELECT profile_name FROM contacts WHERE mid=?", talkId);

                String name = (groupName != null ? groupName : (talkName != null ? talkName : "No Name" + ":" + ":" + "talkId" + talkId));

                if (timeEpochStr == null) {
                    saveUnresolvedIds(serverId, talkId, unresolvedFilePath);
                }

                // groupNameが取得できた場合、from_midとsender_nameを取得する
                String from_mid = null;
                String sender_name = null;
                if (groupName != null) {
                    from_mid = queryDatabase(db1, "SELECT from_mid FROM chat_history WHERE server_id=?", serverId);
                    if (from_mid != null) {
                        sender_name = queryDatabase(db2, "SELECT profile_name FROM contacts WHERE mid=?", from_mid);
                    }
                }                // sender_nameが取得できた場合、nameを更新する
                if (sender_name != null) {
                    name = groupName + ": " + sender_name;
                }
                String mediaDescription = "";
                if (media != null) {
                    switch (media) {
                        case "7":
                            mediaDescription = moduleContext.getResources().getString(R.string.sticker);
                            break;
                        case "1":
                            mediaDescription = moduleContext.getResources().getString(R.string.picture);
                            break;
                        case "2":
                            mediaDescription = moduleContext.getResources().getString(R.string.video);
                            break;
                        default:
                            mediaDescription = "";
                            break;
                    }
                }

                String logEntry = (timeFormatted != null ? timeFormatted : "No Time: ")
                        + name
                        + ": "
                        + ((content != null) ? content : (mediaDescription.isEmpty() ? "No content:" + serverId : ""))
                        + mediaDescription;


                File fileToWrite = new File(context.getFilesDir(), Main_file);

                try {

                    if (!fileToWrite.getParentFile().exists()) {
                        if (!fileToWrite.getParentFile().mkdirs()) {
                            XposedBridge.log("Failed to create directory " + fileToWrite.getParent());
                        }
                    }
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToWrite, true))) {
                        writer.write(logEntry);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    XposedBridge.log("IOException occurred while writing to file: " + e.getMessage());
                }
            }

        }
    }
    private void saveUnresolvedIds(String serverId, String talkId, String filePath) {
        String newEntry = "serverId:" + serverId + ",talkId:" + talkId;


        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(newEntry)) {

                    return;
                }
            }
        } catch (IOException e) {

        }

        // 新しいエントリーを書き込む
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(newEntry);
            writer.newLine();
        } catch (IOException e) {

        }
    }



    private void resolveUnresolvedIds(XC_LoadPackage.LoadPackageParam loadPackageParam, Context context, SQLiteDatabase db1, SQLiteDatabase db2,Context moduleContext) {
        String unresolvedFilePath = context.getFilesDir() + "/UnresolvedIds.txt";

        File unresolvedFile = new File(unresolvedFilePath);
        File testFile = new File(context.getFilesDir(), Main_file);

        if (!unresolvedFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(unresolvedFile));
             BufferedWriter testWriter = new BufferedWriter(new FileWriter(testFile, true))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String serverId = parts[0].split(":")[1];
                String talkId = parts[1].split(":")[1];

                String content = queryDatabase(db1, "SELECT content FROM chat_history WHERE server_id=?", serverId);
                String imageCheck = queryDatabase(db1, "SELECT attachement_image FROM chat_history WHERE server_id=?", serverId);
                String timeEpochStr = queryDatabase(db1, "SELECT created_time FROM chat_history WHERE server_id=?", serverId);
                String timeFormatted = formatMessageTime(timeEpochStr);
                String groupName = queryDatabase(db1, "SELECT name FROM groups WHERE id=?", talkId);
                String media = queryDatabase(db1, "SELECT attachement_type FROM chat_history WHERE server_id=?", serverId);

                String talkName = queryDatabase(db2, "SELECT profile_name FROM contacts WHERE mid=?", talkId);

                String name = (groupName != null ? groupName : (talkName != null ? talkName : "No Name" + ":" + ":" + "talkId" + talkId));
                // groupNameが取得できた場合、from_midとsender_nameを取得する
                String from_mid = null;
                String sender_name = null;
                if (groupName != null) {
                    from_mid = queryDatabase(db1, "SELECT from_mid FROM chat_history WHERE server_id=?", serverId);
                    if (from_mid != null) {
                        sender_name = queryDatabase(db2, "SELECT profile_name FROM contacts WHERE mid=?", from_mid);
                    }
                }

                if (sender_name != null) {
                    name = groupName + ": " + sender_name;
                }
                String mediaDescription = "";
                if (media != null) {
                    switch (media) {
                        case "7":
                            mediaDescription = moduleContext.getResources().getString(R.string.sticker);
                            break;
                        case "1":
                            mediaDescription = moduleContext.getResources().getString(R.string.picture);
                            break;
                        case "2":
                            mediaDescription = moduleContext.getResources().getString(R.string.video);
                            break;
                        default:
                            mediaDescription = "";
                            break;
                    }
                }

                String logEntry = (timeFormatted != null ? timeFormatted : "No Time: ")
                        + name
                        + ": " + (content != null ? content : "NO get id:" + serverId)
                        + mediaDescription;
                if (timeEpochStr == null) {
                    saveUnresolvedIds(serverId, talkId, unresolvedFilePath);
                }
                testWriter.write(moduleContext.getResources().getString(R.string.reacquisition) + logEntry);
                testWriter.newLine();


            }



            try (BufferedWriter clearWriter = new BufferedWriter(new FileWriter(unresolvedFile))) {
                clearWriter.write("");
            }

        } catch (IOException e) {
            XposedBridge.log("IOException occurred while resolving and saving unresolved IDs: " + e.getMessage());
        }
    }




    private String formatMessageTime(String timeEpochStr) {
        if (timeEpochStr == null) return null;
        long timeEpoch = Long.parseLong(timeEpochStr);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timeEpoch));
    }



}
