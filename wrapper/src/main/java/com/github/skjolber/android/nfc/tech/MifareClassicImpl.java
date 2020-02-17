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

import android.nfc.TagLostException;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Provides access to MIFARE Classic properties and I/O operations on a {@link TagImpl}.
 *
 * <p>Acquire a {@link MifareClassicImpl} object using {@link #get}.
 *
 * <p>MIFARE Classic is also known as MIFARE Standard.
 * <p>MIFARE Classic tags are divided into sectors, and each sector is sub-divided into
 * blocks. Block size is always 16 bytes ({@link #BLOCK_SIZE}. Sector size varies.
 * <ul>
 * <li>MIFARE Classic Mini are 320 bytes ({@link #SIZE_MINI}), with 5 sectors each of 4 blocks.
 * <li>MIFARE Classic 1k are 1024 bytes ({@link #SIZE_1K}), with 16 sectors each of 4 blocks.
 * <li>MIFARE Classic 2k are 2048 bytes ({@link #SIZE_2K}), with 32 sectors each of 4 blocks.
 * <li>MIFARE Classic 4k are 4096 bytes ({@link #SIZE_4K}). The first 32 sectors contain 4 blocks
 * and the last 8 sectors contain 16 blocks.
 * </ul>
 *
 * <p>MIFARE Classic tags require authentication on a per-sector basis before any
 * other I/O operations on that sector can be performed. There are two keys per sector,
 * and ACL bits determine what I/O operations are allowed on that sector after
 * authenticating with a key. {@see #authenticateSectorWithKeyA} and
 * {@see #authenticateSectorWithKeyB}.
 *
 * <p>Three well-known authentication keys are defined in this class:
 * {@link #KEY_DEFAULT}, {@link #KEY_MIFARE_APPLICATION_DIRECTORY},
 * {@link #KEY_NFC_FORUM}.
 * <ul>
 * <li>{@link #KEY_DEFAULT} is the default factory key for MIFARE Classic.
 * <li>{@link #KEY_MIFARE_APPLICATION_DIRECTORY} is the well-known key for
 * MIFARE Classic cards that have been formatted according to the
 * MIFARE Application Directory (MAD) specification.
 * <li>{@link #KEY_NFC_FORUM} is the well-known key for MIFARE Classic cards that
 * have been formatted according to the NXP specification for NDEF on MIFARE Classic.
 *
 * <p>Implementation of this class on a Android NFC device is optional.
 * If it is not implemented, then
 * {@link MifareClassicImpl} will never be enumerated in {@link TagImpl#getTechList}.
 * If it is enumerated, then all {@link MifareClassicImpl} I/O operations will be supported,
 * and {@link NdefImpl#MIFARE_CLASSIC} NDEF tags will also be supported. In either case,
 * {@link NfcAImpl} will also be enumerated on the tag, because all MIFARE Classic tags are also
 * {@link NfcAImpl}.
 *
 * <p class="note"><strong>Note:</strong> Methods that perform I/O operations
 * require the {@link android.Manifest.permission#NFC} permission.
 */
public final class MifareClassicImpl extends MifareClassic {

    private static final String TAG = "NFC";

    private static final int MAX_BLOCK_COUNT = 256;
    private static final int MAX_SECTOR_COUNT = 40;

    private boolean mIsEmulated;
    private int mType;
    private int mSize;

    private BasicTagTechnologyImpl delegate;

    public MifareClassicImpl(TagImpl tag) throws RemoteException {
        this.delegate = new BasicTagTechnologyImpl(tag, TagTechnology.MIFARE_CLASSIC);

        NfcA a = NfcAImpl.get(tag);  // MIFARE Classic is always based on NFC a

        mIsEmulated = false;

        switch (a.getSak()) {
        case 0x01:
        case 0x08:
            mType = TYPE_CLASSIC;
            mSize = SIZE_1K;
            break;
        case 0x09:
            mType = TYPE_CLASSIC;
            mSize = SIZE_MINI;
            break;
        case 0x10:
            mType = TYPE_PLUS;
            mSize = SIZE_2K;
            // SecLevel = SL2
            break;
        case 0x11:
            mType = TYPE_PLUS;
            mSize = SIZE_4K;
            // Seclevel = SL2
            break;
        case 0x18:
            mType = TYPE_CLASSIC;
            mSize = SIZE_4K;
            break;
        case 0x28:
            mType = TYPE_CLASSIC;
            mSize = SIZE_1K;
            mIsEmulated = true;
            break;
        case 0x38:
            mType = TYPE_CLASSIC;
            mSize = SIZE_4K;
            mIsEmulated = true;
            break;
        case 0x88:
            mType = TYPE_CLASSIC;
            mSize = SIZE_1K;
            // NXP-tag: false
            break;
        case 0x98:
        case 0xB8:
            mType = TYPE_PRO;
            mSize = SIZE_4K;
            break;
        default:
            // Stack incorrectly reported a MifareClassic. We cannot handle this
            // gracefully - we have no idea of the memory layout. Bail.
            throw new RuntimeException(
                    "Tag incorrectly enumerated as MIFARE Classic, SAK = " + a.getSak());
        }
    }

    /**
     * Return the type of this MIFARE Classic compatible tag.
     * <p>One of {@link #TYPE_UNKNOWN}, {@link #TYPE_CLASSIC}, {@link #TYPE_PLUS} or
     * {@link #TYPE_PRO}.
     * <p>Does not cause any RF activity and does not block.
     *
     * @return type
     */
    @Override
    public int getType() {
        return mType;
    }

    /**
     * Return the size of the tag in bytes
     * <p>One of {@link #SIZE_MINI}, {@link #SIZE_1K}, {@link #SIZE_2K}, {@link #SIZE_4K}.
     * These constants are equal to their respective size in bytes.
     * <p>Does not cause any RF activity and does not block.
     * @return size in bytes
     */
    @Override
    public int getSize() {
        return mSize;
    }

    /**
     * Return true if the tag is emulated, determined at discovery time.
     * These are actually smart-cards that emulate a MIFARE Classic interface.
     * They can be treated identically to a MIFARE Classic tag.
     * @hide
     */
    public boolean isEmulated() {
        return mIsEmulated;
    }

    /**
     * Return the number of MIFARE Classic sectors.
     * <p>Does not cause any RF activity and does not block.
     * @return number of sectors
     */
    @Override
    public int getSectorCount() {
        switch (mSize) {
        case SIZE_1K:
            return 16;
        case SIZE_2K:
            return 32;
        case SIZE_4K:
            return 40;
        case SIZE_MINI:
            return 5;
        default:
            return 0;
        }
    }

    /**
     * Return the total number of MIFARE Classic blocks.
     * <p>Does not cause any RF activity and does not block.
     * @return total number of blocks
     */
    @Override
    public int getBlockCount() {
        return mSize / BLOCK_SIZE;
    }

    /**
     * Return the number of blocks in the given sector.
     * <p>Does not cause any RF activity and does not block.
     *
     * @param sectorIndex index of sector, starting from 0
     * @return number of blocks in the sector
     */
    @Override
    public int getBlockCountInSector(int sectorIndex) {
        validateSector(sectorIndex);

        if (sectorIndex < 32) {
            return 4;
        } else {
            return 16;
        }
    }

    /**
     * Return the sector that contains a given block.
     * <p>Does not cause any RF activity and does not block.
     *
     * @param blockIndex index of block to lookup, starting from 0
     * @return sector index that contains the block
     */
    @Override
    public int blockToSector(int blockIndex) {
        validateBlock(blockIndex);

        if (blockIndex < 32 * 4) {
            return blockIndex / 4;
        } else {
            return 32 + (blockIndex - 32 * 4) / 16;
        }
    }

    /**
     * Return the first block of a given sector.
     * <p>Does not cause any RF activity and does not block.
     *
     * @param sectorIndex index of sector to lookup, starting from 0
     * @return block index of first block in sector
     */
    @Override
    public int sectorToBlock(int sectorIndex) {
        if (sectorIndex < 32) {
            return sectorIndex * 4;
        } else {
            return 32 * 4 + (sectorIndex - 32) * 16;
        }
    }

    /**
     * Authenticate a sector with key A.
     *
     * <p>Successful authentication of a sector with key A enables other
     * I/O operations on that sector. The set of operations granted by key A
     * key depends on the ACL bits set in that sector. For more information
     * see the MIFARE Classic specification on <a href="http://www.nxp.com">http://www.nxp.com</a>.
     *
     * <p>A failed authentication attempt causes an implicit reconnection to the
     * tag, so authentication to other sectors will be lost.
     *
     * <p>This is an I/O operation and will block until complete. It must
     * not be called from the main application thread. A blocked call will be canceled with
     * {@link IOException} if {@link #close} is called from another thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param sectorIndex index of sector to authenticate, starting from 0
     * @param key 6-byte authentication key
     * @return true on success, false on authentication failure
     * @throws TagLostException if the tag leaves the field
     * @throws IOException if there is an I/O failure, or the operation is canceled
     */
    @Override
    public boolean authenticateSectorWithKeyA(int sectorIndex, byte[] key) throws IOException {
        return authenticate(sectorIndex, key, true);
    }

    /**
     * Authenticate a sector with key B.
     *
     * <p>Successful authentication of a sector with key B enables other
     * I/O operations on that sector. The set of operations granted by key B
     * depends on the ACL bits set in that sector. For more information
     * see the MIFARE Classic specification on <a href="http://www.nxp.com">http://www.nxp.com</a>.
     *
     * <p>A failed authentication attempt causes an implicit reconnection to the
     * tag, so authentication to other sectors will be lost.
     *
     * <p>This is an I/O operation and will block until complete. It must
     * not be called from the main application thread. A blocked call will be canceled with
     * {@link IOException} if {@link #close} is called from another thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param sectorIndex index of sector to authenticate, starting from 0
     * @param key 6-byte authentication key
     * @return true on success, false on authentication failure
     * @throws android.nfc.TagLostException if the tag leaves the field
     * @throws IOException if there is an I/O failure, or the operation is canceled
     */
    @Override
    public boolean authenticateSectorWithKeyB(int sectorIndex, byte[] key) throws IOException {
        return authenticate(sectorIndex, key, false);
    }

    private boolean authenticate(int sector, byte[] key, boolean keyA) throws IOException {
        validateSector(sector);
        delegate.checkConnected();

        byte[] cmd = new byte[12];

        // First byte is the command
        if (keyA) {
            cmd[0] = 0x60; // phHal_eMifareAuthentA
        } else {
            cmd[0] = 0x61; // phHal_eMifareAuthentB
        }

        // Second byte is block address
        // Authenticate command takes a block address. Authenticating a block
        // of a sector will authenticate the entire sector.
        cmd[1] = (byte) sectorToBlock(sector);

        // Next 4 bytes are last 4 bytes of UID
        byte[] uid = getTag().getId();
        System.arraycopy(uid, uid.length - 4, cmd, 2, 4);

        // Next 6 bytes are key
        System.arraycopy(key, 0, cmd, 6, 6);

        try {
            if (delegate.transceive(cmd, false) != null) {
                return true;
            }
        } catch (TagLostException e) {
            throw e;
        } catch (IOException e) {
            // No need to deal with, will return false anyway
        }
        return false;
    }

    /**
     * Read 16-byte block.
     *
     * <p>This is an I/O operation and will block until complete. It must
     * not be called from the main application thread. A blocked call will be canceled with
     * {@link IOException} if {@link #close} is called from another thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param blockIndex index of block to read, starting from 0
     * @return 16 byte block
     * @throws TagLostException if the tag leaves the field
     * @throws IOException if there is an I/O failure, or the operation is canceled
     */
    @Override
    public byte[] readBlock(int blockIndex) throws IOException {
        validateBlock(blockIndex);
        delegate.checkConnected();

        byte[] cmd = { 0x30, (byte) blockIndex };
        return delegate.transceive(cmd, false);
    }

    /**
     * Write 16-byte block.
     *
     * <p>This is an I/O operation and will block until complete. It must
     * not be called from the main application thread. A blocked call will be canceled with
     * {@link IOException} if {@link #close} is called from another thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param blockIndex index of block to write, starting from 0
     * @param data 16 bytes of data to write
     * @throws TagLostException if the tag leaves the field
     * @throws IOException if there is an I/O failure, or the operation is canceled
     */
    @Override
    public void writeBlock(int blockIndex, byte[] data) throws IOException {
        validateBlock(blockIndex);
        delegate.checkConnected();
        if (data.length != 16) {
            throw new IllegalArgumentException("must write 16-bytes");
        }

        byte[] cmd = new byte[data.length + 2];
        cmd[0] = (byte) 0xA0; // MF write command
        cmd[1] = (byte) blockIndex;
        System.arraycopy(data, 0, cmd, 2, data.length);

        delegate.transceive(cmd, false);
    }

    /**
     * Increment a value block, storing the result in the temporary block on the tag.
     *
     * <p>This is an I/O operation and will block until complete. It must
     * not be called from the main application thread. A blocked call will be canceled with
     * {@link IOException} if {@link #close} is called from another thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param blockIndex index of block to increment, starting from 0
     * @param value non-negative to increment by
     * @throws TagLostException if the tag leaves the field
     * @throws IOException if there is an I/O failure, or the operation is canceled
     */
    @Override
    public void increment(int blockIndex, int value) throws IOException {
        validateBlock(blockIndex);
        validateValueOperand(value);
        delegate.checkConnected();

        ByteBuffer cmd = ByteBuffer.allocate(6);
        cmd.order(ByteOrder.LITTLE_ENDIAN);
        cmd.put( (byte) 0xC1 );
        cmd.put( (byte) blockIndex );
        cmd.putInt(value);

        delegate.transceive(cmd.array(), false);
    }

    /**
     * Decrement a value block, storing the result in the temporary block on the tag.
     *
     * <p>This is an I/O operation and will block until complete. It must
     * not be called from the main application thread. A blocked call will be canceled with
     * {@link IOException} if {@link #close} is called from another thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param blockIndex index of block to decrement, starting from 0
     * @param value non-negative to decrement by
     * @throws TagLostException if the tag leaves the field
     * @throws IOException if there is an I/O failure, or the operation is canceled
     */
    @Override
    public void decrement(int blockIndex, int value) throws IOException {
        validateBlock(blockIndex);
        validateValueOperand(value);
        delegate.checkConnected();

        ByteBuffer cmd = ByteBuffer.allocate(6);
        cmd.order(ByteOrder.LITTLE_ENDIAN);
        cmd.put( (byte) 0xC0 );
        cmd.put( (byte) blockIndex );
        cmd.putInt(value);

        delegate.transceive(cmd.array(), false);
    }

    /**
     * Copy from the temporary block to a value block.
     *
     * <p>This is an I/O operation and will block until complete. It must
     * not be called from the main application thread. A blocked call will be canceled with
     * {@link IOException} if {@link #close} is called from another thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param blockIndex index of block to copy to
     * @throws TagLostException if the tag leaves the field
     * @throws IOException if there is an I/O failure, or the operation is canceled
     */
    @Override
    public void transfer(int blockIndex) throws IOException {
        validateBlock(blockIndex);
        delegate.checkConnected();

        byte[] cmd = { (byte) 0xB0, (byte) blockIndex };

        delegate.transceive(cmd, false);
    }

    /**
     * Copy from a value block to the temporary block.
     *
     * <p>This is an I/O operation and will block until complete. It must
     * not be called from the main application thread. A blocked call will be canceled with
     * {@link IOException} if {@link #close} is called from another thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param blockIndex index of block to copy from
     * @throws TagLostException if the tag leaves the field
     * @throws IOException if there is an I/O failure, or the operation is canceled
     */
    @Override
    public void restore(int blockIndex) throws IOException {
        validateBlock(blockIndex);
        delegate.checkConnected();

        byte[] cmd = { (byte) 0xC2, (byte) blockIndex };

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
            int err = delegate.getTag().getTagService().setTimeout(TagTechnology.MIFARE_CLASSIC, timeout);
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
            return delegate.getTag().getTagService().getTimeout(TagTechnology.MIFARE_CLASSIC);
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return 0;
        }
    }

    private static void validateSector(int sector) {
        // Do not be too strict on upper bounds checking, since some cards
        // have more addressable memory than they report. For example,
        // MIFARE Plus 2k cards will appear as MIFARE Classic 1k cards when in
        // MIFARE Classic compatibility mode.
        // Note that issuing a command to an out-of-bounds block is safe - the
        // tag should report error causing IOException. This validation is a
        // helper to guard against obvious programming mistakes.
        if (sector < 0 || sector >= MAX_SECTOR_COUNT) {
            throw new IndexOutOfBoundsException("sector out of bounds: " + sector);
        }
    }

    private static void validateBlock(int block) {
        // Just looking for obvious out of bounds...
        if (block < 0 || block >= MAX_BLOCK_COUNT) {
            throw new IndexOutOfBoundsException("block out of bounds: " + block);
        }
    }

    private static void validateValueOperand(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("value operand negative");
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
