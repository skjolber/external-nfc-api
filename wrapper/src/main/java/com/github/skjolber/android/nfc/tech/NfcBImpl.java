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
 * Provides access to NFC-B (ISO 14443-3B) properties and I/O operations on a {@link TagImpl}.
 *
 * <p>Acquire a {@link NfcBImpl} object using {@link #get}.
 * <p>The primary NFC-B I/O operation is {@link #transceive}. Applications must
 * implement their own protocol stack on top of {@link #transceive}.
 *
 * <p class="note"><strong>Note:</strong> Methods that perform I/O operations
 * require the {@link android.Manifest.permission#NFC} permission.
 */
public final class NfcBImpl extends NfcB {

    private byte[] mAppData;
    private byte[] mProtInfo;

    private BasicTagTechnologyImpl delegate;

    public NfcBImpl(TagImpl tag) throws RemoteException {
        this.delegate = new BasicTagTechnologyImpl(tag, TagTechnology.NFC_B);

        Bundle extras = tag.getTechExtras(TagTechnology.NFC_B);
        mAppData = extras.getByteArray(EXTRA_APPDATA);
        mProtInfo = extras.getByteArray(EXTRA_PROTINFO);
    }

    /**
     * Return the Application Data bytes from ATQB/SENSB_RES at tag discovery.
     *
     * <p>Does not cause any RF activity and does not block.
     *
     * @return Application Data bytes from ATQB/SENSB_RES bytes
     */
    public byte[] getApplicationData() {
        return mAppData;
    }

    /**
     * Return the Protocol Info bytes from ATQB/SENSB_RES at tag discovery.
     *
     * <p>Does not cause any RF activity and does not block.
     *
     * @return Protocol Info bytes from ATQB/SENSB_RES bytes
     */
    public byte[] getProtocolInfo() {
        return mProtInfo;
    }

    /**
     * Send raw NFC-B commands to the tag and receive the response.
     *
     * <p>Applications must not append the EoD (CRC) to the payload,
     * it will be automatically calculated.
     * <p>Applications must not send commands that manage the polling
     * loop and initialization (SENSB_REQ, SLOT_MARKER etc).
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
