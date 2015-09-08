package ballotserver.client;

import java.util.List;

import ballotserver.views.ElectionResultView;
import ballotserver.views.ElectionView;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("service")
public interface BallotServerService extends RemoteService {
	
	ElectionResultView countVotes(Long electionId);
	
	Long createNewElection(String question);
	
	List<ElectionView> getElections();
	
	void setElectionQuestion(Long electionId, String question);
	
	void addCandidate(Long electionId, String name, Long id);
	
	void startElection(Long electionId);
	
	void closeElection();
}
