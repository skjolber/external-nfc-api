package com.skjolberg.nfc.command;

import android.nfc.ErrorCodes;
import android.os.RemoteException;

import com.acs.smartcard.ReaderException;
import com.acs.smartcard.TlvProperties;
import com.skjolberg.hce.tech.ReaderTechnology;


public class ACRReaderBluetoothTechnology implements ReaderTechnology {

    protected ACR1255Commands reader;

    protected int maxTransieveLength;

    public ACRReaderBluetoothTechnology(ACR1255Commands reader) {
        this.reader = reader;

        maxTransieveLength = 253;
    }

    @Override
    public int setTimeout(int technology, int timeout) throws RemoteException {
        return ErrorCodes.SUCCESS;
    }

    @Override
    public int getTimeout(int technology) throws RemoteException {
        return 0; // (Integer)properties.getProperty(TlvProperties.PROPERTY_bTimeOut2);
    }

    @Override
    public void resetTimeouts() throws RemoteException {

    }

    @Override
    public boolean canMakeReadOnly(int ndefType) throws RemoteException {
        return false;
    }

    @Override
    public int getMaxTransceiveLength(int technology) throws RemoteException {
        return maxTransieveLength;
    }

    @Override
    public boolean getExtendedLengthApdusSupported() throws RemoteException {
        return maxTransieveLength > 256;
    }

    @Override
    public int reconnect(int handle) throws RemoteException {
        return 0;
    }

}
