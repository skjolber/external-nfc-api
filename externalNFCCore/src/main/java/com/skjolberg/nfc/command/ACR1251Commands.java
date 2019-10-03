package com.skjolberg.nfc.command;

import java.nio.charset.Charset;

import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.skjolberg.nfc.acs.AcrReaderException;

public class ACR1251Commands extends ACRCommands {

    public static final int PICC_OPERATING_PARAMETER_AUTO_PICC_POLLING = 1 << 7;
    public static final int PICC_OPERATING_PARAMETER_AUTO_ATS_GENERATION = 1 << 6;
    public static final int PICC_OPERATING_PARAMETER_POLLING_INTERVAL = 1 << 5;
    public static final int PICC_OPERATING_PARAMETER_POLL_FELICA_424K = 1 << 4;
    public static final int PICC_OPERATING_PARAMETER_POLL_FELICA_212K = 1 << 3;
    public static final int PICC_OPERATING_PARAMETER_POLL_TOPAZ = 1 << 2;
    public static final int PICC_OPERATING_PARAMETER_POLL_ISO14443_TYPE_B = 1 << 1;
    public static final int PICC_OPERATING_PARAMETER_POLL_ISO14443_TYPE_A = 1;

    private static final String TAG = ACR1251Commands.class.getName();

    public ACR1251Commands(String name, ReaderWrapper reader) {
        super(reader);
        this.name = name;
    }

    public Integer getPICC(int slot) throws AcrReaderException {
        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x50, 0x00, 0x00};

        byte[] in = new byte[2];

        //reader.transmit(slot, out, out.length, in, in.length);
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

        final int operation = in[1] & 0xFF;

        Log.d(TAG, "Read PICC " + Integer.toHexString(operation));

        return operation;
    }

    public Boolean setPICC(int slot, boolean iso14443TypeA, boolean iso14443TypeB, boolean topaz, boolean feliCa212K, boolean feliCa424K, boolean polling, boolean autoATSGeneration, boolean autoPICCPolling) {
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

        return setPICC(slot, picc);
    }

    public Boolean setPICC(int slot, int picc) throws AcrReaderException {
        if ((picc & 0xFF) != picc) throw new RuntimeException();

        byte[] pseudo = new byte[]{(byte) 0xFF, 0x00, 0x51, (byte) picc, 0x00};

        byte[] in;

        synchronized (reader) {
            try {
                in = reader.control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);
            } catch (ReaderException e) {
                throw new AcrReaderException(e);
            }
        }

        if (!isSuccessControl(in)) {
            Log.d(TAG, "Unable to set PICC: " + toHexString(in));

            throw new IllegalArgumentException("Card responded with error code");
        }

        final int operation = in[1] & 0xFF;

        if (operation != picc) {
            Log.w(TAG, "Unable to properly update PICC for ACR 1222: Expected " + Integer.toHexString(picc) + " got " + Integer.toHexString(operation));

            return Boolean.FALSE;
        } else {
            Log.d(TAG, "Updated PICC " + Integer.toHexString(operation) + " (" + Integer.toHexString(picc) + ")");

            return Boolean.TRUE;
        }
    }

    public String getFirmware(int slot) throws AcrReaderException {
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


}
