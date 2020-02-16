package com.github.skjolber.android.nfc.tech;

import android.os.RemoteException;

import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagWrapper;
import com.github.skjolber.android.nfc.TagImpl;

import java.io.IOException;

public abstract class MifareUltralight implements BasicTagTechnology {
    /** A MIFARE Ultralight compatible tag of unknown type */
    public static final int TYPE_UNKNOWN = -1;
    /** A MIFARE Ultralight tag */
    public static final int TYPE_ULTRALIGHT = 1;
    /** A MIFARE Ultralight C tag */
    public static final int TYPE_ULTRALIGHT_C = 2;
    /** Size of a MIFARE Ultralight page in bytes */
    public static final int PAGE_SIZE = 4;

    /**
     * Get an instance of {@link MifareUltralightImpl} for the given tag.
     * <p>Returns null if {@link MifareUltralightImpl} was not enumerated in
     * {@link TagImpl#getTechList} - this indicates the tag is not MIFARE
     * Ultralight compatible, or that this Android
     * device does not implement MIFARE Ultralight.
     * <p>Does not cause any RF activity and does not block.
     *
     * @param tag an MIFARE Ultralight compatible tag
     * @return MIFARE Ultralight object
     */
    public static MifareUltralight get(Tag tag) {
        if(tag instanceof TagImpl) {
            TagImpl tagImpl = (TagImpl)tag;
            if (!tagImpl.hasTech(TagTechnology.MIFARE_ULTRALIGHT)) return null;
            try {
                return new MifareUltralightImpl(tagImpl);
            } catch (RemoteException e) {
                return null;
            }
        } else if(tag instanceof TagWrapper) {
            TagWrapper delegate = (TagWrapper)tag;
            return new MifareUltralightWrapper(android.nfc.tech.MifareUltralight.get(delegate.getDelegate()));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public abstract int getType();

    public abstract byte[] readPages(int pageOffset) throws IOException;

    public abstract void writePage(int pageOffset, byte[] data) throws IOException;

    public abstract byte[] transceive(byte[] data) throws IOException;

    public abstract int getMaxTransceiveLength();

    public abstract void setTimeout(int timeout);

    public abstract int getTimeout();
}
