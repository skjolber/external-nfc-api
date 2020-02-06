package com.skjolberg.nfc.external;

import com.skjolberg.service.BackgroundUsbService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class StartServiceActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    private String TAG = StartServiceActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, BackgroundUsbService.class);
        intent.fillIn(getIntent(), 0); // TODO: Find better way to get extras for `UsbManager.getAccessory()` use?
        startService(intent);

		/*
        Intent intent = new Intent();
		intent.setClassName(BackgroundUsbService.class.getPackage().getName(), BackgroundUsbService.class.getName());
        intent.fillIn(getIntent(), 0); // TODO: Find better way to get extras for `UsbManager.getAccessory()` use?
        startService(intent);
		*/

        // See:
        //
        //    <http://permalink.gmane.org/gmane.comp.handhelds.android.devel/154481> &
        //    <http://stackoverflow.com/questions/5567312/android-how-to-execute-main-fucntionality-of-project-only-by-clicking-on-the-ic/5567514#5567514>
        //
        // for combination of `Theme.NoDisplay` and `finish()` in `onCreate()`/`onResume()`.
        //
        finish();
    }
}