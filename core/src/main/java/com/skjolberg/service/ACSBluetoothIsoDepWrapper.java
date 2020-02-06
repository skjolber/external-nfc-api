package com.skjolberg.service;

import android.util.Log;

import com.acs.bluetooth.BluetoothReader;
import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.skjolberg.nfc.command.*;

import org.nfctools.NfcException;
import org.nfctools.api.TagType;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ACSBluetoothIsoDepWrapper implements IsoDepWrapper, BluetoothReader.OnResponseApduAvailableListener {

    private static final String TAG = ACSBluetoothIsoDepWrapper.class.getName();

    private BluetoothReader reader;

    private volatile CountDownLatch latch;
    private byte[] in;

    public ACSBluetoothIsoDepWrapper(BluetoothReader mBluetoothReader) {
        this.reader = mBluetoothReader;
    }

    public BluetoothReader getReader() {
        return reader;
    }

    public synchronized byte[] transceive(byte[] request) {
        Log.d(TAG, "Raw request: " + com.skjolberg.nfc.command.Utils.toHexString(request));

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

            Log.d(TAG, "Raw response: " + com.skjolberg.nfc.command.Utils.toHexString(in));

            return in;
        } catch (Exception e) {
            throw new NfcException(e);
        }
    }

    @Override
    public synchronized byte[] transmitPassThrough(byte[] req) throws ReaderException {
        throw new ReaderException();
    }

    @Override
    public void onResponseApduAvailable(BluetoothReader bluetoothReader, byte[] apdu, int errorCode) {
        Log.d(TAG, "onResponseApduAvailable: " + BluetoothBackgroundService.getResponseString(apdu, errorCode));

        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            this.in = apdu;

        }
        latch.countDown();
    }

}
