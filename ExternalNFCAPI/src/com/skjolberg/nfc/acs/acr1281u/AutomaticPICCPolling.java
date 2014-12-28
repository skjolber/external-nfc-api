package com.skjolberg.nfc.acs.acr1281u;

import java.util.ArrayList;
import java.util.List;

public enum AutomaticPICCPolling {

	AUTO_PICC_POLLING(1 << 0),
	TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND(1 << 1),
	TURN_ANTENNA_FIELD_IF_PICC_IS_INACTIVE(1 << 2),
	// bit 3 RFU
	PICC_POLLING_INTERVAL_250(0x3 << 4, 0x00 << 4),
	PICC_POLLING_INTERVAL_500(0x3 << 4, 0x01 << 4),
	PICC_POLLING_INTERVAL_1000(0x3 << 4, 0x02 << 4),
	PICC_POLLING_INTERVAL_2500(0x3 << 4, 0x03 << 4),
	// bit 6 RFU
	ENFORCE_ISO14443A_PART_4(1 << 7);

	private final int filter;
	private final int value;
	
	private AutomaticPICCPolling(int filter) {
		this(filter, filter);
	}

	private AutomaticPICCPolling(int filter, int value) {
		this.filter = filter;
		this.value = value;
	}

	public int getFilter() {
		return filter;
	}
	
	public int getValue() {
		return value;
	}

	public static List<AutomaticPICCPolling> parse(int picc) {
		ArrayList<AutomaticPICCPolling> values = new ArrayList<AutomaticPICCPolling>();
		
		for(AutomaticPICCPolling acrPICC : values()) {
			if((picc & acrPICC.getFilter()) == acrPICC.getValue()) {
				values.add(acrPICC);
			}
		}
		
		return values;
	}
	
	public static int serialize(AutomaticPICCPolling[] acrPICCs) {
		int value = 0;
		
		for(AutomaticPICCPolling acrPICC : acrPICCs) {
			value |= acrPICC.getValue();
		}
		
		return value;
	}
}
