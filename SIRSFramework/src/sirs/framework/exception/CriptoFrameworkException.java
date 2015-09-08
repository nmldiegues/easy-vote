package sirs.framework.exception;

import java.io.Serializable;

public class CriptoFrameworkException extends RuntimeException implements Serializable{

	private static final long serialVersionUID = 3231478236994617359L;

	public CriptoFrameworkException() { }

	public CriptoFrameworkException(String message) {
		super(message);
	}

	public CriptoFrameworkException(Throwable cause) {
		super(cause);
	}
	
}
