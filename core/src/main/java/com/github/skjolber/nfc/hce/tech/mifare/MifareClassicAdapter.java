package com.github.skjolber.nfc.hce.tech.mifare;

import java.io.IOException;

import org.nfctools.mf.block.DataBlock;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.classic.Key;
import org.nfctools.mf.classic.KeyValue;
import org.nfctools.mf.classic.MfClassicAccess;
import org.nfctools.mf.classic.MfClassicReaderWriter;

import com.github.skjolber.android.nfc.TransceiveResult;
import android.nfc.tech.MifareClassic;
import android.os.RemoteException;
import android.util.Log;

import com.github.skjolber.nfc.hce.tech.CommandTechnology;
import com.github.skjolber.nfc.hce.tech.TagTechnology;

public class MifareClassicAdapter extends DefaultTechnology implements CommandTechnology {

    /**
     * Size of a MIFARE Ultralight page in bytes
     */
    public static final int PAGE_SIZE = 4;

    protected static final String TAG = MifareClassicAdapter.class.getName();

    private MfClassicReaderWriter readerWriter;

    private KeyValue keyValue;

    public MifareClassicAdapter(int slotNumber, MfClassicReaderWriter readerWriter) {
        super(TagTechnology.MIFARE_CLASSIC, slotNumber);
        this.readerWriter = readerWriter;
    }

    public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {
        // Log.d(TAG, "transceive");

        int command = data[0] & 0xFF;
        if (command == 0x60 || command == 0x61) {
            int block = data[1] & 0xFF;

            byte[] key = new byte[6];
            System.arraycopy(data, data.length - key.length, key, 0, key.length);

            keyValue = new KeyValue(command == 0x60 ? Key.A : Key.B, key);

            try {
                readerWriter.loginIntoSector(new MfClassicAccess(keyValue, blockToSector(block), block));

                return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, null);
            } catch (IOException e) {
                Log.d(TAG, "Problem reading block " + block, e);

                return null;
            }
        } else if (command == 0x30) {
            int block = data[1] & 0xFF;

            try {
                MfBlock[] readBlock = readerWriter.readBlock(new MfClassicAccess(keyValue, blockToSector(block), block));

                return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, readBlock[0].getData());
            } catch (IOException e) {
                Log.d(TAG, "Problem reading block " + block, e);

                return null;
            }
        } else if (command == 0xA0) {
            int block = data[1] & 0xFF;

            byte[] payload = new byte[12];
            System.arraycopy(data, data.length - payload.length, payload, 0, payload.length);

            try {
                readerWriter.writeBlock(new MfClassicAccess(keyValue, blockToSector(block), block), new DataBlock(payload));

                return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, null);
            } catch (IOException e) {
                Log.d(TAG, "Problem reading block " + block, e);

                return null;
            }
        } else {
            return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
        }
    }

    public int blockToSector(int blockIndex) {
        if (blockIndex < 32 * 4) {
            return blockIndex / 4;
        } else {
            return 32 + (blockIndex - 32 * 4) / 16;
        }
    }

    public String toString() {
        return MifareClassic.class.getSimpleName();
    }

}
