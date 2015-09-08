package sirs.easyvote.server;

import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import sirs.easyvote.client.EasyVoteService;
import sirs.easyvote.exception.EasyVoteException;
import sirs.easyvote.exception.RequiredLoginException;
import sirs.easyvote.shared.BallotSheetView;
import sirs.easyvote.shared.CandidateView;
import sirs.easyvote.shared.TokenView;
import sirs.easyvote.shared.GenericVerificationView;
import sirs.easyvote.shared.UserInfoView;
import sirs.easyvote.views.ViewVoter;
import sirs.framework.criptography.CriptoUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class EasyVoteServiceImpl extends RemoteServiceServlet implements
EasyVoteService {

	private static final long serialVersionUID = -4149912719644116056L;
	private EasyVoteApp app;
	
	public EasyVoteServiceImpl() {
		app = new EasyVoteApp();
	}
	
	protected void isLoggedIn(Long voterId) throws EasyVoteException {
		HttpSession session = this.getThreadLocalRequest().getSession();
		if (session.getAttribute("voter_user"+voterId) == null) {
			throw new RequiredLoginException();
		}
	}
	
	protected ViewVoter getViewVoter(Long voterId) {
		HttpSession session = this.getThreadLocalRequest().getSession();
		return (ViewVoter)session.getAttribute("voter_user"+voterId);
	}
	
	public void login(Long voterId, String password) throws EasyVoteException,IllegalArgumentException {
		if (getViewVoter(voterId) == null) {
			ViewVoter vv = new ViewVoter(voterId, password);
			HttpSession session = this.getThreadLocalRequest().getSession();
			session.setAttribute("voter_user"+voterId, vv);
		}
	}
	
	@Override
	public TokenView registerVoter(Long voterId) throws EasyVoteException,IllegalArgumentException {
		ViewVoter vv = getViewVoter(voterId); 
		vv.setAuditing(false);
		app.registrationForVotingPhase(vv);
		
		TokenView tokens = new TokenView();
		tokens.setTCtoken(vv.getTcToken().toString());
		tokens.setRtoken(vv.getRegToken().toString());
		return tokens;
	}

	@Override
	public BallotSheetView obtainBallotSheet(Long voterId) throws EasyVoteException,
			IllegalArgumentException {
		
		isLoggedIn(voterId);
		ViewVoter vv = getViewVoter(voterId);
		ballotserver.views.BallotSheetView responseBSV = app.votingPhaseGettingSheet(vv); 
		
		BallotSheetView returnBSV = new BallotSheetView();
		returnBSV.setElectionId(responseBSV.getElectionId());
		returnBSV.setQuestion(responseBSV.getQuestion());
		returnBSV.setSafeOffset(CriptoUtils.base64encode(responseBSV.getSafeOffset()));
		
		ArrayList<CandidateView> returnCandidateList = new ArrayList<CandidateView>(); 
		for (ballotserver.views.CandidateView cv : responseBSV.getCandidates()) {
			returnCandidateList.add(new CandidateView(cv.getName(), cv.getUniqueIdentifier()));
		}
		returnBSV.setCandidates(returnCandidateList);
		
		if(vv.getAuditing()){
			Integer position = 0;
			CandidateView firstName = returnBSV.getCandidates().get(0);
			for(int i = 1; i < returnBSV.getCandidates().size(); i++){
				CandidateView cv = returnBSV.getCandidates().get(i);
				if(firstName.getName().compareTo(cv.getName()) > 0){
					firstName = cv;
					position = i;
				}
			}
			//adjusting a 0 offset to full cycle offset
			if(position == 0){
				vv.setAuditingOffset(returnBSV.getCandidates().size());
			}
			else{
				 vv.setAuditingOffset(position);
			}
		}
		
		return returnBSV;
	}

	@Override
	public void submitVote(Long voterId, int chosenSquare) throws EasyVoteException {
		isLoggedIn(voterId);
		app.castVote(getViewVoter(voterId), chosenSquare);
	}
	
	@Override
	public GenericVerificationView verifyVote(Long voterId) throws EasyVoteException {
		isLoggedIn(voterId);
		return app.verifyVote(getViewVoter(voterId));
	}

	@Override
	public void logout() throws EasyVoteException {
		//do nothing so user can relog and still have the same vv
		//isLoggedIn();
		//HttpSession session = this.getThreadLocalRequest().getSession();
		//session.removeAttribute("voter_user");
	}

	@Override
	public TokenView requireThirdPartyAuditing(Long auditerId) throws EasyVoteException {
		ViewVoter vv = getViewVoter(auditerId);
		vv.setAuditing(true);
		app.registrationForVotingPhase(vv);
		
		TokenView tokens = new TokenView();
		tokens.setTCtoken(vv.getTcToken().toString());
		tokens.setRtoken(vv.getRegToken().toString());
		return tokens;
	}

	@Override
	public GenericVerificationView verifyOffset(Long voterId) throws EasyVoteException {
		isLoggedIn(voterId);
		return app.verifyOffset(getViewVoter(voterId));
	}

	@Override
	public UserInfoView getUserInfo(Long voterID) throws EasyVoteException {
		ViewVoter vv = getViewVoter(voterID);
		
		UserInfoView uv = new UserInfoView();
		uv.setHasVoted(vv.getChosenSquare() == null ? false : true);
		uv.setIsAuditing(vv.getAuditing() == null ? false : vv.getAuditing());
		return uv;
	}
}
