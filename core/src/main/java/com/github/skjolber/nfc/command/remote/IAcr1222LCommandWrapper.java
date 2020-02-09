package com.github.skjolber.nfc.command.remote;

import android.util.Log;

import com.github.skjolber.nfc.acs.AcrReaderException;
import com.github.skjolber.nfc.command.ACR1222Commands;
import com.github.skjolber.nfc.command.FontSet;

public class IAcr1222LCommandWrapper extends CommandWrapper {

    private static final String TAG = IAcr1222LCommandWrapper.class.getName();

    private ACR1222Commands commands;

    public IAcr1222LCommandWrapper(ACR1222Commands commands) {
        this.commands = commands;
    }

    public byte[] getFirmware() {

        String firmware = null;
        Exception exception = null;
        try {
            firmware = commands.getFirmware(0);

            Log.d(TAG, "Got firmware " + firmware);

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
            picc = commands.getACR122PICC(0);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading PICC", e);

            exception = e;
        }

        return returnValue(picc, exception);
    }

    public byte[] setPICC(int picc) {

        Boolean success = null;
        Exception exception = null;
        try {
            success = commands.setACR122PICC(0, picc);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting PICC", e);

            exception = e;
        }

        return returnValue(success, exception);

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
            value = commands.setDefaultLEDAndBuzzerBehaviour(0, parameter);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading setting default led and buzzer behaviour", e);

            exception = e;
        }

        return returnValue(value, exception);
    }

    public byte[] setDefaultLEDAndBuzzerBehaviours(boolean piccPollingStatusLED, boolean piccActivationStatusLED, boolean buzzerForCardInsertionOrRemoval, boolean cardOperationBlinkingLED) throws AcrReaderException {
        Boolean value = null;
        Exception exception = null;
        try {
            value = commands.setDefaultLEDAndBuzzerBehaviours(0, piccPollingStatusLED, piccActivationStatusLED, buzzerForCardInsertionOrRemoval, cardOperationBlinkingLED);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting buzzer", e);

            exception = e;
        }

        return returnValue(value, exception);
    }

    public byte[] lightLED(boolean ready, boolean progress, boolean complete, boolean error) {
        Boolean value = null;
        Exception exception = null;
        try {
            value = commands.lightLED(0, ready, progress, complete, error);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting light", e);

            exception = e;
        }

        return returnValue(value, exception);
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

    public byte[] clearDisplay() {
        Boolean result = null;
        Exception exception = null;
        try {
            result = commands.clearLCD(0);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting LEDs", e);

            exception = e;
        }

        return returnValue(result, exception);
    }

    public byte[] displayText(char fontId, boolean styleBold, int line, int position, byte[] message) {

        FontSet font;
        if (fontId == 'a') {
            font = FontSet.Font1;
        } else if (fontId == 'b') {
            font = FontSet.Font2;
        } else if (fontId == 'c') {
            font = FontSet.Font3;
        } else {
            throw new IllegalArgumentException("Unknown font " + fontId);
        }

        Boolean result = null;
        Exception exception = null;
        try {
            result = commands.displayText(0, font, styleBold, line, position, message);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting LEDs", e);

            exception = e;
        }

        return returnValue(result, exception);
    }

    public byte[] lightDisplayBacklight(boolean on) {
        Boolean result = null;
        Exception exception = null;
        try {
            result = commands.lightBacklight(0, on);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting LEDs", e);

            exception = e;
        }

        return returnValue(result, exception);
    }


}
