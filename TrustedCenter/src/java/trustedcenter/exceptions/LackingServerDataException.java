package trustedcenter.exceptions;


public class LackingServerDataException extends TrustedCenterException{


	private static final long serialVersionUID = 6929812369417230403L;
	private String error;
	
	public LackingServerDataException() {
	}
	
	public LackingServerDataException(String error) {
		this.error = error;
	}
	
	public String getError(){
		return this.error;
	}
	
}
