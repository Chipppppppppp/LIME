package io.github.chipppppppppp.lime;

import android.content.Context;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Switch;
import android.content.SharedPreferences;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Switch switchDeleteVoom = findViewById(R.id.switch_delete_voom);
        Switch switchDeleteWallet = findViewById(R.id.switch_delete_wallet);
        Switch switchDistributeEvenly = findViewById(R.id.switch_distribute_evenly);
        Switch switchDeleteIconLabels = findViewById(R.id.switch_delete_icon_labels);
        Switch switchDeleteAds = findViewById(R.id.switch_delete_ads);
        Switch switchDeleteRecommendation = findViewById(R.id.switch_delete_recommendation);
        Switch switchRedirectWebView = findViewById(R.id.switch_redirect_webview);
        Switch switchOpenInBrowser = findViewById(R.id.switch_open_in_browser);

        try {
            SharedPreferences prefs;
            prefs = getSharedPreferences("settings", MODE_WORLD_READABLE);
            switchDeleteVoom.setChecked(prefs.getBoolean("delete_voom", true));
            switchDeleteWallet.setChecked(prefs.getBoolean("delete_wallet", true));
            switchDistributeEvenly.setChecked(prefs.getBoolean("distribute_evenly", true));
            switchDeleteIconLabels.setChecked(prefs.getBoolean("delete_icon_labels", false));
            switchDeleteAds.setChecked(prefs.getBoolean("delete_ads", true));
            switchDeleteRecommendation.setChecked(prefs.getBoolean("delete_recommendation", true));
            switchRedirectWebView.setChecked(prefs.getBoolean("redirect_webview", true));
            switchOpenInBrowser.setChecked(prefs.getBoolean("open_in_browser", false));

            switchDeleteVoom.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("delete_voom", isChecked).apply();
            });

            switchDeleteWallet.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("delete_wallet", isChecked).apply();
            });

            switchDistributeEvenly.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("distribute_evenly", isChecked).apply();
            });

            switchDeleteIconLabels.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("delete_icon_labels", isChecked).apply();
            });

            switchDeleteAds.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("delete_ads", isChecked).apply();
            });

            switchDeleteRecommendation.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("delete_recommendation", isChecked).apply();
            });

            switchRedirectWebView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("redirect_webview", isChecked).apply();
                if (isChecked) switchOpenInBrowser.setEnabled(true);
                else {
                    switchOpenInBrowser.setEnabled(false);
                    switchOpenInBrowser.setChecked(false);
                }
            });

            switchOpenInBrowser.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("open_in_browser", isChecked).apply();
            });
        } catch (SecurityException e) {
            showModuleNotEnabledAlert(this);
        }
    }

    private void showModuleNotEnabledAlert(Context context) {
        new AlertDialog.Builder(this)
                .setTitle(context.getString(R.string.module_not_enabled_title))
                .setMessage(context.getString(R.string.module_not_enabled_text))
                .setPositiveButton(context.getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
}