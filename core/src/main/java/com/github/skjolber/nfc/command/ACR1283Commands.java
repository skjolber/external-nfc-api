package com.github.skjolber.nfc.command;

import java.nio.charset.Charset;
import java.util.List;

import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.github.skjolber.nfc.acs.Acr1283LReader;
import com.github.skjolber.nfc.acs.AcrAutomaticPICCPolling;
import com.github.skjolber.nfc.acs.AcrDefaultLEDAndBuzzerBehaviour;
import com.github.skjolber.nfc.acs.AcrReaderException;
import com.github.skjolber.nfc.command.acr1281.PICCOperatingParameter;

import custom.java.CommandAPDU;
import custom.java.ResponseAPDU;

public class ACR1283Commands extends ACRCommands {

    public static final int DEFAULT_LED_AND_BUZZER_BEHAVIOUR_LED_PICC_POLLING_STATUS = 1 << 1;
    public static final int DEFAULT_LED_AND_BUZZER_BEHAVIOUR_LED_PICC_ACTIVATION_STATUS_LED = 1 << 2;
    public static final int DEFAULT_LED_AND_BUZZER_BEHAVIOUR_BUZZER_BEEP_ON_TAG_TRANSITION = 1 << 4;
    public static final int DEFAULT_LED_AND_BUZZER_BEHAVIOUR_LED_CARD_OPERATION_BLINK = 1 << 7;

    public static final int LCD_BACKLIGHT_OFF = 0;
    public static final int LCD_BACKLIGHT_ON = 0xFF;

    private static final int LED_GREEN = 1;
    private static final int LED_BLUE = 1 << 1;
    private static final int LED_ORANGE = 1 << 2;
    private static final int LED_RED = 1 << 3;

    private static final String TAG = ACR1283Commands.class.getName();

    private ReaderWrapper reader;

    public ACR1283Commands(String name, ReaderWrapper reader) {
        super(reader);
        this.name = name;
    }

    public List<AcrAutomaticPICCPolling> setAutomaticPICCPolling(int slot, AcrAutomaticPICCPolling... picc) throws ReaderException {
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x23, new byte[]{(byte) AcrAutomaticPICCPolling.serialize(picc)});

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        final int operation = response.getData()[0] & 0xFF;

