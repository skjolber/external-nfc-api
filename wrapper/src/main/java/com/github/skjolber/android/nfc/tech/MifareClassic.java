package com.github.skjolber.android.nfc.tech;

import android.os.RemoteException;

import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagWrapper;
import com.github.skjolber.android.nfc.TagImpl;

import java.io.IOException;

public abstract class MifareClassic implements BasicTagTechnology {
    /**
     * The default factory key.
     */
    public static byte[] KEY_DEFAULT =
            {(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};
    /**
     * The well-known key for tags formatted according to the
     * MIFARE Application Directory (MAD) specification.
     */
    byte[] KEY_MIFARE_APPLICATION_DIRECTORY =
            {(byte)0xA0,(byte)0xA1,(byte)0xA2,(byte)0xA3,(byte)0xA4,(byte)0xA5};
    /**
     * The well-known key for tags formatted according to the
     * NDEF on MIFARE Classic specification.
     */
    byte[] KEY_NFC_FORUM =
            {(byte)0xD3,(byte)0xF7,(byte)0xD3,(byte)0xF7,(byte)0xD3,(byte)0xF7};
    /** A MIFARE Classic compatible card of unknown type */
    public final int TYPE_UNKNOWN = -1;
    /** A MIFARE Classic tag */
    public final int TYPE_CLASSIC = 0;
    /** A MIFARE Plus tag */
    public final int TYPE_PLUS = 1;
    /** A MIFARE Pro tag */
    public final int TYPE_PRO = 2;
    /** Tag contains 16 sectors, each with 4 blocks. */
    public final int SIZE_1K = 1024;
    /** Tag contains 32 sectors, each with 4 blocks. */
    public final int SIZE_2K = 2048;
    /**
     * Tag contains 40 sectors. The first 32 sectors contain 4 blocks and the last 8 sectors
     * contain 16 blocks.
     */
    public final int SIZE_4K = 4096;
    /** Tag contains 5 sectors, each with 4 blocks. */
    public final int SIZE_MINI = 320;
    /** Size of a MIFARE Classic block (in bytes) */
    public final int BLOCK_SIZE = 16;

    /**
     * Get an instance of {@link MifareClassicImpl} for the given tag.
     * <p>Does not cause any RF activity and does not block.
     * <p>Returns null if {@link MifareClassicImpl} was not enumerated in {@link TagImpl#getTechList}.
     * This indicates the tag is not MIFARE Classic compatible, or this Android
     * device does not support MIFARE Classic.
     *
     * @param tag an MIFARE Classic compatible tag
     * @return MIFARE Classic object
     */
    public static MifareClassic get(Tag tag) {
        if(tag instanceof TagImpl) {
            TagImpl tagImpl = (TagImpl)tag;
            if (!tagImpl.hasTech(TagTechnology.MIFARE_CLASSIC)) return null;
            try {
                return new MifareClassicImpl(tagImpl);
            } catch (RemoteException e) {
                return null;
            }
        } else if(tag instanceof TagWrapper) {
            TagWrapper delegate = (TagWrapper)tag;
            return new MifareClassicWrapper(android.nfc.tech.MifareClassic.get(delegate.getDelegate()));
        } else {
            throw new IllegalArgumentException();
        }
    }



    public abstract int getType();

    public abstract int getSize();

    public abstract int getSectorCount();

    public abstract int getBlockCount();

    public abstract int getBlockCountInSector(int sectorIndex);

    public abstract int blockToSector(int blockIndex);

    public abstract int sectorToBlock(int sectorIndex);

    public abstract boolean authenticateSectorWithKeyA(int sectorIndex, byte[] key) throws IOException;

    public abstract boolean authenticateSectorWithKeyB(int sectorIndex, byte[] key) throws IOException;

    public abstract byte[] readBlock(int blockIndex) throws IOException;

    public abstract void writeBlock(int blockIndex, byte[] data) throws IOException;

    public abstract void increment(int blockIndex, int value) throws IOException;

    public abstract void decrement(int blockIndex, int value) throws IOException;

    public abstract void transfer(int blockIndex) throws IOException;

    public abstract void restore(int blockIndex) throws IOException;

    public abstract byte[] transceive(byte[] data) throws IOException;

    public abstract int getMaxTransceiveLength();

    public abstract void setTimeout(int timeout);

    public abstract int getTimeout();
}
