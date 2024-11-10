package io.github.hiro.lime.hooks;


import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import io.github.hiro.lime.hooks.IHook;

public class CrashGuard implements IHook {

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        try {
            Class<?> riClass = XposedHelpers.findClass("Ri.b", loadPackageParam.classLoader);

            XposedHelpers.findAndHookMethod(riClass, "f", int.class, byte[].class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Object instance = param.thisObject;

                    // `b`フィールドを取得
                    Object bField = XposedHelpers.getObjectField(instance, "b");

                    if (bField == null) {
                        XposedBridge.log("CrashGuard: `receiveSource` (b field) is not set; skipping method call.");
                        param.setResult(null);
                    }
                }
            });

        } catch (XposedHelpers.ClassNotFoundError e) {
            XposedBridge.log("CrashGuard: Ri.b class not found, cannot hook method.");
        } catch (NoSuchFieldError e) {
            XposedBridge.log("CrashGuard: `b` field not found in Ri.b, cannot perform null check.");
        } catch (Exception e) {
            XposedBridge.log("CrashGuard: Unexpected error: " + e.getMessage());
        }
    }
}
