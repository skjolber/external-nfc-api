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

import android.os.Bundle;
import android.os.RemoteException;

import com.github.skjolber.android.nfc.Tag;
import com.github.skjolber.android.nfc.TagImpl;

import java.io.IOException;

/**
 * Provides access to NFC-V (ISO 15693) properties and I/O operations on a {@link TagImpl}.
 *
 * <p>Acquire a {@link NfcVImpl} object using {@link #get}.
 * <p>The primary NFC-V I/O operation is {@link #transceive}. Applications must
 * implement their own protocol stack on top of {@link #transceive}.
 *
 * <p class="note"><strong>Note:</strong> Methods that perform I/O operations
 * require the {@link android.Manifest.permission#NFC} permission.
 */
public class NfcVImpl extends NfcV {
    public static final String EXTRA_RESP_FLAGS = "respflags";

    /** @hide */
    public static final String EXTRA_DSFID = "dsfid";

    private byte mRespFlags;
    private byte mDsfId;

    private BasicTagTechnologyImpl delegate;

    public NfcVImpl(TagImpl tag) throws RemoteException {
        this.delegate = new BasicTagTechnologyImpl(tag, TagTechnology.NFC_V);

        Bundle extras = tag.getTechExtras(TagTechnology.NFC_V);
        mRespFlags = extras.getByte(EXTRA_RESP_FLAGS);
        mDsfId = extras.getByte(EXTRA_DSFID);
    }

    /**
     * Return the Response Flag bytes from tag discovery.
     *
     * <p>Does not cause any RF activity and does not block.
     *
     * @return Response Flag bytes
     */
    public byte getResponseFlags() {
        return mRespFlags;
    }

    /**
     * Return the DSF ID bytes from tag discovery.
     *
     * <p>Does not cause any RF activity and does not block.
     *
     * @return DSF ID bytes
     */
    public byte getDsfId() {
        return mDsfId;
    }

    /**
     * Send raw NFC-V commands to the tag and receive the response.
     *
     * <p>Applications must not append the CRC to the payload,
     * it will be automatically calculated. The application does
     * provide FLAGS, CMD and PARAMETER bytes.
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
    public byte[] transceive(byte[] data) throws IOException {
        return delegate.transceive(data, true);
    }


    /**
     * Return the maximum number of bytes that can be sent with {@link #transceive}.
     * @return the maximum number of bytes that can be sent with {@link #transceive}.
     */
    public int getMaxTransceiveLength() {
        return delegate.getMaxTransceiveLengthInternal();
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
