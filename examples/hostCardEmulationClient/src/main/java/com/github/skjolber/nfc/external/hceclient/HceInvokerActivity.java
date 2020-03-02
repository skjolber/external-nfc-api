package com.github.skjolber.nfc.external.hceclient;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;


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

		IsoDep isoDep = IsoDep.get(tag);
		if(isoDep != null) {
			Log.d(TAG, "Historical bytes were '" + Utils.convertBinToASCII(isoDep.getHistoricalBytes()) + "'");
			if(isoDep.getHiLayerResponse() != null) {
				Log.d(TAG, "HiLayer bytes were '" + Utils.convertBinToASCII(isoDep.getHiLayerResponse()) + "'");
			}
			try {
				try {
					// https://stackoverflow.com/questions/44967567/ioexception-with-host-based-card-emulation
					isoDep.setTimeout(1000);

					isoDep.connect();
					final byte[] command = Utils.asBytes(0x00, 0xA4, 0x04, 0x00, 0x06, 0xF0, 0x0A, 0x2B, 0x4C, 0x6D, 0x8E);

					Log.i(TAG, " -> " + Utils.convertBinToASCII(command));

					Log.d(TAG, "Historical bytes were '" + Utils.convertBinToASCII(isoDep.getHistoricalBytes()) + "'");
					if(isoDep.getHiLayerResponse() != null) {
						Log.d(TAG, "HiLayer bytes were '" + Utils.convertBinToASCII(isoDep.getHiLayerResponse()) + "'");
					}


					// 6A 82: file not found

					runOnUiThread(new Runnable() {
						public void run() {
							TextView textView = (TextView) findViewById(R.id.text);

							textView.setText(textView.getText() + " -> " + Utils.convertBinToASCII(command) + "\n");
						}
					});

					final byte[] response = isoDep.transceive(command);

					Log.i(TAG, " <- " + Utils.convertBinToASCII(response));

					runOnUiThread(new Runnable() {
						public void run() {
							TextView textView = (TextView) findViewById(R.id.text);

							textView.setText(textView.getText() + " <- " + Utils.convertBinToASCII(response) + "\n");
						}
					});

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

}
