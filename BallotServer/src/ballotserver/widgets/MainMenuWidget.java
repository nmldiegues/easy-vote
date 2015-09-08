package ballotserver.widgets;

import ballotserver.client.BallotServerAsyncCallback;
import ballotserver.client.Communicator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MainMenuWidget extends VerticalPanel {
	private ElectionsPanel electionsPanel;
	private HorizontalPanel newElectionPanel;
	private Button newElectionButton;
	
	public MainMenuWidget() {
		electionsPanel = new ElectionsPanel();
		this.add(electionsPanel);
		
		newElectionPanel = new HorizontalPanel();
		newElectionPanel.addStyleName("toolbox");
		
		newElectionButton = new Button("Create New Election");
		newElectionButton.addStyleName("tool");
		newElectionButton.addClickHandler(new NewElectionHandler());		
		newElectionPanel.add(newElectionButton);
		
		this.add(newElectionPanel);
		this.getElement().setAttribute("id", "mainPageColumn");
	}
	
	private class NewElectionHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent arg0) {
			//TODO insert election question
			Communicator.getInstance().getService().createNewElection("Question", new NewElectionCallback());
		}
	}
	
	private class NewElectionCallback extends BallotServerAsyncCallback<Long> {
		@Override
		public void onSuccess(Long newElectionId) {
			electionsPanel.update();		
		}
	}
}
