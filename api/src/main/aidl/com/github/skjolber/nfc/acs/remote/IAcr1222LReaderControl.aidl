package com.github.skjolber.nfc.acs.remote;

interface IAcr1222LReaderControl {

	byte[] getFirmware();
	
	byte[] getPICC();
	
	byte[] setPICC(int picc);

	byte[] getDefaultLEDAndBuzzerBehaviour();

	byte[] setDefaultLEDAndBuzzerBehaviour(int value);

	byte[] setLEDs(int leds);
	
	byte[] control(int slotNum, int controlCode, in byte[] command);
		
	byte[] transmit(int slotNum, in byte[] command);
	
	byte[] lightDisplayBacklight(boolean on);
	
	byte[] clearDisplay();
	
	byte[] displayText(char fontId, boolean styleBold, int line, int position, in byte[] message);
	
}
