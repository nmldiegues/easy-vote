package ballotserver.server;

import java.util.List;

import ballotserver.client.BallotServerService;
import ballotserver.views.ElectionResultView;
import ballotserver.views.ElectionView;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;


public class BallotServerServiceImpl extends RemoteServiceServlet implements
BallotServerService {

	private static final long serialVersionUID = -4149912719644116056L;
	
	private BallotServerApp app;
	
	public BallotServerServiceImpl() {
		app = BallotServerRoot.getBallotServerApp();
	}

	@Override
	public ElectionResultView countVotes(Long electionId) {
		return app.countVotes(electionId);
	}

	@Override
	public Long createNewElection(String question) {
		return app.createElection(question);
	}

	@Override
	public List<ElectionView> getElections() {
		return app.getElections();
	}
	
	@Override
	public void addCandidate(Long electionId, String name, Long id) {
		app.addCandidate(electionId, name, id);
	}

	@Override
	public void startElection(Long electionId) {
		app.startElection(electionId);
	}

	@Override
	public void closeElection() {
		app.closeElection();
	}

	@Override
	public void setElectionQuestion(Long electionId, String question) {
		app.setElectionQuestion(electionId, question);
	}
}
