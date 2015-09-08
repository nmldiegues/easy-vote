package sirs.easyvote.exception;

public class ElectionHasBeenCorruptedException extends EasyVoteException{

	
	private static final long serialVersionUID = 6155561648053717539L;
	private String error;
	
	public ElectionHasBeenCorruptedException() {
	}
	
	public ElectionHasBeenCorruptedException(String error) {
		this.error = error;
	}
	
	public ElectionHasBeenCorruptedException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
