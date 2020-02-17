
package org.nfctools.mf.ndef;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.ndeftools.Message;
import org.ndeftools.Record;
import org.nfctools.mf.tlv.NdefMessageTlv;
import org.nfctools.mf.tlv.Tlv;
import org.nfctools.mf.tlv.TypeLengthValueReader;

import com.github.skjolber.nfc.skjolberg.reader.operations.NdefOperations;

import android.nfc.FormatException;
import android.nfc.NdefMessage;

public abstract class AbstractNdefOperations implements NdefOperations {

	private static final String TAG = AbstractNdefOperations.class.getName();
	
	protected byte[] EMPTY = new byte[]{};

    private static final byte FLAG_MB = (byte) 0x80;
    private static final byte FLAG_ME = (byte) 0x40;
    private static final byte FLAG_SR = (byte) 0x10;
    private static final byte FLAG_IL = (byte) 0x08;

	protected boolean formatted;
	protected boolean writable;
	protected Message lastReadRecords;

	protected AbstractNdefOperations(boolean formatted, boolean writable) {
		this.formatted = formatted;
		this.writable = writable;
	}

	@Override
	public boolean hasNdefMessage() throws FormatException {
		if (lastReadRecords != null && !lastReadRecords.isEmpty())
			return true;

		Collection<Record> ndefMessage = readNdefMessage();
		return !ndefMessage.isEmpty();
	}

	@Override
	public boolean isFormatted() {
		return formatted;
	}

	@Override
	public boolean isWritable() {
		return writable;
	}

	@Override
	public void format() {
		format(new Message());
	}

	@Override
	public void formatReadOnly(Message message) {
		format(message);
		makeReadOnly();
	}

	protected void assertWritable() {
		if (!writable)
			throw new IllegalStateException("Tag not writable");
	}

	protected void assertFormatted() {
		if (!formatted)
			throw new IllegalStateException("Tag not formatted");
	}

	protected byte[] convertRecordsToBytes(Message message) {
		if(message.isEmpty()) {
			return new byte[]{};
		}
		
		return message.getNdefMessage().toByteArray();
	}

	protected void convertRecords(TypeLengthValueReader reader) throws FormatException {
		lastReadRecords = new Message();

		while (reader.hasNext()) {
			Tlv tlv = reader.next();
			if (tlv instanceof NdefMessageTlv) {
				
				byte[] payload = ((NdefMessageTlv)tlv).getNdefMessage();
				
				if(payload.length > 0) {
					normalizeMessageBeginEnd(payload);
					
					List<Record> records = new Message(new NdefMessage(payload));
					for (Record record : records) {
						lastReadRecords.add(record);
					}
				}
			}
		}
	}

	/**
	 * 
	 * Modify a sequence of records so that first message has message begin flag and the last message has message end flag.
     *
     * @param ndefMessage message to modify
     */
	
    protected static void normalizeMessageBeginEnd(byte[] ndefMessage) {
    	normalizeMessageBeginEnd(ndefMessage, 0, ndefMessage.length);
    }

	/**
	 * 
	 * Modify a sequence of records so that first message has message begin flag and the last message has message end flag.
	 * 
     * @param ndefMessage message to modify
     * @param offset start offset
     * @param length number of bytes
     */
		
    protected static void normalizeMessageBeginEnd(byte[] ndefMessage, int offset, int length) {
    	// normalize message begin and message end messages

    	int count = offset;
    	while(count < offset + length) {
    		int headerCount = count;
    		int header = (ndefMessage[count++] & 0xff);
    		if (count >= offset + length) {
    			return; // invalid, defer error to NdefMessage parsing
    		}

    		int typeLength = (ndefMessage[count++] & 0xff);
    		if (count >= offset + length) {
    			return;  // invalid, defer error to NdefMessage parsing
    		}

    		int payloadLength;
    		if((header & FLAG_SR) != 0) {
    			payloadLength = (ndefMessage[count++] & 0xff);
    			if (count >= offset + length) {
    				return;  // invalid, defer error to NdefMessage parsing
    			}
    		} else {
    			if (count + 4 >= offset + length) {
    				return;  // invalid, defer error to NdefMessage parsing
    			}
    			payloadLength = (((ndefMessage[count] & 0xff) << 24) + ((ndefMessage[count + 1]  & 0xff) << 16) + ((ndefMessage[count + 2]  & 0xff) << 8) + ((ndefMessage[count+3]  & 0xff) << 0)); // strictly speaking this is a unsigned int

    			count += 4;
    		}

    		if((header & FLAG_IL) != 0) {
        		count += typeLength + payloadLength + (ndefMessage[count++] & 0xff);
    		} else {
        		count += typeLength + payloadLength;
    		}

    		// repair mb and me
    		if(headerCount == offset) {
    			// mb
    			header = header | FLAG_MB;
    		} else {
    			header = header & ~FLAG_MB;
    		}

    		if(count == offset + length) {
    			// me
    			header = header | FLAG_ME;
    		} else {
    			header = header & ~FLAG_ME;
    		}

			ndefMessage[headerCount] = (byte)header;
    	}
    }

	public abstract byte[] readNdefMessageBytes() throws IOException;

	public abstract byte[] getUid();


}
