package ballotserver.widgets;

import java.util.List;

import ballotserver.client.Communicator;
import ballotserver.views.CandidateView;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CandidateListWidget extends VerticalPanel{
	
	private class Candidate extends HorizontalPanel {
		public Candidate(Long id, String name) {
			this.add(new Label("Candidate Id: " + id + " --- Name: " + name));
		}
	}
	
	private static final String NO_CANDIDATES = "There are no candidates for this election.";
	private static final String CANDIDATES_AVAILABLE = "List of candidates: ";
	
	public CandidateListWidget(List<CandidateView> candidates) {
		this.addStyleName("ballotServer-CandidateListWidget");
		
		if (candidates == null || candidates.size() == 0) {
			this.add(new Label(NO_CANDIDATES));
		} else {
			this.add(new Label(CANDIDATES_AVAILABLE));
			for (CandidateView cv : candidates) {
				this.add(new Candidate(cv.getUniqueIdentifier(), cv.getName()));
			}
		}
	}
}
