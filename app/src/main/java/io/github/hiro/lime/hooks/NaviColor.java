package io.github.hiro.lime.hooks;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class NaviColor implements IHook {

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!limeOptions.NaviColor.checked) return;

        XposedBridge.hookAllMethods(Activity.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = activity.getWindow();
                    window.setNavigationBarColor(Color.BLACK);
                }
            }
        });
        // MainActivity の onResume メソッドをフック
        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass("jp.naver.line.android.activity.main.MainActivity"),
                "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;

                        // アクティビティ内のすべてのビューを探索
                        View rootView = activity.getWindow().getDecorView().getRootView();
                        printViewResourceNames(rootView);
                    }

                });


        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass("jp.naver.line.android.activity.main.MainActivity"),
                "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;

                        // Define the view IDs that need to be checked
                        String[] viewIds = {

                        };

                        // Loop through each view ID and apply the background color change
                        for (String viewId : viewIds) {
                            // Get the resource ID for each view
                            int resId = activity.getResources().getIdentifier(viewId, "id", activity.getPackageName());
                            View view = activity.findViewById(resId);

                            // Log the resource ID to verify the view is found
                            if (resId != 0) {
                                //XposedBridge.log("Resource ID for " + viewId + ": " + resId);
                            } else {
                                //XposedBridge.log("Resource ID for " + viewId + " not found.");
                            }

                            // Log the name of the view dynamically
                            if (view != null) {
                                String viewName = getResourceName(activity, resId); // Get the resource name
                                //XposedBridge.log("Found view: " + viewName);

                                // Check the current background color and apply the color filter if it's #111111
                                if (view.getBackground() != null) {
                                    ColorDrawable background = (ColorDrawable) view.getBackground();
                                    int currentColor = background.getColor();

                                    // If the current background color is #111111, change it to #000000
                                    if (currentColor == Color.parseColor("#111111")) {
                                        view.setBackgroundColor(Color.parseColor("#000000"));
                                        //XposedBridge.log("Changed background color of view to #000000");
                                    }
                                }
                            }
                        }

                        // Extend clickable area for main tab container
                        int mainTabContainerResId = activity.getResources().getIdentifier("main_tab_container", "id", activity.getPackageName());
                        ViewGroup mainTabContainer = activity.findViewById(mainTabContainerResId);

                        if (mainTabContainer != null) {
                            for (int i = 2; i < mainTabContainer.getChildCount(); i += 2) {
                                ViewGroup icon = (ViewGroup) mainTabContainer.getChildAt(i);
                                ViewGroup.LayoutParams layoutParams = icon.getLayoutParams();

                                // Save the current height and modify only the width
                                int currentHeight = layoutParams.height;
                                // Set the width of the icon to 0 (collapse it)
                                layoutParams.width = 0;
                                layoutParams.height = currentHeight; // Ensure the height remains unchanged
                                icon.setLayoutParams(layoutParams);

                                View clickableArea = icon.getChildAt(icon.getChildCount() - 1);

                                // Optionally, set a margin to ensure there is space around the icon
                                if (icon != null) {
                                    ViewGroup.MarginLayoutParams iconLayoutParams = (ViewGroup.MarginLayoutParams) icon.getLayoutParams();
                                    iconLayoutParams.setMargins(0, 0, 0, 0); // Reset margins to prevent overlap
                                    icon.setLayoutParams(iconLayoutParams);
                                }
                            }
                        }
                    }

                    // Helper method to get resource name
                    private String getResourceName(Activity activity, int resId) {
                        try {
                            // Ensure the resource name is correctly retrieved
                            String resourceName = activity.getResources().getResourceName(resId);
                            if (resourceName != null) {
                                return resourceName;
                            } else {
                                //XposedBridge.log("Resource name for ID " + resId + " is null.");
                                return "Unknown";
                            }
                        } catch (Resources.NotFoundException e) {
                            //XposedBridge.log("Resource not found for ID " + resId);
                            return "Unknown";
                        }
                    }
                }
        );




        XposedHelpers.findAndHookMethod("android.view.View", loadPackageParam.classLoader, "onMeasure",
                int.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) param.thisObject;
                        checkAndChangeBackgroundColor(view);  // 色変更処理
                    }
                });
        XposedHelpers.findAndHookMethod("android.view.View", loadPackageParam.classLoader, "setLayoutParams",
                ViewGroup.LayoutParams.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) param.thisObject;
                        checkAndChangeBackgroundColor(view);  // 色変更処理
                    }
                });

        XposedHelpers.findAndHookMethod("android.app.Activity", loadPackageParam.classLoader, "onResume", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;

                // アクティビティ内の全てのビューを取得
                View rootView = activity.findViewById(android.R.id.content);
                traverseAndChangeBackground(rootView);
            }

            // 全ての子ビューを再帰的に走査し、背景色を変更
            // 全ての子ビューを再帰的に走査し、背景色を変更
            private void traverseAndChangeBackground(View view) {
                if (view == null) return;

                try {
                    // リソース名を取得
                    String resourceName = view.getResources().getResourceEntryName(view.getId());
                    //XposedBridge.log("onResume - View Resource Name: " + resourceName);

                    // 背景色の変更処理
                    Drawable background = view.getBackground();
                    if (background instanceof ColorDrawable) {
                        int bgColor = ((ColorDrawable) background).getColor();
                        String hexColor = String.format("#%06X", (0xFFFFFF & bgColor)); // 色を16進数形式に変換

                        // 色が #111111 の場合、#FFFFFF に変更
                        if (hexColor.equals("#111111")) {
                            ((ColorDrawable) background).setColor(Color.parseColor("#FFFFFF"));
                            //XposedBridge.log("Changed Background Color for Resource Name: " + resourceName + " from #111111 to #FFFFFF");
                        }
                        // 色が #1F1F1F の場合、#000000 に変更
                        else if (hexColor.equals("#1F1F1F")) {
                            ((ColorDrawable) background).setColor(Color.parseColor("#000000"));
                            //XposedBridge.log("Changed Background Color for Resource Name: " + resourceName + " from #1F1F1F to #000000");
                        }
                    }

                    // 子ビューがあれば再帰的に処理
                    if (view instanceof ViewGroup) {
                        ViewGroup viewGroup = (ViewGroup) view;
                        for (int i = 0; i < viewGroup.getChildCount(); i++) {
                            traverseAndChangeBackground(viewGroup.getChildAt(i));
                        }
                    }

                } catch (Resources.NotFoundException e) {
                    //XposedBridge.log("onResume - Resource name not found for View ID: " + view.getId());
                }
            }

        });


        XposedHelpers.findAndHookMethod("android.view.View", loadPackageParam.classLoader, "setBackgroundResource", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int resId = (int) param.args[0];
                try {
                    // param.thisObject を View にキャストしてから getResources() を呼び出す
                    View view = (View) param.thisObject;
                    String resourceName = view.getResources().getResourceEntryName(resId);
                    //XposedBridge.log("setBackgroundResource - Resource Name: " + resourceName);

                    // 背景リソースを取得
                    Drawable background = view.getBackground();
                    if (background instanceof ColorDrawable) {
                        int bgColor = ((ColorDrawable) background).getColor();
                        String hexColor = String.format("#%06X", (0xFFFFFF & bgColor)); // 色を16進数形式に変換

                        // 色が #111111 の場合、#FFFFFF に変更
                        if (hexColor.equals("#111111")) {
                            ((ColorDrawable) background).setColor(Color.parseColor("#FFFFFF"));
                            //XposedBridge.log("Changed Background Color for Resource Name: " + resourceName + " from #111111 to #FFFFFF");
                        }
                        // 色が #1F1F1F の場合、#000000 に変更
                        else if (hexColor.equals("#1F1F1F")) {
                            ((ColorDrawable) background).setColor(Color.parseColor("#000000"));
                            //XposedBridge.log("Changed Background Color for Resource Name: " + resourceName + " from #1F1F1F to #000000");
                        }
                    }
                } catch (Resources.NotFoundException e) {
                    //XposedBridge.log("setBackgroundResource - Resource name not found for Resource ID: " + resId);
                }
            }
        });



        XposedHelpers.findAndHookMethod("android.app.Activity", loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                Window window = activity.getWindow();

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.BLACK);
                }
            }
        });

        XposedHelpers.findAndHookMethod(
                "android.view.View",
                loadPackageParam.classLoader,
                "onAttachedToWindow",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) param.thisObject;

                        try {
                            String resourceName = getViewResourceName(view);
                            //XposedBridge.log("Resource Name: " + resourceName);

                            // 背景色を変更
                            handleBackgroundColor(view, resourceName);

                            // TextViewの場合のテキスト色変更
                            if (view instanceof TextView) {
                                handleTextColor((TextView) view, resourceName);
                            }

                            // ボタンやその他のカスタムビューもチェック
                            if (view instanceof Button) {
                                handleBackgroundColor(view, resourceName); // ボタンの背景色変更
                            }

                            // ImageView や他のビューでも適用可能
                            if (view instanceof ImageView) {
                                handleImageViewColor((ImageView) view, resourceName);
                            }

                        } catch (Exception e) {
                            //XposedBridge.log("Unexpected error in onAttachedToWindow: " + e.getMessage());
                        }
                    }

                    private String getViewResourceName(View view) {
                        int viewId = view.getId();
                        if (viewId != View.NO_ID) { // IDが無効でない場合にのみ処理
                            try {
                                return view.getResources().getResourceEntryName(viewId);
                            } catch (Resources.NotFoundException e) {
                                //XposedBridge.log("Resource not found for View ID: " + viewId);
                                return "unknown";
                            }
                        }
                        return "no_id";
                    }

                    private void handleBackgroundColor(View view, String resourceName) {
                        Drawable background = view.getBackground();

                        // 背景が ColorDrawable の場合
                        if (background instanceof ColorDrawable) {
                            int bgColor = ((ColorDrawable) background).getColor();
                            String hexColor = convertToHexColor(bgColor);

                            // 背景色を変更する条件
                            if (hexColor.equals("#111111")) {
                                ((ColorDrawable) background).setColor(Color.parseColor("#FFFFFF")); // 白色に変更
                                //XposedBridge.log("Changed Background Color of Resource Name: " + resourceName + " from #111111 to #FFFFFF");
                            } else if (hexColor.equals("#1F1F1F") || resourceName.contains("search_bar_bg")) {
                                ((ColorDrawable) background).setColor(Color.parseColor("#000000")); // 黒色に変更
                                //XposedBridge.log("Changed Background Color of Resource Name: " + resourceName + " to #000000");
                            } else {
                                //XposedBridge.log("View Resource Name: " + resourceName + " Background Color: " + hexColor);
                            }
                        }
                    }

                    private void handleTextColor(TextView textView, String resourceName) {
                        int textColor = textView.getCurrentTextColor();
                        String hexTextColor = convertToHexColor(textColor);

                        // テキスト色のカスタム処理
                        if (hexTextColor.equals("#111111")) {
                            textView.setTextColor(Color.parseColor("#FFFFFF")); // 白色に変更
                            //XposedBridge.log("Changed Text Color of View Resource Name: " + resourceName + " from #111111 to #FFFFFF");
                        } else if (hexTextColor.equals("#1F1F1F")) {
                            textView.setTextColor(Color.parseColor("#000000")); // 黒色に変更
                            //XposedBridge.log("Changed Text Color of View Resource Name: " + resourceName + " from #1F1F1F to #000000");
                        } else {
                            //XposedBridge.log("View Resource Name: " + resourceName + " Text Color: " + hexTextColor);
                        }
                    }

                    private void handleImageViewColor(ImageView imageView, String resourceName) {
                        // 画像ビューの色調整（必要に応じて実装）
                        //XposedBridge.log("ImageView Resource Name: " + resourceName);
                    }

                    private String convertToHexColor(int color) {
                        return String.format("#%06X", (0xFFFFFF & color)); // 16進数形式に変換
                    }
                }
        );




        XposedHelpers.findAndHookMethod("android.view.ViewGroup", loadPackageParam.classLoader, "addView", View.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.args[0];
                try {
                    String resourceName = view.getResources().getResourceEntryName(view.getId());
                    //XposedBridge.log("addView - Added View Resource Name: " + resourceName);
                } catch (Resources.NotFoundException e) {
                    //XposedBridge.log("addView - Resource name not found for View ID: " + view.getId());
                }
            }
        });
