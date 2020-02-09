package com.github.skjolber.nfc;

public interface NfcTag {

	/** Action corresponding to {@linkplain android.nfc.NfcAdapter#ACTION_NDEF_DISCOVERED}. */
	public static final String ACTION_NDEF_DISCOVERED = NfcTag.class.getName() + ".action.NDEF_DISCOVERED";
	/** Action corresponding to {@linkplain android.nfc.NfcAdapter#ACTION_TAG_DISCOVERED}. */
	public static final String ACTION_TAG_DISCOVERED = NfcTag.class.getName() + "action.TAG_DISCOVERED";
	/** Action corresponding to {@linkplain android.nfc.NfcAdapter#ACTION_TECH_DISCOVERED}. */
	public static final String ACTION_TECH_DISCOVERED = NfcTag.class.getName() + ".action.TECH_DISCOVERED";
	/** Action corresponding to hidden {@linkplain android.nfc.NfcAdapter#ACTION_TAG_LEFT_FIELD}. */
	public static final String ACTION_TAG_LEFT_FIELD = NfcTag.class.getName() + ".action.TAG_LEFT_FIELD";

	/** int value indicating the unique (within service lifecycle) tag service handle */
    public static final String EXTRA_TAG_SERVICE_HANDLE = NfcTag.class.getName() + ".extra.SERVICE_HANDLE";
    /** boolean value indicating whether the {@linkplain android.nfc.tech.IsoDep} target is an Host Card Emulation device */
    public static final String EXTRA_HOST_CARD_EMULATION = NfcTag.class.getName() + ".extra.HOST_CARD_EMULATION";

    /** int value indicating the reader slot. Use this value if you want to do issue custom commands to the reader. */
    public static final String EXTRA_TAG_SLOT_NUMBER = NfcTag.class.getName() + ".extra.SLOT_NUMBER";

    /** Ultralight subtype, essensially NTAG21x types (positive numbers) or their corresponding legacy types (negative numbers) */
    public static final String EXTRA_ULTRALIGHT_TYPE = NfcTag.class.getName() + ".extra.ULTRALIGHT_TYPE";

    public static final int EXTRA_ULTRALIGHT_TYPE_NTAG210 = 1; // aka ultralight
    public static final int EXTRA_ULTRALIGHT_TYPE_NTAG212 = 2;
    public static final int EXTRA_ULTRALIGHT_TYPE_NTAG213 = 3; // aka ultralight c or NTAG 203
    public static final int EXTRA_ULTRALIGHT_TYPE_NTAG215 = 4;
    public static final int EXTRA_ULTRALIGHT_TYPE_NTAG216 = 5;
    public static final int EXTRA_ULTRALIGHT_TYPE_NTAG213F = 6;
    public static final int EXTRA_ULTRALIGHT_TYPE_NTAG216F = 7;

}
