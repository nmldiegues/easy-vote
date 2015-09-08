package ballotserver.server;

public class BallotServerRoot {

	private static BallotServerApp app = null;
	
	public static BallotServerApp getBallotServerApp(){
		if(app == null){
			app = new BallotServerApp();
		}
		return app;
	}
	
}
