package trustedcenter.exceptions;

import java.io.Serializable;

public class TrustedCenterException extends RuntimeException implements Serializable{
	private static final long serialVersionUID = 1L;

	public TrustedCenterException() { }

	public TrustedCenterException(String message) {
		super(message);
	}

	public TrustedCenterException(Throwable cause) {
		super(cause);
	}
	
}

