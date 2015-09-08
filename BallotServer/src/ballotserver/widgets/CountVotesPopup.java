package ballotserver.widgets;

import ballotserver.views.CandidateVotesView;
import ballotserver.views.ElectionResultView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CountVotesPopup extends DialogBox {
	private VerticalPanel basePanel;
	private Button closeButton;

	
	public CountVotesPopup(ElectionResultView countView) {
		
		super(false, true);
		basePanel = new VerticalPanel();
		this.setWidget(basePanel);
		this.setText("Election Results - " + countView.getElectionQuestion() + " - id: " + countView.getElectionId());
				
		for (CandidateVotesView vv : countView.getCandidatesResults()) {
			basePanel.add(new Label("Candidate " + vv.getName() + 
					" (id: " + vv.getUniqueIdentifier() + ") has " + vv.getNumberVotes() +
					" (" + formatString("" + (vv.getNumberVotes()*100)/(double)countView.getTotalVotes()) + "%) votes."));
			
		}
		
		basePanel.add(new HTML("<br>"));
		
		basePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		basePanel.add(new ResultChartWidget(countView));
		
		basePanel.add(new HTML("<br>"));
		
		basePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		basePanel.add(new Label("Total number of votes: " + countView.getTotalVotes()));
		basePanel.add(new Label("Blank Votes: " + countView.getBlankVotes()));
		basePanel.add(new Label("Abstention: " + ((countView.getExpectedVotes()-countView.getTotalVotes())*100)/(double)countView.getExpectedVotes() + "%"));
		
		closeButton = new Button("Close");
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent arg0) {
				hide();
			}
		});
		basePanel.add(closeButton);
	}
	
	public String formatString(String s) {
		if(s.length() < 7) {
			return s;
		} else {
			return s.substring(0, 6);
		}
	}
}
