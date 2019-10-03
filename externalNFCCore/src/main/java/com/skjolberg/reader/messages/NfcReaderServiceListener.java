package com.skjolberg.reader.messages;

public interface NfcReaderServiceListener {

    void onServiceStarted();

    void onServiceStopped();

    void onReaderOpen(Object acrCommands, int status);

    void onReaderClosed(int status, String message);
}
