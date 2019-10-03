package com.skjolberg.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.nfctools.NfcException;
import org.nfctools.api.TagInfo;
import org.nfctools.api.TagType;
import org.nfctools.mf.MfConstants;
import org.nfctools.mf.MfException;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.classic.ClassicHandler;
import org.nfctools.mf.classic.MfClassicConstants;
import org.nfctools.mf.classic.MfClassicNdefOperations;
import org.nfctools.mf.classic.MfClassicReaderWriter;
import org.nfctools.mf.mad.Application;
import org.nfctools.mf.mad.ApplicationDirectory;
import org.nfctools.mf.ul.CapabilityBlock;
import org.nfctools.mf.ul.LockPage;
import org.nfctools.mf.ul.MemoryLayout;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.mf.ul.Type2NdefOperations;
import org.nfctools.mf.ul.UltralightHandler;
import org.nfctools.mf.ul.ntag.NfcNtag;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;
import org.nfctools.spi.acs.AcrMfClassicReaderWriter;
import org.nfctools.spi.acs.AcrMfUlNTAGReaderWriter;
import org.nfctools.spi.acs.AcrMfUlReaderWriter;
import org.nfctools.spi.acs.AcsTag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareUltralight;
import android.preference.PreferenceManager;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.skjolberg.hce.tech.TagTechnology;
import com.skjolberg.hce.tech.mifare.MifareClassicAdapter;
import com.skjolberg.hce.tech.mifare.MifareClassicTagFactory;
import com.skjolberg.hce.tech.mifare.MifareDesfireAdapter;
import com.skjolberg.hce.tech.mifare.MifareDesfireTagFactory;
import com.skjolberg.hce.tech.mifare.MifareUltralightAdapter;
import com.skjolberg.hce.tech.mifare.MifareUltralightTagFactory;
import com.skjolberg.hce.tech.mifare.NdefAdapter;
import com.skjolberg.hce.tech.mifare.NdefFormattableAdapter;
import com.skjolberg.hce.tech.mifare.NfcAAdapter;
import com.skjolberg.hce.tech.mifare.PN532NfcAAdapter;
import com.skjolberg.nfc.NfcTag;
import com.skjolberg.nfc.command.Utils;

import custom.java.CommandAPDU;
import custom.java.ResponseAPDU;

public class BackgroundUsbService extends AbstractBackgroundUsbService {

    private static final String TAG = BackgroundUsbService.class.getName();

    @Override
    public void handleTagInit(int slotNumber, byte[] atr, TagType tagType) throws ReaderException {

        int preferredProtocols = Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1;
        int protocol = reader.setProtocol(0, preferredProtocols);

        int state = reader.getState(slotNumber);
        if (state != Reader.CARD_SPECIFIC) {
            ServiceUtil.sendTechBroadcast(BackgroundUsbService.this);
        } else {
            if (uidMode) {
                handleTagInitUIDMode(slotNumber, atr, tagType);
            } else {
                handleTagInitRegularMode(slotNumber, atr, tagType);
            }
        }
    }

    private void handleTagInitRegularMode(int slotNumber, byte[] atr, TagType tagType) throws ReaderException {
        AcsTag acsTag = new AcsTag(tagType, atr, reader, slotNumber);
        IsoDepWrapper wrapper = new ACSIsoDepWrapper(reader, slotNumber);

        if (tagType == TagType.MIFARE_ULTRALIGHT || tagType == TagType.MIFARE_ULTRALIGHT_C) {
            mifareUltralight(slotNumber, atr, tagType, acsTag, wrapper, reader.getReaderName());
        } else if (
                tagType == TagType.MIFARE_PLUS_SL1_2k ||
                        tagType == TagType.MIFARE_PLUS_SL1_4k ||
                        tagType == TagType.MIFARE_PLUS_SL2_2k ||
                        tagType == TagType.MIFARE_PLUS_SL2_4k
        ) {
            mifareClassicPlus(slotNumber, atr, tagType, acsTag, wrapper);
        } else if (
                tagType == TagType.MIFARE_CLASSIC_1K || tagType == TagType.MIFARE_CLASSIC_4K) {
            mifareClassic(slotNumber, atr, tagType, wrapper, acsTag);
        } else if (tagType == TagType.INFINEON_MIFARE_SLE_1K) {
            infineonMifare(slotNumber, atr, tagType, acsTag, wrapper);
        } else if (tagType == TagType.DESFIRE_EV1) {
            desfire(slotNumber, atr, wrapper);
        } else if (tagType == TagType.ISO_14443_TYPE_B_NO_HISTORICAL_BYTES || tagType == TagType.ISO_14443_TYPE_A_NO_HISTORICAL_BYTES) {
            hce(slotNumber, atr, wrapper);
        } else {
            ServiceUtil.sendTechBroadcast(BackgroundUsbService.this);
        }
    }

    public void handleTagInitUIDMode(int slotNumber, byte[] atr, TagType tagType) throws ReaderException {

        int preferredProtocols = Reader.PROTOCOL_T1;
        int protocol = reader.setProtocol(slotNumber, preferredProtocols);

        if (tagType == TagType.MIFARE_ULTRALIGHT || tagType == TagType.MIFARE_ULTRALIGHT_C) {
            try {
                AcsTag tag = new AcsTag(tagType, atr, reader, slotNumber);

                ServiceUtil.ultralight(BackgroundUsbService.this, tag);
            } catch (Exception e) {
                Log.d(TAG, "Problem reading from tag", e);

                ServiceUtil.sendTechBroadcast(BackgroundUsbService.this);
            }
        } else if (
                tagType == TagType.MIFARE_PLUS_SL1_2k ||
                        tagType == TagType.MIFARE_PLUS_SL1_4k ||
                        tagType == TagType.MIFARE_PLUS_SL2_2k ||
                        tagType == TagType.MIFARE_PLUS_SL2_4k ||
                        tagType == TagType.MIFARE_CLASSIC_1K ||
                        tagType == TagType.MIFARE_CLASSIC_4K ||
                        tagType == TagType.INFINEON_MIFARE_SLE_1K
        ) {

            try {
                AcsTag acsTag = new AcsTag(tagType, atr, reader, slotNumber);

                ServiceUtil.mifareClassic(BackgroundUsbService.this, acsTag);
            } catch (Exception e) {
                Log.d(TAG, "Problem reading from tag", e);

                ServiceUtil.sendTechBroadcast(BackgroundUsbService.this);
            }
        } else if (tagType == TagType.DESFIRE_EV1) {
            try {

                IsoDepWrapper wrapper = new ACSIsoDepWrapper(reader, slotNumber);

                ServiceUtil.desfire(BackgroundUsbService.this, wrapper);
            } catch (Exception e) {
                Log.d(TAG, "Problem reading from tag", e);

                ServiceUtil.sendTechBroadcast(BackgroundUsbService.this);
            }
        } else if (tagType == TagType.ISO_14443_TYPE_B_NO_HISTORICAL_BYTES || tagType == TagType.ISO_14443_TYPE_A_NO_HISTORICAL_BYTES) {
            // impossible to get tag id, there is just a phone
            ServiceUtil.sendTechBroadcast(BackgroundUsbService.this);

        } else {
            ServiceUtil.sendTechBroadcast(BackgroundUsbService.this);
        }
    }

}
	

