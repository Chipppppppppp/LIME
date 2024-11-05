package io.github.hiro.lime.hooks;

import android.content.Context;
import android.os.Bundle;
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

public class Test implements IHook {
    private boolean isButtonAdded = false; // ボタンが追加されたかどうかを追跡するフラグ
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        String packageName = loadPackageParam.packageName;

        XposedBridge.log("Hooking package: " + packageName);
      //  hookOnViewAdded(loadPackageParam.classLoader);
        //  hookAllClasses(loadPackageParam.classLoader, loadPackageParam);
       // hookFragmentOnCreateView(loadPackageParam.classLoader);
        //hookChatHistoryActivity(loadPackageParam.classLoader); // ChatHistoryActivityのフック
        //hookLongClickListeners(loadPackageParam.classLoader); // 長押しリスナーのフック
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




    // jp.naver.line.android.activity.chathistory.ChatHistoryActivityをフックするメソッドを追加
    private void hookChatHistoryActivity(ClassLoader classLoader) {
        try {
            Class<?> chatHistoryActivityClass = Class.forName("jp.naver.line.android.activity.chathistory.ChatHistoryActivity", false, classLoader);
            Method onCreateMethod = chatHistoryActivityClass.getDeclaredMethod("onCreate", Bundle.class);
            XposedBridge.hookMethod(onCreateMethod, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("ChatHistoryActivity onCreate called");
                    // 必要に応じて追加の処理を行う
                }
            });
        } catch (ClassNotFoundException e) {
            XposedBridge.log("Class not found: jp.naver.line.android.activity.chathistory.ChatHistoryActivity");
        } catch (NoSuchMethodException e) {
            XposedBridge.log("Method not found: onCreate in ChatHistoryActivity");
        } catch (Throwable e) {
            XposedBridge.log("Error hooking onCreate in ChatHistoryActivity: " + e.getMessage());
        }
    }

    // 長押しリスナーをフックするメソッドを追加
    private void hookLongClickListeners(ClassLoader classLoader) {
        try {
            // 長押しリスナーを持つビューのクラスを取得
            Class<?> viewClass = Class.forName("android.view.View", false, classLoader);
            Method setOnLongClickListenerMethod = viewClass.getDeclaredMethod("setOnLongClickListener", View.OnLongClickListener.class);
            XposedBridge.hookMethod(setOnLongClickListenerMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    View.OnLongClickListener listener = (View.OnLongClickListener) param.args[0];
                    XposedBridge.log("Setting OnLongClickListener: " + listener.getClass().getName());
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // 実際の長押しイベント処理のための追加処理が必要な場合
                    XposedBridge.log("OnLongClickListener set on view.");
                }
            });
        } catch (ClassNotFoundException e) {
            XposedBridge.log("Class not found: android.view.View");
        } catch (NoSuchMethodException e) {
            XposedBridge.log("Method not found: setOnLongClickListener in View");
        } catch (Throwable e) {
            XposedBridge.log("Error hooking setOnLongClickListener: " + e.getMessage());
        }
    }

    private int getIdByName(Context context, String resourceName) {
        return context.getResources().getIdentifier(resourceName, "id", context.getPackageName());
    }

    private void hookAllMethods(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (java.lang.reflect.Modifier.isAbstract(method.getModifiers())) {
                XposedBridge.log("Skipping abstract method: " + method.getName() + " in class: " + clazz.getName());
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

                        // View作成メソッドの出力
                        if (isViewCreationMethod(method)) {
                            XposedBridge.log("View creation method detected: " + className + "." + methodName);
                        }
                    }
                });
            } catch (Throwable e) {
                XposedBridge.log("Error hooking method " + method.getName() + " in class " + clazz.getName() + ": " + e.getMessage());
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

    private boolean isViewCreationMethod(Method method) {
        // View作成に関連するメソッドを検出
        String methodName = method.getName().toLowerCase();

        return methodName.contains("inflate") || methodName.contains("new") || methodName.contains("create");
    }
}
