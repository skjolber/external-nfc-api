package com.skjolberg.nfc.acs;

import java.util.ArrayList;
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
		List<AcrLED> types = new ArrayList<AcrLED>();
		
		if(ready) {
			types.add(AcrLED.GREEN);
		}
		if(progress) {
			types.add(AcrLED.BLUE);
		}
		if(complete) {
			types.add(AcrLED.ORANGE);
		}
		if(error) {
			types.add(AcrLED.RED);
		}
		
		return setLEDs(types.toArray(new AcrLED[types.size()]));
	}
	
	public boolean setLEDs(AcrLED ... types) {
		byte[] response;
		try {
			int operation = Acr1283UReader.serializeLEDs(types);
			
			response = readerControl.setLEDs(operation);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readBoolean(response);
	}

	
	public boolean setDefaultLEDAndBuzzerBehaviours(boolean piccPollingStatusLED, boolean piccActivationStatusLED, boolean buzzerForCardInsertionOrRemoval, boolean cardOperationBlinkingLED) throws AcrReaderException {
		
		List<AcrDefaultLEDAndBuzzerBehaviour> types = new ArrayList<AcrDefaultLEDAndBuzzerBehaviour>();
		
		if(piccPollingStatusLED) {
			types.add(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED);
		}
		if(piccActivationStatusLED) {
			types.add(AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED);
		}
		if(buzzerForCardInsertionOrRemoval) {
			types.add(AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER);
		}
		if(cardOperationBlinkingLED) {
			types.add(AcrDefaultLEDAndBuzzerBehaviour.LED_CARD_OPERATION_BLINK);
		}
		
		return setDefaultLEDAndBuzzerBehaviour(types.toArray(new AcrDefaultLEDAndBuzzerBehaviour[types.size()]));
	}

	public List<AcrDefaultLEDAndBuzzerBehaviour> getDefaultLEDAndBuzzerBehaviour() {
		byte[] response;
		try {
			response = readerControl.getDefaultLEDAndBuzzerBehaviour();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return Acr1283UReader.parseBehaviour(readInteger(response));
	}
	
	public boolean setDefaultLEDAndBuzzerBehaviour(AcrDefaultLEDAndBuzzerBehaviour ... types) {
		byte[] response;
		try {
			int operation = Acr1283UReader.serializeBehaviour(types);
			
			response = readerControl.setDefaultLEDAndBuzzerBehaviour(operation);
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
