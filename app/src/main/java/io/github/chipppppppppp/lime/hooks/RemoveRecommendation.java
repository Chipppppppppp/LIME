package io.github.chipppppppppp.lime.hooks;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class RemoveRecommendation implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("jp.naver.line.android.activity.homev2.view.HomeFragment"),
                "onViewCreated",
                new XC_MethodHook() {
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
                                    if (limeOptions.removeRecommendation.checked && child.getId() == recommendationResId) {
                                        child.setVisibility(View.GONE);
                                        ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
                                        layoutParams.height = 0;
                                        child.setLayoutParams(layoutParams);
                                    } else if (limeOptions.removeAds.checked && child instanceof ViewGroup) {
                                        ViewGroup childGroup = (ViewGroup) child;
                                        for (int j = 0; j < childGroup.getChildCount(); ++j) {
                                            int id = childGroup.getChildAt(j).getId();
                                            if (id == staticNotificationResId || id == rollingNotificationResId || id == hotDealContentResId) {
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
                }
        );
    }
}
