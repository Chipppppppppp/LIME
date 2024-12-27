package io.github.hiro.lime.hooks;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import dalvik.system.DexFile;
import io.github.hiro.lime.LimeOptions;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;

public class test implements IHook {
    private boolean isButtonAdded = false; // ボタンが追加されたかどうかを追跡するフラグ
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        String packageName = loadPackageParam.packageName;

        XposedBridge.log("Hooking package: " + packageName);
 //  hookOnViewAdded(loadPackageParam.classLoader);
      hookAllClassesInPackage(loadPackageParam.classLoader, loadPackageParam);
      //hookFragmentOnCreateView(loadPackageParam.classLoader);
        //hookChatHistoryActivity(loadPackageParam.classLoader); // ChatHistoryActivityのフック
        //hookLongClickListeners(loadPackageParam.classLoader); // 長押しリスナーのフック



    }


    private void hookAllClassesInPackage(ClassLoader classLoader, XC_LoadPackage.LoadPackageParam loadPackageParam) {
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

                // 指定されたパッケージで始まるクラスのみをフック
              //  if (className.startsWith("com.linecorp.line") || className.startsWith("jp.naver.line.android")) {
                    try {
                        Class<?> clazz = Class.forName(className, false, classLoader);
                        hookAllMethods(clazz);
                    } catch (ClassNotFoundException e) {
                        XposedBridge.log("Class not found: " + className);
                    } catch (Throwable e) {
                        XposedBridge.log("Error loading class " + className + ": " + e.getMessage());
                    }
              //  }
            }
        } catch (Throwable e) {
            XposedBridge.log("Error while hooking classes: " + e.getMessage());
        }
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
                    XposedBridge.log("Class not found: " + className);
                } catch (Throwable e) {
                    XposedBridge.log("Error loading class " + className + ": " + e.getMessage());
                }
            }
        } catch (Throwable e) {
            XposedBridge.log("Error while hooking classes: " + e.getMessage());
        }
    }


    private void hookFragmentOnCreateView(ClassLoader classLoader) {
        try {
            Class<?> fragmentClass = Class.forName("androidx.fragment.app.Fragment", false, classLoader);
            Method onCreateViewMethod = fragmentClass.getDeclaredMethod("onCreateView", LayoutInflater.class, ViewGroup.class, Bundle.class);

            XposedBridge.hookMethod(onCreateViewMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("Before calling: " + fragmentClass.getName() + ".onCreateView");
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ViewGroup rootView = (ViewGroup) param.getResult(); // ルートのViewGroupを取得
                    Context context = rootView.getContext();

                    // IDによるビューの取得
                    int messageContainerId = getIdByName(context, "message_context_menu_content_container");
                    View messageContainer = rootView.findViewById(messageContainerId);

                    if (messageContainer != null) {
                        XposedBridge.log("messageContainer found: " + messageContainer.toString());
                    } else {
                        XposedBridge.log("messageContainer not found");
                    }

                    // 新しいビューの追加を監視
                    rootView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                        @Override
                        public void onChildViewAdded(View parent, View child) {
                            XposedBridge.log("Child view added: " + child.toString());
                        }

                        @Override
                        public void onChildViewRemoved(View parent, View child) {
                            XposedBridge.log("Child view removed: " + child.toString());
                        }
                    });
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


    private void hookOnViewAdded(ClassLoader classLoader) {
        try {
            Class<?> constraintLayoutClass = Class.forName("androidx.constraintlayout.widget.ConstraintLayout", false, classLoader);
            Method onViewAddedMethod = constraintLayoutClass.getDeclaredMethod("onViewAdded", View.class);

            XposedBridge.hookMethod(onViewAddedMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    View addedView = (View) param.args[0];
                    XposedBridge.log(addedView.toString());
                }

                private boolean isButtonAdded = false; // ボタンが追加されたかどうかを追跡するフラグ

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View addedView = (View) param.args[0];
                    XposedBridge.log("Called: " + constraintLayoutClass.getName() + ".onViewAdded");

                    // 追加されたビューの ID を取得
                    int addedViewId = addedView.getId();

                    // 親ビューを取得
                    ViewGroup parent = (ViewGroup) param.thisObject;

                    // 追加されたビューのリソース名を取得
                    String resourceName = parent.getContext().getResources().getResourceEntryName(addedViewId);

                    // リソース名が chat_ui_message_context_menu_row_container の場合
                    if ("chat_ui_message_context_menu_row_container".equals(resourceName) && !isButtonAdded) {
                        // ボタンを作成


                    }


                }


                private void createAndAddButton(ConstraintLayout parent, View referenceView) {
                    // ボタンを作成
                    Button newButton = new Button(parent.getContext());
                    newButton.setText("新しいボタン");

                    // ボタンの ID を設定
                    newButton.setId(View.generateViewId());

                    // ボタンのレイアウトパラメータを設定
                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                    );

                    // ボタンを親ビューに追加
                    parent.addView(newButton, params);

                    // 制約を設定
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(parent);

                    // ボタンに対する制約を設定
                    constraintSet.connect(newButton.getId(), ConstraintSet.TOP, referenceView.getId(), ConstraintSet.BOTTOM); // 上に参照ビューを設定
                    constraintSet.connect(newButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START); // 左端に設定
                    constraintSet.connect(newButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END); // 右端に設定
                    constraintSet.setHorizontalBias(newButton.getId(), 0.5f); // 中央に配置

                    // 制約を適用
                    constraintSet.applyTo(parent);
                }

                private void createAndAddButton(ViewGroup parent) {
                    // ボタンを作成
                    Button newButton = new Button(parent.getContext());
                    newButton.setText("新しいボタン"); // ボタンのテキストを設定

                    // ボタンのレイアウトパラメータを設定
                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                    );

                    // 親の一番右に追加するために、適切な位置を設定
                    params.startToEnd = parent.getChildAt(parent.getChildCount() - 1).getId(); // 最後のビューの右側に配置
                    params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID; // 上部を親の上部に固定

                    newButton.setLayoutParams(params);

                    // 親ビューにボタンを追加
                    parent.addView(newButton);
                }


            });
        } catch (ClassNotFoundException e) {
            XposedBridge.log("Class not found: androidx.constraintlayout.widget.ConstraintLayout");
        } catch (NoSuchMethodException e) {
            XposedBridge.log("Method not found: onViewAdded in ConstraintLayout");
        } catch (Throwable e) {
            XposedBridge.log("Error hooking onViewAdded: " + e.getMessage());
        }
    }



   private int getIdByName(Context context, String resourceName) {
        return context.getResources().getIdentifier(resourceName, "id", context.getPackageName());
    }

    private void hookAllMethods(Class<?> clazz) {
        // クラス内のすべてのメソッドを取得
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            // 抽象メソッドをスキップ
            if (java.lang.reflect.Modifier.isAbstract(method.getModifiers())) {
                continue;
            }

            // 対象メソッドが特定のビュー関連メソッドであるか確認
            if (!"invokeSuspend".equals(method.getName()) &&
                    !"run".equals(method.getName()) &&
                    !"setOnTouchListener".equals(method.getName()) &&

                    !"setAlpha".equals(method.getName()) &&
                    !"setEnabled".equals(method.getName()) &&
                    !"setFocusable".equals(method.getName()) &&
                    !"setBackgroundColor".equals(method.getName()) &&

                    !"setHintTextColor".equals(method.getName()) &&  // 新しく追加されたメソッド
                    !"onStart".equals(method.getName()) &&
                    !"setCompoundDrawables".equals(method.getName()) &&
                    !"getActivity".equals(method.getName()) &&  // PendingIntent method
                    !"setState".equals(method.getName())) {   // PendingIntent method
                continue;
            }

            method.setAccessible(true); // アクセス可能に設定

            try {
                // メソッドをフックする
                XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        StringBuilder argsString = new StringBuilder("Args: ");

                        // 引数が複数の場合、すべてを追加
                        for (int i = 0; i < param.args.length; i++) {
                            Object arg = param.args[i];
                            argsString.append("Arg[").append(i).append("]: ")
                                    .append(arg != null ? arg.toString() : "null")
                                    .append(", ");
                        }

// メソッドに応じたログ出力
                        if ("invokeSuspend".equals(method.getName())) {
                      XposedBridge.log("Before calling invokeSuspend in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("run".equals(method.getName())) {
                            XposedBridge.log("Before calling run in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setAlpha".equals(method.getName())) {
                            XposedBridge.log("Before calling setAlpha in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setEnabled".equals(method.getName())) {
                            XposedBridge.log("Before calling setEnabled in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setFocusable".equals(method.getName())) {
                            XposedBridge.log("Before calling setFocusable in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setOnClickListener".equals(method.getName())) {
                            XposedBridge.log("Before calling setOnClickListener in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setBackgroundColor".equals(method.getName())) {
                            XposedBridge.log("Before calling setBackgroundColor in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setPadding".equals(method.getName())) {
                            XposedBridge.log("Before calling setPadding in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setLayoutParams".equals(method.getName())) {
                            XposedBridge.log("Before calling setLayoutParams in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("invalidate".equals(method.getName())) {
                            XposedBridge.log("Before calling invalidate in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setText".equals(method.getName())) {
                            XposedBridge.log("Before calling setText in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setTextColor".equals(method.getName())) {
                            XposedBridge.log("Before calling setTextColor in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setHint".equals(method.getName())) {
                            XposedBridge.log("Before calling setHint in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setHintTextColor".equals(method.getName())) {
                            XposedBridge.log("Before calling setHintTextColor in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setCompoundDrawables".equals(method.getName())) {
                            XposedBridge.log("Before calling setCompoundDrawables in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("onStart".equals(method.getName())) {
                            XposedBridge.log("Before calling onStart in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("getActivity".equals(method.getName())) {
                            XposedBridge.log("Before calling getActivity in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("onViewAdded".equals(method.getName())) {
                            XposedBridge.log("Before calling onViewAdded in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("getService".equals(method.getName())) {
                            XposedBridge.log("Before calling getService in class: " + clazz.getName() + " with args: " + argsString);
                        } else if ("setState".equals(method.getName())) {
                            XposedBridge.log("Before setState invoke in class: " + clazz.getName() + " with args: " + argsString);
                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object result = param.getResult();
                        if ("invokeSuspend".equals(method.getName())) {
                      XposedBridge.log("Before calling invokeSuspend in class: " + clazz.getName() + (result != null ? result.toString() : "null"));
                        } else if ("run".equals(method.getName())) {
                            XposedBridge.log("After calling run in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setAlpha".equals(method.getName())) {
                            XposedBridge.log("After calling setAlpha in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setEnabled".equals(method.getName())) {
                            XposedBridge.log("After calling setEnabled in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setFocusable".equals(method.getName())) {
                            XposedBridge.log("After calling setFocusable in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setOnClickListener".equals(method.getName())) {
                            XposedBridge.log("After calling setOnClickListener in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setBackgroundColor".equals(method.getName())) {
                            XposedBridge.log("After calling setBackgroundColor in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setPadding".equals(method.getName())) {
                            XposedBridge.log("After calling setPadding in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setLayoutParams".equals(method.getName())) {
                            XposedBridge.log("After calling setLayoutParams in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("requestLayout".equals(method.getName())) {
                            XposedBridge.log("After calling requestLayout in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("invalidate".equals(method.getName())) {
                            XposedBridge.log("After calling invalidate in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setText".equals(method.getName())) {
                            XposedBridge.log("After calling setText in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setTextColor".equals(method.getName())) {
                            XposedBridge.log("After calling setTextColor in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setHint".equals(method.getName())) {
                            XposedBridge.log("After calling setHint in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setHintTextColor".equals(method.getName())) {
                            XposedBridge.log("After calling setHintTextColor in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setCompoundDrawables".equals(method.getName())) {
                            XposedBridge.log("After calling setCompoundDrawables in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("onStart".equals(method.getName())) {
                            XposedBridge.log("Before calling onStart in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("getActivity".equals(method.getName())) {
                            XposedBridge.log("After calling getActivity in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("onViewAdded".equals(method.getName())) {
                            XposedBridge.log("After calling onViewAdded in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("getService".equals(method.getName())) {
                            XposedBridge.log("After calling getService in class: " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        } else if ("setState".equals(method.getName())) {
                          XposedBridge.log("setState " + clazz.getName() + " with result: " + (result != null ? result.toString() : "null"));
                        }
                    }
                });
            } catch (IllegalArgumentException e) {
                XposedBridge.log("Error hooking method " + method.getName() + " in class " + clazz.getName() + " : " + e.getMessage());
            } catch (Throwable e) {
                XposedBridge.log("Unexpected error hooking method " + method.getName() + " in class " + clazz.getName() + " : " + Log.getStackTraceString(e));
            }
        }
    }

    private boolean isViewCreationMethod(Method method) {
        // View作成に関連するメソッドを検出
        String methodName = method.getName().toLowerCase();

        return methodName.contains("inflate") || methodName.contains("new") || methodName.contains("create");
    }
}
