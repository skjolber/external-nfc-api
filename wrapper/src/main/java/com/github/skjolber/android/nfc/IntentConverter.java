package com.github.skjolber.android.nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntentConverter {


    public static Intent convert(Intent input) {

        Intent output = new Intent();

        output.setAction(input.getAction());

        // detect supported types
        if(input.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) {

            Parcelable[] messages = input.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            Bundle extras = new Bundle();
            extras.putParcelableArray(NfcAdapter.EXTRA_NDEF_MESSAGES, messages);

            output.putExtras(extras);
        }

        if(input.hasExtra(NfcAdapter.EXTRA_TAG)) {
            android.nfc.Tag tag = (android.nfc.Tag) input.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            output.putExtra(NfcAdapter.EXTRA_TAG, new TagWrapper(tag));
        }

        if(input.hasExtra(NfcAdapter.EXTRA_AID)) {
            output.putExtra(NfcAdapter.EXTRA_AID, input.getParcelableExtra(NfcAdapter.EXTRA_AID));
        }


        if(input.hasExtra(NfcAdapter.EXTRA_ID)) {
            byte[] id = input.getByteArrayExtra(NfcAdapter.EXTRA_ID);

            output.putExtra(NfcAdapter.EXTRA_ID, id);
        }

        // TODO forward all types

        return output;
    }

}
