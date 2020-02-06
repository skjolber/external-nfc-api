/**
 * Copyright 2011-2012 Adrian Stabiszewski, as@nfctools.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skjolberg.reader.operations;

import org.ndeftools.Message;

import android.nfc.FormatException;

public interface NdefOperations {

    /**
     * Get the maximum NDEF message size in bytes.
     */
    int getMaxSize();

    boolean hasNdefMessage() throws FormatException;

    boolean isFormatted();

    /**
     * Determine if the tag is writable.
     */
    boolean isWritable();

    /**
     * Read the current NdefMessage on this tag.
     * @throws FormatException
     */
    Message readNdefMessage() throws FormatException;

    /**
     * Overwrite the NdefMessage on this tag.
     */
    void writeNdefMessage(Message message);

    /**
     * Make a tag read-only.
     */
    void makeReadOnly();

    /**
     * Format a tag as NDEF.
     */
    void format();

    /**
     * Format a tag as NDEF, and write a NdefMessage.
     */
    void format(Message message);

    /**
     * Formats a tag as NDEF, write a NdefMessage, and make read-only.
     */
    void formatReadOnly(Message message);
}
