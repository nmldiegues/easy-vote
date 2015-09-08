package sirs.easyvote.widgets;

import sirs.easyvote.client.Communicator;
import sirs.easyvote.client.EasyVoteAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VoteConfirmationWidget extends DialogBox {

	private VerticalPanel mainPanel;
	private HorizontalPanel buttonPanel;
	private Button closeButton;
	private Button confirmationButton;
	private int chosenPosition;
	private EasyVoteAsyncCallback<Void> cb;
	
	public VoteConfirmationWidget(int chosenPosition, EasyVoteAsyncCallback<Void> cb) {
		
		super(false, true);
		setText("Confirmation Window");
		setAnimationEnabled(true);
		
		this.chosenPosition = chosenPosition;
		this.cb = cb;
		
		mainPanel = new VerticalPanel();
		buttonPanel = new HorizontalPanel();
		
		if(chosenPosition != -1) {
			mainPanel.add(new HTML("Are you sure you want to cast this vote?"));
		} else {
			mainPanel.add(new HTML("Are you sure you want to cast a blank vote?"));
		}
		
		confirmationButton = new Button("Yes");
		confirmationButton.addStyleName("BallotWidget-submitVoteButton");
		confirmationButton.addClickHandler(new ConfirmationHandler());
		buttonPanel.add(confirmationButton);
		
		closeButton = new Button("No");
		closeButton.addStyleName("BallotWidget-submitVoteButton");
		closeButton.addClickHandler(new CloseHandler());
		buttonPanel.add(closeButton);
		
		mainPanel.add(buttonPanel);
		
		setWidget(mainPanel);
	}

	
	private class ConfirmationHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			confirmationButton.setEnabled(false);
			Communicator.getInstance().submitVote(chosenPosition, cb);
			hide();
	
		}
	}
	
	private class CloseHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			closeButton.setEnabled(false);
			hide();
	
		}
	}
	
	
}
