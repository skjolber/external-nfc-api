package com.github.skjolber.nfc.hce.tech.mifare;

import android.nfc.TransceiveResult;
import android.nfc.tech.NfcA;
import android.os.RemoteException;
import android.util.Log;

import com.acs.smartcard.ReaderException;
import com.github.skjolber.nfc.hce.tech.CommandTechnology;
import com.github.skjolber.nfc.hce.tech.TagTechnology;
import com.github.skjolber.nfc.command.ACRCommands;
import com.github.skjolber.nfc.service.ACSIsoDepWrapper;
import com.github.skjolber.nfc.service.IsoDepWrapper;

import java.io.IOException;

public class NfcAAdapter extends DefaultTechnology implements CommandTechnology {

    protected static final String TAG = NfcAAdapter.class.getName();

    private ACSIsoDepWrapper wrapper;
    private boolean print;

    public NfcAAdapter(int slotNumber, IsoDepWrapper wrapper, boolean print) {
        super(TagTechnology.NFC_A, slotNumber);
        this.print = print;
    }

    public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {
        try {
            byte[] transceive;
            if (raw) {
                if (print) {
                    Log.d(TAG, "Transceive raw request " + ACRCommands.toHexString(data));
                }

                transceive = transmitRaw(data);

                if (print) {
                    Log.d(TAG, "Transceive raw response " + ACRCommands.toHexString(transceive));
                }
            } else {
                if (print) {
                    Log.d(TAG, "Transceive request " + ACRCommands.toHexString(data));
                }
                transceive = transceive(data);

                if (print) {
                    Log.d(TAG, "Transceive response " + ACRCommands.toHexString(transceive));
                }
            }

            return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
        } catch (ReaderException e) {
            Log.d(TAG, "Problem sending command", e);

            return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
        }

    }

    public byte[] transmitRaw(byte[] adpu) throws ReaderException {
        return DESFireAdapter.responseADPUToRaw(rawToRequestADPU(adpu));
    }

    public byte[] rawToRequestADPU(byte[] commandMessage) throws ReaderException {
        return transceive(DESFireAdapter.wrapMessage(commandMessage[0], commandMessage, 1, commandMessage.length - 1));
    }

    /**
     * Send a command to the card and return the response.
     *
     * @param command the command
     * @throws IOException
     * @return the PICC response
     */
    public byte[] transceive(byte[] command) throws ReaderException {

        if (print) {
            Log.d(TAG, "===> " + getHexString(command, true) + " (" + command.length + ")");
        }

        byte[] response = wrapper.transceive(command);

        if (print) {
            Log.d(TAG, "<=== " + getHexString(response, true) + " (" + command.length + ")");
        }

        return response;
    }

    public static String getHexString(byte[] a, boolean space) {
        StringBuilder sb = new StringBuilder();
        for (byte b : a) {
            sb.append(String.format("%02x", b & 0xff));
            if (space) {
                sb.append(' ');
            }
        }
        return sb.toString().trim().toUpperCase();
    }

    @Override
    public String toString() {
        return NfcA.class.getSimpleName();
    }
}
