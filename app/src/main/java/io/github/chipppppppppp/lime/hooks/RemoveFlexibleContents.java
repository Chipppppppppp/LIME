package io.github.chipppppppppp.lime.hooks;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class RemoveFlexibleContents implements IHook {
    int recommendationResId, serviceNameResId, notificationResId;
    int serviceRowContainerResId, serviceIconResId, serviceCarouselResId;
    int serviceTitleBackgroundResId, serviceTitleResId, serviceSeeMoreResId, serviceSeeMoreBadgeResId;

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("jp.naver.line.android.activity.main.MainActivity"),
                "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Context context = (Context) param.thisObject;
                        recommendationResId = context.getResources().getIdentifier("home_tab_contents_recommendation_placement", "id", context.getPackageName());
                        serviceNameResId = context.getResources().getIdentifier("home_tab_service_name", "id", context.getPackageName());
                        notificationResId = context.getResources().getIdentifier("notification_hub_row_rolling_view_group", "id", context.getPackageName());
                        serviceRowContainerResId = context.getResources().getIdentifier("service_row_container", "id", context.getPackageName());
                        serviceIconResId = context.getResources().getIdentifier("home_tab_service_icon", "id", context.getPackageName());
                        serviceCarouselResId = context.getResources().getIdentifier("home_tab_service_carousel", "id", context.getPackageName());
                        serviceTitleBackgroundResId = context.getResources().getIdentifier("home_tab_service_title_background", "id", context.getPackageName());
                        serviceTitleResId = context.getResources().getIdentifier("home_tab_service_title", "id", context.getPackageName());
                        serviceSeeMoreResId = context.getResources().getIdentifier("home_tab_service_see_more", "id", context.getPackageName());
                        serviceSeeMoreBadgeResId = context.getResources().getIdentifier("home_tab_service_see_more_badge", "id", context.getPackageName());
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
                        int viewId = view.getId();
                        //String resourceName = getResourceName(view.getContext(), viewId);
                        //XposedBridge.log("View ID: " + viewId + ", Resource Name: " + resourceName);

                        if (limeOptions.removeRecommendation.checked && viewId == recommendationResId
                                || limeOptions.removeServiceLabels.checked && viewId == serviceNameResId
                                || viewId == serviceRowContainerResId
                                || viewId == serviceIconResId
                                || viewId == serviceCarouselResId
                                || viewId == serviceTitleBackgroundResId
                                || viewId == serviceTitleResId
                                || viewId == serviceSeeMoreResId
                                || viewId == serviceSeeMoreBadgeResId) {
                            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                            layoutParams.height = 0;
                            view.setLayoutParams(layoutParams);
                            view.setVisibility(View.GONE);
                        } else if (viewId == notificationResId) {
                            ((View) view.getParent()).setVisibility(View.GONE);
                        }
                    }
                }
        );
    }

    /*
    private String getResourceName(Context context, int resourceId) {
        return context.getResources().getResourceEntryName(resourceId);
    }
    */
}
