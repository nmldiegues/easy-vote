package ballotserver.exceptions;

public class NoActiveElectionException extends BallotServerException {

	

	private static final long serialVersionUID = 1425346457457L;
	private String error;
	
	public NoActiveElectionException() {
	}
	
	public NoActiveElectionException(String error) {
		this.error = error;
	}
	
	public NoActiveElectionException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
