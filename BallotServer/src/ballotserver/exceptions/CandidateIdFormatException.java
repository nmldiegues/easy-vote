package ballotserver.exceptions;

public class CandidateIdFormatException extends BallotServerException{


	private static final long serialVersionUID = 46658314121L;
	private String error;
	
	public CandidateIdFormatException() {
	}
	
	public CandidateIdFormatException(String error) {
		this.error = error;
	}
	
	public CandidateIdFormatException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}

}
