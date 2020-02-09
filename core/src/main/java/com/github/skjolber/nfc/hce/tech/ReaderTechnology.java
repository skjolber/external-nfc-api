package com.github.skjolber.nfc.hce.tech;

import android.os.RemoteException;

public interface ReaderTechnology {

    int setTimeout(int technology, int timeout) throws RemoteException;

    int getTimeout(int technology) throws RemoteException;

    void resetTimeouts() throws RemoteException;

    boolean canMakeReadOnly(int ndefType) throws RemoteException;

    int getMaxTransceiveLength(int technology) throws RemoteException;

    boolean getExtendedLengthApdusSupported() throws RemoteException;

    int reconnect(int handle) throws RemoteException;


}
