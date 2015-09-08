package ballotserver.exceptions;

public class ElectionDoesNotExistException extends BallotServerException{

	private static final long serialVersionUID = 6929877369415330403L;
	private Long electionId;
	
	public ElectionDoesNotExistException() {
	}
	
	public ElectionDoesNotExistException(Long electionId) {
		this.electionId = electionId;
	}
	
	public Long getElectionId(){
		return this.electionId;
	}
}
