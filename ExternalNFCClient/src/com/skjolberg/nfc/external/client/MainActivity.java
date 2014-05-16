/***************************************************************************
 * 
 * This file is part of the 'External NFC API' project at
 * https://github.com/skjolber/external-nfc-api
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 ****************************************************************************/

package com.skjolberg.nfc.external.client;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.ndeftools.Message;
import org.ndeftools.Record;
import org.ndeftools.UnsupportedRecord;
import org.ndeftools.wellknown.UriRecord;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.skjolberg.nfc.NfcReader;
import com.skjolberg.nfc.acs.AcrPICC;
import com.skjolberg.nfc.acs.AcrReader;
import com.skjolberg.nfc.desfire.DesfireReader;
import com.skjolberg.nfc.desfire.VersionInfo;
import com.skjolberg.nfc.util.activity.NfcExternalDetectorActivity;

/**
 * Simple example implementation. Most of the interesting parts are in the logging output.
 * 
 * @author thomas
 *
 */

public class MainActivity extends NfcExternalDetectorActivity {

	private static final String TAG = MainActivity.class.getName();
	
	private boolean service = false;
	private boolean reader = false;
	private boolean tag = false;
	
	private NdefFormatable ndefFormatable;
	private Ndef ndef;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		initializeExternalNfc();
		
