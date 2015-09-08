package ballotserver.exceptions;

public class ElectionAlreadyClosedException extends BallotServerException{


	private static final long serialVersionUID = 543647457L;
	private String error;
	
	public ElectionAlreadyClosedException() {
	}
	
	public ElectionAlreadyClosedException(String error) {
		this.error = error;
	}
	
	public ElectionAlreadyClosedException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}

}
