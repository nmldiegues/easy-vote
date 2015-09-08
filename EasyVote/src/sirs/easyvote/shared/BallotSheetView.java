package sirs.easyvote.shared;

import java.io.Serializable;
import java.util.List;

public class BallotSheetView implements Serializable {
	private static final long serialVersionUID = -4144073679780624818L;
	private Long electionId;
	private List<CandidateView> candidates;
	private String safeOffset;
	private String question;
	
	public BallotSheetView() {
		
	}

	public void setCandidates(List<CandidateView> candidates) {
		this.candidates = candidates;
	}

	public List<CandidateView> getCandidates() {
		return candidates;
	}

	public void setSafeOffset(String safeOffset) {
		this.safeOffset = safeOffset;
	}

	public String getSafeOffset() {
		return safeOffset;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

	public Long getElectionId() {
		return electionId;
	}

	public void setQuestion(String question) {
		this.question = question;
	}
	
	public String getQuestion() {
		return question;
	}
}
