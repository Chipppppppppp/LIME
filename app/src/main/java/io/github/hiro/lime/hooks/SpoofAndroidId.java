package io.github.hiro.lime.hooks;

import android.content.ContentResolver;
import android.provider.Settings;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;
import io.github.hiro.lime.Main;

public class SpoofAndroidId implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!Main.xPackagePrefs.getBoolean("spoof_android_id", false)) return;

        XposedHelpers.findAndHookMethod(
                Settings.Secure.class,
                "getString",
                ContentResolver.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[1].toString().equals(Settings.Secure.ANDROID_ID)) {
                            param.setResult("0000000000000000");
                        }
                    }
                }
        );
    }
}
