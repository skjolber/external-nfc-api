package com.skjolberg.nfc;


public interface NfcReader {

	public static final String ACTION_READER_OPENED = NfcReader.class.getName() + ".action.READER_OPEN";
	
	/** Intent extras of {@linkplain NfcReader} type on {@linkplain #ACTION_READER_OPENED} actions.*/
    public static final String EXTRA_READER_CONTROL = NfcReader.class.getName() + ".action.READER_CONTROL";
	public static final String ACTION_READER_CLOSED = NfcReader.class.getName() + ".action.READER_CLOSED";
	
	/** Request reader state (if service is running). The service will respond with either 
	 * {@linkplain #ACTION_READER_OPENED} or {@linkplain #ACTION_READER_CLOSED}.
	 */
	public static final String ACTION_READER_STATUS = NfcReader.class.getName() + ".action.READER_STATUS";
	
}
