package io.github.hiro.lime;

import android.content.Context;
import android.content.res.AssetManager;

import java.lang.reflect.Method;

public class Utils {
    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static void addModuleAssetPath(Context context) throws Throwable {
        Method mAddAddAssertPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
        mAddAddAssertPath.setAccessible(true);
        mAddAddAssertPath.invoke(context.getResources().getAssets(), Main.modulePath);
    }
}
