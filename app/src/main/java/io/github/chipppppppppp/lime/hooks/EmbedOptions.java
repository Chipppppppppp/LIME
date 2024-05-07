package io.github.chipppppppppp.lime.hooks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Process;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;
import io.github.chipppppppppp.lime.Main;
import io.github.chipppppppppp.lime.R;
import io.github.chipppppppppp.lime.Utils;

public class EmbedOptions implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (Main.xModulePrefs.getBoolean("unembed_options", false)) return;
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("com.linecorp.line.settings.main.LineUserMainSettingsFragment"),
                "onViewCreated",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        ViewGroup viewGroup = ((ViewGroup) param.args[0]);
                        Context context = viewGroup.getContext();

                        Method mAddAddAssertPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
                        mAddAddAssertPath.setAccessible(true);
                        mAddAddAssertPath.invoke(context.getResources().getAssets(), Main.MODULE_PATH);

                        SharedPreferences prefs = context.getSharedPreferences(Main.MODULE + "-options", Context.MODE_PRIVATE);

                        FrameLayout frameLayout = new FrameLayout(context);
                        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));

                        Button button = new Button(context);
                        button.setText(R.string.app_name);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.TOP | Gravity.END;
                        layoutParams.rightMargin = Utils.dpToPx(10, context);
                        layoutParams.topMargin = Utils.dpToPx(5, context);
                        button.setLayoutParams(layoutParams);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setTitle(R.string.options_title);

                        LinearLayout layout = new LinearLayout(context);
                        layout.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT));
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context));

                        Switch switchRedirectWebView = null;
                        for (LimeOptions.Option option : limeOptions.options) {
                            final String name = option.name;

                            Switch switchView = new Switch(context);
                            switchView.setText(option.id);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.topMargin = Utils.dpToPx(20, context);
                            switchView.setLayoutParams(params);

                            switchView.setChecked(option.checked);

                            if (name.equals("redirect_webview")) switchRedirectWebView = switchView;
                            else if (name.equals("open_in_browser")) {
                                switchRedirectWebView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    if (isChecked) switchView.setEnabled(true);
                                    else {
                                        switchView.setChecked(false);
                                        switchView.setEnabled(false);
                                    }
                                });
                                switchView.setEnabled(limeOptions.redirectWebView.checked);
                            }

                            layout.addView(switchView);
                        }

                        ScrollView scrollView = new ScrollView(context);
                        scrollView.addView(layout);
                        builder.setView(scrollView);

                        builder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean optionChanged = false;
                                for (int i = 0; i < layout.getChildCount(); ++i) {
                                    Switch switchView = (Switch) layout.getChildAt(i);
                                    if (limeOptions.options[i].checked != switchView.isChecked()) {
                                        optionChanged = true;
                                    }
                                    prefs.edit().putBoolean(limeOptions.options[i].name, switchView.isChecked()).commit();
                                }

                                if (optionChanged) {
                                    Toast.makeText(context.getApplicationContext(), context.getString(R.string.restarting), Toast.LENGTH_SHORT).show();
                                    Process.killProcess(Process.myPid());
                                    context.startActivity(new Intent().setClassName(Main.PACKAGE, "jp.naver.line.android.activity.SplashActivity"));
                                }
                            }
                        });

                        builder.setNegativeButton(R.string.negative_button, null);

                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                for (int i = 0; i < layout.getChildCount(); ++i) {
                                    Switch switchView = (Switch) layout.getChildAt(i);
                                    switchView.setChecked(limeOptions.options[i].checked);
                                }
                            }
                        });

                        AlertDialog dialog = builder.create();

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.show();
                            }
                        });

                        frameLayout.addView(button);
                        viewGroup.addView(frameLayout);
                    }
                }
        );
    }
}
