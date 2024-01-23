package io.github.chipppppppppp.lime;

import android.app.Activity;
import android.content.Intent;
import android.content.res.XModuleResources;
import android.graphics.Canvas;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

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
        boolean deleteRecommendation = prefs.getBoolean("delete_recommendation", true);
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
                        int timelineResId = activity.getResources().getIdentifier("bnb_timeline", "id", activity.getPackageName());
                        activity.findViewById(timelineResId).setVisibility(View.GONE);
                        if (distributeEvenly) {
                            int timelineSpacerResId = activity.getResources().getIdentifier("bnb_timeline_spacer", "id", activity.getPackageName());
                            activity.findViewById(timelineSpacerResId).setVisibility(View.GONE);
                        }
                    }
                    if (deleteWallet) {
                        int walletResId = activity.getResources().getIdentifier("bnb_wallet", "id", activity.getPackageName());
                        activity.findViewById(walletResId).setVisibility(View.GONE);
                        if (distributeEvenly) {
                            int walletSpacerResId = activity.getResources().getIdentifier("bnb_wallet_spacer", "id", activity.getPackageName());
                            activity.findViewById(walletSpacerResId).setVisibility(View.GONE);
                        }
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

            String[] adClassNames = {
                    "com.linecorp.line.ladsdk.ui.inventory.album.LadAlbumImageAdView",
                    "com.linecorp.line.ladsdk.ui.inventory.home.LadHomeBigBannerImageAdView",
                    "com.linecorp.line.ladsdk.ui.inventory.home.LadHomeBigBannerVideoAdView",
                    "com.linecorp.line.ladsdk.ui.inventory.home.LadHomeImageAdView",
                    "com.linecorp.line.ladsdk.ui.inventory.home.LadHomePerformanceAdView",
                    "com.linecorp.line.ladsdk.ui.inventory.home.LadHomeYjBigBannerAdView",
                    "com.linecorp.line.ladsdk.ui.inventory.home.LadHomeYjImageAdView",
                    "com.linecorp.line.ladsdk.ui.inventory.openchat.LadOpenChatHeaderAdView",
                    "com.linecorp.line.ladsdk.ui.inventory.openchat.LadOpenChatImageAdView",
                    "com.linecorp.line.ladsdk.ui.inventory.timeline.post.LadPostAdView",
                    "com.linecorp.line.ladsdk.ui.inventory.wallet.LadWalletBigBannerImageAdView",
                    "com.linecorp.line.ladsdk.ui.inventory.wallet.LadWalletBigBannerVideoAdView",
                    "com.linecorp.square.v2.view.ad.common.SquareCommonHeaderGoogleBannerAdView",
                    "com.linecorp.square.v2.view.ad.common.SquareCommonHeaderGoogleNativeAdView",
            };
            for (String adClassName : adClassNames) {
                hookTarget = lparam.classLoader.loadClass(adClassName);
                XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        ((View) param.thisObject).setVisibility(View.GONE);
                    }
                });
            }
        }

        if (deleteRecommendation) {
            hookTarget = lparam.classLoader.loadClass("android.widget.LinearLayout");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View view = (View) param.thisObject;
                    if (!(view.getContext() instanceof Activity)) return;
                    Activity activity = (Activity) view.getContext();
                    int recommendationResId = activity.getResources().getIdentifier("home_tab_contents_recommendation_placement", "id", activity.getPackageName());
                    if (view.getId() == recommendationResId) view.setVisibility(View.GONE);
                }
            });
        }

        if (redirectWebView) {
            hookTarget = lparam.classLoader.loadClass("jp.naver.line.android.activity.iab.InAppBrowserActivity");
            XposedBridge.hookAllMethods(hookTarget, "onResume", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    int webViewResId = activity.getResources().getIdentifier("iab_webview", "id", activity.getPackageName());
                    WebView webView = (WebView) activity.findViewById(webViewResId);
                    webView.setVisibility(View.GONE);
                    webView.stopLoading();
                    Uri uri = Uri.parse(webView.getUrl());
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
