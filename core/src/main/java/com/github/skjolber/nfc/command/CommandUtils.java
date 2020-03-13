package com.github.skjolber.nfc.command;


import custom.java.ResponseAPDU;

public class CommandUtils {

    public static boolean isSuccessResponse(ResponseAPDU response) {
        return (response.getSW1() == 0x90 && response.getSW2() == 00);
    }

    public static boolean isFailResponse(ResponseAPDU response) {
        return (response.getSW1() == 0x63 && response.getSW2() == 00);
    }

}
