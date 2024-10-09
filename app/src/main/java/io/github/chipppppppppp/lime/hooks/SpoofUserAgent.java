package io.github.chipppppppppp.lime.hooks;

import android.content.Context;
import android.content.SharedPreferences;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;
import io.github.chipppppppppp.lime.Main;

public class SpoofUserAgent implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!Main.xPackagePrefs.getBoolean("android_secondary", false)) return;

        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass(Constants.USER_AGENT_HOOK.className),
                Constants.USER_AGENT_HOOK.methodName,
                Context.class,
                new XC_MethodHook() {
                    private static boolean hasLoggedSpoofedUserAgent = false;  

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        SharedPreferences prefs = Main.xPackagePrefs;

                        String androidVersion = prefs.getString("android_version", "14.16.0");
                        String osName = prefs.getString("os_name", "Android OS");
                        String osVersion = prefs.getString("os_version", "14");

                        String spoofedUserAgent = "ANDROID\t" + androidVersion + "\t" + osName + "\t" + osVersion;
                        param.setResult(spoofedUserAgent);

                        // ログは一度だけ出力
                        if (!hasLoggedSpoofedUserAgent) {
                            XposedBridge.log("Spoofed User-Agent: " + spoofedUserAgent);
                            hasLoggedSpoofedUserAgent = true;  
                        }
                    }

                }
        );
    }
}
