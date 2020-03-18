package com.github.skjolber.nfc.service;

import com.github.skjolber.android.nfc.tech.IsoDep;
import com.github.skjolber.android.nfc.tech.IsoDepWrapper;

public class IsoDepDeviceHint {

    private final byte[] id;

    private final byte[] historicalBytes;

    private final byte[] hiLayerResponse;

    public IsoDepDeviceHint(android.nfc.tech.IsoDep isoDep) {
        this(new IsoDepWrapper(isoDep));
    }

    public IsoDepDeviceHint(IsoDep isoDep) {

        id = isoDep.getTag().getId();

        historicalBytes = isoDep.getHistoricalBytes();

        hiLayerResponse = isoDep.getHiLayerResponse();
    }

    public byte[] getHiLayerResponse() {
        return hiLayerResponse;
    }

    public byte[] getHistoricalBytes() {
        return historicalBytes;
    }

    public byte[] getId() {
        return id;
    }

    public IsoDepDeviceType guessType() {

        if(isTag()) {
            return IsoDepDeviceType.TAG;
        }
        if(isHostCardEmulation()) {
            return IsoDepDeviceType.HOST_CARD_EMULATION;
        }

        return null;
    }

    public boolean isTag() {
        if(historicalBytes.length != 1) {
            return false;
        }
        if(historicalBytes[0] != (byte)0x80) {
            return false;
        }

        if(id.length != 7 || id[0] != (byte)0x04) {
            return false;
        }

        return true;
    }

    public boolean isHostCardEmulation() {
        if(historicalBytes.length > 0) {
            return false;
        }

        if(id.length != 4 || id[0] != (byte)0x08) {
            return false;
        }

        return true;
    }

}
