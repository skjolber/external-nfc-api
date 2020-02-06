package com.skjolberg.nfc.util;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;



public class Broadcast {

    private static final String TAG = Broadcast.class.getName();

    public static final String HOST_CARD_EMULATION_SERVICE_STARTED = Broadcast.class.getName()+ ":SERVICE_STARTED";
    public static final String HOST_CARD_EMULATION_APPLICATION_SELECTED = Broadcast.class.getName()+ ":APPLICATION_SELECTED";

    public static final String KEY_APPLICATION_ID = "AUTHORIZATION_ID";
	
	private Context context;
	
	public Broadcast(Context context) {
		this.context = context;
	}

	public void broadcast(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		sendBroadcast(intent); 
	}
	
	public void broadcast(String action, String key, boolean value) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(key, value);
		
		sendBroadcast(intent); 
	}

	public void broadcast(String action, String key, String value) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(key, value);
		
		sendBroadcast(intent); 
	}
	
	public void broadcast(String action, String key, String value, String booleanKey, boolean booleanValue) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(key, value);
		intent.putExtra(booleanKey, booleanValue);
		
		sendBroadcast(intent); 
	}


	public void broadcast(String action, String key, byte[] value) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(key, value);
		
		sendBroadcast(intent); 
	}

	public void broadcast(String action, String key, Parcelable[] value) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(key, value);
		
		sendBroadcast(intent); 
	}
	
	private void sendBroadcast(Intent intent) {
		Log.d(TAG, "Broadcast " + intent.getAction());
		
		context.sendBroadcast(intent);
	}
	
}
