package com.github.skjolber.nfc.hce.tech.mifare;

import org.ndeftools.Message;

import com.github.skjolber.android.nfc.ErrorCodes;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.os.RemoteException;
import android.util.Log;

import com.github.skjolber.nfc.hce.tech.NdefTechnology;
import com.github.skjolber.nfc.hce.tech.TagTechnology;
import com.github.skjolber.nfc.skjolberg.reader.operations.NdefOperations;

public class NdefFormattableAdapter extends DefaultTechnology implements NdefTechnology {

    private static final String TAG = NdefFormattableAdapter.class.getName();

    private NdefOperations operations;

    public NdefFormattableAdapter(int slotNumber, NdefOperations operations) {
        super(TagTechnology.NDEF_FORMATABLE, slotNumber);
        this.operations = operations;
    }

    @Override
    public boolean isNdef() throws RemoteException {
        try {
            return operations.isFormatted();
        } catch (Exception e) {
            Log.d(TAG, "Problem calling isNdef()", e);
            throw new RemoteException();
        }
    }

    @Override
    public NdefMessage ndefRead() throws RemoteException {
        try {
            Message message = operations.readNdefMessage();

            return message.getNdefMessage();
        } catch (FormatException e) {
            Log.d(TAG, "Problem calling ndefRead()", e);
            throw new RemoteException();
        }
    }

    @Override
    public int ndefWrite(NdefMessage msg) throws RemoteException {
        try {
            operations.writeNdefMessage(new Message(msg));

            return ErrorCodes.SUCCESS;
        } catch (FormatException e) {
            Log.d(TAG, "Problem calling ndefWrite()", e);
            throw new RemoteException();
        }
    }

    @Override
    public int ndefMakeReadOnly() throws RemoteException {
        try {
            operations.makeReadOnly();

            return ErrorCodes.SUCCESS;
        } catch (Exception e) {
            Log.d(TAG, "Problem calling ndefMakeReadOnly()", e);
            return ErrorCodes.ERROR_IO;
        }
    }

    @Override
    public boolean ndefIsWritable() throws RemoteException {
        try {
            return operations.isWritable();
        } catch (Exception e) {
            Log.d(TAG, "Problem calling ndefIsWritable()", e);
            throw new RemoteException();
        }
    }

    @Override
    public int formatNdef(byte[] key) throws RemoteException {
        try {
            operations.format();

            return ErrorCodes.SUCCESS;
        } catch (Exception e) {
            Log.d(TAG, "Problem calling ndefMakeReadOnly()", e);
            return ErrorCodes.ERROR_IO;
        }
    }

}
