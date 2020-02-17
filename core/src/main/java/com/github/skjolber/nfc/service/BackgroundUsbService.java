package com.github.skjolber.nfc.service;

import org.nfctools.api.TagType;
import org.nfctools.spi.acs.AcsTag;

import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

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
	

