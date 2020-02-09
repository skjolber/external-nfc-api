package com.github.skjolber.nfc.command.remote;

import android.util.Log;

import com.github.skjolber.nfc.command.ACR122Commands;

public class IAcr122UCommandWrapper extends CommandWrapper {

    private static final String TAG = IAcr122UCommandWrapper.class.getName();

    private ACR122Commands commands;

    public IAcr122UCommandWrapper(ACR122Commands commands) {
        this.commands = commands;
    }

    public byte[] getFirmware() {

        String firmware = null;
        Exception exception = null;
        try {
            firmware = commands.getFirmware(0);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading firmware", e);

            exception = e;
        }

        return returnValue(firmware, exception);
    }

    public byte[] getPICC() {
        Integer picc = null;
        Exception exception = null;
        try {
            picc = commands.getPICC(0);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading PICC", e);

            exception = e;
        }

        return returnValue(picc, exception);
    }

    public byte[] setPICC(int picc) {

        Boolean result = null;
        Exception exception = null;
        try {
            result = commands.setPICC(0, picc);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting PICC", e);

            exception = e;
        }

        return returnValue(result, exception);

    }

    public byte[] setBuzzerForCardDetectionAcr122U(boolean enable) {
        Boolean picc = null;
        Exception exception = null;
        try {
            picc = commands.setBuzzerForCardDetection(0, enable);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting buzzer", e);

            exception = e;
        }

        return returnValue(picc, exception);
    }

    public byte[] control(int slotNum, int controlCode, byte[] command) {

        byte[] value = null;
        Exception exception = null;
        try {
            value = commands.control(slotNum, controlCode, command);
        } catch (Exception e) {
            Log.d(TAG, "Problem control", e);

            exception = e;
        }

        return returnValue(value, exception);
    }

    public byte[] transmit(int slotNum, byte[] command) {
        byte[] value = null;
        Exception exception = null;
        try {
            value = commands.transmit(slotNum, command);
        } catch (Exception e) {
            Log.d(TAG, "Problem transmit", e);

            exception = e;
        }

        return returnValue(value, exception);
    }
}
