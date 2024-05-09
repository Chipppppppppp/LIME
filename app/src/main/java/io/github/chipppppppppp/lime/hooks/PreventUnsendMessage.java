package io.github.chipppppppppp.lime.hooks;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class PreventUnsendMessage implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.preventUnsendMessage.checked) return;

        final Class<?> hookTarget = loadPackageParam.classLoader.loadClass(Constants.COMMUNICATION_ENUM_HOOK.className);
        final Method valueOf = hookTarget.getMethod("valueOf", String.class);
        final Object dummy = valueOf.invoke(null, "DUMMY");
        final Object notifiedDestroyMessage = valueOf.invoke(null, "NOTIFIED_DESTROY_MESSAGE");
        XposedHelpers.findAndHookMethod(
                hookTarget,
                Constants.COMMUNICATION_ENUM_HOOK.methodName,
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
                loadPackageParam.classLoader.loadClass(Constants.UNSENT_HOOK.className),
                Constants.UNSENT_HOOK.methodName,
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
