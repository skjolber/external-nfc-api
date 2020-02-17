package com.github.skjolber.android.nfc.tech;

import android.nfc.FormatException;
import android.nfc.NdefMessage;

import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagWrapper;

import java.io.IOException;

public class NdefFormattableWrapper extends NdefFormatable {

    protected android.nfc.tech.NdefFormatable delegate;

    public NdefFormattableWrapper(android.nfc.tech.NdefFormatable delegate) {
        this.delegate = delegate;
    }

    @Override
    public void format(NdefMessage firstMessage) throws IOException, FormatException {
        delegate.format(firstMessage);
    }

    @Override
    public void formatReadOnly(NdefMessage firstMessage) throws IOException, FormatException {
        delegate.formatReadOnly(firstMessage);
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
