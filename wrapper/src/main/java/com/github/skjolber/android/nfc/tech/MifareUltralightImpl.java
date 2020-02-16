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

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;

//TOOD: Ultralight C 3-DES authentication, one-way counter

/**
 * Provides access to MIFARE Ultralight properties and I/O operations on a {@link TagImpl}.
 *
 * <p>Acquire a {@link MifareUltralightImpl} object using {@link #get}.
 *
 * <p>MIFARE Ultralight compatible tags have 4 byte pages {@link #PAGE_SIZE}.
 * The primary operations on an Ultralight tag are {@link #readPages} and
 * {@link #writePage}.
 *
 * <p>The original MIFARE Ultralight consists of a 64 byte EEPROM. The first
 * 4 pages are for the OTP area, manufacturer data, and locking bits. They are
 * readable and some bits are writable. The final 12 pages are the user
 * read/write area. For more information see the NXP data sheet MF0ICU1.
 *
 * <p>The MIFARE Ultralight C consists of a 192 byte EEPROM. The first 4 pages
 * are for OTP, manufacturer data, and locking bits. The next 36 pages are the
 * user read/write area. The next 4 pages are additional locking bits, counters
 * and authentication configuration and are readable. The final 4 pages are for
 * the authentication key and are not readable. For more information see the
 * NXP data sheet MF0ICU2.
 *
 * <p>Implementation of this class on a Android NFC device is optional.
 * If it is not implemented, then
 * {@link MifareUltralightImpl} will never be enumerated in {@link TagImpl#getTechList}.
 * If it is enumerated, then all {@link MifareUltralightImpl} I/O operations will be supported.
 * In either case, {@link NfcAImpl} will also be enumerated on the tag,
 * because all MIFARE Ultralight tags are also {@link NfcAImpl} tags.
 *
 * <p class="note"><strong>Note:</strong> Methods that perform I/O operations
 * require the {@link android.Manifest.permission#NFC} permission.
 */
public final class MifareUltralightImpl extends MifareUltralight {

    /** @hide */
    public static final String EXTRA_IS_UL_C = "isulc";

    private static final String TAG = "NFC";

    private static final int NXP_MANUFACTURER_ID = 0x04;
    private static final int MAX_PAGE_COUNT = 256;

    private int mType;

    private BasicTagTechnologyImpl delegate;

    public MifareUltralightImpl(TagImpl tag) throws RemoteException {
        this.delegate = new BasicTagTechnologyImpl(tag, TagTechnology.MIFARE_ULTRALIGHT);

        // Check if this could actually be a MIFARE
        NfcA a = NfcAImpl.get(tag);

        mType = TYPE_UNKNOWN;

        if (a.getSak() == 0x00 && tag.getId()[0] == NXP_MANUFACTURER_ID) {
            Bundle extras = tag.getTechExtras(TagTechnology.MIFARE_ULTRALIGHT);
            if (extras.getBoolean(EXTRA_IS_UL_C)) {
                mType = TYPE_ULTRALIGHT_C;
            } else {
                mType = TYPE_ULTRALIGHT;
            }
        }
    }

    /**
     * Return the MIFARE Ultralight type of the tag.
     * <p>One of {@link #TYPE_ULTRALIGHT} or {@link #TYPE_ULTRALIGHT_C} or
     * {@link #TYPE_UNKNOWN}.
     * <p>Depending on how the tag has been formatted, it can be impossible
     * to accurately classify between original MIFARE Ultralight and
     * Ultralight C. So treat this method as a hint.
     * <p>Does not cause any RF activity and does not block.
     *
     * @return the type
     */
    @Override
    public int getType() {
        return mType;
    }

    /**
     * Read 4 pages (16 bytes).
     *
     * <p>The MIFARE Ultralight protocol always reads 4 pages at a time, to
     * reduce the number of commands required to read an entire tag.
     * <p>If a read spans past the last readable block, then the tag will
     * return pages that have been wrapped back to the first blocks. MIFARE
     * Ultralight tags have readable blocks 0x00 through 0x0F. So a read to
     * block offset 0x0E would return blocks 0x0E, 0x0F, 0x00, 0x01. MIFARE
     * Ultralight C tags have readable blocks 0x00 through 0x2B. So a read to
     * block 0x2A would return blocks 0x2A, 0x2B, 0x00, 0x01.
     *
     * <p>This is an I/O operation and will block until complete. It must
     * not be called from the main application thread. A blocked call will be canceled with
     * {@link IOException} if {@link #close} is called from another thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param pageOffset index of first page to read, starting from 0
     * @return 4 pages (16 bytes)
     * @throws android.nfc.TagLostException if the tag leaves the field
     * @throws IOException if there is an I/O failure, or the operation is canceled
     */
    @Override
    public byte[] readPages(int pageOffset) throws IOException {
        validatePageIndex(pageOffset);
        delegate.checkConnected();

        byte[] cmd = { 0x30, (byte) pageOffset};
        return delegate.transceive(cmd, false);
    }

    /**
     * Write 1 page (4 bytes).
     *
     * <p>The MIFARE Ultralight protocol always writes 1 page at a time, to
     * minimize EEPROM write cycles.
     *
     * <p>This is an I/O operation and will block until complete. It must
     * not be called from the main application thread. A blocked call will be canceled with
     * {@link IOException} if {@link #close} is called from another thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param pageOffset index of page to write, starting from 0
     * @param data 4 bytes to write
     * @throws android.nfc.TagLostException if the tag leaves the field
     * @throws IOException if there is an I/O failure, or the operation is canceled
     */
    @Override
    public void writePage(int pageOffset, byte[] data) throws IOException {
        validatePageIndex(pageOffset);
        delegate.checkConnected();

        byte[] cmd = new byte[data.length + 2];
        cmd[0] = (byte) 0xA2;
        cmd[1] = (byte) pageOffset;
        System.arraycopy(data, 0, cmd, 2, data.length);

        delegate.transceive(cmd, false);
    }

    /**
     * Send raw NfcA data to a tag and receive the response.
     *
     * <p>This is equivalent to connecting to this tag via {@link NfcAImpl}
     * and calling {@link NfcAImpl#transceive}. Note that all MIFARE Classic
     * tags are based on {@link NfcAImpl} technology.
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
     * @see NfcAImpl#transceive
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
    @Override
    public void setTimeout(int timeout) {
        try {
            int err = delegate.getTag().getTagService().setTimeout(
                    TagTechnology.MIFARE_ULTRALIGHT, timeout);
            if (err != ErrorCodes.SUCCESS) {
                throw new IllegalArgumentException("The supplied timeout is not valid");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        }
    }

    /**
     * Get the current {@link #transceive} timeout in milliseconds.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @return timeout value in milliseconds
     */
    @Override
    public int getTimeout() {
        try {
            return delegate.getTag().getTagService().getTimeout(TagTechnology.MIFARE_ULTRALIGHT);
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return 0;
        }
    }

    private static void validatePageIndex(int pageIndex) {
        // Do not be too strict on upper bounds checking, since some cards
        // may have more addressable memory than they report.
        // Note that issuing a command to an out-of-bounds block is safe - the
        // tag will wrap the read to an addressable area. This validation is a
        // helper to guard against obvious programming mistakes.
        if (pageIndex < 0 || pageIndex >= MAX_PAGE_COUNT) {
            throw new IndexOutOfBoundsException("page out of bounds: " + pageIndex);
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
