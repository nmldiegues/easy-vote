package registration.exceptions;

public class RegistrationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RegistrationException() { }

	public RegistrationException(String message) {
		super(message);
	}

	public RegistrationException(Throwable cause) {
		super(cause);
	}
	
}

