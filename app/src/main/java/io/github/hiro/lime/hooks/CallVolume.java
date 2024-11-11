package io.github.hiro.lime.hooks;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;


import static io.github.hiro.lime.Main.limeOptions;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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