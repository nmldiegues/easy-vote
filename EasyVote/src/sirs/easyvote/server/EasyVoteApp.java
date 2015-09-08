package sirs.easyvote.server;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Random;
import java.util.UUID;

import javax.crypto.SecretKey;

import registration.exceptions.ErrorCreatingRegCertException;
import registration.exceptions.InvalidVoterCredentialsException;
import registration.exceptions.NotEligibleVoterException;
import registration.exceptions.RegistrationException;
import registration.ws.client.service.BlindSignatureService;
import registration.ws.client.service.ExchangeSecretService;
import sirs.easyvote.exception.EasyVoteException;
import sirs.easyvote.exception.ElectionHasBeenCorruptedException;
import sirs.easyvote.exception.ErrorCastingVoteException;
import sirs.easyvote.exception.ErrorCreatingVoterCertException;
import sirs.easyvote.exception.ErrorRetrievingBallotSheetException;
import sirs.easyvote.exception.ErrorRetrievingKeyFromTcException;
import sirs.easyvote.exception.NoElectionActiveException;
import sirs.easyvote.exception.UserAlreadyVotedException;
import sirs.easyvote.exception.VoterDoesNotExistException;
import sirs.easyvote.exception.WrongPasswordException;
import sirs.easyvote.shared.GenericVerificationView;
import sirs.easyvote.views.ViewVoter;
import sirs.framework.config.Config;
import sirs.framework.criptography.CriptoUtils;
import sirs.framework.exception.RemoteException;
import trustedcenter.exceptions.DoesNotHaveCertificateException;
import trustedcenter.exceptions.TrustedCenterException;
import trustedcenter.ws.client.service.GenerateCertificateService;
import trustedcenter.ws.client.service.GetPublicKeyService;
import trustedcenter.ws.client.service.ValidateCertificateService;
import ballotserver.exceptions.BallotServerException;
import ballotserver.exceptions.NoActiveElectionException;
import ballotserver.views.BallotSheetView;
import ballotserver.ws.client.service.CastVoteService;
import ballotserver.ws.client.service.GetBallotSheetService;
import ballotserver.ws.client.service.TranslateOffsetService;

public class EasyVoteApp {

	private PublicKey tcPubKey;
	private PublicKey regPubKey;
	private KeyPair evKeys;
	private BigInteger evSerialNumber;

