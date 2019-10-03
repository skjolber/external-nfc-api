package com.skjolberg.service.utils;

import android.util.Log;

import com.acs.bluetooth.BluetoothReader;
import com.acs.smartcard.ReaderException;
import com.skjolberg.nfc.command.PassthroughCommandException;
import com.skjolberg.nfc.command.ReaderCommandException;
import com.skjolberg.nfc.command.ReaderWrapper;
import com.skjolberg.nfc.command.Utils;

import org.nfctools.NfcException;
import org.nfctools.api.ApduTag;
import org.nfctools.api.Tag;
import org.nfctools.api.TagType;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;
import org.nfctools.scio.Command;
import org.nfctools.scio.Response;
import org.nfctools.spi.acs.Apdu;

import custom.java.CommandAPDU;
import custom.java.ResponseAPDU;

/**
 * Created by skjolber on 10.08.17.
 */

import org.nfctools.NfcException;
import org.nfctools.api.ApduTag;
import org.nfctools.api.Tag;
import org.nfctools.api.TagType;
import org.nfctools.scio.Command;
import org.nfctools.scio.Response;

import android.util.Log;

import com.skjolberg.nfc.command.ReaderWrapper;
import com.skjolberg.nfc.command.Utils;
import com.skjolberg.service.BluetoothBackgroundService;

import java.util.concurrent.CountDownLatch;

import custom.java.CommandAPDU;
import custom.java.ResponseAPDU;


public class BluetoothAcsTag extends Tag implements ApduTag, BluetoothReader.OnResponseApduAvailableListener {

    private static final String TAG = BluetoothAcsTag.class.getName();

    private BluetoothReader reader;

    private volatile CountDownLatch latch;
    private byte[] in;

    public BluetoothAcsTag(TagType tagType, byte[] generalBytes, BluetoothReader mBluetoothReader) {
        super(tagType, generalBytes);
        this.reader = mBluetoothReader;
    }

    public BluetoothReader getReader() {
        return reader;
    }


    public void setReader(BluetoothReader mBluetoothReader) {
        this.reader = reader;
    }

    @Override
    public synchronized Response transmit(Command command) {
        try {
            CommandAPDU commandAPDU = null;
            if (command.isDataOnly()) {
                commandAPDU = new CommandAPDU(0xff, 0, 0, 0, command.getData(), command.getOffset(),
                        command.getLength());
            } else if (command.hasData()) {
                commandAPDU = new CommandAPDU(Apdu.CLS_PTS, command.getInstruction(), command.getP1(), command.getP2(),
                        command.getData());
            } else {
                commandAPDU = new CommandAPDU(Apdu.CLS_PTS, command.getInstruction(), command.getP1(), command.getP2(), command.getLength());
            }
            byte[] out = commandAPDU.getBytes();

            // Log.d(TAG, "Request: " + Utils.toHexString(out));

            in = null;
            latch = new CountDownLatch(1);

            reader.setOnResponseApduAvailableListener(this);
            if (!reader.transmitApdu(out)) {
                throw new NfcException("Unable to transmit ADPU");
            }

            // Log.d(TAG, "ADPU transmitted");

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new NfcException("Problem waiting for response");
            }

            if (in == null) {
                throw new NfcException("No response");
            }

            // Log.d(TAG, "Response: " + Utils.toHexString(in));

            ResponseAPDU responseAPDU = new ResponseAPDU(in);
            return new Response(responseAPDU.getSW1(), responseAPDU.getSW2(), responseAPDU.getData());
        } catch (Exception e) {
            throw new NfcException(e);
        }
    }


    @Override
    public synchronized byte[] transmit(byte[] request) {
        // Log.d(TAG, "Raw request: " + Utils.toHexString(request));
        try {

            in = null;
            latch = new CountDownLatch(1);

            reader.setOnResponseApduAvailableListener(this);
            if (!reader.transmitApdu(request)) {
                throw new NfcException("Unable to transmit ADPU");
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new NfcException("Problem waiting for response");
            }

            // Log.d(TAG, "Raw response: " + Utils.toHexString(in));

            return in;
        } catch (Exception e) {
            throw new NfcException(e);
        }
    }

    @Override
    public void onResponseApduAvailable(BluetoothReader bluetoothReader, byte[] apdu, int errorCode) {
        // Log.d(TAG, "onResponseApduAvailable: " + BluetoothBackgroundService.getResponseString(apdu, errorCode));

        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            this.in = apdu;

        }
        latch.countDown();
    }


    public byte[] transmitPassthrough(byte[] req) {
        byte[] sub = new byte[2 + req.length];
        // 0xD4 magic byte
        // 0x42 InCommunicateThru from PN532

        sub[0] = (byte) 0xD4;
        sub[1] = (byte) 0x42;

        System.arraycopy(req, 0, sub, 2, req.length);
        // 0xD4 magic byte
        // 0x42 InCommunicateThru from PN532

        CommandAPDU command = new CommandAPDU(0xFF, 0x00, 0x00, 0x00, sub, 0, sub.length);

        byte[] responseBytes = transmit(command.getBytes());

        ResponseAPDU response = new ResponseAPDU(responseBytes);

        NfcNtagVersion version = null;

        if (!response.isSuccess()) {
            throw new ReaderCommandException("Unable to issue command " + Utils.toHexString(sub) + ", response " + Utils.toHexString(responseBytes));
        }
        byte[] data = response.getData();

        //if(LOG) log("Status " + (0xFF & data[2]));

        if ((data[2] & 0xFF) != 0) {
            throw new PassthroughCommandException("Got command error", (data[2] & 0xFF));
        }

        byte[] content = new byte[data.length - 3];
        System.arraycopy(data, 3, content, 0, content.length);

        return content;
    }


}