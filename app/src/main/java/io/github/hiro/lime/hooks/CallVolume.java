package io.github.hiro.lime.hooks;

import android.media.AudioManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class CallVolume implements IHook {

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedHelpers.findAndHookMethod(AudioManager.class, "requestAudioFocus",
                AudioManager.OnAudioFocusChangeListener.class, int.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        AudioManager.OnAudioFocusChangeListener listener = (AudioManager.OnAudioFocusChangeListener) param.args[0];
                        int focusGain = (int) param.args[1];
                        int mode = (int) param.args[2];

                        XposedBridge.log("Blocking transient audio focus request.");
                        param.setResult(AudioManager.AUDIOFOCUS_REQUEST_FAILED);

                    }
                });

        XposedHelpers.findAndHookMethod(AudioManager.class, "abandonAudioFocus",
                AudioManager.OnAudioFocusChangeListener.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        AudioManager.OnAudioFocusChangeListener listener = (AudioManager.OnAudioFocusChangeListener) param.args[0];

                    }
                });


    }
}