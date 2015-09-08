package ballotserver.widgets;

import ballotserver.client.BallotServer;
import ballotserver.client.BallotServerAsyncCallback;
import ballotserver.client.Communicator;
import ballotserver.exceptions.CandidateIdFormatException;
import ballotserver.views.ElectionResultView;
import ballotserver.views.ElectionView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ElectionWidget extends VerticalPanel {
	private Long electionId;
	private ElectionsPanel.UpdateCallback updater;
	private Label idLabel;
	private Label statusLabel;
	private Label questionLabel;
	private CandidateListWidget candidateListWidget;
	private HorizontalPanel questionPanel;
	private TextBox questionTB;
	private Button questionButton;
	private TextBox addCandidateNameTB;
	private TextBox addCandidateIdTB;
	private Button addCandidateButton;
	private HorizontalPanel operationsPanel;
	private Button countVotesButton;
	private Button startElectionButton;
	private Button closeElectionButton;
	
	public ElectionWidget(ElectionView ev, ElectionsPanel.UpdateCallback updater) {
		this.electionId = ev.getElectionId();
		this.updater = updater;
		this.addStyleName("ballotServer-ElectionWidget");
		
		idLabel = new Label("Election id: " +electionId.toString());
		this.add(idLabel);
		
		statusLabel = new Label();
		if (!ev.getStarted() && !ev.getClosed()) {
			statusLabel.setText("Status: Not started");
		} else if (ev.getStarted() && !ev.getClosed()) {
			statusLabel.setText("Status: Open");
		} else if (ev.getStarted() && ev.getClosed()) {
			statusLabel.setText("Status: Closed");
		} else {
			statusLabel.setText("Status: Not supposed");
		}
		this.add(statusLabel);
		
		questionLabel = new Label(ev.getQuestion());
		this.add(questionLabel);
		
		candidateListWidget = new CandidateListWidget(ev.getCandidates());
		this.add(candidateListWidget);
		
		if (!ev.getStarted() && !ev.getClosed()) {
			questionPanel = new HorizontalPanel();
			questionPanel.addStyleName("toolbox");
			this.add(questionPanel);
			SetQuestionHandler questionHandler = new SetQuestionHandler();

			questionTB = new TextBox();
			questionTB.addStyleName("tool");
			questionTB.setText("Election's question");
			questionTB.addKeyPressHandler(questionHandler);
			questionPanel.add(questionTB);

			questionButton = new Button("Set Question");
			questionButton.addStyleName("tool");
			questionButton.addClickHandler(questionHandler);
			questionPanel.add(questionButton);
		}
		
		operationsPanel = new HorizontalPanel();
		operationsPanel.addStyleName("toolbox");
		this.add(operationsPanel);
		
		if (!ev.getStarted() && !ev.getClosed()) {
			AddCandidateHandler handler = new AddCandidateHandler();
			
			addCandidateIdTB = new TextBox();
			addCandidateIdTB.addStyleName("tool");
			addCandidateIdTB.setText("New candidate's id");
			addCandidateIdTB.addKeyPressHandler(handler);
			operationsPanel.add(addCandidateIdTB);
			
			addCandidateNameTB = new TextBox();
			addCandidateNameTB.addStyleName("tool");
			addCandidateNameTB.setText("New candidate's name");
			addCandidateNameTB.setWidth("200px");
			addCandidateNameTB.addKeyPressHandler(handler);
			operationsPanel.add(addCandidateNameTB);
			
			addCandidateButton = new Button("Add Candidate");
			addCandidateButton.addStyleName("tool");
			addCandidateButton.addClickHandler(handler);
			operationsPanel.add(addCandidateButton);
		}
		
		if (!ev.getStarted() && !ev.getClosed()) {
			startElectionButton = new Button("Start Election");
			startElectionButton.addStyleName("tool");
			startElectionButton.addClickHandler(new StartHandler());
			operationsPanel.add(startElectionButton);
		}
		
		if (ev.getStarted() && !ev.getClosed()) {
			closeElectionButton = new Button("Close Election");
			closeElectionButton.addStyleName("tool");
			closeElectionButton.addClickHandler(new CloseElectionHandler());
			operationsPanel.add(closeElectionButton);
		}
		
		if (ev.getStarted() && ev.getClosed()) {
			countVotesButton = new Button("Count Votes");
			countVotesButton.addStyleName("tool");
			countVotesButton.addClickHandler(new CountVotesHandler());
			operationsPanel.add(countVotesButton);
		}
	}
	
	private class AddCandidateHandler implements ClickHandler, KeyPressHandler {
		public void onClick(ClickEvent arg0) {
			
			Long candidateID = null;
			
			try{
				candidateID = Long.parseLong(addCandidateIdTB.getText());
			}  catch(NumberFormatException e) {
				BallotServer.popError("The Candidate ID must be a number.");
				return;
			}
			
			Communicator.getInstance().getService().addCandidate(electionId, 
					addCandidateNameTB.getText(), candidateID,
					new UpdateCallback());
		}
		public void onKeyPress(KeyPressEvent arg0) {
			if (arg0.getCharCode() == KeyCodes.KEY_ENTER) {
				
				Long candidateID = null;
				
				try{
					candidateID = Long.parseLong(addCandidateIdTB.getText());
				}  catch(NumberFormatException e) {
					BallotServer.popError("The Candidate ID must be a number.");
					return;
				}
				
				Communicator.getInstance().getService().addCandidate(electionId, 
						addCandidateNameTB.getText(), candidateID,
						new UpdateCallback());
			}
		}
	}
	
	private class SetQuestionHandler implements ClickHandler, KeyPressHandler {
		public void onClick(ClickEvent arg0) {
			Communicator.getInstance().getService().setElectionQuestion(
					electionId, questionTB.getText(), new UpdateCallback());
		}
		
		public void onKeyPress(KeyPressEvent arg0) {
			if (arg0.getCharCode() == KeyCodes.KEY_ENTER) {
				Communicator.getInstance().getService().setElectionQuestion(
						electionId, questionTB.getText(), new UpdateCallback());
			}
		}
	}
	
	private class UpdateCallback extends BallotServerAsyncCallback<Void> {
		@Override
		public void onSuccess(Void arg0) {
			updater.update();
		}
	}
	
	private class CountVotesHandler implements ClickHandler {
		public void onClick(ClickEvent arg0) {
			Communicator.getInstance().getService().countVotes(electionId, new CountVotesCallback());
		}
	}
	
	private class CountVotesCallback extends BallotServerAsyncCallback<ElectionResultView> {
		@Override
		public void onSuccess(ElectionResultView countView) {
			new CountVotesPopup(countView).center();
		}
	}
	
	private class StartHandler implements ClickHandler {
		public void onClick(ClickEvent arg0) {
			Communicator.getInstance().getService().startElection(electionId, new UpdateCallback());
		}
	}
	
	private class CloseElectionHandler implements ClickHandler {
		public void onClick(ClickEvent arg0) {
			Communicator.getInstance().getService().closeElection(new UpdateCallback());
		}
	}
}
