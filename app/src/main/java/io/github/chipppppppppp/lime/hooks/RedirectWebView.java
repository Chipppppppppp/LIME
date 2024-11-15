package io.github.chipppppppppp.lime.hooks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class RedirectWebView implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.redirectWebView.checked) return;

        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass("jp.naver.line.android.activity.iab.InAppBrowserActivity"),
                "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;
                        View rootView = activity.getWindow().getDecorView().getRootView();
                        WebView webView = findWebView(rootView);

                        if (webView != null) {
                            String currentUrl = webView.getUrl();

                            if (currentUrl != null && (
                                    currentUrl.startsWith("https://account-center.lylink.yahoo.co.jp") ||
                                    currentUrl.startsWith("https://access.line.me") ||
                                    currentUrl.startsWith("https://id.lylink.yahoo.co.jp/federation/ly/normal/callback/first") ||
                                    currentUrl.startsWith("https://liff.line.me") ||
                                    currentUrl.startsWith("https://lin.ee") ||
                                    currentUrl.startsWith("https://line.me/R/") ||
                                    currentUrl.startsWith("line://")
                                    )
                               ) {
                                return;
                            }
                            
                            webView.setVisibility(View.GONE);
                            webView.stopLoading();

                            Uri uri = Uri.parse(currentUrl);

                            if (limeOptions.openInBrowser.checked) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(uri);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
                }
        );
    }

    private WebView findWebView(View view) {
        if (view instanceof WebView) {
            return (WebView) view;
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                WebView result = findWebView(group.getChildAt(i));
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
