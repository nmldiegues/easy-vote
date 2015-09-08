package ballotserver.exceptions;

public class ErrorRetreivingTCKeyException extends BallotServerException{

	private static final long serialVersionUID = 6929877369415330403L;
	private String error;
	
	public ErrorRetreivingTCKeyException() {
	}
	
	public ErrorRetreivingTCKeyException(String error) {
		this.error = error;
	}
	
	public ErrorRetreivingTCKeyException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
