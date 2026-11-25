package io.github.chipppppppppp.lime.hooks;

import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class RemoveAds implements IHook {

    static void hookAdFacade(XC_LoadPackage.LoadPackageParam lp) {
        try {
            Class<?> impl = lp.classLoader.loadClass("k03.b");
            XposedBridge.hookAllMethods(impl, "a", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(null);
                }
            });
            XposedBridge.hookAllMethods(impl, "b", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(null);
                }
            });
        } catch (Throwable ignored) {
            // Ad facade not present in this LINE build; other hooks stay intact.
        }
    }

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.removeAds.checked) return;
        hookAdFacade(loadPackageParam);

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.REQUEST_HOOK.className),
                Constants.REQUEST_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String request = param.args[0].toString();
                        // LINE 26.x serves home promotional banners through three RPCs; the
                        // original list covered only the first two, so campaign banners still
                        // rendered.
                        if (request.equals("getBanners")
                                || request.equals("getPrefetchableBanners")
                                || request.equals("getBannersWithInCampaignPeriod")) {
                            param.setResult(null);
                        }
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass("com.linecorp.line.admolin.smartch.v2.view.SmartChannelViewLayout"),
                "dispatchDraw",
                Canvas.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        ((View) ((View) param.thisObject).getParent()).setVisibility(View.GONE);
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.common.view.lifecycle.LadAdView"),
                "onAttachedToWindow",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) ((View) param.thisObject).getParent().getParent();
                        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                        layoutParams.height = 0;
                        view.setLayoutParams(layoutParams);
                        view.setVisibility(View.GONE);
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                ViewGroup.class,
                "addView",
                View.class,
                ViewGroup.LayoutParams.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) param.args[0];
                        String className = view.getClass().getName();
                        if (className.contains("Ad")) {
                            view.setVisibility(View.GONE);
                        }
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass(Constants.WEBVIEW_CLIENT_HOOK.className),
                Constants.WEBVIEW_CLIENT_HOOK.methodName,
                WebView.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        WebView webView = (WebView) param.args[0];
                        webView.evaluateJavascript("(() => {\n" +
                                "    const observer = new MutationObserver(mutations => {\n" +
                                "        mutations.forEach(mutation => {\n" +
                                "            mutation.addedNodes.forEach(node => {\n" +
                                "                if (!node.querySelectorAll) return;\n" +
                                "                node.querySelectorAll('.ad_wrap, .lc__ad_root, .lc__ad_element').forEach(ad => ad.remove());\n" +
                                "            });\n" +
                                "        });\n" +
                                "    });\n" +
                                "    const config = {\n" +
                                "        childList: true,\n" +
                                "        subtree: true\n" +
                                "    };\n" +
                                "    observer.observe(document.body, config);\n" +
                                "})();", null);
                    }
                }
        );
    }
}
