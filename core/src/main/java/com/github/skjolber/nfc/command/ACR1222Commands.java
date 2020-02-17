package com.github.skjolber.nfc.command;

import java.nio.charset.Charset;
import java.util.List;

import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.github.skjolber.nfc.acs.Acr1283LReader;
import com.github.skjolber.nfc.acs.AcrDefaultLEDAndBuzzerBehaviour;
import com.github.skjolber.nfc.acs.AcrReaderException;

import custom.java.CommandAPDU;
import custom.java.ResponseAPDU;


public class ACR1222Commands extends ACRCommands {

    private static final String TAG = ACR1222Commands.class.getName();

    public static final int DEFAULT_LED_AND_BUZZER_BEHAVIOUR_LED_PICC_POLLING_STATUS = 1 << 1;
    public static final int DEFAULT_LED_AND_BUZZER_BEHAVIOUR_LED_PICC_ACTIVATION_STATUS_LED = 1 << 2;
    public static final int DEFAULT_LED_AND_BUZZER_BEHAVIOUR_BUZZER_BEEP_ON_TAG_TRANSITION = 1 << 4;
    public static final int DEFAULT_LED_AND_BUZZER_BEHAVIOUR_LED_CARD_OPERATION_BLINK = 1 << 7;

    public static final int LED_3_STATE = 1 << 3;
    public static final int LED_2_STATE = 1 << 2;
    public static final int LED_1_STATE = 1 << 1;
    public static final int LED_0_STATE = 1;

    public static final int LCD_BACKLIGHT_OFF = 0;
    public static final int LCD_BACKLIGHT_ON = 0xFF;

    public static final int PICC_OPERATING_PARAMETER_AUTO_PICC_POLLING = 1 << 7;
    public static final int PICC_OPERATING_PARAMETER_AUTO_ATS_GENERATION = 1 << 6;
    public static final int PICC_OPERATING_PARAMETER_POLLING_INTERVAL = 1 << 5;
    public static final int PICC_OPERATING_PARAMETER_POLL_FELICA_424K = 1 << 4;
    public static final int PICC_OPERATING_PARAMETER_POLL_FELICA_212K = 1 << 3;
    public static final int PICC_OPERATING_PARAMETER_POLL_TOPAZ = 1 << 2;
    public static final int PICC_OPERATING_PARAMETER_POLL_ISO14443_TYPE_B = 1 << 1;
    public static final int PICC_OPERATING_PARAMETER_POLL_ISO14443_TYPE_A = 1;

    public ACR1222Commands(String name, ReaderWrapper reader) {
        super(reader);
        this.name = name;
    }

    public Integer getACR122PICC(int slot) throws AcrReaderException {
        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x50, 0x00, 0x00};

