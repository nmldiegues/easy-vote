package sirs.easyvote.shared;

import java.io.Serializable;

public class TokenView implements Serializable {
	private static final long serialVersionUID = -660831807596407495L;
	private String TCtoken;
	private String Rtoken;
	
	public TokenView() {
		
	}

	public void setTCtoken(String tCtoken) {
		TCtoken = tCtoken;
	}

	public String getTCtoken() {
		return TCtoken;
	}

	public void setRtoken(String rtoken) {
		Rtoken = rtoken;
	}

	public String getRtoken() {
		return Rtoken;
	}
}
