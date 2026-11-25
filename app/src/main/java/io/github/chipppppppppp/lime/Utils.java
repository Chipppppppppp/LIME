package io.github.chipppppppppp.lime;

import android.content.Context;
import android.content.res.AssetManager;

import java.lang.reflect.Method;

import io.github.chipppppppppp.lime.hooks.Constants;

public class Utils {
    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    // Module string resources only resolve once the module APK has been added to the
    // host app's AssetManager. That can fail (stale path, merged-resource layout), and a
    // thrown Resources.NotFoundException here aborts the caller mid-build - the settings
    // screen then loses the control it was constructing. Return a readable fallback instead.
    public static CharSequence str(Context context, int id, String fallback) {
        try {
            return context.getResources().getText(id);
        } catch (Throwable t) {
            return fallback;
        }
    }

    public static void addModuleAssetPath(Context context) throws Throwable {
        String path = resolveModulePath(context);
        if (path == null) return;
        Method mAddAddAssertPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
        mAddAddAssertPath.setAccessible(true);
        mAddAddAssertPath.invoke(context.getResources().getAssets(), path);
    }

    // LSPosed hands the module a startup path captured at zygote init. It goes stale
    // whenever the module APK is reinstalled while LINE keeps running, and reuse then
    // throws IOException and aborts whatever was being built. Prefer the path the
    // package manager currently reports, and fall back to the startup value only if
    // that lookup fails.
    private static String resolveModulePath(Context context) {
        try {
            String live = context.getPackageManager()
                    .getApplicationInfo(Constants.MODULE_NAME, 0)
                    .sourceDir;
            if (live != null && new java.io.File(live).exists()) return live;
        } catch (Throwable ignored) {
        }
        if (Main.modulePath != null && new java.io.File(Main.modulePath).exists()) {
            return Main.modulePath;
        }
        return null;
    }
}
