package ballotserver.client;

import com.google.gwt.core.client.GWT;

public class Communicator {
	private static Communicator instance = null;
	private BallotServerServiceAsync service = null;
	
	private Communicator() {
		service = GWT.create(BallotServerService.class);
	}
	
	public static Communicator getInstance() {
		if (instance == null) {
			instance = new Communicator();
		}
		return instance;
	}
	
	public BallotServerServiceAsync getService() {
		return service;
	}
}
