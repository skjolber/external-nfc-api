package com.skjolberg.nfc.command;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.Reader.OnStateChangeListener;
import com.acs.smartcard.ReaderException;

import org.nfctools.mf.MfException;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;

import custom.java.CommandAPDU;
import custom.java.ResponseAPDU;

public class ReaderWrapper {

    public static final boolean LOG = true;

    public static final String[] stateStrings = {"Unknown", "Absent", "Present", "Swallowed", "Powered", "Negotiable", "Specific"};

    private String TAG = ReaderWrapper.class.getName();

    private Reader reader;

    public ReaderWrapper(UsbManager mManager) {
        this.reader = new Reader(mManager);
    }

    public boolean isSupported(UsbDevice device) {
        if (LOG) log("isSupported: " + device);
        return reader.isSupported(device);
    }

    public UsbDevice getDevice() {
        UsbDevice device = reader.getDevice();

        if (LOG) log("getDevice: " + device);

        return device;
    }

    public void open(UsbDevice usbDevice) {
        if (LOG) log("open: " + usbDevice);
        reader.open(usbDevice);
    }

    public String getReaderName() {

        String name = reader.getReaderName();

        if (LOG) log("getReaderName: " + name);

        return name;
    }

    public int getNumSlots() {
        int slots = reader.getNumSlots();

        if (LOG) log("getNumSlots: " + slots);

        return slots;
    }

    public void close() {
        if (LOG) log("close");

        reader.close();

    }

    public byte[] power(int slotNum, int action) throws ReaderException {
        byte[] power = reader.power(slotNum, action);

        if (LOG)
            log("power " + slotNum + " " + action + ": " + (power != null ? Utils.convertBinToASCII(power) : null));

        return power;
    }

    public int setProtocol(int slotNum, int preferredProtocols) throws ReaderException {
        int p = reader.setProtocol(slotNum, preferredProtocols);

        if (LOG) log("setProtocol " + slotNum + " " + preferredProtocols + ": " + p);

        return p;
    }

    public void setOnStateChangeListener(
            OnStateChangeListener onStateChangeListener) {

        if (LOG) log("setOnStateChangeListener: " + onStateChangeListener);

        reader.setOnStateChangeListener(onStateChangeListener);
    }

    public int control(int slotNum, int controlCode, byte[] command,
                       int length, byte[] response, int length2) throws ReaderException {

        if (LOG)
            log("control - slotNum: " + slotNum + " controlCode: " + controlCode + "\nrequest: " + Utils.convertBinToASCII(command) + " length " + command.length);

        int control = reader.control(slotNum, controlCode, command, length, response, length2);

        if (LOG)
            log("control " + slotNum + " " + controlCode + " " + Utils.convertBinToASCII(command) + " " + length + "\n" + Utils.convertBinToASCII(response) + " " + length2 + ": " + control);

        return control;
    }

    public ResponseAPDU control(int slot, int controlCode, CommandAPDU command) throws ReaderException {
        return new ResponseAPDU(control(slot, controlCode, command.getBytes()));
    }

    public CommandAPDU control2(int slot, int controlCode, CommandAPDU command) throws ReaderException {
        return new CommandAPDU(control(slot, controlCode, command.getBytes()));
    }

    public CommandAPDU control2(int slot, int controlCode, byte[] command) throws ReaderException {
        return new CommandAPDU(control(slot, controlCode, command));
    }

    public byte[] control(int slotNum, int controlCode, byte[] command) throws ReaderException {

        byte[] response = new byte[1024];

        if (LOG)
            log("control - slotNum: " + slotNum + " controlCode: " + controlCode + "\nrequest: " + Utils.convertBinToASCII(command) + " length " + command.length);

        int control = reader.control(slotNum, controlCode, command, command.length, response, response.length);

        if (response.length < control) {
            throw new RuntimeException("Expected result " + response.length + " <= " + control);
        }

        if (LOG)
            log("control - slotNum: " + slotNum + " controlCode: " + controlCode + "\nrequest: " + Utils.convertBinToASCII(command) + " length " + command.length + "\nresponse: " + Utils.convertBinToASCII(response, 0, control));

        byte[] in = new byte[control];

        System.arraycopy(response, 0, in, 0, control);

        return in;
    }

