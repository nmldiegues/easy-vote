package ballotserver.exceptions;

public class VoterAlreadySubmitedVoteException extends BallotServerException{

	private static final long serialVersionUID = 5207497643712312945L;

	private String error;
	
	public VoterAlreadySubmitedVoteException() {
	}
	
	public VoterAlreadySubmitedVoteException(String error) {
		this.error = error;
	}
	
	public VoterAlreadySubmitedVoteException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
