package sirs.easyvote.exception;

public class ErrorRetrievingKeyFromTcException extends EasyVoteException {

	private static final long serialVersionUID = 3379039446667795133L;
	private String error;
	
	public ErrorRetrievingKeyFromTcException() {
	}
	
	public ErrorRetrievingKeyFromTcException(String error) {
		this.error = error;
	}
	
	public ErrorRetrievingKeyFromTcException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
