package com.skjolberg.nfc.command;

public class PassthroughCommandException extends RuntimeException {

    private int error;

    public PassthroughCommandException(String detailMessage, int error) {
        super(detailMessage);

        this.error = error;
    }

    public PassthroughCommandException(String detailMessage, Throwable throwable, int error) {
        super(detailMessage, throwable);

        this.error = error;
    }

    public PassthroughCommandException(Throwable throwable, int error) {
        super(throwable);

        this.error = error;

    }
}
