package com.skjolberg.nfc.acs.remote;

interface IAcr1252UReaderControl {

	byte[] getFirmware();
	
	byte[] getPICC();

	byte[] setPICC(int picc);
	
	byte[] control(int slotNum, int controlCode, in byte[] command);
		
	byte[] transmit(int slotNum, in byte[] command);

	byte[] getDefaultLEDAndBuzzerBehaviour();

	byte[] setDefaultLEDAndBuzzerBehaviour(int value);

	byte[] setLEDs(int leds);

	byte[] getLEDs();

	byte[] getAutomaticPICCPolling();

	byte[] setAutomaticPICCPolling(int picc);

	byte[] setBuzzer(boolean enable);
}
