package ballotserver.client;

import ballotserver.exceptions.BallotServerException;
import ballotserver.exceptions.CandidateAlreadyExistsException;
import ballotserver.exceptions.CandidateDoesNotExistException;
import ballotserver.exceptions.CandidateIdFormatException;
import ballotserver.exceptions.ElectionAsBeenCorruptedException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class BallotServerAsyncCallback<T> implements AsyncCallback<T> {

	@Override
	public void onFailure(Throwable caught) {
		
		if(caught instanceof CandidateAlreadyExistsException) {
			BallotServer.popError("The Candidate with the ID " + 
					((CandidateAlreadyExistsException) caught).getCandidateID() + " already exists.");
		}else if(caught instanceof CandidateDoesNotExistException) {
			BallotServer.popError("The Candidate with the ID " + 
					((CandidateDoesNotExistException) caught).getCandidateID() + " does not exist.");
		}else if(caught instanceof ElectionAsBeenCorruptedException) {
			BallotServer.popError("The current election has been corrupted.");
		}else if(caught instanceof CandidateIdFormatException) {
			BallotServer.popError("The Candidate ID must be a number.");
		}else if(caught instanceof BallotServerException) {
			BallotServer.popError(((BallotServerException) caught).getMessage());
			caught.printStackTrace();
		}else if(caught instanceof Throwable) {
			caught.printStackTrace();
			BallotServer.popError("Error: " + caught.getMessage());
		}
		
		
		BallotServer.popError(caught.getMessage());
	}
}
