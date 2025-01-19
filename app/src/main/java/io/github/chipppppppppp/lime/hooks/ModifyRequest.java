package io.github.chipppppppppp.lime.hooks;

import android.util.Base64;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;
import io.github.chipppppppppp.lime.Main;

public class ModifyRequest implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.REQUEST_HOOK.className),
                Constants.REQUEST_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        final String script = new String(Base64.decode(Main.xPrefs.getString("encoded_js_modify_request", ""), Base64.NO_WRAP));
                        Context ctx = Context.enter();
                        ctx.setOptimizationLevel(-1);
                        try {
                            Scriptable scope = ctx.initStandardObjects();
                            Object jsData = Context.javaToJS(new Communication(Communication.Type.REQUEST, param.args[0].toString(), param.args[1]), scope);
                            ScriptableObject.putProperty(scope, "data", jsData);
                            ScriptableObject.putProperty(scope, "console", Context.javaToJS(new Console(), scope));

                            String utilityScript =
                                    "function getMember(obj, key) {" +
                                            "    return obj.getClass().getField(key).get(obj);" +
                                            "}" +
                                            "function setMember(obj, key, value) {" +
                                            "    return obj.getClass().getField(key).set(obj, value);" +
                                            "}";
                            ctx.evaluateString(scope, utilityScript, "UtilityScript", 1, null);

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
