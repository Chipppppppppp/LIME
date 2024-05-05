package io.github.chipppppppppp.lime.hooks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.github.chipppppppppp.lime.LimeOptions;

public class BlockTracking implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!limeOptions.blockTracking.checked) return;

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("org.apache.thrift.n"),
                "b",
                new XC_MethodHook() {
                    static final Set<String> requests = new HashSet<>(Arrays.asList(
                            "noop",
                            "pushRecvReports",
                            "reportDeviceState",
                            "reportLocation",
                            "reportNetworkStatus"
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
