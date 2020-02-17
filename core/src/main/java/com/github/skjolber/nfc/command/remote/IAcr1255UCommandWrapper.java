package com.github.skjolber.nfc.command.remote;

import android.util.Log;

import com.github.skjolber.nfc.acs.AcrAutomaticPICCPolling;
import com.github.skjolber.nfc.command.ACR1255Commands;

import java.util.List;

public class IAcr1255UCommandWrapper extends CommandWrapper {

    private static final String TAG = IAcr1255UCommandWrapper.class.getName();

    private ACR1255Commands commands;

    public IAcr1255UCommandWrapper(ACR1255Commands commands) {
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

    public byte[] getAutoPPS() {
        byte[] picc = null;
        Exception exception = null;
        try {
            picc = commands.getAutoPPS(0);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading auto PPS", e);

            exception = e;
        }

        return returnValue(picc, exception);
    }

    public byte[] setAutoPPS(byte tx, byte rx) {
        byte[] picc = null;
        Exception exception = null;
        try {
            picc = commands.setAutoPPS(0, tx, rx);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading LEDs", e);

            exception = e;
        }

        return returnValue(picc, exception);
    }

    public byte[] getAntennaFieldStatus() {
        Byte picc = null;
        Exception exception = null;
        try {
            picc = commands.getAntennaFieldStatus(0);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading antenna field status", e);

            exception = e;
        }

        return returnValue(picc, exception);
    }

    public byte[] setAntennaField(boolean b) {
        Boolean result = null;
        Exception exception = null;
        try {
            result = commands.setAntennaField(0, b);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting antenna field", e);

            exception = e;
        }

        return returnValue(result, exception);


    }

    public byte[] getBluetoothTransmissionPower() {
        Byte picc = null;
        Exception exception = null;
        try {
            picc = commands.getBluetoothTransmissionPower(0);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading bluetooth transmission power", e);

            exception = e;
        }

        return returnValue(picc, exception);
    }

    public byte[] setBluetoothTransmissionPower(byte b) {
        Boolean result = null;
        Exception exception = null;
        try {
            result = commands.setBluetoothTransmissionPower(0, b);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting bluetooth transmission power", e);

            exception = e;
        }

        return returnValue(result, exception);
    }


    public byte[] setSleepModeOption(byte b) {
        Boolean result = null;
        Exception exception = null;
        try {
            result = commands.setSleepModeOption(0, b);
        } catch (Exception e) {
            Log.d(TAG, "Problem setting sleep mode", e);

            exception = e;
        }

        return returnValue(result, exception);
    }
}
