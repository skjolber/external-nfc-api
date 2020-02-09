package com.github.skjolber.nfc.acs;

/**
 * Antenna Field Status
 */

public enum AcrAntennaFieldStatus {

    /** Power off */
    PICC_POWER_OFF,
    /** Ready to Poll Contactless Tag, but not detected */
    PICC_IDLE,
    /** PICC Request (Ref to ISO 14443) Successful, i.e.
     Contactless Tag Detected */
    PICC_READY,
    /** PICC Select (Ref to ISO 14443) Successful */
    PICC_SELECTED,
    /** Activate */
    PICC_ACTIVE

}
