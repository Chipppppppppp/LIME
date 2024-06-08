package io.github.chipppppppppp.lime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

        {
            LinearLayout layoutModifyRequest = new LinearLayout(this);
            layoutModifyRequest.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            layoutModifyRequest.setOrientation(LinearLayout.VERTICAL);
            layoutModifyRequest.setPadding(Utils.dpToPx(20, this), Utils.dpToPx(20, this), Utils.dpToPx(20, this), Utils.dpToPx(20, this));

            EditText editText = new EditText(this);
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
            editText.setText(new String(Base64.decode(prefs.getString("encoded_js_modify_request", ""), Base64.NO_WRAP)));

            layoutModifyRequest.addView(editText);

            LinearLayout buttonLayout = new LinearLayout(this);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonParams.topMargin = Utils.dpToPx(10, this);
            buttonLayout.setLayoutParams(buttonParams);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

            Button copyButton = new Button(this);
            copyButton.setText(R.string.button_copy);
            copyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("", editText.getText().toString());
                    clipboard.setPrimaryClip(clip);
                }
            });

            buttonLayout.addView(copyButton);

            Button pasteButton = new Button(this);
            pasteButton.setText(R.string.button_paste);
            pasteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
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

            ScrollView scrollView = new ScrollView(this);

            scrollView.addView(layoutModifyRequest);

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.modify_request);

            builder.setView(scrollView);

            builder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    prefs.edit().putString("encoded_js_modify_request", Base64.encodeToString(editText.getText().toString().getBytes(), Base64.NO_WRAP)).apply();
                }
            });

            builder.setNegativeButton(R.string.negative_button, null);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    editText.setText(new String(Base64.decode(prefs.getString("encoded_js_modify_request", ""), Base64.NO_WRAP)));
                }
            });

            AlertDialog dialog = builder.create();

            Button button = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = Utils.dpToPx(20, this);
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
            LinearLayout layoutModifyResponse = new LinearLayout(this);
            layoutModifyResponse.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            layoutModifyResponse.setOrientation(LinearLayout.VERTICAL);
            layoutModifyResponse.setPadding(Utils.dpToPx(20, this), Utils.dpToPx(20, this), Utils.dpToPx(20, this), Utils.dpToPx(20, this));

            EditText editText = new EditText(this);
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
            editText.setText(new String(Base64.decode(prefs.getString("encoded_js_modify_response", ""), Base64.NO_WRAP)));

            layoutModifyResponse.addView(editText);

            LinearLayout buttonLayout = new LinearLayout(this);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonParams.topMargin = Utils.dpToPx(10, this);
            buttonLayout.setLayoutParams(buttonParams);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

            Button copyButton = new Button(this);
            copyButton.setText(R.string.button_copy);
            copyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("", editText.getText().toString());
                    clipboard.setPrimaryClip(clip);
                }
            });

            buttonLayout.addView(copyButton);

            Button pasteButton = new Button(this);
            pasteButton.setText(R.string.button_paste);
            pasteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
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

            ScrollView scrollView = new ScrollView(this);

            scrollView.addView(layoutModifyResponse);

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.modify_response);

            builder.setView(scrollView);

            builder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    prefs.edit().putString("encoded_js_modify_response", Base64.encodeToString(editText.getText().toString().getBytes(), Base64.NO_WRAP)).apply();
                }
            });

            builder.setNegativeButton(R.string.negative_button, null);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    editText.setText(new String(Base64.decode(prefs.getString("encoded_js_modify_response", ""), Base64.NO_WRAP)));
                }
            });

            AlertDialog dialog = builder.create();

            Button button = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = Utils.dpToPx(20, this);
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

        if (!switchUnembedOptions.isChecked()) {
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
