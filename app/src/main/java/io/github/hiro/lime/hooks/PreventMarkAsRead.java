package io.github.hiro.lime.hooks;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.lang.reflect.Field;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class PreventMarkAsRead implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.preventMarkAsRead.checked) return;


        findAndHookMethod(
                loadPackageParam.classLoader.loadClass(Constants.MARK_AS_READ_HOOK.className),
                Constants.MARK_AS_READ_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {

                        param.setResult(null);

                    }
                }
        );



        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.REQUEST_HOOK.className),
                Constants.REQUEST_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[0].toString().equals("sendChatChecked")) {
                            param.setResult(null);
                        }
                    }
                }
        );
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                Constants.RESPONSE_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[0] != null && param.args[0].toString().equals("sendChatChecked")) {
                            param.setResult(null);
                        }
                    }
                }
        );

        findAndHookMethod(
                "cj.b",
                loadPackageParam.classLoader,
                "f",
                int.class, byte[].class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Field field = param.thisObject.getClass().getDeclaredField("b");
                        field.setAccessible(true);
                        Object receiveSource = field.get(param.thisObject);
                        if (receiveSource == null) {
                            XposedBridge.log("receiveSource is not set. Skipping method call.");
                            param.setResult(0);
                        }
                    }
                }
        );
        XposedHelpers.findAndHookMethod(
                "jp.naver.line.android.thrift.client.impl.LegacyTalkServiceClientImpl",
                loadPackageParam.classLoader,
                "S2",
                HashMap.class, HashMap.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }

                }
        );



    }
}
