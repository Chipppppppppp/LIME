package io.github.chipppppppppp.lime.hooks;

import android.content.Context;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;
import io.github.chipppppppppp.lime.R;

public class KeepUnread implements IHook {

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (limeOptions.removeKeepUnread.checked) return;

        Class<?> hookTarget;
        hookTarget = loadPackageParam.classLoader.loadClass("jp.naver.line.android.common.view.listview.PopupListView");
        XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                ViewGroup viewGroup = (ViewGroup) param.thisObject;
                Context context = viewGroup.getContext();
                Context moduleContext = context.getApplicationContext().createPackageContext(Constants.MODULE_NAME, Context.CONTEXT_IGNORE_SECURITY);
                String textKeepUnread = moduleContext.getResources().getString(R.string.switch_keep_unread);
                RelativeLayout layout = new RelativeLayout(context);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                layout.setLayoutParams(layoutParams);

                Switch switchView = new Switch(context);
                switchView.setText(textKeepUnread); 
                RelativeLayout.LayoutParams switchParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                switchParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                switchView.setChecked(false);
                switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                });

                layout.addView(switchView, switchParams);

                ((ListView) viewGroup.getChildAt(0)).addFooterView(layout);
            }
        });
        
        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass(Constants.MARK_AS_READ_HOOK.className),
                Constants.MARK_AS_READ_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(null);
                    }
                }
        );
    }

}
