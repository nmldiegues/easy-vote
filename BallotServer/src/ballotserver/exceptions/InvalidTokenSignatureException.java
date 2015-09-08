package ballotserver.exceptions;

public class InvalidTokenSignatureException extends BallotServerException{

	private static final long serialVersionUID = 6929877369415330403L;
	private String error;
	
	public InvalidTokenSignatureException() {
	}
	
	public InvalidTokenSignatureException(String error) {
		this.error = error;
	}
	
	public InvalidTokenSignatureException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
