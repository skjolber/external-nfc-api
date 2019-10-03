package com.skjolberg.hce;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import android.os.RemoteException;
import android.util.Log;

import com.skjolberg.nfc.acs.AcrReader;
import com.skjolberg.nfc.acs.remote.IAcr1251UReaderControl;
import com.skjolberg.nfc.command.ACR1251Commands;
import com.skjolberg.nfc.command.ACRCommands;
import com.skjolberg.nfc.command.remote.IAcr1251UCommandWrapper;

public class IAcr1251UBinder extends IAcr1251UReaderControl.Stub {

    private static final String TAG = IAcr1251UBinder.class.getName();

    private IAcr1251UCommandWrapper wrapper;

    public IAcr1251UBinder() {
        attachInterface(this, IAcr1251UReaderControl.class.getName());
    }

    public void setCommands(ACR1251Commands reader) {
        wrapper = new IAcr1251UCommandWrapper(reader);
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


}