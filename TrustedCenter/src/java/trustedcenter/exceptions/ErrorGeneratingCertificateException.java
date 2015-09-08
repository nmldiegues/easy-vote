package trustedcenter.exceptions;

public class ErrorGeneratingCertificateException extends TrustedCenterException{


	private static final long serialVersionUID = 6929877369415330403L;
	private String error;
	
	public ErrorGeneratingCertificateException() {
	}
	
	public ErrorGeneratingCertificateException(String error) {
		this.error = error;
	}
	
	public ErrorGeneratingCertificateException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
	
}
