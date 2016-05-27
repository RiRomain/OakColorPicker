package com.riromain.oak.colorpickeroak2;


import android.preference.PreferenceActivity;

import com.riromain.oak.colorpickeroak2.settings.SettingsFragment;

import java.util.List;

/**
 * Created by rrinie on 19.04.16.
 */
public class MyPreferenceActivity extends PreferenceActivity
{
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.headers_preference, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return SettingsFragment.class.getName().equals(fragmentName);
    }


}
