package io.github.chipppppppppp.lime.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class PreventMarkAsRead implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.preventMarkAsRead.checked) return;

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("org.apache.thrift.n"),
                "b",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[0].toString().equals("sendChatChecked")) {
                            param.setResult(null);
                        }
                    }
                }
        );
    }
}
