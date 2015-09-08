package ballotserver.views;

import java.io.Serializable;
import java.util.List;

public class ElectionResultView implements Serializable{

	private static final long serialVersionUID = -6946389421293443151L;
	private Long electionId;
	private String electionQuestion;
	private List<CandidateVotesView> candidatesResults;
	private int totalVotes;
	private int expectedVotes;
	private int blankVotes;

	public ElectionResultView(){}
	
	public ElectionResultView(Long electionId, String electionQuestion, List<CandidateVotesView> candidatesResults,
			int totalVotes, int expectedVotes, int blankVotes){
		this.electionId = electionId;
		this.electionQuestion = electionQuestion;
		this.candidatesResults = candidatesResults;
		this.totalVotes = totalVotes;
		this.expectedVotes = expectedVotes;
		this.blankVotes = blankVotes;
	}

	public Long getElectionId() {
		return electionId;
	}
	
	public String getElectionQuestion() {
		return electionQuestion;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

	public List<CandidateVotesView> getCandidatesResults() {
		return candidatesResults;
	}

	public void setCandidatesResults(List<CandidateVotesView> candidatesResults) {
		this.candidatesResults = candidatesResults;
	}
	
	public int getTotalVotes() {
		return totalVotes;
	}
	
	public int getExpectedVotes() {
		return expectedVotes;
	}
	
	public int getBlankVotes() {
		return blankVotes;
	}
	
}
