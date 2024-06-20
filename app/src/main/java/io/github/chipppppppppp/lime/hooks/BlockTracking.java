package io.github.chipppppppppp.lime.hooks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class BlockTracking implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.blockTracking.checked) return;

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.REQUEST_HOOK.className),
                Constants.REQUEST_HOOK.methodName,
                new XC_MethodHook() {
                    static final Set<String> requests = new HashSet<>(Arrays.asList(
                            "noop",
                            "reportAbuseEx",
                            "reportDeviceState",
                            "reportLocation",
                            "reportNetworkStatus",
                            "reportProfile",
                            "reportPushRecvReports",
                            "reportSetting"
                    ));

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (requests.contains(param.args[0].toString())) {
                            param.setResult(null);
                        }
                    }
                }
        );
    }
}
