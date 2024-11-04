package io.github.hiro.lime.hooks;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import dalvik.system.DexFile;
import io.github.hiro.lime.LimeOptions;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;

public class test implements IHook {

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        String packageName = loadPackageParam.packageName;

        XposedBridge.log("Hooking package: " + packageName);
        hookAllClasses(loadPackageParam.classLoader, loadPackageParam);
        hookFragmentOnCreateView(loadPackageParam.classLoader);
    }

    private void hookAllClasses(ClassLoader classLoader, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            String apkPath = loadPackageParam.appInfo.sourceDir;
            if (apkPath == null) {
                XposedBridge.log("Could not get APK path.");
                return;
            }

            DexFile dexFile = new DexFile(new File(apkPath));
            Enumeration<String> classNames = dexFile.entries();
            while (classNames.hasMoreElements()) {
                String className = classNames.nextElement();
                try {
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    hookAllMethods(clazz);
                } catch (ClassNotFoundException e) {
                  //  XposedBridge.log("Class not found: " + className);
                } catch (Throwable e) {
                   // XposedBridge.log("Error loading class " + className + ": " + e.getMessage());
                }
            }
        } catch (Throwable e) {
           // XposedBridge.log("Error while hooking classes: " + e.getMessage());
        }
    }
    private void hookFragmentOnCreateView(ClassLoader classLoader) {
        try {
            Class<?> fragmentClass = Class.forName("androidx.fragment.app.Fragment", false, classLoader);
            Method onCreateViewMethod = fragmentClass.getDeclaredMethod("onCreateView", LayoutInflater.class, ViewGroup.class, Bundle.class);
            XposedBridge.hookMethod(onCreateViewMethod, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ViewGroup rootView = (ViewGroup) param.getResult(); // ルートのViewGroupを取得

                    Context context = rootView.getContext();

                    int messageContainerId = getIdByName(context, "message_context_menu_content_container");
                    View messageContainer = rootView.findViewById(messageContainerId);

                    if (messageContainer instanceof ViewGroup) {
                        Button button = new Button(context);
                        button.setText("New Button");
                        button.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));

                        ((ViewGroup) messageContainer).addView(button);
                        XposedBridge.log("Button added to message_context_menu_content_container");

                        XposedBridge.log("Current child count: " + ((ViewGroup) messageContainer).getChildCount());
                    } else {
                        XposedBridge.log("messageContainer is not an instance of ViewGroup");
                    }

                }
            });
        } catch (ClassNotFoundException e) {
            XposedBridge.log("Class not found: androidx.fragment.app.Fragment");
        } catch (NoSuchMethodException e) {
            XposedBridge.log("Method not found: onCreateView in Fragment");
        } catch (Throwable e) {
            XposedBridge.log("Error hooking onCreateView: " + e.getMessage());
        }
    }

    private int getIdByName(Context context, String resourceName) {
        return context.getResources().getIdentifier(resourceName, "id", context.getPackageName());
    }


    private void hookAllMethods(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {


            if (java.lang.reflect.Modifier.isAbstract(method.getModifiers())) {
            //    XposedBridge.log("Skipping abstract method: " + method.getName() + " in class: " + clazz.getName());
                continue;
            }

            if (!isViewRelatedMethod(method)) {
                continue;
            }

            try {

                XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String methodName = method.getName();
                        String className = clazz.getName();

                        Object[] args = param.args;
                        StringBuilder argsString = new StringBuilder();
                        for (Object arg : args) {
                            argsString.append(arg).append(", ");
                        }
                        XposedBridge.log("Before calling: " + className + "." + methodName + " with args: " + argsString.toString());
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String methodName = method.getName();
                        String className = clazz.getName();
                        XposedBridge.log("Called: " + className + "." + methodName);
                    }
                });
            } catch (Throwable e) {
              //  XposedBridge.log("Error hooking method " + method.getName() + " in class " + clazz.getName() + ": " + e.getMessage());
            }
        }
    }

    private boolean isViewRelatedMethod(Method method) {
        String methodName = method.getName().toLowerCase();

        String[] viewRelatedTerms = {
                "view", "onclick", "setvisibility", "setenabled", "settext", "getview", "addview", "removeview"
        };


        for (String term : viewRelatedTerms) {
            if (methodName.contains(term)) {
                return true;
            }
        }

        return false;
    }

}
