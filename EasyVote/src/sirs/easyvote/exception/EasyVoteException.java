package sirs.easyvote.exception;

import java.io.Serializable;

public class EasyVoteException extends RuntimeException implements Serializable {

	private static final long serialVersionUID = -1218590696230205271L;
	String error;

	public EasyVoteException() {
	}

	public EasyVoteException(String error) {
		this.error = error;
	}
	
	public EasyVoteException(Throwable cause) {
		super(cause);
	}

	public String getError() {
		return error;
	}

}
