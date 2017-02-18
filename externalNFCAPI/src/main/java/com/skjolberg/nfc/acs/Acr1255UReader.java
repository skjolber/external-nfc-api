package com.skjolberg.nfc.acs;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.skjolberg.nfc.acs.remote.IAcr1255UReaderControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Acr1255UReader extends AcrReader {

	private static final String TAG = Acr1255UReader.class.getName();

    public static final int LED_1_GREEN = 1;
    public static final int LED_1_RED = 2;
    public static final int LED_2_BLUE = 4;
    public static final int LED_2_RED = 8;

	private static final int ICC_ACTIVATION_STATUS_LED = 1 << 0;
	private static final int PICC_POLLING_STATUS_LED = 1 << 1;
	// bit 2 RFU
	// bit 3 RFU
	private static final int CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER = 1 << 4;
	private static final int CONTACTLESS_CHIP_RESET_INDICATION_BUZZER = 1 << 5;
	private static final int EXCLUSIVE_MODE_STATUS_BUZZER = 1 << 6;
	private static final int LED_CARD_OPERATION_BLINK = 1 << 7;

    private static final int POLL_ISO14443_TYPE_A = 1;
    private static final int POLL_ISO14443_TYPE_B = 1 << 1;
    private static final int POLL_FELICA_212K = 1 << 2;
    private static final int POLL_FELICA_424K = 1 << 3;
    private static final int POLL_TOPAZ = 1 << 4;

    private static final int RATE_106K = 0;
    private static final int RATE_212K = 1;
    private static final int RATE_424K = 2;

    private static final byte PICC_POWER_OFF = 0x00;
    private static final byte PICC_IDLE = 0x01;
    private static final byte PICC_READY = 0x02;
    private static final byte PICC_SELECTED = 0x03;
    private static final byte PICC_ACTIVE = 0x04;

    private static final byte BLUETOOTH_TRANSMISSION_POWER_DISTANCE_3M = 0x00;
    private static final byte BLUETOOTH_TRANSMISSION_POWER_DISTANCE_7M = 0x01;
    private static final byte BLUETOOTH_TRANSMISSION_POWER_DISTANCE_17M = 0x02;
    private static final byte BLUETOOTH_TRANSMISSION_POWER_DISTANCE_25M = 0x03;

    public static int serializeBehaviour(AcrDefaultLEDAndBuzzerBehaviour... types) {
		int operation = 0;

		for(AcrDefaultLEDAndBuzzerBehaviour type : types) {
			if(type == AcrDefaultLEDAndBuzzerBehaviour.ICC_ACTIVATION_STATUS_LED) {
				operation |= ICC_ACTIVATION_STATUS_LED;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED) {
				operation |= PICC_POLLING_STATUS_LED;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER) {
				operation |= CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.CONTACTLESS_CHIP_RESET_INDICATION_BUZZER) {
				operation |= CONTACTLESS_CHIP_RESET_INDICATION_BUZZER;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.EXCLUSIVE_MODE_STATUS_BUZZER) {
				operation |= EXCLUSIVE_MODE_STATUS_BUZZER;
			} else if(type == AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED) {
				operation |= LED_CARD_OPERATION_BLINK;
			} else {
				throw new IllegalArgumentException("Behaviour " + type + " not supported");
			}
		}
		return operation;
	}

	public static List<AcrDefaultLEDAndBuzzerBehaviour> parseBehaviour(int operation) {
		List<AcrDefaultLEDAndBuzzerBehaviour> behaviours = new ArrayList<AcrDefaultLEDAndBuzzerBehaviour>();

		if((operation & ICC_ACTIVATION_STATUS_LED) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.ICC_ACTIVATION_STATUS_LED);
		}

		if((operation & PICC_POLLING_STATUS_LED) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED);
		}

		if((operation & CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER);
		}

		if((operation & CONTACTLESS_CHIP_RESET_INDICATION_BUZZER) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.CONTACTLESS_CHIP_RESET_INDICATION_BUZZER);
		}

		if((operation & EXCLUSIVE_MODE_STATUS_BUZZER) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.EXCLUSIVE_MODE_STATUS_BUZZER);
		}

		if((operation & LED_CARD_OPERATION_BLINK) != 0) {
			behaviours.add(AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED);
		}

		return behaviours;
	}

	protected IAcr1255UReaderControl readerControl;

	public Acr1255UReader(String name, IAcr1255UReaderControl readerControl) {
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

    public String getSerialNumber() throws AcrReaderException {

        byte[] response;
        try {
            response = readerControl.getSerialNumber();
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

		if((operation & POLL_ISO14443_TYPE_B) != 0) {
			values.add(AcrPICC.POLL_ISO14443_TYPE_B);
		}
		if((operation & POLL_ISO14443_TYPE_A) != 0) {
			values.add(AcrPICC.POLL_ISO14443_TYPE_A);
		}
        if((operation & POLL_FELICA_424K) != 0) {
            values.add(AcrPICC.POLL_FELICA_424K);
        }
        if((operation & POLL_FELICA_212K) != 0) {
            values.add(AcrPICC.POLL_FELICA_212K);
        }
        if((operation & POLL_TOPAZ) != 0) {
            values.add(AcrPICC.POLL_TOPAZ);
        }

		return values;
	}

	public boolean setPICC(AcrPICC ... types) {
		int picc = 0;
		for(AcrPICC type : types) {
			switch(type) {
				case POLL_ISO14443_TYPE_A:{
					picc |= Acr1255UReader.POLL_ISO14443_TYPE_A;
					break;
				}
				case POLL_ISO14443_TYPE_B: {
					picc |= Acr1255UReader.POLL_ISO14443_TYPE_B;
					break;
				}
                case POLL_FELICA_424K: {
                    picc |= Acr1255UReader.POLL_FELICA_424K;
                    break;
                }
                case POLL_FELICA_212K: {
                    picc |= Acr1255UReader.POLL_FELICA_212K;
                    break;
                }
                case POLL_TOPAZ: {
                    picc |= Acr1255UReader.POLL_TOPAZ;
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

    public static final Creator<Acr1255UReader> CREATOR =
            new Creator<Acr1255UReader>() {
        @Override
        public Acr1255UReader createFromParcel(Parcel in) {
        	String name = in.readString();
        	
        	IBinder binder = in.readStrongBinder();

            IAcr1255UReaderControl iin = IAcr1255UReaderControl.Stub.asInterface(binder);
        	
        	return new Acr1255UReader(name, iin);

        }

        @Override
        public Acr1255UReader[] newArray(int size) {
            return new Acr1255UReader[size];
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

	public List<Set<AcrLED>> getLEDs() {
		byte[] response;
		try {
			response = readerControl.getLEDs();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

        int operation = response[0] & 0xFF;

        Set<AcrLED> first = new HashSet<AcrLED>();
        Set<AcrLED> second = new HashSet<AcrLED>();

        if((operation & LED_1_GREEN) != 0) {
            first.add(AcrLED.GREEN);
        }

        if((operation & LED_1_RED) != 0) {
            first.add(AcrLED.RED);
        }

        if((operation & LED_2_BLUE) != 0) {
            second.add(AcrLED.BLUE);
        }

        if((operation & LED_2_RED) != 0) {
            second.add(AcrLED.RED);
        }

        return Arrays.asList(first, second);

	}
	
	public boolean setLEDs(Set<AcrLED> first, Set<AcrLED> second) {
		byte[] response;
		try {
			int operation = 0;

			for(AcrLED type : first) {
				if(type == AcrLED.GREEN) {
					operation |= LED_1_GREEN;
				} else if(type == AcrLED.RED) {
					operation |= LED_1_RED;
				} else {
					throw new IllegalArgumentException("LED " + type + " not supported for LED #1");
				}
			}

            for(AcrLED type : second) {
                if(type == AcrLED.BLUE) {
                    operation |= LED_2_BLUE;
                } else if(type == AcrLED.RED) {
                    operation |= LED_2_RED;
                } else {
                    throw new IllegalArgumentException("LED " + type + " not supported for LED #2");
                }
            }

            response = readerControl.setLEDs(operation);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}
		
		return readBoolean(response);
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


    public boolean setAutoPPS(AcrCommunicationSpeed tx, AcrCommunicationSpeed rx) {
        byte[] response;
        try {

            byte[] speed = new byte[2];
            if(tx == AcrCommunicationSpeed.RATE_106_Kbps) {
                speed[0] = RATE_106K;
            } else if(tx == AcrCommunicationSpeed.RATE_212_Kbps) {
                speed[0] = RATE_212K;
            } else if(tx == AcrCommunicationSpeed.RATE_424_Kbps) {
                speed[0] = RATE_424K;
            } else throw new IllegalArgumentException("Unexpected rate " + tx);

            if(rx == AcrCommunicationSpeed.RATE_106_Kbps) {
                speed[1] = RATE_106K;
            } else if(rx == AcrCommunicationSpeed.RATE_212_Kbps) {
                speed[1] = RATE_212K;
            } else if(rx == AcrCommunicationSpeed.RATE_424_Kbps) {
                speed[1] = RATE_424K;
            } else throw new IllegalArgumentException("Unexpected rate " + rx);

            response = readerControl.setAutoPPS(speed);
        } catch (RemoteException e) {
            throw new AcrReaderException(e);
        }

        return readBoolean(response);
    }

    /**
     * Read Auto PPS.
     *
     * @return list of Max Tx Speed, Current Tx Speed, Max Rx Speed, Current Rx Speed
     */

    public List<AcrCommunicationSpeed> getAutoPPS() {
        byte[] response;
        try {
            response = readerControl.getAutoPPS();
        } catch (RemoteException e) {
            throw new AcrReaderException(e);
        }

        byte[] resultByteArray = readByteArray(response);

        List<AcrCommunicationSpeed> list = new ArrayList<AcrCommunicationSpeed>();
        for(byte b : resultByteArray) {
            list.add(parse(b));
        }

        return list;
    }

    private AcrCommunicationSpeed parse(byte b) {
        if(b == RATE_106K) {
            return AcrCommunicationSpeed.RATE_106_Kbps;
        } else if(b == RATE_212K) {
            return AcrCommunicationSpeed.RATE_212_Kbps;
        } else if(b == RATE_424K) {
            return AcrCommunicationSpeed.RATE_424_Kbps;
        } else throw new IllegalArgumentException("Unexpected communication speed " + Integer.toHexString(0xFF & b));
    }

    public boolean setAntennaField(boolean on) {
        byte[] response;
        try {
            response = readerControl.setAntennaField(on);
        } catch (RemoteException e) {
            throw new AcrReaderException(e);
        }

        return readBoolean(response);
    }

    public AcrAntennaFieldStatus getAntennaFieldStatus() {
        byte[] response;
        try {
            response = readerControl.getAntennaFieldStatus();
        } catch (RemoteException e) {
            throw new AcrReaderException(e);
        }

        byte status = readByte(response);

        switch(status) {
            case PICC_POWER_OFF : {
                return AcrAntennaFieldStatus.PICC_POWER_OFF;
            }
            case PICC_IDLE : {
                return AcrAntennaFieldStatus.PICC_IDLE;
            }
            case PICC_READY: {
                return AcrAntennaFieldStatus.PICC_READY;
            }
            case PICC_SELECTED: {
                return AcrAntennaFieldStatus.PICC_SELECTED;
            }
            case PICC_ACTIVE : {
                return AcrAntennaFieldStatus.PICC_ACTIVE;
            }
        }

        throw new IllegalArgumentException("Unknown antenna field status " + Integer.toHexString(status & 0xFF));
    }

    public AcrBluetoothTransmissionPower getBluetoothTransmissionPower() {
        byte[] response;
        try {
            response = readerControl.getBluetoothTransmissionPower();
        } catch (RemoteException e) {
            throw new AcrReaderException(e);
        }

        byte status = readByte(response);

        switch(status) {
            case BLUETOOTH_TRANSMISSION_POWER_DISTANCE_3M : {
                return AcrBluetoothTransmissionPower.DISTANCE_3M;
            }
            case BLUETOOTH_TRANSMISSION_POWER_DISTANCE_7M : {
                return AcrBluetoothTransmissionPower.DISTANCE_7M;
            }
            case BLUETOOTH_TRANSMISSION_POWER_DISTANCE_17M: {
                return AcrBluetoothTransmissionPower.DISTANCE_17M;
            }
            case BLUETOOTH_TRANSMISSION_POWER_DISTANCE_25M: {
                return AcrBluetoothTransmissionPower.DISTANCE_25M;
            }
        }

        throw new IllegalArgumentException("Unknown bluetooth transmission power " + Integer.toHexString(status & 0xFF));
    }

    public boolean setBluetoothTransmissionPower(AcrBluetoothTransmissionPower distance) {

        byte code;
        switch(distance) {
            case DISTANCE_3M: {
                code = BLUETOOTH_TRANSMISSION_POWER_DISTANCE_3M;
                break;
            }
            case DISTANCE_7M: {
                code = BLUETOOTH_TRANSMISSION_POWER_DISTANCE_7M;
                break;
            }
            case DISTANCE_17M: {
                code = BLUETOOTH_TRANSMISSION_POWER_DISTANCE_17M;
                break;
            }
            case DISTANCE_25M: {
                code = BLUETOOTH_TRANSMISSION_POWER_DISTANCE_25M;
                break;
            }
            default : {
                throw new IllegalArgumentException("Unknown distance " + distance);
            }
        }
        byte[] response;
        try {
            response = readerControl.setBluetoothTransmissionPower(code);
        } catch (RemoteException e) {
            throw new AcrReaderException(e);
        }
        return readBoolean(response);
    }
}
