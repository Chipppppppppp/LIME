package io.github.hiro.lime.hooks;

import de.robv.android.xposed.XposedBridge;

public class Console {
    public void log(Object arg) {
        XposedBridge.log(String.valueOf(arg));
    }
}
