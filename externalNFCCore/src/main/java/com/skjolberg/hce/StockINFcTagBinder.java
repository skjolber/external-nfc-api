package com.skjolberg.hce;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

import android.nfc.ErrorCodes;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.TransceiveResult;
import android.os.RemoteException;
import android.util.Log;

import com.skjolberg.hce.resolve.TagProxy;
import com.skjolberg.hce.resolve.TagProxyStore;
import com.skjolberg.hce.tech.CommandTechnology;
import com.skjolberg.hce.tech.NdefTechnology;
import com.skjolberg.hce.tech.ReaderTechnology;
import com.skjolberg.hce.tech.TagTechnology;
import com.skjolberg.nfc.acs.AcrReader;
import com.skjolberg.nfc.command.ACRCommands;

public class StockINFcTagBinder extends android.nfc.INfcTag.Stub implements INFcTagBinder {

    private static final String TAG = StockINFcTagBinder.class.getName();

    protected TagProxyStore store;

    protected ReaderTechnology readerTechnology;

    public StockINFcTagBinder(TagProxyStore store) {
        this.store = store;
    }

    public TagTechnology getConnected(int nativeHandle) {
        TagProxy proxy = store.get(nativeHandle);

        if (proxy == null) {
            return null;
        }

        return proxy.getCurrent();
    }

    public void setReaderTechnology(ReaderTechnology readerTechnology) {
        this.readerTechnology = readerTechnology;
    }

    public boolean canMakeReadOnly(int ndefType) throws RemoteException {
        //Log.d(TAG, "canMakeReadOnly");

        return readerTechnology.canMakeReadOnly(ndefType);
    }

    public int close(int nativeHandle) throws RemoteException {
        //Log.d(TAG, "close");

        TagProxy proxy = store.get(nativeHandle);

        if (proxy == null) {
            return ErrorCodes.ERROR_DISCONNECT;
        }

        TagTechnology current = proxy.getCurrent();
        if (current != null) {
            proxy.setCurrent(null);
        }

        return ErrorCodes.ERROR_DISCONNECT;
    }

    public int connect(int serviceHandle, int technology) throws RemoteException {
        //Log.d(TAG, "connect");

        TagProxy proxy = store.get(serviceHandle);

        if (proxy == null) {
            Log.d(TAG, "No proxy for " + serviceHandle);

            return ErrorCodes.ERROR_CONNECT;
        }

        if (!proxy.selectTechnology(technology)) {
            Log.d(TAG, "No technology " + technology + " for " + serviceHandle + ": " + proxy.getTechnologies());

            return ErrorCodes.ERROR_NOT_SUPPORTED;
        }

        return ErrorCodes.SUCCESS;
    }

    public int formatNdef(int nativeHandle, byte[] key) throws RemoteException {
        //Log.d(TAG, "formatNdef");
        TagTechnology adapter = getConnected(nativeHandle);
        if (adapter != null) {
            if (adapter instanceof NdefTechnology) {
                NdefTechnology ndefTechnology = (NdefTechnology) adapter;
                return ndefTechnology.formatNdef(key);
            }
        }

        return ErrorCodes.ERROR_IO;
    }


    public boolean getExtendedLengthApdusSupported() throws RemoteException {
        //Log.d(TAG, "getExtendedLengthApdusSupported");

        if (readerTechnology == null) {
            throw new RemoteException("No reader");
        }

        return readerTechnology.getExtendedLengthApdusSupported();
    }


    public int getMaxTransceiveLength(int technology) throws RemoteException {
        //Log.d(TAG, "getMaxTransceiveLength");

        if (readerTechnology == null) {
            throw new RemoteException("No reader");
        }

        return readerTechnology.getMaxTransceiveLength(technology);
    }


    public int[] getTechList(int nativeHandle) throws RemoteException {
        //Log.d(TAG, "getTechList");

        TagProxy proxy = store.get(nativeHandle);

        if (proxy != null) {
            List<TagTechnology> technologies = proxy.getTechnologies();

            int[] techList = new int[technologies.size()];

            for (int i = 0; i < techList.length; i++) {
                techList[i] = technologies.get(i).getTagTechnology();
            }

            return techList;
        }

        throw new RemoteException("No proxy for " + nativeHandle);
    }


    public int getTimeout(int technology) throws RemoteException {
        //Log.d(TAG, "getTimeout");

        if (readerTechnology == null) {
            throw new RemoteException("No reader");
        }

        return readerTechnology.getTimeout(technology);
    }


    public boolean isNdef(int nativeHandle) throws RemoteException {
        //Log.d(TAG, "isNdef");
        TagTechnology adapter = getConnected(nativeHandle);
        if (adapter != null) {
            if (adapter instanceof NdefTechnology) {
                NdefTechnology ndefTechnology = (NdefTechnology) adapter;
                return ndefTechnology.isNdef();
            } else {
                throw new RemoteException("Tag technology " + adapter.getClass().getName() + " does not support isNdef(..)");
            }
        }

        return false;
    }


