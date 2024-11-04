/*
package io.github.hiro.lime.hooks;

import java.lang.reflect.Field;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class SleepCall implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.SleepCall.checked) return;

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                Constants.RESPONSE_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[0].toString().equals("sync")) {
                            Object wrapper = param.args[1].getClass().getDeclaredField("a").get(param.args[1]);
                            Field operationResponseField = wrapper.getClass().getSuperclass().getDeclaredField("value_");
                            operationResponseField.setAccessible(true);
                            Object operationResponse = operationResponseField.get(wrapper);
                            ArrayList<?> operations = (ArrayList<?>) operationResponse.getClass().getDeclaredField("a").get(operationResponse);

                            for (int i = operations.size() - 1; i >= 0; i--) {
                                Object operation = operations.get(i);
                                Field typeField = operation.getClass().getDeclaredField("c");
                                Object type = typeField.get(operation);
                                if (type.toString().equals("NOTIFIED_RECEIVED_CALL")) {
                                    // Operationをnullに設定
                                    operations.set(i, null);
                                }
                            }
                        }
                    }
                }
        );
    }
}
*/