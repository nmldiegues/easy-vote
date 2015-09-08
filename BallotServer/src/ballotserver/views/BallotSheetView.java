package ballotserver.views;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ballotserver.exceptions.WrongBallotSheetViewFormatException;

public class BallotSheetView implements Serializable{

	private static final long serialVersionUID = -8480185508565346433L;
	private List<CandidateView> candidates;
	private byte[] safeOffset;
	private Long electionId;
	private String question;

	public BallotSheetView(){}
	
	public BallotSheetView(List<CandidateView> candidates, byte[] safeOffset, Long electionId, String question){
		this.candidates = candidates;
		this.safeOffset = safeOffset;
		this.electionId = electionId;
		this.question = question;
	}
	
	public BallotSheetView(String candidatesNamesAndIds, byte[] safeOffset, Long electionId, String question) throws WrongBallotSheetViewFormatException{
		this.safeOffset = safeOffset;
		this.candidates = new ArrayList<CandidateView>();
		String[] numberSignSplit = candidatesNamesAndIds.split("#");
		for(int i=0; i < numberSignSplit.length; i++){
			String[] commaSignSplit = numberSignSplit[i].split(",");
			if(commaSignSplit.length != 2){
				throw new WrongBallotSheetViewFormatException();
			}
			this.candidates.add(new CandidateView(commaSignSplit[0], 
					new Long(commaSignSplit[1])));
		}
		this.electionId = electionId;
		this.question = question;
	}
	
	public byte[] getSafeOffset(){
		return this.safeOffset;
	}

	public List<CandidateView> getCandidates() {
		return candidates;
	}
	
	public String produceStringFromBSView(){
		String result = new String("");
		for(CandidateView curCandidateView : getCandidates()){
			result += "#" + curCandidateView.getName() + "," + curCandidateView.getUniqueIdentifier();
		}
		//skipping first #
		return result.substring(1);
	}

	public Long getElectionId() {
		return electionId;
	}

	public String getQuestion() {
		return question;
	}
	
}
