package registration.exceptions;

public class NotEligibleVoterException extends RegistrationException{


	private static final long serialVersionUID = -6468578397692128392L;
	private String error;
	
	public NotEligibleVoterException() {
	}
	
	public NotEligibleVoterException(String error) {
		this.error = error;
	}
	
	public NotEligibleVoterException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
