package io.github.chipppppppppp.lime.hooks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.view.View;
import android.webkit.WebView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class RedirectWebView implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.redirectWebView.checked) return;

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("jp.naver.line.android.activity.iab.InAppBrowserActivity"),
                "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;
                        int webViewResId = activity.getResources().getIdentifier("iab_webview", "id", activity.getPackageName());
                        WebView webView = (WebView) activity.findViewById(webViewResId);
                        webView.setVisibility(View.GONE);
                        webView.stopLoading();
                        Uri uri = Uri.parse(webView.getUrl());
                        if (limeOptions.openInBrowser.checked) {
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
                }
        );
    }
}
