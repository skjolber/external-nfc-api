package com.github.skjolber.nfc.external.hceclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.skjolber.nfc.util.Broadcast;
import com.github.skjolber.nfc.util.CommandAPDU;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 *
 * Hello HCE service.
 *
 */

public class HelloHostApduService extends HostApduService {

	public final static int ISO_SELECT_APPLICATION = 0xA4;//
	public final static int SELECT_APPLICATION = 0x5A;//
    public static final byte OPERATION_OK = (byte)0x00;
    public static final byte APPL_INTEGRITY_ERROR = (byte)0xA1;
    
	private static String TAG = HelloHostApduService.class.getName();
	
	protected Broadcast broadcast = new Broadcast(this);
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, getClass().getName() + " HCE service created");
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

		byte[] response = response(OPERATION_OK, String.format("Hello operation %02X", ins).getBytes(StandardCharsets.UTF_8));
		if(ins == ISO_SELECT_APPLICATION || ins == SELECT_APPLICATION) {
			String application = toHexString(command.getData(), 1, 3);

			Log.i(TAG, "Selected Application " + application);

			response = response(OPERATION_OK, new byte[]{0x03, 0x04, 0x05});
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

		Intent intent = new Intent(this, HceActivity.class);
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