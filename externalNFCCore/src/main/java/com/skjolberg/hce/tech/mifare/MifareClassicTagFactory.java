package com.skjolberg.hce.tech.mifare;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import com.skjolberg.hce.TagFactory;
import com.skjolberg.hce.tech.TagTechnology;
import com.skjolberg.nfc.NfcTag;
import com.skjolberg.nfc.command.Utils;
import com.skjolberg.service.TechnologyType;

/**
 * http://nfc-tools.org/index.php?title=ISO14443A
 *
 */

public class MifareClassicTagFactory extends TagFactory {

    public static final int NXP_MANUFACTURER_ID = 0x04;

    public static final String EXTRA_SAK = "sak";
    public static final String EXTRA_ATQA = "atqa";

    private static final byte[] EXTRA_ATQA_VALUE_1K = new byte[]{0x00, 0x04};
    private static final byte[] EXTRA_ATQA_VALUE_4K = new byte[]{0x00, 0x02};

    private static final short EXTRA_SAK_VALUE_1K = 0x08;
    private static final short EXTRA_SAK_VALUE_4K = 0x18;

    /**
     * Tag contains 16 sectors, each with 4 blocks.
     */
    public static final int SIZE_1K = 1024;
    /**
     * Tag contains 32 sectors, each with 4 blocks.
     */
    public static final int SIZE_2K = 2048;
    /**
     * Tag contains 40 sectors. The first 32 sectors contain 4 blocks and the last 8 sectors
     * contain 16 blocks.
     */
    public static final int SIZE_4K = 4096;

    /**
     * A MIFARE Classic compatible card of unknown type
     */
    public static final int TYPE_UNKNOWN = -1;
    /**
     * A MIFARE Classic tag
     */
    public static final int TYPE_CLASSIC = 0;
    /**
     * A MIFARE Plus tag
     */
    public static final int TYPE_PLUS = 1;
    /**
     * A MIFARE Pro tag
     */
    public static final int TYPE_PRO = 2;

    /** NDEF */
    /**
     * @hide
     */
    public static final int NDEF_MODE_READ_ONLY = 1;
    /**
     * @hide
     */
    public static final int NDEF_MODE_READ_WRITE = 2;
    /**
     * @hide
     */
    public static final int NDEF_MODE_UNKNOWN = 3;

    public static final String EXTRA_NDEF_MSG = "ndefmsg";

    /**
     * @hide
     */
    public static final String EXTRA_NDEF_MAXLENGTH = "ndefmaxlength";

    /**
     * @hide
     */
    public static final String EXTRA_NDEF_CARDSTATE = "ndefcardstate";

    /**
     * @hide
     */
    public static final String EXTRA_NDEF_TYPE = "ndeftype";

    /**
     * @hide
     */
    public static final int TYPE_OTHER = -1;
    /**
     * @hide
     */
    public static final int TYPE_1 = 1;
    /**
     * @hide
     */
    public static final int TYPE_2 = 2;
    /**
     * @hide
     */
    public static final int TYPE_3 = 3;
    /**
     * @hide
     */
    public static final int TYPE_4 = 4;
    /**
     * @hide
     */
    public static final int TYPE_MIFARE_CLASSIC = 101;
    /**
     * @hide
     */
    public static final int TYPE_ICODE_SLI = 102;
    
    /*
    private int serviceHandle;
    private int type;
    private byte[] id;
    private int maxNdefSize;
    private NdefMessage message;
    private Boolean writable;
    
    public MifareUltralightTagFactory(int serviceHandle, int type, byte[] id, int maxNdefSize, NdefMessage message, Boolean writable) {
		this.serviceHandle = serviceHandle;
		this.type = type;
		this.id = id;
		this.maxNdefSize = maxNdefSize;
		this.message = message;
		this.writable = writable;
	}
	*/

