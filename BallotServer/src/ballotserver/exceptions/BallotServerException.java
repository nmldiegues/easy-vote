package ballotserver.exceptions;

import java.io.Serializable;

public class BallotServerException extends RuntimeException implements Serializable{
	private static final long serialVersionUID = 1L;

	public BallotServerException() { }

	public BallotServerException(String message) {
		super(message);
	}

	public BallotServerException(Throwable cause) {
		super(cause);
	}
	
}

