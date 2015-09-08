package ballotserver.client;

import ballotserver.widgets.InfoPopup;
import ballotserver.widgets.MainMenuWidget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class BallotServer implements EntryPoint {
	private MainMenuWidget menu;
	
	public void onModuleLoad() {
		menu = new MainMenuWidget();
		RootPanel.get().add(menu);
	}
	
	public static void popError(String errorMessage) {
		InfoPopup.popInfo("Error", errorMessage);
	}
}
