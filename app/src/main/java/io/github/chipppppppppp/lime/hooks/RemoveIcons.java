package io.github.chipppppppppp.lime.hooks;

import android.app.Activity;
import android.view.View;

import io.github.chipppppppppp.lime.LimeOptions;

public class RemoveIcons implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass("jp.naver.line.android.activity.main.MainActivity"),
                "onResume",
                new XC_MethodHook() {
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
                }
        );
    }
}
