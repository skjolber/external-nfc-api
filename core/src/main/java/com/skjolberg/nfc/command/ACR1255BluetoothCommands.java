package com.skjolberg.nfc.command;

import android.util.Log;

import com.acs.bluetooth.BluetoothReader;
import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.skjolberg.nfc.acs.AcrAutomaticPICCPolling;
import com.skjolberg.nfc.acs.AcrLED;
import com.skjolberg.service.BluetoothBackgroundService;

import org.nfctools.NfcException;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static com.skjolberg.nfc.command.ACRCommands.*;

import custom.java.CommandAPDU;

public class ACR1255BluetoothCommands implements ACR1255Commands, BluetoothReader.OnResponseApduAvailableListener, BluetoothReader.OnEscapeResponseAvailableListener {

    private static final String TAG = ACR1255UsbCommands.class.getName();

    private volatile CountDownLatch latch;
    private byte[] in;

    private BluetoothReader reader;
    private String name;

    public ACR1255BluetoothCommands(String name, BluetoothReader reader) {
        this.name = name;
        this.reader = reader;
    }

    public List<AcrAutomaticPICCPolling> setAutomaticPICCPolling(int slot, AcrAutomaticPICCPolling... picc) throws ReaderException {
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x23, new byte[]{(byte) AcrAutomaticPICCPolling.serialize(picc)});

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        return AcrAutomaticPICCPolling.parse(operation);
    }

    public List<AcrAutomaticPICCPolling> getAutomaticPICCPolling(int slot) throws ReaderException {
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x23, 0x00};

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(command));

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        return AcrAutomaticPICCPolling.parse(operation);
    }

    public Boolean setPICC(int slot, int picc) throws ReaderException {
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x20, new byte[]{(byte) picc});

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        final int operation = response.getData()[0] & 0x1F; // 5 bit

        if (operation != picc) {
            Log.w(TAG, "Unable to properly update PICC: Expected " + Integer.toHexString(picc) + " got " + Integer.toHexString(operation));

            return Boolean.FALSE;
        } else {
            Log.d(TAG, "Updated PICC " + Integer.toHexString(operation) + " (" + Integer.toHexString(picc) + ")");

            return Boolean.TRUE;
        }
    }

    public Integer getPICC(int slot) throws ReaderException {
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x20, 0x0};

        CommandAPDU response = new CommandAPDU(control(slot, Reader.IOCTL_CCID_ESCAPE, command));

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0x1F; // 5 bit

        Log.d(TAG, "Read PICC " + Integer.toHexString(operation));

        return operation;
    }

    public String getFirmware(int slot) throws ReaderException {
        byte[] pseudo = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x18, 0x00};

        CommandAPDU response = new CommandAPDU(control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo));

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        String firmware = new String(response.getData(), Charset.forName("ASCII"));

        Log.d(TAG, "Read firmware " + firmware);

        return firmware;
    }

    public String getSerialNumber(int slot) throws ReaderException {
        byte[] pseudo = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x47, 0x00};

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(pseudo));

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        String firmware = new String(response.getData(), Charset.forName("ASCII"));

        Log.d(TAG, "Read serial number " + firmware);

        return firmware;
    }


    /**
     * Control the current state of the LEDs.
     *
     * @param slot
     * @return
     * @throws ReaderException
     */

    public boolean setLED(int slot, int state) throws ReaderException {

        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x29, new byte[]{(byte) (state)});

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response, 1)) {
            throw new IllegalArgumentException();
        }

        Log.d(TAG, "Set LED state to " + (0xFF & response.getData()[0]));

        return true;
    }


    public List<Set<AcrLED>> getLED(int slot) throws ReaderException {
        int operation = getLED2(slot);

        Log.d(TAG, "Read LED state " + Integer.toHexString(operation));

        Set<AcrLED> first = new HashSet<AcrLED>();
        Set<AcrLED> second = new HashSet<AcrLED>();

        if ((operation & LED_1_GREEN) != 0) {
            first.add(AcrLED.GREEN);
        }

        if ((operation & LED_1_RED) != 0) {
            first.add(AcrLED.RED);
        }

        if ((operation & LED_2_BLUE) != 0) {
            second.add(AcrLED.BLUE);
        }

        if ((operation & LED_2_RED) != 0) {
            second.add(AcrLED.RED);
        }

        return Arrays.asList(first, second);

    }

    public int getLED2(int slot) throws ReaderException {
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x29, 0x00};

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(command));

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        return operation;
    }

    public void setBuzzerBeepDurationOnCardDetection(int slot, int duration) throws ReaderException {
        if ((duration & 0xFF) != duration) throw new RuntimeException();

        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x28, new byte[]{(byte) duration});

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response, 1)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        if (operation != 0x00) {
            throw new IllegalArgumentException();
        }
    }

    public int setDefaultLEDAndBuzzerBehaviour(int slot, int picc) throws ReaderException {
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x21, new byte[]{(byte) picc});

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        Log.d(TAG, "Set default LED and buzzer behaviour " + Integer.toHexString(operation) + " (" + picc + ")");

        return operation;
    }

    public int getDefaultLEDAndBuzzerBehaviour2(int slot) throws ReaderException {
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x21, 0x00};

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(command));

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        Log.d(TAG, "Read default LED and buzzer behaviour " + Integer.toHexString(operation));

        return operation;
    }

    public byte getAntennaFieldStatus(int slot) throws ReaderException {
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x25, 0x00};

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(command));

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        byte data = response.getData()[0];

        Log.d(TAG, "Read antenna field status " + Integer.toHexString(data & 0xFF));

        return data;
    }

    public boolean setAntennaField(int slot, boolean on) throws ReaderException {
        byte b = (byte) (on ? 0x01 : 0x00);

        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x25, new byte[]{b});

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        boolean result = response.getData()[0] == 0x01;

        if (result == on) {
            Log.w(TAG, "Unable to properly update antenna field: Expected " + on + " got " + result);

            return Boolean.FALSE;
        } else {
            Log.d(TAG, "Updated antenna field to " + result);

            return Boolean.TRUE;
        }
    }

    public byte getBluetoothTransmissionPower(int slot) throws ReaderException {
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x50, 0x00};

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(command));

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        byte b = response.getData()[0];

        Log.d(TAG, "Read bluetooth tx power " + Integer.toHexString(b & 0xFF));

        return b;
    }

    public boolean setBluetoothTransmissionPower(int slot, byte power) throws ReaderException {
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x49, new byte[]{power});

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        if (response.getData()[0] == power) {
            Log.w(TAG, "Unable to update bluetoth transmission power: Expected " + Integer.toHexString(power & 0xFF) + " got " + Integer.toHexString(response.getData()[0] & 0xFF));

            return Boolean.FALSE;
        } else {
            Log.d(TAG, "Updated bluetoth transmission power to " + Integer.toHexString(response.getData()[0] & 0xFF));

            return Boolean.TRUE;
        }
    }

    public byte[] setAutoPPS(int slot, byte tx, byte rx) throws ReaderException {
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x24, new byte[]{tx, rx});

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        Log.d(TAG, "Updated auto PPS " + Utils.toHexString(response.getData()));

        return response.getData();
    }

    public byte[] getAutoPPS(int slot) throws ReaderException {
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x24);

        CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        Log.d(TAG, "Read auto PPS " + Utils.toHexString(response.getData()));

        return response.getData();
    }

    public Boolean setSleepModeOption(int slot, byte option) throws ReaderException {
        if (option < 0 || option > 4) throw new RuntimeException();
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x48, option};

        CommandAPDU response = new CommandAPDU(control(slot, Reader.IOCTL_CCID_ESCAPE, command));

        if (!isSuccess(response)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        if (response.getData()[0] == option) {
            Log.w(TAG, "Unable to set sleep mode option: Expected " + Integer.toHexString(option & 0xFF) + " got " + Integer.toHexString(response.getData()[0] & 0xFF));

            return Boolean.FALSE;
        } else {
            Log.d(TAG, "Set sleep mode option " + Integer.toHexString(response.getData()[0] & 0xFF));

            return Boolean.TRUE;
        }
    }

    public CommandAPDU control(int slotNum, int controlCode, CommandAPDU request) throws ReaderException {
        return new CommandAPDU(control(slotNum, controlCode, request.getBytes()));
    }

    @Override
    public byte[] control(int slotNum, int controlCode, byte[] request) throws ReaderException {
        Log.d(TAG, "Raw request: " + Utils.toHexString(request));
        try {

            in = null;
            latch = new CountDownLatch(1);

            reader.setOnEscapeResponseAvailableListener(this);
            if (!reader.transmitEscapeCommand(request)) {
                throw new NfcException("Unable to transmit escape command");
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new NfcException("Problem waiting for response");
            }

            Log.d(TAG, "Raw response: " + Utils.toHexString(in));

            return in;
        } catch (Exception e) {
            throw new NfcException(e);
        }
    }

    @Override
    public byte[] transmit(int slotNum, byte[] command) throws ReaderException {
        return transmit(command);
    }

    public byte[] transmit(byte[] request) {
        Log.d(TAG, "Raw request: " + Utils.toHexString(request));
        try {

            in = null;
            latch = new CountDownLatch(1);

            reader.setOnResponseApduAvailableListener(this);
            if (!reader.transmitApdu(request)) {
                throw new NfcException("Unable to transmit ADPU");
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new NfcException("Problem waiting for response");
            }

            Log.d(TAG, "Raw response: " + Utils.toHexString(in));

            return in;
        } catch (Exception e) {
            throw new NfcException(e);
        }
    }

    @Override
    public void onResponseApduAvailable(BluetoothReader bluetoothReader, byte[] apdu, int errorCode) {
        Log.d(TAG, "onResponseApduAvailable: " + BluetoothBackgroundService.getResponseString(apdu, errorCode));

        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            this.in = apdu;

        }
        latch.countDown();
    }

    @Override
    public void onEscapeResponseAvailable(BluetoothReader bluetoothReader, byte[] bytes, int errorCode) {
        Log.d(TAG, "onEscapeResponseAvailable: " + BluetoothBackgroundService.getResponseString(bytes, errorCode));

        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            this.in = bytes;

        }
        latch.countDown();
    }

    public String getName() {
        return name;
    }
}
