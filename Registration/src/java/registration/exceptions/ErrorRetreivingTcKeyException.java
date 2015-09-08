package registration.exceptions;

public class ErrorRetreivingTcKeyException extends RegistrationException {
	
	private static final long serialVersionUID = -6468534322128392L;
	private String error;
	
	public ErrorRetreivingTcKeyException() {
	}
	
	public ErrorRetreivingTcKeyException(String error) {
		this.error = error;
	}
	
	public ErrorRetreivingTcKeyException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}

}
