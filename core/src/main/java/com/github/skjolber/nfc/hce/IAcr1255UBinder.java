package com.github.skjolber.nfc.hce;

import android.os.RemoteException;
import android.util.Log;

import com.github.skjolber.nfc.acs.AcrReader;
import com.github.skjolber.nfc.acs.remote.IAcr1255UReaderControl;
import com.github.skjolber.nfc.command.ACR1255Commands;
import com.github.skjolber.nfc.command.ACRCommands;
import com.github.skjolber.nfc.command.remote.IAcr1255UCommandWrapper;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class IAcr1255UBinder extends IAcr1255UReaderControl.Stub {

    private static final String TAG = IAcr1255UBinder.class.getName();

    private IAcr1255UCommandWrapper wrapper;

    public IAcr1255UBinder() {
        attachInterface(this, IAcr1255UBinder.class.getName());
    }

    public void setCommands(ACR1255Commands reader) {
        wrapper = new IAcr1255UCommandWrapper(reader);
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

    @Override
    public byte[] getSerialNumber() throws RemoteException {
        return new byte[0];
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
    public byte[] getAutoPPS() throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.getAutoPPS();
    }

    @Override
    public byte[] setAutoPPS(byte[] bytes) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.setAutoPPS(bytes[0], bytes[1]);
    }

    @Override
    public byte[] getAntennaFieldStatus() throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.getAntennaFieldStatus();
    }

    @Override
    public byte[] setAntennaField(boolean b) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.setAntennaField(b);
    }

    @Override
    public byte[] getBluetoothTransmissionPower() throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.getBluetoothTransmissionPower();
    }

    @Override
    public byte[] setBluetoothTransmissionPower(byte b) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.setBluetoothTransmissionPower(b);
    }

    @Override
    public byte[] setSleepModeOption(byte b) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.setSleepModeOption(b);
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
    public byte[] setAutomaticPolling(boolean b) throws RemoteException {
        if (wrapper == null) {
            return noReaderException();
        }
        return wrapper.setAutomaticPolling(b);
    }


}
