package io.github.chipppppppppp.lime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;

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

        if (switchUnembedOptions.isChecked()) {
            for (int i = 1; i < layout.getChildCount(); ++i)
                layout.getChildAt(i).setVisibility(View.VISIBLE);
        } else {
            for (int i = 1; i < layout.getChildCount(); ++i)
                layout.getChildAt(i).setVisibility(View.GONE);
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
                .setPositiveButton(getString(R.string.positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAndRemoveTask();
                    }
                })
                .setCancelable(false)
                .show();
    }
}
