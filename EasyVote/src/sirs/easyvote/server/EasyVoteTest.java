package sirs.easyvote.server;

import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

import ballotserver.views.BallotSheetView;
import registration.domain.EligibleVoter;
import registration.exceptions.RegistrationException;
import registration.persistence.RegistrationPersistence;
import sirs.easyvote.shared.CandidateView;
import sirs.easyvote.views.ViewVoter;
import sirs.framework.config.Config;

public class EasyVoteTest {
	
	public static void main(String[] args) {
		
		ArrayList<ViewVoter> voterViews;
		ArrayList<BallotSheetView> ballotViews;
		ArrayList<EasyVoteThread> threads;
		EasyVoteApp app = null;
		ViewVoter auxView = null;
		
		System.out.println("begin testing");
		System.out.print("Getting Voters from config file....");
		int numVoters = Integer.parseInt(Config.getInstance().getInitParameter("numVoters"));
		
		voterViews = new ArrayList<ViewVoter>();
		
		Properties voters = Config.getInstance().getInitProperties();
		for(Object propertyKey : voters.keySet()){
			String possibleVoter = (String)propertyKey;
			String password = null;
			Long voterId = null;
			if(possibleVoter.contains("voter")){
				voterId = new Long(possibleVoter.substring("voter".length()));
				password = voters.getProperty(possibleVoter);
				auxView = new ViewVoter(voterId, password);
				voterViews.add(auxView);
			}
		}
		
		System.out.println("done.");			
		System.out.print("Creating EasyVoteApplication....");
		
		app = new EasyVoteApp();
		System.out.println("done.");
		
		System.out.print("Creating threads....");
		int numThreads = Integer.parseInt(Config.getInstance().getInitParameter("numThreads"));
		
		threads = new ArrayList<EasyVoteThread>();
		
		for(int i = 0; i < numThreads; i++) {
			if(i%2 == 0){
				threads.add(new VotingThread(i, splitWork(voterViews, i, numVoters/numThreads), app));
			}
			else{
				threads.add(new AuditingThread(i, splitWork(voterViews, i, numVoters/numThreads), app));
			}
		}
		System.out.println("done.");
		System.out.println("Starting threads");
		
		for(EasyVoteThread thread : threads ) {
			thread.start();
		}
		
		System.out.println("Joining threads");
		
		for(EasyVoteThread thread : threads ) {
			try {
				thread.join();
			}catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("testing done");
	}
	
	private static ArrayList<ViewVoter> splitWork(ArrayList<ViewVoter> views, int threadID, int chunk) {
		
		ArrayList<ViewVoter> returnArray = new ArrayList<ViewVoter>();
		
		for(int i = threadID * chunk; i < (threadID * chunk) + chunk; i++) {
			returnArray.add(views.get(i));
		}
		
		return returnArray;
		
	}
	
	private static abstract class EasyVoteThread extends Thread {
		private int threadID;
		private ArrayList<ViewVoter> voterViews;
		private EasyVoteApp app;
		
		public EasyVoteThread(int threadID, ArrayList<ViewVoter> voterViews, EasyVoteApp app) {
			this.threadID = threadID;
			this.voterViews = voterViews;
			this.app = app;
		}
		
		public abstract void run();
		
	}
	
	private static class VotingThread extends EasyVoteThread {
	
		public VotingThread(int threadID, ArrayList<ViewVoter> voterViews, EasyVoteApp app) {
			super(threadID, voterViews, app);
		}
		
		public void run() {
			try{
				ArrayList<BallotSheetView> ballotViews = new ArrayList<BallotSheetView>();
				
				//Registration
				System.out.println("Registering the voters -> VotingThread: " + super.threadID);
				for(ViewVoter vv : super.voterViews) {
					vv.setAuditing(false);
					super.app.registrationForVotingPhase(vv);
				}
				
				//Get Ballot Sheets
				System.out.println("Getting ballot sheets -> VotingThread: " + super.threadID);
				ballotViews = new ArrayList<BallotSheetView>();
				
				for(ViewVoter vv : super.voterViews) {
					ballotViews.add(super.app.votingPhaseGettingSheet(vv));
				}
				
				int nCandidates = ballotViews.get(0).getCandidates().size();
				
				//Casting Votes
				System.out.println("Casting Votes -> VotingThread: " + super.threadID);
				for(ViewVoter vv : super.voterViews) {
					super.app.castVote(vv, vv.getVoterId().intValue() % nCandidates);
				}
				
				//Individual Verification
				System.out.println("Verifying Individual Vote -> VotingThread: " + super.threadID);
				for(ViewVoter vv: super.voterViews){
					super.app.verifyVote(vv);
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private static class AuditingThread extends EasyVoteThread {
		
		public AuditingThread(int threadID, ArrayList<ViewVoter> voterViews, EasyVoteApp app) {
			super(threadID, voterViews, app);
		}
		
		public void run() {
			try{
				ArrayList<BallotSheetView> ballotViews = new ArrayList<BallotSheetView>();
				
				//Registration
				System.out.println("Registering the voters -> AuditingThread: " + super.threadID);
				for(ViewVoter vv : super.voterViews) {
					vv.setAuditing(true);
					super.app.registrationForVotingPhase(vv);
				}
				
				//Get Ballot Sheets
				System.out.println("Getting ballot sheets -> AuditingThread: " + super.threadID);
				ballotViews = new ArrayList<BallotSheetView>();
				
				for(ViewVoter vv : super.voterViews) {
					BallotSheetView returnBSV = super.app.votingPhaseGettingSheet(vv);
					ballotViews.add(returnBSV);
					Integer position = 0;
					ballotserver.views.CandidateView firstName = returnBSV.getCandidates().get(0);
					for(int i = 1; i < returnBSV.getCandidates().size(); i++){
						ballotserver.views.CandidateView cv = returnBSV.getCandidates().get(i);
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
				
				int nCandidates = ballotViews.get(0).getCandidates().size();
				
				//Casting Votes
				System.out.println("Casting Votes -> AuditingThread: " + super.threadID);
				for(ViewVoter vv : super.voterViews) {
					super.app.castVote(vv, vv.getVoterId().intValue() % nCandidates);
				}
				
				//Individual Verification
				System.out.println("Verifying Individual Vote -> AuditingThread: " + super.threadID);
				for(ViewVoter vv: super.voterViews){
					super.app.verifyVote(vv);
				}
				
				//Third Party Verification
				System.out.println("Third Party Auditing Vote -> AuditingThread: " + super.threadID);
				for(ViewVoter vv: super.voterViews){
					super.app.verifyOffset(vv);
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
