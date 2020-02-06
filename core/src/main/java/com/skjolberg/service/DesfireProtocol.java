package com.skjolberg.service;

import java.io.ByteArrayOutputStream;

import android.util.Log;

import com.skjolberg.nfc.command.ACRCommands;

// http://stackoverflow.com/questions/14117025/des-send-and-receive-modes-for-desfire-authentication
// http://nfc-tools.googlecode.com/svn/branches/libfreefare-desfire/libfreefare/mifare_desfire.c
// https://code.google.com/p/java-card-desfire-emulation/
// https://code.google.com/p/java-card-desfire-emulation/source/browse/trunk/java-card-desfire-emulation/DESfire%20Android%20App/src/des/Android/engine/DESfireApi.java
// http://stackoverflow.com/questions/20055122/making-host-card-emulation-work-for-payment
// http://stackoverflow.com/questions/14117025/des-send-and-receive-modes-for-desfire-authentication
// http://stackoverflow.com/questions/15610329/change-cipher-encryption-decryption-mode-while-retaining-iv
// http://noobstah.blogspot.de/2013/04/mifare-desfire-ev1-and-android.html
// http://stackoverflow.com/questions/19296595/android-nfc-isodep-read-file-content
// tja:
// https://code.google.com/p/guard-application-desfire/source/browse/trunk/src/com/example/guardingapp/Utility.java
// https://code.google.com/p/write-ndef-to-desfire/source/browse/trunk/src/CardReader/CardReader.java

// https://code.google.com/p/nfcard/source/browse/src/com/sinpo/xnfc/tech/Iso7816.java
// http://stackoverflow.com/questions/15610329/change-cipher-encryption-decryption-mode-while-retaining-iv
// http://www.nxp.com/documents/other/mf_passwordrequest200902.pdf


public class DesfireProtocol {

    private static final String TAG = DesfireProtocol.class
            .getName();

    /* Commands */
    static final byte GET_VERSION_INFO = (byte) 0x60;
    static final byte GET_APPLICATION_DIRECTORY = (byte) 0x6A;
    static final byte GET_ADDITIONAL_FRAME = (byte) 0xAF;
    static final byte SELECT_APPLICATION = (byte) 0x5A;
    static final byte READ_DATA = (byte) 0xBD;
    static final byte READ_RECORD = (byte) 0xBB;
    static final byte GET_FILES = (byte) 0x6F;
    static final byte GET_FILE_SETTINGS = (byte) 0xF5;

    static final byte INIT_AUTH = (byte) 0x0a;
    static final byte FINISH_AUTH = (byte) 0xAF;

    static final byte GET_KEY_VERSION = (byte) 0x64;

    static final byte WRITE_DATA = (byte) 0x3D;

    static final byte CREATE_APPLICATION = (byte) 0xCA; // {6 bytes: cmd, , , , uint8 settings, uint8 keyNo}
    static final byte DELETE_APPLICATION = (byte) 0xDA; // {4 bytes: }

    /* Status codes */
    public static final byte OPERATION_OK = (byte) 0x00;
    public static final byte NO_CHANGES = (byte) 0x0C;
    public static final byte OUT_OF_EEPROM_ERROR = (byte) 0x0E;
    public static final byte ILLEGAL_COMMAND_CODE = (byte) 0x1C;
    public static final byte INTEGRITY_ERROR = (byte) 0x1E;
    public static final byte NO_SUCH_KEY = (byte) 0x40;
    public static final byte LENGTH_ERROR = (byte) 0x7E;
    public static final byte PERMISSION_ERROR = (byte) 0x9D;
    public static final byte PARAMETER_ERROR = (byte) 0x9E;
    public static final byte APPLICATION_NOT_FOUND = (byte) 0xA0;
    public static final byte APPL_INTEGRITY_ERROR = (byte) 0xA1;
    public static final byte AUTHENTICATION_ERROR = (byte) 0xAE;
    public static final byte ADDITIONAL_FRAME = (byte) 0xAF;
    public static final byte BOUNDARY_ERROR = (byte) 0xBE;
    public static final byte PICC_INTEGRITY_ERROR = (byte) 0xC1;
    public static final byte COMMAND_ABORTED = (byte) 0xCA;
    public static final byte PICC_DISABLED_ERROR = (byte) 0xCD;
    public static final byte COUNT_ERROR = (byte) 0xCE;
    public static final byte DUPLICATE_ERROR = (byte) 0xDE;
    public static final byte EEPROM_ERROR = (byte) 0xEE;
    public static final byte FILE_NOT_FOUND = (byte) 0xF0;
    public static final byte FILE_INTEGRITY_ERROR = (byte) 0xF1;


