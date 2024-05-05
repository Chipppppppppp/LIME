package io.github.chipppppppppp.lime.hooks;

import io.github.chipppppppppp.lime.LimeOptions;

public class SendMuteMessage implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!limeOptions.sendMuteMessage.checked) return;

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("cl5.b"),
                "H",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = Enum.valueOf((Class<Enum>) param.args[0].getClass(), "TO_BE_SENT_SILENTLY");
                    }
                }
        );
    }
}
