package ballotserver.views;

public class CandidateVotesView extends CandidateView{

	private static final long serialVersionUID = 5319427730537918033L;
	private Integer numberVotes;
	
	public CandidateVotesView(){
		super();
	}

	public CandidateVotesView(String name, Long uniqueIdentifier, Integer numberVotes) {
		super(name, uniqueIdentifier);
		this.numberVotes = numberVotes;
	}
	
	public CandidateVotesView(CandidateView ccv, Integer numberVotes){
		super(ccv.getName(), ccv.getUniqueIdentifier());
		this.numberVotes = numberVotes;
	}

	public Integer getNumberVotes() {
		return numberVotes;
	}

	public void setNumberVotes(Integer numberVotes) {
		this.numberVotes = numberVotes;
	}

}
