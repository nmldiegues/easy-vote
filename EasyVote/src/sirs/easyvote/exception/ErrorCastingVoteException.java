package sirs.easyvote.exception;

public class ErrorCastingVoteException extends EasyVoteException {

	private static final long serialVersionUID = 3379039446667795133L;
	private String error;
	
	public ErrorCastingVoteException() {
	}
	
	public ErrorCastingVoteException(String error) {
		this.error = error;
	}
	
	public ErrorCastingVoteException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
