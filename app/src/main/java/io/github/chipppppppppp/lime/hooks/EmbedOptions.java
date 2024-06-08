package io.github.chipppppppppp.lime.hooks;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Process;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

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
                        Utils.addModuleAssetPath(context);

                        SharedPreferences prefs = context.getSharedPreferences(Constants.MODULE_NAME + "-options", Context.MODE_PRIVATE);

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
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.topMargin = Utils.dpToPx(20, context);
                            switchView.setLayoutParams(params);
                            switchView.setText(option.id);
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

                        {
                            final String script = new String(Base64.decode(prefs.getString("encoded_js_modify_request", ""), Base64.NO_WRAP));

                            LinearLayout layoutModifyRequest = new LinearLayout(context);
                            layoutModifyRequest.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT));
                            layoutModifyRequest.setOrientation(LinearLayout.VERTICAL);
                            layoutModifyRequest.setPadding(Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context));

                            EditText editText = new EditText(context);
                            editText.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            editText.setTypeface(Typeface.MONOSPACE);
                            editText.setInputType(InputType.TYPE_CLASS_TEXT |
                                    InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
                                    InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                            editText.setMovementMethod(new ScrollingMovementMethod());
                            editText.setTextIsSelectable(true);
                            editText.setHorizontallyScrolling(true);
                            editText.setVerticalScrollBarEnabled(true);
                            editText.setHorizontalScrollBarEnabled(true);
                            editText.setText(script);

                            layoutModifyRequest.addView(editText);

                            LinearLayout buttonLayout = new LinearLayout(context);
                            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            buttonParams.topMargin = Utils.dpToPx(10, context);
                            buttonLayout.setLayoutParams(buttonParams);
                            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

                            Button copyButton = new Button(context);
                            copyButton.setText(R.string.button_copy);
                            copyButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("", editText.getText().toString());
                                    clipboard.setPrimaryClip(clip);
                                }
                            });

                            buttonLayout.addView(copyButton);

                            Button pasteButton = new Button(context);
                            pasteButton.setText(R.string.button_paste);
                            pasteButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    if (clipboard != null && clipboard.hasPrimaryClip()) {
                                        ClipData clip = clipboard.getPrimaryClip();
                                        if (clip != null && clip.getItemCount() > 0) {
                                            CharSequence pasteData = clip.getItemAt(0).getText();
                                            editText.setText(pasteData);
                                        }
                                    }
                                }
                            });

                            buttonLayout.addView(pasteButton);

                            layoutModifyRequest.addView(buttonLayout);

                            ScrollView scrollView = new ScrollView(context);

                            scrollView.addView(layoutModifyRequest);

                            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                    .setTitle(R.string.modify_request);

                            builder.setView(scrollView);

                            builder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String code = editText.getText().toString();
                                    if (!code.equals(script)) {
                                        prefs.edit().putString("encoded_js_modify_request", Base64.encodeToString(code.getBytes(), Base64.NO_WRAP)).commit();
                                        Toast.makeText(context.getApplicationContext(), context.getString(R.string.restarting), Toast.LENGTH_SHORT).show();
                                        Process.killProcess(Process.myPid());
                                        context.startActivity(new Intent().setClassName(Constants.PACKAGE_NAME, "jp.naver.line.android.activity.SplashActivity"));
                                    }
                                }
                            });

                            builder.setNegativeButton(R.string.negative_button, null);

                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    editText.setText(script);
                                }
                            });

                            AlertDialog dialog = builder.create();

                            Button button = new Button(context);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.topMargin = Utils.dpToPx(20, context);
                            button.setLayoutParams(params);
                            button.setText(R.string.modify_request);

                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.show();
                                }
                            });

                            layout.addView(button);
                        }

                        {
                            final String script = new String(Base64.decode(prefs.getString("encoded_js_modify_response", ""), Base64.NO_WRAP));

                            LinearLayout layoutModifyResponse = new LinearLayout(context);
                            layoutModifyResponse.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT));
                            layoutModifyResponse.setOrientation(LinearLayout.VERTICAL);
                            layoutModifyResponse.setPadding(Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context));

                            EditText editText = new EditText(context);
                            editText.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            editText.setTypeface(Typeface.MONOSPACE);
                            editText.setInputType(InputType.TYPE_CLASS_TEXT |
                                    InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
                                    InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                            editText.setMovementMethod(new ScrollingMovementMethod());
                            editText.setTextIsSelectable(true);
                            editText.setHorizontallyScrolling(true);
                            editText.setVerticalScrollBarEnabled(true);
                            editText.setHorizontalScrollBarEnabled(true);
                            editText.setText(script);

                            layoutModifyResponse.addView(editText);

                            LinearLayout buttonLayout = new LinearLayout(context);
                            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            buttonParams.topMargin = Utils.dpToPx(10, context);
                            buttonLayout.setLayoutParams(buttonParams);
                            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

                            Button copyButton = new Button(context);
                            copyButton.setText(R.string.button_copy);
                            copyButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("", editText.getText().toString());
                                    clipboard.setPrimaryClip(clip);
                                }
                            });

                            buttonLayout.addView(copyButton);

                            Button pasteButton = new Button(context);
                            pasteButton.setText(R.string.button_paste);
                            pasteButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    if (clipboard != null && clipboard.hasPrimaryClip()) {
                                        ClipData clip = clipboard.getPrimaryClip();
                                        if (clip != null && clip.getItemCount() > 0) {
                                            CharSequence pasteData = clip.getItemAt(0).getText();
                                            editText.setText(pasteData);
                                        }
                                    }
                                }
                            });

                            buttonLayout.addView(pasteButton);

                            layoutModifyResponse.addView(buttonLayout);

                            ScrollView scrollView = new ScrollView(context);

                            scrollView.addView(layoutModifyResponse);

                            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                    .setTitle(R.string.modify_response);

                            builder.setView(scrollView);

                            builder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String code = editText.getText().toString();
                                    if (!code.equals(script)) {
                                        prefs.edit().putString("encoded_js_modify_response", Base64.encodeToString(code.getBytes(), Base64.NO_WRAP)).commit();
                                        Toast.makeText(context.getApplicationContext(), context.getString(R.string.restarting), Toast.LENGTH_SHORT).show();
                                        Process.killProcess(Process.myPid());
                                        context.startActivity(new Intent().setClassName(Constants.PACKAGE_NAME, "jp.naver.line.android.activity.SplashActivity"));
                                    }
                                }
                            });

                            builder.setNegativeButton(R.string.negative_button, null);

                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    editText.setText(script);
                                }
                            });

                            AlertDialog dialog = builder.create();

                            Button button = new Button(context);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.topMargin = Utils.dpToPx(20, context);
                            button.setLayoutParams(params);
                            button.setText(R.string.modify_response);

                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.show();
                                }
                            });

                            layout.addView(button);
                        }

                        ScrollView scrollView = new ScrollView(context);

                        scrollView.addView(layout);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setTitle(R.string.options_title);

                        builder.setView(scrollView);

                        builder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean optionChanged = false;
                                for (int i = 0; i < limeOptions.options.length; ++i) {
                                    Switch switchView = (Switch) layout.getChildAt(i);
                                    if (limeOptions.options[i].checked != switchView.isChecked()) {
                                        optionChanged = true;
                                    }
                                    prefs.edit().putBoolean(limeOptions.options[i].name, switchView.isChecked()).commit();
                                }

                                if (optionChanged) {
                                    Toast.makeText(context.getApplicationContext(), context.getString(R.string.restarting), Toast.LENGTH_SHORT).show();
                                    Process.killProcess(Process.myPid());
                                    context.startActivity(new Intent().setClassName(Constants.PACKAGE_NAME, "jp.naver.line.android.activity.SplashActivity"));
                                }
                            }
                        });

                        builder.setNegativeButton(R.string.negative_button, null);

                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                for (int i = 0; i < limeOptions.options.length; ++i) {
                                    Switch switchView = (Switch) layout.getChildAt(i);
                                    switchView.setChecked(limeOptions.options[i].checked);
                                }
                            }
                        });

                        AlertDialog dialog = builder.create();

                        Button button = new Button(context);
                        button.setText(R.string.app_name);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.TOP | Gravity.END;
                        layoutParams.rightMargin = Utils.dpToPx(10, context);
                        layoutParams.topMargin = Utils.dpToPx(5, context);
                        button.setLayoutParams(layoutParams);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.show();
                            }
                        });

                        FrameLayout frameLayout = new FrameLayout(context);
                        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));

                        frameLayout.addView(button);

                        viewGroup.addView(frameLayout);
                    }
                }
        );
    }
}
