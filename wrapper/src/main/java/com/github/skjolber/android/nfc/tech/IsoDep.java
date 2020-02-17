package com.github.skjolber.android.nfc.tech;

import android.os.RemoteException;

import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagImpl;
import com.github.skjolber.android.nfc.TagWrapper;

import java.io.IOException;

public abstract class IsoDep implements BasicTagTechnology {

    /**
     * Get an instance of {@link IsoDepImpl} for the given tag.
     * <p>Does not cause any RF activity and does not block.
     * <p>Returns null if {@link IsoDepImpl} was not enumerated in {@link TagImpl#getTechList}.
     * This indicates the tag does not support ISO-DEP.
     *
     * @param tag an ISO-DEP compatible tag
     * @return ISO-DEP object
     */
    public static IsoDep get(Tag tag) {
        if(tag instanceof TagImpl) {
            TagImpl tagImpl = (TagImpl)tag;
            if (!tagImpl.hasTech(TagTechnology.ISO_DEP)) return null;
            try {
                return new IsoDepImpl(tagImpl);
            } catch (RemoteException e) {
                return null;
            }
        } else if(tag instanceof TagWrapper) {
            TagWrapper delegate = (TagWrapper)tag;
            return new IsoDepWrapper(android.nfc.tech.IsoDep.get(delegate.getDelegate()));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public abstract void setTimeout(int timeout);

    public abstract int getTimeout();

    public abstract byte[] getHistoricalBytes();

    public abstract byte[] getHiLayerResponse();

    public abstract byte[] transceive(byte[] data) throws IOException;

    public abstract int getMaxTransceiveLength();

    public abstract boolean isExtendedLengthApduSupported();
}
