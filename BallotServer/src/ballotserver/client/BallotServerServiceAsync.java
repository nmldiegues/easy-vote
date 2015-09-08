package ballotserver.client;

import java.util.List;

import ballotserver.views.ElectionResultView;
import ballotserver.views.ElectionView;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>EasyVoteService</code>.
 */
public interface BallotServerServiceAsync {
	
	void countVotes(Long electionId, AsyncCallback<ElectionResultView> callback);
	
	void createNewElection(String question, AsyncCallback<Long> callback);
	
	void getElections(AsyncCallback<List<ElectionView>> callback);
	
	void setElectionQuestion(Long electionId, String question, AsyncCallback<Void> callback);

	void addCandidate(Long electionId, String name, Long id, AsyncCallback<Void> callback);

	void startElection(Long electionId, AsyncCallback<Void> callback);

	void closeElection(AsyncCallback<Void> updateCallback);
}