package sirs.easyvote.exception;

public class NoElectionActiveException extends EasyVoteException{


	private static final long serialVersionUID = -6900190100158838524L;
	private String error;
	
	public NoElectionActiveException() {
	}
	
	public NoElectionActiveException(String error) {
		this.error = error;
	}
	
	public NoElectionActiveException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
