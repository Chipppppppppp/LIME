package io.github.hiro.lime.hooks;

import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.BuildConfig;
import io.github.hiro.lime.LimeOptions;
import io.github.hiro.lime.R;
import io.github.hiro.lime.Utils;

public class CheckHookTargetVersion implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (limeOptions.stopVersionCheck.checked) return;
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("jp.naver.line.android.activity.SplashActivity"),
                "onCreate",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Context context = (Context) param.thisObject;
                        PackageManager pm = context.getPackageManager();
                        long versionCode = pm.getPackageInfo(loadPackageParam.packageName, 0).getLongVersionCode();
                        String versionCodeStr = String.valueOf(versionCode); 

                        if (!BuildConfig.HOOK_TARGET_VERSION.equals(versionCodeStr)) {
                            Utils.addModuleAssetPath(context);
                            Toast.makeText(context.getApplicationContext(), context.getString(R.string.incompatible_version), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
}
