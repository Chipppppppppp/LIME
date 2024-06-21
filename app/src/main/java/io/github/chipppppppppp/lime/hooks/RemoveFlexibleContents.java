package io.github.chipppppppppp.lime.hooks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class RemoveFlexibleContents implements IHook {
    int recommendationResId, serviceNameResId, notificationResId;

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
                        serviceNameResId = context.getResources().getIdentifier("home_tab_service_name", "id", context.getPackageName());
                        notificationResId = context.getResources().getIdentifier("notification_hub_row_rolling_view_group", "id", context.getPackageName());
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                View.class,
                "onAttachedToWindow",
                new XC_MethodHook() {
                    View view;
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        view = (View) param.thisObject;
                        if (limeOptions.removeRecommendation.checked && view.getId() == recommendationResId
                                || limeOptions.removeServiceLabels.checked && view.getId() == serviceNameResId) {
                            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                            layoutParams.height = 0;
                            view.setLayoutParams(layoutParams);
                            view.setVisibility(View.GONE);
                        } else if (view.getId() == notificationResId) {
                            ((View) view.getParent()).setVisibility(View.GONE);
                        }
                    }
                }
        );
    }
}
