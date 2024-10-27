package io.github.hiro.lime.hooks;

import android.app.Activity;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

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
