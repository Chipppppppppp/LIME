package io.github.chipppppppppp.lime.hooks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;
import io.github.chipppppppppp.lime.Main;
import io.github.chipppppppppp.lime.R;
import io.github.chipppppppppp.lime.Utils;

public class AddRegistrationOptions implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("com.linecorp.registration.ui.fragment.WelcomeFragment"),
                "onViewCreated",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) param.args[0]).getChildAt(0);
                        Activity activity = (Activity) viewGroup.getContext();
                        Utils.addModuleAssetPath(activity);

                        SharedPreferences prefs = activity.getSharedPreferences(Constants.MODULE_NAME + "-options", Context.MODE_PRIVATE);

                        FrameLayout frameLayout = new FrameLayout(activity);
                        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));

                        LinearLayout linearLayout = new LinearLayout(activity);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                        layoutParams.topMargin = Utils.dpToPx(60, activity);
                        linearLayout.setLayoutParams(layoutParams);
                        linearLayout.setOrientation(LinearLayout.VERTICAL);

                        Switch switchSpoofAndroidId = new Switch(activity);
                        switchSpoofAndroidId.setText(R.string.switch_spoof_android_id);

                        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                                .setTitle(R.string.options_title)
                                .setCancelable(false);

                        TextView textView = new TextView(activity);
                        textView.setText(R.string.spoof_android_id_risk);
                        textView.setPadding(Utils.dpToPx(20, activity), Utils.dpToPx(20, activity), Utils.dpToPx(20, activity), Utils.dpToPx(20, activity));
                        builder.setView(textView);

                        builder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().putBoolean("spoof_android_id", true).apply();
                                Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.need_refresh), Toast.LENGTH_SHORT).show();
                                activity.finish();
                            }
                        });

                        AlertDialog dialog = builder.create();

                        switchSpoofAndroidId.setChecked(prefs.getBoolean("spoof_android_id", false));
                        switchSpoofAndroidId.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) dialog.show();
                            else {
                                prefs.edit().putBoolean("spoof_android_id", false).apply();
                                Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.need_refresh), Toast.LENGTH_SHORT).show();
                                activity.finish();
                            }
                        });

                        Switch switchAndroidSecondary = new Switch(activity);
                        switchAndroidSecondary.setText(R.string.switch_android_secondary);
                        switchAndroidSecondary.setChecked(prefs.getBoolean("android_secondary", false));
                        switchAndroidSecondary.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            prefs.edit().putBoolean("android_secondary", isChecked).apply();
                            Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.need_refresh), Toast.LENGTH_SHORT).show();
                            activity.finish();
                        });

                        linearLayout.addView(switchSpoofAndroidId);
                        linearLayout.addView(switchAndroidSecondary);
                        frameLayout.addView(linearLayout);
                        viewGroup.addView(frameLayout);
                    }
                }
        );
    }
}
