package com.skjolberg.hce;

import android.nfc.ErrorCodes;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.TransceiveResult;
import android.os.RemoteException;
import android.util.Log;

import com.skjolberg.hce.resolve.TagProxy;
import com.skjolberg.hce.tech.CommandTechnology;
import com.skjolberg.hce.tech.NdefTechnology;
import com.skjolberg.hce.tech.ReaderTechnology;
import com.skjolberg.hce.tech.TagTechnology;

import java.util.List;


public interface INFcTagBinder {

    public TagTechnology getConnected(int nativeHandle);

    public void setReaderTechnology(ReaderTechnology readerTechnology);

    public boolean canMakeReadOnly(int ndefType) throws RemoteException;

    public int close(int nativeHandle) throws RemoteException;

    public int connect(int serviceHandle, int technology) throws RemoteException;

    public int formatNdef(int nativeHandle, byte[] key) throws RemoteException;

    public boolean getExtendedLengthApdusSupported() throws RemoteException;

    public int getMaxTransceiveLength(int technology) throws RemoteException;

    public int[] getTechList(int nativeHandle) throws RemoteException;

    public int getTimeout(int technology) throws RemoteException;

    public boolean isNdef(int nativeHandle) throws RemoteException;

    public boolean isPresent(int nativeHandle) throws RemoteException;

    public boolean ndefIsWritable(int nativeHandle) throws RemoteException;

    public int ndefMakeReadOnly(int nativeHandle) throws RemoteException;

    public NdefMessage ndefRead(int nativeHandle) throws RemoteException;

    public int ndefWrite(int nativeHandle, NdefMessage msg) throws RemoteException;

    public int reconnect(int nativehandle) throws RemoteException;

    public Tag rediscover(int nativehandle) throws RemoteException;

    public void resetTimeouts() throws RemoteException;

    public int setTimeout(int technology, int timeout) throws RemoteException;

    public TransceiveResult transceive(int nativeHandle, byte[] data, boolean raw) throws RemoteException;

}
