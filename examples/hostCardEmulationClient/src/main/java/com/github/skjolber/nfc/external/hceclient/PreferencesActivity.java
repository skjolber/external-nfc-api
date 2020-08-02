package com.github.skjolber.nfc.external.hceclient;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * Preference activity
 * 
 */

public class PreferencesActivity extends Activity implements OnSharedPreferenceChangeListener {

	private static final String TAG = PreferencesActivity.class.getName();
	
	public static final String PREFERENCE_HOST_CARD_EMULATION_PING_PONG = "preference_host_card_emulation_ping_pong";
	public static final String PREFERENCE_HOST_CARD_EMULATION_ISO_APPLICATION_ID = "preference_host_card_emulation_iso_application_id";
	public static final String PREFERENCE_HOST_CARD_EMULATION_PREFERENCE = "preference_host_card_emulation_preference";

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

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(TAG, "onSharedPreferenceChanged");
	}
	
}