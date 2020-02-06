package com.skjolberg.hce;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

public class ACRBroadcast {

    private static final String TAG = ACRBroadcast.class.getName();

    private Context context;

    public ACRBroadcast(Context context) {
        this.context = context;
    }

    public void broadcast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }

    public void broadcast(String action, String key, boolean value) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(key, value);

        sendBroadcast(intent);
    }

    public void broadcast(String action, String key, String value) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(key, value);

        sendBroadcast(intent);
    }

    public void broadcast(String action, String key, byte[] value) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(key, value);

        sendBroadcast(intent);
    }

    public void broadcast(String action, String key, Parcelable[] value) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(key, value);

        sendBroadcast(intent);
    }

    public void onTagNdefDiscovered(int slotNumber, int[] techList, Bundle[] bundles, Boolean writable, byte[] id, NdefMessage messages, Object instance) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String action = NfcAdapter.ACTION_TAG_DISCOVERED;

        final Intent intent = new Intent(action);
        intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, new NdefMessage[]{messages});

        Constructor<?>[] constructors = Tag.class.getConstructors();

        // public Tag(byte[] id, int[] techList, Bundle[] techListExtras, int serviceHandle, INfcTag tagService)
        Parcelable tag = (Parcelable) constructors[0].newInstance(id, techList, bundles, slotNumber, instance);

        intent.putExtra(NfcAdapter.EXTRA_TAG, tag);
        intent.putExtra(NfcAdapter.EXTRA_ID, id);

        Log.d(TAG, "Constructed " + tag);

        sendBroadcast(intent);
    }


    private void sendBroadcast(Intent intent) {
        Log.d(TAG, "Broadcast " + intent.getAction());

        context.sendBroadcast(intent);
    }
}
