package sirs.easyvote.exception;

public class UserAlreadyVotedException extends EasyVoteException{

	
	private static final long serialVersionUID = -234235435616L;
	private long voterID;
	
	public UserAlreadyVotedException() {
	}
	
	public UserAlreadyVotedException(long voterID) {
		this.voterID = voterID;
	}
	
	public UserAlreadyVotedException(Throwable cause){
		super(cause);
	}
	
	
	public long getVoterID() {
		return this.voterID;
	}

}
