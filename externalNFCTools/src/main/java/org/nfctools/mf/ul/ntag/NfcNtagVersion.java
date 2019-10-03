package org.nfctools.mf.ul.ntag;

/*
 * *#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*
 * SMARTRAC SDK for Android NFC NTAG
 * ===============================================================================
 * Copyright (C) 2016 SMARTRAC TECHNOLOGY GROUP
 * ===============================================================================
 * SMARTRAC SDK
 * (C) Copyright 2016, Smartrac Technology Fletcher, Inc.
 * 267 Cane Creek Rd, Fletcher, NC, 28732, USA
 * All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#*#
 */


public class NfcNtagVersion {

    public static NfcNtagVersion fromGetVersion(byte[] getVersionBytes) {
        NfcNtagVersion nfcNtagVersion = new NfcNtagVersion(getVersionBytes);
        return nfcNtagVersion;
    }

    public NfcNtagVersion(byte[] versionBytes) {
        name = null;
        memorySize = 0;
        if (versionBytes != null)
        {
            if ((versionBytes[1] == VENDOR_NXP) && (versionBytes[2] == PROD_NTAG) &&
                    (versionBytes.length == 8)) {
                switch (versionBytes[3]) {
                    case SUBTYPE_NTAG:
                    switch (versionBytes[6]) {
                        case STORAGE_NTAG210:
                            memorySize = SIZE_NTAG210;
                            name = NTAG210;
                            type = TYPE_NTAG210;
                            break;
                        case STORAGE_NTAG212:
                            memorySize = SIZE_NTAG212;
                            name = NTAG212;
                            type = TYPE_NTAG212;
                            break;
                        case STORAGE_NTAG213:
                            memorySize = SIZE_NTAG213;
                            name = NTAG213;
                            type = TYPE_NTAG213;
                            break;
                        case STORAGE_NTAG215:
                            memorySize = SIZE_NTAG215;
                            name = NTAG215;
                            type = TYPE_NTAG215;
                            break;
                        case STORAGE_NTAG216:
                            memorySize = SIZE_NTAG216;
                            name = NTAG216;
                            type = TYPE_NTAG216;
                            break;
                        default:
                            memorySize = SIZE_UNKNOWN;
                            name = UNKNOWN;
                            type = TYPE_UNKNOWN;
                            break;
                        }
                        break;
                    case SUBTYPE_NTAG_F:
                        switch (versionBytes[6]) {
                            case STORAGE_NTAG213:
                                memorySize = SIZE_NTAG213;
                                name = NTAG213F;
                                type = TYPE_NTAG213F;
                                break;
                            case STORAGE_NTAG216:
                                memorySize = SIZE_NTAG216;
                                name = NTAG216F;
                                type = TYPE_NTAG216F;
                                break;
                            default:
                                memorySize = SIZE_UNKNOWN;
                                name = UNKNOWN;
                                type = TYPE_UNKNOWN;
                                break;
                        }
                        break;
                    case SUBTYPE_NTAG_I2C:
                        switch (versionBytes[6]) {
                            case STORAGE_NTAG1K:
                                memorySize = SIZE_NTAG1K;
                                if (versionBytes[5] > 1) {
                                    name = NTAGI2CP1K;
                                    type = TYPE_NTAGI2CP1K;
                                } else {
                                    name = NTAGI2C1K;
                                    type = TYPE_NTAGI2C1K;
                                }
                                break;
                            case STORAGE_NTAG2K:
                                memorySize = SIZE_NTAG2K;
                                if (versionBytes[5] > 1) {
                                    name = NTAGI2CP2K;
                                    type = TYPE_NTAGI2CP2K;
                                } else {
                                    name = NTAGI2C2K;
                                    type = TYPE_NTAGI2C2K;
                                }
                                break;
                            default:
                                memorySize = SIZE_UNKNOWN;
                                name = UNKNOWN;
                                type = TYPE_UNKNOWN;
                                break;
                        }
                        break;
                    default:
                        memorySize = SIZE_UNKNOWN;
                        name = UNKNOWN;
                        type = TYPE_UNKNOWN;
                        break;
                }
            }
        }
    }

    public boolean isNTAG213() {
        return type == TYPE_NTAG213 || type == TYPE_NTAG213F;
    }

    public boolean isNTAG215() {
        return type == TYPE_NTAG215;
    }

    public boolean isNTAG212() {
        return type == TYPE_NTAG212;
    }

    public boolean isNTAG210() {
        return type == TYPE_NTAG210;
    }

    public boolean isNTAG216() {
        return type == TYPE_NTAG216 || type == TYPE_NTAG216F;
    }

    public int getMemorySize() {
        return memorySize;
    }

    @Override
    public String toString() {
        return name;
    }

    static final byte   VENDOR_NXP = 0x04;
    static final byte   PROD_NTAG = 0x04;
    static final byte   SUBTYPE_NTAG = 0x02;
    static final byte   SUBTYPE_NTAG_F = 0x04;
    static final byte   SUBTYPE_NTAG_I2C = 0x05;
    static final byte   STORAGE_NTAG210 = 0x0B;
    static final byte   STORAGE_NTAG212 = 0x0E;
    static final byte   STORAGE_NTAG213 = 0x0F;
    static final byte   STORAGE_NTAG215 = 0x11;
    static final byte   STORAGE_NTAG216 = 0x13;
    static final byte   STORAGE_NTAG1K = STORAGE_NTAG216;
    static final byte   STORAGE_NTAG2K = 0x15;
    static final int    SIZE_NTAG210 = 48;
    static final int    SIZE_NTAG212 = 128;
    static final int    SIZE_NTAG213 = 144;
    static final int    SIZE_NTAG215 = 504;
    static final int    SIZE_NTAG216 = 888;
    static final int    SIZE_NTAG1K = SIZE_NTAG216;
    static final int    SIZE_NTAG2K = 1904;
    static final int    SIZE_UNKNOWN = 0;

    public static final String NTAG210 = "NTAG 210";
    public static final String NTAG212 = "NTAG 212";
    public static final String NTAG213 = "NTAG 213";
    public static final String NTAG215 = "NTAG 215";
    public static final String NTAG216 = "NTAG 216";
    public static final String NTAG213F = "NTAG 213F";
    public static final String NTAG216F = "NTAG 216F";
    public static final String NTAGI2C1K = "NTAG I2C 1K";
    public static final String NTAGI2C2K = "NTAG I2C 2K";
    public static final String NTAGI2CP1K = "NTAG I2C plus 1K";
    public static final String NTAGI2CP2K = "NTAG I2C plus 2K";
    public static final String UNKNOWN = "unknown chip";

    public static final int TYPE_NTAG210 = 1;
    public static final int TYPE_NTAG212 = 2;
    public static final int TYPE_NTAG213 = 3;
    public static final int TYPE_NTAG215 = 4;
    public static final int TYPE_NTAG216 = 5;
    public static final int TYPE_NTAG213F = 6;
    public static final int TYPE_NTAG216F = 7;
    public static final int TYPE_NTAGI2C1K = 8;
    public static final int TYPE_NTAGI2C2K = 9;
    public static final int TYPE_NTAGI2CP1K = 10;
    public static final int TYPE_NTAGI2CP2K = 11;
    public static final int TYPE_UNKNOWN = 0;

    private int memorySize;
    private String name;
    private int type;

    public int getType() {
        return type;
    }

    public NfcNtagVersion(int type, String name, int memorySize) {
        this.type = type;
        this.name = name;
        this.memorySize = memorySize;
    }

}
