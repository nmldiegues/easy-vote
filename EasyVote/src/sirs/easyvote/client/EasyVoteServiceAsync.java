package sirs.easyvote.client;

import sirs.easyvote.shared.BallotSheetView;
import sirs.easyvote.shared.GenericVerificationView;
import sirs.easyvote.shared.TokenView;
import sirs.easyvote.shared.UserInfoView;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>EasyVoteService</code>.
 */
public interface EasyVoteServiceAsync {
	
	void registerVoter(Long voterID, AsyncCallback<TokenView> callback)
			throws IllegalArgumentException;
	
	void obtainBallotSheet(Long voterID, AsyncCallback<BallotSheetView> callback);
	
	void login(Long voterID, String password, AsyncCallback<Void> callback);
	
	void logout(AsyncCallback<Void> callback);
	
	void requireThirdPartyAuditing(Long auditerId, AsyncCallback<TokenView> callback);
	
	void submitVote(Long voterID, int chosenSquare, AsyncCallback<Void> callback);
	
	void verifyVote(Long voterID, AsyncCallback<GenericVerificationView> callback);
	
	void verifyOffset(Long voterID, AsyncCallback<GenericVerificationView> callback);

	void getUserInfo(Long userId, AsyncCallback<UserInfoView> callback);
}