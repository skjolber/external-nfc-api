package com.skjolberg.nfc.acs;

import java.util.ArrayList;
import java.util.List;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import com.skjolberg.nfc.acs.remote.IAcr1251UReaderControl;
import com.skjolberg.nfc.acs.remote.IAcr1252UReaderControl;

public class Acr1252UReader extends AcrReader {

	private static final String TAG = Acr1252UReader.class.getName();
	
	private static final int CARD_OPERATION_BLINKING_LED = 1 << 0;
	private static final int PICC_POLLING_STATUS_LED = 1 << 1;
	private static final int PICC_ACTIVATION_STATUS_LED = 1 << 2;
	private static final int CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER = 1 << 3;
	// bit 4 RFU
	private static final int RESET_INDICATION_BUZZER = 1 << 5;
	private static final int COLOR_SELECT_LED_GREEN = 1 << 6;
	private static final int COLOR_SELECT_LED_RED = 1 << 7;
	
	private static final int POLL_TOPAZ = 1 << 4;
	private static final int POLL_FELICA_424K = 1 << 3;
	private static final int POLL_FELICA_212K = 1 << 2;
	private static final int POLL_ISO14443_TYPE_B = 1 << 1;
	private static final int POLL_ISO14443_TYPE_A = 1;
	
	private static final int LED_GREEN = 1 << 1;
	private static final int LED_RED = 1;
	
	protected IAcr1252UReaderControl readerControl;
	
	public Acr1252UReader(String name, IAcr1252UReaderControl readerControl) {
		this.name = name;
		this.readerControl = readerControl;
	}
	
	public static int serializeBehaviour(AcrDefaultLEDAndBuzzerBehaviour... types) {
		int operation = 0;

		for(AcrDefaultLEDAndBuzzerBehaviour type : types) {
			if(type == AcrDefaultLEDAndBuzzerBehaviour.COLOR_SELECT_LED_RED) {
				operation |= COLOR_SELECT_LED_RED;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.COLOR_SELECT_LED_GREEN) {
				operation |= COLOR_SELECT_LED_GREEN;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.CONTACTLESS_CHIP_RESET_INDICATION_BUZZER) {
				operation |= RESET_INDICATION_BUZZER;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER) {
				operation |= CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED) {
				operation |= PICC_ACTIVATION_STATUS_LED;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED) {
				operation |= PICC_POLLING_STATUS_LED;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED) {
				operation |= CARD_OPERATION_BLINKING_LED;
			} else {
				throw new IllegalArgumentException("Behaviour " + type + " not supported");
			}
		}
		return operation;
	}
	
	public static List<AcrDefaultLEDAndBuzzerBehaviour> parseBehaviour(int operation) {
		List<AcrDefaultLEDAndBuzzerBehaviour> behaviours = new ArrayList<AcrDefaultLEDAndBuzzerBehaviour>();
		
		if((operation & COLOR_SELECT_LED_RED) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.COLOR_SELECT_LED_RED);
		}

		if((operation & COLOR_SELECT_LED_GREEN) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.COLOR_SELECT_LED_GREEN);
		}

		if((operation & RESET_INDICATION_BUZZER) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.CONTACTLESS_CHIP_RESET_INDICATION_BUZZER);
		}

		if((operation & CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER);
		}

		if((operation & PICC_ACTIVATION_STATUS_LED) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED);
		}

		if((operation & PICC_POLLING_STATUS_LED) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED);
		}
		
		if((operation & CARD_OPERATION_BLINKING_LED) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED);
		}
		
		return behaviours;
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
	
   @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeString(name);
    	dest.writeStrongBinder(readerControl.asBinder());
    }

    public static final Parcelable.Creator<Acr1252UReader> CREATOR =
            new Parcelable.Creator<Acr1252UReader>() {
        @Override
        public Acr1252UReader createFromParcel(Parcel in) {
        	String name = in.readString();
        	
        	IBinder binder = in.readStrongBinder();
        	
        	IAcr1252UReaderControl iin = IAcr1252UReaderControl.Stub.asInterface(binder);
        	
        	return new Acr1252UReader(name, iin);

        }

        @Override
        public Acr1252UReader[] newArray(int size) {
            return new Acr1252UReader[size];
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
	
	public boolean setBuzzer(boolean value) {
		byte[] response;
		try {
			response = readerControl.setBuzzer(value);
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

}
