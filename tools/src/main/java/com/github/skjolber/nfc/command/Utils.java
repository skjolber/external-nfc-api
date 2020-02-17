package com.github.skjolber.nfc.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.res.Resources;

public class Utils {

    /**
     * Converts the byte array to HEX string.
     *
     * @param buffer the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buffer)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }


    public static byte[] getResource(int r, Activity a) throws IOException {
        Resources res = a.getResources();
        InputStream in = res.openRawResource(r);

        byte[] b = new byte[4096];

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        int read;
        do {
            read = in.read(b, 0, b.length);
            if (read == -1) {
                break;
            }
            bout.write(b, 0, read);
        } while (true);

        return bout.toByteArray();
    }

    public static String convertBinToASCII(byte[] bin, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int x = offset; x < offset + length; x++) {
            sb.append(String.format("%02X ", bin[x]));
        }
        return sb.toString().toUpperCase();
    }

    public static String convertBinToASCII(byte[] bin) {
        return convertBinToASCII(bin, 0, bin.length);
    }

    public static byte[] reverse(byte[] data) {
        byte[] result = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            result[result.length - 1 - i] = data[i];
        }

        return result;
    }
}
