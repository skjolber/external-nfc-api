package com.github.skjolber.nfc.external.hceclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Gravity;
import android.widget.Toast;

public abstract class DialogActivity extends Activity {

	protected AlertDialog alertDialog;
	
	protected void show(AlertDialog altertDialog) {
		synchronized(this) {
			if(alertDialog != null) {
				alertDialog.cancel();
			}
			// create alert dialog
			this.alertDialog = altertDialog;
			
			runOnUiThread(new Runnable() {
				public void run() {
					// show it
					alertDialog.show();
			}});
			
		}
	}
	
	protected boolean hasDialog() {
		return alertDialog != null;
	}
	
	public void hideDialog() {
		synchronized(this) {
			if(alertDialog != null) {
				alertDialog.cancel();
				alertDialog = null;
			}
		}
	}

	public void toast(int string) {
		toast(getString(string));
	}

	public void toast(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}
}
