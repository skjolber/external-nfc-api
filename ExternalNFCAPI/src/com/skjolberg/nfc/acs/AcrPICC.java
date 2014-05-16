package com.skjolberg.nfc.acs;

import java.util.ArrayList;
import java.util.List;

public enum AcrPICC {

	AUTO_PICC_POLLING(1 << 7),
	AUTO_ATS_GENERATION(1 << 6),
	POLLING_INTERVAL(1 << 5),
	POLL_FELICA_424K(1 << 4),
	POLL_FELICA_212K(1 << 3),
	POLL_TOPAZ(1 << 2),
	POLL_ISO14443_TYPE_B(1 << 1),
	POLL_ISO14443_TYPE_A(1);

	private final int filter;
	
	private AcrPICC(int filter) {
		this.filter = filter;
	}
	
	public int getFilter() {
		return filter;
	}
	
	public static List<AcrPICC> parse(int picc) {
		ArrayList<AcrPICC> values = new ArrayList<AcrPICC>();
		
		for(AcrPICC acrPICC : values()) {
			if((picc & acrPICC.getFilter()) != 0) {
				values.add(acrPICC);
			}
		}
		
		return values;
	}
	
	public static int serialize(AcrPICC[] acrPICCs) {
		int value = 0;
		
		for(AcrPICC acrPICC : acrPICCs) {
			value |= acrPICC.getFilter();
		}
		
		return value;
	}

}
