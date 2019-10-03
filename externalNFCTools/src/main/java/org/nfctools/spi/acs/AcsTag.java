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


import org.nfctools.NfcException;
import org.nfctools.api.ApduTag;
import org.nfctools.api.Tag;
import org.nfctools.api.TagType;
import org.nfctools.scio.Command;
import org.nfctools.scio.Response;

import android.util.Log;

import com.skjolberg.nfc.command.ReaderWrapper;
import com.skjolberg.nfc.command.Utils;

import custom.java.CommandAPDU;
import custom.java.ResponseAPDU;


public class AcsTag extends Tag implements ApduTag {

	private static final String TAG = AcsTag.class.getName();
	
	private ReaderWrapper reader;
	private int slot;
	
	public AcsTag(TagType tagType, byte[] generalBytes, ReaderWrapper reader, int slot) {
		super(tagType, generalBytes);
		this.reader = reader;
		this.slot = slot;
	}


	public ReaderWrapper getReader() {
		return reader;
	}


	public void setReader(ReaderWrapper reader) {
		this.reader = reader;
	}


	public int getSlot() {
		return slot;
	}


	public void setSlot(int slot) {
		this.slot = slot;
	}


	@Override
	public Response transmit(Command command) {
		try {
			CommandAPDU commandAPDU = null;
			if (command.isDataOnly()) {
				commandAPDU = new CommandAPDU(0xff, 0, 0, 0, command.getData(), command.getOffset(),
						command.getLength());
			}
			else if (command.hasData()) {
				commandAPDU = new CommandAPDU(Apdu.CLS_PTS, command.getInstruction(), command.getP1(), command.getP2(),
						command.getData());
			}
			else {
				commandAPDU = new CommandAPDU(Apdu.CLS_PTS, command.getInstruction(), command.getP1(), command.getP2(),
						command.getLength());
			}
			byte[] out = commandAPDU.getBytes();
			
			//Log.d(TAG, "Request: " + Utils.toHexString(out));

			byte[] in = reader.transmit(slot, out);
			
			//Log.d(TAG, "Response: " + Utils.toHexString(in));
			
			ResponseAPDU responseAPDU = new ResponseAPDU(in);
			return new Response(responseAPDU.getSW1(), responseAPDU.getSW2(), responseAPDU.getData());
		}
		catch (Exception e) {
			throw new NfcException(e);
		}
	}


	@Override
	public byte[] transmit(byte[] request) {
		Log.d(TAG, "Raw request: " + Utils.toHexString(request));
		try {
			byte[] response =  reader.transmit(slot, request);
			
			Log.d(TAG, "Raw response: " + Utils.toHexString(response));

			return response;
		} catch (Exception e) {
			throw new NfcException(e);
		}
	}

    @Override
    public byte[] transmitPassthrough(byte[] in) {
	    try {
            return reader.transmitPassThrough(slot, in);
        } catch (Exception e) {
            throw new NfcException(e);
        }
	}


}

