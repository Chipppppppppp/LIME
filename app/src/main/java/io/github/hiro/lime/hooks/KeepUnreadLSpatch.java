package io.github.hiro.lime.hooks;

import static io.github.hiro.lime.Main.limeOptions;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class KeepUnreadLSpatch implements IHook {

    static boolean keepUnread = false;
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.KeepUnreadLSpatch.checked) return;

        XposedHelpers.findAndHookMethod(
                "com.linecorp.line.chatlist.view.fragment.ChatListFragment",
                loadPackageParam.classLoader,
                "onCreateView",
                LayoutInflater.class, ViewGroup.class, android.os.Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View rootView = (View) param.getResult();
                        Context context = rootView.getContext();

                        RelativeLayout layout = new RelativeLayout(context);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layout.setLayoutParams(layoutParams);


                        Switch switchView = new Switch(context);
                        switchView.setText("");
                        switchView.setTextColor(Color.WHITE);

                        RelativeLayout.LayoutParams switchParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        switchParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                        switchView.setChecked(false);

                        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            keepUnread = isChecked;
                        });

                        layout.addView(switchView, switchParams);

                        if (rootView instanceof ViewGroup) {
                            ViewGroup rootViewGroup = (ViewGroup) rootView;
                            if (rootViewGroup.getChildCount() > 0 && rootViewGroup.getChildAt(0) instanceof ListView) {
                                ListView listView = (ListView) rootViewGroup.getChildAt(0);
                                listView.addFooterView(layout);
                            } else {
                                rootViewGroup.addView(layout);
                            }
                        }
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass(Constants.MARK_AS_READ_HOOK.className),
                Constants.MARK_AS_READ_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (keepUnread) {
                            param.setResult(null);
                        }
                    }
                }
        );
    }
}



