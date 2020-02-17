package com.github.skjolber.android.nfc;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

public abstract class Tag implements Parcelable {

    /**
     * Construct a mock Tag.
     * <p>This is an application constructed tag, so NfcAdapter methods on this Tag may fail
     * with {@link IllegalArgumentException} since it does not represent a physical Tag.
     * <p>This constructor might be useful for mock testing.
     * @param id The tag identifier, can be null
     * @param techList must not be null
     * @return freshly constructed tag
     */
    public static Tag createMockTag(byte[] id, int[] techList, Bundle[] techListExtras) {
        // set serviceHandle to 0 and tagService to null to indicate mock tag
        return new TagImpl(id, techList, techListExtras, 0, null);
    }

    public abstract byte[] getId();

    public abstract String[] getTechList();

}
