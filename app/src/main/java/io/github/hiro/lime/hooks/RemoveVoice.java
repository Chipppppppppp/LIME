package io.github.hiro.lime.hooks;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class RemoveVoice implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        Class<?> chatHistoryActivityClass = XposedHelpers.findClass(
                "jp.naver.line.android.activity.chathistory.ChatHistoryActivity",
                loadPackageParam.classLoader
        );

        XposedHelpers.findAndHookMethod(chatHistoryActivityClass, "onCreate", Bundle.class, new XC_MethodHook() {
            Context moduleContext;

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (moduleContext == null) {
                    try {
                        Context systemContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                        moduleContext = systemContext.createPackageContext("io.github.hiro.lime", Context.CONTEXT_IGNORE_SECURITY);
                    } catch (Exception e) {
                        XposedBridge.log("Failed to get module context: " + e.getMessage());
                    }
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (moduleContext == null) {
                    XposedBridge.log("Module context is null. Skipping hook.");
                    return;
                }

                // アクティビティインスタンスを取得
                Activity activity = (Activity) param.thisObject;

                // 現在のアクティビティで使用されているリソースをログに出力
                try {
                    Resources resources = activity.getResources();

                    // レイアウトのログを出力
                    int contentViewId = resources.getIdentifier("content_view", "id", activity.getPackageName());
                    if (contentViewId != 0) {
                        View contentView = activity.findViewById(contentViewId);
                        if (contentView != null) {
                            XposedBridge.log("Content View: " + contentView.toString());
                        }
                    }

                    // すべてのビューをログに出力
                    logAllViews((ViewGroup) activity.findViewById(android.R.id.content));

                } catch (Exception e) {
                    XposedBridge.log("Error logging resources: " + e.getMessage());
                }
            }

            private void logAllViews(ViewGroup root) {
                if (root == null) return;
                for (int i = 0; i < root.getChildCount(); i++) {
                    View child = root.getChildAt(i);
                    XposedBridge.log("View: " + child.toString());

                    if (child instanceof ViewGroup) {
                        logAllViews((ViewGroup) child); // 再帰的にログ出力
                    }
                }
            }
        });
    }
}