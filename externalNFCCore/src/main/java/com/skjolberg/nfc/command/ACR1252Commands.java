package com.skjolberg.nfc.command;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.skjolberg.nfc.acs.Acr1252UReader;
import com.skjolberg.nfc.acs.AcrAutomaticPICCPolling;
import com.skjolberg.nfc.acs.AcrDefaultLEDAndBuzzerBehaviour;
import com.skjolberg.nfc.acs.AcrLED;
import com.skjolberg.nfc.acs.AcrReaderException;

import custom.java.CommandAPDU;

public class ACR1252Commands extends ACRCommands {

    public static final int LED_GREEN = 1 << 1;
    public static final int LED_RED = 1;

    public static final int PICC_OPERATING_PARAMETER_POLL_ISO14443_TYPE_B = 1 << 1;
    public static final int PICC_OPERATING_PARAMETER_POLL_ISO14443_TYPE_A = 1;

    private static final String TAG = ACR1252Commands.class.getName();

    public ACR1252Commands(String name, ReaderWrapper reader) {
        super(reader);
        this.name = name;
    }

    public Integer getPICC(int slot) throws AcrReaderException {
        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x50, 0x00, 0x00};

        byte[] in = new byte[2];

        //reader.transmit(slot, out, out.length, in, in.length);
        synchronized (reader) {
            try {
                reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo, pseudo.length, in, in.length);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }
        if (!isSuccessControl(in)) {
            Log.e(TAG, "Unable to read PICC");

            throw new IllegalArgumentException("Card responded with error code");
        }

        final int operation = in[1] & 0xFF;

        Log.d(TAG, "Read PICC " + Integer.toHexString(operation));

        return operation;
    }

    public Boolean setPICC(int slot, boolean iso14443TypeA, boolean iso14443TypeB) {
        int picc = 0;

        if (iso14443TypeA) {
            picc |= PICC_OPERATING_PARAMETER_POLL_ISO14443_TYPE_A;
        }
        if (iso14443TypeB) {
            picc |= PICC_OPERATING_PARAMETER_POLL_ISO14443_TYPE_B;
        }

        return setPICC(slot, picc);
    }

    public Boolean setPICC(int slot, int picc) throws AcrReaderException {
        if ((picc & 0xFF) != picc) throw new RuntimeException();

        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x51, (byte) picc, 0x00};

        byte[] in;

        synchronized (reader) {
            try {
                in = reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        if (!isSuccessControl(in)) {
            Log.d(TAG, "Unable to set PICC: " + toHexString(in));

            throw new IllegalArgumentException("Card responded with error code");
        }

        final int operation = in[1] & 0xFF;

        if (operation != picc) {
            Log.w(TAG, "Unable to properly update PICC for ACR 1222: Expected " + Integer.toHexString(picc) + " got " + Integer.toHexString(operation));

            return Boolean.FALSE;
        } else {
            Log.d(TAG, "Updated PICC " + Integer.toHexString(operation) + " (" + Integer.toHexString(picc) + ")");

            return Boolean.TRUE;
        }
    }

    public String getFirmware(int slot) throws AcrReaderException {
        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x48, 0x00, 0x00};

        byte[] in;
        synchronized (reader) {
            try {
                in = reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        String firmware = new String(in, Charset.forName("ASCII"));

        Log.d(TAG, "Read firmware " + firmware);

        return firmware;
    }

    public List<AcrAutomaticPICCPolling> setAutomaticPICCPolling(int slot, AcrAutomaticPICCPolling... picc) throws ReaderException {
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x23, new byte[]{(byte) AcrAutomaticPICCPolling.serialize(picc)});

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        return AcrAutomaticPICCPolling.parse(operation);
    }

    public List<AcrAutomaticPICCPolling> getAutomaticPICCPolling(int slot) throws ReaderException {
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x23, 0x00};

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        return AcrAutomaticPICCPolling.parse(operation);
    }

    public List<AcrDefaultLEDAndBuzzerBehaviour> setDefaultLEDAndBuzzerBehaviour(int slot, AcrDefaultLEDAndBuzzerBehaviour... picc) throws ReaderException {
        return Acr1252UReader.parseBehaviour(setDefaultLEDAndBuzzerBehaviour(slot, Acr1252UReader.serializeBehaviour(picc)));
    }

    public int setDefaultLEDAndBuzzerBehaviour(int slot, int picc) throws ReaderException {
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x21, new byte[]{(byte) picc});

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        Log.d(TAG, "Set default LED and buzzer behaviour " + Integer.toHexString(operation) + " (" + picc + ")");

        return operation;
    }

    public List<AcrDefaultLEDAndBuzzerBehaviour> getDefaultLEDAndBuzzerBehaviour(int slot) throws ReaderException {
        final int operation = getDefaultLEDAndBuzzerBehaviour2(slot);

        Log.d(TAG, "Read default LED and buzzer behaviour " + Integer.toHexString(operation));

        return Acr1252UReader.parseBehaviour(operation);
    }

    public int getDefaultLEDAndBuzzerBehaviour2(int slot) throws ReaderException {
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x21, 0x00};

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        Log.d(TAG, "Read default LED and buzzer behaviour " + Integer.toHexString(operation));

        return operation;
    }

    public boolean setLED(int slot, int state) throws ReaderException {

        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x29, new byte[]{(byte) (state)});

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response, 1)) {
            throw new IllegalArgumentException();
        }

        Log.d(TAG, "Set LED state to " + (0xFF & response.getData()[0]));

        return true;
    }


    public List<AcrLED> getLED(int slot) throws ReaderException {
        int operation = getLED2(slot);

        Log.d(TAG, "Read LED state " + Integer.toHexString(operation));

        List<AcrLED> leds = new ArrayList<AcrLED>();

        if ((operation & LED_GREEN) != 0) {
            leds.add(AcrLED.GREEN);
        }

        if ((operation & LED_RED) != 0) {
            leds.add(AcrLED.RED);
        }

        return leds;

    }

    public int getLED2(int slot) throws ReaderException {
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x29, 0x00};

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response, 1)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        return operation;
    }

    public boolean setBuzzer(int slot, boolean enable) throws AcrReaderException {
        byte[] pseudo = new byte[]{(byte) 0xE0, 0x00, 0x28, (byte) (enable ? 0xFF : 0x00), 0x00};

        byte[] in = new byte[300];

        synchronized (reader) {
            try {
                reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo, pseudo.length, in, in.length);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        if (isSuccessControl(in)) {
            Log.d(TAG, "Successfully set buzzer " + (enable ? "on" : "off"));

            return true;
        } else {
            Log.d(TAG, "Failed to set buzzer");

            return false;
        }
    }
}
