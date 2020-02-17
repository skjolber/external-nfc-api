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
import com.github.skjolber.android.nfc.TagWrapper;
import com.github.skjolber.android.nfc.TagImpl;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;

/**
 * Provides access to ISO-DEP (ISO 14443-4) properties and I/O operations on a {@link TagImpl}.
 *
 * <p>Acquire an {@link IsoDepImpl} object using {@link #get}.
 * <p>The primary ISO-DEP I/O operation is {@link #transceive}. Applications must
 * implement their own protocol stack on top of {@link #transceive}.
 * <p>Tags that enumerate the {@link IsoDepImpl} technology in {@link TagImpl#getTechList}
 * will also enumerate
 * {@link NfcAImpl} or {@link NfcB} (since IsoDep builds on top of either of these).
 *
 * <p class="note"><strong>Note:</strong> Methods that perform I/O operations
 * require the {@link android.Manifest.permission#NFC} permission.
 */
public class IsoDepImpl extends IsoDep {
    private static final String TAG = "NFC";

    public static final String EXTRA_HI_LAYER_RESP = "hiresp";
    public static final String EXTRA_HIST_BYTES = "histbytes";

    private byte[] mHiLayerResponse = null;
    private byte[] mHistBytes = null;

    private BasicTagTechnologyImpl delegate;

    public IsoDepImpl(TagImpl tag)
            throws RemoteException {
        this.delegate = new BasicTagTechnologyImpl(tag, TagTechnology.ISO_DEP);
        Bundle extras = tag.getTechExtras(TagTechnology.ISO_DEP);
        if (extras != null) {
            mHiLayerResponse = extras.getByteArray(EXTRA_HI_LAYER_RESP);
            mHistBytes = extras.getByteArray(EXTRA_HIST_BYTES);
        }
    }

    /**
         * Set the timeout of {@link #transceive} in milliseconds.
         * <p>The timeout only applies to ISO-DEP {@link #transceive}, and is
         * reset to a default value when {@link #close} is called.
         * <p>Setting a longer timeout may be useful when performing
         * transactions that require a long processing time on the tag
         * such as key generation.
         *
         * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
         *
         * @param timeout timeout value in milliseconds
         */
    @Override
    public void setTimeout(int timeout) {
        try {
            int err = delegate.getTag().getTagService().setTimeout(TagTechnology.ISO_DEP, timeout);
            if (err != ErrorCodes.SUCCESS) {
                throw new IllegalArgumentException("The supplied timeout is not valid");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        }
    }

    /**
     * Get the current timeout for {@link #transceive} in milliseconds.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @return timeout value in milliseconds
     */
    @Override
    public int getTimeout() {
        try {
            return delegate.getTag().getTagService().getTimeout(TagTechnology.ISO_DEP);
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return 0;
        }
    }

    /**
     * Return the ISO-DEP historical bytes for {@link NfcAImpl} tags.
     * <p>Does not cause any RF activity and does not block.
     * <p>The historical bytes can be used to help identify a tag. They are present
     * only on {@link IsoDepImpl} tags that are based on {@link NfcAImpl} RF technology.
     * If this tag is not {@link NfcAImpl} then null is returned.
     * <p>In ISO 14443-4 terminology, the historical bytes are a subset of the RATS
     * response.
     *
     * @return ISO-DEP historical bytes, or null if this is not a {@link NfcAImpl} tag
     */
    @Override
    public byte[] getHistoricalBytes() {
        return mHistBytes;
    }

    /**
     * Return the higher layer response bytes for {@link NfcB} tags.
     * <p>Does not cause any RF activity and does not block.
     * <p>The higher layer response bytes can be used to help identify a tag.
     * They are present only on {@link IsoDepImpl} tags that are based on {@link NfcB}
     * RF technology. If this tag is not {@link NfcB} then null is returned.
     * <p>In ISO 14443-4 terminology, the higher layer bytes are a subset of the
     * ATTRIB response.
     *
     * @return ISO-DEP historical bytes, or null if this is not a {@link NfcB} tag
     */
    @Override
    public byte[] getHiLayerResponse() {
        return mHiLayerResponse;
    }

    /**
     * Send raw ISO-DEP data to the tag and receive the response.
     *
     * <p>Applications must only send the INF payload, and not the start of frame and
     * end of frame indicators. Applications do not need to fragment the payload, it
     * will be automatically fragmented and defragmented by {@link #transceive} if
     * it exceeds FSD/FSC limits.
     *
     * <p>Use {@link #getMaxTransceiveLength} to retrieve the maximum number of bytes
     * that can be sent with {@link #transceive}.
     *
     * <p>This is an I/O operation and will block until complete. It must
     * not be called from the main application thread. A blocked call will be canceled with
     * {@link IOException} if {@link #close} is called from another thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param data command bytes to send, must not be null
     * @return response bytes received, will not be null
     * @throws android.nfc.TagLostException if the tag leaves the field
     * @throws IOException if there is an I/O failure, or this operation is canceled
     */
    @Override
    public byte[] transceive(byte[] data) throws IOException {
        return delegate.transceive(data, true);
    }

    /**
     * Return the maximum number of bytes that can be sent with {@link #transceive}.
     * @return the maximum number of bytes that can be sent with {@link #transceive}.
     */
    @Override
    public int getMaxTransceiveLength() {
        return delegate.getMaxTransceiveLengthInternal();
    }

    /**
     * <p>Standard APDUs have a 1-byte length field, allowing a maximum of
     * 255 payload bytes, which results in a maximum APDU length of 261 bytes.
     *
     * <p>Extended length APDUs have a 3-byte length field, allowing 65535
     * payload bytes.
     *
     * <p>Some NFC adapters, like the one used in the Nexus S and the Galaxy Nexus
     * do not support extended length APDUs. They are expected to be well-supported
     * in the future though. Use this method to check for extended length APDU
     * support.
     *
     * @return whether the NFC adapter on this device supports extended length APDUs.
     */
    @Override
    public boolean isExtendedLengthApduSupported() {
        try {
            return delegate.getTag().getTagService().getExtendedLengthApdusSupported();
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return false;
        }
    }

    @Override
    public Tag getTag() {
        return delegate.getTag();
    }

    @Override
    public void connect() throws IOException {
        delegate.connect();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

}
