package io.github.chipppppppppp.lime.hooks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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

                        View rootView = activity.findViewById(android.R.id.content);

                        logAllViews(rootView);

                        WebView webView = findWebView(rootView);
                        if (webView != null) {
                            String url = webView.getUrl();
                            if (url != null) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                activity.startActivity(intent);
                                webView.setVisibility(View.GONE);
                                webView.stopLoading();
                                activity.finish();
                            }
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

    private void logAllViews(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                logAllViews(group.getChildAt(i));
            }
        }
    }
}
