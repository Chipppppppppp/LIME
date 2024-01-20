package io.github.chipppppppppp.lime;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import androidx.browser.customtabs.CustomTabsIntent;
import java.lang.reflect.*;

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
        boolean deleteWallet = prefs.getBoolean("delete_wallet", true);
        boolean deleteAds = prefs.getBoolean("delete_ads", true);
        boolean redirectWebView = prefs.getBoolean("redirect_webview", true);
        boolean openInBrowser = prefs.getBoolean("open_in_browser", false);

        Class hookTarget;

        if (deleteVoom || deleteWallet) {
            hookTarget = lparam.classLoader.loadClass("jp.naver.line.android.activity.main.MainActivity");
            XposedHelpers.findAndHookMethod(hookTarget, "onResume", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    if (deleteVoom) {
                        int timelineSpacerResId = activity.getResources().getIdentifier("bnb_timeline_spacer", "id", activity.getPackageName());
                        int timelineResId = activity.getResources().getIdentifier("bnb_timeline", "id", activity.getPackageName());
                        activity.findViewById(timelineSpacerResId).setVisibility(View.GONE);
                        activity.findViewById(timelineResId).setVisibility(View.GONE);
                    }
                    if (deleteWallet) {
                        int walletSpacerResId = activity.getResources().getIdentifier("bnb_wallet_spacer", "id", activity.getPackageName());
                        int walletResId = activity.getResources().getIdentifier("bnb_wallet", "id", activity.getPackageName());
                        activity.findViewById(walletSpacerResId).setVisibility(View.GONE);
                        activity.findViewById(walletResId).setVisibility(View.GONE);
                    }
                }
            });
        }

        if (deleteAds) {
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.common.view.lifecycle.LadAdView");
            XposedHelpers.findAndHookMethod(hookTarget, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    View view = ((View) ((View) param.thisObject).getParent().getParent().getParent());
                    view.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.height = 0;
                    view.setLayoutParams(layoutParams);
                }
            });
            XposedHelpers.findAndHookMethod(hookTarget, "onDrawForeground", Canvas.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View view = ((View) ((View) param.thisObject).getParent().getParent().getParent());
                    view.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.height = 0;
                    view.setLayoutParams(layoutParams);
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
                    if (!activity.getClass().getName().equals("jp.naver.line.android.activity.iab.InAppBrowserActivity")) return;
                    webView.setVisibility(View.GONE);
                    webView.stopLoading();
                    Uri uri = Uri.parse((String) param.args[0]);
                    if (openInBrowser) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        activity.startActivity(intent);
                    } else {
                        CustomTabsIntent tabsIntent = new CustomTabsIntent.Builder()
                                .setShowTitle(true)
                                .build();
                        tabsIntent.launchUrl(activity, uri);
                    }
                    activity.finish();
                }
            });
        }
    }
}
