package ballotserver.exceptions;

public class CandidateDoesNotExistException extends BallotServerException{

	private static final long serialVersionUID = 22325342523626L;
	private Long candidateID;
	
	public CandidateDoesNotExistException() {
	}
	
	public CandidateDoesNotExistException(Long candidateID) {
		this.candidateID = candidateID;
	}
	
	public Long getCandidateID(){
		return this.candidateID;
	}
}

