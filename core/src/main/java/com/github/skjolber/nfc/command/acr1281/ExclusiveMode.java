package com.github.skjolber.nfc.command.acr1281;

public enum ExclusiveMode {

    SHARE(0x00),
    EXCLUSIVE(0x01);

    private ExclusiveMode(int value) {
        this.value = value;
    }

    private final int value;

    public int getValue() {
        return value;
    }

    public static ExclusiveMode parse(int value) {
        for (ExclusiveMode pollingInterval : values()) {
            if (value == pollingInterval.getValue()) {
                return pollingInterval;
            }
        }
        throw new IllegalArgumentException();
    }

    public byte[] getData() {
        byte[] data = new byte[1];

        data[0] = (byte) (value);

        return data;
    }
}