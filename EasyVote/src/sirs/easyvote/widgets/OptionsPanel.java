package sirs.easyvote.widgets;

import sirs.easyvote.client.Communicator;
import sirs.easyvote.client.EasyVoteAsyncCallback;
import sirs.easyvote.shared.UserInfoView;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

public class OptionsPanel extends Grid {
	private Button voteButton;
	private Button verifyVoteButton;
	private Button auditButton;
	private Button verifyOffsetButton;
	private Label voteLabel;
	private Label verifyVoteLabel;
	private Label auditLabel;
	private Label verifyOffsetLabel;

	public OptionsPanel() {
		this.resize(4, 2);
		this.setVisible(false);
		this.addStyleName("optionsPanel");
		
		voteButton = new Button("Vote");
		this.setWidget(0, 0, voteButton);
		voteLabel = new Label("Submit your vote in the current active election.");
		this.setWidget(0, 1, voteLabel);
		
		verifyVoteButton = new Button("Verifiy Vote");
		this.setWidget(1, 0, verifyVoteButton);
		verifyVoteLabel = new Label("Verify that your vote was cast correctly.");
		this.setWidget(1, 1, verifyVoteLabel);
		
		auditButton = new Button("Audit");
		this.setWidget(2, 0, auditButton);
		auditLabel = new Label("Submit a vote in audit mode.");
		this.setWidget(2, 1, auditLabel);
		
		verifyOffsetButton = new Button("Verify Offset");
		this.setWidget(3, 0, verifyOffsetButton);
		verifyOffsetLabel = new Label("Verify the offset of your vote is correct.");
		this.setWidget(3, 1, verifyOffsetLabel);
	}
	
	public void setHandlers(ClickHandler votecb, ClickHandler vervotecb,
			ClickHandler auditcb, ClickHandler veroffsetcb) {
		
		voteButton.addClickHandler(votecb);
		verifyVoteButton.addClickHandler(vervotecb);
		auditButton.addClickHandler(auditcb);
		verifyOffsetButton.addClickHandler(veroffsetcb);
	}
	
	public void showOptions() {
		Communicator.getInstance().getUserInfo(new UserInfoCallback());
	}
	
	private void toggleButtonsVisibility(UserInfoView user) {
		if (!user.getHasVoted()) {
			voteButton.setVisible(true);
			voteLabel.setVisible(true);
			auditButton.setVisible(true);
			auditLabel.setVisible(true);
		} else {
			voteButton.setVisible(false);
			voteLabel.setVisible(false);
			auditButton.setVisible(false);
			auditLabel.setVisible(false);
		}
		
		if (user.getHasVoted()) {
			verifyVoteButton.setVisible(true);
			verifyVoteLabel.setVisible(true);
		} else {
			verifyVoteButton.setVisible(false);
			verifyVoteLabel.setVisible(false);
		}
		
		if (user.getHasVoted() && user.getIsAuditing()) {
			verifyOffsetButton.setVisible(true);
			verifyOffsetLabel.setVisible(true);
		} else {
			verifyOffsetButton.setVisible(false);
			verifyOffsetLabel.setVisible(false);
		}
		
		this.setVisible(true);
	}
	
	private class UserInfoCallback extends EasyVoteAsyncCallback<UserInfoView> {
		public void onSuccess(UserInfoView user) {
			toggleButtonsVisibility(user);
		}
	};
}
