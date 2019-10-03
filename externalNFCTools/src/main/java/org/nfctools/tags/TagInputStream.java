/**
 * Copyright 2011-2012 Adrian Stabiszewski, as@nfctools.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nfctools.tags;

import android.util.Log;

import com.acs.smartcard.ReaderException;

import java.io.IOException;
import java.io.InputStream;

import org.nfctools.NfcException;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.MemoryLayout;
import org.nfctools.mf.ul.MfUlReaderWriter;

public class TagInputStream extends InputStream {

    private static final String TAG = TagInputStream.class.getName();

	private MemoryLayout memoryLayout;
	private MfUlReaderWriter readerWriter;

	private byte[] currentBlock;
	private int currentPage;
	private int currentByte;

	public TagInputStream(MemoryLayout memoryLayout, MfUlReaderWriter readerWriter) {
		this.memoryLayout = memoryLayout;
		this.readerWriter = readerWriter;
		currentPage = memoryLayout.getFirstDataPage();
	}

	private void readNextBlock() {
		try {
            //Log.d(TAG, "Read page " + currentPage);

            MfBlock[] blocks = readerWriter.readBlock(currentPage, 1);
			currentBlock = blocks[0].getData();
			currentPage++;
			currentByte = 0;
		}
		catch (IOException e) {
			throw new NfcException(e);
		} catch (ReaderException e) {
            throw new NfcException(e);
        }
    }

    @Override
	public int available() throws IOException {
		if (currentBlock == null) {
			return memoryLayout.getMaxSize();
		}
		else {
			return getRemainigPages() * memoryLayout.getBytesPerPage()  + (currentBlock.length - currentByte);
		}
	}

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {

        // flush buffered data first
        if(currentByte < currentBlock.length) {
            int remainder = currentBlock.length - currentByte;

            int read = Math.min(remainder, length);

            System.arraycopy(currentBlock, currentByte, buffer, offset, read);

            currentByte += read;

            return read;
        }

        int remainigPages = getRemainigPages();
        if(remainigPages <= 0) {
            return -1;
        }
        int readLength = Math.min(length, available());

        int requestedPages = readLength / memoryLayout.getBytesPerPage();
        if(readLength % memoryLayout.getBytesPerPage() != 0) {
            requestedPages++;
        }

        int pagesToRead = Math.min(requestedPages, remainigPages);

        try {
            //Log.d(TAG, "Read page " + currentPage + "-" + (currentPage + pagesToRead - 1));
            MfBlock[] mfBlocks = readerWriter.readBlock(currentPage, pagesToRead);

            currentBlock = new byte[mfBlocks.length * memoryLayout.getBytesPerPage()];
            for(int i = 0; i < mfBlocks.length; i++) {
                System.arraycopy(mfBlocks[i].getData(), 0, currentBlock, i * memoryLayout.getBytesPerPage(), memoryLayout.getBytesPerPage());
            }

            currentByte = 0;
            currentPage += pagesToRead;

            return read(buffer, offset, length);
        }
        catch (IOException e) {
            throw new NfcException(e);
        } catch (ReaderException e) {
            throw new NfcException(e);
        }
    }

    @Override
	public int read() throws IOException {
		if (currentBlock == null || currentByte >= currentBlock.length) {
			if (available() > 0)
				readNextBlock();
			else
				return -1;
		}

		byte returnByte = currentBlock[currentByte++];
		return returnByte & 0xFF;
	}

    public int getRemainigPages() {
        return (memoryLayout.getLastDataPage() - currentPage + 1);
    }
}
