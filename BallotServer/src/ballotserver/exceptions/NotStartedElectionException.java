package ballotserver.exceptions;

public class NotStartedElectionException extends BallotServerException{

	private static final long serialVersionUID = 4543645L;
	private String error;
	
	public NotStartedElectionException() {
	}
	
	public NotStartedElectionException(String error) {
		this.error = error;
	}
	
	public NotStartedElectionException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}	

}
