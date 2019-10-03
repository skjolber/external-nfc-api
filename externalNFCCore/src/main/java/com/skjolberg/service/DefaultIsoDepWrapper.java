package com.skjolberg.service;

import java.io.IOException;

import android.nfc.tech.IsoDep;

import com.acs.smartcard.ReaderException;

public class DefaultIsoDepWrapper implements IsoDepWrapper {

    private IsoDep isoDep;

    public DefaultIsoDepWrapper(IsoDep isoDep) {
        this.isoDep = isoDep;
    }

    public byte[] transceive(byte[] data) throws ReaderException {
        try {
            return isoDep.transceive(data);
        } catch (IOException e) {
            throw new ReaderException(e);
        }
    }

    @Override
    public byte[] transmitPassThrough(byte[] req) throws ReaderException {
        throw new ReaderException();
    }
}
