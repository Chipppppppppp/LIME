package io.github.chipppppppppp.lime.hooks;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class RemoveFlexibleContents implements IHook {
    int recommendationResId, notificationNameResId, serviceNameResId;

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("jp.naver.line.android.activity.main.MainActivity"),
                "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Context context = (Context) param.thisObject;
                        recommendationResId = context.getResources().getIdentifier("home_tab_contents_recommendation_placement", "id", context.getPackageName());
                        notificationNameResId = context.getResources().getIdentifier("notification_hub_item_name", "id", context.getPackageName());
                        serviceNameResId = context.getResources().getIdentifier("home_tab_service_name", "id", context.getPackageName());
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
                        if (limeOptions.removeRecommendation.checked && view.getId() == recommendationResId
                            || limeOptions.removeServiceLabels.checked && view.getId() == serviceNameResId) {
                            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                            layoutParams.height = 0;
                            view.setLayoutParams(layoutParams);
                            view.setVisibility(View.GONE);
                        } else if (limeOptions.removePremiumRecommendation.checked && view.getId() == notificationNameResId) {
                            view = (View) view.getParent();
                            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                            layoutParams.height = 0;
                            view.setLayoutParams(layoutParams);
                            view.setVisibility(View.GONE);
                        }
                    }
                }
        );
    }
}
