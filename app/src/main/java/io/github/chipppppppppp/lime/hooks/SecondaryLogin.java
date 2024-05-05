package io.github.chipppppppppp.lime.hooks;

import android.os.Bundle;
import android.view.View;

import io.github.chipppppppppp.lime.LimeOptions;

public class SecondaryLogin implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!xPackagePrefs.getBoolean("android_secondary", false)) return;

        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass("com.linecorp.registration.ui.fragment.WelcomeFragment"),
                "onViewCreated",
                View.class,
                Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object k = param.thisObject.getClass().getDeclaredField("k").get(param.thisObject);
                        Object c = k.getClass().getDeclaredField("c").get(k);
                        View secondaryLogin = (View) c.getClass().getDeclaredField("c").get(c);
                        secondaryLogin.setVisibility(View.VISIBLE);
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass("ak1.c$c"),
                "b",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult("DESKTOPWIN");
                    }
                }
        );
    }
}
