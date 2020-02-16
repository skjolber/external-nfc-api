package com.github.skjolber.android.nfc.tech;

import android.os.RemoteException;

import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagImpl;
import com.github.skjolber.android.nfc.TagWrapper;

import java.io.IOException;

public abstract class NfcA implements BasicTagTechnology {

    String EXTRA_SAK = "sak";
    String EXTRA_ATQA = "atqa";

    /**
     * Get an instance of {@link NfcAImpl} for the given tag.
     * <p>Returns null if {@link NfcAImpl} was not enumerated in {@link TagImpl#getTechList}.
     * This indicates the tag does not support NFC-A.
     * <p>Does not cause any RF activity and does not block.
     *
     * @param tag an NFC-A compatible tag
     * @return NFC-A object
     */
    public static NfcA get(Tag tag) {
        if(tag instanceof TagImpl) {
            TagImpl tagImpl = (TagImpl)tag;
            if (!tagImpl.hasTech(TagTechnology.NFC_A)) return null;
            try {
                return new NfcAImpl(tagImpl);
            } catch (RemoteException e) {
                return null;
            }
        } else if(tag instanceof TagWrapper) {
            TagWrapper delegate = (TagWrapper)tag;
            return new NfcAWrapper(android.nfc.tech.NfcA.get(delegate.getDelegate()));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public abstract byte[] getAtqa();

    public abstract short getSak();

    public abstract byte[] transceive(byte[] data) throws IOException;

    public abstract int getMaxTransceiveLength();

    public abstract void setTimeout(int timeout);

    public abstract int getTimeout();
}
