package io.github.hiro.lime.hooks;

import android.app.Application;
import android.content.Context;
import java.io.File;
import java.lang.reflect.Field;


import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class CrashGuard implements IHook {

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.hookAllMethods(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application appContext = (Application) param.thisObject;
                clearCache(appContext);
            }

            private void clearCache(Context context) {
                try {
                    File cacheDir = context.getCacheDir();
                    if (cacheDir != null && cacheDir.isDirectory()) {
                        deleteDir(cacheDir);
                        XposedBridge.log("Cache cleared successfully.");
                    }
                } catch (Exception e) {
                    XposedBridge.log("Failed to clear cache: " + e.getMessage());
                }
            }

            private boolean deleteDir(File dir) {
                if (dir != null && dir.isDirectory()) {
                    String[] children = dir.list();
                    if (children != null) {
                        for (String child : children) {
                            boolean success = deleteDir(new File(dir, child));
                            if (!success) {
                                return false;
                            }
                        }
                    }
                }
                return dir.delete();
            }
        });


    }
}
