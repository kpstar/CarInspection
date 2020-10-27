package com.coretal.carinspection.fragments;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.URLUtil;

import com.coretal.carinspection.BuildConfig;
import com.coretal.carinspection.R;
import com.coretal.carinspection.activities.MainActivity;
import com.coretal.carinspection.preferences.PreferenceFragmentCompat;
import com.coretal.carinspection.utils.AlertHelper;
import com.coretal.carinspection.utils.Contents;

import java.util.Locale;

import androidx.preference.PreferenceCategory;

/**
 * Created by Kangtle_R on 1/22/2018.
 */

public class ConfigPrefFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public ConfigPrefFragment() {
        // Required empty public constructor
    }

    public static ConfigPrefFragment newInstance() {
        ConfigPrefFragment fragment = new ConfigPrefFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        PreferenceCategory appCategory = (PreferenceCategory) findPreference("category_app");

        String newTitle = String.format(Locale.US, "App Configuration - v%s(%d)", BuildConfig.VERSION_NAME, 0); // BuildConfig.VERSION_CODE);
        appCategory.setTitle(newTitle);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case Contents.Config.WS_CONFIG_URL:
                if (!URLUtil.isValidUrl(sharedPreferences.getString(key, ""))){
                    AlertHelper.message(getActivity(), "Warnning", "The url is not valid");
                    sharedPreferences.edit().putString(key, Contents.API_ROOT).apply();
                    reload();
                }
                break;
            case Contents.Config.CONF_APP_SCHEMA_COLOR_BUTTON:
            case Contents.Config.CONF_APP_SCHEMA_COLOR_CHECK:
            case Contents.Config.CONF_APP_SCHEMA_COLOR_UNCHECK:
            case Contents.Config.CONF_APP_SCHEMA_COLOR_BACKGROUND:
                MainActivity activity = (MainActivity)getActivity();
                if (activity != null) activity.refresh();
                break;
            default:
                break;
        }
    }

    public void reload(){
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.preferences);
    }
}
