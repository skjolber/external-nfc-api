package com.github.skjolber.nfc.hce.tech;

import android.os.RemoteException;

import com.github.skjolber.android.nfc.TransceiveResult;

public interface CommandTechnology extends TagTechnology {

    TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException;

}
