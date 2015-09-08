package sirs.easyvote.widgets;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class InfoPanelWidget extends HorizontalPanel {
	private Image throbber;
	private Label information;
	
	public InfoPanelWidget() {
		throbber = new Image("throbber1.gif");
		throbber.setVisible(false);
		this.add(throbber);
		
		information = new Label();
		this.add(information);
		
		this.addStyleName("easyvote-infoPanel");
	}
	
	public void setInformation(String info, boolean showThrobber) {
		information.setText(info);
		throbber.setVisible(showThrobber);
	}
	
	public void setInformation(String info) {
		setInformation(info, false);
	}
}
