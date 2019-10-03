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


import org.nfctools.NfcException;
import org.nfctools.mf.tlv.LockControlTlv;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;

public class MemoryLayout {

    /** A MIFARE Ultralight compatible tag of unknown type */
    public static final int TYPE_UNKNOWN = -1;
    /** A MIFARE Ultralight tag */
    public static final int TYPE_ULTRALIGHT = 1;
    /** A MIFARE Ultralight C tag */
    public static final int TYPE_ULTRALIGHT_C = 2;

    public static final int TYPE_NTAG_203 = 2;
    public static final int TYPE_NTAG_210 = 3;
    public static final int TYPE_NTAG_212 = 4;
    public static final int TYPE_NTAG_213 = 5;
    public static final int TYPE_NTAG_215 = 6;
    public static final int TYPE_NTAG_216 = 7;

    // int type, LockPage[] lockPages, int firstPage, int lastPage, int firstDataPage, int lastDataPage

	public static final MemoryLayout ULTRALIGHT = new MemoryLayout(TYPE_ULTRALIGHT, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }) }, 0, 15, 4, 15);

    // 144 - 0x12
	public static final MemoryLayout NTAG203 = new MemoryLayout(TYPE_NTAG_203, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0x28, new byte[] { 0, 1 }) }, 0, 41, 4, 0x27);

    // 48 - 0x06
    public static final MemoryLayout NTAG210 = new MemoryLayout(TYPE_NTAG_210, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }) }, 0, 15, 4, 15);

    // 128 - 0x10
    public static final MemoryLayout NTAG212 = new MemoryLayout(TYPE_NTAG_212, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0x24, new byte[] { 0, 1, 2}) }, 0, 15, 4, 15);

    // 144 - 0x12
    public static final MemoryLayout NTAG213 = new MemoryLayout(TYPE_NTAG_213, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0x28, new byte[] { 0, 1, 2 }) }, 0, 40, 4, 0x23);

    public static final MemoryLayout NTAG213F = new MemoryLayout(TYPE_NTAG_213, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0x28, new byte[] { 0, 1, 2}) }, 0, 44, 4, 0x27);

    // 496 - 0x3E
    public static final MemoryLayout NTAG215 = new MemoryLayout(TYPE_NTAG_215, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0x82, new byte[] { 0, 1, 2}) }, 0, 134, 4, 0x81);

    // 872 - 0x6D
    public static final MemoryLayout NTAG216 = new MemoryLayout(TYPE_NTAG_216, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0xE2, new byte[] { 0, 1, 2 }) }, 0, 230, 4, 0xE1);

    // 888 - 0x6F
    public static final MemoryLayout NTAG216F = new MemoryLayout(TYPE_NTAG_216, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0xE2, new byte[] { 0, 1, 2 }) }, 0, 230, 4, 0xE1);

    public static final MemoryLayout ULTRALIGHT_C = new MemoryLayout(TYPE_ULTRALIGHT_C, new LockPage[] {new LockPage(2, new byte[] { 2, 3 }), new LockPage(40, new byte[] { 0, 1 }) }, 0, 47, 4, 39);

	private LockPage[] lockPages;
	private int firstPage;
	private int lastPage;
	private int firstDataPage;
	private int lastDataPage;
	private int bytesPerPage = 4;
	private int capabilityPage = 3;
	private boolean dynamicLockBytes;
	private int type;
	
	private MemoryLayout(int type, LockPage[] lockPages, int firstPage, int lastPage, int firstDataPage, int lastDataPage) {
		this.type = type;
		this.lockPages = lockPages;
		this.firstPage = firstPage;
		this.lastPage = lastPage;
		this.firstDataPage = firstDataPage;
		this.lastDataPage = lastDataPage;
		dynamicLockBytes = lockPages.length > 1;
	}

	public int getType() {
		return type;
	}
	
	public LockPage[] getLockPages() {
		return lockPages;
	}

	public int getFirstDataPage() {
		return firstDataPage;
	}

	public int getLastDataPage() {
		return lastDataPage;
	}

	public int getMaxSize() {
		return (lastDataPage - firstDataPage + 1) * bytesPerPage;
	}

	public int getBytesPerPage() {
		return bytesPerPage;
	}

	public int getCapabilityPage() {
		return capabilityPage;
	}

	public int getFirstPage() {
		return firstPage;
	}

	public int getLastPage() {
		return lastPage;
	}

	public CapabilityBlock createCapabilityBlock() {
		return new CapabilityBlock((byte)0x10, (byte)(getMaxSize() / 8), false);
	}

	public boolean hasDynamicLockBytes() {
		return dynamicLockBytes;
	}

	public LockControlTlv createLockControlTlv() {
		if (hasDynamicLockBytes()) {
			LockControlTlv tlv = new LockControlTlv();
			tlv.setPageAddress(10);
			tlv.setByteOffset(0);
			tlv.setSize(16);
			tlv.setBytesPerPage(bytesPerPage);
			tlv.setBytesLockedPerLockBit(4);
			return tlv;
		}
		else {
			return null;
		}
	}

    public static MemoryLayout getUltralightMemoryLayout(int version) {
/*

    // int type, LockPage[] lockPages, int firstPage, int lastPage, int firstDataPage, int lastDataPage

	public static final MemoryLayout ULTRALIGHT = new MemoryLayout(TYPE_ULTRALIGHT, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }) }, 0, 15, 4, 15);

    // 144 - 0x12
	public static final MemoryLayout NTAG203 = new MemoryLayout(TYPE_NTAG_203, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0x28, new byte[] { 0, 1 }) }, 0, 41, 4, 0x27);

    // 48 - 0x06
    public static final MemoryLayout NTAG210 = new MemoryLayout(TYPE_NTAG_210, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }) }, 0, 15, 4, 15);

    // 128 - 0x10
    public static final MemoryLayout NTAG212 = new MemoryLayout(TYPE_NTAG_212, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0x24, new byte[] { 0, 1, 2}) }, 0, 15, 4, 15);

    // 144 - 0x12
    public static final MemoryLayout NTAG213 = new MemoryLayout(TYPE_NTAG_213, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0x28, new byte[] { 0, 1, 2 }) }, 0, 40, 4, 0x23);

    public static final MemoryLayout NTAG213F = new MemoryLayout(TYPE_NTAG_213, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0x28, new byte[] { 0, 1, 2}) }, 0, 44, 4, 0x27);

    // 496 - 0x3E
    public static final MemoryLayout NTAG215 = new MemoryLayout(TYPE_NTAG_215, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0x82, new byte[] { 0, 1, 2}) }, 0, 134, 4, 0x81);

    // 872 - 0x6D
    public static final MemoryLayout NTAG216 = new MemoryLayout(TYPE_NTAG_216, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0xE2, new byte[] { 0, 1, 2 }) }, 0, 230, 4, 0xE1);

    // 888 - 0x6F
    public static final MemoryLayout NTAG216F = new MemoryLayout(TYPE_NTAG_216, new LockPage[] { new LockPage(2, new byte[] { 2, 3 }), new LockPage(0xE2, new byte[] { 0, 1, 2 }) }, 0, 230, 4, 0xE1);

    public static final MemoryLayout ULTRALIGHT_C = new MemoryLayout(TYPE_ULTRALIGHT_C, new LockPage[] {new LockPage(2, new byte[] { 2, 3 }), new LockPage(40, new byte[] { 0, 1 }) }, 0, 47, 4, 39);


*/

        if(version < 0) {
            switch(-version) {
                case NfcNtagVersion.TYPE_NTAG210: { //0x06
                    return MemoryLayout.ULTRALIGHT;
                }
                case NfcNtagVersion.TYPE_NTAG213 : { //0x12
                    return MemoryLayout.ULTRALIGHT_C;
                }
                default : {
                    throw new NfcException("Unknown tag type " + version);
                }
            }
        } else {
            switch(version) {
                case NfcNtagVersion.TYPE_NTAG210: { // 0x06
                    return MemoryLayout.NTAG210;
                }
                case NfcNtagVersion.TYPE_NTAG212: { // 0x10
                    return MemoryLayout.NTAG212;
                }
                case NfcNtagVersion.TYPE_NTAG213: { //0x12
                    return MemoryLayout.NTAG213;
                }
                case NfcNtagVersion.TYPE_NTAG215: { //0x3E
                    return MemoryLayout.NTAG215;
                }
                case NfcNtagVersion.TYPE_NTAG216 : { // 0x6D
                    return MemoryLayout.NTAG216;
                }
                case NfcNtagVersion.TYPE_NTAG216F : { // 0x6F
                    return MemoryLayout.NTAG216F;
                }
                default : {
                    throw new NfcException("Unknown tag type " + version);
                }
            }
        }
    }

	/*
	public void parseBlock0(byte[] data) {
		if (data != null && data.length == 16) { // check the Capability Container (CC bytes)
			Log.d("ChipMeta.parseBlock0", Util.ByteArrayToHexString(data));
			if (bitCompare(data[12], (byte) 0xe1)
					&& bitCompare(data[13], (byte) 0x10)
					&& bitCompare(data[14], (byte) 0x12)
					) {
				this.type = ATRHistorical.MIFARE_ULTRALIGHT_C;
			}
		}
	}
	*/
}
