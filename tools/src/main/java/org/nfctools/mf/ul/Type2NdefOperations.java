package org.nfctools.mf.ul;
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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.ndeftools.Message;
import org.ndeftools.Record;
import org.nfctools.NfcException;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ndef.AbstractNdefOperations;
import org.nfctools.mf.tlv.NdefMessageTlv;
import org.nfctools.mf.tlv.Tlv;
import org.nfctools.mf.tlv.TypeLengthValueReader;
import org.nfctools.mf.tlv.TypeLengthValueWriter;
import org.nfctools.tags.TagInputStream;
import org.nfctools.tags.TagOutputStream;

import android.nfc.FormatException;

import com.acs.smartcard.ReaderException;

public class Type2NdefOperations extends AbstractNdefOperations {
	
	private MemoryLayout memoryLayout;
	private MfUlReaderWriter readerWriter;
	private byte[] uid;
	
	public Type2NdefOperations(MemoryLayout memoryLayout, MfUlReaderWriter readerWriter, boolean formatted, boolean writable, byte[] uid) {
		super(formatted, writable);
		this.memoryLayout = memoryLayout;
		this.readerWriter = readerWriter;

		this.uid = uid;
	}

	public byte[] getUid() {
		return uid;
	}

	public void setUid(byte[] uid) {
		this.uid = uid;
	}

	public MemoryLayout getMemoryLayout() {
		return memoryLayout;
	}

	@Override
	public int getMaxSize() {
		return memoryLayout.getMaxSize();
	}
	
	public MfUlReaderWriter getReaderWriter() {
		return readerWriter;
	}
	
	public byte[] readNdefMessageBytes() {
		assertFormatted();
		TypeLengthValueReader reader = new TypeLengthValueReader(new TagInputStream(memoryLayout, readerWriter));
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		while (reader.hasNext()) {
			Tlv tlv = reader.next();
			if (tlv instanceof NdefMessageTlv) {
				
				byte[] payload = ((NdefMessageTlv)tlv).getNdefMessage();
				
				if(payload.length > 0) {
					try {
						bout.write(payload);
					} catch (IOException e) {
						// will never happen
					}
				}
			}
		}
		return bout.toByteArray();
	}


	@Override
	public Message readNdefMessage() throws FormatException {
		assertFormatted();
		if (lastReadRecords != null) {
			return lastReadRecords;
		}
		else {
			TypeLengthValueReader reader = new TypeLengthValueReader(new TagInputStream(memoryLayout, readerWriter));
			convertRecords(reader);
			return lastReadRecords;
		}
	}

	@Override
	public void writeNdefMessage(Message message) {
		lastReadRecords = null;
		assertWritable();
		assertFormatted();
		byte[] bytes = convertNdefMessage(message);
		writeBufferOnTag(bytes);
	}
	
	public void writeNdefMessage(byte[] ndefMessageBytes) {
		lastReadRecords = null;
		assertWritable();
		assertFormatted();
		byte[] bytes = convertNdefMessage(ndefMessageBytes);
		writeBufferOnTag(bytes);
	}

	@Override
	public void makeReadOnly() {
		assertWritable();
		assertFormatted();
		setLockBytes();
		writable = false;
	}

	@Override
	public void format(Message message) {
		try {
			formatCapabilityBlock();
			writeNdefMessage(message);
		}
		catch (IOException e) {
			throw new NfcException(e);
		}
	}

	private byte[] convertNdefMessage(Message message) {
		return convertNdefMessage(convertRecordsToBytes(message));
	}
	
	private byte[] convertNdefMessage(byte[] bytes) {
		TagOutputStream out = new TagOutputStream(getMaxSize());
		TypeLengthValueWriter writer = new TypeLengthValueWriter(out);
		if (memoryLayout.hasDynamicLockBytes()) {
			writer.write(memoryLayout.createLockControlTlv());
		}
		writer.write(new NdefMessageTlv(bytes));
		writer.close();
		return out.getBuffer();
	}

	private void writeBufferOnTag(byte[] buffer) {
		assertWritable();
		assertFormatted();
		try {
			int offset = 0;
			for (int page = memoryLayout.getFirstDataPage(); page <= memoryLayout.getLastDataPage(); page++) {
				DataBlock block = new DataBlock(buffer, offset);
				readerWriter.writeBlock(page, block);
				offset += memoryLayout.getBytesPerPage();
			}
		}
		catch (IOException e) {
			throw new NfcException(e);
		}
	}

	private void formatCapabilityBlock() throws IOException {
		assertWritable();
		CapabilityBlock block = memoryLayout.createCapabilityBlock();
		readerWriter.writeBlock(memoryLayout.getCapabilityPage(), block);
		formatted = true;
	}

	private void setLockBytes() {
		try {
			for (LockPage lockPage : memoryLayout.getLockPages()) {
				MfBlock[] block = readerWriter.readBlock(lockPage.getPage(), 1);

				for (int lockByte : lockPage.getLockBytes()) {
					block[0].getData()[lockByte] = (byte)0xff;
				}
				readerWriter.writeBlock(lockPage.getPage(), block);
			}
			MfBlock[] readBlock = readerWriter.readBlock(memoryLayout.getCapabilityPage(), 1);
			CapabilityBlock capabilityBlock = new CapabilityBlock(readBlock[0].getData());
			capabilityBlock.setReadOnly();
			readerWriter.writeBlock(memoryLayout.getCapabilityPage(), capabilityBlock);
		}
		catch (IOException e) {
			throw new NfcException(e);
		} catch (ReaderException e) {
            throw new NfcException(e);
        }
    }
}