    static final byte APPLICATION_CRYPTO_DES = (byte) 0x00;
    static final byte APPLICATION_CRYPTO_3K3DES = (byte) 0x40;
    static final byte APPLICATION_CRYPTO_AES = (byte) 0x80;

    private IsoDepWrapper mTagTech;

    public DesfireProtocol(IsoDepWrapper tagTech) {
        mTagTech = tagTech;
    }

    public void selectApp(int appId) throws Exception {
        byte[] appIdBuff = new byte[3];
        appIdBuff[0] = (byte) ((appId & 0xFF0000) >> 16);
        appIdBuff[1] = (byte) ((appId & 0xFF00) >> 8);
        appIdBuff[2] = (byte) (appId & 0xFF);

        sendRequest(SELECT_APPLICATION, appIdBuff);
    }

    public int[] getFileList() throws Exception {
        byte[] buf = sendRequest(GET_FILES);
        int[] fileIds = new int[buf.length];
        for (int x = 0; x < buf.length; x++) {
            fileIds[x] = (int) buf[x];
        }
        return fileIds;
    }

    public byte[] readFile(int fileNo) throws Exception {
        return sendRequest(READ_DATA, new byte[]{
                (byte) fileNo,
                (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x0, (byte) 0x0, (byte) 0x0
        });
    }

    public byte[] readRecord(int fileNum) throws Exception {
        return sendRequest(READ_RECORD, new byte[]{
                (byte) fileNum,
                (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x0, (byte) 0x0, (byte) 0x0
        });
    }

    private byte[] sendRequest(byte command) throws Exception {
        return sendRequest(command, null);
    }

    public VersionInfo getVersionInfo() throws Exception {
        byte[] bytes = sendRequest(GET_VERSION_INFO);

        // Log.d(TAG, "Version info " + ACRCommands.toHexString(bytes));

        return new VersionInfo(bytes);
    }

    private byte[] sendRequest(byte command, byte[] parameters) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] recvBuffer = mTagTech.transceive(wrapMessage(command, parameters));

        while (true) {
            if (recvBuffer[recvBuffer.length - 2] != (byte) 0x91)
                throw new Exception("Invalid response");

            output.write(recvBuffer, 0, recvBuffer.length - 2);

            byte status = recvBuffer[recvBuffer.length - 1];
            if (status == OPERATION_OK) {
                break;
            } else if (status == ADDITIONAL_FRAME) {
                recvBuffer = mTagTech.transceive(wrapMessage(GET_ADDITIONAL_FRAME, null));
            } else if (status == PERMISSION_ERROR) {
                throw new Exception("Permission denied");
            } else {
                throw new Exception("Unknown status code: " + Integer.toHexString(status & 0xFF));
            }
        }

        return output.toByteArray();
    }

    private byte[] wrapMessage(byte command, byte[] parameters) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        stream.write((byte) 0x90);
        stream.write(command);
        stream.write((byte) 0x00);
        stream.write((byte) 0x00);
        if (parameters != null) {
            stream.write((byte) parameters.length);
            stream.write(parameters);
        }
        stream.write((byte) 0x00);

        return stream.toByteArray();
    }

    public boolean createApplication(byte[] aid, byte keyNumber) {
        //sendCommand(Utils.hexStringToByteArray("90 CA 00 00 05 " + Utils.addSpaces(AID)+ " 01 "+Utils.hexDump(keyNumber)+" 00"));//Create App
        throw new RuntimeException();
    }

}
