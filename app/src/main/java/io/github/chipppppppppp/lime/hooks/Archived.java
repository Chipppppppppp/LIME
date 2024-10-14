package io.github.chipppppppppp.lime.hooks;

import static io.github.chipppppppppp.lime.Main.limeOptions;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class Archived implements IHook {

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.Archived.checked) return;

        XposedBridge.hookAllMethods(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application appContext = (Application) param.thisObject;
                if (appContext == null) return;

                File dbFile = appContext.getDatabasePath("naver_line");
                if (!dbFile.exists()) return;

                SQLiteDatabase.OpenParams dbParams = new SQLiteDatabase.OpenParams.Builder()
                        .addOpenFlags(SQLiteDatabase.OPEN_READWRITE)
                        .build();
                SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile, dbParams);

                hookSAMethod(loadPackageParam, db, appContext);
                hookMessageDeletion(loadPackageParam, appContext, db, appContext); 
            }
        });
    }

    private void hookMessageDeletion(XC_LoadPackage.LoadPackageParam loadPackageParam, Context context, SQLiteDatabase db, Context moduleContext) {
        if (!limeOptions.Archived.checked) return;

        try {
            XposedBridge.hookAllMethods(
                    loadPackageParam.classLoader.loadClass(Constants.REQUEST_HOOK.className),
                    Constants.REQUEST_HOOK.methodName,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String paramValue = param.args[1].toString();
                            String talkId = extractTalkId(paramValue);
                            if (talkId == null) return;

                            if (paramValue.contains("hidden:true")) {
                                saveTalkIdToFile(talkId, context);
                            } else if (paramValue.contains("hidden:false")) {
                                deleteTalkIdFromFile(talkId, context);
                            }

                            updateArchivedChatsFromFile(db, context, moduleContext);
                        }
                    });
        } catch (ClassNotFoundException ignored) {
        }
    }

    private void deleteTalkIdFromFile(String talkId, Context moduleContext) {
        File file = new File(moduleContext.getFilesDir(), "hidelist.txt");
        if (!file.exists()) return;

        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().equals(talkId)) {
                        lines.add(line);
                    }
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String remainingLine : lines) {
                    writer.write(remainingLine);
                    writer.newLine();
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void hookSAMethod(XC_LoadPackage.LoadPackageParam loadPackageParam, SQLiteDatabase db, Context context) {
        Class<?> targetClass = XposedHelpers.findClass("SA.Q", loadPackageParam.classLoader);

        XposedBridge.hookAllMethods(targetClass, "invokeSuspend", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Context appContext = AndroidAppHelper.currentApplication();
                if (appContext == null) return;

                File dbFile = appContext.getDatabasePath("naver_line");
                if (!dbFile.exists()) return;

                SQLiteDatabase.OpenParams dbParams = new SQLiteDatabase.OpenParams.Builder()
                        .addOpenFlags(SQLiteDatabase.OPEN_READWRITE)
                        .build();
                SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile, dbParams);

                List<String> chatIds = readChatIdsFromFile(appContext, context);
                for (String chatId : chatIds) {
                    if (!chatId.isEmpty()) {
                        updateIsArchived(db, chatId);
                    }
                }

                if (db != null) db.close();
            }
        });
    }

    private List<String> readChatIdsFromFile(Context context, Context moduleContext) {
        List<String> chatIds = new ArrayList<>();
        File file = new File(moduleContext.getFilesDir(), "hidelist.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                chatIds.add(line.trim());
            }
        } catch (IOException ignored) {
        }

        return chatIds;
    }

    private void saveTalkIdToFile(String talkId, Context moduleContext) {
        File file = new File(moduleContext.getFilesDir(), "hidelist.txt");

        try {
            if (!file.exists()) file.createNewFile();

            List<String> existingIds = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    existingIds.add(line.trim());
                }
            }

            if (!existingIds.contains(talkId.trim())) {
                try (FileWriter writer = new FileWriter(file, true)) {
                    writer.write(talkId + "\n");
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void updateArchivedChatsFromFile(SQLiteDatabase db, Context context, Context moduleContext) {
        File file = new File(moduleContext.getFilesDir(), "hidelist.txt");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String chatId;
            while ((chatId = reader.readLine()) != null) {
                chatId = chatId.trim();
                if (!chatId.isEmpty()) {
                    updateIsArchived(db, chatId);
                }
            }
        } catch (IOException ignored) {
        }
    }

    private String extractTalkId(String paramValue) {
        String requestPrefix = "setChatHiddenStatusRequest:SetChatHiddenStatusRequest(reqSeq:0, chatMid:";
        int startIndex = paramValue.indexOf(requestPrefix);
        if (startIndex == -1) return null;

        int chatMidStartIndex = startIndex + requestPrefix.length();
        int endIndex = paramValue.indexOf(",", chatMidStartIndex);
        if (endIndex == -1) endIndex = paramValue.indexOf(")", chatMidStartIndex);

        return endIndex != -1 ? paramValue.substring(chatMidStartIndex, endIndex).trim() : null;
    }

    private String queryDatabase(SQLiteDatabase db, String query, String... selectionArgs) {
        if (db == null) return null;

        try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
            if (cursor.moveToFirst()) return cursor.getString(0);
        } catch (Exception ignored) {
        }
        return null;
    }

    private void updateDatabase(SQLiteDatabase db, String query, Object... bindArgs) {
        if (db == null) return;

        try {
            db.beginTransaction();
            db.execSQL(query, bindArgs);
            db.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            db.endTransaction();
        }
    }

    private void updateIsArchived(SQLiteDatabase db, String chatId) {
        String updateQuery = "UPDATE chat SET is_archived = 1 WHERE chat_id = ?";
        updateDatabase(db, updateQuery, chatId);

        String selectQuery = "SELECT is_archived FROM chat WHERE chat_id = ?";
        queryDatabase(db, selectQuery, chatId);
    }
}
