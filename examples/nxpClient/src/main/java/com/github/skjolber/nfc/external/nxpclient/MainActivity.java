package com.github.skjolber.nfc.external.nxpclient;

import android.content.Intent;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nxp.nfclib.classic.IMFClassic;
import com.nxp.nfclib.classic.IMFClassicEV1;
import com.nxp.nfclib.exceptions.CloneDetectedException;
import com.nxp.nfclib.exceptions.ReaderException;
import com.nxp.nfclib.exceptions.SmartCardException;
import com.nxp.nfclib.icode.IICodeSLI;
import com.nxp.nfclib.icode.IICodeSLIL;
import com.nxp.nfclib.icode.IICodeSLIS;
import com.nxp.nfclib.icode.IICodeSLIX;
import com.nxp.nfclib.icode.IICodeSLIX2;
import com.nxp.nfclib.icode.IICodeSLIXL;
import com.nxp.nfclib.icode.IICodeSLIXS;
import com.nxp.nfclib.ntag.INTAGI2Cplus;
import com.nxp.nfclib.ntag.INTag203x;
import com.nxp.nfclib.ntag.INTag210;
import com.nxp.nfclib.ntag.INTag213215216;
import com.nxp.nfclib.ntag.INTag213F216F;
import com.nxp.nfclib.ntag.INTagI2C;
import com.nxp.nfclib.plus.IPlusSL1;
import com.nxp.nfclib.ultralight.IUltralight;
import com.nxp.nfclib.ultralight.IUltralightC;
import com.nxp.nfclib.ultralight.IUltralightEV1;
import com.nxp.nfclib.utils.Utilities;
import com.nxp.nfcliblite.NxpNfcLibLite;
import com.nxp.nfcliblite.Nxpnfcliblitecallback;
import com.github.skjolber.nfc.NfcReader;
import com.github.skjolber.nfc.NfcTag;
import com.github.skjolber.nfc.util.activity.NfcExternalDetectorActivity;
import com.nxp.nfcliblite.cards.IDESFireEV1;
import com.nxp.nfcliblite.cards.IPlus;

public class MainActivity extends NfcExternalDetectorActivity {

	private static final String TAG = MainActivity.class.getName();
	
	private NxpNfcLibLite libInstance = null;

	private TextView textView = null;

