package com.skjolberg.nfc.acs;

import java.util.ArrayList;
import java.util.List;

/**
 * ACR 1281 and ACR 1283 and 1252
 * 
 * @author thomas
 *
 */

public enum AcrAutomaticPICCPolling {

	AUTO_PICC_POLLING(1 << 0),
	TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND(1 << 1),
	TURN_ANTENNA_FIELD_IF_PICC_IS_INACTIVE(1 << 2),
	
	ACTIVATE_PICC_WHEN_DETECTED(1 << 3), // for ACR 1252
	
	PICC_POLLING_INTERVAL_250(0x3 << 4, 0x00 << 4),
	PICC_POLLING_INTERVAL_500(0x3 << 4, 0x01 << 4),
	PICC_POLLING_INTERVAL_1000(0x3 << 4, 0x02 << 4),
	PICC_POLLING_INTERVAL_2500(0x3 << 4, 0x03 << 4),
	// bit 6 RFU
	ENFORCE_ISO14443A_PART_4(1 << 7);

	private final int filter;
	private final int value;
	
	private AcrAutomaticPICCPolling(int filter) {
		this(filter, filter);
	}

	private AcrAutomaticPICCPolling(int filter, int value) {
		this.filter = filter;
		this.value = value;
	}

	public int getFilter() {
		return filter;
	}
	
	public int getValue() {
		return value;
	}

	public static List<AcrAutomaticPICCPolling> parse(int picc) {
		ArrayList<AcrAutomaticPICCPolling> values = new ArrayList<AcrAutomaticPICCPolling>();
		
		for(AcrAutomaticPICCPolling acrPICC : values()) {
			if((picc & acrPICC.getFilter()) == acrPICC.getValue()) {
				values.add(acrPICC);
			}
		}
		
		return values;
	}
	
	public static int serialize(AcrAutomaticPICCPolling[] acrPICCs) {
		int value = 0;
		
		for(AcrAutomaticPICCPolling acrPICC : acrPICCs) {
			value |= acrPICC.getValue();
		}
		
		return value;
	}
}
