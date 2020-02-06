package com.skjolberg.nfc.command.acr1281;

public class PICCOperatingParameter {

    public static final int ISO_14443_TYPE_A = 1 << 0;
    public static final int ISO_14443_TYPE_B = 1 << 1;

    private boolean iso14443TypeA;
    private boolean iso14443TypeB;

    public PICCOperatingParameter(int value) {
        iso14443TypeA = (value & ISO_14443_TYPE_A) != 0;
        iso14443TypeB = (value & ISO_14443_TYPE_B) != 0;
    }

    public PICCOperatingParameter() {
    }

    public boolean isIso14443TypeA() {
        return iso14443TypeA;
    }

    public void setIso14443TypeA(boolean iso14443TypeA) {
        this.iso14443TypeA = iso14443TypeA;
    }

    public boolean isIso14443TypeB() {
        return iso14443TypeB;
    }

    public void setIso14443TypeB(boolean iso14443TypeB) {
        this.iso14443TypeB = iso14443TypeB;
    }

    public byte getOperation() {
        int parameter = 0;

        if (iso14443TypeA) {
            parameter |= ISO_14443_TYPE_A;
        }

        if (iso14443TypeB) {
            parameter |= ISO_14443_TYPE_B;
        }

        if ((parameter & 0xFF) != parameter) throw new RuntimeException();

        return (byte) (parameter & 0xFF);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (iso14443TypeA ? 1231 : 1237);
        result = prime * result + (iso14443TypeB ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PICCOperatingParameter other = (PICCOperatingParameter) obj;
        if (iso14443TypeA != other.iso14443TypeA)
            return false;
        if (iso14443TypeB != other.iso14443TypeB)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PICCOperatingParameter [iso14443TypeA=" + iso14443TypeA
                + ", iso14443TypeB=" + iso14443TypeB + "]";
    }

    public byte[] getData() {
        byte[] data = new byte[1];

        data[0] = (byte) getOperation();

        return data;
    }
}
