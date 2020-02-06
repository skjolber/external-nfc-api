package org.nfctools.mf.ul.ntag;

/*
 * *#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*
 * SMARTRAC SDK for Android NFC NTAG
 * ===============================================================================
 * Copyright (C) 2016 SMARTRAC TECHNOLOGY GROUP
 * ===============================================================================
 * SMARTRAC SDK
 * (C) Copyright 2016, Smartrac Technology Fletcher, Inc.
 * 267 Cane Creek Rd, Fletcher, NC, 28732, USA
 * All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#
 */


import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.TagTechnology;
import android.util.Log;

import com.acs.smartcard.ReaderException;
import com.skjolberg.nfc.command.ReaderWrapper;
import com.skjolberg.nfc.command.Utils;
import com.skjolberg.service.IsoDepWrapper;

import org.nfctools.mf.MfException;
import org.nfctools.mf.ul.MfUlReaderWriter;

import java.io.IOException;
import java.io.Reader;

import custom.java.CommandAPDU;
import custom.java.ResponseAPDU;

public class NfcNtag  {

    private static final String TAG = NfcNtag.class.getName();

    private IsoDepWrapper reader;

    public NfcNtag(IsoDepWrapper reader) {
        this.reader = reader;
    }

    // NTAG GET_VERSION
    public byte[] getVersion() throws MfException, ReaderException {
        byte[] req = new byte [1];
        byte[] resp;

        req[0] = NfcNtagOpcode.GET_VERSION;

        return transceive(req);
    }

    // NTAG READ
    public byte[] read(int addr) throws MfException, ReaderException  {
        byte[] req = new byte[2];
        byte[] resp;

        req[0] = NfcNtagOpcode.READ;
        req[1] = (byte)(addr & 0xFF);

        return transceive(req);

    }

    public byte[] fastRead(int startAddr, int endAddr) throws MfException, ReaderException  {

        byte[] req = new byte[3];
        byte[] resp;

        req[0] = NfcNtagOpcode.FAST_READ;
        req[1] = (byte)(startAddr & 0xFF);
        req[2] = (byte)(endAddr & 0xFF);

        return resp = transceive(req);
    }

    // NTAG WRITE
    public byte[] write(int addr, byte[] data) throws MfException, ReaderException {

        if (data == null) {
            throw new IllegalArgumentException();
        }
        if (data.length != 4) {
            throw new IllegalArgumentException();
        }

        byte[] req = new byte[6];
        req[0] = NfcNtagOpcode.WRITE;
        req[1] = (byte)(addr & 0xFF);

        System.arraycopy(data, 0, req, 2, 4);
        return transceive(req);

    }

    // NTAG READ_CNT
    public int readCnt() throws MfException, ReaderException {
        byte[] req = new byte[2];

        req[0] = NfcNtagOpcode.READ_CNT;
        req[1] = 0x02;

        byte[] resp = transceive(req);

        return resp[0] + resp[1] * 256 + resp[2] * 65536;
    }

    // NTAG PWD_AUTH
    public byte[] pwdAuth(byte[] password) throws MfException, ReaderException {

        if (password == null) {
            return null;
        }
        if (password.length != 4) {
            return null;
        }

        byte[] req = new byte[5];
        byte[] resp;

        req[0] = NfcNtagOpcode.PWD_AUTH;

        System.arraycopy(password, 0, req, 1, 4);

        return transceive(req); // result will be the PACK
    }

    // NTAG READ_SIG
    public byte[] readSig() throws MfException, ReaderException {
        byte[] req = new byte[2];
        byte[] resp;

        req[0] = NfcNtagOpcode.READ_SIG;
        req[1] = 0x00;

        return transceive(req); // result will be the NTAG signature
    }

    // NTAG SECTOR_SELECT
    public boolean sectorSelect(byte sector) throws MfException, ReaderException {
        byte[] req1 = new byte[2];
        byte[] req2 = new byte[4];

        req1[0] = NfcNtagOpcode.SECTOR_SELECT;
        req1[1] = (byte)0xFF;

        transceive(req1);

        req2[0] = sector;
        req2[1] = 0x00;

        // The second part of this command works with negative acknowledge:
        // If the tag does not respond, the command worked OK.
        try {
            transceive(req2);
        } catch (Exception ex) {
            return true;
        }
        return false;
    }

    // MF UL-C AUTHENTICATE part 1
    public byte[] mfulcAuth1() throws MfException, ReaderException {
        byte[] req = new byte[2];
        byte[] resp;

        req[0] = NfcNtagOpcode.MFULC_AUTH1;
        req[1] = 0x00;

        return transceive(req); // result will be "AFh" + ekRndB
    }

    // MF UL-C AUTHENTICATE part 2
    public byte[] mfulcAuth2(byte[] ekRndAB) throws MfException, ReaderException {
        if (ekRndAB == null) {
            return null;
        }
        if (ekRndAB.length != 16) {
            return null;
        }

        byte[] req = new byte[17];
        byte[] resp;

        req[0] = NfcNtagOpcode.MFULC_AUTH2;

        System.arraycopy(ekRndAB, 0, req, 1, 16);
        return transceive(req); // ekRndA'
    }

    private byte[] transceive(byte[] req) throws MfException, ReaderException {

        byte[] sub = new byte[2 + req.length];
        // 0xD4 magic byte
        // 0x42 InCommunicateThru from PN532

        sub[0] = (byte)0xD4;
        sub[1] = (byte)0x42;

        System.arraycopy(req, 0, sub, 2, req.length);
        // 0xD4 magic byte
        // 0x42 InCommunicateThru from PN532

        CommandAPDU command = new CommandAPDU(0xFF, 0x00, 0x00, 0x00, sub, 0, sub.length);

        byte[] responseBytes = reader.transceive(command.getBytes());

        ResponseAPDU response = new ResponseAPDU(responseBytes);

        NfcNtagVersion version = null;

        MfUlReaderWriter readerWriter;
        if(!response.isSuccess()) {
            throw new MfException("Unable to issue command " + Utils.toHexString(sub) + ", response " + Utils.toHexString(responseBytes));
        }
        byte[] data = response.getData();

        // Log.d(TAG, "Status " + (0xFF & data[2]));

        if( (data[2] & 0xFF) != 0) {
            throw new MfException("Got status " + (data[2] & 0xFF));
        }

        byte[] content = new byte[data.length - 3];
        System.arraycopy(data, 3, content, 0, content.length);

        return content;
    }

}
