package com.skjolberg.nfc.external.hceclient;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.skjolberg.nfc.util.Broadcast;


public class MainActivity extends DialogActivity {

	private static String TAG = MainActivity.class.getName();

	private boolean receiving = false;

	private final BroadcastReceiver hostCardEmulationBroadcastReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, final Intent intent) {

			String action = intent.getAction();

			if (Broadcast.HOST_CARD_EMULATION_SERVICE_STARTED.equals(action)) {
				runOnUiThread(new Runnable(){
					public void run() {
						TextView textView = (TextView) findViewById(R.id.text);

						textView.setText(textView.getText() + MainActivity.this.getString(R.string.hceServiceStarted) + "\n");
					}
				});

			} else if (Broadcast.HOST_CARD_EMULATION_APPLICATION_SELECTED.equals(action)) {

				runOnUiThread(new Runnable(){
					public void run() {
						TextView textView = (TextView) findViewById(R.id.text);

						String applicationId = intent.getStringExtra(Broadcast.KEY_APPLICATION_ID);
						
						textView.setText(textView.getText() + MainActivity.this.getString(R.string.hceApplicationSelected, applicationId) + "\n");
					}
				});

			} else throw new IllegalArgumentException("Unexpected action " + action);
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
		CardEmulation cardEmulation = CardEmulation.getInstance(NfcAdapter.getDefaultAdapter(this));
		
		boolean defaultService = cardEmulation.isDefaultServiceForAid(new ComponentName(this, ExternalNFCHostApduService.class), ExternalNFCHostApduService.AID);
		
		if(!defaultService) {
			Log.d(TAG, "Expected default service for AID " + ExternalNFCHostApduService.AID);
		}
		Log.d(TAG, "Service AID is " + ExternalNFCHostApduService.AID);
		
		enableBroadcast();
		
		showHelpfulDialog();
    }


    private void showHelpfulDialog() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);                 
		alert.setTitle(R.string.dialogTitle);  
		alert.setMessage(R.string.dialogMessage);
		
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


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	public void enableBroadcast() {
		if(!receiving) {
			IntentFilter serviceFilter = new IntentFilter();
			serviceFilter.addAction(Broadcast.HOST_CARD_EMULATION_APPLICATION_SELECTED);
			serviceFilter.addAction(Broadcast.HOST_CARD_EMULATION_SERVICE_STARTED);
			registerReceiver(hostCardEmulationBroadcastReceiver, serviceFilter);

			receiving = true;
		}
	}

	public void disableBroadcast() {
		if(receiving) {
			unregisterReceiver(hostCardEmulationBroadcastReceiver);

			receiving = false;
		}
	}

	public void showHelp(View view) {
		String url = "https://developer.android.com/guide/topics/connectivity/nfc/hce.html#ScreenOffBehavior";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}
	
	public void clear(View view) {
		TextView textView = (TextView) findViewById(R.id.text);
		textView.setText("");
	}
	
	@Override
	protected void onDestroy() {
		disableBroadcast();
		
		super.onDestroy();
	}

}
