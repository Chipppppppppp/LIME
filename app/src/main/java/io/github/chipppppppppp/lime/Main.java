package io.github.chipppppppppp.lime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.XModuleResources;
import android.graphics.Canvas;
import android.net.Uri;
import android.view.View;
import android.webkit.WebView;

import androidx.browser.customtabs.CustomTabsIntent;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Main implements IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit {
    public String MODULE_PATH;
    public final String PACKAGE = "jp.naver.line.android";
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lparam) throws Throwable {
        if (!lparam.packageName.equals(PACKAGE)) return;
        XSharedPreferences prefs = new XSharedPreferences("io.github.chipppppppppp.lime", "settings");
        prefs.reload();
        boolean deleteVoom = prefs.getBoolean("delete_voom", true);
        boolean deleteWallet = prefs.getBoolean("delete_wallet", true);
        boolean distributeEvenly = prefs.getBoolean("distribute_evenly", true);
        boolean deleteIconLabels = prefs.getBoolean("delete_icon_labels", false);
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
                        if (distributeEvenly) activity.findViewById(timelineSpacerResId).setVisibility(View.GONE);
                        activity.findViewById(timelineResId).setVisibility(View.GONE);
                    }
                    if (deleteWallet) {
                        int walletSpacerResId = activity.getResources().getIdentifier("bnb_wallet_spacer", "id", activity.getPackageName());
                        int walletResId = activity.getResources().getIdentifier("bnb_wallet", "id", activity.getPackageName());
                        if (distributeEvenly) activity.findViewById(walletSpacerResId).setVisibility(View.GONE);
                        activity.findViewById(walletResId).setVisibility(View.GONE);
                    }
                }
            });
        }

        if (deleteIconLabels) {
            hookTarget = lparam.classLoader.loadClass("jp.naver.line.android.activity.main.bottomnavigationbar.BottomNavigationBarTextView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
        }

        if (deleteAds) {
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.admolin.smartch.v2.view.SmartChannelViewLayout");
            XposedHelpers.findAndHookMethod(hookTarget, "dispatchDraw", Canvas.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) ((View) param.thisObject).getParent()).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.common.view.lifecycle.LadAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.inventory.album.LadAlbumImageAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.inventory.home.LadHomeBigBannerImageAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.inventory.home.LadHomeBigBannerVideoAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.inventory.home.LadHomeImageAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.inventory.home.LadHomePerformanceAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.inventory.home.LadHomeYjBigBannerAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.inventory.home.LadHomeYjImageAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.inventory.openchat.LadOpenChatHeaderAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.inventory.openchat.LadOpenChatImageAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.inventory.timeline.post.LadPostAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.inventory.wallet.LadWalletBigBannerImageAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.inventory.wallet.LadWalletBigBannerVideoAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.square.v2.view.ad.common.SquareCommonHeaderGoogleBannerAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
            hookTarget = lparam.classLoader.loadClass("com.linecorp.square.v2.view.ad.common.SquareCommonHeaderGoogleNativeAdView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ((View) param.thisObject).setVisibility(View.GONE);
                }
            });
        }

        if (redirectWebView) {
            hookTarget = lparam.classLoader.loadClass("android.webkit.WebView");
            XposedBridge.hookAllMethods(hookTarget, "loadUrl", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    WebView webView = (WebView) param.thisObject;
                    Context context = webView.getContext();
                    if (!(context instanceof Activity)) return;
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

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(PACKAGE)) return;

        XModuleResources xModuleResources = XModuleResources.createInstance(MODULE_PATH, resparam.res);

        XSharedPreferences prefs = new XSharedPreferences("io.github.chipppppppppp.lime", "settings");
        prefs.reload();
        boolean deleteIconLabels = prefs.getBoolean("delete_icon_labels", false);

        if(deleteIconLabels) {
            resparam.res.setReplacement(PACKAGE, "dimen", "main_bnb_button_height", xModuleResources.fwd(R.dimen.main_bnb_button_height));
            resparam.res.setReplacement(PACKAGE, "dimen", "main_bnb_button_width", xModuleResources.fwd(R.dimen.main_bnb_button_width));
            resparam.res.hookLayout(PACKAGE, "layout", "app_main_bottom_navigation_bar_button", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) throws Throwable {
                    liparam.view.setTranslationY(xModuleResources.getDimensionPixelSize(R.dimen.gnav_icon_offset));
                }
            });
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }
}
