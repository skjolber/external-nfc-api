package com.skjolberg.nfc.external.stub;

import android.nfc.INfcTag;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcel;

import java.lang.reflect.Constructor;

/**
 * File for alternative restore of an INfcTag. For devices which stub have been removed from the Android runtime.
 */

public class NfcTagHelper {

    public static Tag convert(Tag tag) {

        Parcel in = Parcel.obtain();

        tag.writeToParcel(in, 0);
        // read the parcel, but restore the strong binding using local class

        in.setDataPosition(0);

        // copied from tag creator

        INfcTag tagService;

        // Tag fields
        byte[] id = readBytesWithNull(in);
        int[] techList = new int[in.readInt()];
        in.readIntArray(techList);
        Bundle[] techExtras = in.createTypedArray(Bundle.CREATOR);
        int serviceHandle = in.readInt();
        int isMock = in.readInt();
        if (isMock == 0) {
            tagService = NfcTagHelper.Stub.asInterface(in.readStrongBinder());
        }
        else {
            tagService = null;
        }

        return createTag(id, techList, techExtras, serviceHandle, tagService);
    }

    private static Tag createTag(byte[] id, int[] techList, Bundle[] bundles, int serviceHandle, Object tagService) {
        Constructor<?>[] constructors = Tag.class.getConstructors();

        if(id == null) {
            id = new byte[]{};
        }
        try {
            return (Tag) constructors[0].newInstance(id, techList, bundles, serviceHandle, tagService);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static byte[] readBytesWithNull(Parcel in) {
        int len = in.readInt();
        byte[] result = null;
        if (len >= 0) {
            result = new byte[len];
            in.readByteArray(result);
        }
        return result;
    }


        /** Local-side IPC implementation stub class. */
        public static abstract class Stub extends android.nfc.INfcTag.Stub {
            private static final String DESCRIPTOR = "android.nfc.INfcTag";
            /** Construct the stub at attach it to the interface. */
            public Stub()
            {
                this.attachInterface(this, DESCRIPTOR);
            }
            /**
             * Cast an IBinder object into an android.nfc.INfcTag interface,
             * generating a proxy if needed.
             */
            public static android.nfc.INfcTag asInterface(android.os.IBinder obj)
            {
                if ((obj==null)) {
                    return null;
                }
                android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
                if (((iin!=null)&&(iin instanceof android.nfc.INfcTag))) {
                    return ((android.nfc.INfcTag)iin);
                }
                return new Proxy(obj);
            }
            @Override public android.os.IBinder asBinder()
            {
                return this;
            }
            @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
            {
                switch (code)
                {
                    case INTERFACE_TRANSACTION:
                    {
                        reply.writeString(DESCRIPTOR);
                        return true;
                    }
                    case TRANSACTION_close:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        int _result = this.close(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    }
                    case TRANSACTION_connect:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        int _arg1;
                        _arg1 = data.readInt();
                        int _result = this.connect(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    }
                    case TRANSACTION_reconnect:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        int _result = this.reconnect(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    }
                    case TRANSACTION_getTechList:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        int[] _result = this.getTechList(_arg0);
                        reply.writeNoException();
                        reply.writeIntArray(_result);
                        return true;
                    }
                    case TRANSACTION_isNdef:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        boolean _result = this.isNdef(_arg0);
                        reply.writeNoException();
                        reply.writeInt(((_result)?(1):(0)));
                        return true;
                    }
                    case TRANSACTION_isPresent:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        boolean _result = this.isPresent(_arg0);
                        reply.writeNoException();
                        reply.writeInt(((_result)?(1):(0)));
                        return true;
                    }
                    case TRANSACTION_transceive:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        byte[] _arg1;
                        _arg1 = data.createByteArray();
                        boolean _arg2;
                        _arg2 = (0!=data.readInt());
                        android.nfc.TransceiveResult _result = this.transceive(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        if ((_result!=null)) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                        }
                        else {
                            reply.writeInt(0);
                        }
                        return true;
                    }
                    case TRANSACTION_ndefRead:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        android.nfc.NdefMessage _result = this.ndefRead(_arg0);
                        reply.writeNoException();
                        if ((_result!=null)) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                        }
                        else {
                            reply.writeInt(0);
                        }
                        return true;
                    }
                    case TRANSACTION_ndefWrite:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        android.nfc.NdefMessage _arg1;
                        if ((0!=data.readInt())) {
                            _arg1 = android.nfc.NdefMessage.CREATOR.createFromParcel(data);
                        }
                        else {
                            _arg1 = null;
                        }
                        int _result = this.ndefWrite(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    }
                    case TRANSACTION_ndefMakeReadOnly:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        int _result = this.ndefMakeReadOnly(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    }
                    case TRANSACTION_ndefIsWritable:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        boolean _result = this.ndefIsWritable(_arg0);
                        reply.writeNoException();
                        reply.writeInt(((_result)?(1):(0)));
                        return true;
                    }
                    case TRANSACTION_formatNdef:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        byte[] _arg1;
                        _arg1 = data.createByteArray();
                        int _result = this.formatNdef(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    }
                    case TRANSACTION_rediscover:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        android.nfc.Tag _result = this.rediscover(_arg0);
                        reply.writeNoException();
                        if ((_result!=null)) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                        }
                        else {
                            reply.writeInt(0);
                        }
                        return true;
                    }
                    case TRANSACTION_setTimeout:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        int _arg1;
                        _arg1 = data.readInt();
                        int _result = this.setTimeout(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    }
                    case TRANSACTION_getTimeout:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        int _result = this.getTimeout(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    }
                    case TRANSACTION_resetTimeouts:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        this.resetTimeouts();
                        reply.writeNoException();
                        return true;
                    }
                    case TRANSACTION_canMakeReadOnly:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        boolean _result = this.canMakeReadOnly(_arg0);
                        reply.writeNoException();
                        reply.writeInt(((_result)?(1):(0)));
                        return true;
                    }
                    case TRANSACTION_getMaxTransceiveLength:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0;
                        _arg0 = data.readInt();
                        int _result = this.getMaxTransceiveLength(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    }
                    case TRANSACTION_getExtendedLengthApdusSupported:
                    {
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result = this.getExtendedLengthApdusSupported();
                        reply.writeNoException();
                        reply.writeInt(((_result)?(1):(0)));
                        return true;
                    }
                }
                return super.onTransact(code, data, reply, flags);
            }
            private static class Proxy implements android.nfc.INfcTag
            {
                private android.os.IBinder mRemote;
                Proxy(android.os.IBinder remote)
                {
                    mRemote = remote;
                }
                @Override public android.os.IBinder asBinder()
                {
                    return mRemote;
                }
                public String getInterfaceDescriptor()
                {
                    return DESCRIPTOR;
                }
                @Override public int close(int nativeHandle) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    int _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativeHandle);
                        mRemote.transact(TRANSACTION_close, _data, _reply, 0);
                        _reply.readException();
                        _result = _reply.readInt();
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public int connect(int nativeHandle, int technology) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    int _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativeHandle);
                        _data.writeInt(technology);
                        mRemote.transact(TRANSACTION_connect, _data, _reply, 0);
                        _reply.readException();
                        _result = _reply.readInt();
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public int reconnect(int nativeHandle) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    int _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativeHandle);
                        mRemote.transact(TRANSACTION_reconnect, _data, _reply, 0);
                        _reply.readException();
                        _result = _reply.readInt();
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public int[] getTechList(int nativeHandle) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    int[] _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativeHandle);
                        mRemote.transact(TRANSACTION_getTechList, _data, _reply, 0);
                        _reply.readException();
                        _result = _reply.createIntArray();
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public boolean isNdef(int nativeHandle) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    boolean _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativeHandle);
                        mRemote.transact(TRANSACTION_isNdef, _data, _reply, 0);
                        _reply.readException();
                        _result = (0!=_reply.readInt());
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public boolean isPresent(int nativeHandle) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    boolean _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativeHandle);
                        mRemote.transact(TRANSACTION_isPresent, _data, _reply, 0);
                        _reply.readException();
                        _result = (0!=_reply.readInt());
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public android.nfc.TransceiveResult transceive(int nativeHandle, byte[] data, boolean raw) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    android.nfc.TransceiveResult _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativeHandle);
                        _data.writeByteArray(data);
                        _data.writeInt(((raw)?(1):(0)));
                        mRemote.transact(TRANSACTION_transceive, _data, _reply, 0);
                        _reply.readException();
                        if ((0!=_reply.readInt())) {
                            _result = android.nfc.TransceiveResult.CREATOR.createFromParcel(_reply);
                        }
                        else {
                            _result = null;
                        }
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public android.nfc.NdefMessage ndefRead(int nativeHandle) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    android.nfc.NdefMessage _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativeHandle);
                        mRemote.transact(TRANSACTION_ndefRead, _data, _reply, 0);
                        _reply.readException();
                        if ((0!=_reply.readInt())) {
                            _result = android.nfc.NdefMessage.CREATOR.createFromParcel(_reply);
                        }
                        else {
                            _result = null;
                        }
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public int ndefWrite(int nativeHandle, android.nfc.NdefMessage msg) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    int _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativeHandle);
                        if ((msg!=null)) {
                            _data.writeInt(1);
                            msg.writeToParcel(_data, 0);
                        }
                        else {
                            _data.writeInt(0);
                        }
                        mRemote.transact(TRANSACTION_ndefWrite, _data, _reply, 0);
                        _reply.readException();
                        _result = _reply.readInt();
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public int ndefMakeReadOnly(int nativeHandle) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    int _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativeHandle);
                        mRemote.transact(TRANSACTION_ndefMakeReadOnly, _data, _reply, 0);
                        _reply.readException();
                        _result = _reply.readInt();
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public boolean ndefIsWritable(int nativeHandle) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    boolean _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativeHandle);
                        mRemote.transact(TRANSACTION_ndefIsWritable, _data, _reply, 0);
                        _reply.readException();
                        _result = (0!=_reply.readInt());
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public int formatNdef(int nativeHandle, byte[] key) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    int _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativeHandle);
                        _data.writeByteArray(key);
                        mRemote.transact(TRANSACTION_formatNdef, _data, _reply, 0);
                        _reply.readException();
                        _result = _reply.readInt();
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public android.nfc.Tag rediscover(int nativehandle) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    android.nfc.Tag _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(nativehandle);
                        mRemote.transact(TRANSACTION_rediscover, _data, _reply, 0);
                        _reply.readException();
                        if ((0!=_reply.readInt())) {
                            _result = android.nfc.Tag.CREATOR.createFromParcel(_reply);
                        }
                        else {
                            _result = null;
                        }
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public int setTimeout(int technology, int timeout) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    int _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(technology);
                        _data.writeInt(timeout);
                        mRemote.transact(TRANSACTION_setTimeout, _data, _reply, 0);
                        _reply.readException();
                        _result = _reply.readInt();
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public int getTimeout(int technology) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    int _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(technology);
                        mRemote.transact(TRANSACTION_getTimeout, _data, _reply, 0);
                        _reply.readException();
                        _result = _reply.readInt();
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public void resetTimeouts() throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        mRemote.transact(TRANSACTION_resetTimeouts, _data, _reply, 0);
                        _reply.readException();
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                }
                @Override public boolean canMakeReadOnly(int ndefType) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    boolean _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(ndefType);
                        mRemote.transact(TRANSACTION_canMakeReadOnly, _data, _reply, 0);
                        _reply.readException();
                        _result = (0!=_reply.readInt());
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public int getMaxTransceiveLength(int technology) throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    int _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        _data.writeInt(technology);
                        mRemote.transact(TRANSACTION_getMaxTransceiveLength, _data, _reply, 0);
                        _reply.readException();
                        _result = _reply.readInt();
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
                @Override public boolean getExtendedLengthApdusSupported() throws android.os.RemoteException
                {
                    android.os.Parcel _data = android.os.Parcel.obtain();
                    android.os.Parcel _reply = android.os.Parcel.obtain();
                    boolean _result;
                    try {
                        _data.writeInterfaceToken(DESCRIPTOR);
                        mRemote.transact(TRANSACTION_getExtendedLengthApdusSupported, _data, _reply, 0);
                        _reply.readException();
                        _result = (0!=_reply.readInt());
                    }
                    finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                    return _result;
                }
            }
            static final int TRANSACTION_close = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
            static final int TRANSACTION_connect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
            static final int TRANSACTION_reconnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
            static final int TRANSACTION_getTechList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
            static final int TRANSACTION_isNdef = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
            static final int TRANSACTION_isPresent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
            static final int TRANSACTION_transceive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
            static final int TRANSACTION_ndefRead = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
            static final int TRANSACTION_ndefWrite = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
            static final int TRANSACTION_ndefMakeReadOnly = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
            static final int TRANSACTION_ndefIsWritable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
            static final int TRANSACTION_formatNdef = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
            static final int TRANSACTION_rediscover = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
            static final int TRANSACTION_setTimeout = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
            static final int TRANSACTION_getTimeout = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
            static final int TRANSACTION_resetTimeouts = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
            static final int TRANSACTION_canMakeReadOnly = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
            static final int TRANSACTION_getMaxTransceiveLength = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
            static final int TRANSACTION_getExtendedLengthApdusSupported = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
        }

}
