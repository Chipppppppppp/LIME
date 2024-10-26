package io.github.chipppppppppp.lime.hooks;

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

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import io.github.chipppppppppp.lime.LimeOptions;
import io.github.chipppppppppp.lime.R;

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

public class UnsentCap implements IHook {

    public static final String Main_file = "unsent_capture.txt";
    public static final String Main_backup = "capture_backup.txt";
    public static final String Unresolved_Ids = "Unresolved_Ids.txt";
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
                        Context moduleContext = AndroidAppHelper.currentApplication().createPackageContext(
                                "io.github.chipppppppppp.lime", Context.CONTEXT_IGNORE_SECURITY);

                        RelativeLayout container = new RelativeLayout(appContext);
                        container.setLayoutParams(new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

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
                                } catch (IOException ignored) {
                                    Toast.makeText(appContext, moduleContext.getResources().getString(R.string.file_creation_failed), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            if (backupFile.length() > 0) {
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(backupFile)))) {
                                    StringBuilder output = new StringBuilder();
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        output.append(line).append("\n");
                                    }

                                    HorizontalScrollView horizontalScrollView = new HorizontalScrollView(appContext);
                                    ScrollView verticalScrollView = new ScrollView(appContext);
                                    TextView textView = new TextView(appContext);
                                    textView.setText(output.toString());
                                    textView.setMaxLines(Integer.MAX_VALUE);
                                    textView.setHorizontallyScrolling(true);
                                    textView.setHorizontalScrollBarEnabled(true);

                                    horizontalScrollView.addView(textView);
                                    verticalScrollView.addView(horizontalScrollView);

