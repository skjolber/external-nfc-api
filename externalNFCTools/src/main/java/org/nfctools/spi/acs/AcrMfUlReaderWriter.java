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
package org.nfctools.spi.acs;

import java.io.IOException;

import org.nfctools.api.ApduTag;
import org.nfctools.mf.MfException;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.DataBlock;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.scio.Command;
import org.nfctools.scio.Response;

import android.util.Log;

import com.acs.smartcard.ReaderException;

import custom.java.CommandAPDU;
import custom.java.ResponseAPDU;

public class AcrMfUlReaderWriter implements MfUlReaderWriter {

    private static final String TAG = AcrMfUlReaderWriter.class.getName();

	private ApduTag tag;

	public AcrMfUlReaderWriter(ApduTag tag) {
		this.tag = tag;
	}

    public byte[] transmit(byte[] data) throws ReaderException {
	    return tag.transmit(data);
    }

    public Response transmit(Command command) throws ReaderException {
        return tag.transmit(command);
    }

    @Override
	public MfBlock[] readBlock(int startPage, int pagesToRead) throws IOException {
		MfBlock[] returnBlocks = new MfBlock[pagesToRead];

        /*
        // max 4 pages per read using this command
		int reads = pagesToRead / 4;
		if(pagesToRead % 4 != 0) {
			reads++;
		}
        Log.d(TAG, "Read " + pagesToRead  + " pages in " + reads + " reads");
		for (int i = 0; i < reads; i++) {
			int range = Math.min(4, pagesToRead - (i * 4));
			//Command readBlock = new Command(Apdu.INS_READ_BINARY, 0x00, startPage + i * 4, range * 4);

            Log.d(TAG, "Read from page " + (startPage + i * 4) + " for " + (range * 4) + " bytes");

			CommandAPDU readBlock = new CommandAPDU(Apdu.CLS_PTS, Apdu.INS_READ_BINARY, 0x00, startPage + i * 4, range * 4);

			ResponseAPDU readBlockResponse = new ResponseAPDU(tag.transmit(readBlock.getBytes()));

			if (readBlockResponse.isFailure()) {
				throw new MfException("Reading " + range + " blocks failed. Page: " + (startPage + i * 4) + ", Response: " + readBlockResponse);
			}

			byte[] data = readBlockResponse.getData();

			for(int k = 0; k < range; k++) {
				byte[] block = new byte[4];
				System.arraycopy(data, k * 4, block, 0, 4);
				returnBlocks[(i * 4) + k] = new DataBlock(block);
			}
		}
		*/

		for (int currentPage = 0; currentPage < pagesToRead; currentPage++) {
			int pageNumber = startPage + currentPage;

			Command readBlock = new Command(Apdu.INS_READ_BINARY, 0x00, pageNumber, 4);
			Response readBlockResponse = tag.transmit(readBlock);
			
			if (readBlockResponse.isFailure()) {
				throw new MfException("Reading block failed. Page: " + pageNumber + ", Response: " + readBlockResponse);
			}
			
			returnBlocks[currentPage] = new DataBlock(readBlockResponse.getData());
		}
		return returnBlocks;
	}

	@Override
	public void writeBlock(int startPage, MfBlock... mfBlock) throws IOException {
		for (int currentBlock = 0; currentBlock < mfBlock.length; currentBlock++) {
			int blockNumber = startPage + currentBlock;

			Command writeBlock = new Command(Apdu.INS_UPDATE_BINARY, 0x00, blockNumber, mfBlock[currentBlock].getData());
			Response writeBlockResponse = tag.transmit(writeBlock);
			
			if (writeBlockResponse.isFailure()) {
				throw new MfException("Writing block failed. Page: " + blockNumber + ", Response: "
						+ writeBlockResponse);
			}
		}
	}

    @Override
    public int getMaxPagesPerRead() {
        return 1;
    }

    @Override
    public byte[] transmitPassthrough(byte[] data) {
        return tag.transmitPassthrough(data);
    }
}
