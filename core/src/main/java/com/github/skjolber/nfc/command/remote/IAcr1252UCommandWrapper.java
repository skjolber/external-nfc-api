package com.github.skjolber.nfc.command.remote;

import java.util.List;

import android.util.Log;

import com.github.skjolber.nfc.acs.AcrAutomaticPICCPolling;
import com.github.skjolber.nfc.command.ACR1252Commands;

public class IAcr1252UCommandWrapper extends CommandWrapper {

    private static final String TAG = IAcr1252UCommandWrapper.class.getName();

    private ACR1252Commands commands;

    public IAcr1252UCommandWrapper(ACR1252Commands commands) {
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

    public byte[] getDefaultLEDAndBuzzerBehaviour() {
        Integer value = null;
        Exception exception = null;
        try {
            value = commands.getDefaultLEDAndBuzzerBehaviour2(0);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading default led and buzzer behaviour", e);

            exception = e;
        }

        return returnValue(value, exception);
    }

    public byte[] setDefaultLEDAndBuzzerBehaviour(int parameter) {
        Boolean value = null;
        Exception exception = null;
        try {
            int response = commands.setDefaultLEDAndBuzzerBehaviour(0, parameter);

            value = response == parameter;
        } catch (Exception e) {
            Log.d(TAG, "Problem reading setting default led and buzzer behaviour", e);

            exception = e;
        }

        return returnValue(value, exception);
    }

    public byte[] setAutomaticPICCPolling(int picc) {

        Boolean result = null;
        Exception exception = null;
        try {
            List<AcrAutomaticPICCPolling> parse = AcrAutomaticPICCPolling.parse(picc);

            List<AcrAutomaticPICCPolling> serialize = commands.setAutomaticPICCPolling(0, parse.toArray(new AcrAutomaticPICCPolling[parse.size()]));

            result = parse.equals(serialize);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting automatic PICC", e);

            exception = e;
        }

        return returnValue(result, exception);

    }


    public byte[] getAutomaticPICCPolling() {
        Integer picc = null;
        Exception exception = null;
        try {
            List<AcrAutomaticPICCPolling> parse = commands.getAutomaticPICCPolling(0);

            picc = AcrAutomaticPICCPolling.serialize(parse.toArray(new AcrAutomaticPICCPolling[parse.size()]));
        } catch (Exception e) {
            Log.d(TAG, "Problem reading automatic PICC", e);

            exception = e;
        }

        return returnValue(picc, exception);
    }

    public byte[] getLEDs() {
        Integer picc = null;
        Exception exception = null;
        try {
            picc = commands.getLED2(0);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading LEDs", e);

            exception = e;
        }

        return returnValue(picc, exception);

    }

    public byte[] setLEDs(int leds) {
        Boolean result = null;
        Exception exception = null;
        try {
            result = commands.setLED(0, leds);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting LEDs", e);

            exception = e;
        }

        return returnValue(result, exception);

    }


    public byte[] setBuzzer(boolean enable) {
        Boolean picc = null;
        Exception exception = null;
        try {
            picc = commands.setBuzzer(0, enable);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting buzzer", e);

            exception = e;
        }

        return returnValue(picc, exception);
    }

}
