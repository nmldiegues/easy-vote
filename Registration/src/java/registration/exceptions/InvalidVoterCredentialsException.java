package registration.exceptions;

public class InvalidVoterCredentialsException extends RegistrationException{

	private static final long serialVersionUID = -5200397643712312945L;

private String error;
	
	public InvalidVoterCredentialsException() {
	}
	
	public InvalidVoterCredentialsException(String error) {
		this.error = error;
	}
	
	public InvalidVoterCredentialsException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}

}
