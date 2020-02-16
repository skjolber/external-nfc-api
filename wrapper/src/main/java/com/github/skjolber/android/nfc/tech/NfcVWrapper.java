package com.github.skjolber.android.nfc.tech;

import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagWrapper;

import java.io.IOException;

public class NfcVWrapper extends NfcV {

    protected android.nfc.tech.NfcV delegate;

    public NfcVWrapper(android.nfc.tech.NfcV delegate) {
        this.delegate = delegate;
    }

    @Override
    public byte[] transceive(byte[] data) throws IOException {
        return delegate.transceive(data);
    }

    @Override
    public int getMaxTransceiveLength() {
        return delegate.getMaxTransceiveLength();
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

