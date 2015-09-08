package sirs.easyvote.exception;

public class ErrorCreatingVoterCertException extends EasyVoteException{

	private static final long serialVersionUID = -3835443060718921705L;
	private String error;
	private long voterID;
	
	public ErrorCreatingVoterCertException() {
	}
	
	public ErrorCreatingVoterCertException(String error, long voterID) {
		this.error = error;
		this.voterID = voterID;
	}
	
	public ErrorCreatingVoterCertException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
	
	public long getVoterID() {
		return this.voterID;
	}
}
