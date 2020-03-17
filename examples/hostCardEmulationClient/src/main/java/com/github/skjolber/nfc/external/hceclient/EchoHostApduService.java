package com.github.skjolber.nfc.external.hceclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.skjolber.nfc.util.Broadcast;
import com.github.skjolber.nfc.util.CommandAPDU;

public class EchoHostApduService extends HostApduService {

	public final static int ISO_SELECT_APPLICATION = 0xA4;//
	public final static int SELECT_APPLICATION = 0x5A;//
    public static final byte OPERATION_OK = (byte)0x00;
    public static final byte APPL_INTEGRITY_ERROR = (byte)0xA1;
    
	private static String TAG = EchoHostApduService.class.getName();
	
	protected static String AID = "F00A2B4C6D8E";
	
	protected Broadcast broadcast = new Broadcast(this);
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "HCE service created");
		
		CardEmulation cardEmulation = CardEmulation.getInstance(NfcAdapter.getDefaultAdapter(this));
		
		boolean defaultService = cardEmulation.isDefaultServiceForAid(new ComponentName(this, EchoHostApduService.class), AID);
		
		if(!defaultService) {
			throw new IllegalArgumentException("Expected default service for AID " + AID);
		}
		Log.d(TAG, "HCE service AID is " + AID);
	}
	
	@Override
	public byte[] processCommandApdu(byte[] request, Bundle extras) {
		Log.d(TAG, "Process command ADPU " + toHexString(request));

		CommandAPDU command = new CommandAPDU(request);
		int ins = command.getINS();

		if(extras != null) {
			for (String s : extras.keySet()) {
				Log.d(TAG, "Got extras " + s + ": " + extras.get(s));
			}
		}

		byte[] response = response(OPERATION_OK);
		if(ins == ISO_SELECT_APPLICATION || ins == SELECT_APPLICATION) {
			String application = toHexString(command.getData(), 1, 3);

			Log.i(TAG, "Selected Application " + application);
		} else {
			Log.i(TAG, "Received unknown command " + String.format("0x%08X", ins));

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

			boolean pingPong = prefs.getBoolean(PreferencesActivity.PREFERENCE_HOST_CARD_EMULATION_PING_PONG, true);

			if (pingPong) {
				if (PingPong.isPing(request)) {
					// respond with pong
					Log.d(TAG, "Detected ping command, respond with pong");

					return PingPong.getPong();
				}
			}
		}

		Intent intent = new Intent(this, EchoHostAdpuServiceActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Broadcast.HOST_CARD_EMULATION_ACTION_PROCESS_COMMAND_ADPU);

		intent.putExtra(Broadcast.HOST_CARD_EMULATION_EXTRA_COMMAND, request);
		intent.putExtra(Broadcast.HOST_CARD_EMULATION_EXTRA_RESPONSE, request);

		startActivity(intent);

		Intent b = new Intent();
		b.setAction(Broadcast.HOST_CARD_EMULATION_ACTION_PROCESS_COMMAND_ADPU);
		b.putExtra(Broadcast.HOST_CARD_EMULATION_EXTRA_COMMAND, request);
		b.putExtra(Broadcast.HOST_CARD_EMULATION_EXTRA_RESPONSE, response);

		broadcast.sendBroadcast(b);

		return response;
	}

	public byte[] response(byte command, byte[] contents) {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(contents);
			bout.write(0x91);
			bout.write(command);
			return bout.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	private byte[] response(byte command) {
		return new byte[]{(byte) 0x91, command};
	}

	@Override
	public void onDeactivated(int reason) {
		Log.i(TAG, "Deactivated: " + reason);

		broadcast.broadcast(Broadcast.HOST_CARD_EMULATION_ACTION_DEACTIVATED);
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

}