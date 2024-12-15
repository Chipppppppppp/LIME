package io.github.hiro.lime.hooks;

import android.app.AndroidAppHelper;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import java.io.BufferedReader;
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

public class Disabled_Group_notification implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.MuteGroup.checked) return;
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                Constants.RESPONSE_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String paramValue = param.args[1].toString();
                        if (paramValue.contains("chatName:") && paramValue.contains("getChats_result")) {
                            String chatName = extractChatName(paramValue);
                            if (chatName != null) {
                                //  XposedBridge.log("Extracted chatName: " + chatName);
                                Context moduleContext = AndroidAppHelper.currentApplication();
                                File dir = moduleContext.getFilesDir();
                                saveChatNameToFile(chatName, dir);
                            }
                        }
                    }

                    private String extractChatName(String paramValue) {
                        String marker = "chatName:";
                        int startIndex = paramValue.indexOf(marker);
                        if (startIndex != -1) {
                            startIndex += marker.length();
                            int endIndex = paramValue.indexOf(',', startIndex);
                            if (endIndex != -1) {
                                return paramValue.substring(startIndex, endIndex).trim();
                            }
                        }
                        return null;
                    }

                    private void saveChatNameToFile(String chatName, File dir) {
                        if (!dir.exists() && !dir.mkdirs()) {
                         //   XposedBridge.log("Failed to create directory: " + dir.getPath());
                            return;
                        }

                        File file = new File(dir, "Notification.txt");

                        try {
                            if (!file.exists() && !file.createNewFile()) {
                              //  XposedBridge.log("Failed to create file: " + file.getPath());
                                return;
                            }
                            List<String> existingChatNames = new ArrayList<>();
                            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    existingChatNames.add(line.trim());
                                }
                            } catch (IOException ignored) {
                             //   XposedBridge.log("Error reading file: " + e.getMessage());
                            }
                            if (!existingChatNames.contains(chatName.trim())) {
                                try (FileWriter writer = new FileWriter(file, true)) {
                                    writer.write(chatName + "\n");
                                 //   XposedBridge.log("Saved chatName: " + chatName);
                                } catch (IOException ignored) {
                                 //   XposedBridge.log("Error writing to file: " + e.getMessage());
                                }
                            } else {
                               // XposedBridge.log("Chat name already exists: " + chatName);
                            }
                        } catch (IOException ignored) {
                         //   XposedBridge.log("Error accessing file: " + e.getMessage());
                        }
                    }
                });




        XposedHelpers.findAndHookMethod(NotificationManager.class, "notify",
                String.class, int.class, Notification.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        int id = (int) param.args[1];
                        Notification notification = (Notification) param.args[2];

                        logNotificationDetails("NotificationManager.notify (with tag)", id, notification);
                        String subText = notification.extras.getString(Notification.EXTRA_SUB_TEXT);
                        List<String> chatNamesFromFile = loadNamesFromFile();
                        for (String chatName : chatNamesFromFile) {
                            if (subText != null && subText.contains(chatName)) {
                                param.setResult(null);
                                return;
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            String channelId = notification.getChannelId();
                            NotificationManager manager = (NotificationManager) AndroidAppHelper.currentApplication().getSystemService(Context.NOTIFICATION_SERVICE);
                            NotificationChannel channel = manager.getNotificationChannel(channelId);
                            if (channel != null) {
                                String channelName = channel.getName().toString();
                                // XposedBridge.log("Notification Channel Name: " + channelName);
                            }
                        }
                    }
                });

        XposedHelpers.findAndHookMethod(Notification.Builder.class, "setContentTitle",
                CharSequence.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        CharSequence title = (CharSequence) param.args[0];

                        List<String> chatNamesFromFile = loadNamesFromFile();

                        for (String chatName : chatNamesFromFile) {
                            if (title != null && title.toString().contains(chatName)) {
                                //XposedBridge.log("Notification title contains chatName from file. Title: " + title);
                                param.setResult(null); // nullを返して通知を表示しない
                                return;
                            }
                        }
                        //XposedBridge.log("Notification title does not contain any chatName from file. Title: " + title);
                    }
                });
    }

    // 通知の詳細をログに記録するヘルパーメソッド
    private void logNotificationDetails(String method, int id, Notification notification) {

        if (notification.extras != null) {

            String title = notification.extras.getString(Notification.EXTRA_TITLE); // タイトル
            String text = notification.extras.getString(Notification.EXTRA_TEXT);   // メインのテキスト
            String subText = notification.extras.getString(Notification.EXTRA_SUB_TEXT); // サブテキスト

            /*
            XposedBridge.log("Notification Title: " + (title != null ? title : "No Title"));
            XposedBridge.log("Notification Text: " + (text != null ? text : "No Text"));
            XposedBridge.log("Notification SubText: " + (subText != null ? subText : "No SubText"));
             */
        } else {
            // XposedBridge.log("Notification extras is null.");
        }
        //XposedBridge.log("Notification Icon: " + notification.icon);
    }

    private List<String> loadNamesFromFile() {
        List<String> names = new ArrayList<>();
        Context moduleContext = AndroidAppHelper.currentApplication();
        File dir = moduleContext.getFilesDir();
        File file = new File(dir, "Notification.txt");
        if (!file.exists()) {
            return names;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                names.add(line.trim());
            }
        } catch (IOException ignored) {
            //     XposedBridge.log("Error reading names from file: " + e.getMessage());
        }

        return names;
    }
}