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
package org.nfctools.api;

import java.util.Arrays;

import android.util.Log;

import com.skjolberg.nfc.command.Utils;

public enum TagType {

	/**
	 * Unkown tag
	 */
	UNKNOWN("Unknown"),
	/**
	 * Mifare Classic with 1k memory
	 */
	MIFARE_CLASSIC_1K("Mifare Classic 1K"),

	/**
	 * Mifare Classic with 4k memory
	 */
	MIFARE_CLASSIC_4K("Mifare Classic 4K"), 
	
	MIFARE_PLUS_SL1_2K("Mifar Plus SL1 2K"),
	MIFARE_PLUS_SL1_4K("Mifar Plus SL1 4K"),
	MIFARE_PLUS_SL2_2K("Mifar Plus SL2 2K"),
	MIFARE_PLUS_SL2_4K("Mifar Plus SL2 4K"),
	
	MIFARE_ULTRALIGHT("Mifare Ultralight"), 

	MIFARE_ULTRALIGHT_C("Mifare Ultralight C"), 

	MIFARE_MINI("Mifare Mini"), 
	
	TOPAZ_JEWEL("Topaz Jewel"), 
	
	FELICA("FeliCa"), FELICA_212K("FeliCa 212K"), FELICA_424K("FeliCa 424K"),
	/**
	 * Tag with NFCIP (P2P) capabilities
	 */
	NFCIP("P2NFCIP"),
	
	DESFIRE_EV1("DESfire EV1"),
	
	ISO_DEP("ISO_DEP"),
	
	ISO_14443_TYPE_B_NO_HISTORICAL_BYTES("ISO 14443 Type B without historical bytes"), 
	
	ISO_14443_TYPE_A_NO_HISTORICAL_BYTES("RFID - ISO 14443 Type A - NXP DESFire or DESFire EV1"),
	
	MIFARE_PLUS_SL1_2k("Mifare Plus SL1 2K"), 
	
	MIFARE_PLUS_SL1_4k("Mifare Plus SL1 4K"), 
	
	MIFARE_PLUS_SL2_2k("Mifare Plus SL2 2K"), 
	
	MIFARE_PLUS_SL2_4k("Mifare Plus SL2 4K"),

	INFINEON_MIFARE_SLE_1K("Infineon Mifare SLE 66R35"),

	;
	
	private final String name;
	
	private TagType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	private static final String TAG = TagType.class.getName();
	
	public static TagType identifyTagType(byte[] historicalBytes) {
		TagType tagType = TagType.UNKNOWN;
		if (historicalBytes.length >= 11) {
			//Log.d(TAG, Utils.toHexString(historicalBytes));

			int tagId = (historicalBytes[13] & 0xff) << 8 | (historicalBytes[14] & 0xff);

			switch (tagId) {
				case 0x0001:
					return TagType.MIFARE_CLASSIC_1K;
				case 0x0002:
					return TagType.MIFARE_CLASSIC_4K;
				case 0x0003:
					return TagType.MIFARE_ULTRALIGHT;
				case 0x0026:
					return TagType.MIFARE_MINI;
				case 0xF004:
					return TagType.TOPAZ_JEWEL;
				case 0xF011:
					return TagType.FELICA_212K;
				case 0xF012:
					return TagType.FELICA_424K;
				case 0xFF40:
					return TagType.NFCIP;
				case 0xFF88:
					return TagType.INFINEON_MIFARE_SLE_1K;
				default :
				{
					Log.w(TAG, "Unknown tag id " + Utils.toHexString(new byte[]{historicalBytes[13], historicalBytes[14]}) + " (" + Integer.toHexString(tagId) + ")");
				}
			}
		} else if(Arrays.equals(historicalBytes, new byte[]{0x3B, (byte) 0x81, (byte) 0x80, 0x01, (byte) 0x80, (byte)0x80})) {
			return TagType.DESFIRE_EV1;
		} else if(Arrays.equals(historicalBytes, new byte[]{0x3B, (byte) 0x80, (byte) 0x80, 0x01, 0x01})) {
			return TagType.ISO_14443_TYPE_B_NO_HISTORICAL_BYTES;
		} else if(Arrays.equals(historicalBytes, new byte[]{0x3B, (byte) 0x81, (byte) 0x80, 0x01, (byte) 0x80, (byte) 0x80})) {
			return TagType.ISO_14443_TYPE_A_NO_HISTORICAL_BYTES;
			
			
		} else {
			Log.d(TAG, Utils.convertBinToASCII(historicalBytes));
		}
		
		return tagType;
	}


	
}
