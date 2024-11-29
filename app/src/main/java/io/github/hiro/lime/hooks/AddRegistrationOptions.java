package io.github.hiro.lime.hooks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;
import io.github.hiro.lime.R;
import io.github.hiro.lime.Utils;

public class AddRegistrationOptions implements IHook {

    private Switch switchAndroidSecondary;

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
                        switchSpoofAndroidId.setChecked(prefs.getBoolean("spoof_android_id", false));
                        switchSpoofAndroidId.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                showSpoofAndroidIdDialog(activity, prefs);
                            } else {
                                prefs.edit().putBoolean("spoof_android_id", false).apply();
                                Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.need_refresh), Toast.LENGTH_SHORT).show();
                                activity.finish();
                            }
                        });


                        switchAndroidSecondary = new Switch(activity);
                        switchAndroidSecondary.setText(R.string.switch_android_secondary);
                        switchAndroidSecondary.setChecked(prefs.getBoolean("android_secondary", false));
                        switchAndroidSecondary.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                showSpoofVersionIdDialog(activity, prefs);
                            } else {
                                prefs.edit().putBoolean("android_secondary", false).apply();
                            }
                        });

                        linearLayout.addView(switchSpoofAndroidId);
                        linearLayout.addView(switchAndroidSecondary);
                        frameLayout.addView(linearLayout);
                        viewGroup.addView(frameLayout);
                    }
                }
        );
    }

    private void showSpoofVersionIdDialog(Activity activity, SharedPreferences prefs) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.options_title);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(Utils.dpToPx(20, activity), Utils.dpToPx(20, activity), Utils.dpToPx(20, activity), Utils.dpToPx(20, activity));

        TextView textView = new TextView(activity);
        textView.setText(R.string.spoof_version_id_risk);
        layout.addView(textView);

        EditText editTextDeviceName = new EditText(activity);
        editTextDeviceName.setHint(R.string.spoof_device_name);
        editTextDeviceName.setText(prefs.getString("device_name", "ANDROID"));
        layout.addView(editTextDeviceName);

        EditText editTextOsName = new EditText(activity);
        editTextOsName.setHint(R.string.spoof_os_name);
        editTextOsName.setText(prefs.getString("os_name", "Android OS"));
        layout.addView(editTextOsName);

        EditText editTextOsVersion = new EditText(activity);
        editTextOsVersion.setHint(R.string.spoof_os_version);
        editTextOsVersion.setText(prefs.getString("os_version", "14"));
        layout.addView(editTextOsVersion);

        EditText editTextAndroidVersion = new EditText(activity);
        editTextAndroidVersion.setHint(R.string.spoof_android_version);
        editTextAndroidVersion.setText(prefs.getString("android_version", "14.16.0"));
        layout.addView(editTextAndroidVersion);

        builder.setView(layout);
        builder.setPositiveButton(R.string.positive_button, (dialog, which) -> {
            prefs.edit()
                    .putBoolean("android_secondary", true)
                    .putString("device_name", editTextDeviceName.getText().toString()) // device_nameを保存
                    .putString("os_name", editTextOsName.getText().toString())
                    .putString("os_version", editTextOsVersion.getText().toString())
                    .putString("android_version", editTextAndroidVersion.getText().toString())
                    .apply();

            switchAndroidSecondary.setChecked(true);

            Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.need_refresh), Toast.LENGTH_SHORT).show();
            activity.finish();
        });

        builder.setNegativeButton(R.string.negative_button, null);
        builder.show();
    }


    private void showSpoofAndroidIdDialog(Activity activity, SharedPreferences prefs) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.options_title);

        TextView textView = new TextView(activity);
        textView.setText(R.string.spoof_android_id_risk);
        textView.setPadding(Utils.dpToPx(20, activity), Utils.dpToPx(20, activity), Utils.dpToPx(20, activity), Utils.dpToPx(20, activity));
        builder.setView(textView);

        builder.setPositiveButton(R.string.positive_button, (dialog, which) -> {
            prefs.edit().putBoolean("spoof_android_id", true).apply();
            Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.need_refresh), Toast.LENGTH_SHORT).show();
            activity.finish();
        });

        builder.setNegativeButton(R.string.negative_button, null);
        builder.show();
    }
}
