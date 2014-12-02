package com.skjolberg.nfc.external.nxpclient;

import java.io.IOException;

import android.content.Intent;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nxp.nfclib.exceptions.SmartCardException;
import com.nxp.nfclib.utils.Utilities;
import com.nxp.nfcliblite.Interface.NxpNfcLibLite;
import com.nxp.nfcliblite.cards.DESFire;
import com.skjolberg.nfc.NfcReader;
import com.skjolberg.nfc.util.activity.NfcExternalDetectorActivity;


public class MainActivity extends NfcExternalDetectorActivity {

	private static final String TAG = MainActivity.class.getName();
	
	private NxpNfcLibLite libInstance = null;
	
	private TextView textView = null;
	private DESFire desfire;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
		libInstance = NxpNfcLibLite.getInstance();
		libInstance.registerActivity(this);
		
        textView = (TextView) findViewById(R.id.text);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	public void clear(View view) {
		textView.setText("");
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onPause() {
		libInstance.stopForeGroundDispatch();
		super.onPause();
	}

	@Override
	protected void onResume() {
		libInstance.startForeGroundDispatch();
		super.onResume();
	}
	
	protected void onExternalNfcIntentDetected(Intent intent, String action) {
		// default to same as native NFC
		libInstance.stopForeGroundDispatch();
		onNfcIntentDetected(intent, action);
		libInstance.startForeGroundDispatch();
	}
	
	@Override
	protected void onExternalNfcServiceStopped(Intent intent) {
		Log.d(TAG, "Service started");
	}

	@Override
	protected void onExternalNfcServiceStarted(Intent intent) {
		Log.d(TAG, "Service stopped");
	}
	
	
	@Override
	protected void onExternalNfcReaderOpened(Intent intent) {
		Log.d(TAG, "Reader open");
	}

	@Override
	protected void onExternalNfcReaderClosed(Intent intent) {
    	if(intent.hasExtra(NfcReader.EXTRA_READER_STATUS_CODE)) {
    		Log.d(TAG, "Disconnect status code " + intent.getIntExtra(NfcReader.EXTRA_READER_STATUS_CODE, -1));
    	}

    	if(intent.hasExtra(NfcReader.EXTRA_READER_STATUS_MESSAGE)) {
    		Log.d(TAG, "Disconnect status message " + intent.getCharSequenceExtra(NfcReader.EXTRA_READER_STATUS_MESSAGE));
    	}
	}

   /**
     * 
     * NFC feature was found and is currently enabled
     * 
     */
	
	@Override
	protected void onNfcStateEnabled() {
		toast(getString(R.string.nfcAvailableEnabled));
	}

    /**
     * 
     * NFC feature was found but is currently disabled
     * 
     */
	
	@Override
	protected void onNfcStateDisabled() {
		toast(getString(R.string.nfcAvailableDisabled));
	}

	/**
     * 
     * NFC setting changed since last check. For example, the user enabled NFC in the wireless settings.
     * 
     */
	
	@Override
	protected void onNfcStateChange(boolean enabled) {
		if(enabled) {
			toast(getString(R.string.nfcAvailableEnabled));
		} else {
			toast(getString(R.string.nfcAvailableDisabled));
		}
	}

	/**
	 * 
	 * This device does not have NFC hardware
	 * 
	 */
	
	@Override
	protected void onNfcFeatureNotFound() {
		toast(getString(R.string.noNfcMessage));
	}
	
	@Override
	protected void onNfcTagLost(Intent intent) {
		Log.d(TAG, "Tag lost");
	}
    
	protected void onExternalNfcTagLost(Intent intent) {
		// default to same as native NFC
		onNfcTagLost(intent);
	}
	
	@Override
	protected void onNfcIntentDetected(Intent intent, String action) {
		Tag tag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		
		desfire = DESFire.getInstance(tag);

		if(desfire != null) {
			try {
				testDesfireLite();
			} catch (Throwable t) {
				Log.e(TAG, "Test failed", t);
				showMessage("Problem testing desfire lite: " + t.toString());
			}
		}
	}

	private void showMessage(final String string) {
		Log.d(TAG, string);
		
		runOnUiThread(new Runnable(){
			public void run() {
				textView.append(string + "\n");
			}
		});
		
	}

	/**
	 * Mifare Desfire Card Logic. Copied from NXP sample lite client
	 * 
	 * @throws IOException 
	 */
	protected void testDesfireLite() throws IOException {
		showMessage("DesFire Card Detected :" + "Desfire EV1");
		
		desfire.connect();
		desfire.setTimeOut(2000);
		testDesfireFormat();
		testDesfirepersonalize();
		testDesfireauthenticate();
		testDesfireupdatePICCMasterKey();
		testDesfireauthenticate();
		testDesfireupdateApplicationMasterKey();
		testDesfireWrite();
		testDesfireRead();
		desfire.setTimeOut(2000);
		desfire.close();
	}
	
	/** Desfire read IO Operations. */
	private void testDesfireRead() {

		boolean res = false;
		byte[] data = null;
		try {
			Log.d(TAG, "testDesfireRead, start");
			data = desfire.read(5);
			res = true;
			showMessage(
					"Data Read from the card..." + Utilities.dumpBytes(data)
					);
		} catch (SmartCardException e) {
			Log.d(TAG, "Problem running test", e);
		} finally {
			showMessage(
					"Data Read from the card..." + (res ? Utilities.dumpBytes(data) : "None")
					);
		}
		Log.d(TAG, "testDesfireRead, result is " + res);
		Log.d(TAG, "testDesfireRead, End");
	}

	/** Desfire Write IO Operations. */
	private void testDesfireWrite() {

		byte[] data = new byte[] { 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
				0x11 };

		boolean res = false;
		try {
			Log.d(TAG, "testDesfireWrite, start");
			desfire.write(data);
			res = true;
		} catch (SmartCardException e) {
			Log.d(TAG, "Problem running test", e);
		} finally {
			showMessage("Data Written: " + (res ? Utilities.dumpBytes(data) : "None"));
		}
		Log.d(TAG, "testDesfireWrite, result is " + res);
		Log.d(TAG, "testDesfireWrite, End");

	}

	/** Desfire Update Application master key IO Operations. */
	private void testDesfireupdateApplicationMasterKey() {
		byte[] oldKey = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		byte[] newKey = new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		boolean res = false;
		try {
			Log.d(TAG, "testDesfireupdateApplicationMasterKey, start");
			desfire.updateApplicationMasterKey(oldKey, newKey);
			res = true;
		} catch (SmartCardException e) {
			Log.d(TAG, "Problem running test", e);
		} finally {
			showMessage("Update Application MasterKey: " + res);
		}
		Log.d(TAG, "testDesfireupdateApplicationMasterKey, result is "
				+ res);
		Log.d(TAG, "testDesfireupdateApplicationMasterKey, End");
	}

	/** Desfire Authenticate IO Operations .*/
	private void testDesfireauthenticate() {
		byte[] masterKey = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		byte[] appId = { 0x12, 0x12, 0x12 };
		byte[] appkey = new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		boolean res = false;
		try {
			Log.d(TAG, "testDesfireauthenticate, start");
			desfire.authenticate(masterKey, appId, appkey);
			res = true;
		} catch (SmartCardException e) {
			Log.d(TAG, "Problem running test", e);
		} finally {
			showMessage("Authenticate: " + res);
		}
		Log.d(TAG, "testDesfireauthenticate, result is " + res);
		Log.d(TAG, "testDesfireauthenticate, End");
	}

	/** Desfire personalize Operations. */
	private void testDesfirepersonalize() {
		byte[] mykey = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		byte[] appKey = new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		boolean res = false;
		try {
			Log.d(TAG, "testDesfirepersonalize, start");

			desfire.personalize(mykey, new byte[] { 0x12, 0x12, 0x12 }, appKey);
			res = true;
		} catch (SmartCardException e) {
			Log.d(TAG, "Problem running test", e);
		} finally {
			showMessage("personalize: " + res);
		}
		Log.d(TAG, "testDesfirepersonalize, result is " + res);
		Log.d(TAG, "testDesfirepersonalize, End");

	}

	/** Desfire update PICC Master key Operations . */
	private void testDesfireupdatePICCMasterKey() {
		byte[] oldKey = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		byte[] newKey = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		boolean res = false;
		try {
			Log.d(TAG, "testDesfireupdatePICCMasterKey, start");
			desfire.updatePICCMasterKey(oldKey, newKey);
			res = true;
		} catch (SmartCardException e) {
			Log.d(TAG, "Problem running test", e);
		} finally {
			showMessage("Desfire Update PICC Master Key: " + res);
		}
		Log.d(TAG, "testDesfireupdatePICCMasterKey, result is " + res);
		Log.d(TAG, "testDesfireupdatePICCMasterKey, End");

	}

	/** Desfire Format Operations . */
	private void testDesfireFormat() {
		byte[] mykey = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		boolean res = false;
		try {
			Log.d(TAG, "testDesfireFormat, start");
			desfire.format(mykey);
			res = true;
		} catch (SmartCardException e) {
			Log.d(TAG, "Problem testing desfire format", e);
		} finally {
			showMessage("Format: " + res);
		}
		Log.d(TAG, "testDesfireFormat, result is " + res);
		Log.d(TAG, "testDesfireFormat, End");
	}

	public void toast(int id) {
		toast(getString(id));
	}
	
	public void toast(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}
	

}
