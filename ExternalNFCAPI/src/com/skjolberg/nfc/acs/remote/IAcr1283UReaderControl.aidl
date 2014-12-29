package com.skjolberg.nfc.acs.remote;

interface IAcr1283UReaderControl {

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

}
