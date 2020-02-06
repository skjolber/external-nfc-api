package com.skjolberg.nfc.command;

public enum FontSet {
    Font1(1, 0x00, 2, 0x40),
    Font2(2, 0x01, 2, 0x40),
    Font3(3, 0x10, 4, 0x20);

    private final int number;
    private final int value;
    private final int lines;
    private final int addressIncrement;

    private FontSet(int number, int value, int lines, int addressIncrement) {
        this.number = number;
        this.value = value;
        this.lines = lines;
        this.addressIncrement = addressIncrement;
    }

    public int getNumber() {
        return number;
    }

    public int getValue() {
        return value;
    }

    public int getLines() {
        return lines;
    }

    public int getLineLength() {
        return 16;
    }

    public int getMaxCharacters() {
        return lines * getLineLength();
    }

    public int getAddressIncrement() {
        return addressIncrement;
    }

    public static FontSet parse(int number) {
        for (FontSet font : values()) {
            if (font.getNumber() == number) {
                return font;
            }
        }

        throw new IllegalArgumentException();
    }
}