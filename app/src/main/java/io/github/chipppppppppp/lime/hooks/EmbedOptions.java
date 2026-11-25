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
                        ViewGroup root = ((ViewGroup) param.args[0]);
                        Context context = root.getContext();
                        // LINE 15+/26 settings screen dropped "header_root"; anchor on the settings
                        // list (falls back to the fragment root) so the LIME button still appears.
                        int anchorId = context.getResources().getIdentifier("setting_list", "id", context.getPackageName());
                        View anchor = anchorId != 0 ? root.findViewById(anchorId) : null;
                        if (anchor == null) anchor = root;
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
                            try {
                                switchView.setText(option.id);
                            } catch (android.content.res.Resources.NotFoundException e) {
                                // Module resources are not merged into LINE; if addAssetPath
                                // failed the label id is unresolvable. Fall back to the option key
                                // so the row still renders instead of aborting the whole screen.
                                switchView.setText(name);
                            }
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
                            copyButton.setText(Utils.str(context, R.string.button_copy, "Copy"));
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
                            pasteButton.setText(Utils.str(context, R.string.button_paste, "Paste"));
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
                                    .setTitle(Utils.str(context, R.string.modify_request, "Modify request"));

                            builder.setView(scrollView);

                            builder.setPositiveButton(Utils.str(context, R.string.positive_button, "OK"), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String code = editText.getText().toString();
                                    if (!code.equals(script)) {
                                        prefs.edit().putString("encoded_js_modify_request", Base64.encodeToString(code.getBytes(), Base64.NO_WRAP)).commit();
                                        Toast.makeText(context.getApplicationContext(), Utils.str(context, R.string.restarting, "Restarting"), Toast.LENGTH_SHORT).show();
                                        Process.killProcess(Process.myPid());
                                        context.startActivity(new Intent().setClassName(Constants.PACKAGE_NAME, "jp.naver.line.android.activity.SplashActivity"));
                                    }
                                }
                            });

                            builder.setNegativeButton(Utils.str(context, R.string.negative_button, "Cancel"), null);

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
                            button.setText(Utils.str(context, R.string.modify_request, "Modify request"));

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
                            copyButton.setText(Utils.str(context, R.string.button_copy, "Copy"));
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
                            pasteButton.setText(Utils.str(context, R.string.button_paste, "Paste"));
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
                                    .setTitle(Utils.str(context, R.string.modify_response, "Modify response"));

                            builder.setView(scrollView);

                            builder.setPositiveButton(Utils.str(context, R.string.positive_button, "OK"), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String code = editText.getText().toString();
                                    if (!code.equals(script)) {
                                        prefs.edit().putString("encoded_js_modify_response", Base64.encodeToString(code.getBytes(), Base64.NO_WRAP)).commit();
                                        Toast.makeText(context.getApplicationContext(), Utils.str(context, R.string.restarting, "Restarting"), Toast.LENGTH_SHORT).show();
                                        Process.killProcess(Process.myPid());
                                        context.startActivity(new Intent().setClassName(Constants.PACKAGE_NAME, "jp.naver.line.android.activity.SplashActivity"));
                                    }
                                }
                            });

                            builder.setNegativeButton(Utils.str(context, R.string.negative_button, "Cancel"), null);

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
                            button.setText(Utils.str(context, R.string.modify_response, "Modify response"));

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
                                .setTitle(Utils.str(context, R.string.options_title, "LIME"));

                        builder.setView(scrollView);

                        builder.setPositiveButton(Utils.str(context, R.string.positive_button, "OK"), new DialogInterface.OnClickListener() {
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
                                    Toast.makeText(context.getApplicationContext(), Utils.str(context, R.string.restarting, "Restarting"), Toast.LENGTH_SHORT).show();
                                    Process.killProcess(Process.myPid());
                                    context.startActivity(new Intent().setClassName(Constants.PACKAGE_NAME, "jp.naver.line.android.activity.SplashActivity"));
                                }
                            }
                        });

                        builder.setNegativeButton(Utils.str(context, R.string.negative_button, "Cancel"), null);

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
                        button.setText(Utils.str(context, R.string.app_name, "LIME"));
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
                        // Keep the button clear of the gesture/navigation area. A fixed 5dp sits
                        // inside mandatorySystemGestures (bottom 63px here), so the control hugs
                        // the screen edge and is easy to miss. Add the bottom system inset.
                        int systemBottomInset = 0;
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            android.view.WindowInsets insets = root.getRootWindowInsets();
                            if (insets != null) systemBottomInset = insets.getSystemWindowInsetBottom();
                        }
                        layoutParams.bottomMargin = Utils.dpToPx(5, context)
                                + Math.max(systemBottomInset, Utils.dpToPx(16, context));
                        layoutParams.rightMargin = Utils.dpToPx(15, context);
                        button.setLayoutParams(layoutParams);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.show();
                            }
                        });

                        ViewGroup anchorParent = (ViewGroup) anchor.getParent();
                        if (anchorParent != null) {
                            // Wrap the anchor in a FrameLayout (reusing its LayoutParams so it keeps
                            // its slot in the parent) and overlay the LIME button at the bottom-end.
                            int index = anchorParent.indexOfChild(anchor);
                            ViewGroup.LayoutParams anchorLp = anchor.getLayoutParams();
                            anchorParent.removeView(anchor);
                            FrameLayout frameLayout = new FrameLayout(context);
                            frameLayout.setLayoutParams(anchorLp);
                            anchorParent.addView(frameLayout, index);
                            frameLayout.addView(anchor, new FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT));
                            frameLayout.addView(button);
                        } else {
                            // Fallback: no wrappable parent (layout changed again) — add directly.
                            root.addView(button);
                        }
                    }
                }
        );
    }
}
