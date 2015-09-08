package trustedcenter.exceptions;

public class TCCriptographicException extends TrustedCenterException{


	private static final long serialVersionUID = 6929877369415330403L;
	private String error;
	
	public TCCriptographicException() {
	}
	
	public TCCriptographicException(String error) {
		this.error = error;
	}
	
	public TCCriptographicException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
	
}
