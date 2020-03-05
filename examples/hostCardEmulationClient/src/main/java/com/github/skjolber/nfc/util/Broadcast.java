package com.github.skjolber.nfc.util;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;



public class Broadcast {

	private static final String TAG = Broadcast.class.getName();

    public static final String HOST_CARD_EMULATION_ACTION_PROCESS_COMMAND_ADPU = Broadcast.class.getName()+ ":PROCESS_COMMAND_ADPU";
	public static final String HOST_CARD_EMULATION_ACTION_DEACTIVATED = Broadcast.class.getName()+ ":DEACTIVATED";
	public static final String HOST_CARD_EMULATION_EXTRA_COMMAND = Broadcast.class.getName()+ "extra:COMMAND";
	public static final String HOST_CARD_EMULATION_EXTRA_RESPONSE = Broadcast.class.getName()+ "extra:RESPONSE";

	private Context context;
	
	public Broadcast(Context context) {
		this.context = context;
	}

	public Intent broadcast(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		sendBroadcast(intent);

		return intent;
	}
	
	public Intent broadcast(String action, String key, boolean value) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(key, value);

		return sendBroadcast(intent);
	}

	public Intent broadcast(String action, String key, String value) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(key, value);

		return sendBroadcast(intent);
	}
	
	public Intent broadcast(String action, String key, String value, String booleanKey, boolean booleanValue) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(key, value);
		intent.putExtra(booleanKey, booleanValue);

		return sendBroadcast(intent);
	}


	public Intent broadcast(String action, String key, byte[] value) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(key, value);

		return sendBroadcast(intent);
	}

	public Intent broadcast(String action, String key, Parcelable[] value) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(key, value);
		
		return sendBroadcast(intent);
	}
	
	public Intent sendBroadcast(Intent intent) {
		Log.d(TAG, "Broadcast " + intent.getAction());
		
		context.sendBroadcast(intent);

		return intent;
	}
	
}
