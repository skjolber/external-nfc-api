package com.skjolberg.hce;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import android.os.RemoteException;
import android.util.Log;

import com.skjolberg.nfc.acs.AcrReader;
import com.skjolberg.nfc.acs.remote.IAcr1251UReaderControl;
import com.skjolberg.nfc.acs.remote.IAcr1252UReaderControl;
import com.skjolberg.nfc.command.ACR1251Commands;
import com.skjolberg.nfc.command.ACR1252Commands;
import com.skjolberg.nfc.command.ACRCommands;
import com.skjolberg.nfc.command.remote.IAcr1251UCommandWrapper;
import com.skjolberg.nfc.command.remote.IAcr1252UCommandWrapper;

public class IAcr1252UBinder extends IAcr1252UReaderControl.Stub {

    private static final String TAG = IAcr1252UBinder.class.getName();

    private IAcr1252UCommandWrapper wrapper;

    public IAcr1252UBinder() {
        attachInterface(this, IAcr1252UReaderControl.class.getName());
    }

    public void setCommands(ACR1252Commands reader) {
        wrapper = new IAcr1252UCommandWrapper(reader);
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
    public byte[] getLEDs() throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.getLEDs();
    }

    @Override
    public byte[] setLEDs(int leds) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.setLEDs(leds);
    }

    @Override
    public byte[] setBuzzer(boolean enable) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.setBuzzer(enable);
    }
}