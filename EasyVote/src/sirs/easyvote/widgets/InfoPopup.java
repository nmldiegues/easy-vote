package sirs.easyvote.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class InfoPopup {
	static public void popInfo(String title, String info) {
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText(title);
		dialogBox.setAnimationEnabled(true);
		
		VerticalPanel base = new VerticalPanel();
		base.add(new Label(info));
		Button closeButton = new Button("Close"); 
		base.add(closeButton);
		
		dialogBox.setWidget(base);
		
		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});
		
		dialogBox.center();
		closeButton.setFocus(true);
	}
}
