package io.github.chipppppppppp.lime.hooks;

import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class RemoveAds implements IHook {
    static final Set<String> adClassNames = new HashSet<>(Arrays.asList(
            "com.google.android.gms.ads.nativead.NativeAdView",
            "com.linecorp.line.ladsdk.ui.inventory.album.LadAlbumImageAdView",
            "com.linecorp.line.ladsdk.ui.inventory.album.LadAlbumVideoAdView",
            "com.linecorp.line.ladsdk.ui.inventory.album.LadAlbumYjImageAdView",
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
            "com.linecorp.square.v2.view.ad.common.SquareCommonHeaderGoogleNativeAdView"
    ));

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.removeAds.checked) return;

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
                View.class,
                "onAttachedToWindow",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) param.thisObject;
                        if (adClassNames.contains(view.getClass().getName())) {
                            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                            layoutParams.height = 0;
                            view.setLayoutParams(layoutParams);
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
