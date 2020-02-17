package com.github.skjolber.nfc.hce.tech.mifare;

import java.io.ByteArrayOutputStream;

import android.util.Log;

import com.acs.smartcard.ReaderException;
import com.github.skjolber.nfc.service.IsoDepWrapper;

public class DESFireAdapter {

    private static final String TAG = DESFireAdapter.class.getName();

    /* Status codes */
    public static final byte OPERATION_OK = (byte) 0x00;
    public static final byte ADDITIONAL_FRAME = (byte) 0xAF;
    public static final byte STATUS_OK = (byte) 0x91;

    public static final int MAX_CAPDU_SIZE = 55;
    public static final int MAX_RAPDU_SIZE = 60;

    private IsoDepWrapper isoDep;
    private boolean print;

    public DESFireAdapter(IsoDepWrapper isoDep, boolean print) {
        this.isoDep = isoDep;
        this.print = print;
    }

    public IsoDepWrapper getIsoDep() {
        return isoDep;
    }

    /**
     * Send compressed command message
     *
     * @param message
     * @return
     * @throws Exception
     */

    public byte[] transmitRaw(byte[] adpu) throws ReaderException {
        return responseADPUToRaw(rawToRequestADPU(adpu));
    }

    public static byte[] responseADPUToRaw(byte[] response) throws ReaderException {

        byte[] result = new byte[response.length - 1];
        result[0] = response[response.length - 1];

        System.arraycopy(response, 0, result, 1, response.length - 2);

        return result;
    }

    public byte[] rawToRequestADPU(byte[] commandMessage) throws ReaderException {
        return transceive(wrapMessage(commandMessage[0], commandMessage, 1, commandMessage.length - 1));
    }

    public static byte[] wrapMessage(byte command) throws ReaderException {
        return new byte[]{(byte) 0x90, command, 0x00, 0x00, 0x00};
    }

    private static byte[] wrapCommand(byte command, byte[] parameters) {
        return wrapCommand(command, parameters, 0, parameters.length);
    }

    private static byte[] wrapCommand(byte command, byte[] parameters, int offset, int length) {
        byte[] stream;
        if (parameters != null && length > 0) {
            stream = new byte[5 + 1 + length];
        } else {
            stream = new byte[5];
        }
        stream[0] = (byte) 0x90;
        stream[1] = command;

        if (parameters != null && parameters.length > 0) {
            stream[4] = (byte) length;
            System.arraycopy(parameters, offset, stream, 5, length);
        }

        return stream;
    }

    public static byte[] wrapMessage(byte command, byte[] parameters, int offset, int length) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        stream.write((byte) 0x90);
        stream.write(command);
        stream.write((byte) 0x00);
        stream.write((byte) 0x00);
        if (parameters != null && length > 0) {
            // actually no length if empty length
            stream.write(length);
            stream.write(parameters, offset, length);
        }
        stream.write((byte) 0x00);

        return stream.toByteArray();
    }

    /**
     * Send a command to the card and return the response.
     *
     * <p>{@linkplain #connect()} should be called beforehand.
     *
     * @param command the command
     * @throws ReaderException
     * @return the PICC response
     */
    public byte[] transceive(byte[] command) throws ReaderException {

        if (print) {
            Log.d(TAG, "===> " + getHexString(command, true) + " (" + command.length + ")");
        }

        byte[] response = isoDep.transceive(command);

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

    public byte[] transmitChain(byte[] adpu) throws ReaderException {
        return receieveResponseChain(sendRequestChain(adpu));
    }

    public byte[] receieveResponseChain(byte[] response) throws ReaderException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        output.write(0x00);
        do {

            output.write(response, 0, response.length - 2);

            if (response[response.length - 1] != ADDITIONAL_FRAME) {
                byte[] result = output.toByteArray();

                result[0] = response[response.length - 1];

                return result;
            }

            response = transceive(wrapMessage(ADDITIONAL_FRAME));
        } while (true);
    }

    public byte[] sendRequestChain(byte[] commandMessage) throws ReaderException {

        int offset = 1; // data area of apdu

        byte nextCommand = commandMessage[0];
        while (true) {
            int nextLength = Math.min(MAX_CAPDU_SIZE - 1, commandMessage.length - offset);

            byte[] request = wrapMessage(nextCommand, commandMessage, offset, nextLength);

            byte[] response = transceive(request);
            if (response[response.length - 2] != STATUS_OK) {
                return response;
            }

            offset += nextLength;
            if (offset == commandMessage.length) {
                return response;
            }

            if (response.length != 2) {
                throw new IllegalArgumentException("Expected empty response payload while transmitting request");
            }
            byte status = response[response.length - 1];
            if (status != ADDITIONAL_FRAME) {
                throw new ReaderException("PICC error code: " + Integer.toHexString(status & 0xFF));
            }
            nextCommand = ADDITIONAL_FRAME;
        }

    }

}
