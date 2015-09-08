package ballotserver.exceptions;

public class ElectionAlreadyExistsException extends BallotServerException{

	
	private static final long serialVersionUID = 1243648679523L;
	private String error;
	
	public ElectionAlreadyExistsException() {
	}
	
	public ElectionAlreadyExistsException(String error) {
		this.error = error;
	}
	
	public ElectionAlreadyExistsException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}

}
