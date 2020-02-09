package com.github.skjolber.nfc.hce;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import android.os.RemoteException;
import android.util.Log;

import com.github.skjolber.nfc.acs.AcrReader;
import com.github.skjolber.nfc.acs.remote.IAcr1222LReaderControl;
import com.github.skjolber.nfc.command.ACR1222Commands;
import com.github.skjolber.nfc.command.ACRCommands;
import com.github.skjolber.nfc.command.remote.IAcr1222LCommandWrapper;

public class IAcr1222LBinder extends IAcr1222LReaderControl.Stub {

    private static final String TAG = IAcr1222LBinder.class.getName();

    private IAcr1222LCommandWrapper iAcr1222LCommandWrapper;

    public IAcr1222LBinder() {
        attachInterface(this, IAcr1222LReaderControl.class.getName());
    }

    public void setAcr1222LCommands(ACR1222Commands reader) {
        iAcr1222LCommandWrapper = new IAcr1222LCommandWrapper(reader);
    }

    public void clearReader() {
        this.iAcr1222LCommandWrapper = null;
    }

    @Override
    public byte[] getFirmware() throws RemoteException {
        Log.d(TAG, "getFirmwareAcr1222L");
        if (iAcr1222LCommandWrapper == null) {
            return noReaderException();
        }
        return iAcr1222LCommandWrapper.getFirmware();
    }

    @Override
    public byte[] getPICC() throws RemoteException {
        if (iAcr1222LCommandWrapper == null) {
            return noReaderException();
        }
        return iAcr1222LCommandWrapper.getPICC();
    }

	/*
	@Override
	public byte[] lightLED(boolean ready, boolean progress, boolean complete, boolean error) throws RemoteException {
		if(iAcr1222LCommandWrapper == null) {
			return noReaderException();
		}
		return iAcr1222LCommandWrapper.lightLED(ready, progress, complete, error);
	}

	@Override
	public byte[] setDefaultLEDAndBuzzerBehaviours(boolean piccPollingStatusLED, boolean piccActivationStatusLED, boolean buzzerForCardInsertionOrRemoval, boolean cardOperationBlinkingLED) throws RemoteException {
		if(iAcr1222LCommandWrapper == null) {
			return noReaderException();
		}
		
		return iAcr1222LCommandWrapper.setDefaultLEDAndBuzzerBehaviours(piccPollingStatusLED, piccActivationStatusLED, buzzerForCardInsertionOrRemoval, cardOperationBlinkingLED);
	}
	*/

    @Override
    public byte[] setPICC(int picc) {
        if (iAcr1222LCommandWrapper == null) {
            return noReaderException();
        }
        return iAcr1222LCommandWrapper.setPICC(picc);
    }

    private byte[] noReaderException() {


        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(AcrReader.VERSION);
            dout.writeInt(AcrReader.STATUS_EXCEPTION);
            dout.writeUTF("Reader not connected");

            byte[] response = out.toByteArray();

            Log.d(TAG, "Send exception response length " + response.length + ":" + ACRCommands.toHexString(response));

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public byte[] control(int slotNum, int controlCode, byte[] command)
            throws RemoteException {
        if (iAcr1222LCommandWrapper == null) {
            return noReaderException();
        }

        return iAcr1222LCommandWrapper.control(slotNum, controlCode, command);
    }

    @Override
    public byte[] transmit(int slotNum, byte[] command) throws RemoteException {
        if (iAcr1222LCommandWrapper == null) {
            return noReaderException();
        }

        return iAcr1222LCommandWrapper.transmit(slotNum, command);
    }

    @Override
    public byte[] setLEDs(int leds) throws RemoteException {
        if (iAcr1222LCommandWrapper == null) {
            return noReaderException();
        }
        return iAcr1222LCommandWrapper.setLEDs(leds);
    }

    @Override
    public byte[] getDefaultLEDAndBuzzerBehaviour() throws RemoteException {
        if (iAcr1222LCommandWrapper == null) {
            return noReaderException();
        }
        return iAcr1222LCommandWrapper.getDefaultLEDAndBuzzerBehaviour();
    }

    @Override
    public byte[] setDefaultLEDAndBuzzerBehaviour(int parameter) throws RemoteException {
        if (iAcr1222LCommandWrapper == null) {
            return noReaderException();
        }
        return iAcr1222LCommandWrapper.setDefaultLEDAndBuzzerBehaviour(parameter);
    }

    @Override
    public byte[] clearDisplay() throws RemoteException {
        if (iAcr1222LCommandWrapper == null) {
            return noReaderException();
        }
        return iAcr1222LCommandWrapper.clearDisplay();
    }

    @Override
    public byte[] displayText(char fontId, boolean styleBold, int line, int position, byte[] message) throws RemoteException {
        if (iAcr1222LCommandWrapper == null) {
            return noReaderException();
        }
        return iAcr1222LCommandWrapper.displayText(fontId, styleBold, line, position, message);
    }

    @Override
    public byte[] lightDisplayBacklight(boolean on) throws RemoteException {
        if (iAcr1222LCommandWrapper == null) {
            return noReaderException();
        }
        return iAcr1222LCommandWrapper.lightDisplayBacklight(on);
    }


}
