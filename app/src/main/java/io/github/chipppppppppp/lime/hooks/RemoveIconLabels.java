package io.github.chipppppppppp.lime.hooks;

import android.view.View;

import io.github.chipppppppppp.lime.LimeOptions;

public class RemoveIconLabels implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!limeOptions.removeIconLabels.checked) return;

        XposedBridge.hookAllConstructors(
                loadPackageParam.classLoader.loadClass("jp.naver.line.android.activity.main.bottomnavigationbar.BottomNavigationBarTextView"),
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        ((View) param.thisObject).settVisibility(View.GONE);
                    }
                }
        );
    }
}
