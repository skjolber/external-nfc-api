package com.github.skjolber.nfc.acs;

public enum AcrPICC {

	AUTO_PICC_POLLING,
	AUTO_ATS_GENERATION,
	/** 1 for 250, 0 for 500 milliseconds */
	POLLING_INTERVAL,
	POLL_FELICA_424K,
	POLL_FELICA_212K,
	POLL_TOPAZ,
	POLL_ISO14443_TYPE_B,
	POLL_ISO14443_TYPE_A;


}
