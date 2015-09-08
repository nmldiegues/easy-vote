package trustedcenter.exceptions;

import java.math.BigInteger;

public class InvalidCertificateException extends TrustedCenterException{


	private static final long serialVersionUID = 6929812369417230403L;
	private BigInteger number;
	private String name;
	
	public InvalidCertificateException() {
	}
	
	public InvalidCertificateException(String name, BigInteger number) {
		this.number = number;
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public BigInteger getNumber(){
		return this.number;
	}
	
}
