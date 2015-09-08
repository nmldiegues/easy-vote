package ballotserver.ws;


import java.math.BigInteger;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.bouncycastle.util.Arrays;

import sirs.framework.config.Config;
import sirs.framework.criptography.CriptoUtils;
import trustedcenter.ws.client.service.ValidateCertificateService;
import ballotserver.exceptions.BallotServerException;
import ballotserver.exceptions.InvalidDigitalSignatureException;
import ballotserver.exceptions.InvalidMACException;
import ballotserver.exceptions.InvalidTokenSignatureException;
import ballotserver.server.BallotServerApp;
import ballotserver.server.BallotServerRoot;
import ballotserver.views.BallotSheetView;
import ballotserver.ws.ties.BallotServerFault;
import ballotserver.ws.ties.BallotServerFault_Exception;
import ballotserver.ws.ties.BallotServerPortType;
import ballotserver.ws.ties.CastVoteRequestType;
import ballotserver.ws.ties.CastVoteResponseType;
import ballotserver.ws.ties.ExchangeSecretRequestType;
import ballotserver.ws.ties.ExchangeSecretResponseType;
import ballotserver.ws.ties.GetBallotSheetRequestType;
import ballotserver.ws.ties.GetBallotSheetResponseType;
import ballotserver.ws.ties.SafeOffsetEncryptionRequestType;
import ballotserver.ws.ties.SafeOffsetEncryptionResponseType;
import ballotserver.ws.ties.ServiceError;
import ballotserver.ws.ties.ServiceError_Exception;
import ballotserver.ws.ties.TranslateOffsetRequestType;
import ballotserver.ws.ties.TranslateOffsetResponseType;
import ballotserver.ws.ties.VerifyVoteRequestType;
import ballotserver.ws.ties.VerifyVoteResponseType;


@javax.jws.WebService (endpointInterface="ballotserver.ws.ties.BallotServerPortType")
public class BallotServerServiceImpl implements BallotServerPortType {

	private BallotServerApp app;
	
	public BallotServerServiceImpl(){
		app = BallotServerRoot.getBallotServerApp();
	}

	@Override
	public GetBallotSheetResponseType getBallotSheet(GetBallotSheetRequestType parameters)
			throws BallotServerFault_Exception, ServiceError_Exception {
		try{
			//acquire shared symmetric key based off both received tokens
			byte[] regToken = CriptoUtils.base64decode(parameters.getRegToken());
			byte[] tcToken = CriptoUtils.base64decode(parameters.getTcToken());
			SecretKey sharedKey = app.getSharedKey(regToken, tcToken);
			
			//confidentiality:
			//deciphering arguments signedTokens with acquired shared key
			byte[] regSignedToken = CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(parameters.getRegSignedToken()), sharedKey);
			byte[] tcSignedToken = CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(parameters.getTcSignedToken()), sharedKey);
			