    public ResponseAPDU transmit(int slot, CommandAPDU command) throws ReaderException {
        return new ResponseAPDU(transmit(slot, command.getBytes()));
    }

    public CommandAPDU transmit2(int slot, CommandAPDU command) throws ReaderException {
        return new CommandAPDU(transmit(slot, command.getBytes()));
    }

    public byte[] transmit(int slotNum, byte[] command) throws ReaderException {

        byte[] response = new byte[1024];

        int transmit = reader.transmit(slotNum, command, command.length, response, response.length);

        if (LOG)
            log("transmit - slotNum: " + slotNum + "\nrequest: " + Utils.convertBinToASCII(command) + " length " + command.length + "\nresponse: " + Utils.convertBinToASCII(response, 0, Math.min(transmit, response.length)));

        if (response.length < transmit) {
            throw new RuntimeException("Expected result " + response.length + " <= " + transmit);
        }

        byte[] in = new byte[transmit];

        System.arraycopy(response, 0, in, 0, transmit);

        return in;
    }

    public byte[] transmitPassThrough(int slotNumber, byte[] req) throws ReaderException {
        byte[] sub = new byte[2 + req.length];
        // 0xD4 magic byte
        // 0x42 InCommunicateThru from PN532

        sub[0] = (byte) 0xD4;
        sub[1] = (byte) 0x42;

        System.arraycopy(req, 0, sub, 2, req.length);
        // 0xD4 magic byte
        // 0x42 InCommunicateThru from PN532

        CommandAPDU command = new CommandAPDU(0xFF, 0x00, 0x00, 0x00, sub, 0, sub.length);

        byte[] responseBytes = transmit(slotNumber, command.getBytes());

        ResponseAPDU response = new ResponseAPDU(responseBytes);

        NfcNtagVersion version = null;

        if (!response.isSuccess()) {
            throw new ReaderCommandException("Unable to issue command " + Utils.toHexString(sub) + ", response " + Utils.toHexString(responseBytes));
        }
        byte[] data = response.getData();

        if (LOG) log("Status " + (0xFF & data[2]));

        if ((data[2] & 0xFF) != 0) {
            throw new PassthroughCommandException("Got command error", (data[2] & 0xFF));
        }

        byte[] content = new byte[data.length - 3];
        System.arraycopy(data, 3, content, 0, content.length);

        return content;
    }

    public int transmit(int slotNum, byte[] command, int length, byte[] response, int responseLength) throws ReaderException {

        int transmit = reader.transmit(slotNum, command, length, response, responseLength);

        if (LOG)
            log("transmit - slotNum: " + slotNum + "\nrequest: " + Utils.convertBinToASCII(command, 0, length) + " length " + command.length + "\nresponse: " + Utils.convertBinToASCII(response, 0, Math.min(transmit, responseLength)));

        if (response.length < transmit) {
            throw new RuntimeException("Expected result " + responseLength + " <= " + transmit);
        }

        return transmit;

    }

    public int getState(int slotNum) {
        int state = reader.getState(slotNum);

        if (LOG) log("getState " + slotNum + ": " + state);

        return state;
    }

    public byte[] getAtr(int slotNum) {

        byte[] atr = reader.getAtr(slotNum);

        if (LOG)
            log("getState " + slotNum + ": " + (atr != null ? Utils.convertBinToASCII(atr) : null));

        return atr;
    }

    public int getProtocol(int slotNum) {

        int protocol = reader.getProtocol(slotNum);

        if (LOG) log("getProtocol " + slotNum + ": " + protocol);

        return protocol;
    }

    private void log(String string) {
        Log.d(TAG, string);
    }


}
