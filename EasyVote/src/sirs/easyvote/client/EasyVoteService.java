package sirs.easyvote.client;

import sirs.easyvote.exception.EasyVoteException;
import sirs.easyvote.shared.BallotSheetView;
import sirs.easyvote.shared.GenericVerificationView;
import sirs.easyvote.shared.TokenView;
import sirs.easyvote.shared.UserInfoView;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("service")
public interface EasyVoteService extends RemoteService {
	
	TokenView registerVoter(Long voterID) throws EasyVoteException,IllegalArgumentException;
	
	BallotSheetView obtainBallotSheet(Long voterID) throws EasyVoteException,IllegalArgumentException;
	
	void login(Long voterID, String password) throws EasyVoteException;
	
	void logout() throws EasyVoteException;
	
	TokenView requireThirdPartyAuditing(Long auditerId) throws EasyVoteException;
	
	void submitVote(Long voterID, int chosenSquare) throws EasyVoteException;
	
	GenericVerificationView verifyVote(Long voterID) throws EasyVoteException;
	
	GenericVerificationView verifyOffset(Long voterID) throws EasyVoteException;
	
	UserInfoView getUserInfo(Long voterID) throws EasyVoteException;
}