    public boolean isPresent(int nativeHandle) throws RemoteException {
        //Log.d(TAG, "isPresent");
        TagProxy proxy = store.get(nativeHandle);

        if (proxy == null) {
            throw new RemoteException();
        }

        return proxy.isPresent();
    }


    public boolean ndefIsWritable(int nativeHandle) throws RemoteException {
        //Log.d(TAG, "ndefIsWritable");
        TagTechnology adapter = getConnected(nativeHandle);
        if (adapter != null) {
            if (adapter instanceof NdefTechnology) {
                NdefTechnology ndefTechnology = (NdefTechnology) adapter;
                return ndefTechnology.ndefIsWritable();
            } else {
                throw new RemoteException("Tag technology " + adapter.getClass().getName() + " does not support ndefIsWritable(..)");
            }
        }

        return false;
    }


    public int ndefMakeReadOnly(int nativeHandle) throws RemoteException {
        //Log.d(TAG, "ndefMakeReadOnly");
        TagTechnology adapter = getConnected(nativeHandle);
        if (adapter != null) {
            if (adapter instanceof NdefTechnology) {
                NdefTechnology ndefTechnology = (NdefTechnology) adapter;
                return ndefTechnology.ndefMakeReadOnly();
            } else {
                throw new RemoteException("Tag technology " + adapter.getClass().getName() + " does not support ndefMakeReadOnly(..)");
            }
        }

        return ErrorCodes.ERROR_IO;
    }


    public NdefMessage ndefRead(int nativeHandle) throws RemoteException {
        //Log.d(TAG, "ndefRead");

        TagTechnology adapter = getConnected(nativeHandle);
        if (adapter != null) {
            if (adapter instanceof NdefTechnology) {
                NdefTechnology ndefTechnology = (NdefTechnology) adapter;
                return ndefTechnology.ndefRead();
            } else {
                throw new RemoteException("Tag technology " + adapter.getClass().getName() + " does not support ndefRead(..)");
            }
        }

        return null;

    }


    public int ndefWrite(int nativeHandle, NdefMessage msg) throws RemoteException {
        //Log.d(TAG, "ndefWrite");
        TagTechnology adapter = getConnected(nativeHandle);
        if (adapter != null) {
            if (adapter instanceof NdefTechnology) {
                NdefTechnology ndefTechnology = (NdefTechnology) adapter;
                return ndefTechnology.ndefWrite(msg);
            } else {
                throw new RemoteException("Tag technology " + adapter.getClass().getName() + " does not support ndefWrite(..)");
            }
        }
        return ErrorCodes.ERROR_IO;
    }


    public int reconnect(int nativehandle) throws RemoteException {
        //Log.d(TAG, "reconnect");

        if (readerTechnology == null) {
            throw new RemoteException("No reader");
        }

        return readerTechnology.reconnect(nativehandle);
    }


    public Tag rediscover(int nativehandle) throws RemoteException {
        //Log.d(TAG, "rediscover");

        TagProxy proxy = store.get(nativehandle);

        if (proxy == null) {
            throw new RemoteException();
        }

        return proxy.rediscover(this);
    }


    public void resetTimeouts() throws RemoteException {
        //Log.d(TAG, "resetTimeouts");

        if (readerTechnology == null) {
            throw new RemoteException("No reader");
        }

        readerTechnology.resetTimeouts();
    }

    public int setTimeout(int technology, int timeout) throws RemoteException {
        //Log.d(TAG, "setTimeout");

        if (readerTechnology == null) {
            throw new RemoteException("No reader");
        }

        return readerTechnology.setTimeout(technology, timeout);
    }

    public TransceiveResult transceive(int nativeHandle, byte[] data, boolean raw) throws RemoteException {
        //Log.d(TAG, "transceive");
        TagTechnology adapter = getConnected(nativeHandle);
        if (adapter != null) {
            if (adapter instanceof CommandTechnology) {
                CommandTechnology technology = (CommandTechnology) adapter;

                return technology.transceive(data, raw);
            } else {
                throw new RemoteException("Tag technology " + adapter.getClass().getName() + " does not support transceive(..)");
            }
        }

        return new TransceiveResult(TransceiveResult.RESULT_TAGLOST, null);
    }

    private byte[] noReaderException() {


        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(AcrReader.VERSION);
            dout.writeInt(AcrReader.STATUS_EXCEPTION);
            dout.writeUTF("Reader not connected");

            byte[] response = out.toByteArray();

            Log.d(TAG, "Send exception response length " + response.length + ":" + ACRCommands.toHexString(response));

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}