package io.github.chipppppppppp.lime.hooks;

import android.graphics.Canvas;
import android.view.View;
import android.webkit.WebView;

import io.github.chipppppppppp.lime.LimeOptions;

public class RemoveAds implements IHook {
    static final String[] adClassNames = {
            "com.linecorp.line.ladsdk.ui.inventory.album.LadAlbumImageAdView",
            "com.linecorp.line.ladsdk.ui.inventory.home.LadHomeBigBannerImageAdView",
            "com.linecorp.line.ladsdk.ui.inventory.home.LadHomeBigBannerVideoAdView",
            "com.linecorp.line.ladsdk.ui.inventory.home.LadHomeImageAdView",
            "com.linecorp.line.ladsdk.ui.inventory.home.LadHomePerformanceAdView",
            "com.linecorp.line.ladsdk.ui.inventory.home.LadHomeYjBigBannerAdView",
            "com.linecorp.line.ladsdk.ui.inventory.home.LadHomeYjImageAdView",
            "com.linecorp.line.ladsdk.ui.inventory.openchat.LadOpenChatImageAdView",
            "com.linecorp.line.ladsdk.ui.inventory.timeline.post.LadPostAdView",
            "com.linecorp.line.ladsdk.ui.inventory.wallet.LadWalletBigBannerImageAdView",
            "com.linecorp.line.ladsdk.ui.inventory.wallet.LadWalletBigBannerVideoAdView",
            "com.linecorp.square.v2.view.ad.common.SquareCommonHeaderGoogleBannerAdView",
            "com.linecorp.square.v2.view.ad.common.SquareCommonHeaderGoogleNativeAdView",
    };

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!limeOptions.removeAds.checked) return;

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("org.apache.thrift.n"),
                "b",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String request = param.args[0].toString();
                        if (request.equals("getBanners") || request.equals("getPrefetchableBanners")) {
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
                        if (limeOptions.removeAds.checked) {
                            ((View) ((View) param.thisObject).getParent().getParent()).setVisibility(View.GONE);
                        }
                    }
                }
        );

        for (String adClassName : adClassNames) {
            XposedBridge.hookAllMethods(
                    loadPackageParam.classLoader.loadClass(adClassName),
                    "onAttachedToWindow",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            ((View) ((View) param.thisObject).getParent()).setVisibility(View.GONE);
                        }
                    }
            );
        }

        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass("k74.k"),
                "onPageFinished",
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
