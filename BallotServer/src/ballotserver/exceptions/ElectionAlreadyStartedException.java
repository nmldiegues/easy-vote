package ballotserver.exceptions;

public class ElectionAlreadyStartedException extends BallotServerException {


	private static final long serialVersionUID = 32425325L;
	private String error;
	
	public ElectionAlreadyStartedException() {
	}
	
	public ElectionAlreadyStartedException(String error) {
		this.error = error;
	}
	
	public ElectionAlreadyStartedException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}



}
