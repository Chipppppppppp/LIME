package io.github.hiro.lime.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class RemoveVoiceRecord implements IHook {
    private boolean isXg1EaRunCalled = true;
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.RemoveVoiceRecord.checked) return;

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("xg1.e$a"),
                "run",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        isXg1EaRunCalled = true;
                    }

                }
        );
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("TS.f"),
                "run",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        isXg1EaRunCalled = false;
                    }

                }
        );
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("af0.e"),
                "run",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (isXg1EaRunCalled) {
                            return;
                        }
                        param.setResult(null);
                    }
                }
        );
    }
}