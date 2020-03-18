package com.github.skjolber.nfc.service;

public class TechnologyType {

    private static final String TAG = TechnologyType.class.getName();

    private static byte HEADER = 0x3B;
    private static byte ISO14443_T0 = (byte) 0x80;
    private static byte ISO14443_TD1 = (byte) 0x80;
    private static byte ISO14443_TD2 = (byte) 0x01;

    private static byte CATEGORY_INDICATOR_BYTE = (byte) 0x80;
    private static byte APPLICATION_IDENTIFIER_PRECENCE_INDICATOR = 0x4F;

    private static byte[] REGISTRED_APPLICATION_PROVIDER_IDENTIFIER = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x06};

    public static boolean isNFCA(byte[] atr) {
        // 3b8f8001804f0ca0000003060300030000000068
        if (isISO14443Part3(atr)) {
            return getISO14443Part3Standard(atr) == 0x03;
        }
        return false;
    }

    public static boolean isNFCB(byte[] atr) {
        if (isISO14443Part3(atr)) {
            return getISO14443Part3Standard(atr) == 0x07;
        }
        return false;
    }


    public static boolean isISO14443Part3(byte[] atr) {
        if (!isISO14443Part3Or4Header(atr)) {
            return false;
        }
        // 0  1  2  3  4  5  6  7  8  9 10 11 12
        //3b 8f 80 01 80 4f 0c a0 00 00 03 06 03 00 030000000068
        return atr[4] == CATEGORY_INDICATOR_BYTE && atr[5] == APPLICATION_IDENTIFIER_PRECENCE_INDICATOR && matches(atr, REGISTRED_APPLICATION_PROVIDER_IDENTIFIER, 7);
    }

    private static boolean matches(byte[] atr, byte[] search, int i) {
        if (i + search.length > atr.length) {
            return false;
        }
        for (int k = 0; k < search.length; k++) {
            if (atr[i + k] != search[k]) {
                return false;
            }
        }

        return true;
    }

    public static boolean isISO14443Part3Or4Header(byte[] atr) {

        // 3b8f8001804f0ca0000003060300030000000068
        if (atr[0] != HEADER) {
            return false;
        }
        if ((atr[1] & 0xF0) != (ISO14443_T0 & 0xFF)) {
            return false;
        }

        if (atr[2] != ISO14443_TD1) {
            return false;
        }
        if (atr[3] != ISO14443_TD2) {
            return false;
        }

        return true;
    }

    public static int getISO14443Part3Standard(byte[] atr) {
        /*
        0x01: ISO 14443-1 Type A
        0x02: ISO 14443-2 Type A
        0x03: ISO 14443-3 Type A
        0x05: ISO 14443-1 Type B
        0x06: ISO 14443-2 Type B
        0x07: ISO 14443-3 Type B
        */
        return atr[12];
    }

    public static byte[] getHistoricalBytes(byte[] atr) {
        // see https://android.googlesource.com/platform/libcore/+/0ebbfbdbca73d6261a77183f68e1f3e56c339f9f/ojluni/src/main/java/javax/smartcardio/ATR.java
        if (atr.length < 2) {
            return new byte[]{};
        }
        if ((atr[0] != 0x3b) && (atr[0] != 0x3f)) {
            return new byte[]{};
        }
        int t0 = (atr[1] & 0xf0) >> 4;
        int n = atr[1] & 0xf;
        int i = 2;
        while ((t0 != 0) && (i < atr.length)) {
            if ((t0 & 1) != 0) {
                i++;
            }
            if ((t0 & 2) != 0) {
                i++;
            }
            if ((t0 & 4) != 0) {
                i++;
            }
            if ((t0 & 8) != 0) {
                if (i >= atr.length) {
                    return new byte[]{};
                }
                t0 = (atr[i++] & 0xf0) >> 4;
            } else {
                t0 = 0;
            }
        }
        int k = i + n;
        if ((k == atr.length) || (k == atr.length - 1)) {
            byte[] b = new byte[n];
            System.arraycopy(atr, i, b, 0, n);
            return b;
        }
        return new byte[]{};
    }

}
