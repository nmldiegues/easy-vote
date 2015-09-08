package ballotserver.server;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.crypto.SecretKey;

import registration.exceptions.RegistrationException;
import registration.ws.client.service.TotalVotersService;
import sirs.framework.config.Config;
import sirs.framework.criptography.CriptoUtils;
import trustedcenter.exceptions.TrustedCenterException;
import trustedcenter.ws.client.service.GenerateCertificateService;
import trustedcenter.ws.client.service.GetPublicKeyService;
import trustedcenter.ws.client.service.ValidateCertificateService;
import ballotserver.domain.BallotSheet;
import ballotserver.domain.Candidate;
import ballotserver.domain.Election;
import ballotserver.exceptions.BallotServerException;
import ballotserver.exceptions.CandidateAlreadyExistsException;
import ballotserver.exceptions.CandidateDoesNotExistException;
import ballotserver.exceptions.ElectionAlreadyClosedException;
import ballotserver.exceptions.ElectionAlreadyExistsException;
import ballotserver.exceptions.ElectionAlreadyStartedException;
import ballotserver.exceptions.ElectionAsBeenCorruptedException;
import ballotserver.exceptions.ErrorRetreivingTCKeyException;
import ballotserver.exceptions.MultipleActiveElectionsAttemptException;
import ballotserver.exceptions.NoActiveElectionException;
import ballotserver.exceptions.NoSuchTokenPairException;
import ballotserver.exceptions.VoterAlreadySubmitedVoteException;
import ballotserver.persistence.BallotServerPersistence;
import ballotserver.views.BallotSheetView;
import ballotserver.views.CandidateView;
import ballotserver.views.CandidateVotesView;
import ballotserver.views.ElectionResultView;
import ballotserver.views.ElectionView;
import ballotserver.ws.client.service.SafeOffsetEncryptionService;

public class BallotServerApp {

	private static int DID_NOT_VOTE = -2; 
	
	private BigInteger serialNumber;
	private KeyPair bsKeys;
	private SecretKey bsSymKey;
	private PublicKey tcPubKey;
	private PublicKey regPubKey;
	