                                    new AlertDialog.Builder(appContext)
                                            .setTitle(moduleContext.getResources().getString(R.string.backup))
                                            .setView(verticalScrollView)
                                            .setPositiveButton(moduleContext.getResources().getString(R.string.positive_button), null)
                                            .create()
                                            .show();
                                } catch (IOException ignored) {
                                    Toast.makeText(appContext, moduleContext.getResources().getString(R.string.failed_read_backup_file, Main_backup), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(appContext, moduleContext.getResources().getString(R.string.no_backup_found), Toast.LENGTH_SHORT).show();
                            }
                        });

                        clearFileButton.setOnClickListener(v -> new AlertDialog.Builder(appContext)
                                .setTitle(moduleContext.getResources().getString(R.string.confirm))
                                .setMessage(moduleContext.getResources().getString(R.string.confirm_delete))
                                .setPositiveButton(moduleContext.getResources().getString(R.string.yes), (dialog, which) -> {
                                    File backupFile = new File(appContext.getFilesDir(), Main_backup);
                                    if (backupFile.exists()) {
                                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile))) {
                                            writer.write("");
                                            Toast.makeText(appContext, moduleContext.getResources().getString(R.string.file_content_deleted), Toast.LENGTH_SHORT).show();
                                        } catch (IOException ignored) {
                                            Toast.makeText(appContext, moduleContext.getResources().getString(R.string.file_delete_failed), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(appContext, moduleContext.getResources().getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton(moduleContext.getResources().getString(R.string.no), null)
                                .create()
                                .show());

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
                                "io.github.chipppppppppp.lime", Context.CONTEXT_IGNORE_SECURITY);

                        View rootView = (View) param.getResult();
                        Context context = rootView.getContext();

                        File originalFile = new File(context.getFilesDir(), Main_file);
                        if (!originalFile.exists()) {
                            try {
                                originalFile.createNewFile();
                            } catch (IOException ignored) {
                                Toast.makeText(context, moduleContext.getResources().getString(R.string.file_creation_failed), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        if (originalFile.length() > 0) {
                            int lineCount = countLinesInFile(originalFile);
                            if (lineCount > 0) {
                                Button button = new Button(context);
                                button.setText(Integer.toString(lineCount));
                                button.setId(View.generateViewId());
                                button.setLayoutParams(new RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                                button.setOnClickListener(v -> {
                                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(originalFile)))) {
                                        StringBuilder output = new StringBuilder();
                                        boolean showToast = false;
                                        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                                        boolean messageShown = prefs.getBoolean("messageShown", false);

                                        String line;
                                        while ((line = reader.readLine()) != null) {
                                            if (line.contains("No content") || line.contains("No name")) {
                                                if (!messageShown) {
                                                    showToast = true;
                                                    prefs.edit().putBoolean("messageShown", true).apply();
                                                }
                                                continue;
                                            }
                                            output.append(line).append("\n");
                                        }

                                        if (showToast) {
                                            Toast.makeText(context, moduleContext.getResources().getString(R.string.no_get_restart_app), Toast.LENGTH_SHORT).show();
                                        }

                                        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(context);
                                        ScrollView verticalScrollView = new ScrollView(context);
                                        TextView textView = new TextView(context);
                                        textView.setText(output.toString());
                                        textView.setMaxLines(Integer.MAX_VALUE);
                                        textView.setHorizontallyScrolling(true);
                                        textView.setHorizontalScrollBarEnabled(true);

                                        horizontalScrollView.addView(textView);
                                        verticalScrollView.addView(horizontalScrollView);

                                        new AlertDialog.Builder(context)
                                                .setTitle(moduleContext.getResources().getString(R.string.deleted_messages))
                                                .setView(verticalScrollView)
                                                .setPositiveButton(moduleContext.getResources().getString(R.string.positive_button), (dialog, which) -> {
                                                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(context.getFilesDir(), Main_backup), true))) {
                                                        writer.write(output.toString());
                                                        new BufferedWriter(new FileWriter(originalFile)).close();
                                                        Toast.makeText(context, moduleContext.getResources().getString(R.string.content_moved_to_backup), Toast.LENGTH_SHORT).show();
                                                        prefs.edit().putBoolean("messageShown", false).apply();
                                                    } catch (IOException ignored) {
                                                        Toast.makeText(context, moduleContext.getResources().getString(R.string.file_move_failed), Toast.LENGTH_SHORT).show();
                                                    }

                                                    if (button.getParent() instanceof ViewGroup) {
                                                        ((ViewGroup) button.getParent()).removeView(button);
                                                    }
                                                })
                                                .create()
                                                .show();

                                    } catch (IOException ignored) {
                                        Toast.makeText(context, moduleContext.getResources().getString(R.string.failed_read_backup_file, Main_backup), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                if (rootView instanceof ViewGroup) {
                                    ((ViewGroup) rootView).addView(button);
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
                            "io.github.chipppppppppp.lime", Context.CONTEXT_IGNORE_SECURITY);
                } catch (PackageManager.NameNotFoundException e) {
                    XposedBridge.log("Failed to create package context: " + e.getMessage());
                    return;
                }

                File dbFile1 = appContext.getDatabasePath("naver_line");
                File dbFile2 = appContext.getDatabasePath("contact");
                if (!dbFile1.exists() || !dbFile2.exists()) return;

                SQLiteDatabase.OpenParams.Builder builder1 = new SQLiteDatabase.OpenParams.Builder().addOpenFlags(SQLiteDatabase.OPEN_READWRITE);
                SQLiteDatabase db1 = SQLiteDatabase.openDatabase(dbFile1, builder1.build());
                SQLiteDatabase.OpenParams.Builder builder2 = new SQLiteDatabase.OpenParams.Builder().addOpenFlags(SQLiteDatabase.OPEN_READWRITE);
                SQLiteDatabase db2 = SQLiteDatabase.openDatabase(dbFile2, builder2.build());

                hookMessageDeletion(loadPackageParam, appContext, db1, db2);
                resolveUnresolvedIds(loadPackageParam, appContext, db1, db2, moduleContext);
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
        if (cursor.moveToFirst()) result = cursor.getString(0);
        cursor.close();
        return result;
    }

    private int countLinesInFile(File file) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!(line.contains("No content") || line.contains("No name"))) count++;
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
                            Context appContext = AndroidAppHelper.currentApplication();
                            if (appContext == null) return;

                            File dbFile = appContext.getDatabasePath("naver_line");
                            if (!dbFile.exists()) return;

                            SQLiteDatabase.OpenParams dbParams = new SQLiteDatabase.OpenParams.Builder()
                                    .addOpenFlags(SQLiteDatabase.OPEN_READWRITE)
                                    .build();
                            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile, dbParams);
                            String paramValue = param.args[1].toString();
                            if (!paramValue.contains("type:NOTIFIED_DESTROY_MESSAGE,")) {

                                if (db != null) db.close(); // 条件を満たさない場合はデータベースをクローズ
                                return;
                            }
                            Context moduleContext = appContext.createPackageContext(
                                    "io.github.chipppppppppp.lime", Context.CONTEXT_IGNORE_SECURITY);
                            processMessage(paramValue, moduleContext, db1, db2, context);

                            if (db != null) db.close();
                        }
                    });
        } catch (ClassNotFoundException ignored ) {
        }
    }

    private void processMessage(String paramValue, Context moduleContext, SQLiteDatabase db1, SQLiteDatabase db2, Context context) {
        String unresolvedFilePath = context.getFilesDir() + "/" + Unresolved_Ids;
        String[] operations = paramValue.split("Operation\\(");
        for (String operation : operations) {
            if (operation.trim().isEmpty()) continue;

            String revision = null, createdTime = null, type = null, from = null, to = null, param12 = null, param22 = null, operationContent = null, serverId = null, talkId = null;
            String[] parts = operation.split(",");
            for (String part : parts) {
                part = part.trim();
                if (part.startsWith("param1:")) talkId = part.substring("param1:".length()).trim();
                else if (part.startsWith("param2:")) serverId = part.substring("param2:".length()).trim();
                else if (part.startsWith("revision:")) revision = part.substring("revision:".length()).trim();
                else if (part.startsWith("createdTime:")) createdTime = part.substring("createdTime:".length()).trim();
                else if (part.startsWith("type:")) type = part.substring("type:".length()).trim();
                else if (part.startsWith("from:")) from = part.substring("from:".length()).trim();
                else if (part.startsWith("to:")) to = part.substring("to:".length()).trim();
                else if (part.startsWith("contentMetadata:")) param12 = part.substring("contentMetadata:".length()).trim();
                else if (part.startsWith("operationContent:")) operationContent = part.substring("operationContent:".length()).trim();
            }

            if (serverId == null || talkId == null) continue;

            String content = queryDatabase(db1, "SELECT content FROM chat_history WHERE server_id=?", serverId);
            String timeEpochStr = queryDatabase(db1, "SELECT created_time FROM chat_history WHERE server_id=?", serverId);
            String timeFormatted = formatMessageTime(timeEpochStr);
            String groupName = queryDatabase(db1, "SELECT name FROM groups WHERE id=?", talkId);
            String media = queryDatabase(db1, "SELECT attachement_type FROM chat_history WHERE server_id=?", serverId);
            String talkName = queryDatabase(db2, "SELECT profile_name FROM contacts WHERE mid=?", talkId);

            String name = (groupName != null ? groupName : (talkName != null ? talkName : "No Name" + ":" + "talkId" + talkId));
            if (timeEpochStr == null) saveUnresolvedIds(serverId, talkId, unresolvedFilePath);

            String from_mid = null, sender_name = null;
            if (groupName != null) {
                from_mid = queryDatabase(db1, "SELECT from_mid FROM chat_history WHERE server_id=?", serverId);
                if (from_mid != null) sender_name = queryDatabase(db2, "SELECT profile_name FROM contacts WHERE mid=?", from_mid);
            }

            if (sender_name != null) name = groupName + ": " + sender_name;

            String mediaDescription = "";
            if (media != null) {
                switch (media) {
                    case "7": mediaDescription = moduleContext.getResources().getString(R.string.sticker); break;
                    case "1": mediaDescription = moduleContext.getResources().getString(R.string.picture); break;
                    case "2": mediaDescription = moduleContext.getResources().getString(R.string.video); break;
                }
            }

            String logEntry = (timeFormatted != null ? timeFormatted : "No Time: ")
                    + name
                    + ": " + ((content != null) ? content : "No content:" + serverId)
                    + mediaDescription;

            File fileToWrite = new File(context.getFilesDir(), Main_file);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToWrite, true))) {
                writer.write(logEntry);
                writer.newLine();
            } catch (IOException e) {
                XposedBridge.log("IOException occurred while writing to file: " + e.getMessage());
            }
        }
    }

    private void saveUnresolvedIds(String serverId, String talkId, String filePath) {
        String newEntry = "serverId:" + serverId + ",talkId:" + talkId;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(newEntry)) return;
            }
        } catch (IOException ignored) {}

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(newEntry);
            writer.newLine();
        } catch (IOException ignored) {}
    }

    private void resolveUnresolvedIds(XC_LoadPackage.LoadPackageParam loadPackageParam, Context context, SQLiteDatabase db1, SQLiteDatabase db2, Context moduleContext) {
        String unresolvedFilePath = context.getFilesDir() + "/" + Unresolved_Ids;
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
                if (content == null) continue;

                String timeEpochStr = queryDatabase(db1, "SELECT created_time FROM chat_history WHERE server_id=?", serverId);
                String timeFormatted = formatMessageTime(timeEpochStr);
                String groupName = queryDatabase(db1, "SELECT name FROM groups WHERE id=?", talkId);
                String talkName = queryDatabase(db2, "SELECT profile_name FROM contacts WHERE mid=?", talkId);
                String name = (groupName != null ? groupName : (talkName != null ? talkName : "No Name" + ":" + "talkId" + talkId));

                String media = queryDatabase(db1, "SELECT attachement_type FROM chat_history WHERE server_id=?", serverId);
                String mediaDescription = "";
                if (media != null) {
                    switch (media) {
                        case "7": mediaDescription = moduleContext.getResources().getString(R.string.sticker); break;
                        case "1": mediaDescription = moduleContext.getResources().getString(R.string.picture); break;
                        case "2": mediaDescription = moduleContext.getResources().getString(R.string.video); break;
                    }
                }

                String from_mid = null, sender_name = null;
                if (groupName != null) {
                    from_mid = queryDatabase(db1, "SELECT from_mid FROM chat_history WHERE server_id=?", serverId);
                    if (from_mid != null) sender_name = queryDatabase(db2, "SELECT profile_name FROM contacts WHERE mid=?", from_mid);
                }

                if (sender_name != null) name = groupName + ": " + sender_name;

                testWriter.write((timeFormatted != null ? timeFormatted : "No Time: ")
                        + name + ": " + content + mediaDescription);
                testWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        unresolvedFile.delete();
    }
    private String formatMessageTime(String timeEpochStr) {
        if (timeEpochStr == null) return null;
        long timeEpoch = Long.parseLong(timeEpochStr);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timeEpoch));
    }
}
