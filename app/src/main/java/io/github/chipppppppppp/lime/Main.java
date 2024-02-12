package io.github.chipppppppppp.lime;

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import android.app.AndroidAppHelper;
import android.content.res.XModuleResources;
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

    public class LimeOptions {
        public class LimeOption {
            String name;
            public int id;
            public boolean checked;

            private LimeOption(String name, int id, boolean checked) {
                this.name = name;
                this.id = id;
                this.checked = checked;
            }
        }

        public LimeOption deleteVoom = new LimeOption("delete_voom", R.string.switch_delete_voom, true);
        public LimeOption deleteWallet = new LimeOption("delete_wallet", R.string.switch_delete_wallet, true);
        public LimeOption deleteNewsOrCall = new LimeOption("delete_news_or_call", R.string.switch_delete_news_or_call, true);
        public LimeOption distributeEvenly = new LimeOption("distribute_evenly", R.string.switch_distribute_evenly, true);
        public LimeOption deleteIconLabels = new LimeOption("delete_icon_labels", R.string.switch_delete_icon_labels, true);
        public LimeOption deleteAds = new LimeOption("delete_ads", R.string.switch_delete_ads, true);
        public LimeOption deleteRecommendation = new LimeOption("delete_recommendation", R.string.switch_delete_recommendation, true);
        public LimeOption deleteReplyMute = new LimeOption("delete_reply_mute", R.string.switch_delete_reply_mute, true);
        public LimeOption redirectWebView = new LimeOption("redirect_webview", R.string.switch_redirect_webview, true);
        public LimeOption openInBrowser = new LimeOption("open_in_browser", R.string.switch_open_in_browser, false);
        public LimeOption preventMarkAsRead = new LimeOption("prevent_mark_as_read", R.string.switch_prevent_mark_as_read, false);
        public LimeOption preventUnsendMessage = new LimeOption("prevent_unsend_message", R.string.switch_prevent_unsend_message, false);
        public static final int size = 12;

        LimeOption getByIndex(int idx) {
            switch (idx) {
                case 0:
                    return deleteVoom;
                case 1:
                    return deleteWallet;
                case 2:
                    return deleteNewsOrCall;
                case 3:
                    return distributeEvenly;
                case 4:
                    return deleteIconLabels;
                case 5:
                    return deleteAds;
                case 6:
                    return deleteRecommendation;
                case 7:
                    return deleteReplyMute;
                case 8:
                    return redirectWebView;
                case 9:
                    return openInBrowser;
                case 10:
                    return preventMarkAsRead;
                case 11:
                    return preventUnsendMessage;
                default:
                    throw new IllegalArgumentException("Invalid index: " + idx);
            }
        }
    }

    public LimeOptions limeOptions = new LimeOptions();

    private int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lparam) throws Throwable {
        if (!lparam.packageName.equals(PACKAGE)) return;

        XSharedPreferences xPrefs = new XSharedPreferences(PACKAGE, "io.github.chipppppppppp.lime-options");
        for (int i = 0; i < limeOptions.size; ++i) {
            LimeOptions.LimeOption option = limeOptions.getByIndex(i);
            option.checked = xPrefs.getBoolean(option.name, option.checked);
        }

        Class hookTarget;

        hookTarget = lparam.classLoader.loadClass("com.linecorp.line.settings.main.LineUserMainSettingsFragment");
        XposedBridge.hookAllMethods(hookTarget, "onViewCreated", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                ViewGroup viewGroup = ((ViewGroup) param.args[0]);
                Context context = viewGroup.getContext();
                Context moduleContext;
                SharedPreferences prefs;
                try {
                    moduleContext = AndroidAppHelper.currentApplication().createPackageContext("io.github.chipppppppppp.lime", Context.CONTEXT_IGNORE_SECURITY);
                    prefs = AndroidAppHelper.currentApplication().getSharedPreferences("io.github.chipppppppppp.lime-options", Context.MODE_PRIVATE);
                } catch (Exception e) {
                    XposedBridge.log(e.toString());
                    return;
                }

                FrameLayout frameLayout = new FrameLayout(context);
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                Button button = new Button(context);
                button.setText("LIME");
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.TOP | Gravity.END;
                layoutParams.rightMargin = dpToPx(10, context);
                layoutParams.topMargin = dpToPx(5, context);
                button.setLayoutParams(layoutParams);

                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle(moduleContext.getString(R.string.option))
                        .setCancelable(false);
                LinearLayout layout = new LinearLayout(context);
                layout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(dpToPx(20, context), dpToPx(20, context), dpToPx(20, context), dpToPx(20, context));

                Switch switchRedirectWebView = null;
                for (int i = 0; i < limeOptions.size; ++i) {
                    LimeOptions.LimeOption option = limeOptions.getByIndex(i);
                    String name = option.name;

                    Switch switchView = new Switch(context);
                    switchView.setText(moduleContext.getString(option.id));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.topMargin = dpToPx(20, context);
                    switchView.setLayoutParams(params);

                    switchView.setChecked(option.checked);
                    switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        option.checked = isChecked;
                        prefs.edit().putBoolean(name, isChecked).apply();
                    });

                    if (name == "redirect_webview") switchRedirectWebView = switchView;
                    else if (name == "open_in_browser") {
                        switchRedirectWebView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            limeOptions.redirectWebView.checked = isChecked;
                            prefs.edit().putBoolean("redirect_webview", isChecked).apply();
                            if (isChecked) switchView.setEnabled(true);
                            else {
                                switchView.setChecked(false);
                                switchView.setEnabled(false);
                            }
                        });
                        switchView.setEnabled(limeOptions.redirectWebView.checked);
                    }

                    layout.addView(switchView);
                }

                ScrollView scrollView = new ScrollView(context);
                scrollView.addView(layout);
                builder.setView(scrollView);

                builder.setPositiveButton(moduleContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context.getApplicationContext(), moduleContext.getString(R.string.need_restart), Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog dialog = builder.create();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.show();
                    }
                });

                frameLayout.addView(button);
                viewGroup.addView(frameLayout);
            }
        });

        hookTarget = lparam.classLoader.loadClass("jp.naver.line.android.activity.main.MainActivity");
        XposedHelpers.findAndHookMethod(hookTarget, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                if (limeOptions.deleteVoom.checked) {
                    int timelineResId = activity.getResources().getIdentifier("bnb_timeline", "id", activity.getPackageName());
                    activity.findViewById(timelineResId).setVisibility(View.GONE);
                    if (limeOptions.distributeEvenly.checked) {
                        int timelineSpacerResId = activity.getResources().getIdentifier("bnb_timeline_spacer", "id", activity.getPackageName());
                        activity.findViewById(timelineSpacerResId).setVisibility(View.GONE);
                    }
                }
                if (limeOptions.deleteWallet.checked) {
                    int walletResId = activity.getResources().getIdentifier("bnb_wallet", "id", activity.getPackageName());
                    activity.findViewById(walletResId).setVisibility(View.GONE);
                    if (limeOptions.distributeEvenly.checked) {
                        int walletSpacerResId = activity.getResources().getIdentifier("bnb_wallet_spacer", "id", activity.getPackageName());
                        activity.findViewById(walletSpacerResId).setVisibility(View.GONE);
                    }
                }
                if (limeOptions.deleteNewsOrCall.checked) {
                    int newsResId = activity.getResources().getIdentifier("bnb_news", "id", activity.getPackageName());
                    activity.findViewById(newsResId).setVisibility(View.GONE);
                    int callResId = activity.getResources().getIdentifier("bnb_call", "id", activity.getPackageName());
                    activity.findViewById(callResId).setVisibility(View.GONE);
                    if (limeOptions.distributeEvenly.checked) {
                        int newsSpacerResId = activity.getResources().getIdentifier("bnb_news_spacer", "id", activity.getPackageName());
                        activity.findViewById(newsSpacerResId).setVisibility(View.GONE);
                        int callSpacerResId = activity.getResources().getIdentifier("bnb_call_spacer", "id", activity.getPackageName());
                        activity.findViewById(callSpacerResId).setVisibility(View.GONE);
                    }
                }
            }
        });

        hookTarget = lparam.classLoader.loadClass("jp.naver.line.android.activity.main.bottomnavigationbar.BottomNavigationBarTextView");
        XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (limeOptions.deleteIconLabels.checked) ((View) param.thisObject).setVisibility(View.GONE);
            }
        });

        hookTarget = lparam.classLoader.loadClass("com.linecorp.line.admolin.smartch.v2.view.SmartChannelViewLayout");
        XposedHelpers.findAndHookMethod(hookTarget, "dispatchDraw", Canvas.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (limeOptions.deleteAds.checked) ((View) ((View) param.thisObject).getParent()).setVisibility(View.GONE);
            }
        });

        hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.common.view.lifecycle.LadAdView");
        XposedHelpers.findAndHookMethod(hookTarget, "onAttachedToWindow", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (limeOptions.deleteAds.checked) {
                    View view = (View) param.thisObject;
                    view.setVisibility(View.GONE);
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.height = 0;
                    view.setLayoutParams(layoutParams);

                    view = (View) ((View) param.thisObject).getParent();
                    view.setVisibility(View.GONE);
                    layoutParams = view.getLayoutParams();
                    layoutParams.height = 0;
                    view.setLayoutParams(layoutParams);

                    view = (View) ((View) param.thisObject).getParent().getParent();
                    view.setVisibility(View.GONE);
                    layoutParams = view.getLayoutParams();
                    layoutParams.height = 0;
                    view.setLayoutParams(layoutParams);

                    view = (View) ((View) param.thisObject).getParent().getParent().getParent();
                    view.setVisibility(View.GONE);
                    layoutParams = view.getLayoutParams();
                    layoutParams.height = 0;
                    view.setLayoutParams(layoutParams);
                }
            }
        });

        final String[] adClassNames = {
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
        for (String adClassName : adClassNames) {
            hookTarget = lparam.classLoader.loadClass(adClassName);
            XposedBridge.hookAllMethods(hookTarget, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (limeOptions.deleteAds.checked) {
                        View view = (View) param.thisObject;
                        view.setVisibility(View.GONE);
                        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                        layoutParams.height = 0;
                        view.setLayoutParams(layoutParams);

                        view = (View) ((View) param.thisObject).getParent();
                        view.setVisibility(View.GONE);
                        layoutParams = view.getLayoutParams();
                        layoutParams.height = 0;
                        view.setLayoutParams(layoutParams);
                    }
                }
            });
        }

        hookTarget = lparam.classLoader.loadClass("jp.naver.line.android.activity.homev2.view.HomeFragment");
        XposedBridge.hookAllMethods(hookTarget, "onViewCreated", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                ViewGroup view = (ViewGroup) param.args[0];
                Context context = view.getContext();
                int recyclerViewResId = context.getResources().getIdentifier("home_tab_recycler_view", "id", context.getPackageName());
                int recommendationResId = context.getResources().getIdentifier("home_tab_contents_recommendation_placement", "id", context.getPackageName());
                int staticNotificationResId = context.getResources().getIdentifier("notification_hub_row_static_view_group", "id", context.getPackageName());
                int rollingNotificationResId = context.getResources().getIdentifier("notification_hub_row_rolling_view_group", "id", context.getPackageName());
                ViewGroup recyclerView = view.findViewById(recyclerViewResId);
                recyclerView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
                            View child = recyclerView.getChildAt(i);
                            if (limeOptions.deleteRecommendation.checked && child.getId() == recommendationResId) {
                                child.setVisibility(View.GONE);
                                ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
                                layoutParams.height = 0;
                                child.setLayoutParams(layoutParams);
                            } else if (limeOptions.deleteAds.checked && child instanceof ViewGroup) {
                                ViewGroup childGroup = (ViewGroup) child;
                                for (int j = 0; j < childGroup.getChildCount(); ++j) {
                                    int id = childGroup.getChildAt(j).getId();
                                    if (id == staticNotificationResId || id == rollingNotificationResId) {
                                        child.setVisibility(View.GONE);
                                        ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
                                        layoutParams.height = 0;
                                        child.setLayoutParams(layoutParams);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });

        XposedHelpers.findAndHookMethod(Notification.Builder.class, "addAction", Notification.Action.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!limeOptions.deleteReplyMute.checked) return;
                Application app = AndroidAppHelper.currentApplication();
                Notification.Action a = (Notification.Action) param.args[0];
                String muteChatString = app.getString(app.getResources().getIdentifier("notification_button_mute", "string", app.getPackageName()));
                if (muteChatString.equals(a.title)) {
                    param.setResult(param.thisObject);
                }
            }
        });

        hookTarget = lparam.classLoader.loadClass("jp.naver.line.android.activity.iab.InAppBrowserActivity");
        XposedBridge.hookAllMethods(hookTarget, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!limeOptions.redirectWebView.checked) return;
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
        });

        XposedHelpers.findAndHookMethod(Notification.Builder.class, "addAction", Notification.Action.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (limeOptions.deleteReplyMute.checked) {
                    Application app = AndroidAppHelper.currentApplication();
                    Notification.Action a = (Notification.Action) param.args[0];
                    String muteChatString = app.getString(app.getResources().getIdentifier("notification_button_mute", "string", app.getPackageName()));
                    if (muteChatString.equals(a.title)) {
                        param.setResult(param.thisObject);
                    }
                }
            }
        });

        hookTarget = lparam.classLoader.loadClass("nl5.jo");
        XposedBridge.hookAllMethods(hookTarget, "write", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (limeOptions.preventMarkAsRead.checked) param.setResult(null);
            }
        });

        hookTarget = lparam.classLoader.loadClass("nl5.kd");
        final Method valueOf = hookTarget.getMethod("valueOf", String.class);
        final Object dummy = valueOf.invoke(null, "DUMMY");
        final Object notifiedDestroyMessage = valueOf.invoke(null, "NOTIFIED_DESTROY_MESSAGE");
        XposedHelpers.findAndHookMethod(hookTarget, "a", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (limeOptions.preventUnsendMessage.checked && param.getResult() == notifiedDestroyMessage) param.setResult(dummy);
            }
        });
        hookTarget = lparam.classLoader.loadClass("kk5.b");
        XposedHelpers.findAndHookMethod(hookTarget, "u", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (limeOptions.preventUnsendMessage.checked && param.getResult().equals("UNSENT")) param.setResult("");
            }
        });
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(PACKAGE)) return;

        if (limeOptions.deleteIconLabels.checked) {
            XModuleResources xModuleResources = XModuleResources.createInstance(MODULE_PATH, resparam.res);

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
