package com.skjolberg.nfc.acs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import android.os.Parcelable;
import android.util.Log;

import com.skjolberg.nfc.NfcReader;


public abstract class AcrReader implements Parcelable, NfcReader {
	
	protected static final String DESCRIPTOR = "android.nfc.INfcTag";

	private static final String TAG = AcrReader.class.getName();
	
	// http://stackoverflow.com/questions/15604145/recommended-approach-for-handling-errors-across-process-using-aidl-android
	public static final int STATUS_OK = 0;
	public static final int STATUS_EXCEPTION = 1;
	
	public static final int VERSION = 1;

	protected String name;
	
	public abstract String getFirmware();
	
	public abstract List<AcrPICC> getPICC();

	public abstract boolean setPICC(AcrPICC ... types);
	
	public abstract byte[] control(int slotNum, int controlCode, byte[] command);

	public abstract byte[] transmit(int slotNum, byte[] command);
	
	public String getName() {
		return name;
	}

	   /**
     * Converts the byte array to HEX string.
     * 
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    protected static String toHexString(byte[] buffer) {
		StringBuilder sb = new StringBuilder();
		for(byte b: buffer)
			sb.append(String.format("%02x", b&0xff));
		return sb.toString();
    }
    
    protected static int readInteger(byte[] response) {
    	try {
			DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

			int version = din.readInt();
			if(version == VERSION) {
				int status = din.readInt();
				
				if(status == STATUS_OK) {
					return din.readInt();
				} else {
					throw new AcrReaderException(din.readUTF());
				}
			} else {
				throw new IllegalArgumentException("Unexpected version " + version);
			}
		} catch (IOException e) {
			throw new AcrReaderException(e);
		}
	}

    protected static boolean readBoolean(byte[] response) {
    	try {
			DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

			int version = din.readInt();
			if(version == VERSION) {
				int status = din.readInt();
				
				if(status == STATUS_OK) {
					return din.readBoolean();
				} else {
					throw new AcrReaderException(din.readUTF());
				}
			} else {
				throw new IllegalArgumentException("Unexpected version " + version);
			}
		} catch (IOException e) {
			throw new AcrReaderException(e);
		}
	}

    protected static String readString(byte[] response) {
		try {
			DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

			int version = din.readInt();
			if(version == VERSION) {
				int status = din.readInt();
				
				if(status == STATUS_OK) {
					return din.readUTF();
				} else {
					throw new AcrReaderException(din.readUTF());
				}
			} else {
				throw new IllegalArgumentException("Unexpected version " + version);
			}
		} catch (IOException e) {
			Log.d(TAG, "Problem reading string length " + response.length + ": "  + toHexString(response));
			throw new AcrReaderException(e);
		}
	}
    
    protected static byte[] readByteArray(byte[] response) {
		try {
			DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

			int version = din.readInt();
			if(version == VERSION) {
				int status = din.readInt();
				
				if(status == STATUS_OK) {
					int length = din.readInt();
					byte[] array = new byte[length];
					din.readFully(array);
					return array;
				} else {
					throw new AcrReaderException(din.readUTF());
				}
			} else {
				throw new IllegalArgumentException("Unexpected version " + version);
			}
		} catch (IOException e) {
			Log.d(TAG, "Problem reading string length " + response.length + ": "  + toHexString(response));
			throw new AcrReaderException(e);
		}
	}
}
