package io.github.chipppppppppp.lime.hooks;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;

public class Ringtone implements IHook {
    private android.media.Ringtone ringtone = null;
    private boolean isPlaying = false; 
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!limeOptions.callTone.checked) return;

        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                Constants.RESPONSE_HOOK.methodName,
                new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String paramValue = param.args[1].toString();

                        if (paramValue.contains("type:NOTIFIED_RECEIVED_CALL,") && !isPlaying) {
                            Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                            if (context != null) {
                                Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                                ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
                                ringtone.play();
                                isPlaying = true;
                            }

                            if (paramValue.contains("RESULT=REJECTED,") ||
                                    paramValue.contains("RESULT=REJECTED,")) {
                                if (ringtone != null && ringtone.isPlaying()) {
                                    ringtone.stop(); 
                                    isPlaying = false;
                                }
                            }
                        }
                    }
                });

        Class<?> voIPBaseFragmentClass = loadPackageParam.classLoader.loadClass("com.linecorp.voip2.common.base.VoIPBaseFragment");
        XposedBridge.hookAllMethods(voIPBaseFragmentClass, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (ringtone != null && ringtone.isPlaying()) {
                    ringtone.stop();
                    isPlaying = false;
                }
            }
        });
    }
}
