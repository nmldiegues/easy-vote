package sirs.easyvote.exception;

public class IdFormatException extends EasyVoteException{


	private static final long serialVersionUID = 43254587591L;
	private String error;
	
	public IdFormatException() {
	}
	
	public IdFormatException(String error) {
		this.error = error;
	}
	
	public IdFormatException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
