package trustedcenter.exceptions;

public class PersistenceException extends TrustedCenterException{

	private static final long serialVersionUID = 4167864788108645250L;
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
