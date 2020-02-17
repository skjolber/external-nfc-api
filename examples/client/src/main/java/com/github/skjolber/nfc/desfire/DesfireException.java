/***************************************************************************
 * 
 * This file is part of the 'External NFC API' project at
 * https://github.com/skjolber/external-nfc-api
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 ****************************************************************************/

package com.github.skjolber.nfc.desfire;

public class DesfireException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final byte status;
	
	public DesfireException(byte status) {
		super();
		
		this.status = status;
	}

	public DesfireException(byte status, String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		this.status = status;
	}

	public DesfireException(byte status, String detailMessage) {
		super(detailMessage);
		this.status = status;
	}

	public DesfireException(byte status, Throwable throwable) {
		super(throwable);
		this.status = status;
	}

	public byte getStatus() {
		return status;
	}
}
