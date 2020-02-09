package com.github.skjolber.nfc.util.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.github.skjolber.nfc.NfcReader;
import com.github.skjolber.nfc.NfcService;
import com.github.skjolber.nfc.NfcTag;

/**
 * 
 * Abstract {@link Activity} for detecting incoming NFC messages in external readers.<br/><br/>
 * 
 *  - detect NFC service.<br/>
 *  - detect NFC reader.<br/>
 *  - detect NFC tags.<br/>
 *  <br/>
 *  <br/>
 *  NFC tags from external readers can be interacted with in the same way as tags from native NFC.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public abstract class NfcExternalDetectorActivity extends NfcDetectorActivity {

	private static final String TAG = NfcExternalDetectorActivity.class.getName();

	private boolean recieveTagBroadcasts = false;
	private boolean recieveReaderBroadcasts = false;
	private boolean recieveServiceBroadcasts = false;
	
    private final BroadcastReceiver tagReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            
            if (NfcTag.ACTION_NDEF_DISCOVERED.equals(action)) {
            	Log.d(TAG, "Process NDEF discovered action");

            	onExternalNfcIntentDetected(intent, NfcTag.ACTION_NDEF_DISCOVERED);
            } else if (NfcTag.ACTION_TAG_DISCOVERED.equals(action)) {
            	Log.d(TAG, "Process TAG discovered action");

            	onExternalNfcIntentDetected(intent, NfcTag.ACTION_TAG_DISCOVERED);
            } else  if (NfcTag.ACTION_TECH_DISCOVERED.equals(action)) {
            	Log.d(TAG, "Process TECH discovered action");;

            	onExternalNfcIntentDetected(intent, NfcTag.ACTION_TECH_DISCOVERED);
            } else  if (NfcTag.ACTION_TAG_LEFT_FIELD.equals(action)) {
            	Log.d(TAG, "Process tag left field");
            	
            	onExternalNfcTagLost(intent);
            } else {
            	Log.d(TAG, "Ignore action " + action);
            }
        }

    };
    
    private final BroadcastReceiver readerReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            
            if (NfcReader.ACTION_READER_OPENED.equals(action)) {
            	Log.d(TAG, "Reader opened");

            	onExternalNfcReaderOpened(intent);
            } else if (NfcReader.ACTION_READER_CLOSED.equals(action)) {
            	Log.d(TAG, "Reader closed");
            	
            	onExternalNfcReaderClosed(intent);
            } else {
            	throw new IllegalArgumentException("Unexpected action " + action);
            }
        }

    };
    
    private final BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            
            if (NfcService.ACTION_SERVICE_STARTED.equals(action)) {
            	Log.d(TAG, "Service started");
            	
            	onExternalNfcServiceStarted(intent);
            } else  if (NfcService.ACTION_SERVICE_STOPPED.equals(action)) {
            	Log.d(TAG, "Service stopped");
            	
            	onExternalNfcServiceStopped(intent);
            } else {
            	throw new IllegalArgumentException("Unexpected action " + action);
            }
        }

    };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		startReceivingTagBroadcasts();
		startReceivingReaderBroadcasts();
		startReceivingServiceBroadcasts();
	}
	
	protected void initializeExternalNfc() {
		broadcast(NfcService.ACTION_SERVICE_STATUS);
		broadcast(NfcReader.ACTION_READER_STATUS);
	}
	
    protected void startService() {
		Intent intent = new Intent();
		intent.setClassName("com.github.skjolber.nfc.external", "com.github.skjolber.nfc.service.BackgroundUsbService");
        startService(intent);
    }
    
	@Override
	protected void onDestroy() {
		stopReceivingTagBroadcasts();
		stopReceivingReaderBroadcasts();
		stopReceivingServiceBroadcasts();
		
		super.onDestroy();
	}

	protected void stopService() {
		Intent intent = new Intent();
		intent.setClassName("com.github.skjolber.nfc.external", "com.github.skjolber.nfc.service.BackgroundUsbService");
        stopService(intent);
	}

	private void startReceivingTagBroadcasts() {
		if(!recieveTagBroadcasts) {
			Log.d(TAG, "Start receiving tag broadcasts");
			
			recieveTagBroadcasts = true;
			
    		// register receiver
    		IntentFilter filter = new IntentFilter();
            filter.addAction(NfcTag.ACTION_NDEF_DISCOVERED);
            filter.addAction(NfcTag.ACTION_TAG_DISCOVERED);
            filter.addAction(NfcTag.ACTION_TECH_DISCOVERED);
            filter.addAction(NfcTag.ACTION_TAG_LEFT_FIELD);
            
            registerReceiver(tagReceiver, filter);
		}
	}

	private void stopReceivingTagBroadcasts() {
		if(recieveTagBroadcasts) {
			Log.d(TAG, "Stop receiving tag broadcasts");

			recieveTagBroadcasts = false;

            unregisterReceiver(tagReceiver);
		}
	}
	
	private void startReceivingReaderBroadcasts() {
		if(!recieveReaderBroadcasts) {
			Log.d(TAG, "Start receiving reader broadcasts");
			
			recieveReaderBroadcasts = true;
			
    		// register receiver
    		IntentFilter filter = new IntentFilter();
            filter.addAction(NfcReader.ACTION_READER_OPENED);
            filter.addAction(NfcReader.ACTION_READER_CLOSED);
            
            registerReceiver(readerReceiver, filter);
		}
	}

	private void stopReceivingReaderBroadcasts() {
		if(recieveReaderBroadcasts) {
			Log.d(TAG, "Stop receiving broadcasts");

			recieveReaderBroadcasts = false;

            unregisterReceiver(readerReceiver);
		}
	}
	
	private void startReceivingServiceBroadcasts() {
		if(!recieveServiceBroadcasts) {
			Log.d(TAG, "Start receiving service broadcasts");
			
			recieveServiceBroadcasts = true;
			
    		// register receiver
    		IntentFilter filter = new IntentFilter();
            filter.addAction(NfcService.ACTION_SERVICE_STARTED);
            filter.addAction(NfcService.ACTION_SERVICE_STOPPED);
            
            registerReceiver(serviceReceiver, filter);
		}
	}

	private void stopReceivingServiceBroadcasts() {
		if(recieveServiceBroadcasts) {
			Log.d(TAG, "Stop receiving broadcasts");

			recieveServiceBroadcasts = false;

            unregisterReceiver(serviceReceiver);
		}
	}
	/**
     * Converts the byte array to HEX string.
     * 
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer) {
		return toHexString(buffer, 0, buffer.length);
    }
 
	
	/**
     * Converts the byte array to HEX string.
     * 
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer, int offset, int length) {
		StringBuilder sb = new StringBuilder();
		for(int i = offset; i < offset + length; i++) {
			byte b = buffer[i];
			sb.append(String.format("%02x", b&0xff));
		}
		return sb.toString().toUpperCase();
    }
	
	protected void broadcast(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		sendBroadcast(intent); 
	}

	protected abstract void onExternalNfcServiceStopped(Intent intent);

	protected abstract void onExternalNfcServiceStarted(Intent intent);

    /**
     * 
     * An external NFC reader was connected
     * @param intent
     * 
     */
    
    protected abstract void onExternalNfcReaderOpened(Intent intent);

    /**
     * 
     * An external NFC reader was disconnected
     * @param intent 
     *
     */

    protected abstract void onExternalNfcReaderClosed(Intent intent);

	protected abstract void onExternalNfcTagLost(Intent intent);
	
	protected abstract void onExternalNfcIntentDetected(Intent intent, String action);

}
