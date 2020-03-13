package com.github.skjolber.nfc.hce.tech.mifare;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;

import com.github.skjolber.android.nfc.INfcTag;
import com.github.skjolber.nfc.hce.TagFactory;
import com.github.skjolber.nfc.hce.tech.TagTechnology;
import com.github.skjolber.nfc.NfcTag;
import com.github.skjolber.nfc.service.AbstractService;

/**
 * http://nfc-tools.org/index.php?title=ISO14443A
 *
 */

public class MifareDesfireTagFactory extends TagFactory {

    private static final String TAG = MifareDesfireTagFactory.class.getName();

    public static final int NXP_MANUFACTURER_ID = 0x04;

    /**
     * @hide
     */
    public static final String EXTRA_HI_LAYER_RESP = "hiresp";
    /**
     * @hide
     */
    public static final String EXTRA_HIST_BYTES = "histbytes";

    public static final byte[] EXTRA_ATQA_VALUE = new byte[]{0x44, 0x03};
    public static final short EXTRA_SAK_VALUE = 0x20;

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

    public Intent getTag(int serviceHandle, int slotNumber, byte[] atr, byte[] hiLayer, byte[] id, boolean hce, byte[] historicalBytes, INfcTag tagService) {

        /*
        if (id != null && id[0] != NXP_MANUFACTURER_ID) {
            throw new IllegalArgumentException("Non-NXP tag id");
        }
        */

        List<Bundle> bundles = new ArrayList<Bundle>();
        List<Integer> tech = new ArrayList<Integer>();

        Bundle nfcA = new Bundle();
        nfcA.putShort(MifareUltralightTagFactory.EXTRA_SAK, EXTRA_SAK_VALUE);
        nfcA.putByteArray(MifareUltralightTagFactory.EXTRA_ATQA, EXTRA_ATQA_VALUE);
        bundles.add(nfcA);
        tech.add(TagTechnology.NFC_A);

        Bundle desfire = new Bundle();
        desfire.putByteArray(EXTRA_HIST_BYTES, historicalBytes);
        if(hiLayer != null) {
            desfire.putByteArray(EXTRA_HI_LAYER_RESP, hiLayer);
        }
        bundles.add(desfire);
        tech.add(TagTechnology.ISO_DEP);

        final Intent intent = new Intent(NfcTag.ACTION_TAG_DISCOVERED);

        int[] techArray = new int[tech.size()];
        for (int i = 0; i < techArray.length; i++) {
            techArray[i] = tech.get(i);
        }

        intent.putExtra(NfcAdapter.EXTRA_TAG, createTag(id, techArray, bundles.toArray(new Bundle[bundles.size()]), serviceHandle, tagService));
        if (id != null) {
            intent.putExtra(NfcAdapter.EXTRA_ID, id);
        }
        intent.putExtra(NfcTag.EXTRA_TAG_SERVICE_HANDLE, serviceHandle);
        intent.putExtra(NfcTag.EXTRA_TAG_SLOT_NUMBER, slotNumber);
        intent.putExtra(NfcTag.EXTRA_HOST_CARD_EMULATION, hce);

        return intent;

    }

}
