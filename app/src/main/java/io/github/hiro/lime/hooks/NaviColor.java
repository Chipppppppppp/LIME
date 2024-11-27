package io.github.hiro.lime.hooks;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class NaviColor implements IHook {
    String[] excludedResourceNames = {
            "chathistory_message_list",
            "profile_area_binding","fallback_header_background","user_profile_area","common_dialog_edit_text","chat_ui_oa_status_bar_button",
            "user_profile_cover_dim_layer","status_bar_background_view","default_color_animation_layer","main_tab_search_bar_scanner_icon","header_button_layout",
            "profile_area_binding","social_profile_header_back","user_profile_cover_dim_layer","chat_ui_announcement_unfold_content_unfold_button",
            "social_profile_link","user_profile_button_area_separator","user_profile_root","content","status_bar_background_view","chathistory_oa_status_bar_holder_view"
            ,"chat_ui_fragment_container","chathistory_header_search_box_viewstub","tab_container","no_id","user_profile_status_message_edit_button"
    };
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

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.BLACK);
                }
            }
        });







// onAttachedToWindow メソッドをフックして、背景色が #1F1F1F の場合に #000000 に変更
        XposedHelpers.findAndHookMethod("android.view.View", loadPackageParam.classLoader, "onAttachedToWindow", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.thisObject;
                checkAndChangeBackgroundColor(view);
            }
        });



// setBackgroundColor メソッドをフックして、背景色が #1F1F1F の場合に #000000 に変更
        XposedHelpers.findAndHookMethod("android.view.View", loadPackageParam.classLoader, "setBackgroundColor", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.thisObject;
                checkAndChangeBackgroundColor(view);
            }
        });



    }


    private void checkAndChangeBackgroundColor(View view) {
        try {


            // リソース名を取得
            String resourceName = getViewResourceName(view);
             //XposedBridge("Resource Name: " + resourceName);


            // 背景を取得
            Drawable background = view.getBackground();

            // 背景が null でないことを確認
            if (background != null) {
                // 背景のクラス名をログに出力
                 //XposedBridge("Background Class Name: " + background.getClass().getName());

                if (background instanceof ColorDrawable) {
                    // 現在の背景色を取得
                    int currentColor = ((ColorDrawable) background).getColor();

                    // 色が指定された条件に一致する場合、#000000 に変更
                    if (currentColor == Color.parseColor("#111111") || currentColor == Color.parseColor("#1A1A1A")) {
                        ((ColorDrawable) background).setColor(Color.parseColor("#000000"));
                         //XposedBridge("Changed Background Color of Resource Name: " + resourceName + " to #000000");
                    }
                } else if (background instanceof BitmapDrawable) {
                     //XposedBridge("BitmapDrawable background, cannot change color directly.");
                } else {
                     //XposedBridge("Unknown background type for Resource Name: " + resourceName + ", Class Name: " + background.getClass().getName());
                }
            } else {
                 //XposedBridge("Background is null for Resource Name: " + resourceName);
            }
        } catch (Resources.NotFoundException e) {
             //XposedBridge("Resource name not found for View ID: " + view.getId());
        } finally {

        }
    }


    // リソース名を取得するためのメソッド
    private String getViewResourceName(View view) {
        int viewId = view.getId();
        if (viewId != View.NO_ID) { // IDが無効でない場合にのみ処理
            try {
                return view.getResources().getResourceEntryName(viewId);
            } catch (Resources.NotFoundException e) {
                //XposedBridge("Resource not found for View ID: " + viewId);
                return "unknown";
            }
        }
        return "no_id";
    }





    // 色を16進数形式に変換するメソッド
    private String convertToHexColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color)); // 16進数形式に変換
    }


}





