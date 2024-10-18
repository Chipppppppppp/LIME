package io.github.chipppppppppp.lime.hooks;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class test implements IHook {
    private Ringtone ringtone = null;
    private boolean isPlaying = false; // 着信音が再生中かどうかを管理するフラグ

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                Constants.RESPONSE_HOOK.methodName,
                new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String paramValue = param.args[1].toString();

                        // 着信音を再生する条件
                        if (paramValue.contains("type:NOTIFIED_RECEIVED_CALL,") && !isPlaying) {
                            try {
                                // アプリケーションのコンテキストを取得
                                Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                                if (context != null) {
                                    Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                                    ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
                                    ringtone.play(); // 着信音を再生
                                    isPlaying = true; // 再生中フラグをセット
                                    XposedBridge.log("Ringtone is playing."); // デバッグログ
                                }
                            } catch (Exception e) {
                                XposedBridge.log(e);
                            }
                        }

                        // 着信音を停止する条件
                        if (paramValue.contains("RESULT=REJECTED,") ||
                                paramValue.contains("RESULT=REJECTED,")) {
                            if (ringtone != null && ringtone.isPlaying()) {
                                ringtone.stop(); // 着信音を停止
                                isPlaying = false; // 再生中フラグをリセット
                                XposedBridge.log("Ringtone has been stopped."); // デバッグログ
                            }
                        }
                    }
                });

        Class<?> voIPBaseFragmentClass = loadPackageParam.classLoader.loadClass("com.linecorp.voip2.common.base.VoIPBaseFragment");

        XposedBridge.hookAllMethods(voIPBaseFragmentClass, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (ringtone != null && ringtone.isPlaying()) {
                    ringtone.stop(); // フラグがリセットされる前に着信音を停止
                    isPlaying = false; // 再生中フラグをリセット
                    XposedBridge.log("Ringtone has been stopped in onCreate."); // デバッグログ
                }
            }
        });
    }
}
