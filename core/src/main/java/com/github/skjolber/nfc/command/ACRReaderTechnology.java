package com.github.skjolber.nfc.command;

import com.github.skjolber.android.nfc.ErrorCodes;
import android.os.RemoteException;

import com.acs.smartcard.ReaderException;
import com.acs.smartcard.TlvProperties;
import com.github.skjolber.nfc.hce.tech.ReaderTechnology;

public class ACRReaderTechnology implements ReaderTechnology {

    protected ACRCommands reader;
    protected TlvProperties properties;

    protected int maxTransieveLength;

    public ACRReaderTechnology(ACRCommands reader) throws ReaderException {
        this.reader = reader;

        reader.initFeatures();

        properties = reader.getProperties();

        /*
        dwMaxAPDUDataSize
        Maximal size of data the reader and its driver can
        support
        0: short APDU only.
        0<X<=256: forbidden values (RFU)
        256 < X <= 0x10000: short and extended APDU of up
        to X bytes of data
        0x10000 < X: invalid values (RFU)
        */
        Integer maxAPDUDataSize = (Integer) properties.getProperty(TlvProperties.PROPERTY_dwMaxAPDUDataSize);

        if (maxAPDUDataSize.intValue() == 0) {
            maxTransieveLength = 253;
        } else {
            maxTransieveLength = maxAPDUDataSize;
        }
    }

    @Override
    public int setTimeout(int technology, int timeout) throws RemoteException {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
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
