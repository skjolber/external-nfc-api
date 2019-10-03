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


public class NfcNtagOpcode {
    public static final byte GET_VERSION = 0x60;
    public static final byte READ = 0x30;
    public static final byte FAST_READ = 0x3A;
    public static final byte WRITE = (byte) 0xA2;
    public static final byte READ_CNT = 0x39;
    public static final byte PWD_AUTH = 0x1B;
    public static final byte READ_SIG = 0x3C;
    public static final byte SECTOR_SELECT = (byte) 0xC2;
    public static final byte MFULC_AUTH1 = 0x1A;
    public static final byte MFULC_AUTH2 = (byte) 0xAF;
}
