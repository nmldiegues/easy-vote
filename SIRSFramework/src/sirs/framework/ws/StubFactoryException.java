package sirs.framework.ws;

/**
 * This exception type can be thrown by stub factories when they have problems
 * creating stubs for whatever reason
 */
public class StubFactoryException extends Exception {

	private static final long serialVersionUID = 1L;

	public StubFactoryException(String message) {
		super(message);
	}

	public StubFactoryException(Throwable cause) {
		super(cause);
	}

	public StubFactoryException(String message, Throwable cause) {
		super(message, cause);
	}

}
