package io.github.chipppppppppp.lime.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;
import io.github.chipppppppppp.lime.Main;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ChangeCommunication implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.outputCommunication.checked) return;

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.REQUEST_HOOK.className),
                Constants.REQUEST_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String script = Main.xPrefs.getString("custom_js", "");
                        Context ctx = Context.enter();
                        try {
                            Scriptable scope = ctx.initStandardObjects();
                            Object jsCommunication = Context.javaToJS(new Communication(Communication.Type.REQUEST, param.args[0].toString(), param.args[1]), scope);
                            ScriptableObject.putProperty(scope, "communication", jsCommunication);
                            ctx.evaluateString(scope, script, "Script", 1, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            Context.exit();
                        }
                    }
                }
        );

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                Constants.RESPONSE_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String script = Main.xPrefs.getString("custom_js", "");
                        Context ctx = Context.enter();
                        try {
                            Scriptable scope = ctx.initStandardObjects();
                            Object jsCommunication = Context.javaToJS(new Communication(Communication.Type.RESPONSE, param.args[0].toString(), param.args[1]), scope);
                            ScriptableObject.putProperty(scope, "communication", jsCommunication);
                            ctx.evaluateString(scope, script, "Script", 1, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            Context.exit();
                        }
                    }
                }
        );
    }
}
