package com.skjolberg.nfc.command;


import custom.java.ResponseAPDU;

public class CommandUtils {

    public static boolean isSuccessResponse(ResponseAPDU response) {
        return (response.getSW1() == 90 && response.getSW2() == 00);
    }

    public static boolean isFailResponse(ResponseAPDU response) {
        return (response.getSW1() == 63 && response.getSW2() == 00);
    }

}
