package com.github.skjolber.nfc.command;

public class ReaderCommandException extends RuntimeException {

    public ReaderCommandException(String detailMessage) {
        super(detailMessage);
    }

    public ReaderCommandException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ReaderCommandException(Throwable throwable) {
        super(throwable);
    }
}
