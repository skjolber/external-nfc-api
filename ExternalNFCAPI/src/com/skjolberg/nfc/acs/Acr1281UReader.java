package com.skjolberg.nfc.acs;

import java.util.ArrayList;
import java.util.List;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import com.skjolberg.nfc.acs.acr1281u.AutomaticPICCPolling;
import com.skjolberg.nfc.acs.acr1281u.DefaultLEDAndBuzzerBehaviour;
import com.skjolberg.nfc.acs.remote.IAcr1281UReaderControl;

public class Acr1281UReader extends AcrReader {

	private static final String TAG = Acr1281UReader.class.getName();
	
	private static final int LED_GREEN = 1 << 1;
	private static final int LED_RED = 1;
	
	protected IAcr1281UReaderControl readerControl;
	
	public Acr1281UReader(String name, IAcr1281UReaderControl readerControl) {
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
			for(AcrPICC type : types) {
				if(type != AcrPICC.POLL_ISO14443_TYPE_B && type != AcrPICC.POLL_ISO14443_TYPE_A) {
					throw new IllegalArgumentException("PICC " + type + " not supported");
				}
			}
			response = readerControl.setPICC(AcrPICC.serialize(types));
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

    public static final Parcelable.Creator<Acr1281UReader> CREATOR =
            new Parcelable.Creator<Acr1281UReader>() {
        @Override
        public Acr1281UReader createFromParcel(Parcel in) {
        	String name = in.readString();
        	
        	IBinder binder = in.readStrongBinder();
        	
        	IAcr1281UReaderControl iin = IAcr1281UReaderControl.Stub.asInterface(binder);
        	
        	return new Acr1281UReader(name, iin);

        }

        @Override
        public Acr1281UReader[] newArray(int size) {
            return new Acr1281UReader[size];
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

	public List<AcrLED> getLEDs() {
		byte[] response;
		try {
			response = readerControl.getLEDs();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		List<AcrLED> leds = new ArrayList<AcrLED>();

		if((response[0] & LED_RED) != 0) {
			leds.add(AcrLED.RED);
		}
		if((response[0] & LED_GREEN) != 0) {
			leds.add(AcrLED.GREEN);
		}
		
		return leds;
	}
	
	public boolean setLEDs(AcrLED ... types) {
		byte[] response;
		try {
			int operation = 0;
			for(AcrLED type : types) {
				if(type == AcrLED.GREEN) {
					operation |= LED_GREEN;
				} else if(type == AcrLED.RED) {
					operation |= LED_RED;
				} else {
					throw new IllegalArgumentException("LED " + type + " not supported");
				}
			}
			response = readerControl.setLEDs(operation);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readBoolean(response);
	}

	public List<AutomaticPICCPolling> getAutomaticPICCPolling() {
		byte[] response;
		try {
			response = readerControl.getAutomaticPICCPolling();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return AutomaticPICCPolling.parse(readInteger(response));
	}
	
	public boolean setAutomaticPICCPolling(AutomaticPICCPolling ... types) {
		byte[] response;
		try {
			response = readerControl.setAutomaticPICCPolling(AutomaticPICCPolling.serialize(types));
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readBoolean(response);
	}
	
	public boolean setExclusiveMode(boolean shared) {
		byte[] response;
		try {
			response = readerControl.setExclusiveMode(shared);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		byte[] resultByteArray = readByteArray(response);
		
		return resultByteArray[1] != 0;
	}
	
	public boolean getExclusiveMode() {
		byte[] response;
		try {
			response = readerControl.getExclusiveMode();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		byte[] resultByteArray = readByteArray(response);
		
		return resultByteArray[1] != 0;
	}

	public List<DefaultLEDAndBuzzerBehaviour> getDefaultLEDAndBuzzerBehaviour() {
		byte[] response;
		try {
			response = readerControl.getDefaultLEDAndBuzzerBehaviour();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return DefaultLEDAndBuzzerBehaviour.parse(readInteger(response));
	}
	
	public boolean setDefaultLEDAndBuzzerBehaviour(DefaultLEDAndBuzzerBehaviour ... types) {
		byte[] response;
		try {
			response = readerControl.setDefaultLEDAndBuzzerBehaviour(DefaultLEDAndBuzzerBehaviour.serialize(types));
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readBoolean(response);
	}
}
