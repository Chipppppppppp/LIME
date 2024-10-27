package io.github.hiro.lime.hooks;

import static io.github.hiro.lime.Main.limeOptions;

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
import io.github.hiro.lime.LimeOptions;

public class Archived implements IHook {


    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.Archived.checked) return;
        XposedBridge.hookAllMethods(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application appContext = (Application) param.thisObject;

                if (appContext == null) {
                    return;
                }
                Context moduleContext = appContext;

                File dbFile = appContext.getDatabasePath("naver_line");

                if (dbFile.exists()) {
                    SQLiteDatabase.OpenParams.Builder builder = new SQLiteDatabase.OpenParams.Builder();
                    builder.addOpenFlags(SQLiteDatabase.OPEN_READWRITE);
                    SQLiteDatabase.OpenParams dbParams = builder.build();

                    SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile, dbParams);

                    hookSAMethod(loadPackageParam, db, appContext);
                    hookMessageDeletion(loadPackageParam, appContext, db, moduleContext); // moduleContextを渡す
                } else {
                }
            }
        });
    }
    private void hookMessageDeletion(XC_LoadPackage.LoadPackageParam loadPackageParam, Context context, SQLiteDatabase db,Context moduleContext) {
        if (!limeOptions.Archived.checked) return;
        try {


            XposedBridge.hookAllMethods(
                    loadPackageParam.classLoader.loadClass(Constants.REQUEST_HOOK.className),
                    Constants.REQUEST_HOOK.methodName,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String paramValue = param.args[1].toString();
                            if (paramValue.contains("hidden:true")) {
                                String talkId = extractTalkId(paramValue);
                                if (talkId != null) {
                                    saveTalkIdToFile(talkId, context);
                                    updateArchivedChatsFromFile(db, context,moduleContext);
                                }
                            }
                            if (paramValue.contains("hidden:false")) {
                                String talkId = extractTalkId(paramValue);
                                if (talkId != null) {

                                    deleteTalkIdFromFile(talkId, context);
                                    updateArchivedChatsFromFile(db, context,moduleContext);
                                }
                            }

                        }
                    });

        } catch (ClassNotFoundException e) {
        }
    }

    private void deleteTalkIdFromFile(String talkId, Context moduleContext) {
        File dir = moduleContext.getFilesDir(); // moduleContextを使用
        File file = new File(dir, "hidelist.txt");

        if (file.exists()) {
            try {
                List<String> lines = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().equals(talkId)) {
                        lines.add(line);
                    }
                }
                reader.close();

                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for (String remainingLine : lines) {
                    writer.write(remainingLine);
                    writer.newLine();
                }
                writer.close();
            } catch (IOException e) {
            }
        } else {
        }
    }






    private void hookSAMethod(XC_LoadPackage.LoadPackageParam loadPackageParam, SQLiteDatabase db, Context context) {

        //ChatListViewModel
        Class<?> targetClass = XposedHelpers.findClass("eB.Q", loadPackageParam.classLoader);

        XposedBridge.hookAllMethods(targetClass, "invokeSuspend", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {


                Context appContext = AndroidAppHelper.currentApplication();
                if (appContext == null) {
                    return;
                }

                File dbFile = appContext.getDatabasePath("naver_line");
                SQLiteDatabase db = null;

                if (dbFile.exists()) {
                    SQLiteDatabase.OpenParams.Builder builder = new SQLiteDatabase.OpenParams.Builder();
                    builder.addOpenFlags(SQLiteDatabase.OPEN_READWRITE);
                    SQLiteDatabase.OpenParams dbParams = builder.build();
                    db = SQLiteDatabase.openDatabase(dbFile, dbParams);
                } else {
                    return;
                }
                List<String> chatIds = readChatIdsFromFile(appContext, context);  // 変更点
                for (String chatId : chatIds) {
                    if (!chatId.isEmpty()) {
                        updateIsArchived(db, chatId);
                    }
                }

                if (db != null) {
                    db.close();
                }
            }
        });
    }



    private List<String> readChatIdsFromFile(Context context,Context moduleContext) {
        List<String> chatIds = new ArrayList<>();
        File dir = moduleContext.getFilesDir(); // moduleContextを使用
        File file = new File(dir, "hidelist.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                chatIds.add(line.trim());
            }
        } catch (IOException e) {

        }

        return chatIds;
    }


    private void saveTalkIdToFile(String talkId, Context moduleContext) {
        File dir = moduleContext.getFilesDir(); // moduleContextを使用
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "hidelist.txt");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            List<String> existingIds = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    existingIds.add(line.trim());
                }
            } catch (IOException e) {
            }
            if (!existingIds.contains(talkId.trim())) {
                try (FileWriter writer = new FileWriter(file, true)) {
                    writer.write(talkId + "\n");
                }
            }
        } catch (IOException e) {
        }
    }



    private void updateArchivedChatsFromFile(SQLiteDatabase db, Context context,Context moduleContext) {
        File dir = moduleContext.getFilesDir(); // moduleContextを使用
        File file = new File(dir, "hidelist.txt");

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String chatId;
            while ((chatId = reader.readLine()) != null) {
                chatId = chatId.trim();
                if (!chatId.isEmpty()) {
                    updateIsArchived(db, chatId);
                }
            }
        } catch (IOException e) {

        }
    }

    private String extractTalkId(String paramValue) {
        String talkId = null;
        String requestPrefix = "setChatHiddenStatusRequest:SetChatHiddenStatusRequest(reqSeq:0, chatMid:";
        int startIndex = paramValue.indexOf(requestPrefix);

        if (startIndex != -1) {
            int chatMidStartIndex = startIndex + requestPrefix.length();
            int endIndex = paramValue.indexOf(",", chatMidStartIndex);
            if (endIndex == -1) {
                endIndex = paramValue.indexOf(")", chatMidStartIndex);
            }
            if (endIndex != -1) {
                talkId = paramValue.substring(chatMidStartIndex, endIndex).trim();

            }
        }

        if (talkId == null) {

        }

        return talkId;
    }

    private String queryDatabase(SQLiteDatabase db, String query, String... selectionArgs) {
        if (db == null) {

            return null;
        }
        try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
        }
        return null;
    }

    private void updateDatabase(SQLiteDatabase db, String query, Object... bindArgs) {
        if (db == null) {
            return;
        }
        try {
            db.beginTransaction();
            db.execSQL(query, bindArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
    }

    private void updateIsArchived(SQLiteDatabase db, String chatId) {
        String updateQuery = "UPDATE chat SET is_archived = 1 WHERE chat_id = ?";
        updateDatabase(db, updateQuery, chatId);

        String selectQuery = "SELECT is_archived FROM chat WHERE chat_id = ?";
        String result = queryDatabase(db, selectQuery, chatId);
        if (result != null) {
        } else {
        }
    }
}
