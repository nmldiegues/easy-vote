package registration.exceptions;

public class PersistenceException extends RegistrationException{

	private static final long serialVersionUID = -1617907510627093502L;
	private String error;
	
	public PersistenceException() {
	}
	
	public PersistenceException(String error) {
		this.error = error;
	}
	
	public PersistenceException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
