package com.github.skjolber.nfc.acs;


public class AcrReaderException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AcrReaderException() {
		super();
	}

	public AcrReaderException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public AcrReaderException(String detailMessage) {
		super(detailMessage);
	}

	public AcrReaderException(Throwable throwable) {
		super(throwable);
	}
	
}