			//integrity and authenticity:
			//verifying the received MAC
			byte[] computedHash = CriptoUtils.computeDigest(regSignedToken, regToken,
					tcSignedToken, tcToken, sharedKey.getEncoded());
			byte[] receivedHash = CriptoUtils.base64decode(parameters.getMac());
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidMACException("Invalid MAC in webservice server: getBallotSheet");
			}
			
			//Verify Registration signature on the token
			if(!CriptoUtils.verify(CriptoUtils.publicKeyToCipherParameters(app.getRegPubKey()), regToken, regSignedToken)) {
				throw new InvalidTokenSignatureException();
			}
			//Verify TC signature on the token
			if(!CriptoUtils.verify(CriptoUtils.publicKeyToCipherParameters(app.getTcPubKey()), tcToken, tcSignedToken)) {
				throw new InvalidTokenSignatureException();
			}
			
			BallotSheetView bs = app.generateBallotSheet(regToken, tcToken);
			
			GetBallotSheetResponseType response = new GetBallotSheetResponseType();
			
			//transforming List<CandidateView> into a String
			//candidateName1,candidateId1#candidateName2,candidateId2...
			String candidateNamesAndIds = bs.produceStringFromBSView();
			
			//integrity and authenticity:
			//using a MAC with the shared key and the SIRSFramework hashing function
			//that means hashing the message (parameters) along with the shared secret
			//encoding result for sending
			response.setMac(CriptoUtils.base64encode(CriptoUtils.computeDigest(
					candidateNamesAndIds.getBytes(), bs.getSafeOffset(), bs.getElectionId().toString().getBytes(),
					bs.getQuestion().getBytes(), sharedKey.getEncoded())));
			
			//confidentiality:
			//ciphering candNamesAndIds, safeOffset, id and question with the shared key with the voter
			response.setCandNamesAndIds(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(candidateNamesAndIds.getBytes(), sharedKey)));
			response.setSafeOffset(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(bs.getSafeOffset(), sharedKey)));
			response.setElectionId(CriptoUtils.base64encode(CriptoUtils.cipherWithSymKey(
					bs.getElectionId().toString().getBytes(), sharedKey)));
			response.setQuestion(CriptoUtils.base64encode(CriptoUtils.cipherWithSymKey(
					bs.getQuestion().getBytes(), sharedKey)));
			
			return response;
			
		}
		catch(BallotServerException e){
			BallotServerFault ff = new BallotServerFault();
			ff.setFaultType(e.getClass().getName());
			throw new BallotServerFault_Exception(e.getMessage(), ff, e);
		}
		catch (Exception e) {
			throw new ServiceError_Exception("Service currently unavailable. Please try again later.",
					new ServiceError(), e);
		}
	}

	@Override
	public ExchangeSecretResponseType exchangeSecret(ExchangeSecretRequestType parameters)
			throws BallotServerFault_Exception, ServiceError_Exception {
		try{
			//confidentiality:
			//deciphering arguments with self private key
			byte[] regToken = CriptoUtils.decipherWithPrivateKey(
					CriptoUtils.base64decode(parameters.getRegToken()), app.getBsKeys().getPrivate());
			byte[] tcToken = CriptoUtils.decipherWithPrivateKey(
					CriptoUtils.base64decode(parameters.getTcToken()), app.getBsKeys().getPrivate());
			SecretKey sharedKey = CriptoUtils.recreateAESKey(CriptoUtils.decipherWithPrivateKey(
					CriptoUtils.base64decode(parameters.getSharedKey()), app.getBsKeys().getPrivate()));
			
			//save the new shared key with this voter
			app.registerSharedKey(regToken, tcToken, sharedKey);
			
			ExchangeSecretResponseType response = new ExchangeSecretResponseType();
			return response;
		}catch(BallotServerException e) {
			BallotServerFault rf = new BallotServerFault();
			rf.setFaultType(e.getClass().getName());
			throw new BallotServerFault_Exception(e.getMessage(), rf, e);
		}catch (Exception e) {
			throw new ServiceError_Exception("Service currently unavailable. Please try again later.",
					new ServiceError(), e);
		}
	}

	@Override
	public CastVoteResponseType castVote(CastVoteRequestType parameters)
			throws BallotServerFault_Exception, ServiceError_Exception {
		//acquire shared symmetric key based off both received tokens
		byte[] regToken = CriptoUtils.base64decode(parameters.getRegToken());
		byte[] tcToken = CriptoUtils.base64decode(parameters.getTcToken());
		SecretKey sharedKey = app.getSharedKey(regToken, tcToken);
		
		//confidentiality:
		//deciphering arguments signedTokens and chosenSquare with acquired shared key
		byte[] regSignedToken = CriptoUtils.decipherWithSymKey(
				CriptoUtils.base64decode(parameters.getRegSignedToken()), sharedKey);
		byte[] tcSignedToken = CriptoUtils.decipherWithSymKey(
				CriptoUtils.base64decode(parameters.getTcSignedToken()), sharedKey);
		Integer chosenSquare = Integer.parseInt(new String(
				CriptoUtils.decipherWithSymKey(
						CriptoUtils.base64decode(parameters.getChosenSquare()),
						sharedKey)));
		Boolean auditing = Boolean.parseBoolean(new String(
				CriptoUtils.decipherWithSymKey(CriptoUtils.base64decode(
						parameters.getAuditing()), sharedKey)));
		
		//Verify Registration signature on the token
		if(!CriptoUtils.verify(CriptoUtils.publicKeyToCipherParameters(app.getRegPubKey()), regToken, regSignedToken)) {
			throw new InvalidTokenSignatureException();
		}
		//Verify TC signature on the token
		if(!CriptoUtils.verify(CriptoUtils.publicKeyToCipherParameters(app.getTcPubKey()), tcToken, tcSignedToken)) {
			throw new InvalidTokenSignatureException();
		}
		
		//integrity and authenticity:
		//verifying the received MAC
		byte[] computedHash = CriptoUtils.computeDigest(regSignedToken, regToken,
				tcSignedToken, tcToken, chosenSquare.toString().getBytes(), 
				auditing.toString().getBytes(), sharedKey.getEncoded());
		byte[] receivedHash = CriptoUtils.base64decode(parameters.getMac());
		if(!Arrays.areEqual(computedHash, receivedHash)){
			throw new InvalidMACException("Invalid MAC in webservice server: getBallotSheet");
		}
		
		app.registerVote(regToken, tcToken, chosenSquare, auditing);
		
		return new CastVoteResponseType();
	}

	@Override
	public SafeOffsetEncryptionResponseType safeOffsetEncryption(SafeOffsetEncryptionRequestType parameters)
			throws BallotServerFault_Exception, ServiceError_Exception {
		//confidentiality:
		//deciphering serialNumber with self public key
		BigInteger requesterSerialNumber = new BigInteger(CriptoUtils.decipherWithPrivateKey(CriptoUtils.base64decode(
						parameters.getCertNumber()), app.getBsKeys().getPrivate()));
		
		String operation = parameters.getOperation();
		byte[] safeOffset = CriptoUtils.base64decode(parameters.getSafeOffset());
		
		//acquiring requester public key
		PublicKey requesterPubKey = new ValidateCertificateService(Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
				app.getTcPubKey(), app.getBsKeys().getPrivate(), app.getBSSerialNumber(),
				requesterSerialNumber, "").execute();
		
		//integrity and authenticity:
		//verifying digital signature
		byte[] computedHash = CriptoUtils.computeDigest(operation.getBytes(), safeOffset, 
				requesterSerialNumber.toByteArray());
		byte[] receivedHash = CriptoUtils.decipherWithPublicKey(CriptoUtils.base64decode(
				parameters.getDigitalSignature()), requesterPubKey);
		if(!Arrays.areEqual(computedHash, receivedHash)){
			throw new InvalidDigitalSignatureException("Invalid digital signature in webservice server: safeOffsetEncryption");
		}
		
		byte[] recomputedSafeOffset = null;
		if(operation.equals("cipher")){
			recomputedSafeOffset = CriptoUtils.cipherWithSymKey(safeOffset, app.getBSSecretKey());
		}
		else{
			recomputedSafeOffset = CriptoUtils.decipherWithSymKey(safeOffset, app.getBSSecretKey());
		}
		
		SafeOffsetEncryptionResponseType response = new SafeOffsetEncryptionResponseType();
		
		//integrity and authenticity:
		//setting up digital signature with own private key
		response.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
				CriptoUtils.computeDigest(recomputedSafeOffset), app.getBsKeys().getPrivate())));
		
		response.setSafeOffset(CriptoUtils.base64encode(recomputedSafeOffset));
		
		return response;
	}
	
	public VerifyVoteResponseType verifyVote(VerifyVoteRequestType parameters) 
		throws BallotServerFault_Exception, ServiceError_Exception {
		
		try{
			//acquire shared symmetric key based off both received tokens
			byte[] regToken = CriptoUtils.base64decode(parameters.getRegToken());
			byte[] tcToken = CriptoUtils.base64decode(parameters.getTcToken());
			SecretKey sharedKey = app.getSharedKey(regToken, tcToken);
			
			
			//confidentiality:
			//deciphering arguments signedTokens with acquired shared key
			byte[] regSignedToken = CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(parameters.getRegSignedToken()), sharedKey);
			byte[] tcSignedToken = CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(parameters.getTcSignedToken()), sharedKey);
			byte[] safeOffset = CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(parameters.getSafeOffset()), sharedKey);
			
			//integrity and authenticity:
			//verifying the received MAC
			byte[] computedHash = CriptoUtils.computeDigest(regSignedToken, regToken,
					tcSignedToken, tcToken, safeOffset, sharedKey.getEncoded());
			byte[] receivedHash = CriptoUtils.base64decode(parameters.getMac());
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidMACException("Invalid MAC in webservice server: verifyVote");
			}
			
			//Verify Registration signature on the token
			if(!CriptoUtils.verify(CriptoUtils.publicKeyToCipherParameters(app.getRegPubKey()), regToken, regSignedToken)) {
				throw new InvalidTokenSignatureException();
			}
			//Verify TC signature on the token
			if(!CriptoUtils.verify(CriptoUtils.publicKeyToCipherParameters(app.getTcPubKey()), tcToken, tcSignedToken)) {
				throw new InvalidTokenSignatureException();
			}
			
			Integer chosenSquare = app.verifyVote(regToken, tcToken, safeOffset);
			
			VerifyVoteResponseType response = new VerifyVoteResponseType();
			
			//integrity and authenticity:
			//using a MAC with the shared key and the SIRSFramework hashing function
			//that means hashing the message (parameters) along with the shared secret
			//encoding result for sending
			response.setMac(CriptoUtils.base64encode(CriptoUtils.computeDigest(
					chosenSquare.toString().getBytes(), sharedKey.getEncoded())));
			
			//confidentiality:
			//ciphering candNamesAndIds and safeOffset with the shared key with the voter
			response.setChosenSquare(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(chosenSquare.toString().getBytes(), sharedKey)));
			
			return response;
			
		}
		catch(BallotServerException e){
			BallotServerFault ff = new BallotServerFault();
			ff.setFaultType(e.getClass().getName());
			throw new BallotServerFault_Exception(e.getMessage(), ff, e);
		}
		catch (Exception e) {
			throw new ServiceError_Exception("Service currently unavailable. Please try again later.",
					new ServiceError(), e);
		}
		
	}

	@Override
	public TranslateOffsetResponseType translateOffset(TranslateOffsetRequestType parameters)
	throws BallotServerFault_Exception, ServiceError_Exception {
		try{
			//acquire shared symmetric key based off both received tokens
			byte[] regToken = CriptoUtils.base64decode(parameters.getRegToken());
			byte[] tcToken = CriptoUtils.base64decode(parameters.getTcToken());
			SecretKey sharedKey = app.getSharedKey(regToken, tcToken);


			//confidentiality:
			//deciphering arguments signedTokens with acquired shared key
			byte[] regSignedToken = CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(parameters.getRegSignedToken()), sharedKey);
			byte[] tcSignedToken = CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(parameters.getTcSignedToken()), sharedKey);
			byte[] safeOffset = CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(parameters.getSafeOffset()), sharedKey);

			//integrity and authenticity:
			//verifying the received MAC
			byte[] computedHash = CriptoUtils.computeDigest(regSignedToken, regToken,
					tcSignedToken, tcToken, safeOffset, sharedKey.getEncoded());
			byte[] receivedHash = CriptoUtils.base64decode(parameters.getMac());
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidMACException("Invalid MAC in webservice server: translateOffset");
			}

			//Verify Registration signature on the token
			if(!CriptoUtils.verify(CriptoUtils.publicKeyToCipherParameters(app.getRegPubKey()), regToken, regSignedToken)) {
				throw new InvalidTokenSignatureException();
			}
			//Verify TC signature on the token
			if(!CriptoUtils.verify(CriptoUtils.publicKeyToCipherParameters(app.getTcPubKey()), tcToken, tcSignedToken)) {
				throw new InvalidTokenSignatureException();
			}

			TranslateOffsetResponseType response = new TranslateOffsetResponseType();
			
			Integer offset = app.translateOffset(regToken, tcToken, safeOffset);

			//integrity and authenticity:
			//using a MAC with the shared key and the SIRSFramework hashing function
			//that means hashing the message (parameters) along with the shared secret
			//encoding result for sending
			response.setMac(CriptoUtils.base64encode(CriptoUtils.computeDigest(
					offset.toString().getBytes(), sharedKey.getEncoded())));
			
			//confidentiality:
			//ciphering candNamesAndIds and safeOffset with the shared key with the voter
			response.setOffset(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(offset.toString().getBytes(), sharedKey)));

			return response;

		}
		catch(BallotServerException e){
			BallotServerFault ff = new BallotServerFault();
			ff.setFaultType(e.getClass().getName());
			throw new BallotServerFault_Exception(e.getMessage(), ff, e);
		}
		catch (Exception e) {
			throw new ServiceError_Exception("Service currently unavailable. Please try again later.",
					new ServiceError(), e);
		}
	}
}
