package com.github.skjolber.nfc.hce;

import java.lang.reflect.Constructor;

import android.os.Bundle;

import com.github.skjolber.android.nfc.INfcTag;
import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagImpl;

public class TagFactory {

    public Tag createTag(byte[] id, int[] techList, Bundle[] bundles, int serviceHandle, INfcTag tagService) {
        return new TagImpl(id, techList, bundles, serviceHandle, tagService);
    }

}
