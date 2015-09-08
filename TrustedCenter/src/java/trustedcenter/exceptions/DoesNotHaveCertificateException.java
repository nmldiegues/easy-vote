package trustedcenter.exceptions;

public class DoesNotHaveCertificateException extends TrustedCenterException{


	private static final long serialVersionUID = 6929877369417230403L;
	private String error;
	
	public DoesNotHaveCertificateException() {
	}
	
	public DoesNotHaveCertificateException(String error) {
		this.error = error;
	}
	
	public String getError(){
		return this.error;
	}
	
}
