package com.mask.maskclock;

import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
    int easterEgg = 1;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference version = findPreference("informations");
        version.setSummary(BuildConfig.VERSION_NAME);

        // Easter egg stuff
        version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (easterEgg == 7) {
                    Toast.makeText(getActivity(), getString(R.string.stay_safe_out_there), Toast.LENGTH_LONG).show();
                    easterEgg = 0;
                }
                else
                    easterEgg++;
                return true;
            }
        });
    }
}