    public Intent getTag(int serviceHandle, int slotNumber, int type, int size, byte[] id, int maxNdefSize, NdefMessage message, boolean formatted, Boolean writable, byte[] atr, Object tagService) {

        List<Bundle> bundles = new ArrayList<Bundle>();
        List<Integer> tech = new ArrayList<Integer>();

        if (TechnologyType.isNFCA(atr)) {
            Bundle nfcA = new Bundle();

            if (type == TYPE_CLASSIC) {
                if (size == SIZE_1K) {
                    nfcA.putShort(EXTRA_SAK, EXTRA_SAK_VALUE_1K);
                    nfcA.putByteArray(EXTRA_ATQA, EXTRA_ATQA_VALUE_1K);
                } else if (size == SIZE_4K) {
                    nfcA.putShort(EXTRA_SAK, EXTRA_SAK_VALUE_4K);
                    nfcA.putByteArray(EXTRA_ATQA, EXTRA_ATQA_VALUE_4K);
                } else {
                    throw new IllegalArgumentException("Size 1K or 4K expected");
                }
            } else {
                throw new IllegalArgumentException("Type mifare classic expected");
            }

            bundles.add(nfcA);
            tech.add(TagTechnology.NFC_A);
        }

        Bundle ultralight = new Bundle();
        bundles.add(ultralight);
        tech.add(TagTechnology.MIFARE_CLASSIC);

        final Intent intent;
        if (message != null) {
            intent = new Intent(NfcTag.ACTION_NDEF_DISCOVERED);

            intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, new NdefMessage[]{message});

            Bundle ndef = new Bundle();
            ndef.putParcelable(EXTRA_NDEF_MSG, message);
            if (writable != null) {
                ndef.putInt(EXTRA_NDEF_CARDSTATE, writable ? NDEF_MODE_READ_WRITE : NDEF_MODE_READ_ONLY);
            } else {
                ndef.putInt(EXTRA_NDEF_CARDSTATE, NDEF_MODE_UNKNOWN);
            }

            ndef.putInt(EXTRA_NDEF_MAXLENGTH, maxNdefSize);
            ndef.putInt(EXTRA_NDEF_TYPE, TYPE_2);

            bundles.add(ndef);

            tech.add(TagTechnology.NDEF);
        } else if (formatted) {
            intent = new Intent(NfcTag.ACTION_TAG_DISCOVERED);

            Bundle ndef = new Bundle();
            if (writable != null) {
                ndef.putInt(EXTRA_NDEF_CARDSTATE, writable ? NDEF_MODE_READ_WRITE : NDEF_MODE_READ_ONLY);
            } else {
                ndef.putInt(EXTRA_NDEF_CARDSTATE, NDEF_MODE_UNKNOWN);
            }

            ndef.putInt(EXTRA_NDEF_MAXLENGTH, maxNdefSize);
            ndef.putInt(EXTRA_NDEF_TYPE, TYPE_2);

            bundles.add(ndef);

            tech.add(TagTechnology.NDEF);
        } else {
            intent = new Intent(NfcTag.ACTION_TAG_DISCOVERED);

            Bundle ndefFormatable = new Bundle();
            bundles.add(ndefFormatable);

            tech.add(TagTechnology.NDEF_FORMATABLE);
        }

        int[] techArray = new int[tech.size()];
        for (int i = 0; i < techArray.length; i++) {
            techArray[i] = tech.get(i);
        }


        if (id == null) {
            throw new IllegalArgumentException("No tag id");
        }

        if (id[0] != NXP_MANUFACTURER_ID) {
            throw new IllegalArgumentException("Non-NXP tag id " + Utils.toHexString(id));
        }

        intent.putExtra(NfcAdapter.EXTRA_TAG, createTag(id, techArray, bundles.toArray(new Bundle[bundles.size()]), serviceHandle, tagService));
        intent.putExtra(NfcAdapter.EXTRA_ID, id);

        intent.putExtra(NfcTag.EXTRA_TAG_SERVICE_HANDLE, serviceHandle);
        intent.putExtra(NfcTag.EXTRA_TAG_SLOT_NUMBER, slotNumber);

        return intent;

    }

}
