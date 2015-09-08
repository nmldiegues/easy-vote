package sirs.easyvote.client;

import sirs.easyvote.shared.BallotSheetView;
import sirs.easyvote.shared.GenericVerificationView;
import sirs.easyvote.shared.TokenView;
import sirs.easyvote.shared.UserInfoView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Communicator {
	private static Communicator instance = null;
	private EasyVoteServiceAsync service = null;
	private Long userId;
	
	private Communicator() {
		service = GWT.create(EasyVoteService.class);
	}
	
	public static Communicator getInstance() {
		if (instance == null) {
			instance = new Communicator();
		}
		return instance;
	}

	public void login(final Long voterId, String pass, final AsyncCallback<Void> callback) {
		service.login(voterId, pass,
				new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
		
					@Override
					public void onSuccess(Void result) {
						userId = voterId;
						callback.onSuccess(result);
					}
				}
		);
	}
	
	public void logout(AsyncCallback<Void> callback) {
		service.logout(callback);
	}
	
	public void obtainBallotSheet(AsyncCallback<BallotSheetView> callback) {
		service.obtainBallotSheet(userId, callback);
	}

	public void registerVoter(AsyncCallback<TokenView> callback) throws IllegalArgumentException {
		service.registerVoter(userId, callback);
	}

	public void requireThirdPartyAuditing(AsyncCallback<TokenView> callback) {
		service.requireThirdPartyAuditing(userId, callback);
	}

	public void submitVote(int chosenSquare, AsyncCallback<Void> callback) {
		service.submitVote(userId, chosenSquare, callback);
	}

	public void verifyVote(AsyncCallback<GenericVerificationView> callback) {
		service.verifyVote(userId, callback);
	}
	
	public void verifyOffset(AsyncCallback<GenericVerificationView> callback) {
		service.verifyOffset(userId, callback);
	}

	public void getUserInfo(AsyncCallback<UserInfoView> callback) {
		service.getUserInfo(userId, callback);
	}
}
