package com.github.skjolber.nfc.hce;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.skjolber.nfc.NfcReader;
import com.github.skjolber.nfc.NfcService;
import com.github.skjolber.nfc.acs.Acr1222LReader;
import com.github.skjolber.nfc.acs.Acr122UReader;
import com.github.skjolber.nfc.acs.Acr1251UReader;
import com.github.skjolber.nfc.acs.Acr1252UReader;
import com.github.skjolber.nfc.acs.Acr1255UReader;
import com.github.skjolber.nfc.acs.Acr1281UReader;
import com.github.skjolber.nfc.acs.Acr1283LReader;
import com.github.skjolber.nfc.command.ACR1222Commands;
import com.github.skjolber.nfc.command.ACR122Commands;
import com.github.skjolber.nfc.command.ACR1251Commands;
import com.github.skjolber.nfc.command.ACR1252Commands;
import com.github.skjolber.nfc.command.ACR1255BluetoothCommands;
import com.github.skjolber.nfc.command.ACR1255UsbCommands;
import com.github.skjolber.nfc.command.ACR1281Commands;
import com.github.skjolber.nfc.command.ACR1283Commands;
import com.github.skjolber.nfc.command.ACRCommands;
import com.github.skjolber.nfc.messages.NfcReaderServiceListener;

public class DefaultNfcReaderServiceListener implements NfcReaderServiceListener {

    private static final String TAG = DefaultNfcReaderServiceListener.class.getName();

    private Context context;
    private IAcr122UBinder acr122Binder;
    private IAcr1222LBinder acr1222Binder;
    private IAcr1251UBinder acr1251Binder;
    private IAcr1281UBinder acr1281Binder;
    private IAcr1283Binder acr1283Binder;
    private IAcr1252UBinder acr1252Binder;
    private IAcr1255UBinder acr1255Binder;

    public DefaultNfcReaderServiceListener(IAcr122UBinder acr122Binder, IAcr1222LBinder acr1222Binder, IAcr1251UBinder acr1251Binder, IAcr1281UBinder acr1281Binder, IAcr1283Binder acr1283Binder, IAcr1252UBinder acr1252Binder, IAcr1255UBinder acr1255Binder, Context context) {
        this.acr122Binder = acr122Binder;
        this.acr1222Binder = acr1222Binder;
        this.acr1251Binder = acr1251Binder;
        this.acr1281Binder = acr1281Binder;
        this.acr1283Binder = acr1283Binder;
        this.acr1252Binder = acr1252Binder;
        this.acr1255Binder = acr1255Binder;
        this.context = context;
    }

    public void onServiceStarted() {
        broadcast(NfcService.ACTION_SERVICE_STARTED);
    }

    public void onServiceStopped() {
        broadcast(NfcService.ACTION_SERVICE_STOPPED);
    }

    @Override
    public void onReaderOpen(Object r, int status) {
        Intent intent = new Intent();

        intent.setAction(NfcReader.ACTION_READER_OPENED);
        if (r != null) {
            if (r instanceof ACRCommands) {
                ACRCommands reader = (ACRCommands) r;
                if (reader instanceof ACR122Commands) {
                    acr122Binder.setAcr122UCommands((ACR122Commands) reader);
                    intent.putExtra(NfcReader.EXTRA_READER_CONTROL, new Acr122UReader(reader.getName(), acr122Binder));
                } else if (reader instanceof ACR1222Commands) {
                    acr1222Binder.setAcr1222LCommands((ACR1222Commands) reader);
                    intent.putExtra(NfcReader.EXTRA_READER_CONTROL, new Acr1222LReader(reader.getName(), acr1222Binder));
                } else if (reader instanceof ACR1251Commands) {
                    acr1251Binder.setCommands((ACR1251Commands) reader);
                    intent.putExtra(NfcReader.EXTRA_READER_CONTROL, new Acr1251UReader(reader.getName(), acr1251Binder));
                } else if (reader instanceof ACR1252Commands) {
                    acr1252Binder.setCommands((ACR1252Commands) reader);
                    intent.putExtra(NfcReader.EXTRA_READER_CONTROL, new Acr1252UReader(reader.getName(), acr1252Binder));
                } else if (reader instanceof ACR1255UsbCommands) {
                    acr1255Binder.setCommands((ACR1255UsbCommands) reader);
                    intent.putExtra(NfcReader.EXTRA_READER_CONTROL, new Acr1255UReader(reader.getName(), acr1255Binder));
                } else if (reader instanceof ACR1281Commands) {
                    acr1281Binder.setCommands((ACR1281Commands) reader);
                    intent.putExtra(NfcReader.EXTRA_READER_CONTROL, new Acr1281UReader(reader.getName(), acr1281Binder));
                } else if (reader instanceof ACR1283Commands) {
                    acr1283Binder.setCommands((ACR1283Commands) reader);
                    intent.putExtra(NfcReader.EXTRA_READER_CONTROL, new Acr1283LReader(reader.getName(), acr1283Binder));
                } else {
                    Log.d(TAG, "Not supporting reader extras");
                }
            } else if (r instanceof ACR1255BluetoothCommands) {
                ACR1255BluetoothCommands reader = (ACR1255BluetoothCommands) r;
                acr1255Binder.setCommands(reader);
                intent.putExtra(NfcReader.EXTRA_READER_CONTROL, new Acr1255UReader(reader.getName(), acr1255Binder));
            }
        } else {
            Log.d(TAG, "No reader extras");
        }

        intent.putExtra(NfcReader.EXTRA_READER_STATUS_CODE, status);

        sendBroadcast(intent);
    }

    public void onReaderClosed(int status, String message) {
        if (acr122Binder != null) acr122Binder.clearReader();
        if (acr1222Binder != null) acr1222Binder.clearReader();
        if (acr1251Binder != null) acr1251Binder.clearReader();
        if (acr1252Binder != null) acr1252Binder.clearReader();
        if (acr1255Binder != null) acr1255Binder.clearReader();
        if (acr1281Binder != null) acr1281Binder.clearReader();
        if (acr1283Binder != null) acr1283Binder.clearReader();

        Intent intent = new Intent();
        intent.setAction(NfcReader.ACTION_READER_CLOSED);

        if (status != -1) {
            intent.putExtra(NfcReader.EXTRA_READER_STATUS_CODE, status);
        }

        if (message != null) {
            intent.putExtra(NfcReader.EXTRA_READER_STATUS_MESSAGE, message);
        }

        sendBroadcast(intent);
    }

    public void broadcast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }

    private void sendBroadcast(Intent intent) {
        Log.d(TAG, "Broadcast " + intent.getAction());

        context.sendBroadcast(intent);
    }
}
