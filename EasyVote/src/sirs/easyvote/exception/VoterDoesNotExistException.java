package sirs.easyvote.exception;

import sirs.easyvote.exception.EasyVoteException;

public class VoterDoesNotExistException extends EasyVoteException {

	
	private static final long serialVersionUID = 8657010098274206452L;
	private String error;
	private long voterID;
	
	public VoterDoesNotExistException() {
	}
	
	public VoterDoesNotExistException(String error, long voterID) {
		this.error = error;
		this.voterID = voterID;
	}
	
	public VoterDoesNotExistException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
	
	public long getVoterID() {
		return this.voterID;
	}
}
