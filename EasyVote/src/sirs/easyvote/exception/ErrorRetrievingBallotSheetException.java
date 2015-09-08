package sirs.easyvote.exception;

public class ErrorRetrievingBallotSheetException extends EasyVoteException {

	private static final long serialVersionUID = 3379039446667795133L;
	private String error;
	
	public ErrorRetrievingBallotSheetException() {
	}
	
	public ErrorRetrievingBallotSheetException(String error) {
		this.error = error;
	}
	
	public ErrorRetrievingBallotSheetException(Throwable cause){
		super(cause);
	}
	
	public String getError(){
		return this.error;
	}
}
