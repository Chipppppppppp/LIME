package io.github.hiro.lime.hooks;

import java.lang.reflect.Field;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class RemoveNotification implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.RemoveNotification.checked) return;
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                Constants.RESPONSE_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!"sync".equals(param.args[0].toString())) return;

                        try {
                            Object wrapper = param.args[1].getClass().getDeclaredField("a").get(param.args[1]);
                            if (wrapper == null) return;

                            Field operationResponseField = wrapper.getClass().getSuperclass().getDeclaredField("value_");
                            operationResponseField.setAccessible(true);
                            Object operationResponse = operationResponseField.get(wrapper);
                            if (operationResponse == null) return;

                            ArrayList<?> operations = (ArrayList<?>) operationResponse.getClass().getDeclaredField("a").get(operationResponse);
                            if (operations == null) return;


                            for (int i = operations.size() - 1; i >= 0; i--) {
                                Object operation = operations.get(i);
                                Field typeField = operation.getClass().getDeclaredField("c");
                                typeField.setAccessible(true);
                                Object type = typeField.get(operation);


                                if ("NOTIFIED_UPDATE_PROFILE".equals(type.toString())) {
                                    typeField.set(operation, type.getClass().getMethod("valueOf", String.class).invoke(type, "DUMMY"));
                                }
                            }
                        } catch (NoSuchFieldException | IllegalAccessException |
                                 IllegalArgumentException e) {
                       //     XposedBridge.log("RemoveNotification: Error accessing fields - " + e.getMessage());
                        } catch (Exception e) {
                       //     XposedBridge.log("RemoveNotification: Unexpected error - " + e.getMessage());
                        }
                    }
                }
        );

    }
}
