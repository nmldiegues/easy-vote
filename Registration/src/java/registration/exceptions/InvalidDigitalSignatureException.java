package registration.exceptions;

public class InvalidDigitalSignatureException extends RegistrationException{

	private static final long serialVersionUID = -5200397643712312945L;

private String error;
	
	public InvalidDigitalSignatureException() {
	}
	
	public InvalidDigitalSignatureException(String error) {
		this.error = error;
	}
	
	public InvalidDigitalSignatureException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}

}
