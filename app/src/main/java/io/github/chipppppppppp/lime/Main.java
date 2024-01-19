package io.github.chipppppppppp.lime;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Main implements IXposedHookLoadPackage {
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lparam) throws Throwable {
        if (!lparam.packageName.equals("jp.naver.line.android")) return;
        XSharedPreferences prefs = new XSharedPreferences("io.github.chipppppppppp.lime", "settings");
        prefs.reload();
        boolean deleteVoom = prefs.getBoolean("delete_voom", true);
        boolean deleteAds = prefs.getBoolean("delete_ads", true);
        boolean redirectWebView = prefs.getBoolean("redirect_web_view", true);
        boolean openInBrowser = prefs.getBoolean("open_in_browser", false);

        Class hookTarget;

        if (deleteVoom) {
            hookTarget = lparam.classLoader.loadClass("jp.naver.line.android.activity.main.MainActivity");
            XposedHelpers.findAndHookMethod(hookTarget, "onResume", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    int resourceId = activity.getResources().getIdentifier("main_tab_container", "id", activity.getPackageName());
                    ViewGroup vG = ((ViewGroup) activity.findViewById(resourceId));
                    View chat = vG.getChildAt(4); // chat
                    vG.getChildAt(5).setVisibility(View.GONE); // timeline spacer
                    vG.getChildAt(6).setVisibility(View.GONE); // timeline
                    View newsSpacer = vG.getChildAt(7); // news spacer

                    ConstraintLayout.LayoutParams paramChat = (ConstraintLayout.LayoutParams)chat.getLayoutParams();
                    paramChat.rightToLeft = vG.getChildAt(7).getId();
                    chat.setLayoutParams(paramChat);

                    ConstraintLayout.LayoutParams paramNewsSpacer = (ConstraintLayout.LayoutParams)newsSpacer.getLayoutParams();
                    paramNewsSpacer.leftToRight = chat.getId();
                    newsSpacer.setLayoutParams(paramNewsSpacer);
                }
            });
        }

        if (deleteAds) {
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.common.view.lifecycle.LadAdView");
            XposedHelpers.findAndHookMethod(hookTarget, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ViewGroup viewGroup = ((ViewGroup) ((View) param.thisObject).getParent().getParent().getParent());
                    viewGroup.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
                    layoutParams.height = 0;
                    viewGroup.setLayoutParams(layoutParams);
                }
            });
            XposedHelpers.findAndHookMethod(hookTarget, "onDrawForeground", Canvas.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ViewGroup viewGroup = ((ViewGroup) ((View) param.thisObject).getParent().getParent().getParent());
                    viewGroup.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
                    layoutParams.height = 0;
                    viewGroup.setLayoutParams(layoutParams);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.admolin.smartch.v2.view.SmartChannelViewLayout");
            XposedHelpers.findAndHookMethod(hookTarget, "dispatchDraw", Canvas.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) ((View) param.thisObject).getParent()).setVisibility(View.GONE);
                }
            });
        }

        if (redirectWebView) {
            hookTarget = lparam.classLoader.loadClass("android.webkit.WebView");
            XposedBridge.hookAllMethods(hookTarget, "loadUrl", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    WebView webView = (WebView) param.thisObject;
                    Activity activity = (Activity) webView.getContext();
                    webView.setVisibility(View.GONE);
                    webView.stopLoading();
                    Uri uri = Uri.parse((String) param.args[0]);
                    if (openInBrowser) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        activity.startActivity(intent);
                    } else {
                        CustomTabsIntent tabsIntent = new CustomTabsIntent.Builder().setShowTitle(true).build();
                        tabsIntent.launchUrl(activity, uri);
                    }
                    activity.finish();
                }
            });
        }
    }
}
