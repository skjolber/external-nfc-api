package com.skjolberg.nfc.acs;

import java.util.List;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import com.skjolberg.nfc.acs.remote.IAcr1222LReaderControl;

public class Acr1222LReader extends AcrReader {

	protected IAcr1222LReaderControl readerControl;
	
	public Acr1222LReader(String name, IAcr1222LReaderControl readerControl) {
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
		
		return AcrPICC.parse(readInteger(response));
	}

	public boolean setPICC(AcrPICC ... types) {
		byte[] response;
		try {
			response = readerControl.setPICC(AcrPICC.serialize(types));
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readBoolean(response);
	}
	
	public boolean lightLED(boolean ready, boolean progress, boolean complete, boolean error) {
		byte[] response;
		try {
			response = readerControl.lightLED(ready, progress, complete, error);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readBoolean(response);
	}
	
	public boolean setDefaultLEDAndBuzzerBehaviours(boolean piccPollingStatusLED, boolean piccActivationStatusLED, boolean buzzerForCardInsertionOrRemoval, boolean cardOperationBlinkingLED) throws AcrReaderException {
		byte[] response;
		try {
			response = readerControl.setDefaultLEDAndBuzzerBehaviours(piccPollingStatusLED, piccActivationStatusLED, buzzerForCardInsertionOrRemoval, cardOperationBlinkingLED);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readBoolean(response);
	}

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
	
	@Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeString(name);
    	dest.writeStrongBinder(readerControl.asBinder());
    }

    public static final Parcelable.Creator<Acr1222LReader> CREATOR =
            new Parcelable.Creator<Acr1222LReader>() {
        @Override
        public Acr1222LReader createFromParcel(Parcel in) {
        	String name = in.readString();
        	
        	IBinder binder = in.readStrongBinder();
        	
        	IAcr1222LReaderControl iin = IAcr1222LReaderControl.Stub.asInterface(binder);
        	
        	return new Acr1222LReader(name, iin);
        }

        @Override
        public Acr1222LReader[] newArray(int size) {
            return new Acr1222LReader[size];
        }
    };
	
  
}