	public EasyVoteApp() {
		//Note that acquiring TC key and generating the certificate, should be done in a secure
		//fashion, most likely within physical presence to prove our identity for issuing of a certificate

		//generate keypair and request a certificate for it from the TrustedCenter
		this.evKeys = CriptoUtils.generateJavaKeys(1024);

		//but first, get TC key
		try {
			this.tcPubKey = new GetPublicKeyService(
					Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress")).execute();
		}catch (TrustedCenterException e) {
			throw new ErrorRetrievingKeyFromTcException(e.getMessage());
		}

		//requesting a certificate creation now
		try {
			this.evSerialNumber = new GenerateCertificateService(
					Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
					"EasyVote", this.evKeys.getPublic()).execute();
		} catch(TrustedCenterException e) {
			throw new ErrorCreatingRegCertException(e.getMessage());
		}

		//getting Registration key
		try{
			this.regPubKey = new ValidateCertificateService(
					Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
					this.tcPubKey, this.evKeys.getPrivate(), this.evSerialNumber,
					new BigInteger("-1"), "Registration").execute();	
		}catch (TrustedCenterException e) {
			throw new ErrorRetrievingKeyFromTcException(e.getMessage());
		}
	}


	public ViewVoter registrationForVotingPhase(ViewVoter vv){
		if((vv.getRegSignedToken() != null) && (vv.getTcSignedToken() != null)){
			return vv;
		}
		
		//generate keypair and request a certificate for it from the TrustedCenter
		vv.setVoterKeys(CriptoUtils.generateJavaKeys(1024));

		//requesting a certificate creation now
		GenerateCertificateService request = new GenerateCertificateService(
				Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
				"voter" + vv.getVoterId(),
				vv.getVoterKeys().getPublic());

		try {
			vv.setVoterSerialNumber(request.execute());
		}catch(TrustedCenterException e) {
			throw new ErrorCreatingVoterCertException(e.getMessage(), vv.getVoterId());
		}

		Config config = Config.getInstance();
		SecretKey sharedKeyWithReg = null;
		try{
			sharedKeyWithReg = new ExchangeSecretService(config.getInitParameter("registration.ws.EndpointAddress"), 
					regPubKey, vv.getVoterKeys().getPrivate(), vv.getVoterSerialNumber(), vv.getVoterId(), vv.getPassword()).execute();
		}
		catch(RegistrationException e){
			if(e instanceof InvalidVoterCredentialsException) {
				throw new WrongPasswordException(e.getMessage(), vv.getVoterId());
			}else if(e instanceof NotEligibleVoterException) {
				throw new VoterDoesNotExistException(e.getMessage(), vv.getVoterId());
			}else {
				throw new EasyVoteException(e.getMessage());
			}
		}

		//blind token to the Registration
		try{
			vv.setRegToken(UUID.randomUUID());
			vv.setRegBlindingFactor(CriptoUtils.generateBlindingFactor(CriptoUtils.publicKeyToCipherParameters(regPubKey)));
			vv.setRegBlindedToken(CriptoUtils.blind(CriptoUtils.publicKeyToCipherParameters(regPubKey), vv.getRegBlindingFactor(), vv.getRegToken().toString().getBytes()));

			BlindSignatureService service = new BlindSignatureService(config.getInitParameter("registration.ws.EndpointAddress"),
					sharedKeyWithReg,
					regPubKey,
					vv.getVoterKeys().getPrivate(),
					vv.getRegBlindedToken(),
					vv.getVoterSerialNumber(),
					vv.getVoterId(),
					vv.getPassword());

			//Request for registration and assign 
			vv.setRegBlindedSignedToken(service.execute());
			
			//Get unblinded Registration token
			vv.setRegSignedToken(CriptoUtils.unblind(
					CriptoUtils.publicKeyToCipherParameters(regPubKey),
					vv.getRegBlindingFactor(),
					vv.getRegBlindedSignedToken()));
			
			//Verify is user already voted checking the registration key
			if(!CriptoUtils.verify(CriptoUtils.publicKeyToCipherParameters(regPubKey), vv.getRegToken().toString().getBytes(), vv.getRegSignedToken())) {
				throw new UserAlreadyVotedException(vv.getVoterId());
			}
			
		} catch(RegistrationException e) {
			if(e instanceof InvalidVoterCredentialsException) {
				throw new WrongPasswordException(e.getMessage(), vv.getVoterId());
			}else if(e instanceof NotEligibleVoterException) {
				throw new VoterDoesNotExistException(e.getMessage(), vv.getVoterId());
			}else {
				throw new EasyVoteException(e.getMessage());
			}
		} catch(RemoteException e){
			throw new EasyVoteException(e);
		} 	

		SecretKey sharedKeyWithTC = null;
		try{
			sharedKeyWithTC = new trustedcenter.ws.client.service.ExchangeSecretService(
					config.getInitParameter("trustedcenter.ws.EndpointAddress"), 
					tcPubKey, vv.getVoterKeys().getPrivate(), vv.getVoterSerialNumber(),
					vv.getVoterId()).execute();
		}
		catch(TrustedCenterException e){
			if(e instanceof DoesNotHaveCertificateException) {
				throw new VoterDoesNotExistException(e.getMessage(), vv.getVoterId());
			}else {
				throw new EasyVoteException(e.getMessage());
			}
		}

		try{
			//blind token to the TrustedCenter
			vv.setTcToken(UUID.randomUUID());
			vv.setTcBlindingFactor(CriptoUtils.generateBlindingFactor(CriptoUtils.publicKeyToCipherParameters(tcPubKey)));
			vv.setTcBlindedToken(CriptoUtils.blind(CriptoUtils.publicKeyToCipherParameters(tcPubKey), vv.getTcBlindingFactor(), vv.getTcToken().toString().getBytes()));

			trustedcenter.ws.client.service.BlindSignatureService tcService = new trustedcenter.ws.client.service.BlindSignatureService(
					config.getInitParameter("trustedcenter.ws.EndpointAddress"),
					sharedKeyWithTC,
					tcPubKey,
					vv.getVoterKeys().getPrivate(),
					vv.getTcBlindedToken(),
					vv.getVoterSerialNumber(),
					vv.getVoterId());

			//Request for registration and assign 
			vv.setTcBlindedSignedToken(tcService.execute());

			//Get unblinded TC token
			vv.setTcSignedToken(CriptoUtils.unblind(
					CriptoUtils.publicKeyToCipherParameters(tcPubKey),
					vv.getTcBlindingFactor(), 
					vv.getTcBlindedSignedToken()));
			
			//Verify is user already voted checking the registration key
			if(!CriptoUtils.verify(CriptoUtils.publicKeyToCipherParameters(tcPubKey), vv.getTcToken().toString().getBytes(), vv.getTcSignedToken())) {
				throw new UserAlreadyVotedException(vv.getVoterId());
			}

		} catch(TrustedCenterException e) {
			throw new EasyVoteException(e.getMessage());
		} catch(RemoteException e){
			throw new EasyVoteException(e);
		} 	

		return vv;
	}
	
	public int getNumberBallots(){
		return Integer.parseInt(Config.getInstance().getInitParameter("number.ballotservers"));
	}
	
	public String getBallotName(int number){
		return Config.getInstance().getInitParameter("name.ballotserver" + number);
	}
	
	public String getBallotEndpoint(String name){
		return Config.getInstance().getInitParameter(name.toLowerCase() + ".ws.EndpointAddress");
	}
	
	public BallotSheetView votingPhaseGettingSheet(ViewVoter vv){
		Random randomGenerator = new Random();
		int bsNumber = randomGenerator.nextInt(getNumberBallots())+1;
		
		vv.setBallotServerName(getBallotName(bsNumber));
		vv.setBallotServerEndpoint(getBallotEndpoint(vv.getBallotServerName()));

		PublicKey bsPubKey = null;
		//getting ballot sheet key
		try{
			bsPubKey = new ValidateCertificateService(
					Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
					this.tcPubKey, this.evKeys.getPrivate(), this.evSerialNumber,
					new BigInteger("-1"), vv.getBallotServerName()).execute();	
		}catch (TrustedCenterException e) {
			throw new ErrorRetrievingKeyFromTcException(e);
		}

		SecretKey sharedKeyWithBS = null;
		if(vv.getSharedKey() == null){
			sharedKeyWithBS = CriptoUtils.generateJavaSymKey(128);
			vv.setSharedKey(sharedKeyWithBS);
			try{
				new ballotserver.ws.client.service.ExchangeSecretService(
						vv.getBallotServerEndpoint(), bsPubKey, sharedKeyWithBS,
						vv.getRegToken().toString().getBytes(), vv.getTcToken().toString().getBytes()).execute();
			}
			catch(BallotServerException e){
				throw new ErrorRetrievingBallotSheetException(e);
			}
		}
		else{
			sharedKeyWithBS = vv.getSharedKey();
		}
		
		try{
			BallotSheetView bsView = new GetBallotSheetService(vv.getBallotServerEndpoint(),
					sharedKeyWithBS, vv.getRegSignedToken(), 
					vv.getRegToken().toString().getBytes(), vv.getTcSignedToken(), 
					vv.getTcToken().toString().getBytes()).execute();

			vv.setBallotSheetView(bsView);
			
			return bsView;
		}
		catch(BallotServerException e){
			if(e instanceof NoActiveElectionException) {
				throw new NoElectionActiveException(e);
			} else {
				throw new ErrorRetrievingBallotSheetException(e);
			}
		}		
	}
	
	public void castVote(ViewVoter vv, Integer chosenSquare){
		vv.setChosenSquare(chosenSquare);
		
		try{	
			new CastVoteService(vv.getBallotServerEndpoint(), vv.getSharedKey(), vv.getRegSignedToken(), 
					vv.getRegToken().toString().getBytes(), vv.getTcSignedToken(), 
					vv.getTcToken().toString().getBytes(), vv.getChosenSquare(), vv.getAuditing()).execute();
		}
		catch(BallotServerException e){
			throw new ErrorCastingVoteException(e);
		}
	}
	
	public GenericVerificationView verifyVote(ViewVoter vv) {
		Integer squareChosenReceived;		
		try {
			squareChosenReceived = new ballotserver.ws.client.service.VerifyVoteService(
				vv.getBallotServerEndpoint(), vv.getSharedKey(), vv.getRegSignedToken(),
				vv.getRegToken().toString().getBytes(), vv.getTcSignedToken(), 
				vv.getTcToken().toString().getBytes(), vv.getBallotSheetView().getSafeOffset()).execute();
		}catch (BallotServerException e) {
			throw new ElectionHasBeenCorruptedException(e);
		}

		if(!vv.getChosenSquare().equals(squareChosenReceived)) {
			throw new ElectionHasBeenCorruptedException();
		}
		return new GenericVerificationView(vv.getChosenSquare(), squareChosenReceived);
	}
	
	public GenericVerificationView verifyOffset(ViewVoter vv){
		Integer offsetReceived;
		try{
			offsetReceived = new TranslateOffsetService(vv.getBallotServerEndpoint(), vv.getSharedKey(),
					vv.getRegSignedToken(), vv.getRegToken().toString().getBytes(), vv.getTcSignedToken(),
					vv.getTcToken().toString().getBytes(), vv.getBallotSheetView().getSafeOffset()).execute();
		}catch (BallotServerException e) {
			throw new ElectionHasBeenCorruptedException(e);
		}
		
		if(!vv.getAuditingOffset().equals(offsetReceived)){
			throw new ElectionHasBeenCorruptedException();
		}
		return new GenericVerificationView(vv.getAuditingOffset(), offsetReceived);
	}
	
}
