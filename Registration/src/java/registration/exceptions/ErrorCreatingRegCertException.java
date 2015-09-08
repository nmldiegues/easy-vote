package registration.exceptions;

public class ErrorCreatingRegCertException extends RegistrationException{
	
	private static final long serialVersionUID = -6468798397692128392L;
	private String error;
	
	public ErrorCreatingRegCertException() {
	}
	
	public ErrorCreatingRegCertException(String error) {
		this.error = error;
	}
	
	public ErrorCreatingRegCertException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
