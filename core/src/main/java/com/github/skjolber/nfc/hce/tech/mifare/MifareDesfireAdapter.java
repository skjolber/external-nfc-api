package com.github.skjolber.nfc.hce.tech.mifare;

import com.github.skjolber.android.nfc.TransceiveResult;
import android.nfc.tech.IsoDep;
import android.os.RemoteException;
import android.util.Log;

import com.acs.smartcard.ReaderException;
import com.github.skjolber.nfc.hce.tech.CommandTechnology;
import com.github.skjolber.nfc.hce.tech.TagTechnology;
import com.github.skjolber.nfc.service.IsoDepWrapper;

public class MifareDesfireAdapter extends DefaultTechnology implements CommandTechnology {

    protected static final String TAG = MifareDesfireAdapter.class.getName();

    private DESFireAdapter adapter;
    private boolean hostCardEmulation;

    public MifareDesfireAdapter(int slotNumber, IsoDepWrapper isoDep, boolean hostCardEmulation) {
        super(TagTechnology.ISO_DEP, slotNumber);
        this.adapter = new DESFireAdapter(isoDep, false);
        this.hostCardEmulation = hostCardEmulation;
    }

    public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {

        try {
            byte[] transceive;
            if (hostCardEmulation && data[0] == 0x00) {
                //log("Transceive iso request " + ACRCommands.toHexString(data));

                transceive = adapter.transceive(data);

                //log("Transceive iso response " + ACRCommands.toHexString(transceive));
            } else if (raw) {
                //log("Transceive raw request " + ACRCommands.toHexString(data));

                transceive = adapter.transmitRaw(data);

                //log("Transceive raw response " + ACRCommands.toHexString(transceive));
            } else {
                //log("Transceive request " + ACRCommands.toHexString(data));

                transceive = adapter.transceive(data);

                //log("Transceive response " + ACRCommands.toHexString(transceive));
            }

            return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
        } catch (ReaderException e) {
            Log.d(TAG, "Problem sending command", e);

            return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
        }

    }

    private void log(String string) {
        //Log.d(TAG, string);
    }

    public String toString() {
        return IsoDep.class.getSimpleName();
    }
}
