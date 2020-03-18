/*
 * DesfireFileSettings.java
 *
 * Copyright (C) 2011 Eric Butler
 *
 * Authors:
 * Eric Butler <eric@codebutler.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.skjolber.nfc.service.desfire;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public abstract class DesfireFileSettings implements Parcelable {
    public final byte   fileType;
    public final byte   commSetting;
    public final byte[] accessRights;

    /* DesfireFile Types */
    static final byte STANDARD_DATA_FILE = (byte) 0x00;
    static final byte BACKUP_DATA_FILE   = (byte) 0x01;
    static final byte VALUE_FILE         = (byte) 0x02;
    static final byte LINEAR_RECORD_FILE = (byte) 0x03;
    static final byte CYCLIC_RECORD_FILE = (byte) 0x04;
    
    public static DesfireFileSettings Create (byte[] data) throws Exception {
        byte fileType = (byte) data[0];

        ByteArrayInputStream is = new ByteArrayInputStream(data);

        if (fileType == STANDARD_DATA_FILE || fileType == BACKUP_DATA_FILE)
            return new StandardDesfireFileSettings(is);
        else if (fileType == LINEAR_RECORD_FILE || fileType == CYCLIC_RECORD_FILE)
            return new RecordDesfireFileSettings(is);
        else if (fileType == VALUE_FILE)
            return new ValueDesfireFileSettings(is);
        else
            throw new Exception("Unknown file type: " + Integer.toHexString(fileType));
    }

    private DesfireFileSettings (ByteArrayInputStream stream) {
        fileType    = (byte) stream.read();
        commSetting = (byte) stream.read();

        accessRights = new byte[2];
        stream.read(accessRights, 0, accessRights.length);
    }

    private DesfireFileSettings (byte fileType, byte commSetting, byte[] accessRights) {
        this.fileType     = fileType;
        this.commSetting  = commSetting;
        this.accessRights = accessRights;
    }

    public String getFileTypeName () {
        switch (fileType) {
            case STANDARD_DATA_FILE:
                return "Standard";
            case BACKUP_DATA_FILE:
                return "Backup";
            case VALUE_FILE:
                return "Value";
            case LINEAR_RECORD_FILE:
                return "Linear Record";
            case CYCLIC_RECORD_FILE:
                return "Cyclic Record";
            default:
                return "Unknown";
        }
    }

    public static final Creator<DesfireFileSettings> CREATOR = new Creator<DesfireFileSettings>() {
        public DesfireFileSettings createFromParcel(Parcel source) {
            byte fileType       = source.readByte();
            byte commSetting    = source.readByte();
            byte[] accessRights = new byte[source.readInt()];
            source.readByteArray(accessRights);

            if (fileType == STANDARD_DATA_FILE || fileType == BACKUP_DATA_FILE) {
                int fileSize = source.readInt();
                return new StandardDesfireFileSettings(fileType, commSetting, accessRights, fileSize);
            } else if (fileType == LINEAR_RECORD_FILE || fileType == CYCLIC_RECORD_FILE) {
                int recordSize = source.readInt();
                int maxRecords = source.readInt();
                int curRecords = source.readInt();
                return new RecordDesfireFileSettings(fileType, commSetting, accessRights, recordSize, maxRecords, curRecords);
            } else {
                return new UnsupportedDesfireFileSettings(fileType);
            }
        }

        public DesfireFileSettings[] newArray(int size) {
            return new DesfireFileSettings[size];
        }
    };

    public void writeToParcel (Parcel parcel, int flags) {
        parcel.writeByte(fileType);
        parcel.writeByte(commSetting);
        parcel.writeInt(accessRights.length);
        parcel.writeByteArray(accessRights);
    }

    public int describeContents () {
        return 0;
    }

    public static class StandardDesfireFileSettings extends DesfireFileSettings {
        public final int fileSize;

        private StandardDesfireFileSettings (ByteArrayInputStream is) throws IOException {
            super(is);
            
            fileSize = readReverseUnsigned3(is);
        }

        StandardDesfireFileSettings (byte fileType, byte commSetting, byte[] accessRights, int fileSize) {
            super(fileType, commSetting, accessRights);
            this.fileSize = fileSize;
        }

        @Override
        public void writeToParcel (Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);
            parcel.writeInt(fileSize);
        }
    }

    public static class RecordDesfireFileSettings extends DesfireFileSettings {
        public final int recordSize;
        public final int maxRecords;
        public final int curRecords;

        public RecordDesfireFileSettings(ByteArrayInputStream is) throws IOException {
            super(is);

            recordSize = readReverseUnsigned3(is);
            maxRecords = readReverseUnsigned3(is);
            curRecords = readReverseUnsigned3(is);
        }

        RecordDesfireFileSettings (byte fileType, byte commSetting, byte[] accessRights, int recordSize, int maxRecords, int curRecords) {
            super(fileType, commSetting, accessRights);
            this.recordSize = recordSize;
            this.maxRecords = maxRecords;
            this.curRecords = curRecords;
        }

        @Override
        public void writeToParcel (Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);
            parcel.writeInt(recordSize);
            parcel.writeInt(maxRecords);
            parcel.writeInt(curRecords);
        }
    }

    public static class UnsupportedDesfireFileSettings extends DesfireFileSettings {
        public UnsupportedDesfireFileSettings(byte fileType) {
            super(fileType, Byte.MIN_VALUE, new byte[0]);
        }
    }
    
    public int readAccessKey() {
    	return (accessRights[0] & 0xF0) >> 4;
    }

    public int writeAccessKey() {
    	return accessRights[0] & 0x0F;
    }

    public int readWriteAccessKey() {
    	return (accessRights[1] & 0xF0) >> 4;
    }

    public int changeAccessKey() {
    	return accessRights[1] & 0x0F;
    }

    public boolean freeReadAccess() {
    	return readAccessKey() == 0xE || readWriteAccessKey() == 0xE;
    }
    
    public boolean freeWriteAccess() {
    	return writeAccessKey() == 0xE || readWriteAccessKey() == 0xE;
    }
    
    public byte[] getAccessRights() {
		return accessRights;
	}
    
    public static class ValueDesfireFileSettings extends DesfireFileSettings {
        public final long lowerLimit;
        public final long upperLimit;
        public final long limitedCreditValue;
        public final byte limitedCreditEnabled;

        public ValueDesfireFileSettings(ByteArrayInputStream is) throws IOException {
            super(is);

            lowerLimit = readUnsigned4Reverse(is);
            upperLimit = readUnsigned4Reverse(is);
            limitedCreditValue = readUnsigned4Reverse(is);
            
            limitedCreditEnabled = (byte)is.read();
        }

        public ValueDesfireFileSettings(byte fileType, byte commSetting, byte[] accessRights, long lowerLimit, long upperLimit, long limitedCreditValue, byte limitedCreditEnabled) {
            super(fileType, commSetting, accessRights);
			this.lowerLimit = lowerLimit;
			this.upperLimit = upperLimit;
			this.limitedCreditValue = limitedCreditValue;
			this.limitedCreditEnabled = limitedCreditEnabled;
		}

        @Override
        public void writeToParcel (Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);
            parcel.writeLong(lowerLimit);
            parcel.writeLong(upperLimit);
            parcel.writeLong(limitedCreditValue);
            parcel.writeByte(limitedCreditEnabled);
        }
    }

    private static long readUnsigned4Reverse(InputStream in) throws IOException {
    	int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((long)(ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
    }
    
    private static int readReverseUnsigned3(InputStream in) throws IOException {
    	int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        if ((ch1 | ch2 | ch3) < 0)
            throw new EOFException();
        return (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
    }
}
