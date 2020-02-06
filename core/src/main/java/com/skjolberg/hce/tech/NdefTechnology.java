package com.skjolberg.hce.tech;

import android.nfc.NdefMessage;
import android.os.RemoteException;

public interface NdefTechnology extends TagTechnology {

    boolean isNdef() throws RemoteException;

    NdefMessage ndefRead() throws RemoteException;

    int ndefWrite(NdefMessage msg) throws RemoteException;

    int ndefMakeReadOnly() throws RemoteException;

    boolean ndefIsWritable() throws RemoteException;

    int formatNdef(byte[] key) throws RemoteException;

}
