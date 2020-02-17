package com.github.skjolber.nfc.acs.remote;

interface IAcr1283LReaderControl {

	byte[] getFirmware();
	
	byte[] getPICC();

	byte[] setPICC(int picc);

	byte[] getAutomaticPICCPolling();

	byte[] setAutomaticPICCPolling(int picc);

	byte[] setLEDs(int leds);

	byte[] getDefaultLEDAndBuzzerBehaviour();

	byte[] setDefaultLEDAndBuzzerBehaviour(int value);

	byte[] control(int slotNum, int controlCode, in byte[] command);
		
	byte[] transmit(int slotNum, in byte[] command);

	byte[] lightDisplayBacklight(boolean on);
	
	byte[] clearDisplay();
	
	byte[] displayText(char fontId, boolean styleBold, int line, int position, in byte[] message);

	byte[] setDisplayContrast(int contrast);
}
