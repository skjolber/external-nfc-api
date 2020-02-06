package com.skjolberg.service;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.util.Log;

import com.skjolberg.hce.tech.mifare.MifareClassicTagFactory;
import com.skjolberg.nfc.NfcTag;
import com.skjolberg.nfc.command.*;
import com.skjolberg.nfc.command.Utils;

import org.nfctools.api.ApduTag;
import org.nfctools.api.TagType;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.classic.MfClassicReaderWriter;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.spi.acs.AcrMfClassicReaderWriter;
import org.nfctools.spi.acs.AcrMfUlReaderWriter;
import org.nfctools.spi.acs.AcsTag;

import custom.java.ResponseAPDU;


public class ServiceUtil {

    private static final String TAG = ServiceUtil.class.getName();

    private static byte[] mifareClassicUIDCommand = new byte[]{(byte) 0xFF, (byte) 0xCA, 0x00, 0x00, 0x00};


    public static void sendTagIdIndent(Context context, byte[] uid) {
        final Intent intent = new Intent(NfcTag.ACTION_TAG_DISCOVERED);

        if (uid != null) {
            intent.putExtra(NfcAdapter.EXTRA_ID, uid);
        }

        context.sendBroadcast(intent);
    }

    public static void ultralight(Context context, ApduTag tag) {

        MfUlReaderWriter readerWriter = new AcrMfUlReaderWriter(tag);

        MfBlock[] initBlocks = null;

        byte[] uid;
        try {
            // get uid from first two blocks:
            // 3 bytes from index 0
            // 4 bytes from index 1

            initBlocks = readerWriter.readBlock(0, 2);

            uid = new byte[7];
            System.arraycopy(initBlocks[0].getData(), 0, uid, 0, 3);
            System.arraycopy(initBlocks[1].getData(), 0, uid, 3, 4);
        } catch (Exception e) {
            Log.w(TAG, "Problem reading tag UID", e);
            uid = new byte[]{MifareClassicTagFactory.NXP_MANUFACTURER_ID};
        }

        ServiceUtil.sendTagIdIndent(context, uid);
    }

    public static void mifareClassic(Context context, ApduTag acsTag) {
        ResponseAPDU responseAPDU = new ResponseAPDU(acsTag.transmit(mifareClassicUIDCommand));

        byte[] uid;
        if (responseAPDU.isSuccess()) {
            uid = com.skjolberg.nfc.command.Utils.reverse(responseAPDU.getData());
        } else {
            // try the old way
            org.nfctools.mf.classic.MemoryLayout memoryLayout;

            int type = MifareClassicTagFactory.TYPE_CLASSIC;
            int size;

            switch (acsTag.getTagType()) {
                case INFINEON_MIFARE_SLE_1K:
                case MIFARE_CLASSIC_1K:
                case MIFARE_PLUS_SL1_2k:
                case MIFARE_PLUS_SL2_2k: {
                    memoryLayout = org.nfctools.mf.classic.MemoryLayout.CLASSIC_1K;
                    size = MifareClassicTagFactory.SIZE_1K;

                    break;
                }
                default: {
                    memoryLayout = org.nfctools.mf.classic.MemoryLayout.CLASSIC_4K;
                    size = MifareClassicTagFactory.SIZE_4K;
                }
            }

            MfClassicReaderWriter readerWriter = new AcrMfClassicReaderWriter(acsTag, memoryLayout);

            try {
                uid = readerWriter.getTagInfo().getId();

                if (isBlank(uid)) {
                    Log.w(TAG, "Unable to read tag UID");
                    uid = null;
                } else {
                    Log.i(TAG, "Read tag id " + com.skjolberg.nfc.command.Utils.toHexString(uid));
                }
            } catch (Exception e) {
                Log.w(TAG, "Problem reading tag UID", e);
                uid = null;
            }
        }

        ServiceUtil.sendTagIdIndent(context, uid);
    }

    public static void desfire(Context context, IsoDepWrapper wrapper) {
        byte[] uid;

        DesfireProtocol desfireProtocol = new DesfireProtocol(wrapper);

        try {
            VersionInfo versionInfo = desfireProtocol.getVersionInfo();

            uid = versionInfo.getUid();
        } catch (Exception e) {
            Log.d(TAG, "Problem getting manufacturer data", e);

            uid = null;
        }

        ServiceUtil.sendTagIdIndent(context, uid);

    }

    public static void sendTechBroadcast(Context context) {
        Intent intent = new Intent(NfcTag.ACTION_TECH_DISCOVERED);

        context.sendBroadcast(intent);
    }

    public static boolean isBlank(byte[] uid) {
        for (byte b : uid) {
            if (b != (byte) 0x00) {
                return false;
            }
        }

        return true;
    }

    public static TagType identifyTagType(String name, byte[] historicalBytes) {

        if (name != null) {
            if (name.contains("1252") || name.contains("1255")) {
                if (historicalBytes.length >= 11) {
                    //Log.d(TAG, Utils.toHexString(historicalBytes));

                    int tagId = (historicalBytes[13] & 0xff) << 8 | (historicalBytes[14] & 0xff);

                    byte standard = historicalBytes[12];

                    if (standard == 0x11) {
                        // felicia
                        switch (tagId) {
                            case 0x003B:
                                return TagType.FELICA_212K;
                            case 0xF012:
                                return TagType.FELICA_424K;
                            default: {
                                Log.w(TAG, "Unknown tag id " + com.skjolberg.nfc.command.Utils.toHexString(new byte[]{historicalBytes[13], historicalBytes[14]}) + " (" + Integer.toHexString(tagId) + ")");
                            }
                        }
                    } else {
                        switch (tagId) {
                            case 0x0001:
                                return TagType.MIFARE_CLASSIC_1K;
                            case 0x0002:
                                return TagType.MIFARE_CLASSIC_4K;
                            case 0x0003:
                                return TagType.MIFARE_ULTRALIGHT;
                            case 0x0026:
                                return TagType.MIFARE_MINI;
                            case 0x003A:
                                return TagType.MIFARE_ULTRALIGHT_C;
                            case 0x0036:
                                return TagType.MIFARE_PLUS_SL1_2k;
                            case 0x0037:
                                return TagType.MIFARE_PLUS_SL1_4k;
                            case 0x0038:
                                return TagType.MIFARE_PLUS_SL2_2k;
                            case 0x0039:
                                return TagType.MIFARE_PLUS_SL2_4k;
                            case 0x0030:
                                return TagType.TOPAZ_JEWEL;
                            case 0xFF40:
                                return TagType.NFCIP;
                            case 0xFF88:
                                return TagType.INFINEON_MIFARE_SLE_1K;
                            default: {

                                if ((historicalBytes[13] & 0xFF) == 0xFF) {
                                    Log.i(TAG, "Assume android device for " + com.skjolberg.nfc.command.Utils.toHexString(new byte[]{historicalBytes[13], historicalBytes[14]}) + " (" + Integer.toHexString(tagId) + ")");

                                    return TagType.UNKNOWN;
                                } else {
                                    Log.w(TAG, "Unknown tag id " + Utils.toHexString(new byte[]{historicalBytes[13], historicalBytes[14]}) + " (" + Integer.toHexString(tagId) + ")");
                                }
                            }
                        }


                    }

                }
            }
        }

        return TagType.identifyTagType(historicalBytes);
    }
}
