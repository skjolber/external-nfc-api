package com.skjolberg.nfc.external.hceclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.ComponentName;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import com.skjolberg.nfc.util.Broadcast;
import com.skjolberg.nfc.util.CommandAPDU;

public class ExternalNFCHostApduService extends HostApduService {

	public final static int ISO_SELECT_APPLICATION = 0xA4;//
	public final static int SELECT_APPLICATION = 0x5A;//
    public static final byte OPERATION_OK = (byte)0x00;
    public static final byte APPL_INTEGRITY_ERROR = (byte)0xA1;
    
	private static String TAG = ExternalNFCHostApduService.class.getName();
	
	protected static String AID = "F00A2B4C6D8E";
	
	protected Broadcast broadcast = new Broadcast(this);
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Service created");
		
		CardEmulation cardEmulation = CardEmulation.getInstance(NfcAdapter.getDefaultAdapter(this));
		
		boolean defaultService = cardEmulation.isDefaultServiceForAid(new ComponentName(this, ExternalNFCHostApduService.class), AID);
		
		if(!defaultService) {
			throw new IllegalArgumentException("Expected default service for AID " + AID);
		}
		Log.d(TAG, "Service AID is " + AID);
	}
	
	@Override
	public byte[] processCommandApdu(byte[] buffer, Bundle extras) {
		Log.d(TAG, "Process command Apdu " + toHexString(buffer));

		CommandAPDU command = new CommandAPDU(buffer);
		int ins = command.getINS();
		
		switch (ins) {
			case ISO_SELECT_APPLICATION: {
				Log.d(TAG, "ISO Select Application");
				
				broadcast.broadcast(Broadcast.HOST_CARD_EMULATION_SERVICE_STARTED);
				
				return send(OPERATION_OK);
			}
			case SELECT_APPLICATION: {

				String application = toHexString(command.getData(), 1, 3);
				
				Log.d(TAG, "Selected application " + application);

				broadcast.broadcast(Broadcast.HOST_CARD_EMULATION_APPLICATION_SELECTED, Broadcast.KEY_APPLICATION_ID, application);

				return send(OPERATION_OK);
			}
			default : {
				Log.d(TAG, "Receieved unknown command " + Integer.toHexString(ins));
			}
		}
		
		return send(APPL_INTEGRITY_ERROR);

	}

	public byte[] send(byte command, byte[] contents) {
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

	private byte[] send(byte command) {
		return new byte[]{(byte) 0x91, command};
	}

	@Override
	public void onDeactivated(int reason) {
		Log.i(TAG, "Deactivated: " + reason);
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