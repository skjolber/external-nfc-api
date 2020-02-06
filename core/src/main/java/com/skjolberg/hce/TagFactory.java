package com.skjolberg.hce;

import java.lang.reflect.Constructor;

import android.nfc.Tag;
import android.os.Bundle;

public class TagFactory {

    public Tag createTag(byte[] id, int[] techList, Bundle[] bundles, int serviceHandle, Object tagService) {
        Constructor<?>[] constructors = Tag.class.getConstructors();

        if (id == null) {
            id = new byte[]{};
        }
        // public Tag(byte[] id, int[] techList, Bundle[] techListExtras, int serviceHandle, INfcTag tagService)
        try {
            return (Tag) constructors[0].newInstance(id, techList, bundles, serviceHandle, tagService);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