        setDetecting(true);
	}
    
	@Override
	public void onResume() {
		super.onResume();		

	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	public void onNfcIntentDetected(Intent intent, String action) {

       	setTagPresent(true);

		if(intent.hasExtra(NfcAdapter.EXTRA_ID)) {
			byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
			Log.d(TAG, "Tag id " + toHexString(id).toUpperCase());
			
			setTagId(toHexString(id));
		} else {
			Log.d(TAG, "No tag id");
			
			setTagId(getString(R.string.tagIdNone));
		}

		if(intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) {
			
			Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (messages != null) {
				Log.d(TAG, "NDEF message");

				NdefMessage[] ndefMessages = new NdefMessage[messages.length];
			    for (int i = 0; i < messages.length; i++) {
			        ndefMessages[i] = (NdefMessage) messages[i];
			    }
			    
		    	// read as much as possible
				Message message = new Message();
				for (int i = 0; i < messages.length; i++) {
			    	NdefMessage ndefMessage = (NdefMessage) messages[i];
			        
					for(NdefRecord ndefRecord : ndefMessage.getRecords()) {
						
						Record record;
						try {
							record = Record.parse(ndefRecord);
							
							Log.d(TAG, "NDEF record " + record.getClass().getName());
						} catch (FormatException e) {
							// if the record is unsupported or corrupted, keep as unsupported record
							record = UnsupportedRecord.parse(ndefRecord);
						}
						
						message.add(record);
					}
			    }
				showRecords(message);
			} else {
				hideRecords();
			}
		} else {
			Log.d(TAG, "No NDEF message");
			
			hideRecords();
		}
		
		setTagType(getString(R.string.tagTypeNone));
		if(intent.hasExtra(NfcAdapter.EXTRA_TAG)) {

			Tag tag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	
			try {
				String[] techList = tag.getTechList();
		
				for (String tech : techList) {
					Log.d(TAG, "Tech " + tech);
		
					if (tech.equals(android.nfc.tech.MifareUltralight.class.getName())) {
		
						setTagType(getString(R.string.tagTypeMifareUltralight));
						
						MifareUltralight mifareUltralight = MifareUltralight.get(tag);
						if(mifareUltralight == null) {
							throw new IllegalArgumentException("No Mifare Ultralight");
						}
						try {
							mifareUltralight.connect();
					
							int offset = 4;
							int length;
							
							int type = mifareUltralight.getType();
							switch (type) {
							case MifareUltralight.TYPE_ULTRALIGHT: {
									length = 12;
									
									break;
							}
							case MifareUltralight.TYPE_ULTRALIGHT_C: {
								length = 36;
					
								break;
							}
							default :
								throw new IllegalArgumentException("Unknown mifare ultralight tag " + type);
							}
							
							int readLength = 4;
							
							ByteArrayOutputStream bout = new ByteArrayOutputStream();
							
							for (int i = offset; i < offset + length; i+= readLength) {
								bout.write(mifareUltralight.readPages(i));
							}
							
							byte[] buffer = bout.toByteArray();
							
							StringBuilder builder = new StringBuilder();
							for(int k = 0; k < buffer.length; k+= readLength) {
								builder.append(toHexString(buffer, k, readLength));
								builder.append('\n');
							}
							
							Log.d(TAG, builder.toString());
							
							mifareUltralight.close();
						} catch(Exception e) {
							Log.d(TAG, "Problem processing tag technology", e);
						}
					} else if (tech.equals(android.nfc.tech.NfcA.class.getName())) {
						Log.d(TAG, "Ignore " + tech);
					} else if (tech.equals(android.nfc.tech.NfcB.class.getName())) {
						Log.d(TAG, "Ignore " + tech);
					} else if (tech.equals(android.nfc.tech.NfcF.class.getName())) {
						Log.d(TAG, "Ignore " + tech);
					} else if (tech.equals(android.nfc.tech.NfcV.class.getName())) {
						Log.d(TAG, "Ignore " + tech);
					} else if (tech.equals(android.nfc.tech.IsoDep.class.getName())) {
						android.nfc.tech.IsoDep isoDep = IsoDep.get(tag);
						
						setTagType(getString(R.string.tagTypeDesfire));

						Log.d(TAG, "Got " + isoDep.getClass().getName());
	
						isoDep.connect();
						
						DesfireReader reader = new DesfireReader(isoDep);
						
						VersionInfo versionInfo = reader.getVersionInfo();
						
						Log.d(TAG, "Got version info - hardware version " + versionInfo.getHardwareVersion() + " / software version " + versionInfo.getSoftwareVersion());
						
						isoDep.close();
						
	
					} else if (tech.equals(android.nfc.tech.MifareClassic.class.getName())) {
						android.nfc.tech.MifareClassic mifareClassic = MifareClassic.get(tag);
						
						setTagType(getString(R.string.tagTypeMifareClassic));

						Log.d(TAG, "Got " + mifareClassic.getClass().getName());
					} else if (tech.equals(android.nfc.tech.Ndef.class.getName())) {
						this.ndef = Ndef.get(tag);
						
						Log.d(TAG, "Got " + ndef.getClass().getName());
	
					} else if (tech.equals(android.nfc.tech.NdefFormatable.class.getName())) {
	
						this.ndefFormatable = NdefFormatable.get(tag);
						
						Log.d(TAG, "Got " + ndefFormatable.getClass().getName());
	
					}
	
				}
			} catch(Exception e) {
				Log.d(TAG, "Problem processing tag technology", e);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_ndef_format) {
			ndefFormat();
			return true;
		} else if (id == R.id.action_ndef_write) {
			ndefWrite();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	
	
	private void ndefWrite() {
		Log.d(TAG, "NDEF write");
		
		Message message = new Message();
		
		UriRecord record = new UriRecord();
		record.setUri(Uri.parse("https://github.com/skjolber/external-nfc-api"));
		
		message.add(record);
		
		try {
			ndef.connect();
			
			ndef.writeNdefMessage(message.getNdefMessage());
			
			ndef.close();
		} catch (Exception e) {
			Log.d(TAG, "Problem writing NDEF message", e);
		}
	}

	private void ndefFormat() {
		Log.d(TAG, "NDEF format write");
		
		Message message = new Message();
		
		UriRecord record = new UriRecord();
		record.setUri(Uri.parse("https://github.com/skjolber/external-nfc-api"));
		
		message.add(record);
		
		try {
			ndefFormatable.connect();
			
			ndefFormatable.format(message.getNdefMessage());
			
			ndefFormatable.close();
		} catch (Exception e) {
			Log.d(TAG, "Problem writing NDEF message", e);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		for(int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			if(item.getItemId() == R.id.action_ndef_format) {
				item.setVisible(ndefFormatable != null);
			} else if(item.getItemId() == R.id.action_ndef_write) {
				item.setVisible(ndef != null);
			}
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		private MainActivity activity;
		
		public PlaceholderFragment() {
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
						
			return rootView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
		}
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			 
			this.activity = (MainActivity) activity;
		}
	}

	public void setServiceStarted(final boolean started) {
		this.service = started;
		
		if(started) {
			setTextViewText(R.id.serviceStatus, R.string.serviceStatusStarted);
			
			setViewVisibility(R.id.readerStatusRow, View.VISIBLE);
		} else {
			setTextViewText(R.id.serviceStatus, R.string.serviceStatusStopped);
			
			setViewVisibility(R.id.readerStatusRow, View.GONE);
		}
	}
	
	public void setReaderOpen(final boolean open) {
		this.reader = open;
		if(open) {
			setTextViewText(R.id.readerStatus, R.string.readerStatusOpen);
			
			setViewVisibility(R.id.tagStatusRow, View.VISIBLE);
			setViewVisibility(R.id.tagIdRow, View.GONE);
			setViewVisibility(R.id.tagTypeRow, View.GONE);
		} else {
			setTextViewText(R.id.readerStatus, R.string.readerStatusClosed);
			
			setViewVisibility(R.id.tagStatusRow, View.GONE);
			setViewVisibility(R.id.tagIdRow, View.GONE);
			setViewVisibility(R.id.tagTypeRow, View.GONE);
		}
	}

	private void setViewVisibility(int id, int visibility) {
		View view = findViewById(id);
		view.setVisibility(visibility);
	}

	public void setTagPresent(final boolean present) {
		this.tag = present;
		
		invalidateOptionsMenu();
		
		if(present) {
			setTextViewText(R.id.tagStatus, R.string.tagStatusPresent);
			
			setViewVisibility(R.id.tagIdRow, View.VISIBLE);
			setViewVisibility(R.id.tagTypeRow, View.VISIBLE);
		} else {
			setTextViewText(R.id.tagStatus, R.string.tagStatusAbsent);
			
			setViewVisibility(R.id.tagIdRow, View.GONE);
			setViewVisibility(R.id.tagTypeRow, View.GONE);
			
			hideRecords();
		}
	}

	public void setTextViewText(final int resource, final int string) {
		setTextViewText(resource, getString(string));
	}

	public void setTagType(final String type) {
		setTextViewText(R.id.tagType, type);
	}

	private void clearTagType() {
		setTagType(getString(R.string.tagTypeNone));			
	}

	public void setTagId(final String type) {
		setTextViewText(R.id.tagId, type);
	}

	private void clearTagId() {
		setTagId(getString(R.string.tagIdNone));			
	}
	
	public void setTextViewText(final int resource, final String string) {
		runOnUiThread(new Runnable() {
			public void run() {
				TextView textView = (TextView) findViewById(resource);
				textView.setText(string);
				textView.setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	protected void onExternalNfcServiceStopped() {
		setServiceStarted(false);
	}

	@Override
	protected void onExternalNfcServiceStarted() {
		setServiceStarted(true);
	}

	@Override
	protected void onExternalNfcReaderConnected(Intent intent) {
		setReaderOpen(true);
		
		Log.d(TAG, "Reader open");

    	if(intent.hasExtra(NfcReader.EXTRA_READER_CONTROL)) {
    		AcrReader reader = intent.getParcelableExtra(NfcReader.EXTRA_READER_CONTROL);
    		
    		String name = reader.getName();
    		
    		String firmware = reader.getFirmware();
    		
    		List<AcrPICC> picc = reader.getPICC();
    		
    		Log.d(TAG, "Got reader " + name + " with firmware " + firmware + " and PICC setting " + picc);
    	} else {
    		Log.d(TAG, "No reader supplied");
    	}
	}

	@Override
	protected void onExternalNfcReaderDisconnected() {
		setReaderOpen(false);
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
		setTagPresent(false);
	}
    
	protected void onExternalNfcTagLost(Intent intent) {
		// default to same as native NFC
		this.ndef = null;
		this.ndefFormatable = null;

		onNfcTagLost(intent);
	}
	
	protected void onExternalNfcIntentDetected(Intent intent, String action) {
		// default to same as native NFC
		onNfcIntentDetected(intent, action);
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
	 * 
	 * Show NDEF records in the list
	 * 
	 */
	
	private void showRecords(Message message) {
		// display the message in the gui
		
		ListView listView = (ListView) findViewById(R.id.recordListView);
		View ndefRecords = findViewById(R.id.ndefRecords);
		if(message != null && !message.isEmpty()) {
			Log.d(TAG, "Show " + message.size() + " records");

			ArrayAdapter<? extends Object> adapter = new NdefRecordAdapter(this, message);
			listView.setAdapter(adapter);
			listView.setVisibility(View.VISIBLE);
			
			ndefRecords.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.GONE);
			
			ndefRecords.setVisibility(View.GONE);
		}
		
	}
	
	private void hideRecords() {
		ListView listView = (ListView) findViewById(R.id.recordListView);
		View ndefRecords = findViewById(R.id.ndefRecords);
		
		listView.setVisibility(View.GONE);
		ndefRecords.setVisibility(View.GONE);
	}

}
