package ballotserver.exceptions;

public class MultipleActiveElectionsAttemptException extends BallotServerException {
	private static final long serialVersionUID = 425367657564400L;
	private String error;
	
	public MultipleActiveElectionsAttemptException() {
	}
	
	public MultipleActiveElectionsAttemptException(String error) {
		this.error = error;
	}
	
	public MultipleActiveElectionsAttemptException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
