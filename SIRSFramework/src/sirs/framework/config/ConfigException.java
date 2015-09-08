package sirs.framework.config;

/**
 * Exception type used for configuration-related problems.
 */
public class ConfigException extends Exception {

	private static final long serialVersionUID = 1L;

	public ConfigException(String message) {
		super(message);
	}

	public ConfigException(Throwable cause) {
		super(cause);
	}

	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}

}
