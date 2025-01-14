
package io.github.hiro.lime.hooks;


import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;


public class KeepUnread implements IHook {
    static boolean keepUnread = false;


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


                        Resources resources = appContext.getResources();
                        Configuration configuration = resources.getConfiguration();
                        int smallestWidthDp = configuration.smallestScreenWidthDp;


                        float density = resources.getDisplayMetrics().density;


                        float keep_unread_horizontalMarginFactor = getkeep_unread_horizontalMarginFactor(appContext);
                        int keep_unread_verticalMarginDp = getkeep_unread_verticalMarginDp(appContext);

                        int horizontalMarginPx = (int) (smallestWidthDp * keep_unread_horizontalMarginFactor * density);
                        int verticalMarginPx = (int) (keep_unread_verticalMarginDp * density);

                        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        imageParams.setMargins(horizontalMarginPx, verticalMarginPx, 0, 0);



                        imageView.setOnClickListener(v -> {
                            keepUnread = !keepUnread;
                            updateSwitchImage(imageView, keepUnread, moduleContext);
                            saveStateToFile(appContext, keepUnread);
                        });


                        layout.addView(imageView, imageParams);


                        if (rootView instanceof ViewGroup) {
                            ViewGroup rootViewGroup = (ViewGroup) rootView;


                            boolean added = false;
                            for (int i = 0; i < rootViewGroup.getChildCount(); i++) {
                                View child = rootViewGroup.getChildAt(i);
                                if (child instanceof ListView) {
                                    ListView listView = (ListView) child;
                                    listView.addFooterView(layout);
                                    added = true;
                                    break;
                                }
                            }


                            if (!added) {
                                rootViewGroup.addView(layout);
                            }
                        }
                    }

                    private Map<String, String> readSettingsFromExternalFile(Context context) {
                        String fileName = "margin_settings.txt";
                        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LimeBackup");
                        File file = new File(dir, fileName);
                        Map<String, String> settings = new HashMap<>();

                        if (!file.exists()) {

                        }

                        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String[] parts = line.split("=", 2);
                                if (parts.length == 2) {
                                    settings.put(parts[0].trim(), parts[1].trim());
                                }
                            }
                        } catch (IOException e) {
                            Log.e("FileError", "Error reading file: " + e.getMessage());
                        }
                        return settings;
                    }

                    private float getkeep_unread_horizontalMarginFactor(Context context) {
                        Map<String, String> settings = readSettingsFromExternalFile(context);
                        try {
                            return Float.parseFloat(settings.getOrDefault("keep_unread_horizontalMarginFactor", "0.5"));
                        } catch (NumberFormatException e) {
                            return 0.5f; // エラー時のデフォルト値
                        }
                    }

                    private int getkeep_unread_verticalMarginDp(Context context) {
                        Map<String, String> settings = readSettingsFromExternalFile(context);
                        try {
                            return Integer.parseInt(settings.getOrDefault("keep_unread_verticalMarginDp", "15"));
                        } catch (NumberFormatException e) {
                            return 15; // エラー時のデフォルト値
                        }
                    }

                    private void updateSwitchImage(ImageView imageView, boolean isOn, Context moduleContext) {
                        String imageName = isOn ? "switch_on.png" : "switch_off.png"; // 拡張子を追加
                        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LimeBackup");

                        // ディレクトリが存在しない場合は作成
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        File imageFile = new File(dir, imageName);

                        // 画像ファイルが存在しない場合はリソースからコピー
                        if (!imageFile.exists()) {
                            try (InputStream in = moduleContext.getResources().openRawResource(
                                    moduleContext.getResources().getIdentifier(imageName.replace(".png", ""), "drawable", "io.github.hiro.lime"));
                                 OutputStream out = new FileOutputStream(imageFile)) {
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = in.read(buffer)) > 0) {
                                    out.write(buffer, 0, length);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        // コピーした画像をImageViewに設定
                        if (imageFile.exists()) {
                            Drawable drawable = Drawable.createFromPath(imageFile.getAbsolutePath());
                            if (drawable != null) {
                                Map<String, String> settings = readSettingsFromExternalFile(moduleContext);
                                float sizeInDp = Float.parseFloat(settings.getOrDefault("keep_unread_size", "60")); // 既定値 38dp
                                int sizeInPx = dpToPx(moduleContext, sizeInDp); // dp を px に変換
                                drawable = scaleDrawable(drawable, sizeInPx, sizeInPx);
                                imageView.setImageDrawable(drawable);
                            }
                        }
                    }
                    private int dpToPx(Context context, float dp) {
                        float density = context.getResources().getDisplayMetrics().density;
                        return Math.round(dp * density);
                    }



                    private Drawable scaleDrawable(Drawable drawable, int width, int height) {
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                        return new BitmapDrawable(scaledBitmap);
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
    }


    private void saveStateToFile(Context context, boolean state) {
        String filename = "keep_unread_state.txt";
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write((state ? "1" : "0").getBytes());
        } catch (IOException ignored) {
        }
    }
}

