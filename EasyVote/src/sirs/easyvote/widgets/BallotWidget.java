package sirs.easyvote.widgets;

import java.util.ArrayList;
import java.util.List;

import sirs.easyvote.client.Communicator;
import sirs.easyvote.client.EasyVoteAsyncCallback;
import sirs.easyvote.shared.BallotSheetView;
import sirs.easyvote.shared.CandidateView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class BallotWidget extends FlowPanel {
	private VerticalPanel electionInfo;
	private Label electionIdLabel;
	private Label electionQuestion;
	private ArrayList<RadioButton> squares;
	private Grid optionsTable;
	private Button submitVoteButton;
	private Button submitBlankVoteButton;
	private EasyVoteAsyncCallback<Void> callback;
	
	public BallotWidget(BallotSheetView bsv, EasyVoteAsyncCallback<Void> cb) {
		electionInfo = new VerticalPanel();
		this.add(electionInfo);
		
		electionIdLabel = new Label("Election id: " + bsv.getElectionId());
		electionInfo.add(electionIdLabel);
		
		electionQuestion = new Label(bsv.getQuestion());
		this.add(electionQuestion);
		
		squares = new ArrayList<RadioButton>();
		callback = cb;
		List<CandidateView> candidateList = bsv.getCandidates();
		
		optionsTable = new Grid(candidateList.size()+1, 2);
		optionsTable.addStyleName("BallotWidget-optionsTable");
		for (int i = 0; i < candidateList.size(); i++) {
			optionsTable.getCellFormatter().addStyleName(i, 0, "BallotWidget-optionsTableCells");
			optionsTable.getCellFormatter().addStyleName(i, 1, "BallotWidget-optionsTableCells");
			optionsTable.getCellFormatter().setHorizontalAlignment(i, 1, HasHorizontalAlignment.ALIGN_CENTER);
			
			optionsTable.setText(i, 0, candidateList.get(i).getName());
			RadioButton button = new RadioButton("candidates");
			squares.add(button);
			optionsTable.setWidget(i, 1, button);
		}
		this.add(optionsTable);
		
		submitVoteButton = new Button("Submit Vote");
		submitVoteButton.addStyleName("BallotWidget-submitVoteButton");
		submitVoteButton.addClickHandler(new SubmitHandler());
		this.add(submitVoteButton);
		
		submitBlankVoteButton = new Button("Submit Blank Vote");
		submitBlankVoteButton.addStyleName("BallotWidget-submitVoteButton");
		submitBlankVoteButton.addClickHandler(new SubmitBlankHandler());
		this.add(submitBlankVoteButton);
		
		this.addStyleName("easyvote-BallotWidget");
	}
	
	private int getChosenSquarePosition() {
		for (int i = 0; i < squares.size(); i++) {
			if (squares.get(i).getValue() == true){
				return i;
			}
		}
		return -1;
	}
	
	private class SubmitHandler implements ClickHandler {
		public void onClick(ClickEvent event) {
			if (getChosenSquarePosition() != -1) {
				new VoteConfirmationWidget(getChosenSquarePosition(), callback).center();
				//Communicator.getInstance().submitVote(getChosenSquarePosition(), callback);
			}
		}
	}

	private class SubmitBlankHandler implements ClickHandler {
		public void onClick(ClickEvent event) {
			new VoteConfirmationWidget(-1, callback).center();
			//Communicator.getInstance().submitVote(-1, callback);
		
		}
	}
}
