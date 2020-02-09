package com.github.skjolber.nfc.command.remote;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import com.github.skjolber.nfc.acs.AcrReader;

public class CommandWrapper {

    private static final String TAG = CommandWrapper.class.getName();

    protected byte[] returnValue(Integer picc, Exception exception) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(AcrReader.VERSION);

            if (picc != null) {
                dout.writeInt(AcrReader.STATUS_OK);
                dout.writeInt(picc);
            } else {
                dout.writeInt(AcrReader.STATUS_EXCEPTION);
                dout.writeUTF(exception.toString());
            }
            byte[] response = out.toByteArray();

            // Log.d(TAG, "Send response length " + response.length + ":" + ACRCommands.toHexString(response));

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected byte[] returnValue(String firmware, Exception exception) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(AcrReader.VERSION);

            if (firmware != null) {
                dout.writeInt(AcrReader.STATUS_OK);
                dout.writeUTF(firmware);
            } else {
                dout.writeInt(AcrReader.STATUS_EXCEPTION);
                dout.writeUTF(exception.toString());
            }
            byte[] response = out.toByteArray();

            // Log.d(TAG, "Send response length " + response.length + ":" + ACRCommands.toHexString(response));

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected byte[] returnValue(Boolean value, Exception exception) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(AcrReader.VERSION);

            if (value != null) {
                dout.writeInt(AcrReader.STATUS_OK);
                dout.writeBoolean(value);
            } else {
                dout.writeInt(AcrReader.STATUS_EXCEPTION);
                dout.writeUTF(exception.toString());
            }
            byte[] response = out.toByteArray();

            // Log.d(TAG, "Send response length " + response.length + ":" + ACRCommands.toHexString(response));

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected byte[] returnValue(byte[] value, Exception exception) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(AcrReader.VERSION);

            if (value != null) {
                dout.writeInt(AcrReader.STATUS_OK);
                dout.writeInt(value.length);
                dout.write(value);
            } else {
                dout.writeInt(AcrReader.STATUS_EXCEPTION);
                dout.writeUTF(exception.toString());
            }
            byte[] response = out.toByteArray();

            // Log.d(TAG, "Send response length " + response.length + ":" + ACRCommands.toHexString(response));

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected byte[] returnValue(Byte picc, Exception exception) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(AcrReader.VERSION);

            if (picc != null) {
                dout.writeInt(AcrReader.STATUS_OK);
                dout.writeByte(picc & 0xFF); // strictly speaking not necessary with an and
            } else {
                dout.writeInt(AcrReader.STATUS_EXCEPTION);
                dout.writeUTF(exception.toString());
            }
            byte[] response = out.toByteArray();

            // Log.d(TAG, "Send response length " + response.length + ":" + ACRCommands.toHexString(response));

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
