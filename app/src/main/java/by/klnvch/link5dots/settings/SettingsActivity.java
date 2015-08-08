package by.klnvch.link5dots.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import by.klnvch.link5dots.R;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (s.equals(SettingsUtils.APP_LANGUAGE)) {
                SettingsActivity.this.finish();
                SettingsActivity.this.startActivity(getIntent());
            } else if (s.equals(SettingsUtils.USER_NAME)) {
                String username = sharedPreferences.getString(s, getString(R.string.device_info_default));
                TextView tvUsername = (TextView) findViewById(R.id.username_details);
                tvUsername.setText(username);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString(SettingsUtils.USER_NAME, getString(R.string.device_info_default));
        TextView tvUsername = (TextView) findViewById(R.id.username_details);
        tvUsername.setText(username);

        findViewById(R.id.username).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsernameDialog dialog = new UsernameDialog();
                dialog.show(getSupportFragmentManager(), null);
            }
        });

        findViewById(R.id.language).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LanguageDialog dialog = new LanguageDialog();
                dialog.show(getSupportFragmentManager(), null);
            }
        });

        findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(listener);

        super.onDestroy();
    }
}
