package io.github.hiro.lime.hooks;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class PreventMarkAsRead implements IHook {
    private boolean isSendChatCheckedEnabled = false; // デフォルト値

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {


        if (limeOptions.preventMarkAsRead.checked ) {
            Class<?> chatHistoryActivityClass = XposedHelpers.findClass("jp.naver.line.android.activity.chathistory.ChatHistoryActivity", loadPackageParam.classLoader);
            XposedHelpers.findAndHookMethod(chatHistoryActivityClass, "onCreate", Bundle.class, new XC_MethodHook() {
                Context moduleContext;


                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (moduleContext == null) {
                        try {
                            Context systemContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                            moduleContext = systemContext.createPackageContext("io.github.hiro.lime", Context.CONTEXT_IGNORE_SECURITY);
                        } catch (Exception e) {
                            //XposedBridge.log("Failed to get module context: " + e.getMessage());
                        }
                    }
                }


                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (moduleContext == null) {
                        //XposedBridge.log("Module context is null. Skipping hook.");
                        return;
                    }
                    Activity activity = (Activity) param.thisObject;
                    addButton(activity, moduleContext);
                }
                private void addButton(Activity activity, Context moduleContext) {
                    isSendChatCheckedEnabled = readStateFromFile(moduleContext);

                    ToggleButton toggleButton = new ToggleButton(activity);
                    toggleButton.setTextOn("UnRead");
                    toggleButton.setTextOff("Read");
                    toggleButton.setChecked(isSendChatCheckedEnabled); // 初期状態を反映
                    toggleButton.setBackgroundColor(Color.BLACK);
                    toggleButton.setTextColor(Color.WHITE);
                    toggleButton.setTextSize(12);
                    int width = 180;
                    int height = 80;
                    FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(width, height);

                    frameParams.gravity = Gravity.TOP | Gravity.END;
                    frameParams.topMargin = 150;
                    frameParams.rightMargin = 300;
                    toggleButton.setLayoutParams(frameParams);

                    toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        isSendChatCheckedEnabled = isChecked;
                        send_chat_checked_state(moduleContext, isSendChatCheckedEnabled);
                    });

                    ViewGroup layout = activity.findViewById(android.R.id.content);
                    layout.addView(toggleButton);
                }

                private void send_chat_checked_state (Context context,boolean state){
                    String filename = "send_chat_checked_state.txt";
                    try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
                        fos.write((state ? "1" : "0").getBytes());
                    } catch (IOException ignored) {

                    }
                }
                private boolean readStateFromFile(Context context) {
                    String filename = "send_chat_checked_state.txt";
                    try (FileInputStream fis = context.openFileInput(filename)) {
                        int c;
                        StringBuilder sb = new StringBuilder();
                        while ((c = fis.read()) != -1) {
                            sb.append((char) c);
                        }
                        return "1".equals(sb.toString());
                    } catch (IOException ignored) {
                        return true;
                    }
                }

            });
            XposedHelpers.findAndHookMethod(
                    loadPackageParam.classLoader.loadClass(Constants.MARK_AS_READ_HOOK.className),
                    Constants.MARK_AS_READ_HOOK.methodName,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {

                                param.setResult(null);
                            }

                    }
            );
                XposedBridge.hookAllMethods(
                        loadPackageParam.classLoader.loadClass(Constants.REQUEST_HOOK.className),
                        Constants.REQUEST_HOOK.methodName,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                if (param.args[0].toString().equals("sendChatChecked")) {
                                    if (!isSendChatCheckedEnabled) { // isSendChatCheckedEnabledがfalseの場合のみnullを設定
                                        param.setResult(null);
                                    }
                                }
                            }
                        }
                );

                XposedBridge.hookAllMethods(
                        loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                        Constants.RESPONSE_HOOK.methodName,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                if (param.args[0] != null && param.args[0].toString().equals("sendChatChecked")) {
                                    if (!isSendChatCheckedEnabled) {
                                        param.setResult(null);
                                    }
                                }
                            }
                        }
                );
            }
        }
    }

