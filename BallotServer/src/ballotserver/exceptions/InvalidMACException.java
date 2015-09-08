package ballotserver.exceptions;

public class InvalidMACException extends BallotServerException{

	private static final long serialVersionUID = -5200397643712312945L;

	private String error;
	
	public InvalidMACException() {
	}
	
	public InvalidMACException(String error) {
		this.error = error;
	}
	
	public InvalidMACException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}

}
