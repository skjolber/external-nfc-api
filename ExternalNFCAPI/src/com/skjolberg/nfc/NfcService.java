package com.skjolberg.nfc;


public interface NfcService {

	public static final String ACTION_SERVICE_STARTED = NfcService.class.getName() + ".action.SERVICE_STARTED";
	public static final String ACTION_SERVICE_STOPPED = NfcService.class.getName() + ".action.SERVICE_STOPPED";
	
	/** Request service state (if running). If the service is running, it will respond with a corresponding {@linkplain #ACTION_SERVICE_STARTED}. */
	public static final String ACTION_SERVICE_STATUS = NfcService.class.getName() + ".action.SERVICE_STATUS";

}
