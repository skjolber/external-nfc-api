package com.skjolberg.hce;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import android.os.RemoteException;
import android.util.Log;

import com.skjolberg.nfc.acs.AcrReader;
import com.skjolberg.nfc.acs.remote.IAcr1283LReaderControl;
import com.skjolberg.nfc.command.ACR1281Commands;
import com.skjolberg.nfc.command.ACR1283Commands;
import com.skjolberg.nfc.command.ACRCommands;
import com.skjolberg.nfc.command.remote.IAcr1283CommandWrapper;

public class IAcr1283Binder extends IAcr1283LReaderControl.Stub {

    private static final String TAG = IAcr1283Binder.class.getName();

    private IAcr1283CommandWrapper wrapper;

    public IAcr1283Binder() {
        attachInterface(this, IAcr1283CommandWrapper.class.getName());
    }

    public void setCommands(ACR1283Commands reader) {
        wrapper = new IAcr1283CommandWrapper(reader);
    }

    public void clearReader() {
        this.wrapper = null;
    }

    public byte[] getFirmware() {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.getFirmware();
    }

    private byte[] noReaderException() {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(AcrReader.VERSION);
            dout.writeInt(AcrReader.STATUS_EXCEPTION);
            dout.writeUTF("Reader not connected");

            byte[] response = out.toByteArray();

            Log.d(TAG, "Send exception response length " + response.length + ":" + ACRCommands.toHexString(response));

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public byte[] getPICC() {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.getPICC();
    }

    public byte[] setPICC(int picc) {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.setPICC(picc);
    }

    @Override
    public byte[] control(int slotNum, int controlCode, byte[] command)
            throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }

        return wrapper.control(slotNum, controlCode, command);
    }

    @Override
    public byte[] transmit(int slotNum, byte[] command) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }

        return wrapper.transmit(slotNum, command);
    }

    @Override
    public byte[] getAutomaticPICCPolling() throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.getAutomaticPICCPolling();
    }

    @Override
    public byte[] setAutomaticPICCPolling(int picc) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.setAutomaticPICCPolling(picc);
    }

    @Override
    public byte[] setLEDs(int leds) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.setLEDs(leds);
    }

    @Override
    public byte[] getDefaultLEDAndBuzzerBehaviour() throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.getDefaultLEDAndBuzzerBehaviour();
    }

    @Override
    public byte[] setDefaultLEDAndBuzzerBehaviour(int parameter) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.setDefaultLEDAndBuzzerBehaviour(parameter);
    }

    @Override
    public byte[] clearDisplay() throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.clearDisplay();
    }

    @Override
    public byte[] displayText(char fontId, boolean styleBold, int line, int position, byte[] message) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.displayText(fontId, styleBold, line, position, message);
    }

    @Override
    public byte[] lightDisplayBacklight(boolean on) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.lightDisplayBacklight(on);
    }

    public byte[] setDisplayContrast(int contrast) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.setDisplayContrast(contrast);
    }

}