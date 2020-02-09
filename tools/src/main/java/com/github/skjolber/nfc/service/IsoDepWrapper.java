package com.github.skjolber.nfc.service;

import com.acs.smartcard.ReaderException;

public interface IsoDepWrapper {

    byte[] transceive(byte[] data) throws ReaderException;

    byte[] transmitPassThrough(byte[] req) throws ReaderException;

}