	private Nxpnfcliblitecallback mCallback = new Nxpnfcliblitecallback() {

		@Override
		public void onNewTagDetected(Tag tag) {
			Log.d(TAG, "-------------- onNewTagDetected ------");
		}

		@Override
		public void onUltraLightCardDetected(final IUltralight card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onUltraLightCCardDetected(final IUltralightC card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onUltraLightEV1CardDetected(final IUltralightEV1 card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onClassicCardDetected(final IMFClassic card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onClassicEV1CardDetected(final IMFClassicEV1 card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onDESFireCardDetected(final IDESFireEV1 card) {
			Log.i(TAG, card.getClass().getName() + " card detected");

			try {
				card.getReader().close();
				card.getReader().connect();

				desfireCardLogic(card);
			} catch (Throwable t) {
				Log.w(TAG, "Problem DesfireEV1 operations", t);
			}

		}

		@Override
		public void onPlusCardDetected(final IPlus card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onPlusSL1CardDetected(IPlusSL1 card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onICodeSLIDetected(final IICodeSLI card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onICodeSLILDetected(final IICodeSLIL card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onICodeSLISDetected(final IICodeSLIS card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onICodeSLIXDetected(final IICodeSLIX card) {
			Log.i(TAG, card.getClass().getName() + " card detected");

		}

		@Override
		public void onICodeSLIXLDetected(final IICodeSLIXL card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onICodeSLIXSDetected(final IICodeSLIXS card) {
			Log.i(TAG, card.getClass().getName() + " card card");
		}

		@Override
		public void onICodeSLIX2Detected(final IICodeSLIX2 card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onNTag203xCardDetected(final INTag203x card) {
			Log.i(TAG, card.getClass().getName() + " card detected");

		}

		@Override
		public void onNTag210CardDetected(final INTag210 card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onNTag213215216CardDetected(final INTag213215216 card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onNTag213F216FCardDetected(final INTag213F216F card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onNTagI2CCardDetected(final INTagI2C card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onNTagI2CplusCardDetected(final INTAGI2Cplus card) {
			Log.i(TAG, card.getClass().getName() + " card detected");
		}

		@Override
		public void onCardNotSupported(Tag tag) {
			Log.i(TAG, "Card NOT supported");
		}


	};

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
		onNfcIntentDetected(intent, action);
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

		if(intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
			Tag tag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

			try {
				Log.d(TAG, "Filter intent");
				libInstance.filterIntent(tag, mCallback);
			} catch (CloneDetectedException e) {
				Log.d(TAG, "Clone Detected", e);
			}

			try {
				String[] techList = tag.getTechList();

				for (String tech : techList) {
					Log.d(TAG, "Tech " + tech);

					if (tech.equals(android.nfc.tech.MifareUltralight.class.getName())) {
						MifareUltralight mifareUltralight = MifareUltralight.get(tag);
						if(mifareUltralight == null) {
							throw new IllegalArgumentException("No Mifare Ultralight");
						}
						int type = mifareUltralight.getType();

						Log.d(TAG, "Got MifareUltralight type " + type);
					} else if (tech.equals(android.nfc.tech.NfcA.class.getName())) {
						NfcA nfcA = NfcA.get(tag);
						if(nfcA == null) {
							throw new IllegalArgumentException("No NfcA");
						}
						byte[] atqa = nfcA.getAtqa();
						short sak = nfcA.getSak();

						Log.d(TAG, "Got NfcA with ATQA " + toHexString(atqa) + " and sak " + Integer.toHexString(sak));
					} else if (tech.equals(android.nfc.tech.NfcB.class.getName())) {
						NfcB nfcB = NfcB.get(tag);
						if(nfcB == null) {
							throw new IllegalArgumentException("No NfcB");
						}
						byte[] applicationData = nfcB.getApplicationData();
						byte[] protocolInfo = nfcB.getProtocolInfo();

						Log.d(TAG, "Got NfcB with application data " + toHexString(applicationData) + " and protcol info " + toHexString(protocolInfo));
					} else if (tech.equals(android.nfc.tech.NfcF.class.getName())) {
						Log.d(TAG, "Ignore " + tech);
					} else if (tech.equals(android.nfc.tech.NfcV.class.getName())) {
						Log.d(TAG, "Ignore " + tech);
					} else if (tech.equals(android.nfc.tech.IsoDep.class.getName())) {
						android.nfc.tech.IsoDep isoDep = IsoDep.get(tag);
						if(isoDep == null) {
							throw new IllegalArgumentException("No IsoDep");
						}
						boolean hostCardEmulation = intent.getBooleanExtra(NfcTag.EXTRA_HOST_CARD_EMULATION, false);

						if(hostCardEmulation) {
							Log.d(TAG, "Got HCE device");
						} else {
							Log.d(TAG, "Got " + IsoDep.class.getName());
						}

					} else if (tech.equals(android.nfc.tech.MifareClassic.class.getName())) {
						android.nfc.tech.MifareClassic mifareClassic = MifareClassic.get(tag);
						if(mifareClassic == null) {
							throw new IllegalArgumentException("No MifareClassic");
						}

						Log.d(TAG, "Got " + MifareClassic.class.getName());
					} else if (tech.equals(android.nfc.tech.Ndef.class.getName())) {
						Ndef ndef = Ndef.get(tag);
						if(ndef == null) {
							throw new IllegalArgumentException("No NDEF");
						}
						Log.d(TAG, "Got " + Ndef.class.getName());

					} else if (tech.equals(android.nfc.tech.NdefFormatable.class.getName())) {
						NdefFormatable ndefFormatable = NdefFormatable.get(tag);
						if(ndefFormatable == null) {
							throw new IllegalArgumentException("No NdefFormatable");
						}
						Log.d(TAG, "Got " + NdefFormatable.class.getName());

					}
				}

			} catch(Exception e) {
				Log.d(TAG, "Problem processing tag technology", e);
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

	public void toast(int id) {
		toast(getString(id));
	}
	
	public void toast(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}

	/**
	 * Mifare DESFire Card Logic.
	 *
	 * @throws SmartCardException
	 */
	protected void desfireCardLogic(IDESFireEV1 card) throws SmartCardException {

		try {
			card.getReader().setTimeout(2000);
			/* Do the following only if write checkbox is selected */
			testDESFirepersonalize(card);
			testDESFireauthenticate(card);
			testDESFireupdatePICCMasterKey(card);
			testDESFireauthenticate(card);
			testDESFireupdateApplicationMasterKey(card);
			testDESFireauthenticate(card);
			testDESFireWrite(card);
			testDESFireRead(card);

			card.getReader().setTimeout(2000);
			//showCardDetails(mDESFire.getCardDetails());
			/* Do the following only if write checkbox is selected */
			testDESFireFormat(card);

			card.getReader().close();
		} catch (ReaderException e) {
			Log.w(TAG, "Problem running Desfire EV1 operations", e);
		}
	}


	/** DESFire read IO Operations. */
	private void testDESFireRead(IDESFireEV1 card) {

		boolean res = false;
		try {
			Log.d(TAG, "testDESFireRead, start");
			byte[] data = card.read(5);
			res = true;
			showMessage(
					"Data Read from the card..." + Utilities.dumpBytes(data));
		} catch (SmartCardException e) {
			showMessage("Data Read from the card: " + res);
			e.printStackTrace();
		}
		Log.d(TAG, "testDESFireRead, result is " + res);
		Log.d(TAG, "testDESFireRead, End");
	}

	/** DESFire Write IO Operations. */
	private void testDESFireWrite(IDESFireEV1 card) {

		byte[] data = new byte[] { 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
				0x11 };

		boolean res = false;
		try {
			Log.d(TAG, "testDESFireWrite, start");
			card.write(data);
			res = true;
			showMessage("Data Written: " + Utilities.dumpBytes(data));
		} catch (SmartCardException e) {
			showMessage("Data Written: " + res);
			e.printStackTrace();
		}
		Log.d(TAG, "testDESFireWrite, result is " + res);
		Log.d(TAG, "testDESFireWrite, End");

	}

	/** DESFire Update Application master key IO Operations. */
	private void testDESFireupdateApplicationMasterKey(IDESFireEV1 card) {
		byte[] oldKey = new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		byte[] newKey = new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		byte[] masterKey = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		byte[] appId = { 0x12, 0x12, 0x12 };
		boolean res = false;
		try {
			Log.d(TAG, "testDESFireupdateApplicationMasterKey, start");
			card.updateApplicationMasterKey(masterKey, appId, oldKey,
					newKey);
			res = true;
			showMessage("Update Application MasterKey: " + res);
		} catch (SmartCardException e) {
			showMessage("Update Application MasterKey: " + res);
			e.printStackTrace();
		}
		Log.d(TAG, "testDESFireupdateApplicationMasterKey, result is "
				+ res);
		Log.d(TAG, "testDESFireupdateApplicationMasterKey, End");
	}

	/** DESFire Authenticate IO Operations . */
	private void testDESFireauthenticate(IDESFireEV1 card) {
		byte[] masterKey = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		byte[] appId = { 0x12, 0x12, 0x12 };
		byte[] appkey = new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		boolean res = false;
		try {
			Log.d(TAG, "testDESFireauthenticate, start");
			card.authenticate(masterKey, appId, appkey);
			res = true;
			showMessage("Authenticate: " + res);
		} catch (SmartCardException e) {
			showMessage("Authenticate: " + res);
			e.printStackTrace();
		}
		Log.d(TAG, "testDESFireauthenticate, result is " + res);
		Log.d(TAG, "testDESFireauthenticate, End");
	}

	/** DESFire personalize Operations. */
	private void testDESFirepersonalize(IDESFireEV1 card) {
		byte[] mykey = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		byte[] appKey = new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		boolean res = false;
		try {
			Log.d(TAG, "testDESFirepersonalize, start");

			card.personalize(mykey, new byte[]{0x12, 0x12, 0x12}, appKey);
			res = true;
			showMessage("personalize: " + res);
		} catch (SmartCardException e) {
			showMessage("personalize: " + res);
			e.printStackTrace();
		}
		Log.d(TAG, "testDESFirepersonalize, result is " + res);
		Log.d(TAG, "testDESFirepersonalize, End");

	}

	/** DESFire update PICC Master key Operations . */
	private void testDESFireupdatePICCMasterKey(IDESFireEV1 card) {
		byte[] oldKey = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		byte[] newKey = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		boolean res = false;
		try {
			Log.d(TAG, "testDESFireupdatePICCMasterKey, start");
			card.updatePICCMasterKey(oldKey, newKey);
			res = true;
			showMessage("DESFire Update PICC Master Key: " + res);
		} catch (SmartCardException e) {
			showMessage("DESFire Update PICC Master Key: " + res);
			e.printStackTrace();
		}
		Log.d(TAG, "testDESFireupdatePICCMasterKey, result is " + res);
		Log.d(TAG, "testDESFireupdatePICCMasterKey, End");

	}

	/** DESFire Format Operations . */
	private void testDESFireFormat(IDESFireEV1 card) {
		byte[] mykey = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		boolean res = false;
		try {
			Log.d(TAG, "testDESFireFormat, start");
			card.format(mykey);
			res = true;
			showMessage("Format: " + res);
		} catch (SmartCardException e) {
			showMessage("Format: " + res);
			e.printStackTrace();
		}
		Log.d(TAG, "testDESFireFormat, result is " + res);
		Log.d(TAG, "testDESFireFormat, End");
	}


}
