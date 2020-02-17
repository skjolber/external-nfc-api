package com.github.skjolber.android.nfc.tech;

import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagWrapper;

import java.io.IOException;

public class MifareClassicWrapper extends MifareClassic {

    protected android.nfc.tech.MifareClassic delegate;

    public MifareClassicWrapper(android.nfc.tech.MifareClassic delegate) {
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


    @Override
    public int getType() {
        return delegate.getType();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public int getSectorCount() {
        return delegate.getSectorCount();
    }

    @Override
    public int getBlockCount() {
        return delegate.getBlockCount();
    }

    @Override
    public int getBlockCountInSector(int sectorIndex) {
        return delegate.getBlockCountInSector(sectorIndex);
    }

    @Override
    public int blockToSector(int blockIndex) {
        return delegate.blockToSector(blockIndex);
    }

    @Override
    public int sectorToBlock(int sectorIndex) {
        return delegate.sectorToBlock(sectorIndex);
    }

    @Override
    public boolean authenticateSectorWithKeyA(int sectorIndex, byte[] key) throws IOException {
        return delegate.authenticateSectorWithKeyA(sectorIndex, key);
    }

    @Override
    public boolean authenticateSectorWithKeyB(int sectorIndex, byte[] key) throws IOException {
        return delegate.authenticateSectorWithKeyB(sectorIndex, key);
    }

    @Override
    public byte[] readBlock(int blockIndex) throws IOException {
        return delegate.readBlock(blockIndex);
    }

    @Override
    public void writeBlock(int blockIndex, byte[] data) throws IOException {
        delegate.writeBlock(blockIndex, data);
    }

    @Override
    public void increment(int blockIndex, int value) throws IOException {
        delegate.increment(blockIndex, value);
    }

    @Override
    public void decrement(int blockIndex, int value) throws IOException {
        delegate.decrement(blockIndex, value);
    }

    @Override
    public void transfer(int blockIndex) throws IOException {
        delegate.transfer(blockIndex);
    }

    @Override
    public void restore(int blockIndex) throws IOException {
        delegate.restore(blockIndex);
    }

    public byte[] transceive(byte[] data) throws IOException {
        return delegate.transceive(data);
    }

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
