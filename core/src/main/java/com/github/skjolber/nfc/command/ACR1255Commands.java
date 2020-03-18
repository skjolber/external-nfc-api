package com.github.skjolber.nfc.command;

import com.acs.smartcard.ReaderException;
import com.github.skjolber.nfc.acs.AcrAutomaticPICCPolling;
import com.github.skjolber.nfc.acs.AcrLED;

import java.util.List;
import java.util.Set;

public interface ACR1255Commands {

    public static final int LED_1_GREEN = 1;
    public static final int LED_1_RED = 2;
    public static final int LED_2_BLUE = 4;
    public static final int LED_2_RED = 8;

    /**
     *
     * Note: Defaults to 10001011
     *
     * @return list of resulting picc parameters.
     * @throws ReaderException
     */

    List<AcrAutomaticPICCPolling> setAutomaticPICCPolling(int slot, AcrAutomaticPICCPolling... picc) throws ReaderException;

    List<AcrAutomaticPICCPolling> getAutomaticPICCPolling(int slot) throws ReaderException;

    Boolean setPICC(int slot, int picc) throws ReaderException;

    Integer getPICC(int slot) throws ReaderException;

    String getFirmware(int slot) throws ReaderException;

    String getSerialNumber(int slot) throws ReaderException;

    boolean setLED(int slot, int state) throws ReaderException;

    List<Set<AcrLED>> getLED(int slot) throws ReaderException;

    int getLED2(int slot) throws ReaderException;

    void setBuzzerBeepDurationOnCardDetection(int slot, int duration) throws ReaderException;

    int setDefaultLEDAndBuzzerBehaviour(int slot, int picc) throws ReaderException;

    int getDefaultLEDAndBuzzerBehaviour2(int slot) throws ReaderException;

    byte getAntennaFieldStatus(int slot) throws ReaderException;

    boolean setAntennaField(int slot, boolean on) throws ReaderException;

    byte getBluetoothTransmissionPower(int slot) throws ReaderException;

    boolean setBluetoothTransmissionPower(int slot, byte power) throws ReaderException;

    byte[] setAutoPPS(int slot, byte tx, byte rx) throws ReaderException;

    byte[] getAutoPPS(int slot) throws ReaderException;

    boolean setSleepModeOption(int slot, byte option) throws ReaderException;

    byte[] control(int slotNum, int controlCode, byte[] command) throws ReaderException;

    byte[] transmit(int slotNum, byte[] command) throws ReaderException;

    boolean setAutomaticPolling(int slotNum, boolean b) throws ReaderException;

    int getBatteryLevel(int slot) throws ReaderException;

}
