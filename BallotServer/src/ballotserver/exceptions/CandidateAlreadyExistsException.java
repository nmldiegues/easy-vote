package ballotserver.exceptions;

public class CandidateAlreadyExistsException extends BallotServerException{


	private static final long serialVersionUID = 2123252523626L;
	private Long candidateID;
	
	public CandidateAlreadyExistsException() {
	}
	
	public CandidateAlreadyExistsException(Long candidateID) {
		this.candidateID = candidateID;
	}
	
	public Long getCandidateID(){
		return this.candidateID;
	}
}
