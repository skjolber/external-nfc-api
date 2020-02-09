package com.github.skjolber.nfc.acs;

import java.util.ArrayList;
import java.util.List;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import com.github.skjolber.nfc.acs.remote.IAcr122UReaderControl;

public class Acr122UReader extends AcrReader {

	private static final String TAG = Acr122UReader.class.getName();
	
	private static final int AUTO_PICC_POLLING = 1 << 7;
	private static final int AUTO_ATS_GENERATION = 1 << 6;
	/** 1 for 250, 0 for 500 milliseconds */
	private static final int POLLING_INTERVAL = 1 << 5;
	private static final int POLL_FELICA_424K = 1 << 4;
	private static final int POLL_FELICA_212K = 1 << 3;
	private static final int POLL_TOPAZ = 1 << 2;
	private static final int POLL_ISO14443_TYPE_B = 1 << 1;
	private static final int POLL_ISO14443_TYPE_A = 1;
	
	protected IAcr122UReaderControl readerControl;
	
	public Acr122UReader(String name, IAcr122UReaderControl readerControl) {
		this.name = name;
		this.readerControl = readerControl;
	}
	
	public String getFirmware() throws AcrReaderException {
		
		byte[] response;
		try {
			response = readerControl.getFirmware();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readString(response);
	}

	public List<AcrPICC> getPICC() {
		byte[] response;
		try {
			response = readerControl.getPICC();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		int operation = readInteger(response);

		ArrayList<AcrPICC> values = new ArrayList<AcrPICC>();

		if((operation & AUTO_PICC_POLLING) != 0) {
			values.add(AcrPICC.AUTO_PICC_POLLING);
		}
		if((operation & AUTO_ATS_GENERATION) != 0) {
			values.add(AcrPICC.AUTO_ATS_GENERATION);
		}
		if((operation & POLLING_INTERVAL) != 0) {
			values.add(AcrPICC.POLLING_INTERVAL);
		}
		if((operation & POLL_TOPAZ) != 0) {
			values.add(AcrPICC.POLL_TOPAZ);
		}
		if((operation & POLL_FELICA_424K) != 0) {
			values.add(AcrPICC.POLL_FELICA_424K);
		}
		if((operation & POLL_FELICA_212K) != 0) {
			values.add(AcrPICC.POLL_FELICA_212K);
		}
		if((operation & POLL_ISO14443_TYPE_B) != 0) {
			values.add(AcrPICC.POLL_ISO14443_TYPE_B);
		}
		if((operation & POLL_ISO14443_TYPE_A) != 0) {
			values.add(AcrPICC.POLL_ISO14443_TYPE_A);
		}
		
		return values;
	}

	public boolean setPICC(AcrPICC ... types) {
		int picc = 0;
		for(AcrPICC type : types) {
			switch(type) {
				case AUTO_PICC_POLLING:{
					picc |= AUTO_PICC_POLLING;
					break;
				}
				case AUTO_ATS_GENERATION:{
					picc |= AUTO_ATS_GENERATION;
					break;
				}
				case POLLING_INTERVAL:{
					picc |= POLLING_INTERVAL;
					break;
				}
				case POLL_FELICA_424K:{
					picc |= POLL_FELICA_424K;
					break;
				}
				case POLL_FELICA_212K:{
					picc |= POLL_FELICA_212K;
					break;
				}
				case POLL_TOPAZ:{
					picc |= POLL_TOPAZ;
					break;
				}
				case POLL_ISO14443_TYPE_A:{
					picc |= POLL_ISO14443_TYPE_A;
					break;
				}
				case POLL_ISO14443_TYPE_B: {
					picc |= POLL_ISO14443_TYPE_B;
					break;
				}
				default : {
					throw new IllegalArgumentException("Unexpected PICC " + type);
				}
			}
		}
		byte[] response;
		try {
			response = readerControl.setPICC(picc);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readBoolean(response);
	}

	
	public boolean setBuzzerForCardDetection(boolean value) {
		byte[] response;
		try {
			response = readerControl.setBuzzerForCardDetection(value);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readBoolean(response);
	}
	
   @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeString(name);
    	dest.writeStrongBinder(readerControl.asBinder());
    }

    public static final Parcelable.Creator<Acr122UReader> CREATOR =
            new Parcelable.Creator<Acr122UReader>() {
        @Override
        public Acr122UReader createFromParcel(Parcel in) {
        	String name = in.readString();
        	
        	IBinder binder = in.readStrongBinder();
        	
        	IAcr122UReaderControl iin = IAcr122UReaderControl.Stub.asInterface(binder);
        	
        	return new Acr122UReader(name, iin);

        }

        @Override
        public Acr122UReader[] newArray(int size) {
            return new Acr122UReader[size];
        }
    };

	@Override
	public byte[] control(int slotNum, int controlCode, byte[] command) {
		byte[] response;
		try {
			response = readerControl.control(slotNum, controlCode, command);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readByteArray(response);
	}

	@Override
	public byte[] transmit(int slotNum, byte[] command) {
		byte[] response;
		try {
			response = readerControl.transmit(slotNum, command);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readByteArray(response);
	}

}
