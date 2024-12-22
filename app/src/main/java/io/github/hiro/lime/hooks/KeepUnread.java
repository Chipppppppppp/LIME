package io.github.hiro.lime.hooks;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class KeepUnread implements IHook {
    static boolean keepUnread = true;

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (limeOptions.removeKeepUnread.checked) return;

        XposedHelpers.findAndHookMethod(
                "com.linecorp.line.chatlist.view.fragment.ChatListFragment",
                loadPackageParam.classLoader,
                "onCreateView",
                LayoutInflater.class, ViewGroup.class, android.os.Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View rootView = (View) param.getResult();
                        Context appContext = rootView.getContext();

                        Context moduleContext = AndroidAppHelper.currentApplication().createPackageContext(
                                "io.github.hiro.lime", Context.CONTEXT_IGNORE_SECURITY);

                        RelativeLayout layout = new RelativeLayout(appContext);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layout.setLayoutParams(layoutParams);

                        keepUnread = readStateFromFile(appContext);
                        ImageView imageView = new ImageView(appContext);
                        updateSwitchImage(imageView, keepUnread, moduleContext);

                        DisplayMetrics displayMetrics = appContext.getResources().getDisplayMetrics();
                        int screenWidth = displayMetrics.widthPixels;
                        int screenHeight = displayMetrics.heightPixels;

                        int horizontalMargin = (int) (screenWidth * 0.5);
                        int verticalMargin = (int) (screenHeight * 0.015);

                        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        imageParams.setMargins(horizontalMargin, verticalMargin, 0, 0); // 動的に計算されたマージンを設定

                        imageView.setOnClickListener(v -> {
                            keepUnread = !keepUnread;
                            updateSwitchImage(imageView, keepUnread, moduleContext);
                            saveStateToFile(appContext, keepUnread);
                        });

                        layout.addView(imageView, imageParams);

                        if (rootView instanceof ViewGroup) {
                            ViewGroup rootViewGroup = (ViewGroup) rootView;
                            if (rootViewGroup.getChildCount() > 0 && rootViewGroup.getChildAt(0) instanceof ListView) {
                                ListView listView = (ListView) rootViewGroup.getChildAt(0);
                                listView.addFooterView(layout);
                            } else {
                                rootViewGroup.addView(layout);
                            }
                        }
                    }
                    private void updateSwitchImage(ImageView imageView, boolean isOn, Context moduleContext) {

                        String imageName = isOn ? "switch_on" : "switch_off";
                        int imageResource = moduleContext.getResources().getIdentifier(imageName, "drawable", "io.github.hiro.lime");

                        if (imageResource != 0) {
                            Drawable drawable = moduleContext.getResources().getDrawable(imageResource, null);
                            if (drawable != null) {
                                drawable = scaleDrawable(drawable, 86, 86);
                                imageView.setImageDrawable(drawable);
                            }
                        }
                    }

                    private Drawable scaleDrawable(Drawable drawable, int width, int height) {
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                        return new BitmapDrawable(scaledBitmap);
                    }

                    private void saveStateToFile(Context context, boolean state) {
                        String filename = "keep_unread_state.txt";
                        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
                            fos.write((state ? "1" : "0").getBytes());
                        } catch (IOException ignored) {
                        }
                    }

                    private boolean readStateFromFile(Context context) {
                        String filename = "keep_unread_state.txt";
                        try (FileInputStream fis = context.openFileInput(filename)) {
                            int c;
                            StringBuilder sb = new StringBuilder();
                            while ((c = fis.read()) != -1) {
                                sb.append((char) c);
                            }
                            return "1".equals(sb.toString());
                        } catch (IOException ignored) {
                            return false;
                        }
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass(Constants.MARK_AS_READ_HOOK.className),
                Constants.MARK_AS_READ_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (keepUnread) {
                            param.setResult(null);
                        }
                    }
                }
        );

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                Constants.RESPONSE_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[0] != null && param.args[0].toString().equals("sendChatChecked")) {
                            param.setResult(null);
                        }
                    }
                }
        );

    }
}
