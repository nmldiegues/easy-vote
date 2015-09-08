package sirs.easyvote.widgets;

import sirs.easyvote.client.Communicator;
import sirs.easyvote.client.EasyVote;
import sirs.easyvote.exception.IdFormatException;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

public class LoginWidget extends Grid {
	private HorizontalPanel whoAmIPanel;
	private FlowPanel inputPanel;
	private TextBox nameField;
	private TextBox passField;
	private Button sendButton;
	private Button logoutButton;
	private Label userIdLabel;
	private AsyncCallback<Void> loginCallback;
	private AsyncCallback<Void> logoutCallback;
	
	public LoginWidget(AsyncCallback<Void> logincb, AsyncCallback<Void> logoutcb) {
		this.resize(1, 2);
		this.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		this.addStyleName("easyvote-LoginWidget");
		LoginHandler handler = new LoginHandler();
		
		whoAmIPanel = new HorizontalPanel();
		whoAmIPanel.setVisible(false);
		userIdLabel = new Label();
		userIdLabel.addStyleName("LoginWidget-component");
		whoAmIPanel.add(userIdLabel);
		
		logoutButton = new Button("Logout");
		logoutButton.addStyleName("LoginWidget-logoutButton");
		logoutButton.addClickHandler(new LogoutHandler());
		whoAmIPanel.add(logoutButton);
		setWidget(0, 1, whoAmIPanel);
		
		inputPanel = new FlowPanel();
		loginCallback = logincb;
		logoutCallback = logoutcb;
		
		nameField = new TextBox();
		nameField.setText("VoterID");
		nameField.addStyleName("LoginWidget-component");
		nameField.addKeyUpHandler(handler);
		inputPanel.add(nameField);
		
		passField = new PasswordTextBox();
		passField.setText("Password");
		passField.addKeyUpHandler(handler);
		passField.addStyleName("LoginWidget-component");
		inputPanel.add(passField);
		
		sendButton = new Button("Login");
		sendButton.addClickHandler(handler);
		inputPanel.add(sendButton);
		
		this.setWidget(0, 0, inputPanel);
	}		
	
	public void promptLogin() {
		nameField.setText("VoterID");
		passField.setText("Password");
		sendButton.setEnabled(true);
		whoAmIPanel.setVisible(false);
		inputPanel.setVisible(true);
		nameField.setFocus(true);
		nameField.selectAll();
	}
	
	public void userIsAuthenticated() {
		userIdLabel.setText("You are: " + nameField.getText());
		inputPanel.setVisible(false);
		whoAmIPanel.setVisible(true);
	}
	
	private void login() {
		
		Long voterId = null;
	
		try {
			voterId = Long.parseLong(nameField.getText());
		} catch(NumberFormatException e) {
			EasyVote.setError(new HTML("The ID of the user must be a number."));
			return;
		}
		
		String pass = passField.getText();
	
		sendButton.setEnabled(false);
		Communicator.getInstance().login(voterId, pass, loginCallback);
	}
	
	private class LoginHandler implements ClickHandler, KeyUpHandler {
		public void onClick(ClickEvent event) {
			login();
		}
		
		public void onKeyUp(KeyUpEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				login();
			}
		}
	}
	
	private class LogoutHandler implements ClickHandler {
		public void onClick(ClickEvent event) {
			Communicator.getInstance().logout(logoutCallback);
		}
	}
}
