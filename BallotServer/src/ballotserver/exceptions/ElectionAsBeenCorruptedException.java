package ballotserver.exceptions;

public class ElectionAsBeenCorruptedException extends BallotServerException{

	
	private static final long serialVersionUID = 32423412315584L;
	private String error;
	
	public ElectionAsBeenCorruptedException() {
	}
	
	public ElectionAsBeenCorruptedException(String error) {
		this.error = error;
	}
	
	public ElectionAsBeenCorruptedException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}

}
