package com.github.skjolber.android.nfc;

import com.github.skjolber.android.nfc.TagImpl;

/**
 * @hide
 */
interface INfcUnlockHandler {

    boolean onUnlockAttempted(in TagImpl tag);

}
