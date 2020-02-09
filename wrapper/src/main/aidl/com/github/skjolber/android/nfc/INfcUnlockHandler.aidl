package com.github.skjolber.android.nfc;

import com.github.skjolber.android.nfc.Tag;

/**
 * @hide
 */
interface INfcUnlockHandler {

    boolean onUnlockAttempted(in Tag tag);

}
