package com.sdunk.jiraestimator.view.preferences;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sdunk.jiraestimator.R;
import com.sdunk.jiraestimator.db.DBExecutor;
import com.sdunk.jiraestimator.db.DBUtils;
import com.sdunk.jiraestimator.view.login.LoginActivity;
import com.sdunk.jiraestimator.view.project.ProjectSelectActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference projectPreference = findPreference("project");
            if (projectPreference != null) {
                projectPreference.setOnPreferenceClickListener(preference -> {
                    startActivity(new Intent(getContext(), ProjectSelectActivity.class));
                    return true;
                });
            }

            Preference userPreference = findPreference("user");
            if (userPreference != null) {
                userPreference.setOnPreferenceClickListener(preference -> {
                    DBExecutor.getInstance().diskIO().execute(() -> {
                        Activity activity = SettingsFragment.this.getActivity();
                        if (activity != null) {
                            DBUtils.clearLoggedInData(requireContext());
                            Intent intent = new Intent();
                            intent.setClass(getContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            activity.runOnUiThread(() -> startActivity(intent));
                        }
                    });
                    return true;
                });
            }
        }


    }
}