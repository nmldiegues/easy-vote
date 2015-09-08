package ballotserver.widgets;

import java.util.ArrayList;
import java.util.List;

import ballotserver.client.BallotServerAsyncCallback;
import ballotserver.client.Communicator;
import ballotserver.views.ElectionView;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ElectionsPanel extends VerticalPanel {
	private static final String NO_ELECTIONS = "No elections available.";
	private static final String ELECTIONS_AVAILABLE = "The following elections are available:";
	
	private HorizontalPanel infoPanel;
	private Image throbberImage;
	private Label infoLabel;
	private List<ElectionWidget> electionWidgets = null;
	private Label captionLabel;
	
	public ElectionsPanel() {
		infoPanel = new HorizontalPanel();
		infoPanel.addStyleName("electionsPanel-infoPanel");
		this.add(infoPanel);
		
		throbberImage = new Image("throbber1.gif");
		infoPanel.add(throbberImage);
		
		infoLabel = new Label();
		infoPanel.add(infoLabel);
		
		electionWidgets = new ArrayList<ElectionWidget>();
		
		captionLabel = new Label(NO_ELECTIONS);
		captionLabel.getElement().setAttribute("style", "font-weight:bold;");
		this.add(captionLabel);
		this.setWidth("100%");
		update();
	}

	public void update() {
		Window.scrollTo(0, 0);
		infoLabel.setText("Updating list of elections...");
		infoPanel.setVisible(true);
		Communicator.getInstance().getService().getElections(new GetElectionsCallback());
	}
	
	private class GetElectionsCallback extends BallotServerAsyncCallback<List<ElectionView>> {
		@Override
		public void onSuccess(List<ElectionView> elections) {
			refresh(elections);
			infoPanel.setVisible(false);
		}
		
		@Override
		public void onFailure(Throwable caught) {
			infoPanel.setVisible(false);
			super.onFailure(caught);
		}
	}
	
	private void refresh(List<ElectionView> elections) {
		for (ElectionWidget ew : electionWidgets) {
			this.remove(ew);
		}
		electionWidgets.clear();
		
		if (elections == null || elections.size() == 0) {
			captionLabel.setText(NO_ELECTIONS);
		} else {
			captionLabel.setText(ELECTIONS_AVAILABLE);
			for (ElectionView ev : elections) {
				ElectionWidget newElection = new ElectionWidget(ev, new UpdateCallback(this));
				electionWidgets.add(newElection);
				this.add(newElection);
			}
		}
	}
	
	public class UpdateCallback {
		private ElectionsPanel parent;
		
		public UpdateCallback(ElectionsPanel parent) {
			this.parent = parent;
		}
		
		public void update() {
			parent.update();
		}
	}
}
