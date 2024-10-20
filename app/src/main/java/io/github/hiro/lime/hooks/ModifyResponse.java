package io.github.hiro.lime.hooks;

import android.util.Base64;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;
import io.github.hiro.lime.Main;

public class ModifyResponse implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                Constants.RESPONSE_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        final String script = new String(Base64.decode(Main.xPrefs.getString("encoded_js_modify_response", ""), Base64.NO_WRAP));
                        Context ctx = Context.enter();
                        ctx.setOptimizationLevel(-1);
                        try {
                            Scriptable scope = ctx.initStandardObjects();
                            Object jsData = Context.javaToJS(new Communication(Communication.Type.RESPONSE, param.args[0].toString(), param.args[1]), scope);
                            ScriptableObject.putProperty(scope, "data", jsData);
                            ScriptableObject.putProperty(scope, "console", Context.javaToJS(new Console(), scope));
                            ctx.evaluateString(scope, script, "Script", 1, null);
                        } catch (Exception e) {
                            XposedBridge.log(e.toString());
                        } finally {
                            Context.exit();
                        }
                    }
                }
        );
    }
}
