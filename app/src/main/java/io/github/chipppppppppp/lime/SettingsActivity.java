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
        Switch switchDeleteAds = findViewById(R.id.switch_delete_ads);
        Switch switchRedirectWebView = findViewById(R.id.switch_redirect_web_view);
        Switch switchOpenInBrowser = findViewById(R.id.switch_open_in_browser);

        try {
            SharedPreferences prefs;
            prefs = getSharedPreferences("settings", MODE_WORLD_READABLE);
            switchDeleteVoom.setChecked(prefs.getBoolean("delete_voom", true));
            switchDeleteAds.setChecked(prefs.getBoolean("delete_ads", true));
            switchRedirectWebView.setChecked(prefs.getBoolean("redirect_web_view", true));
            switchOpenInBrowser.setChecked(prefs.getBoolean("open_in_browser", false));

            switchDeleteVoom.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("delete_voom", isChecked).apply();
            });

            switchDeleteAds.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("delete_ads", isChecked).apply();
            });

            switchRedirectWebView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("redirect_web_view", isChecked).apply();
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
                .setTitle(context.getString(R.string.app_name))
                .setMessage(context.getString(R.string.app_name))
                .setPositiveButton(context.getString(R.string.app_name), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
}