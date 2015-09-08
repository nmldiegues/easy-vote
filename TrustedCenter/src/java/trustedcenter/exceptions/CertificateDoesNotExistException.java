package trustedcenter.exceptions;

public class CertificateDoesNotExistException extends TrustedCenterException{


	private static final long serialVersionUID = 1929877369417230403L;
	private String identifier;
	
	public CertificateDoesNotExistException() {
	}
	
	public CertificateDoesNotExistException(String identifier) {
		this.identifier = identifier;
	}
	
	public String getError(){
		return this.identifier;
	}
	
}