        byte[] in;
        synchronized (reader) {
            try {
                in = reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        if (!isSuccessControl(in)) {
            Log.e(TAG, "Unable to read PICC, response was " + Utils.toHexString(in));

            throw new IllegalArgumentException();
        }

        final Integer operation = in[0] & 0xFF;

        Log.d(TAG, "Read 122 PICC " + Integer.toHexString(operation));

        return operation;
    }

    public int getPICC(int slot) throws AcrReaderException {
        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x50, 0x00, 0x00};

        byte[] in = new byte[6];

        //reader.transmit(slot, out, out.length, in, in.length);
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

            Log.d(TAG, "Read 1222L PICC " + Integer.toHexString(operation));

            return operation;
        } else {
            Log.e(TAG, "Unable to read 1222L PICC " + ACRCommands.toHexString(in));

            return -1;

        }
    }

    /**
     * Set PICC Operation Parameter
     * v1.01
     *
     * @param slot
     * @param picc
     * @return
     * @throws AcrReaderException
     */


    public Boolean setPICC(int slot, int picc) throws AcrReaderException {
        if ((picc & 0x3) != picc) throw new RuntimeException();

        byte[] pseudo = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x20, 0x01, (byte) picc};

        byte[] in;
        synchronized (reader) {
            try {
                in = reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        CommandAPDU response = new CommandAPDU(in);

        if (response.getCLA() == 0xE1 && response.getNc() == 1) {
            final int operation = response.getData()[0] & 0x03;

            if (operation != picc) {
                Log.w(TAG, "Unable to properly update PICC for ACR 1222: Expected " + Integer.toHexString(picc) + " got " + Integer.toHexString(operation));

                return Boolean.FALSE;
            } else {
                Log.d(TAG, "Updated PICC " + Integer.toHexString(operation) + " (" + Integer.toHexString(picc) + ")");

                return Boolean.TRUE;
            }
        } else {
            Log.d(TAG, "Unable to set PICC: ");

            throw new IllegalArgumentException();
        }
    }

    public Boolean setACR122PICC(int slot, boolean iso14443TypeA, boolean iso14443TypeB, boolean topaz, boolean feliCa212K, boolean feliCa424K, boolean polling, boolean autoATSGeneration, boolean autoPICCPolling) {
        int picc = 0;

        if (iso14443TypeA) {
            picc |= PICC_OPERATING_PARAMETER_POLL_ISO14443_TYPE_A;
        }
        if (iso14443TypeB) {
            picc |= PICC_OPERATING_PARAMETER_POLL_ISO14443_TYPE_B;
        }
        if (topaz) {
            picc |= PICC_OPERATING_PARAMETER_POLL_TOPAZ;
        }
        if (feliCa212K) {
            picc |= PICC_OPERATING_PARAMETER_POLL_FELICA_212K;
        }
        if (feliCa424K) {
            picc |= PICC_OPERATING_PARAMETER_POLL_FELICA_424K;
        }
        if (polling) {
            picc |= PICC_OPERATING_PARAMETER_POLLING_INTERVAL;
        }
        if (autoATSGeneration) {
            picc |= PICC_OPERATING_PARAMETER_AUTO_ATS_GENERATION;
        }
        if (autoPICCPolling) {
            picc |= PICC_OPERATING_PARAMETER_AUTO_PICC_POLLING;
        }

        return setACR122PICC(slot, picc);
    }


    public Boolean setACR122PICC(int slot, int picc) throws AcrReaderException {
        if ((picc & 0xFF) != picc) throw new RuntimeException();

        CommandAPDU command = new CommandAPDU(0xFF, 0x00, 0x51, picc);
        //byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x51, (byte) picc, 0x00};

        byte[] pseudo = command.getBytes();

        byte[] in = new byte[10];

        synchronized (reader) {
            try {
                reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo, pseudo.length, in, in.length);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        if (!isSuccessControl(in)) {
            Log.e(TAG, "Unable to read PICC");

            throw new IllegalArgumentException("Card responded with error code");
        }

        final int operation = in[0] & 0xFF;

        if (operation != picc) {
            Log.w(TAG, "Unable to properly update PICC for ACR 1222: Expected " + Integer.toHexString(picc) + " got " + Integer.toHexString(operation));

            return Boolean.FALSE;
        } else {
            Log.d(TAG, "Updated PICC " + Integer.toHexString(operation) + " (" + Integer.toHexString(picc) + ")");

            return Boolean.TRUE;
        }
    }

    public boolean setDefaultLEDAndBuzzerBehaviours(int slot, boolean piccPollingStatusLED, boolean piccActivationStatusLED, boolean buzzerForCardInsertionOrRemoval, boolean cardOperationBlinkingLED) throws ReaderException {
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

        return setDefaultLEDAndBuzzerBehaviour(slot, behaviour);
    }

    public boolean setDefaultLEDAndBuzzerBehaviour(int slot, int picc) throws ReaderException {
        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x21, new byte[]{(byte) picc});

        CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

        if (!isSuccess(response)) {
            throw new IllegalArgumentException("Card responded with error code");
        }

        final int operation = response.getData()[0] & 0xFF;

        Log.d(TAG, "Set default LED and buzzer behaviour " + Integer.toHexString(operation) + " (" + picc + ")");

        return operation == picc;
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


    public String getFirmware(int slot) throws AcrReaderException {
        byte[] pseudo = new byte[]{(byte) 0xE0, 0x00, 0x00, 0x18, 0x00};

        byte[] in = new byte[300];

        int responseLength;
        synchronized (reader) {
            try {
                responseLength = reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo, pseudo.length, in, in.length);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        //int length = (in[4] & 0xff) + 5;

        byte[] adpu = new byte[responseLength];
        System.arraycopy(in, 0, adpu, 0, adpu.length);

        CommandAPDU response = new CommandAPDU(adpu);

        if (response.getCLA() == 0xE1 && response.getP1() == 0x00 && response.getP2() == 0x00) {
            String firmware = new String(response.getData(), Charset.forName("ASCII"));

            Log.d(TAG, "Read firmware " + firmware);

            return firmware;
        } else {
            Log.d(TAG, "Failed to read firmware");

            throw new IllegalArgumentException("Card responded with error code");
        }

    }

    /**
     * v1.01
     *
     * @param slot
     * @return
     * @throws AcrReaderException
     */

    public byte[] readSerialNumber2(int slot) throws AcrReaderException {

        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x33, 0x00);

        byte[] pseudo = command.getBytes();

        byte[] in = new byte[5 + 16];

        synchronized (reader) {
            try {
                reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo, pseudo.length, in, in.length);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        CommandAPDU response = new CommandAPDU(in);

        if (response.getCLA() == 0xE1 && response.getNc() == 16) {
            Log.d(TAG, "Successfully read serial number");

            return response.getData();
        } else {
            Log.d(TAG, "Failed to set buzzer on card control");

            throw new IllegalArgumentException("Card responded with error code");
        }
    }

    public byte[] readSerialNumber(int slot) throws AcrReaderException {
        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x49, 0x00, 0x00};

        byte[] in = new byte[300];

        synchronized (reader) {
            try {
                reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo, pseudo.length, in, in.length);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        return in;
    }

    public String readACR122Firmware(int slot) throws AcrReaderException {
        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x48, 0x00, 0x00};

        byte[] in;
        synchronized (reader) {
            try {
                in = reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        String firmware = new String(in, Charset.forName("ASCII"));

        Log.d(TAG, "Read firmware " + firmware);

        return firmware;
    }

    /**
     * This seems not to work, the updated duration is much longer than expected.
     * <p>
     * v1.01
     *
     * @param slot
     * @param duration
     * @return
     * @throws AcrReaderException
     */

    public boolean setBuzzerBeepDurationOnCardDetection(int slot, int duration) throws AcrReaderException {
        if ((duration & 0xFF) != duration) throw new RuntimeException();

        CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x28, new byte[]{(byte) duration});

        byte[] pseudo = command.getBytes();

        Log.d(TAG, "Write data " + ACRCommands.toHexString(pseudo));

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
            Log.d(TAG, "Successfully set buzzer beep duration on card control");

            return true;
        } else {
            Log.d(TAG, "Failed to set buzzer beep duration on card control");

            return false;
        }
    }

    /**
     * v1.00
     *
     * @param slot
     * @param parameter
     * @return
     * @throws AcrReaderException
     */

    public boolean beepBuzzer(int slot, int t1, int t2, int repetitions) throws AcrReaderException {
        if ((t1 & 0xFF) != t1) throw new RuntimeException();
        if ((t2 & 0xFF) != t2) throw new RuntimeException();
        if ((repetitions & 0xFF) != repetitions) throw new RuntimeException();

        CommandAPDU command = new CommandAPDU(0xFF, 0x00, 0x42, 0x00, new byte[]{(byte) t1, (byte) t2, (byte) repetitions});

        byte[] pseudo = command.getBytes();

        Log.d(TAG, "Write data " + ACRCommands.toHexString(pseudo));

        byte[] in = new byte[6];

        synchronized (reader) {
            try {
                reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo, pseudo.length, in, in.length);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        Log.d(TAG, "Read " + ACRCommands.toHexString(in));

        if (isSuccessControl(in)) {
            Log.d(TAG, "Successfully set buzzer control");

            return true;
        } else {
            Log.d(TAG, "Failed to set buzzer control");

            return false;
        }
    }

    /**
     * v1.00
     *
     * @param slot
     * @param parameter - time out parameter (unit: 5 sec)
     * @return true if success
     * @throws AcrReaderException
     */

    public boolean setTimeoutParameter(int slot, int parameter) throws AcrReaderException {
        if ((parameter & 0xFF) != parameter) throw new RuntimeException();

        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x41, (byte) (parameter), 0x00};

        byte[] in = new byte[8];

        synchronized (reader) {
            try {
                reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo, pseudo.length, in, in.length);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        if (isSuccessControl(in)) {
            Log.d(TAG, "Successfully set timeout parameter");

            return true;
        } else {
            Log.d(TAG, "Failed to set timeout parameter");

            return false;
        }
    }

    /**
     * Control LED and Buzzer states.
     * <p>
     * v1.00
     *
     * @param slot
     * @param state
     * @param t1
     * @param t2
     * @param repetitions
     * @param linkToBuzzer
     * @return
     * @throws AcrReaderException
     */

    public boolean blinkLEDAndBeepBuzzer(int slot, int state, int t1, int t2, int repetitions, int linkToBuzzer) throws AcrReaderException {
        if ((state & 0xFF) != state) throw new RuntimeException();
        if ((t1 & 0xFF) != t1) throw new RuntimeException();
        if ((t2 & 0xFF) != t2) throw new RuntimeException();
        if ((repetitions & 0xFF) != repetitions) throw new RuntimeException();
        if ((linkToBuzzer & 0xFF) != linkToBuzzer) throw new RuntimeException();

        CommandAPDU command = new CommandAPDU(0xFF, 0x00, 0x40, state, new byte[]{(byte) t1, (byte) t2, (byte) repetitions, (byte) linkToBuzzer});

        byte[] pseudo = command.getBytes();

        Log.d(TAG, "Write data " + ACRCommands.toHexString(pseudo));

        byte[] in = new byte[6];

        synchronized (reader) {
            try {
                reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo, pseudo.length, in, in.length);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }
        Log.d(TAG, "Read data " + ACRCommands.toHexString(in));

        ResponseAPDU response = new ResponseAPDU(in);

        if (response.getSW1() == 0x90) {
            Log.d(TAG, "Successfully set buzzer control to " + Integer.toHexString(response.getSW2()));

            return true;
        } else {
            Log.d(TAG, "Failed to set buzzer control");

            return false;
        }
    }

    /**
     * Set the LED behaviour when a card is detected
     * v1.00
     *
     * @param slot
     * @param enable
     * @return
     * @throws AcrReaderException
     */

    public boolean setLEDOnCardDetection(int slot, boolean enable) throws AcrReaderException {
        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x43, (byte) (enable ? 00 : 0xFF), 0x00};

        Log.d(TAG, "Write data " + ACRCommands.toHexString(pseudo));

        byte[] in = new byte[10];

        synchronized (reader) {
            try {
                reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo, pseudo.length, in, in.length);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        Log.d(TAG, "Read data " + ACRCommands.toHexString(in));

        ResponseAPDU response = new ResponseAPDU(in);

        if (response.getSW1() != 0x90 || response.getSW2() != 0x00) {
            Log.d(TAG, "Unable to set LED for card detection");

            return false;
        }

        Log.d(TAG, "Set LED for card detection to " + (enable ? "on" : "off"));

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
     * @throws AcrReaderException
     */

    public boolean lightLED(int slot, boolean ready, boolean progress, boolean complete, boolean error) throws AcrReaderException {
        int ledState = 0;
        if (ready) {
            ledState |= LED_0_STATE; // green
        }
        if (progress) {
            ledState |= LED_1_STATE; // blue
        }
        if (complete) {
            ledState |= LED_2_STATE; // orange
        }
        if (error) {
            ledState |= LED_3_STATE; // red
        }
        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x44, (byte) (ledState), 0x00};

        Log.d(TAG, "Write data " + ACRCommands.toHexString(pseudo));

        byte[] in = new byte[10];

        synchronized (reader) {
            try {
                reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo, pseudo.length, in, in.length);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        Log.d(TAG, "Read data " + ACRCommands.toHexString(in));

        ResponseAPDU response = new ResponseAPDU(in);

        if (!isSuccessControl(response)) {
            Log.d(TAG, "Unable to set LED state");

            return false;
        }

        Log.d(TAG, "Set LED state");

        return true;
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
            throw new IllegalArgumentException();
        }

        Log.d(TAG, "Set LED state to " + (0xFF & response.getData()[0]));

        return true;
    }

}
