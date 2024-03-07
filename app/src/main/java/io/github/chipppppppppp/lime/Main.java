package io.github.chipppppppppp.lime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
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
    public static final String PACKAGE = "jp.naver.line.android";
    public static final String MODULE = "io.github.chipppppppppp.lime";

    public static final String[] adClassNames = {
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

    XSharedPreferences xModulePrefs;
    XSharedPreferences xPackagePrefs;
    XSharedPreferences xPrefs;
    public LimeOptions limeOptions = new LimeOptions();
    public boolean keepUnread = false;

    public void handleLoadPackage(@NonNull XC_LoadPackage.LoadPackageParam lparam) throws Throwable {
        if (!lparam.packageName.equals(PACKAGE)) return;

        xModulePrefs = new XSharedPreferences(MODULE, "options");
        xPackagePrefs = new XSharedPreferences(PACKAGE, MODULE + "-options");
        if (xModulePrefs.getBoolean("unembed_options", false)) {
            xPrefs = xModulePrefs;
        } else {
            xPrefs = xPackagePrefs;
        }
        for (LimeOptions.Option option : limeOptions.options) {
            option.checked = xPrefs.getBoolean(option.name, option.checked);
        }

        Class<?> hookTarget;

        if (xPackagePrefs.getBoolean("spoof_android_id", false)) {
            XposedHelpers.findAndHookMethod(Settings.Secure.class, "getString", ContentResolver.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[1].toString().equals(Settings.Secure.ANDROID_ID)) {
                        param.setResult("0000000000000000");
                    }
                }
            });
        }

        if (xPackagePrefs.getBoolean("android_secondary", false)) {
            hookTarget = lparam.classLoader.loadClass("com.linecorp.registration.ui.fragment.WelcomeFragment");
            XposedHelpers.findAndHookMethod(hookTarget, "onViewCreated", android.view.View.class, android.os.Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object k = param.thisObject.getClass().getDeclaredField("k").get(param.thisObject);
                    Object c = k.getClass().getDeclaredField("c").get(k);
                    View secondaryLogin = (View) c.getClass().getDeclaredField("c").get(c);
                    secondaryLogin.setVisibility(View.VISIBLE);
                }
            });

            hookTarget = lparam.classLoader.loadClass("cj1.b$c");
            XposedHelpers.findAndHookMethod(hookTarget, "b", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult("ANDROIDSECONDARY");
                }
            });
        }

        hookTarget = lparam.classLoader.loadClass("com.linecorp.registration.ui.fragment.WelcomeFragment");
        XposedBridge.hookAllMethods(hookTarget, "onViewCreated", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ViewGroup viewGroup = (ViewGroup) ((ViewGroup) param.args[0]).getChildAt(0);
                Activity activity = (Activity) viewGroup.getContext();

                Method mAddAddAssertPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
                mAddAddAssertPath.setAccessible(true);
                mAddAddAssertPath.invoke(activity.getResources().getAssets(), MODULE_PATH);

                SharedPreferences prefs = activity.getSharedPreferences(MODULE + "-options", Context.MODE_PRIVATE);

                FrameLayout frameLayout = new FrameLayout(activity);
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                LinearLayout linearLayout = new LinearLayout(activity);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                layoutParams.topMargin = Utils.dpToPx(60, activity);
                linearLayout.setLayoutParams(layoutParams);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                Switch switchSpoofAndroidId = new Switch(activity);
                switchSpoofAndroidId.setText(R.string.switch_spoof_android_id);

                AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                        .setTitle(R.string.options_title)
                        .setCancelable(false);

                TextView textView = new TextView(activity);
                textView.setText(R.string.spoof_android_id_risk);
                textView.setPadding(Utils.dpToPx(20, activity), Utils.dpToPx(20, activity), Utils.dpToPx(20, activity), Utils.dpToPx(20, activity));
                builder.setView(textView);

                builder.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.restarting), Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }
                });

                AlertDialog dialog = builder.create();

                switchSpoofAndroidId.setChecked(prefs.getBoolean("spoof_android_id", false));
                switchSpoofAndroidId.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    prefs.edit().putBoolean("spoof_android_id", isChecked).apply();
                    if (isChecked) dialog.show();
                    else {
                        Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.restarting), Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }
                });

                Switch switchAndroidSecondary = new Switch(activity);
                switchAndroidSecondary.setText(R.string.switch_android_secondary);
                switchAndroidSecondary.setChecked(prefs.getBoolean("android_secondary", false));
                switchAndroidSecondary.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    prefs.edit().putBoolean("android_secondary", isChecked).apply();
                    Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.restarting), Toast.LENGTH_SHORT).show();
                    activity.finish();
                });

                linearLayout.addView(switchSpoofAndroidId);
                linearLayout.addView(switchAndroidSecondary);
                frameLayout.addView(linearLayout);
                viewGroup.addView(frameLayout);
            }
        });

        if (!xModulePrefs.getBoolean("unembed_options", false)) {
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.settings.main.LineUserMainSettingsFragment");
            XposedBridge.hookAllMethods(hookTarget, "onViewCreated", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ViewGroup viewGroup = ((ViewGroup) param.args[0]);
                    Context context = viewGroup.getContext();

                    Method mAddAddAssertPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
                    mAddAddAssertPath.setAccessible(true);
                    mAddAddAssertPath.invoke(context.getResources().getAssets(), MODULE_PATH);

                    SharedPreferences prefs = context.getSharedPreferences(MODULE + "-options", Context.MODE_PRIVATE);

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
                    layoutParams.rightMargin = Utils.dpToPx(10, context);
                    layoutParams.topMargin = Utils.dpToPx(5, context);
                    button.setLayoutParams(layoutParams);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setTitle(R.string.options_title)
                            .setCancelable(false);

                    LinearLayout layout = new LinearLayout(context);
                    layout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setPadding(Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context));

                    Switch switchRedirectWebView = null;
                    for (LimeOptions.Option option : limeOptions.options) {
                        final String name = option.name;

                        Switch switchView = new Switch(context);
                        switchView.setText(option.id);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.topMargin = Utils.dpToPx(20, context);
                        switchView.setLayoutParams(params);

                        switchView.setChecked(option.checked);
                        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            prefs.edit().putBoolean(name, isChecked).apply();
                        });

                        if (name == "redirect_webview") switchRedirectWebView = switchView;
                        else if (name == "open_in_browser") {
                            switchRedirectWebView.setOnCheckedChangeListener((buttonView, isChecked) -> {
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

                    builder.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean optionChanged = false;
                            for (LimeOptions.Option option : limeOptions.options) {
                                if (option.checked != prefs.getBoolean(option.name, option.checked))
                                    optionChanged = true;
                            }

                            if (optionChanged) {
                                Toast.makeText(context.getApplicationContext(), context.getString(R.string.restarting), Toast.LENGTH_SHORT).show();
                                Process.killProcess(Process.myPid());
                                context.startActivity(new Intent().setClassName(PACKAGE, "jp.naver.line.android.activity.SplashActivity"));
                            }
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
        }

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

        if (limeOptions.deleteIconLabels.checked) {
            hookTarget = lparam.classLoader.loadClass("jp.naver.line.android.activity.main.bottomnavigationbar.BottomNavigationBarTextView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Utils.hideView((View) param.thisObject);
                }
            });
        }

        if (limeOptions.deleteAds.checked) {
            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.admolin.smartch.v2.view.SmartChannelViewLayout");
            XposedHelpers.findAndHookMethod(hookTarget, "dispatchDraw", Canvas.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Utils.hideView((View) ((View) param.thisObject).getParent());
                }
            });

            hookTarget = lparam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.common.view.lifecycle.LadAdView");
            XposedHelpers.findAndHookMethod(hookTarget, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (limeOptions.deleteAds.checked) {
                        View view = (View) param.thisObject;
                        Utils.hideView(view);

                        view = (View) view.getParent();
                        Utils.hideView(view);

                        view = (View) view.getParent();
                        Utils.hideView(view);
                    }
                }
            });

            for (String adClassName : adClassNames) {
                hookTarget = lparam.classLoader.loadClass(adClassName);
                XposedBridge.hookAllMethods(hookTarget, "onAttachedToWindow", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) param.thisObject;
                        Utils.hideView(view);

                        view = (View) view.getParent();
                        Utils.hideView(view);
                    }
                });
            }
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
                int hotDealContentResId = context.getResources().getIdentifier("hot_deal_content_recycler_view", "id", context.getPackageName());

                ViewGroup recyclerView = view.findViewById(recyclerViewResId);
                recyclerView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
                            View child = recyclerView.getChildAt(i);
                            if (limeOptions.deleteRecommendation.checked && child.getId() == recommendationResId) {
                                Utils.hideView(child);
                                ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
                                layoutParams.height = 0;
                                child.setLayoutParams(layoutParams);
                            } else if (limeOptions.deleteAds.checked && child instanceof ViewGroup) {
                                ViewGroup childGroup = (ViewGroup) child;
                                for (int j = 0; j < childGroup.getChildCount(); ++j) {
                                    int id = childGroup.getChildAt(j).getId();
                                    if (id == staticNotificationResId || id == rollingNotificationResId || id == hotDealContentResId) {
                                        Utils.hideView(child);
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

        if (limeOptions.deleteReplyMute.checked) {
            XposedHelpers.findAndHookMethod(Notification.Builder.class, "addAction", Notification.Action.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Application app = AndroidAppHelper.currentApplication();
                    Notification.Action a = (Notification.Action) param.args[0];
                    String muteChatString = app.getString(app.getResources().getIdentifier("notification_button_mute", "string", app.getPackageName()));
                    if (muteChatString.equals(a.title)) {
                        param.setResult(param.thisObject);
                    }
                }
            });
        }

        if (limeOptions.redirectWebView.checked) {
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
        }

        if (limeOptions.deleteReplyMute.checked) {
            XposedHelpers.findAndHookMethod(Notification.Builder.class, "addAction", Notification.Action.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Application app = AndroidAppHelper.currentApplication();
                    Notification.Action a = (Notification.Action) param.args[0];
                    String muteChatString = app.getString(app.getResources().getIdentifier("notification_button_mute", "string", app.getPackageName()));
                    if (muteChatString.equals(a.title)) param.setResult(param.thisObject);
                }
            });
        }

        if (limeOptions.preventMarkAsRead.checked) {
            hookTarget = lparam.classLoader.loadClass("org.apache.thrift.n");
            XposedBridge.hookAllMethods(hookTarget, "b", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[0].toString().equals("sendChatChecked")) {
                        param.setResult(null);
                    }
                }
            });
        }

        if (limeOptions.preventUnsendMessage.checked) {
            hookTarget = lparam.classLoader.loadClass("tn5.id");
            final Object dummy = Enum.valueOf((Class<Enum>) hookTarget, "DUMMY");
            final Object notifiedDestroyMessage = Enum.valueOf((Class<Enum>) hookTarget, "NOTIFIED_DESTROY_MESSAGE");

            hookTarget = lparam.classLoader.loadClass("tn5.jd");
            XposedBridge.hookAllMethods(hookTarget, "read", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Field type = param.thisObject.getClass().getDeclaredField("d");
                    if (type.get(param.thisObject) == notifiedDestroyMessage) {
                        type.set(param.thisObject, dummy);
                    }
                }
            });

            hookTarget = lparam.classLoader.loadClass("om5.c");
            XposedHelpers.findAndHookMethod(hookTarget, "u", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.getResult().equals("UNSENT")) param.setResult("");
                }
            });
        }

        if (limeOptions.sendMuteMessage.checked) {
            hookTarget = lparam.classLoader.loadClass("dj5.b");
            XposedHelpers.findAndHookMethod(hookTarget, "H", "og5.f", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = Enum.valueOf((Class<Enum>) param.args[0].getClass(), "TO_BE_SENT_SILENTLY");
                }
            });
        }

        if (!limeOptions.deleteKeepUnread.checked) {
            hookTarget = lparam.classLoader.loadClass("jp.naver.line.android.common.view.listview.PopupListView");
            XposedBridge.hookAllConstructors(hookTarget, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ViewGroup viewGroup = (ViewGroup) param.thisObject;
                    Context context = viewGroup.getContext();

                    RelativeLayout layout = new RelativeLayout(context);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    layout.setLayoutParams(layoutParams);

                    Switch switchView = new Switch(context);
                    RelativeLayout.LayoutParams switchParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    switchParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                    switchView.setChecked(false);
                    switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        keepUnread = isChecked;
                    });

                    layout.addView(switchView, switchParams);

                    ((ListView) viewGroup.getChildAt(0)).addFooterView(layout);
                }
            });

            hookTarget = lparam.classLoader.loadClass("bd1.d$d");
            XposedHelpers.findAndHookMethod(hookTarget, "run", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (keepUnread) param.setResult(null);
                }
            });
        }

        if (limeOptions.blockTracking.checked) {
            hookTarget = lparam.classLoader.loadClass("org.apache.thrift.n");
            XposedBridge.hookAllMethods(hookTarget, "b", new XC_MethodHook() {
                private static final Set<String> requests = new HashSet<>(Arrays.asList(
                        "getBanners",
                        "getHomeFlexContent",
                        "getPrefetchableBanners",
                        "pushRecvReports",
                        "reportDeviceState",
                        "reportLocation",
                        "reportNetworkStatus"
                ));

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (requests.contains(param.args[0].toString())) {
                        param.setResult(null);
                    }
                }
            });
        }

        if (limeOptions.unlockThemes.checked) {
            hookTarget = lparam.classLoader.loadClass("sn5.d2");
            XposedBridge.hookAllMethods(hookTarget, "read", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.thisObject.getClass().getDeclaredField("r").set(param.thisObject, true);
                }
            });

            hookTarget = lparam.classLoader.loadClass("gf4.a2");
            XposedBridge.hookAllMethods(hookTarget, "read", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.thisObject.getClass().getDeclaredField("a").set(param.thisObject, null);
                }
            });

            hookTarget = lparam.classLoader.loadClass("sn5.q2");
            XposedBridge.hookAllMethods(hookTarget, "read", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.thisObject.getClass().getDeclaredField("a").set(param.thisObject, true);
                }
            });
        }
    }

    @Override
    public void handleInitPackageResources(@NonNull XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
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
    public void initZygote(@NonNull StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }
}
