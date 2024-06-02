package io.github.chipppppppppp.lime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;

import de.robv.android.xposed.XposedBridge;

public class MainActivity extends Activity {
    public LimeOptions limeOptions = new LimeOptions();

    @Deprecated
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs;
        try {
            prefs = getSharedPreferences("options", MODE_WORLD_READABLE);
        } catch (SecurityException e) {
            showModuleNotEnabledAlert();
            return;
        }

        for (LimeOptions.Option option : limeOptions.options) {
            option.checked = prefs.getBoolean(option.name, option.checked);
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(Utils.dpToPx(20, this), Utils.dpToPx(20, this), Utils.dpToPx(20, this), Utils.dpToPx(20, this));

        Switch switchUnembedOptions = new Switch(this);
        {
            switchUnembedOptions.setText(getString(R.string.switch_unembed_options));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = Utils.dpToPx(20, this);
            switchUnembedOptions.setLayoutParams(params);

            switchUnembedOptions.setChecked(prefs.getBoolean("unembed_options", false));
            switchUnembedOptions.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("unembed_options", isChecked).apply();
                if (isChecked) {
                    for (int i = 1; i < layout.getChildCount(); ++i)
                        layout.getChildAt(i).setVisibility(View.VISIBLE);
                } else {
                    for (int i = 1; i < layout.getChildCount(); ++i)
                        layout.getChildAt(i).setVisibility(View.GONE);
                }
            });

            layout.addView(switchUnembedOptions);
        }

        Switch switchRedirectWebView = null;
        for (LimeOptions.Option option : limeOptions.options) {
            final String name = option.name;

            Switch switchView = new Switch(this);
            switchView.setText(getString(option.id));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = Utils.dpToPx(20, this);
            switchView.setLayoutParams(params);

            switchView.setChecked(option.checked);
            switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(name, isChecked).apply();
            });

            if (name == "redirect_webview") switchRedirectWebView = switchView;
            else if (name == "open_in_browser") {
                switchRedirectWebView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    prefs.edit().putBoolean("redirect_webview", isChecked).apply();
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

        EditText editText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = Utils.dpToPx(20, this);
        editText.setLayoutParams(params);
        editText.setTypeface(Typeface.MONOSPACE);
        editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
                android.text.InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        editText.setVerticalScrollBarEnabled(true);
        editText.setMovementMethod(new ScrollingMovementMethod());
        editText.setText(prefs.getString("custom_js", ""));

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                prefs.edit().putString("custom_js", s.toString()).apply();
            }
        });

        layout.addView(editText);

        if (switchUnembedOptions.isChecked()) {
            for (int i = 1; i < layout.getChildCount(); ++i)
                layout.getChildAt(i).setVisibility(View.VISIBLE);
            editText.setVisibility(View.VISIBLE);
        } else {
            for (int i = 1; i < layout.getChildCount(); ++i)
                layout.getChildAt(i).setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);

        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.addView(scrollView);
    }

    private void showModuleNotEnabledAlert() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.module_not_enabled_title))
                .setMessage(getString(R.string.module_not_enabled_text))
                .setPositiveButton(getString(R.string.positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAndRemoveTask();
                    }
                })
                .setCancelable(false)
                .show();
    }
}
