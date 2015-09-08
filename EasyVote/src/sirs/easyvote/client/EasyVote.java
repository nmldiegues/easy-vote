package sirs.easyvote.client;

import sirs.easyvote.exception.ElectionHasBeenCorruptedException;
import sirs.easyvote.shared.BallotSheetView;
import sirs.easyvote.shared.GenericVerificationView;
import sirs.easyvote.shared.TokenView;
import sirs.easyvote.widgets.BallotWidget;
import sirs.easyvote.widgets.InfoPanelWidget;
import sirs.easyvote.widgets.LoginWidget;
import sirs.easyvote.widgets.OptionsPanel;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class EasyVote implements EntryPoint {
	private FlowPanel basePanel;
	private OptionsPanel optionsPanel;
	private InfoPanelWidget infoPanel;
	private VerticalPanel tokensPanel;
	private LoginWidget loginWidget;
	private BallotWidget displayedBallot;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		HTMLPanel logoContainer = new HTMLPanel("<img src=\"easyvote.jpg\" />");
		logoContainer.getElement().setAttribute("id", "logoContainer");
		RootPanel.get().add(logoContainer); // Use RootPanel.get() to get the entire body element
		
		basePanel = new FlowPanel();
		basePanel.getElement().setAttribute("id", "mainPageColumn");
		RootPanel.get().add(basePanel);
		
		loginWidget = new LoginWidget(new LoginCallback(), new LogoutCallback());
		basePanel.add(loginWidget);
		
		infoPanel = new InfoPanelWidget();
		infoPanel.setInformation("Please use your credentials to log in.");
		basePanel.add(infoPanel);
		
		tokensPanel = new VerticalPanel();
		tokensPanel.setVisible(false);
		tokensPanel.setStyleName("tokensPanel");
		basePanel.add(tokensPanel);
		
		optionsPanel = new OptionsPanel();
		optionsPanel.setHandlers(new VoteOptionHandler(), new VerifyVoteOptionHandler(),
				new AuditOptionHandler(), new VerifyOffsetOptionHandler());
		basePanel.add(optionsPanel);
		
		loginWidget.promptLogin();
	}
	
	public static void setError(Widget w) {
		ErrorPopup errorPopup = new ErrorPopup(w);
		errorPopup.center();
	}
	
	// --- handlers -----------------------------------------------------------
	private class VoteOptionHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			optionsPanel.setVisible(false);
			infoPanel.setInformation("Registering...", true);
			Communicator.getInstance().registerVoter(new RegisterVoterCallback());
		}
	}
	
	private class VerifyVoteOptionHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			optionsPanel.setVisible(false);
			infoPanel.setInformation("Verifying your vote...", true);
			Communicator.getInstance().verifyVote(new VerifyVoteCallback());
		}
	}
	
	private class AuditOptionHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			optionsPanel.setVisible(false);
			infoPanel.setInformation("Registering (audit mode)...", true);
			Communicator.getInstance().requireThirdPartyAuditing(new RegisterVoterCallback());
		}
	}
	
	private class VerifyOffsetOptionHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			optionsPanel.setVisible(false);
			infoPanel.setInformation("Verifying offset...", true);
			Communicator.getInstance().verifyOffset(new VerifyOffsetCallback());
		}
	}
	
	// --- callbacks ----------------------------------------------------------
	private class LoginCallback extends EasyVoteAsyncCallback<Void> {
		public void onSuccess(Void result) {
			loginWidget.userIsAuthenticated();
			infoPanel.setInformation("Please choose what you want to do.");
			optionsPanel.showOptions();
		}
	};
	
	private class LogoutCallback extends EasyVoteAsyncCallback<Void> {
		public void onSuccess(Void result) {
			tokensPanel.setVisible(false);
			optionsPanel.setVisible(false);
			if (displayedBallot != null) {
				basePanel.remove(displayedBallot);
			}
			infoPanel.setInformation("Please use your credentials to log in.");
			loginWidget.promptLogin();
		}
	};
	
	private class RegisterVoterCallback extends EasyVoteAsyncCallback<TokenView> {
		public void onSuccess(TokenView vv) {
			tokensPanel.clear();
			tokensPanel.add(new Label("Your tokens for this election are:"));
			tokensPanel.add(new Label("TC Token: " + vv.getTCtoken()));
			tokensPanel.add(new Label("R Token: " + vv.getRtoken()));
			tokensPanel.add(new Label("You can use these tokens later to verifiy your vote."));
			tokensPanel.setVisible(true);
			
			infoPanel.setInformation("Obtaining ballot...", true);
			Communicator.getInstance().obtainBallotSheet(new ObtainBallotSheetCallback());
		}
	};
	
	private class ObtainBallotSheetCallback extends EasyVoteAsyncCallback<BallotSheetView> {
		public void onSuccess(BallotSheetView bsv) {
			infoPanel.setInformation("Please select one candidate. Press the Submit Vote button to cast your vote.");
			displayedBallot = new BallotWidget(bsv, new SubmitVoteCallback()); 
			basePanel.add(displayedBallot);
		}
	};
	
	private class SubmitVoteCallback extends EasyVoteAsyncCallback<Void> {
		public void onSuccess(Void result) {
			infoPanel.setInformation("Your vote was submitted successfully!");
			if (displayedBallot != null) {
				basePanel.remove(displayedBallot);
			}
			tokensPanel.clear();
			tokensPanel.setVisible(false);
			optionsPanel.showOptions();
		}
	};
	
	private class VerifyVoteCallback extends EasyVoteAsyncCallback<GenericVerificationView> {
		public void onSuccess(GenericVerificationView result) {
			infoPanel.setInformation("Your vote was cast correctly! The saved chosen square "
					+ result.getChosenValue() + " matches the received value " + result.getReceivedValue() + ".");
			optionsPanel.showOptions();
		}
		
		public void onFailure(Throwable caught) {
			if (caught instanceof ElectionHasBeenCorruptedException) {
				infoPanel.setInformation("The chosen square in the central database does not match the one you chose!");
			} else {
				super.onFailure(caught);
			}
			optionsPanel.showOptions();
		}
	};
	
	private class VerifyOffsetCallback extends EasyVoteAsyncCallback<GenericVerificationView> {
		public void onSuccess(GenericVerificationView result) {
			infoPanel.setInformation("The offset computed locally " + result.getChosenValue() +
					" matches the translated value " + result.getReceivedValue() + ".");
			optionsPanel.showOptions();
		}
		
		public void onFailure(Throwable caught) {
			if (caught instanceof ElectionHasBeenCorruptedException) {
				infoPanel.setInformation("The translated offset does not match the local computation!");
			} else {
				super.onFailure(caught);
			}
			optionsPanel.showOptions();
		}
	};
}