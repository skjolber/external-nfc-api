package com.github.skjolber.nfc.hce.tech;

import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcBarcode;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;


public interface TagTechnology {

    /**
     * This technology is an instance of {@link NfcA}.
     * <p>Support for this technology type is mandatory.
     *
     * @hide
     */
    public static final int NFC_A = 1;

    /**
     * This technology is an instance of {@link NfcB}.
     * <p>Support for this technology type is mandatory.
     *
     * @hide
     */
    public static final int NFC_B = 2;

    /**
     * This technology is an instance of {@link IsoDep}.
     * <p>Support for this technology type is mandatory.
     *
     * @hide
     */
    public static final int ISO_DEP = 3;

    /**
     * This technology is an instance of {@link NfcF}.
     * <p>Support for this technology type is mandatory.
     *
     * @hide
     */
    public static final int NFC_F = 4;

    /**
     * This technology is an instance of {@link NfcV}.
     * <p>Support for this technology type is mandatory.
     *
     * @hide
     */
    public static final int NFC_V = 5;

    /**
     * This technology is an instance of {@link Ndef}.
     * <p>Support for this technology type is mandatory.
     *
     * @hide
     */
    public static final int NDEF = 6;

    /**
     * This technology is an instance of {@link NdefFormatable}.
     * <p>Support for this technology type is mandatory.
     *
     * @hide
     */
    public static final int NDEF_FORMATABLE = 7;

    /**
     * This technology is an instance of {@link MifareClassic}.
     * <p>Support for this technology type is optional. If a stack doesn't support this technology
     * type tags using it must still be discovered and present the lower level radio interface
     * technologies in use.
     *
     * @hide
     */
    public static final int MIFARE_CLASSIC = 8;

    /**
     * This technology is an instance of {@link MifareUltralight}.
     * <p>Support for this technology type is optional. If a stack doesn't support this technology
     * type tags using it must still be discovered and present the lower level radio interface
     * technologies in use.
     *
     * @hide
     */
    public static final int MIFARE_ULTRALIGHT = 9;

    /**
     * This technology is an instance of {@link NfcBarcode}.
     * <p>Support for this technology type is optional. If a stack doesn't support this technology
     * type tags using it must still be discovered and present the lower level radio interface
     * technologies in use.
     *
     * @hide
     */
    public static final int NFC_BARCODE = 10;


    int getTagTechnology();

}
