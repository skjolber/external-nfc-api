package com.github.skjolber.android.nfc.tech;

import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagWrapper;

import java.io.IOException;

public class IsoDepWrapper extends IsoDep {

    protected android.nfc.tech.IsoDep delegate;

    public IsoDepWrapper(android.nfc.tech.IsoDep delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setTimeout(int timeout) {
        delegate.setTimeout(timeout);
    }

    @Override
    public int getTimeout() {
        return delegate.getTimeout();
    }

    public byte[] getHistoricalBytes() {
        return delegate.getHistoricalBytes();
    }

    public byte[] getHiLayerResponse() {
        return delegate.getHiLayerResponse();
    }

    public byte[] transceive(byte[] data) throws IOException {
        return delegate.transceive(data);
    }

    public int getMaxTransceiveLength() {
        return delegate.getMaxTransceiveLength();
    }

    public boolean isExtendedLengthApduSupported() {
        return this.delegate.isExtendedLengthApduSupported();
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
