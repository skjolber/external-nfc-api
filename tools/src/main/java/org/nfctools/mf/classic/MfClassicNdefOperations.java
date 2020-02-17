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
package org.nfctools.mf.classic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.ndeftools.Message;
import org.nfctools.NfcException;
import org.nfctools.api.TagInfo;
import org.nfctools.mf.MfConstants;
import org.nfctools.mf.block.TrailerBlock;
import org.nfctools.mf.mad.Application;
import org.nfctools.mf.mad.ApplicationDirectory;
import org.nfctools.mf.mad.MadKeyConfig;
import org.nfctools.mf.ndef.AbstractNdefOperations;
import org.nfctools.mf.tlv.NdefMessageTlv;
import org.nfctools.mf.tlv.Tlv;
import org.nfctools.mf.tlv.TypeLengthValueReader;
import org.nfctools.mf.tlv.TypeLengthValueWriter;
import org.nfctools.tags.TagInputStream;
import org.nfctools.tags.TagOutputStream;

import android.nfc.FormatException;

public class MfClassicNdefOperations extends AbstractNdefOperations {

	private static final String TAG = MfClassicNdefOperations.class.getName();
	
	private MfClassicReaderWriter readerWriter;
	private byte[] writeKey = MfConstants.NDEF_KEY;
	private TagInfo tagInfo;

	public MfClassicNdefOperations(MfClassicReaderWriter readerWriter, TagInfo tagInfo, boolean formatted, boolean writeable) {
		super(formatted, writeable);
		this.readerWriter = readerWriter;
		this.tagInfo = tagInfo;
	}
	
	public MfClassicReaderWriter getReaderWriter() {
		return readerWriter;
	}
	
	public TagInfo getTagInfo() {
		return tagInfo;
	}

	@Override
	public int getMaxSize() {
		assertFormatted();
		return getApplication().getAllocatedSize();
	}

	
	@Override
	public Message readNdefMessage() throws FormatException {
		assertFormatted();
		if (lastReadRecords != null) {
			return lastReadRecords;
		}
		else {
			try {
				Application application = getApplication();
				// TODO create TagInputStream for better performance
				byte[] tlvWrappedNdefMessage = application.read(new KeyValue(Key.A, MfConstants.NDEF_KEY));
				
				TypeLengthValueReader reader = new TypeLengthValueReader(
						new ByteArrayInputStream(tlvWrappedNdefMessage));
				convertRecords(reader);
				return lastReadRecords;
			}
			catch (IOException e) {
				throw new NfcException(e);
			}
		}
	}

	private Application getApplication() {
		try {
			ApplicationDirectory applicationDirectory = readerWriter.getApplicationDirectory(new MadKeyConfig(Key.A,
					writeKey, writeKey));
			Application application = applicationDirectory.openApplication(MfConstants.NDEF_APP_ID);
			return application;
		}
		catch (IOException e) {
			throw new NfcException(e);
		}
	}

	@Override
	public void writeNdefMessage(Message message) {
		assertWritable();
		assertFormatted();
		writeMessage(getApplication(), message);
	}

	@Override
	public void makeReadOnly() {
		assertFormatted();
		assertWritable();
		try {
			Application application = getApplication();
			application.getApplicationDirectory().makeReadOnly();
			application.makeReadOnly(new KeyValue(Key.B, writeKey));
			writable = false;
		}
		catch (IOException e) {
			throw new NfcException(e);
		}
	}

	@Override
	public void format(Message message) {
		Application application = createNewApplication();
		writeMessage(application, message);
		formatted = true;
	}

	private Application createNewApplication() {
		try {
			ApplicationDirectory applicationDirectory = readerWriter.createApplicationDirectory(new MadKeyConfig(Key.A,
					MfConstants.TRANSPORT_KEY, writeKey));
			TrailerBlock readWriteTrailerBlock = ClassicHandler.createReadWriteDataTrailerBlock();
			readWriteTrailerBlock.setKey(Key.A, writeKey);
			readWriteTrailerBlock.setKey(Key.B, writeKey);
			Application application = applicationDirectory.createApplication(MfConstants.NDEF_APP_ID,
					applicationDirectory.getMaxContinousSize(), writeKey, readWriteTrailerBlock);
			return application;
		}
		catch (IOException e) {
			throw new NfcException(e);
		}
	}

	private void writeMessage(Application application, Message message) {
		try {
			byte[] tlvWrappedNdefData = wrapNdefMessageWithTlv(convertRecordsToBytes(message),
					application.getAllocatedSize());
			application.write(new KeyValue(Key.B, writeKey), tlvWrappedNdefData);
		}
		catch (IOException e) {
			throw new NfcException(e);
		}
	}

	private byte[] wrapNdefMessageWithTlv(byte[] ndefMessage, int maxSize) {
		TagOutputStream out = new TagOutputStream(maxSize);
		TypeLengthValueWriter writer = new TypeLengthValueWriter(out);
		writer.write(new NdefMessageTlv(ndefMessage));
		writer.close();
		return out.getBuffer();
	}

	public byte[] readNdefMessageBytes() throws IOException {
		assertFormatted();
		Application application = getApplication();
		
		byte[] tlvWrappedNdefMessage = application.read(new KeyValue(Key.A, MfConstants.NDEF_KEY));
		
		TypeLengthValueReader reader = new TypeLengthValueReader(new ByteArrayInputStream(tlvWrappedNdefMessage));
		
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
	public byte[] getUid() {
		return tagInfo.getId();
	}
}
