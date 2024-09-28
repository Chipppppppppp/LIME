package io.github.chipppppppppp.lime.hooks;

import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class RemoveAds implements IHook {
  
    static final List<String> adClassNames = new ArrayList<>(List.of(

    ));

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.removeAds.checked) return;


        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.REQUEST_HOOK.className),
                Constants.REQUEST_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String request = param.args[0].toString();
                        if (request.equals("getBanners") || request.equals("getPrefetchableBanners")) {
                            param.setResult(null);
                        }
                    }
                }
        );


        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass("com.linecorp.line.admolin.smartch.v2.view.SmartChannelViewLayout"),
                "dispatchDraw",
                Canvas.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        ((View) ((View) param.thisObject).getParent()).setVisibility(View.GONE);
                    }
                }
        );


        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass("com.linecorp.line.ladsdk.ui.common.view.lifecycle.LadAdView"),
                "onAttachedToWindow",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) ((View) param.thisObject).getParent().getParent();
                        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                        layoutParams.height = 0;
                        view.setLayoutParams(layoutParams);
                        view.setVisibility(View.GONE);
                    }
                }
        );

   
        XposedHelpers.findAndHookMethod(
                ViewGroup.class,
                "addView",
                View.class,
                ViewGroup.LayoutParams.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) param.args[0];
                        String className = view.getClass().getName();

                     
                        if (className.contains("Ad") ) {
                          

                     
                            if (!adClassNames.contains(className)) {
                                adClassNames.add(className);
                              
                            }

               
                            view.setVisibility(View.GONE);
                        }
                    }
                }
        );

 
        for (String adClassName : adClassNames) {
            XposedBridge.hookAllConstructors(
                    loadPackageParam.classLoader.loadClass(adClassName),
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            View view = (View) param.thisObject;
                            view.setVisibility(View.GONE);
                            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    if (view.getVisibility() != View.GONE) {
                                        view.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    }
            );
        }

    
        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass(Constants.WEBVIEW_CLIENT_HOOK.className),
                Constants.WEBVIEW_CLIENT_HOOK.methodName,
                WebView.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        WebView webView = (WebView) param.args[0];
                        webView.evaluateJavascript("(() => {\n" +
                                "    const observer = new MutationObserver(mutations => {\n" +
                                "        mutations.forEach(mutation => {\n" +
                                "            mutation.addedNodes.forEach(node => {\n" +
                                "                if (!node.querySelectorAll) return;\n" +
                                "                node.querySelectorAll('.ad_wrap, .lc__ad_root, .lc__ad_element').forEach(ad => ad.remove());\n" +
                                "            });\n" +
                                "        });\n" +
                                "    });\n" +
                                "    const config = {\n" +
                                "        childList: true,\n" +
                                "        subtree: true\n" +
                                "    };\n" +
                                "    observer.observe(document.body, config);\n" +
                                "})();", null);
                    }
                }
        );
    }
}
