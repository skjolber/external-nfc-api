package com.github.skjolber.nfc.hce.tech;

import android.nfc.TransceiveResult;
import android.os.RemoteException;

public interface CommandTechnology extends TagTechnology {

    TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException;

}
