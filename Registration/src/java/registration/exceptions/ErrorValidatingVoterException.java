package registration.exceptions;

public class ErrorValidatingVoterException extends RegistrationException{

	private static final long serialVersionUID = 3218590675658605611L;
	
private String error;
	
	public ErrorValidatingVoterException() {
	}
	
	public ErrorValidatingVoterException(String error) {
		this.error = error;
	}
	
	public ErrorValidatingVoterException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
