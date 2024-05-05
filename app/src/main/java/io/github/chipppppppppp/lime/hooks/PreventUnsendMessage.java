package io.github.chipppppppppp.lime.hooks;

import java.lang.reflect.Method;

import io.github.chipppppppppp.lime.LimeOptions;

public class PreventUnsendMessage implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!limeOptions.preventUnsendMessage.checked) return;

        Class<?> hookTarget = loadPackageParam.classLoader.loadClass("rp5.id");
        final Method valueOf = hookTarget.getMethod("valueOf", String.class);
        final Object dummy = valueOf.invoke(null, "DUMMY");
        final Object notifiedDestroyMessage = valueOf.invoke(null, "NOTIFIED_DESTROY_MESSAGE");
        XposedHelpers.findAndHookMethod(
                hookTarget,
                "a",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.getResult() == notifiedDestroyMessage) {
                            param.setResult(dummy);
                        }
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass("mo5.c"),
                "u",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.getResult().equals("UNSENT")) {
                            param.setResult("");
                        }
                    }
                }
        );
    }
}
