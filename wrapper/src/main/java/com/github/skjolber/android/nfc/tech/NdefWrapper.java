package com.github.skjolber.android.nfc.tech;

import android.nfc.FormatException;
import android.nfc.NdefMessage;

import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagWrapper;

import java.io.IOException;

public class NdefWrapper extends Ndef {

    protected android.nfc.tech.Ndef delegate;

    public NdefWrapper(android.nfc.tech.Ndef delegate) {
            this.delegate = delegate;
        }

    @Override
    public NdefMessage getCachedNdefMessage() {
        return delegate.getCachedNdefMessage();
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public int getMaxSize() {
        return delegate.getMaxSize();
    }

    @Override
    public boolean isWritable() {
        return delegate.isWritable();
    }

    @Override
    public NdefMessage getNdefMessage() throws IOException, FormatException {
        return delegate.getNdefMessage();
    }

    @Override
    public void writeNdefMessage(NdefMessage msg) throws IOException, FormatException {
        delegate.writeNdefMessage(msg);
    }

    @Override
    public boolean canMakeReadOnly() {
        return delegate.canMakeReadOnly();
    }

    @Override
    public boolean makeReadOnly() throws IOException {
        return delegate.makeReadOnly();
    }


    @Override
    public Tag getTag() {
        return new TagWrapper(delegate.getTag());
    }

    @Override
    public void connect() throws IOException {
        delegate.connect();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }
}
