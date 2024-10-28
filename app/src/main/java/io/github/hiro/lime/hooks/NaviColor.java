package io.github.hiro.lime.hooks;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class NaviColor implements IHook {

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
         if (!limeOptions.NaviColor.checked) return;
        XposedBridge.hookAllMethods(Activity.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = activity.getWindow();
                    window.setNavigationBarColor(Color.BLACK);
                }
            }
        });
        XposedHelpers.findAndHookMethod("android.app.Activity", loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                Window window = activity.getWindow();

                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.BLACK);
            }
        });
    }
}
        /*

        XposedHelpers.findAndHookMethod(
                View.class,
                "onAttachedToWindow",
                new XC_MethodHook() {
                    View view;

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        view = (View) param.thisObject;


                        int viewId = view.getId();
                        String resourceName = getResourceName(view.getContext(), viewId);
                        XposedBridge.log("View ID: " + viewId + ", Resource Name: " + resourceName);

                    }
                }
        );
    }

    private int getIdByName(Context context, String resourceName) {
        return context.getResources().getIdentifier(resourceName, "id", context.getPackageName());
    }

    private String getResourceName(Context context, int resourceId) {
        return context.getResources().getResourceEntryName(resourceId);
    }
}



        XposedHelpers.findAndHookMethod("android.view.View", loadPackageParam.classLoader, "onAttachedToWindow", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.thisObject;

                try {
                    // View IDからリソース名を取得
                    String resourceName = view.getResources().getResourceEntryName(view.getId());

                    // 背景色を取得して条件をチェック
                    Drawable background = view.getBackground();
                    if (background instanceof ColorDrawable) {
                        int bgColor = ((ColorDrawable) background).getColor();
                        String hexColor = String.format("#%06X", (0xFFFFFF & bgColor)); // 色を16進数形式に変換

                        // 背景色が#111111または#1F1F1Fの場合、#000000に変更
                        if (hexColor.equals("#111111") || hexColor.equals("#1F1F1F")) {
                            ((ColorDrawable) background).setColor(Color.parseColor("#000000"));
                            XposedBridge.log("Changed Background Color of View Resource Name: " + resourceName + " from " + hexColor + " to #000000");
                        } else {
                            XposedBridge.log("View Resource Name: " + resourceName + " Background Color: " + hexColor);
                        }
                    }

                    // テキストビューの場合、テキスト色を取得して条件をチェック
                    if (view instanceof TextView) {
                        int textColor = ((TextView) view).getCurrentTextColor();
                        String hexTextColor = String.format("#%06X", (0xFFFFFF & textColor)); // 色を16進数形式に変換

                        // テキスト色が#111111の場合、#000000に変更
                        if (hexTextColor.equals("#111111")) {
                            ((TextView) view).setTextColor(Color.parseColor("#000000"));
                            XposedBridge.log("Changed Text Color of View Resource Name: " + resourceName + " from " + hexTextColor + " to #000000");
                        } else {
                            XposedBridge.log("View Resource Name: " + resourceName + " Text Color: " + hexTextColor);
                        }
                    }

                    // ボタンの場合、背景色を取得して条件をチェック
                    if (view instanceof Button) {
                        Drawable buttonBackground = view.getBackground();
                        if (buttonBackground instanceof ColorDrawable) {
                            int buttonBgColor = ((ColorDrawable) buttonBackground).getColor();
                            String hexButtonColor = String.format("#%06X", (0xFFFFFF & buttonBgColor)); // 色を16進数形式に変換

                            // 背景色が#111111または#1F1F1Fの場合、#000000に変更
                            if (hexButtonColor.equals("#111111") || hexButtonColor.equals("#1F1F1F")) {
                                ((ColorDrawable) buttonBackground).setColor(Color.parseColor("#000000"));
                                XposedBridge.log("Changed Button Background Color of Resource Name: " + resourceName + " from " + hexButtonColor + " to #000000");
                            } else {
                                XposedBridge.log("Button Resource Name: " + resourceName + " Background Color: " + hexButtonColor);
                            }
                        }
                    }

                } catch (Resources.NotFoundException e) {
                    // リソース名が見つからなかった場合の処理
                    XposedBridge.log("View ID: " + view.getId() + " - Resource name not found.");
                }
            }
        });
*/




