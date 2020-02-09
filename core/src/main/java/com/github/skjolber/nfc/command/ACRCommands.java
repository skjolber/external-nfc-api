package com.github.skjolber.nfc.command;

import android.util.Log;

import com.acs.smartcard.Features;
import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.acs.smartcard.TlvProperties;

import custom.java.CommandAPDU;
import custom.java.ResponseAPDU;


public class ACRCommands extends CommandUtils {

    private static final String TAG = ACRCommands.class.getName();

    private static final String[] propertyStrings = {"Unknown", "wLcdLayout",
            "bEntryValidationCondition", "bTimeOut2", "wLcdMaxCharacters",
            "wLcdMaxLines", "bMinPINSize", "bMaxPINSize", "sFirmwareID",
            "bPPDUSupport", "dwMaxAPDUDataSize", "wIdVendor", "wIdProduct"};

    protected String name;

    protected final ReaderWrapper reader;

    protected Features features;

    public ACRCommands(ReaderWrapper reader) {
        this.reader = reader;
    }

    public String getName() {
        return name;
    }

    public static boolean isSuccess(CommandAPDU response) {
        return response.getCLA() == 0xE1 && response.getP1() == 0x00 && response.getP2() == 0x00 && response.getINS() == 0x00;
    }

    public static boolean isSuccess(CommandAPDU response, int length) {
        return response.getCLA() == 0xE1 && response.getP1() == 0x00 && response.getP2() == 0x00 && response.getINS() == 0x00 && response.getData().length == length;
    }

    public static boolean isSuccessControl(byte[] in) {
        return (in[in.length - 2] & 0xFF) == 0x90;
    }

    public static boolean isSuccessControl(ResponseAPDU in) {
        return in.getSW1() == 0x90 && in.getSW2() == 0x00;
    }

    public static boolean isZero(byte[] in, int value) {
        return is(in, 0, value);
    }

    public static boolean isFirst(byte[] in, int value) {
        return is(in, 1, value);
    }

    public static boolean isSecond(byte[] in, int value) {
        return is(in, 2, value);
    }

    public static boolean is(byte[] in, int index, int value) {
        return (in[1] & 0xFF) == 0x90;
    }


    public ResponseAPDU control(int slotnum, CommandAPDU command) throws ReaderException {
        Log.d(TAG, "control " + ACRCommands.toHexString(command.getBytes()));

        byte[] control = reader.control(slotnum, Reader.IOCTL_CCID_ESCAPE, command.getBytes());

        return new ResponseAPDU(control);
    }

    public ResponseAPDU transmit(int slotnum, CommandAPDU command) throws ReaderException {
        Log.d(TAG, "transmit " + ACRCommands.toHexString(command.getBytes()));

        byte[] control = reader.transmit(slotnum, command.getBytes());

        return new ResponseAPDU(control);
    }

    public byte[] control(int slotNum, int controlCode, byte[] command) throws ReaderException {
        Log.d(TAG, "control " + ACRCommands.toHexString(command));

        return reader.control(slotNum, controlCode, command);
    }

    public byte[] transmit(int slotNum, byte[] command) throws ReaderException {
        Log.d(TAG, "transmit " + ACRCommands.toHexString(command));

        return reader.transmit(slotNum, command);
    }

    /**
     * Converts the byte array to HEX string.
     *
     * @param buffer the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buffer)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    /**
     * Converts the integer to HEX string.
     *
     * @param i the integer.
     * @return the HEX string.
     */
    private String toHexString(int i) {
        String hexString = Integer.toHexString(i);
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }

        return hexString.toUpperCase();
    }

    public void initFeatures() throws ReaderException {
        byte[] control = reader.control(0, Reader.IOCTL_GET_FEATURE_REQUEST, new byte[]{});

        features = new Features();
        features.fromByteArray(control, control.length);
    }

    public TlvProperties getProperties() throws ReaderException {
        int controlCode = features.getControlCode(Features.FEATURE_GET_TLV_PROPERTIES);

        byte[] properties = reader.control(0, controlCode, new byte[]{});

        TlvProperties readerProperties = new TlvProperties(properties, properties.length);

        /*
        for (int i = TlvProperties.PROPERTY_wLcdLayout; i <= TlvProperties.PROPERTY_wIdProduct; i++) {

            Object property = readerProperties.getProperty(i);
            if(property == null) {
                continue;
            }
            if (property instanceof Integer) {
                Log.d(TAG, propertyStrings[i] + ": " + toHexString((Integer) property));
            } else {
                Log.d(TAG, propertyStrings[i] + ": " + property);
            }
        }
        */

        return readerProperties;

    }

}