// onLayout メソッドをフックして、背景色が #1F1F1F の場合に #000000 に変更
        XposedHelpers.findAndHookMethod("android.view.View", loadPackageParam.classLoader, "onLayout",
                boolean.class, int.class, int.class, int.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) param.thisObject;
                        checkAndChangeBackgroundColor(view);
                    }
                });

// onAttachedToWindow メソッドをフックして、背景色が #1F1F1F の場合に #000000 に変更
        XposedHelpers.findAndHookMethod("android.view.View", loadPackageParam.classLoader, "onAttachedToWindow", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.thisObject;
                checkAndChangeBackgroundColor(view);
            }
        });

// onDraw メソッドをフックして、背景色が #1F1F1F の場合に #000000 に変更
        XposedHelpers.findAndHookMethod("android.view.View", loadPackageParam.classLoader, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.thisObject;
                checkAndChangeBackgroundColor(view);
            }
        });

// setBackground メソッドをフックして、背景色が #1F1F1F の場合に #000000 に変更
        XposedHelpers.findAndHookMethod("android.view.View", loadPackageParam.classLoader, "setBackground", Drawable.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.thisObject;
                checkAndChangeBackgroundColor(view);
            }
        });

// setBackgroundColor メソッドをフックして、背景色が #1F1F1F の場合に #000000 に変更
        XposedHelpers.findAndHookMethod("android.view.View", loadPackageParam.classLoader, "setBackgroundColor", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.thisObject;
                checkAndChangeBackgroundColor(view);
            }
        });

