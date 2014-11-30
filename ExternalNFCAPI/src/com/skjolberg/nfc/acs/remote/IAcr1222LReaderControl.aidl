package com.skjolberg.nfc.acs.remote;

interface IAcr1222LReaderControl {

	byte[] getFirmware();
	
	byte[] getPICC();
	
	byte[] setPICC(int picc);

	byte[] setDefaultLEDAndBuzzerBehaviours(boolean piccPollingStatusLED, boolean piccActivationStatusLED, boolean buzzerForCardInsertionOrRemoval, boolean cardOperationBlinkingLED);
	
	byte[] lightLED(boolean ready, boolean progress, boolean complete, boolean error);

	byte[] control(int slotNum, int controlCode, in byte[] command);
		
	byte[] transmit(int slotNum, in byte[] command);
	
}
