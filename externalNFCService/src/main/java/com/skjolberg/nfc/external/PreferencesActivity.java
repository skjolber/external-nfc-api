package com.skjolberg.nfc.external;

import com.skjolberg.service.BackgroundUsbService;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

/**
 * Preferences
 *
 * http://www.cs.dartmouth.edu/~campbell/cs65/lecture12/lecture12.html
 *
 */

public class PreferencesActivity extends Activity implements OnSharedPreferenceChangeListener {

    private static final String TAG = PreferencesActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();

    }

    public static class PrefsFragment extends PreferenceFragment {

        Preference[] toggle;
        PreferenceCategory mCategory;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            mCategory = (PreferenceCategory) findPreference("category_tag");

            Preference readNdef = mCategory.findPreference(BackgroundUsbService.PREFERENCE_AUTO_READ_NDEF);
            Preference autoReadUID = mCategory.findPreference(BackgroundUsbService.PREFERENCE_AUTO_READ_UID);
            Preference ntag21x = mCategory.findPreference(BackgroundUsbService.PREFERENCE_NTAG21X_ULTRALIGHT);

            toggle = new Preference[]{readNdef, autoReadUID, ntag21x};

            CheckBoxPreference uidMode = (CheckBoxPreference) mCategory.findPreference(BackgroundUsbService.PREFERENCE_UID_MODE);
            if (uidMode.isChecked()) {
                for (Preference preference : toggle) {
                    mCategory.removePreference(preference);
                }
            }

            uidMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    refresh((Boolean) newValue);
                    return true;
                }
            });
        }

        public void refresh(Boolean value) {
            if (value) {
                for (Preference preference : toggle) {
                    mCategory.removePreference(preference);
                }
            } else {
                for (Preference preference : toggle) {
                    mCategory.addPreference(preference);
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged");

        if (key.equals(BackgroundUsbService.PREFERENCE_AUTO_START_ON_READER_CONNECT)) {
            ActivityAliasTools.setUsbDeviceFilter(this, sharedPreferences.getBoolean(key, false));
        } else if (key.equals(BackgroundUsbService.PREFERENCE_AUTO_START_ON_RESTART)) {
            ActivityAliasTools.setBootFilter(this, sharedPreferences.getBoolean(key, false));
        }

    }

}