// onVisibilityChanged メソッドをフックして、背景色が #1F1F1F の場合に #000000 に変更
        XposedHelpers.findAndHookMethod("android.view.View", loadPackageParam.classLoader, "onVisibilityChanged", View.class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.thisObject;
                checkAndChangeBackgroundColor(view);
            }
        });
        XposedHelpers.findAndHookMethod("android.view.ViewGroup", loadPackageParam.classLoader, "addView", View.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.args[0];
                checkAndChangeBackgroundColor(view);
            }
        });

        XposedHelpers.findAndHookMethod("android.app.Activity", loadPackageParam.classLoader, "onResume", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                View rootView = activity.getWindow().getDecorView();

                // 背景色を #000000 に変更
                rootView.setBackgroundColor(Color.parseColor("#000000"));
                //XposedBridge.log("Changed Activity Background Color to #000000");
            }
        });


    }


        private String getResourceName (Context context, int resourceId){
            return context.getResources().getResourceEntryName(resourceId);
        }

    // ビュー階層を再帰的に走査してリソース名をログ出力するメソッド
    private  void logResourceNames(View view) {
        if (view != null) {
            try {
                // リソース名を取得
                String resourceName = getViewResourceName(view);
                if (resourceName != null) {
                    //XposedBridge.log("Resource Name: " + resourceName);
                }
            } catch (Resources.NotFoundException e) {
                // リソースが見つからなかった場合
                //XposedBridge.log("Resource name not found for View ID: " + view.getId());
            }

            // 子ビューを再帰的に処理
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    logResourceNames(viewGroup.getChildAt(i));
                }
            }
        }
    }


    private boolean isChangingColor = false;

    private void checkAndChangeBackgroundColor(View view) {
        try {
            // Prevent recursion
            if (isChangingColor) {
                return; // Exit if we're already changing the color
            }
            isChangingColor = true;

            // リソース名を取得
            String resourceName = getViewResourceName(view);
            //XposedBridge.log("Resource Name: " + resourceName);

            // 特定のリソース名の場合、背景がnullであっても黒色に変更する
            if ("bnb_home_spacer_v2".equals(resourceName) ||
                    "bnb_chat_spacer".equals(resourceName) ||
                    "bnb_timeline_spacer".equals(resourceName) ||
                    "bnb_news_spacer".equals(resourceName) ||
                    "bnb_wallet_spacer".equals(resourceName)) {
                //XposedBridge.log("Background is null for Resource Name: " + resourceName + ", changing color to black.");
                ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#000000"));
                view.setBackground(colorDrawable); // Set black background
                //XposedBridge.log("Set background color of Resource Name: " + resourceName + " to #000000");
            }

            // 背景を取得
            Drawable background = view.getBackground();

            // 背景が null でないことを確認
            if (background != null) {
                // 背景のクラス名をログに出力
                //XposedBridge.log("Background Class Name: " + background.getClass().getName());

                if (background instanceof ColorDrawable) {
                    ((ColorDrawable) background).setColor(Color.parseColor("#000000"));
                    //XposedBridge.log("Changed Background Color of Resource Name: " + resourceName + " to #000000");
                } else if (background instanceof BitmapDrawable) {
                    //XposedBridge.log("BitmapDrawable background, cannot change color directly.");
                } else if (background instanceof LayerDrawable) {
                    LayerDrawable layerDrawable = (LayerDrawable) background;
                    for (int i = 0; i < layerDrawable.getNumberOfLayers(); i++) {
                        Drawable layer = layerDrawable.getDrawable(i);
                        if (layer instanceof ColorDrawable) {
                            ((ColorDrawable) layer).setColor(Color.parseColor("#000000"));
                            //XposedBridge.log("Changed LayerDrawable Background Color of Resource Name: " + resourceName + " to #000000");
                        }
                    }
                } else if (background instanceof ClipDrawable) {
                    ClipDrawable clipDrawable = (ClipDrawable) background;
                    Drawable drawable = clipDrawable.getDrawable();
                    if (drawable instanceof ColorDrawable) {
                        ((ColorDrawable) drawable).setColor(Color.parseColor("#000000"));
                        //XposedBridge.log("Changed ClipDrawable Background Color of Resource Name: " + resourceName + " to #000000");
                    }
                } else if (background instanceof InsetDrawable) {
                    InsetDrawable insetDrawable = (InsetDrawable) background;
                    Drawable drawable = insetDrawable.getDrawable();
                    if (drawable instanceof ColorDrawable) {
                        ((ColorDrawable) drawable).setColor(Color.parseColor("#000000"));
                        //XposedBridge.log("Changed InsetDrawable Background Color of Resource Name: " + resourceName + " to #000000");
                    }
                } else if (background instanceof DrawableWrapper) {
                    DrawableWrapper drawableWrapper = (DrawableWrapper) background;
                    Drawable wrappedDrawable = drawableWrapper.getDrawable();
                    if (wrappedDrawable instanceof ColorDrawable) {
                        ((ColorDrawable) wrappedDrawable).setColor(Color.parseColor("#000000"));
                        //XposedBridge.log("Changed DrawableWrapper Background Color for Resource Name: " + resourceName + " to #000000");
                    }
                } else if (background instanceof NinePatchDrawable) {
                    NinePatchDrawable ninePatchDrawable = (NinePatchDrawable) background;
                    //XposedBridge.log("NinePatchDrawable background, color change not directly supported.");
                }  else if (background instanceof StateListDrawable) {
                    StateListDrawable stateListDrawable = (StateListDrawable) background;
                    //XposedBridge.log("StateListDrawable background detected for Resource Name: " + resourceName);
                    // 必要に応じて状態ごとの処理を追加
                } else {
                    //XposedBridge.log("Unknown background type for Resource Name: " + resourceName + ", Class Name: " + background.getClass().getName());
                }
            } else {
                //XposedBridge.log("Background is null for Resource Name: " + resourceName);
            }
        } catch (Resources.NotFoundException e) {
            //XposedBridge.log("Resource name not found for View ID: " + view.getId());
        } finally {
            isChangingColor = false; // Reset the flag after the method execution
        }
    }



    // すべてのビューのリソース名を出力するメソッド
    private void printViewResourceNames(View view) {
        // ビューのIDを取得
        int viewId = view.getId();
        if (viewId != View.NO_ID) {
            String resourceName = view.getContext().getResources().getResourceEntryName(viewId);
            //XposedBridge.log("Resource Name: " + resourceName);
        }

        // ビューが ViewGroup の場合、その子ビューを再帰的に探索
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                printViewResourceNames(viewGroup.getChildAt(i));
            }
        }
    }

    // リソース名を取得するためのメソッド
    private String getViewResourceName(View view) {
        int viewId = view.getId();
        if (viewId != View.NO_ID) { // IDが無効でない場合にのみ処理
            try {
                return view.getResources().getResourceEntryName(viewId);
            } catch (Resources.NotFoundException e) {
                //XposedBridge.log("Resource not found for View ID: " + viewId);
                return "unknown";
            }
        }
        return "no_id";
    }





    // 色を16進数形式に変換するメソッド
    private String convertToHexColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color)); // 16進数形式に変換
    }


}





