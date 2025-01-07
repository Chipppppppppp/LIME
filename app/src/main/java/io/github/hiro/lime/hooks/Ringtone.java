package io.github.hiro.lime.hooks;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

        public class Ringtone implements IHook {
            private android.media.Ringtone ringtone = null;
            private boolean isPlaying = false;
            @Override
            public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
                if (!limeOptions.calltone.checked) return;

                XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Application appContext = (Application) param.thisObject;
                        if (appContext == null) {
                            return;
                        }

                        XposedBridge.hookAllMethods(
                                loadPackageParam.classLoader.loadClass(Constants.RESPONSE_HOOK.className),
                                Constants.RESPONSE_HOOK.methodName,
                                new XC_MethodHook() {

                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                        String paramValue = param.args[1].toString();
                                        Context context = AndroidAppHelper.currentApplication().getApplicationContext();

                                        if (paramValue.contains("type:NOTIFIED_RECEIVED_CALL,") && !isPlaying) {
                                            if (context != null) {
                                                Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                                                ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
                                                ringtone.play();
                                                isPlaying = true;
                                            }
                                        }

                                        if (paramValue.contains("RESULT=REJECTED,") || paramValue.contains("RESULT=REJECTED,")) {
                                            if (ringtone != null && ringtone.isPlaying()) {
                                                ringtone.stop();
                                                isPlaying = false;
                                            }
                                        }
                                        if (paramValue.contains("contentType:CALL,") && paramValue.contains("RESULT=NORMAL")) {
                                            if (context != null) {
                                                Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                android.media.Ringtone notificationRingtone = RingtoneManager.getRingtone(context, notificationUri);
                                                notificationRingtone.play();
                                            }
                                        }
                                    }
                                });


                        Class<?> targetClass = loadPackageParam.classLoader.loadClass("com.linecorp.andromeda.audio.AudioManager");
                        Method[] methods = targetClass.getDeclaredMethods();

                        for (Method method : methods) {
                            XposedBridge.hookMethod(method, new XC_MethodHook() {

                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                                    if (method.getName().equals("stop")) {
                                        if (ringtone != null && ringtone.isPlaying()) {
                                            ringtone.stop();
                                            isPlaying = false;
                                        }
                                    }
                                    if (method.getName().equals("setServerConfig")) {
                                        if (ringtone != null && ringtone.isPlaying()) {
                                            ringtone.stop();
                                            isPlaying = false;
                                        }
                                    }

                                    if (method.getName().equals("start")) {
                                        if (appContext != null) {
                                            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                                            ringtone = RingtoneManager.getRingtone(appContext, ringtoneUri);

                                            if (ringtone != null) {
                                                ringtone.play();
                                                isPlaying = true;
                                            } else {
                                               return;
                                            }
                                        } else {
                                          return;
                                        }
                                    }
                                    if (method.getName().equals("ACTIVATED") && param.args != null && param.args.length > 0) {
                                        Object arg0 = param.args[0];
                                        if ("ACTIVATED".equals(arg0)) {
                                            if (ringtone != null && ringtone.isPlaying()) {
                                                ringtone.stop();
                                                isPlaying = false;
                                            }
                                        }
                                    }
                                    if (limeOptions.MuteTone.checked) {
                                        if (method.getName().equals("processToneEvent")) {
                                            param.setResult(null);
                                        }
                                    }
                                }
                            });

                            if (limeOptions.MuteTone.checked) {
                                if (method.getName().equals("processToneEvent")) {
                                    param.setResult(null);
                                }
                            }
                        }
                    }
                });
            }
        }


