package com.github.skjolber.nfc.external.hceclient;

import android.nfc.tech.IsoDep;
import android.util.Log;

import java.util.Arrays;

public class IsoDepDeviceHint {

    private final byte[] id;

    private final byte[] historicalBytes;

    private final byte[] hiLayerResponse;

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

        if(isDesfireEV1()) {
            return IsoDepDeviceType.TAG;
        }
        if(isHostCardEmulation()) {
            return IsoDepDeviceType.HOST_CARD_EMULATION;
        }

        return null;
    }

    public boolean isDesfireEV1() {
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
