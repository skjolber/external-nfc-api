package com.github.skjolber.nfc.external.client;

import android.util.Log;

import com.github.skjolber.android.nfc.tech.IsoDep;

import java.io.IOException;

public class PingPong {

    private static String TAG = PingPong.class.getName();

    public static void playPingPong(IsoDep isoDep) throws IOException {
        byte[] ping = getPing();

        int count = 0;
        long elapsed = 0;

        while (isoDep.isConnected()) {
            Log.i(TAG, " <- " + toHexString(ping));
            long time = System.currentTimeMillis();
            final byte[] pong = isoDep.transceive(ping);
            time = System.currentTimeMillis() - time;
            Log.i(TAG, " <- " + toHexString(pong));
            if (!isPong(ping, pong)) {
                Log.d(TAG, "No pong to the ping");
                break;
            } else {
                count++;
                elapsed += time;
                Log.d(TAG, "Ping-pong in " + time + " ms (average " + (elapsed / count)  + " ms)");
            }
        }
    }
    public static byte[] getPong() {
        byte[] ping = getPing();

        byte[] pong = new byte[10];
        for (int i = 0; i < ping.length; i++) {
            pong[ping.length - 1 - i] = (byte) i;
        }

        return pong;
    }

    public static byte[] getPing() {
        byte[] ping = new byte[10];
        for (int i = 0; i < ping.length; i++) {
            ping[i] = (byte) i;
        }
        return ping;
    }


    public static boolean isPing(byte[] ping) {
        for (int i = 0; i < ping.length; i++) {
            if(ping[i] != (byte) i) {
                return false;
            }
        }

        return true;
    }

    private static boolean isPong(byte[] ping, byte[] pong) {
        if(ping.length != pong.length) {
            return false;
        }
        for(int i = 0; i < ping.length; i++) {
            if(ping[i] != pong[ping.length - 1 - i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts the byte array to HEX string.
     *
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer) {
        return toHexString(buffer, 0, buffer.length);
    }


    /**
     * Converts the byte array to HEX string.
     *
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for(int i = offset; i < offset + length; i++) {
            byte b = buffer[i];
            sb.append(String.format("%02x", b&0xff));
        }
        return sb.toString().toUpperCase();
    }
}
