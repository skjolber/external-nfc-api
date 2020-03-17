package com.github.skjolber.nfc.external.hceclient;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import com.github.skjolber.android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.github.skjolber.nfc.service.IsoDepDeviceHint;
import com.github.skjolber.nfc.util.CommandAPDU;

import java.io.IOException;
import java.util.Arrays;


/**
 *
 * Activity for this app to play the HCE invoker using internal NFC.
 *
 * So this activity transmits the AID upon initial connection.
 *
 */

public class HceInvokerActivity extends DialogActivity implements NfcAdapter.ReaderCallback {

	private static String TAG = HceInvokerActivity.class.getName();

	@TargetApi(Build.VERSION_CODES.N)
	private static class OnTagRemovedListener implements NfcAdapter.OnTagRemovedListener {

		private final Activity activity;

		public OnTagRemovedListener(Activity context) {
			this.activity = context;
		}

		public void onTagRemoved() {
			activity.runOnUiThread(new Runnable(){
				public void run() {
					TextView textView = (TextView) activity.findViewById(R.id.text);

					textView.setText(textView.getText() + "Tag removed" + "\n");
				}
			});
		}

	}

	private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoker);

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);

		showHelpfulDialog();
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.internal_invoker, menu);
        return true;
    }

	@Override
	protected void onResume() {
		super.onResume();

		nfcAdapter.enableReaderMode(this, this,NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
	}

	@Override
	protected void onPause() {
		super.onPause();

		nfcAdapter.disableReaderMode(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onTagDiscovered(Tag tag) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			// causes problems in android 8
			//nfcAdapter.ignore(tag, 2000, new OnTagRemovedListener(this), null);
		}

		com.github.skjolber.android.nfc.Tag wrapped = com.github.skjolber.android.nfc.Tag.get(tag);
		IsoDep isoDep = IsoDep.get(wrapped);
		if(isoDep != null) {
			Log.d(TAG, "Historical bytes were '" + Utils.toHexString(isoDep.getHistoricalBytes()) + "'");
			if(isoDep.getHiLayerResponse() != null) {
				Log.d(TAG, "HiLayer bytes were '" + Utils.toHexString(isoDep.getHiLayerResponse()) + "'");
			}
			Log.d(TAG, "Technologies were " + Arrays.asList(tag.getTechList()) + ", id was " + Utils.toHexString(tag.getId()));

			IsoDepDeviceHint hint = new IsoDepDeviceHint(isoDep);

			if(hint.isTag()) {
				Log.d(TAG, "Device hints indicate a Desfire EV1 card");
			} else if(hint.isHostCardEmulation()) {
				Log.d(TAG, "Device hints indicate a Host Card Emulation device");
			} else {
				Log.d(TAG, "Device hints unable to indicate a type");
			}

			try {
				try {
					// https://stackoverflow.com/questions/44967567/ioexception-with-host-based-card-emulation
					isoDep.setTimeout(1000);

					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

						// attempt to select demo HCE application using iso adpu
					String isoApplicationString = prefs.getString(PreferencesActivity.PREFERENCE_HOST_CARD_EMULATION_ISO_APPLICATION_ID, null);
					// clean whitespace
					isoApplicationString = isoApplicationString.replaceAll("\\s","");

					byte[] key = hexStringToByteArray(isoApplicationString);

					isoDep.connect();

					final byte[] command = new CommandAPDU(0x00, 0xA4, 0x04, 00, key).getBytes();

					Log.i(TAG, " -> " + Utils.toHexString(command));

					// 6A 82: file not found

					runOnUiThread(new Runnable() {
						public void run() {
							TextView textView = (TextView) findViewById(R.id.text);

							textView.setText(textView.getText() + " -> " + Utils.toHexString(command) + "\n");
						}
					});

					final byte[] response = isoDep.transceive(command);

					Log.i(TAG, " <- " + Utils.toHexString(response));

					runOnUiThread(new Runnable() {
						public void run() {
							TextView textView = (TextView) findViewById(R.id.text);

							textView.setText(textView.getText() + " <- " + Utils.toHexString(response) + "\n");
						}
					});
					if(response[response.length - 2] == (byte)0x90 || response[response.length - 1] == (byte)0x00) {
						boolean pingPong = prefs.getBoolean(PreferencesActivity.PREFERENCE_HOST_CARD_EMULATION_PING_PONG, true);

						if (pingPong) {
							Log.d(TAG, "Invoker will attempt to play ping-pong");
							PingPong.playPingPong(isoDep);
						}
					}

				} finally {
					isoDep.close();
				}
			} catch (final IOException e) {
				Log.i(TAG, "Problem on tag discovered", e);

				runOnUiThread(new Runnable() {
					public void run() {
						TextView textView = (TextView) findViewById(R.id.text);

						if (e.getMessage() != null) {
							textView.setText(textView.getText() + " " + e.getClass().getSimpleName() + "\n");
						} else {
							textView.setText(textView.getText() + " " + e.getClass().getSimpleName() + "\n" + e.getMessage() + "\n");
						}
					}
				});

			}
		} else {
			Log.i(TAG, "Ignoring non-IsoDep tag");
		}
	}

	public void clear(View view) {
		TextView textView = (TextView) findViewById(R.id.text);
		textView.setText("");
	}


	private void showHelpfulDialog() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.invokerDialogTitle);
		alert.setMessage(R.string.invokerDialogMessage);

		alert.setNegativeButton(R.string.dialogClose, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				return;
			}
		});

		final AlertDialog dialog = alert.create();

		dialog.setCanceledOnTouchOutside(true);
		dialog.setCancelable(true); // back button

		show(dialog);
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}
}
