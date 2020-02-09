package com.github.skjolber.nfc.desfire;

import java.io.ByteArrayOutputStream;

import android.nfc.tech.IsoDep;
import android.util.Log;

public class DesfireReader {
	
	private static final String TAG = DesfireReader.class.getName();

    /* Commands */
    public static final byte GET_VERSION_INFO    = (byte) 0x60;
    public static final byte GET_APPLICATION_DIRECTORY = (byte) 0x6A;
    public static final byte GET_ADDITIONAL_FRAME      = (byte) 0xAF;
    public static final byte SELECT_APPLICATION        = (byte) 0x5A;
    public static final byte READ_DATA                 = (byte) 0xBD;
    public static final byte READ_RECORD               = (byte) 0xBB;
    public static final byte GET_FILES                 = (byte) 0x6F;
    public static final byte GET_FILE_SETTINGS         = (byte) 0xF5;

    public static final byte INIT_AUTH         = (byte) 0x0a;
    public static final byte FINISH_AUTH         = (byte) 0xAF;

    public static final byte GET_KEY_VERSION = (byte) 0x64;
    
    public static final byte CREATE_APPLICATION = (byte) 0xCA; // {6 bytes: cmd, , , , uint8 settings, uint8 keyNo}
    public static final byte DELETE_APPLICATION = (byte) 0xDA; // {4 bytes: }

    /* Status codes */
    public static final byte STATUS_OPERATION_OK = (byte)0x00;
    public static final byte STATUS_PERMISSION_ERROR = (byte)0x9D;
    public static final byte STATUS_ADDITIONAL_FRAME = (byte)0xAF;
    
    /**
     * Converts the byte array to HEX string.
     * 
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer) {
		StringBuilder sb = new StringBuilder();
		for(byte b: buffer)
			sb.append(String.format("%02x", b&0xff));
		return sb.toString();
    }
    
    private IsoDep isoDep;

    public DesfireReader(IsoDep isoDep) {
        this.isoDep = isoDep;
    }

    public int[] getApplicationDirectory() throws Exception {
        byte[] apps = sendRequest(GET_APPLICATION_DIRECTORY);

        int[] appIds = new int[apps.length / 3];

        for (int app = 0; app < apps.length; app += 3) {
            appIds[app / 3] = ((apps[app] & 0xFF) << 16) + ((apps[app + 1] & 0xFF) << 8) + (apps[app + 2] & 0xFF);;
        }

        return appIds;
    }
    
    public void selectApplication(int appId) throws Exception {
        byte[] buffer = new byte[3];
        buffer[0] = (byte) ((appId & 0xFF0000) >> 16);
        buffer[1] = (byte) ((appId & 0xFF00) >> 8);
        buffer[2] = (byte) (appId & 0xFF);

        sendRequest(SELECT_APPLICATION, buffer);
    }

    public int[] getFiles() throws Exception {
        byte[] buf = sendRequest(GET_FILES);
        int[] fileIds = new int[buf.length];
        for (int x = 0; x < buf.length; x++) {
            fileIds[x] = (int)buf[x];
        }
        return fileIds;
    }

    public DesfireFileSettings getFileSettings (int fileNo) throws Exception {
        byte[] data = sendRequest(GET_FILE_SETTINGS, new byte[] { (byte) fileNo });
        return DesfireFileSettings.Create(data);
    }

    public byte[] readFile (int fileNo) throws Exception {
        return sendRequest(READ_DATA, new byte[] {
            (byte) fileNo,
            (byte) 0x0, (byte) 0x0, (byte) 0x0,
            (byte) 0x0, (byte) 0x0, (byte) 0x0
        });
    }

    public byte[] readRecord (int fileNum) throws Exception {
        return sendRequest(READ_RECORD, new byte[]{
                (byte) fileNum,
                (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x0, (byte) 0x0, (byte) 0x0
        });
    }

    private byte[] sendRequest (byte command) throws Exception {
        return sendRequest(command, null);
    }
    
    public VersionInfo getVersionInfo() throws Exception {
    	byte[] bytes = sendRequest(GET_VERSION_INFO);

    	Log.d(TAG, "Got version info " + toHexString(bytes));
    	
    	return new VersionInfo(bytes);
    }

    private byte[] sendRequest (byte command, byte[] parameters) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Log.d(TAG, "Request : " + toHexString(wrapCommand(command, parameters)));

        byte[] recvBuffer = isoDep.transceive(wrapCommand(command, parameters));

        Log.d(TAG, "Response: " + toHexString(recvBuffer));
        
        while (true) {
            output.write(recvBuffer, 1, recvBuffer.length - 1);

            byte status = recvBuffer[0];
            if (status == STATUS_OPERATION_OK) {
                break;
            } else if (status == STATUS_ADDITIONAL_FRAME) {
                Log.d(TAG, "Request : " + toHexString(wrapCommand(command, parameters)));
                
                recvBuffer = isoDep.transceive(wrapCommand(GET_ADDITIONAL_FRAME, null));
                
                Log.d(TAG, "Response: " + toHexString(recvBuffer));
             } else if (status == STATUS_PERMISSION_ERROR) {
                throw new DesfireException(status, "Permission denied");
            } else {
                throw new DesfireException(status, "Unknown status code: " + Integer.toHexString(status & 0xFF));
            }
        }
        
        return output.toByteArray();
    }

    private byte[] wrapCommand(byte command, byte[] parameters) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        stream.write(command);
        if (parameters != null) {
            stream.write(parameters.length);
            stream.write(parameters);
        }

        return stream.toByteArray();
    }
    
}
