package com.skjolberg.nfc.acs;

import java.util.ArrayList;
import java.util.List;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import com.skjolberg.nfc.acs.remote.IAcr1283UReaderControl;

public class Acr1283UReader extends AcrReader {

	private static final String TAG = Acr1283UReader.class.getName();
	
	private static final int LED_GREEN = 1;
	private static final int LED_BLUE = 1 << 1;
	private static final int LED_ORANGE = 1 << 2;
	private static final int LED_RED = 1 << 3;
	
	// behaviour bits
	// bit 0 RFU
	private static final int PICC_POLLING_STATUS_LED = 1 << 1;
	private static final int PICC_ACTIVATION_STATUS_LED = 1 << 2;
	private static final int CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER = 1 << 3;
	// bit 4 RFU
	// bit 5 RFU
	// bit 6 RFU
	private static final int LED_CARD_OPERATION_BLINK = 1 << 7;
	
	public static int serializeBehaviour(AcrDefaultLEDAndBuzzerBehaviour... types) {
		int operation = 0;

		for(AcrDefaultLEDAndBuzzerBehaviour type : types) {
			if(type == AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED) {
				operation |= PICC_POLLING_STATUS_LED;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED) {
				operation |= PICC_ACTIVATION_STATUS_LED;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER) {
				operation |= CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.LED_CARD_OPERATION_BLINK) {
				operation |= LED_CARD_OPERATION_BLINK;
			} else {
				throw new IllegalArgumentException("Behaviour " + type + " not supported");
			}
		}
		return operation;
	}
	
	public static List<AcrDefaultLEDAndBuzzerBehaviour> parseBehaviour(int operation) {
		List<AcrDefaultLEDAndBuzzerBehaviour> behaviours = new ArrayList<AcrDefaultLEDAndBuzzerBehaviour>();

		if((operation & PICC_ACTIVATION_STATUS_LED) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED);
		}

		if((operation & PICC_ACTIVATION_STATUS_LED) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED);
		}

		if((operation & CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER);
		}

		if((operation & LED_CARD_OPERATION_BLINK) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.LED_CARD_OPERATION_BLINK);
		}

		return behaviours;
	}

	public static int serializeLEDs(AcrLED... types) {
		int operation = 0;
		for(AcrLED type : types) {
			if(type == AcrLED.GREEN) {
				operation |= LED_GREEN;
			} else if(type == AcrLED.RED) {
				operation |= LED_RED;
			} else if(type == AcrLED.BLUE) {
				operation |= LED_BLUE;
			} else if(type == AcrLED.ORANGE) {
				operation |= LED_ORANGE;
			} else {
				throw new IllegalArgumentException("LED " + type + " not supported");
			}
		}
		return operation;
	}

	public static List<AcrLED> parseLEDs(int operation) {
		List<AcrLED> leds = new ArrayList<AcrLED>();

		if((operation & LED_RED) != 0) {
			leds.add(AcrLED.RED);
		}
		if((operation & LED_GREEN) != 0) {
			leds.add(AcrLED.GREEN);
		}
		if((operation & LED_BLUE) != 0) {
			leds.add(AcrLED.BLUE);
		}
		if((operation & LED_ORANGE) != 0) {
			leds.add(AcrLED.ORANGE);
		}
		
		return leds;
	}
	
	protected IAcr1283UReaderControl readerControl;
	
	public Acr1283UReader(String name, IAcr1283UReaderControl readerControl) {
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

    public static final Parcelable.Creator<Acr1283UReader> CREATOR =
            new Parcelable.Creator<Acr1283UReader>() {
        @Override
        public Acr1283UReader createFromParcel(Parcel in) {
        	String name = in.readString();
        	
        	IBinder binder = in.readStrongBinder();
        	
        	IAcr1283UReaderControl iin = IAcr1283UReaderControl.Stub.asInterface(binder);
        	
        	return new Acr1283UReader(name, iin);

        }

        @Override
        public Acr1283UReader[] newArray(int size) {
            return new Acr1283UReader[size];
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
	
	public boolean setLEDs(AcrLED ... types) {
		byte[] response;
		try {
			int operation = serializeLEDs(types);
			response = readerControl.setLEDs(operation);
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
	
	public List<AcrAutomaticPICCPolling> getAutomaticPICCPolling() {
		byte[] response;
		try {
			response = readerControl.getAutomaticPICCPolling();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return AcrAutomaticPICCPolling.parse(readInteger(response));
	}
	
	public boolean setAutomaticPICCPolling(AcrAutomaticPICCPolling ... types) {
		byte[] response;
		try {
			response = readerControl.setAutomaticPICCPolling(AcrAutomaticPICCPolling.serialize(types));
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readBoolean(response);
	}
	
	public List<AcrDefaultLEDAndBuzzerBehaviour> getDefaultLEDAndBuzzerBehaviour() {
		byte[] response;
		try {
			response = readerControl.getDefaultLEDAndBuzzerBehaviour();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		
		
		return parseBehaviour(readInteger(response));
	}
	
	public boolean setDefaultLEDAndBuzzerBehaviour(AcrDefaultLEDAndBuzzerBehaviour ... types) {
		byte[] response;
		try {
			int operation = serializeBehaviour(types);
			
			response = readerControl.setDefaultLEDAndBuzzerBehaviour(operation);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readBoolean(response);
	}

}
