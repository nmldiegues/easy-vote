package sirs.easyvote.exception;

public class RequiredLoginException extends EasyVoteException{

	private static final long serialVersionUID = 5097353760065928886L;
	String error;

	public RequiredLoginException() {
	}

	public RequiredLoginException(String error) {
		this.error = error;
	}
	
	public RequiredLoginException(Throwable cause) {
		super(cause);
	}

	public String getError() {
		return error;
	}
}
