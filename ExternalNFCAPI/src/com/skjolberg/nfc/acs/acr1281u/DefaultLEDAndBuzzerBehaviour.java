package com.skjolberg.nfc.acs.acr1281u;

import java.util.ArrayList;
import java.util.List;

public enum DefaultLEDAndBuzzerBehaviour {

	LED_ICC_ACTIVATION_STATUS_LED(1 << 0),
	LED_PICC_POLLING_STATUS_LED(1 << 1),
	// bit 2 RFU
	// bit 3 RFU
	CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER(1 << 4),
	CONTACTLESS_CHIP_RESET_INDICATION_BUZZER(1 << 5),
	EXCLUSIVE_MODE_STATUS_BUZZER(1 << 6),
	LED_CARD_OPERATION_BLINK(1 << 7);

	private final int filter;

	private DefaultLEDAndBuzzerBehaviour(int filter) {
		this.filter = filter;
	}
	
	public int getFilter() {
		return filter;
	}
	
	public static List<DefaultLEDAndBuzzerBehaviour> parse(int value) {
		ArrayList<DefaultLEDAndBuzzerBehaviour> values = new ArrayList<DefaultLEDAndBuzzerBehaviour>();
		
		for(DefaultLEDAndBuzzerBehaviour DefaultLEDAndBuzzerBehaviour : values()) {
			if((value & DefaultLEDAndBuzzerBehaviour.getFilter()) != 0) {
				values.add(DefaultLEDAndBuzzerBehaviour);
			}
		}
		
		return values;
	}
	
	public static int serialize(DefaultLEDAndBuzzerBehaviour[] DefaultLEDAndBuzzerBehaviours) {
		int value = 0;
		
		for(DefaultLEDAndBuzzerBehaviour DefaultLEDAndBuzzerBehaviour : DefaultLEDAndBuzzerBehaviours) {
			value |= DefaultLEDAndBuzzerBehaviour.getFilter();
		}
		
		return value;
	}
}
