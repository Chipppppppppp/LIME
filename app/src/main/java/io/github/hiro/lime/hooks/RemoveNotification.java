package io.github.hiro.lime.hooks;

import java.lang.reflect.Field;
import java.util.ArrayList;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class RemoveNotification implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.RemoveNotification.checked) return;

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                Constants.RESPONSE_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        // syncの場合の処理
                        if (param.args[0].toString().equals("sync")) {
                            Object wrapper = param.args[1].getClass().getDeclaredField("a").get(param.args[1]);
                            Field operationResponseField = wrapper.getClass().getSuperclass().getDeclaredField("value_");
                            operationResponseField.setAccessible(true);
                            Object operationResponse = operationResponseField.get(wrapper);
                            ArrayList<?> operations = (ArrayList<?>) operationResponse.getClass().getDeclaredField("a").get(operationResponse);

                            for (int i = operations.size() - 1; i >= 0; i--) {
                                Object operation = operations.get(i);
                                Field typeField = operation.getClass().getDeclaredField("c");
                                Object type = typeField.get(operation);
                                if (type.toString().equals("NOTIFIED_UPDATE_PROFILE")) {
                                    typeField.set(operation, type.getClass().getMethod("valueOf", String.class).invoke(type, "DUMMY"));
                                }
                            }
                        }

                        // getChats_resultの場合の処理
                        if (param.args[0].toString().equals("getChats_result")) {
                            try {
                                // wrapperオブジェクトにアクセス
                                Object wrapper = param.args[1].getClass().getDeclaredField("a").get(param.args[1]);
                                Field responseField = wrapper.getClass().getSuperclass().getDeclaredField("value_");
                                responseField.setAccessible(true);
                                Object getChatsResponse = responseField.get(wrapper);

                                // GetChatsResponseからチャットのリストを取得
                                ArrayList<?> chats = (ArrayList<?>) getChatsResponse.getClass().getDeclaredField("chats").get(getChatsResponse);
                                XposedBridge.log("Number of chats: " + chats.size()); // チャット数をログ出力

                                // 各チャットをループ処理
                                for (Object chat : chats) {
                                    try {
                                        // notificationDisabledフィールドを取得
                                        Field notificationDisabledField = chat.getClass().getDeclaredField("notificationDisabled");
                                        notificationDisabledField.setAccessible(true);

                                        // notificationDisabledの値をログ出力
                                        XposedBridge.log("Chat notificationDisabled before: " + notificationDisabledField.get(chat));

                                        // notificationDisabledをtrueに設定
                                        notificationDisabledField.set(chat, true);
                                        XposedBridge.log("Chat notificationDisabled after: " + notificationDisabledField.get(chat));

                                        // 他のフィールドの値をログ出力する例
                                        Field chatMidField = chat.getClass().getDeclaredField("chatMid"); // 例としてchatMidフィールドを取得
                                        chatMidField.setAccessible(true);
                                        XposedBridge.log("Chat MID: " + chatMidField.get(chat));

                                    } catch (NoSuchFieldException e) {
                                        XposedBridge.log("Field 'notificationDisabled' not found in chat object: " + e.getMessage());
                                    } catch (IllegalAccessException e) {
                                        XposedBridge.log("Cannot access 'notificationDisabled' field: " + e.getMessage());
                                    }
                                }

                                // すべてのnotificationDisabledフィールドがtrueに設定されたことをログに出力
                                XposedBridge.log("All notificationDisabled fields have been set to true.");
                            } catch (Exception e) {
                                XposedBridge.log("Error while processing getChats_result: " + e.getMessage());
                            }
                        }
                    }
                }
        );
    }
}
