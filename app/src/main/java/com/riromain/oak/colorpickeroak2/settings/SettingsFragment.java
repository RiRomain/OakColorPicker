package com.riromain.oak.colorpickeroak2.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

import com.riromain.oak.colorpickeroak2.PrefConst;
import com.riromain.oak.colorpickeroak2.R;

/**
 * Created by rrinie on 19.04.16.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Load the preference from XML resource
        addPreferencesFromResource(R.xml.preferences);

        EditTextPreference emailPref = (EditTextPreference) findPreference(PrefConst.ACCOUNT_EMAIL_KEY);
        EditTextPreference passwordPref = (EditTextPreference) findPreference(PrefConst.ACCOUNT_PASSWORD_KEY);

        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(PrefConst.PREFS_NAME, Context.MODE_PRIVATE);
        emailPref.setText(sharedPreferences.getString(PrefConst.ACCOUNT_EMAIL_KEY, ""));
        passwordPref.setText(sharedPreferences.getString(PrefConst.ACCOUNT_PASSWORD_KEY, ""));
    }
}
