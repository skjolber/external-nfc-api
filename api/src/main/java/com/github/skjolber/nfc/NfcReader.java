package com.github.skjolber.nfc;


public interface NfcReader {

	public static final String ACTION_READER_OPENED = NfcReader.class.getName() + ".action.READER_OPEN";

	/** Intent extras of {@linkplain NfcReader} type on {@linkplain #ACTION_READER_OPENED} actions.*/
    public static final String EXTRA_READER_CONTROL = NfcReader.class.getName() + ".action.READER_CONTROL";
	public static final String ACTION_READER_CLOSED = NfcReader.class.getName() + ".action.READER_CLOSED";

	/** Optional status code for {@linkplain #ACTION_READER_OPENED} or {@linkplain #ACTION_READER_CLOSED} actions */
    public static final String EXTRA_READER_STATUS_CODE = NfcReader.class.getName() + ".action.READER_STATUS_CODE";
	/** Optional status message for {@linkplain #ACTION_READER_OPENED} or {@linkplain #ACTION_READER_CLOSED} actions */
    public static final String EXTRA_READER_STATUS_MESSAGE = NfcReader.class.getName() + ".action.READER_STATUS_MESSAGE";

    public static final int READER_STATUS_OK = 0;
    /** generic error */
    public static final int READER_STATUS_ERROR = 1;
    public static final int READER_STATUS_ERROR_UNABLE_TO_CLAIM_USB_INTERFACE = 2;
    public static final int READER_STATUS_USB_DEVICE_DISCONNECTED = 3;
    public static final int READER_STATUS_SERVICE_STOPPED = 4;

	/** Request reader state (if service is running). The service will respond with either 
	 * {@linkplain #ACTION_READER_OPENED} or {@linkplain #ACTION_READER_CLOSED}.
	 */
	public static final String ACTION_READER_STATUS = NfcReader.class.getName() + ".action.READER_STATUS";
	
}
