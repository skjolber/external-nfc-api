/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.skjolber.android.nfc.tech;

import com.github.skjolber.android.nfc.ErrorCodes;
import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagImpl;
import com.github.skjolber.android.nfc.TagWrapper;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;

/**
 * Provides access to NFC-F (JIS 6319-4) properties and I/O operations on a {@link TagImpl}.
 *
 * <p>Acquire a {@link NfcF} object using {@link #get}.
 * <p>The primary NFC-F I/O operation is {@link #transceive}. Applications must
 * implement their own protocol stack on top of {@link #transceive}.
 *
 * <p class="note"><strong>Note:</strong> Methods that perform I/O operations
 * require the {@link android.Manifest.permission#NFC} permission.
 */
public abstract class NfcF implements BasicTagTechnology {

    /** @hide */
    public static final String EXTRA_SC = "systemcode";
    /** @hide */
    public static final String EXTRA_PMM = "pmm";

    /**
     * Get an instance of {@link NfcF} for the given tag.
     * <p>Returns null if {@link NfcF} was not enumerated in {@link TagImpl#getTechList}.
     * This indicates the tag does not support NFC-F.
     * <p>Does not cause any RF activity and does not block.
     *
     * @param tag an NFC-F compatible tag
     * @return NFC-F object
     */
    public static NfcF get(Tag tag) {
        if(tag instanceof TagImpl) {
            TagImpl tagImpl = (TagImpl)tag;
            if (!tagImpl.hasTech(TagTechnology.NFC_F)) return null;
            try {
                return new NfcFImpl(tagImpl);
            } catch (RemoteException e) {
                return null;
            }
        } else if(tag instanceof TagWrapper) {
            TagWrapper delegate = (TagWrapper)tag;
            return new NfcFWrapper(android.nfc.tech.NfcF.get(delegate.getDelegate()));
        } else {
            throw new IllegalArgumentException();
        }

    }

    /**
     * Return the System Code bytes from tag discovery.
     *
     * <p>Does not cause any RF activity and does not block.
     *
     * @return System Code bytes
     */
    public abstract byte[] getSystemCode();

    /**
     * Return the Manufacturer bytes from tag discovery.
     *
     * <p>Does not cause any RF activity and does not block.
     *
     * @return Manufacturer bytes
     */
    public abstract byte[] getManufacturer();

    /**
     * Send raw NFC-F commands to the tag and receive the response.
     *
     * <p>Applications must not prefix the SoD (preamble and sync code)
     * and/or append the EoD (CRC) to the payload, it will be automatically calculated.
     *
     * <p>A typical NFC-F frame for this method looks like:
     * <pre>
     * LENGTH (1 byte) --- CMD (1 byte) -- IDm (8 bytes) -- PARAMS (LENGTH - 10 bytes)
     * </pre>
     *
     * <p>Use {@link #getMaxTransceiveLength} to retrieve the maximum amount of bytes
     * that can be sent with {@link #transceive}.
     *
     * <p>This is an I/O operation and will block until complete. It must
     * not be called from the main application thread. A blocked call will be canceled with
     * {@link IOException} if {@link #close} is called from another thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param data bytes to send
     * @return bytes received in response
     * @throws android.nfc.TagLostException if the tag leaves the field
     * @throws IOException if there is an I/O failure, or this operation is canceled
     */
    public abstract byte[] transceive(byte[] data) throws IOException;

    /**
     * Return the maximum number of bytes that can be sent with {@link #transceive}.
     * @return the maximum number of bytes that can be sent with {@link #transceive}.
     */
    public abstract int getMaxTransceiveLength();

    /**
     * Set the {@link #transceive} timeout in milliseconds.
     *
     * <p>The timeout only applies to {@link #transceive} on this object,
     * and is reset to a default value when {@link #close} is called.
     *
     * <p>Setting a longer timeout may be useful when performing
     * transactions that require a long processing time on the tag
     * such as key generation.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param timeout timeout value in milliseconds
     */
    public abstract void setTimeout(int timeout);

    /**
     * Get the current {@link #transceive} timeout in milliseconds.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @return timeout value in milliseconds
     */
    public abstract int getTimeout();
}
