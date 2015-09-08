package ballotserver.views;

import java.io.Serializable;
import java.util.List;

public class ElectionView implements Serializable {
	private static final long serialVersionUID = 1134243534534L;
	private Long electionId;
	private String question;
	private List<CandidateView> candidates;
	private boolean started;
	private boolean closed;

	public ElectionView(){}
	
	public ElectionView(Long electionId, String question, List<CandidateView> candidates){
		this.electionId = electionId;
		this.candidates = candidates;
		this.question = question;
		this.started = false;
		this.started = false;
	}

	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

	public List<CandidateView> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<CandidateView> candidates) {
		this.candidates = candidates;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public boolean getStarted() {
		return started;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public boolean getClosed() {
		return closed;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getQuestion() {
		return question;
	}
}
