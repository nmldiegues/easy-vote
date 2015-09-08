package ballotserver.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ErrorPopup extends DialogBox{

	public ErrorPopup(Widget w){
		super(false, true);
		VerticalPanel response = new VerticalPanel();
		setText("Erro");
		setAnimationEnabled(true);
		setWidget(response);
		response.add(w);
		w.addStyleName("errorMessage");
		response.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		Button closeButton = new Button("Fechar");
		response.add(closeButton);
		closeButton.addStyleName("closeButton");
		
		closeButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				hide();
			}
		});
	}
}
