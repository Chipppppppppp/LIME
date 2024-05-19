package io.github.chipppppppppp.lime.hooks;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;
import io.github.chipppppppppp.lime.Main;

public class SecondaryLogin implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!Main.xPackagePrefs.getBoolean("android_secondary", false)) return;

        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass(Constants.USER_AGENT_HOOK.className),
                Constants.USER_AGENT_HOOK.methodName,
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult("ANDROID\t32767.2147483647.2147483647");
                    }
                }
        );
    }
}
