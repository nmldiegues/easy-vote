package sirs.easyvote.exception;

public class WrongPasswordException extends EasyVoteException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7090501008696102250L;
	private String error;
	private long voterID;
	
	public WrongPasswordException() {
	}
	
	public WrongPasswordException(String error, long voterID) {
		this.error = error;
		this.voterID = voterID;
	}
	
	public WrongPasswordException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
	
	public long getVoterID() {
		return this.voterID;
	}
}
