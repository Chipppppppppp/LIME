package io.github.chipppppppppp.lime.hooks;

import android.app.Application;
import android.app.Notification;

import io.github.chipppppppppp.lime.LimeOptions;

public class RemoveReplyMute implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!limeOptions.removeReplyMute.checked) return;

        XposedHelpers.findAndHookMethod(
                Notification.Builder.class,
                "addAction",
                Notification.Action.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Application app = AndroidAppHelper.currentApplication();
                        Notification.Action a = (Notification.Action) param.args[0];
                        String muteChatString = app.getString(app.getResources().getIdentifier("notification_button_mute", "string", app.getPackageName()));
                        if (muteChatString.equals(a.title)) {
                            param.setResult(param.thisObject);
                        }
                    }
                }
        );
    }
}
