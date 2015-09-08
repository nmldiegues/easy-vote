package sirs.easyvote.client;

import sirs.easyvote.exception.EasyVoteException;
import sirs.easyvote.exception.ElectionHasBeenCorruptedException;
import sirs.easyvote.exception.ErrorCastingVoteException;
import sirs.easyvote.exception.ErrorCreatingVoterCertException;
import sirs.easyvote.exception.ErrorRetrievingBallotSheetException;
import sirs.easyvote.exception.ErrorRetrievingKeyFromTcException;
import sirs.easyvote.exception.IdFormatException;
import sirs.easyvote.exception.NoElectionActiveException;
import sirs.easyvote.exception.RequiredLoginException;
import sirs.easyvote.exception.UserAlreadyVotedException;
import sirs.easyvote.exception.VoterDoesNotExistException;
import sirs.easyvote.exception.WrongPasswordException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

public abstract class EasyVoteAsyncCallback<T> implements AsyncCallback<T> {

	@Override
	public void onFailure(Throwable caught) {
		
		if(caught instanceof ErrorCreatingVoterCertException) {
			EasyVote.setError(new HTML("Couldn't create a certificate for the user " + 
					((ErrorCreatingVoterCertException) caught).getVoterID()));
		}else if(caught instanceof WrongPasswordException) {
			EasyVote.setError(new HTML("Invalid Password for the user: " + ((WrongPasswordException) caught).getVoterID()));
		}else if(caught instanceof VoterDoesNotExistException) {
			EasyVote.setError(new HTML("The user with the ID " + ((VoterDoesNotExistException) caught).getVoterID() + " is not elegible for voting."));
		}else if(caught instanceof ErrorRetrievingKeyFromTcException) {
			EasyVote.setError(new HTML("An error ocurred while trying to get a key from trustedcenter."));
		}else if(caught instanceof RequiredLoginException) {
			EasyVote.setError(new HTML("Login required to use that function."));
		}else if(caught instanceof UserAlreadyVotedException) {
			EasyVote.setError(new HTML("The user with the ID " + ((UserAlreadyVotedException) caught).getVoterID() + " has already casted a vote."));
		}else if(caught instanceof ElectionHasBeenCorruptedException) {
			EasyVote.setError(new HTML("The election has been corrupted!"));
		}else if(caught instanceof ErrorCastingVoteException) {
			EasyVote.setError(new HTML("An error ocurred while casting the vote."));
		}else if(caught instanceof ErrorRetrievingBallotSheetException) {
			EasyVote.setError(new HTML("An error ocurred while getting the ballot sheet."));
			caught.printStackTrace();
		}else if(caught instanceof NoElectionActiveException) {
			EasyVote.setError(new HTML("There isn't any active election."));
		}else if(caught instanceof IdFormatException) {
			EasyVote.setError(new HTML("The ID of the user must be a number."));
		}else if(caught instanceof EasyVoteException) {
			EasyVote.setError(new HTML(((EasyVoteException) caught).getError()));
			caught.printStackTrace();
		}else if(caught instanceof Throwable) {
			caught.printStackTrace(System.out);
			EasyVote.setError(new HTML("Error: "  + caught.getMessage()));
		}
		
	}

	
	

}
