package com.skjolberg.nfc;

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

}