        return AcrAutomaticPICCPolling.parse(operation);
    }

    public List<AcrAutomaticPICCPolling> getAutomaticPICCPolling(int slot) throws ReaderException {
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x23, 0x00};

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        final int operation = response.getData()[0] & 0xFF;

        return AcrAutomaticPICCPolling.parse(operation);
    }


    public PICCOperatingParameter setPICC(int slot, PICCOperatingParameter picc) throws ReaderException {
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x20, new byte[]{(byte) picc.getOperation()});

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        final int operation = response.getData()[0] & 0xFF;

        Log.d(TAG, "Set PICC " + Integer.toHexString(operation) + " (" + Integer.toHexString(picc.getOperation()) + ")");

        return new PICCOperatingParameter(operation);
    }

    public PICCOperatingParameter getPICC(int slot) throws ReaderException {
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x20, 0x0};

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response, 1)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        final int operation = response.getData()[0] & 0xFF;

        Log.d(TAG, "Read PICC " + Integer.toHexString(operation));

        return new PICCOperatingParameter(operation);
    }

    public String getFirmware(int slot) throws ReaderException {
        byte[] pseudo = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x18, 0x00};

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        String firmware = new String(response.getData(), Charset.forName("ASCII"));

        Log.d(TAG, "Read firmware " + firmware);

        return firmware;
    }

    /**
     * Control the current state of the LEDs.
     *
     * @param slot
     * @param ready    green led
     * @param progress blue led
     * @param complete orange led
     * @param error
     * @return
     * @throws ReaderException
     */

    public boolean setLED(int slot, int state) throws ReaderException {

        CommandAPDU command = new CommandAPDU(0xFF, 0x00, 0x00, 0x44, new byte[]{(byte) (state)});

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response, 1)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        Log.d(TAG, "Set LED state to " + (0xFF & response.getData()[0]));

        return true;
    }

    /**
     * Control the current state of the LEDs.
     *
     * @param slot
     * @param ready    green led
     * @param progress blue led
     * @param complete orange led
     * @param error    red
     * @return
     * @throws ReaderException
     */

    public boolean lightLED(int slot, boolean ready, boolean progress, boolean complete, boolean error) throws ReaderException {
        int ledState = 0;
        if (ready) {
            ledState |= LED_GREEN; // green
        }
        if (progress) {
            ledState |= LED_BLUE; // blue
        }
        if (complete) {
            ledState |= LED_ORANGE; // orange
        }
        if (error) {
            ledState |= LED_RED; // red
        }

        return setLED(slot, ledState);
    }

    public void setBuzzerBeepDurationOnCardDetection(int slot, int duration) throws ReaderException {
        if ((duration & 0xFF) != duration) throw new RuntimeException();

        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x28, new byte[]{(byte) duration});

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response, 1)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        final int operation = response.getData()[0] & 0xFF;

        if (operation != 0x00) {
            throw new IllegalArgumentException();
        }
    }

    public List<AcrDefaultLEDAndBuzzerBehaviour> setDefaultLEDAndBuzzerBehaviour(int slot, AcrDefaultLEDAndBuzzerBehaviour... picc) throws ReaderException {
        return Acr1283LReader.parseBehaviour(setDefaultLEDAndBuzzerBehaviour(slot, Acr1283LReader.serializeBehaviour(picc)));
    }

    public int setDefaultLEDAndBuzzerBehaviour(int slot, int picc) throws ReaderException {
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x21, new byte[]{(byte) picc});

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        Log.d(TAG, "Set default LED and buzzer behaviour " + Integer.toHexString(operation) + " (" + picc + ")");

        return operation;
    }

    public boolean setDefaultLEDAndBuzzerBehaviours(int slot, boolean piccPollingStatusLED, boolean piccActivationStatusLED, boolean buzzerForCardInsertionOrRemoval, boolean cardOperationBlinkingLED) throws AcrReaderException {
        int behaviour = 0;

        if (piccPollingStatusLED) {
            behaviour |= DEFAULT_LED_AND_BUZZER_BEHAVIOUR_LED_PICC_POLLING_STATUS;
        }
        if (piccActivationStatusLED) {
            behaviour |= DEFAULT_LED_AND_BUZZER_BEHAVIOUR_LED_PICC_ACTIVATION_STATUS_LED;
        }
        if (buzzerForCardInsertionOrRemoval) {
            behaviour |= DEFAULT_LED_AND_BUZZER_BEHAVIOUR_BUZZER_BEEP_ON_TAG_TRANSITION;
        }
        if (cardOperationBlinkingLED) {
            behaviour |= DEFAULT_LED_AND_BUZZER_BEHAVIOUR_LED_CARD_OPERATION_BLINK;
        }

        byte[] pseudo = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x21, 0x01, (byte) behaviour};

        byte[] in = new byte[6];

        synchronized (reader) {
            try {
                reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo, pseudo.length, in, in.length);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        CommandAPDU response = new CommandAPDU(in);

        if (response.getCLA() == 0xE1 && response.getNc() == 1) {
            final int operation = response.getData()[0] & 0xFF;

            if (operation != behaviour) {
                Log.w(TAG, "Unable to properly set LED and buzzer default behaviours for ACR 1283: Expected " + Integer.toHexString(behaviour) + " got " + Integer.toHexString(operation));
            } else {
                Log.d(TAG, "Successfully set LED and buzzer default behaviours");
            }

            return true;
        } else {
            Log.d(TAG, "Failed to set LED and buzzer default behaviours");

            return false;
        }
    }


    public List<AcrDefaultLEDAndBuzzerBehaviour> getDefaultLEDAndBuzzerBehaviour(int slot) throws ReaderException {
        final int operation = getDefaultLEDAndBuzzerBehaviour2(slot);

        Log.d(TAG, "Read default LED and buzzer behaviour " + Integer.toHexString(operation));

        return Acr1283LReader.parseBehaviour(operation);
    }

    public int getDefaultLEDAndBuzzerBehaviour2(int slot) throws ReaderException {
        byte[] command = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x21, 0x00};

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException();
        }

        final int operation = response.getData()[0] & 0xFF;

        Log.d(TAG, "Read default LED and buzzer behaviour " + Integer.toHexString(operation));

        return operation;
    }

    public boolean lightBacklight(int slot, boolean on) throws AcrReaderException {
        int ledState;
        if (on) {
            ledState = LCD_BACKLIGHT_ON; // green
        } else {
            ledState = LCD_BACKLIGHT_OFF;
        }

        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x64, (byte) (ledState), 0x00};

        byte[] in;
        synchronized (reader) {
            try {
                in = reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        ResponseAPDU response = new ResponseAPDU(in);

        if (!isSuccessControl(response)) {
            Log.d(TAG, "Unable to set backlight state");

            return false;
        }

        Log.d(TAG, "Set backlight state to " + (on ? "on" : "off"));

        return true;
    }

    public boolean setDisplayContrast(int slot, int contrast) throws AcrReaderException {

        if (contrast > 0x0F || contrast < 0) {
            throw new IllegalArgumentException("Expect contrast in range 0x0 to 0xF");
        }

        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x6C, (byte) (contrast), 0x00};

        byte[] in;
        synchronized (reader) {
            try {
                in = reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        ResponseAPDU response = new ResponseAPDU(in);

        if (!isSuccessControl(response)) {
            Log.d(TAG, "Unable to set backlight contrast");

            return false;
        }

        Log.d(TAG, "Set backlight contrast to " + Integer.toHexString(contrast));

        return true;
    }

    public boolean clearLCD(int slot) throws AcrReaderException {
        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x60, 0x00, 0x00};

        byte[] in;
        synchronized (reader) {
            try {
                in = reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        ResponseAPDU response = new ResponseAPDU(in);

        if (!isSuccessControl(response)) {
            Log.d(TAG, "Unable to clear LCD");

            return false;
        }

        Log.d(TAG, "Cleared LCD");

        return true;
    }

    public boolean displayText(int slot, FontSet font, boolean bold, int line, int position, byte[] message) throws AcrReaderException {

        if (line >= font.getLines()) {
            throw new IllegalArgumentException("Font " + font + " supports " + font.getLines() + " lines");
        }

        if (message.length > font.getLineLength()) {
            throw new IllegalArgumentException("Font " + font + " supports " + font.getLineLength() + " chars per line and does not wrap");
        }

        if (position >= font.getLineLength()) {
            throw new IllegalArgumentException("Font " + font + " supports " + font.getLineLength() + " chars per line");
        }

        if (position + message.length > font.getLineLength()) {
            throw new IllegalArgumentException("Font " + font + " supports " + font.getLineLength() + " chars per line");
        }

        byte[] pseudo = new byte[5 + message.length];

        pseudo[0] = (byte) 0xFF;
        pseudo[1] = (byte) ((bold ? 0x01 : 0x00) | (font.getValue() << 4)); // option
        pseudo[2] = (byte) 0x68;
        pseudo[3] = (byte) (line * font.getAddressIncrement() + position); // xyPosition
        pseudo[4] = (byte) (message.length); // message length
        System.arraycopy(message, 0, pseudo, 5, message.length);

        byte[] in;
        synchronized (reader) {
            try {
                in = reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }


        ResponseAPDU response = new ResponseAPDU(in);

        if (!isSuccessControl(response)) {
            Log.d(TAG, "Unable to set text: " + ACRCommands.toHexString(in));

            return false;
        }

        Log.d(TAG, "Set text");

        return true;
    }


}
