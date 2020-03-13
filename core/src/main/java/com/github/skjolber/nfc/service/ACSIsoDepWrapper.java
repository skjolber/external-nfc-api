package com.github.skjolber.nfc.service;

import com.acs.smartcard.ReaderException;
import com.github.skjolber.nfc.command.ReaderWrapper;

public class ACSIsoDepWrapper implements IsoDepWrapper {

    private static final String TAG = ACSIsoDepWrapper.class.getName();

    private ReaderWrapper isoDep;
    private int slotNum;

    public ACSIsoDepWrapper(ReaderWrapper isoDep, int slotNum) {
        this.isoDep = isoDep;
        this.slotNum = slotNum;
    }

    public byte[] transceive(byte[] data) throws ReaderException {

        //Log.d(TAG, "Transceive request " + ACRCommands.toHexString(data));

        byte[] buffer = new byte[2048];
        int read;
        try {
            read = isoDep.transmit(slotNum, data, data.length, buffer, buffer.length);
        } catch (ReaderException e) {
            throw new ReaderException(e);
        }

        byte[] response = new byte[read];
        System.arraycopy(buffer, 0, response, 0, read);

        //Log.d(TAG, "Transceive response " + ACRCommands.toHexString(response));

        return response;
    }

    @Override
    public byte[] transmitPassThrough(byte[] req) throws ReaderException {
        throw new ReaderException();
    }
}