	public BallotServerApp(){
		//Note that acquiring TC key and generating the certificate, should be done in a secure
		//fashion, most likely within physical presence to prove our identity for issuing of a certificate

		//generate keypair and request a certificate for it from the TrustedCenter
		this.bsKeys = CriptoUtils.generateJavaKeys(1024);
		try{
			this.tcPubKey = new GetPublicKeyService(Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress")).execute();
		}
		catch(TrustedCenterException e){
			throw new ErrorRetreivingTCKeyException(e);
		}
		
		//generating symmetric key for safeOffset ciphering in ballot sheet generation
		this.bsSymKey = CriptoUtils.generateJavaSymKey(128);
		
		//requesting a certificate creation now
		serialNumber = new GenerateCertificateService(Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
				getMyName(), bsKeys.getPublic()).execute();
		
		//getting registration public key
		regPubKey = new ValidateCertificateService(Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
				tcPubKey, bsKeys.getPrivate(), serialNumber, new BigInteger("-1"), "Registration").execute();
	}
	
	public int getNumberBallots(){
		return Integer.parseInt(Config.getInstance().getInitParameter("number.ballotservers"));
	}
	
	public int getMyBallotNumber(){
		return Integer.parseInt(Config.getInstance().getInitParameter("my.ballotserver.id"));
	}
	
	public String getMyName(){
		return Config.getInstance().getInitParameter("name.ballotserver" + getMyBallotNumber());
	}
	
	public PublicKey getRegPubKey(){
		return this.regPubKey;
	}
	
	public BigInteger getBSSerialNumber(){
		return this.serialNumber;
	}
	
	public SecretKey getBSSecretKey(){
		return this.bsSymKey;
	}
	
	public List<String> getAllBallotServersNames(){
		List<String> result = new ArrayList<String>();
		int numberServers = getNumberBallots();
		for(int iter = 1; iter <= numberServers; iter++){
			result.add(Config.getInstance().getInitParameter("name.ballotserver" + iter));
		}
		
		return result;
	}
	
	public String getBallotEndpoint(String ballotName){
		return Config.getInstance().getInitParameter(ballotName.toLowerCase() + ".ws.EndpointAddress");
	}
	
	public SecretKey getSharedKey(byte[] regToken, byte[] tcToken){
		return BallotServerPersistence.getBallotSheet(regToken, tcToken).getObjectSharedKey();
	}
	
	public void registerSharedKey(byte[] regToken, byte[] tcToken, SecretKey key){
		BallotSheet vote;
		try{
			vote = BallotServerPersistence.getBallotSheet(regToken, tcToken);
		}
		catch(NoSuchTokenPairException e){
			//typical scenario, voter hadn't started interaction with Ballot Server
			vote = new BallotSheet();
			vote.setRegToken(regToken);
			vote.setTcToken(tcToken);
			vote.setObjectSharedKey(key);
			vote.setChosenSquare(DID_NOT_VOTE);
			BallotServerPersistence.addBallotSheet(vote);
			return;
		}
		//specific scenario, voter re-requested a shared key
		//verifying that user hasn't requested for the ballot sheet
		if(vote.getSafeOffset() == null){
			vote.setObjectSharedKey(key);
			//auditing votes won't be counted. if this vote is a normal
			//one, which is the normal scenario, then this will be changed
			//when he actually casts the vote
			vote.setAuditing(true);
			BallotServerPersistence.updateBallotSheet(vote);
		}
		else{
			throw new BallotServerException("Already requested a ballot sheet, can't change the registered shared key");
		}
	}
	
	public PublicKey getTcPubKey(){
		return this.tcPubKey;
	}
	
	public KeyPair getBsKeys(){
		return this.bsKeys;
	}
	
	public BallotSheetView generateBallotSheet(byte[] regToken, byte[] tcToken){
		List<String> candidateNames = new ArrayList<String>();
		List<Long> candidateIds = new ArrayList<Long>();
		byte[] safeOffset = null;
		Election curElection = BallotServerPersistence.getCurrentElection();
		
		//if the election isn't active can't generate the ballot
		if(curElection.equals(null) || !curElection.getStarted()) {
			throw new NoActiveElectionException();
		}
		
		//generate the candidate order, denoted by the safeOffset
		byte[] masterToken = new byte[regToken.length];
		Random randomGenerator = new Random();
		int k = randomGenerator.nextInt(regToken.length);
		int iter;
		for(iter = 0; iter < k; iter++){
			masterToken[iter] = regToken[iter];
		}
		for(iter = k; iter < regToken.length; iter++){
			masterToken[iter] = tcToken[iter];
		}
		
		
		BigInteger masterTokenNumber = new BigInteger(masterToken);
		BigInteger offset = BigInteger.valueOf(randomGenerator.nextInt(curElection.getCandidates().size())+1);
		BigInteger blindedOffset = masterTokenNumber.multiply(offset);
		safeOffset = blindedOffset.toByteArray();
		
		//Get all ballot servers' public keys and cipher the blindedOffset with it iteratively
		
		safeOffset = CriptoUtils.cipherWithSymKey(safeOffset, bsSymKey);
		String myName = getMyName();
		for(String curName : getAllBallotServersNames()){
			if(!curName.equals(myName)){
				PublicKey remotePubKey = new ValidateCertificateService(
						Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
						this.tcPubKey, this.bsKeys.getPrivate(), this.serialNumber,
						new BigInteger("-1"), curName).execute();
				
				safeOffset = new SafeOffsetEncryptionService(getBallotEndpoint(curName), remotePubKey,
						bsKeys.getPrivate(), serialNumber, safeOffset, "cipher").execute();
			}
		}
		
		//prepare the lists of candidate names and ids according to offset
		for (Candidate c : curElection.getCandidateList()) {
			candidateNames.add(c.getPersonName());
			candidateIds.add(c.getId());
		}
		Collections.rotate(candidateNames, offset.intValue());
		Collections.rotate(candidateIds, offset.intValue());
		
		//store k and safeOffset value persistently, and my Ballot Server Id
		BallotSheet domainBS = BallotServerPersistence.getBallotSheet(regToken, tcToken);
		boolean newBallot = true;
		if(domainBS.getSafeOffset() != null){
			newBallot = false;
		}
		domainBS.setBallotServerGenerator(getMyName());
		domainBS.setK(k);
		domainBS.setSafeOffset(safeOffset);
		try{
		BallotServerPersistence.updateBallotSheet(domainBS);
		}
		catch(org.hibernate.StaleObjectStateException e){
			System.out.println("STALE OBJECT EXCEPTION IN UPDATE BS!!!!!!!!!!!!!!");
		}
		if(newBallot){
			try{
			BallotServerPersistence.addBallotSheetToCurrentElection(domainBS);
			}
			catch(org.hibernate.StaleObjectStateException e){
				System.out.println("STALE OBJECT EXCEPTION IN ADD BS TO ELECTION!!!!!!!!!!!!!!");
			}
		}
				
		List<CandidateView> candidatesView = new ArrayList<CandidateView>();
		for(int curIter = 0; curIter < candidateNames.size(); curIter++){
			candidatesView.add(new CandidateView(candidateNames.get(curIter), candidateIds.get(curIter)));
		}
		BallotSheetView bs = new BallotSheetView(candidatesView, safeOffset, curElection.getId(), curElection.getQuestion());
		return bs;
	}
	
	public void registerVote(byte[] regToken, byte[] tcToken, Integer chosenSquare, boolean auditing){
		BallotSheet bs = BallotServerPersistence.getBallotSheet(regToken, tcToken);
		if(bs.getChosenSquare() != DID_NOT_VOTE){
			throw new VoterAlreadySubmitedVoteException();
		}
		bs.setChosenSquare(chosenSquare);
		bs.setAuditing(auditing);
		BallotServerPersistence.updateBallotSheet(bs);
	}
	
	public Long createElection(String question) {
		try{
			BallotServerPersistence.getCurrentElection();
			throw new ElectionAlreadyExistsException();
		} catch(NoActiveElectionException e){}
		
		Election newElection = new Election();
		newElection.setQuestion(question);
		BallotServerPersistence.addElection(newElection);
		return newElection.getId();
	}
	
	public List<ElectionView> getElections() {
		ArrayList<ElectionView> elections = new ArrayList<ElectionView>();
		List<Election> elist = BallotServerPersistence.getElections();
		
		for (Election e : elist) {
			ArrayList<CandidateView> ecandidates = new ArrayList<CandidateView>();
			
			for (Candidate c : e.getCandidateList()) {
				ecandidates.add(new CandidateView(c.getPersonName(), c.getId()));
			}
			
			ElectionView eview = new ElectionView(e.getId(), e.getQuestion(), ecandidates);
			eview.setStarted(e.getStarted());
			eview.setClosed(e.getClosed());
			elections.add(eview);
		}
		
		return elections;
	}
	
	public void startElection(Long id) {
		try {
			BallotServerPersistence.getCurrentElection();
		} catch(NoActiveElectionException e) {
			Election election = BallotServerPersistence.getElection(id);

			if(election.getStarted())
				throw new ElectionAlreadyStartedException();

			if(election.getClosed())
				throw new ElectionAlreadyClosedException();

			election.setStarted();
			BallotServerPersistence.updateElection(election);
			return;
		}
		throw new MultipleActiveElectionsAttemptException();
	}
	
	public void closeElection() {
		
		Election election = BallotServerPersistence.getCurrentElection();
		
		election.setClosed();
		BallotServerPersistence.updateElection(election);	
	}
	
	public void setElectionQuestion(Long electionId, String question) {
		Election election = BallotServerPersistence.getElection(electionId);

		election.setQuestion(question);
		BallotServerPersistence.updateElection(election);	
	}
	
	public void addCandidate(Long electionId, String name, Long id) {
		
		Election election = BallotServerPersistence.getElection(electionId);
		
		if(election.getStarted())
			throw new ElectionAlreadyStartedException();
		
		if(election.getClosed())
			throw new ElectionAlreadyClosedException();
		
		try{
			election.getCandidate(name, id);
			throw new CandidateAlreadyExistsException(id);
		}
		catch(CandidateDoesNotExistException e){}
		
		Candidate candidate = new Candidate();
		candidate.setPersonName(name);
		candidate.setId(id);
		BallotServerPersistence.addCandidate(candidate);
		
		election.addCandidate(candidate);
		BallotServerPersistence.updateElection(election);
		
	}
	
	public void deleteCandidate(Long electionId, String name, Long id) {
		
		Election election = BallotServerPersistence.getElection(electionId);
		
		if(election.getStarted())
			throw new ElectionAlreadyStartedException();
		
		if(election.getClosed())
			throw new ElectionAlreadyClosedException();
		
		election.deleteCandidate(name, id);
		BallotServerPersistence.updateElection(election);
	}
		
	public ElectionResultView countVotes(Long electionId){
		Election election = BallotServerPersistence.getElection(electionId);
		if (!election.getClosed()) {
			election.setClosed();
			BallotServerPersistence.updateElection(election);
		}
		
		HashMap<Long, Integer> voteCount = new HashMap<Long, Integer>();
		List<Candidate> candidates = election.getCandidateList(); 
		for(Candidate c : candidates){
			voteCount.put(c.getId(), 0);
		}
		
		int totalVotes = 0;
		int expectedVotes = 0;
		int blankVotes = 0;
		
		for(BallotSheet bs : election.getBallotSheets()){
			if(bs.getAuditing()){
				continue;
			}
			
			if(bs.getChosenSquare() == -1 ) {
				blankVotes++;
				totalVotes++;
				continue;
			}
			byte[] safeOffset = bs.getSafeOffset();
			byte[] regToken = bs.getRegToken();
			byte[] tcToken = bs.getTcToken();
			int k = bs.getK();
			byte[] blindedOffset = null;
			
			
			String firstBallotName = bs.getBallotServerGenerator();
			String myName = getMyName();
			List<String> bsNames = getAllBallotServersNames();
			for(int i = bsNames.size(); i > 0; i--){
				String curName = bsNames.get(i-1);
				if(!curName.equals(firstBallotName)){
					if(curName.equals(myName)){
						safeOffset = CriptoUtils.decipherWithSymKey(safeOffset, this.bsSymKey);
					}
					else{
						PublicKey remotePubKey = new ValidateCertificateService(
								Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
								this.tcPubKey, this.bsKeys.getPrivate(), this.serialNumber,
								new BigInteger("-1"), curName).execute();
						safeOffset = new SafeOffsetEncryptionService(getBallotEndpoint(curName), remotePubKey,
								bsKeys.getPrivate(), serialNumber, safeOffset, "decipher").execute();
					}
				}
			}
			if(firstBallotName.equals(myName)){
				safeOffset = CriptoUtils.decipherWithSymKey(safeOffset, this.bsSymKey);
			}
			else{
				PublicKey remotePubKey = new ValidateCertificateService(
						Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
						this.tcPubKey, this.bsKeys.getPrivate(), this.serialNumber,
						new BigInteger("-1"), firstBallotName).execute();
				safeOffset = new SafeOffsetEncryptionService(getBallotEndpoint(firstBallotName), remotePubKey,
						bsKeys.getPrivate(), serialNumber, safeOffset, "decipher").execute();
			}
			blindedOffset = safeOffset;
			int iter;
			byte[] masterToken = new byte[regToken.length];
			for(iter = 0; iter < k; iter++){
				masterToken[iter] = regToken[iter];
			}
			for(iter = k; iter < regToken.length; iter++){
				masterToken[iter] = tcToken[iter];
			}
			BigInteger masterTokenNumber = new BigInteger(masterToken);
			BigInteger blindedOffsetNumber = new BigInteger(blindedOffset);
			int offset = (blindedOffsetNumber.divide(masterTokenNumber)).intValue();
			
			int size = election.getCandidates().size();
			//only count vote if offset is within the correct interval
			//if a vote was planted in the DB, the safeOffset must have been a guessed
			//value since there's no way he could predict it by going over the blinding
			//that randomly depends on the 2 tokens, plus N symmetric consecutive ciphers
			//thus he's guessing in an interval of 2^128 numbers. getting an offset
			//within 0-size-1 of the candidates, is considered impossible
			if(offset >= 0 && offset <= size){
				Long candId = candidates.get((size - offset + bs.getChosenSquare()) % size).getId();
				voteCount.put(candId, voteCount.get(candId) + 1);
				totalVotes++;
			}
		}
		List<CandidateVotesView> cvv = new ArrayList<CandidateVotesView>();
		for(Candidate c : candidates){
			cvv.add(new CandidateVotesView(c.getPersonName(), c.getId(),
					voteCount.get(c.getId())));
		}
		
		try {
			expectedVotes = new TotalVotersService(Config.getInstance().getInitParameter("registration.ws.EndpointAddress"), 
				regPubKey, bsKeys.getPrivate(), serialNumber).execute(); 
		}catch (RegistrationException e) {
			throw new BallotServerException(e);
		}
		
		return new ElectionResultView(electionId, election.getQuestion(), cvv, totalVotes, expectedVotes, blankVotes);
	}
	
	public Integer verifyVote(byte[] regToken, byte[] tcToken, byte[] safeOffsetSent) {
		
		BallotSheet bs = BallotServerPersistence.getBallotSheet(regToken, tcToken);
		byte[] blindedOffsetSent = safeOffsetSent;
		byte[] blindedOffset = bs.getSafeOffset();
		
		String firstBallotName = bs.getBallotServerGenerator();
		String myName = getMyName();
		List<String> bsNames = getAllBallotServersNames();
		
		// Decipher the SafeOffset received and the SafeOffset stored
		for(int i = bsNames.size(); i > 0; i--){
			String curName = bsNames.get(i-1);
			if(!curName.equals(firstBallotName)){
				if(curName.equals(myName)){
					blindedOffsetSent = CriptoUtils.decipherWithSymKey(blindedOffsetSent, this.bsSymKey);
					
					blindedOffset = CriptoUtils.decipherWithSymKey(blindedOffset, this.bsSymKey);
					
				}
				else{
					PublicKey remotePubKey = new ValidateCertificateService(
							Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
							this.tcPubKey, this.bsKeys.getPrivate(), this.serialNumber,
							new BigInteger("-1"), curName).execute();
					
					blindedOffsetSent = new SafeOffsetEncryptionService(getBallotEndpoint(curName), remotePubKey,
							bsKeys.getPrivate(), serialNumber, blindedOffsetSent, "decipher").execute();
					
					blindedOffset = new SafeOffsetEncryptionService(getBallotEndpoint(curName), remotePubKey,
							bsKeys.getPrivate(), serialNumber, blindedOffset, "decipher").execute();
				}
			}
		}
		
		if(firstBallotName.equals(myName)){
			blindedOffsetSent = CriptoUtils.decipherWithSymKey(blindedOffsetSent, this.bsSymKey);
			
			blindedOffset = CriptoUtils.decipherWithSymKey(blindedOffset, this.bsSymKey);
		}
		else{
			PublicKey remotePubKey = new ValidateCertificateService(
					Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
					this.tcPubKey, this.bsKeys.getPrivate(), this.serialNumber,
					new BigInteger("-1"), firstBallotName).execute();
			
			blindedOffsetSent = new SafeOffsetEncryptionService(getBallotEndpoint(firstBallotName), remotePubKey,
					bsKeys.getPrivate(), serialNumber, blindedOffsetSent, "decipher").execute();
			
			blindedOffset = new SafeOffsetEncryptionService(getBallotEndpoint(firstBallotName), remotePubKey,
					bsKeys.getPrivate(), serialNumber, blindedOffset, "decipher").execute();
		}
		
		//Check if they match
		if(Arrays.equals(blindedOffset, blindedOffsetSent)) {
			return bs.getChosenSquare();
		}else {
			throw new ElectionAsBeenCorruptedException();
		}
	}
	
	public Integer translateOffset(byte[] regToken, byte[] tcToken, byte[] blindedOffsetSent){
		BallotSheet bs = BallotServerPersistence.getBallotSheet(regToken, tcToken);
		
		String firstBallotName = bs.getBallotServerGenerator();
		String myName = getMyName();
		List<String> bsNames = getAllBallotServersNames();
		
		// Decipher the SafeOffset received and the SafeOffset stored
		for(int i = bsNames.size(); i > 0; i--){
			String curName = bsNames.get(i-1);
			if(!curName.equals(firstBallotName)){
				if(curName.equals(myName)){
					blindedOffsetSent = CriptoUtils.decipherWithSymKey(blindedOffsetSent, this.bsSymKey);
				}
				else{
					PublicKey remotePubKey = new ValidateCertificateService(
							Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
							this.tcPubKey, this.bsKeys.getPrivate(), this.serialNumber,
							new BigInteger("-1"), curName).execute();
					
					blindedOffsetSent = new SafeOffsetEncryptionService(getBallotEndpoint(curName), remotePubKey,
							bsKeys.getPrivate(), serialNumber, blindedOffsetSent, "decipher").execute();
				}
			}
		}
		
		if(firstBallotName.equals(myName)){
			blindedOffsetSent = CriptoUtils.decipherWithSymKey(blindedOffsetSent, this.bsSymKey);
		}
		else{
			PublicKey remotePubKey = new ValidateCertificateService(
					Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
					this.tcPubKey, this.bsKeys.getPrivate(), this.serialNumber,
					new BigInteger("-1"), firstBallotName).execute();
			
			blindedOffsetSent = new SafeOffsetEncryptionService(getBallotEndpoint(firstBallotName), remotePubKey,
					bsKeys.getPrivate(), serialNumber, blindedOffsetSent, "decipher").execute();
		}
		
		int iter;
		int k = bs.getK();
		byte[] masterToken = new byte[regToken.length];
		for(iter = 0; iter < k; iter++){
			masterToken[iter] = regToken[iter];
		}
		for(iter = k; iter < regToken.length; iter++){
			masterToken[iter] = tcToken[iter];
		}
		BigInteger masterTokenNumber = new BigInteger(masterToken);
		BigInteger blindedOffsetNumber = new BigInteger(blindedOffsetSent);
		return new Integer((blindedOffsetNumber.divide(masterTokenNumber)).intValue());
	}
	
}
