package com.github.skjolber.android.nfc.tech;

import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagWrapper;

import java.io.IOException;

public class MifareUltralightWrapper extends MifareUltralight {

    protected android.nfc.tech.MifareUltralight delegate;

    public MifareUltralightWrapper(android.nfc.tech.MifareUltralight delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getType() {
        return delegate.getType();
    }

    @Override
    public byte[] readPages(int pageOffset) throws IOException {
        return delegate.readPages(pageOffset);
    }

    @Override
    public void writePage(int pageOffset, byte[] data) throws IOException {
        delegate.writePage(pageOffset, data);
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
    public void setTimeout(int timeout) {
        delegate.setTimeout(timeout);
    }

    @Override
    public int getTimeout() {
        return delegate.getTimeout();
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
