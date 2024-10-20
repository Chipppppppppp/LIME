package io.github.hiro.lime.hooks;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

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

                        recommendationResId = getIdByName(context, "home_tab_contents_recommendation_placement");
                        serviceNameResId = getIdByName(context, "home_tab_service_name");
                        notificationResId = getIdByName(context, "notification_hub_row_rolling_view_group");
                        serviceRowContainerResId = getIdByName(context, "service_row_container");
                        serviceIconResId = getIdByName(context, "home_tab_service_icon");
                        serviceCarouselResId = getIdByName(context, "home_tab_service_carousel");
                        serviceTitleBackgroundResId = getIdByName(context, "home_tab_service_title_background");
                        serviceTitleResId = getIdByName(context, "home_tab_service_title");
                        serviceSeeMoreResId = getIdByName(context, "home_tab_service_see_more");
                        serviceSeeMoreBadgeResId = getIdByName(context, "home_tab_service_see_more_badge");
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                View.class,
                "onAttachedToWindow",
                new XC_MethodHook() {
                    View view;

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        view = (View) param.thisObject;


                        int viewId = view.getId();
                      //  String resourceName = getResourceName(view.getContext(), viewId);
                      //  XposedBridge.log("View ID: " + viewId + ", Resource Name: " + resourceName);

                        if (limeOptions.removeRecommendation.checked && viewId == recommendationResId
                                || limeOptions.removeServiceLabels.checked && viewId == serviceNameResId
                                || limeOptions.removeAllServices.checked && (viewId == serviceRowContainerResId
                                || viewId == serviceIconResId
                                || viewId == serviceCarouselResId
                                || viewId == serviceTitleBackgroundResId
                                || viewId == serviceTitleResId
                                || viewId == serviceSeeMoreResId
                                || viewId == serviceSeeMoreBadgeResId)) {
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

    private int getIdByName(Context context, String resourceName) {
        return context.getResources().getIdentifier(resourceName, "id", context.getPackageName());
    }

    private String getResourceName(Context context, int resourceId) {
        return context.getResources().getResourceEntryName(resourceId);
    }
}
