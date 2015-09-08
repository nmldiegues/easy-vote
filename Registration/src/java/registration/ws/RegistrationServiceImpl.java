package registration.ws;

import java.math.BigInteger;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.bouncycastle.util.Arrays;

import registration.exceptions.ErrorValidatingVoterException;
import registration.exceptions.InvalidDigitalSignatureException;
import registration.exceptions.RegistrationException;
import registration.server.RegistrationApp;
import registration.ws.ties.BlindSignatureRequestType;
import registration.ws.ties.BlindSignatureResponseType;
import registration.ws.ties.ExchangeSecretRequestType;
import registration.ws.ties.ExchangeSecretResponseType;
import registration.ws.ties.RegistrationFault;
import registration.ws.ties.RegistrationFault_Exception;
import registration.ws.ties.RegistrationPortType;
import registration.ws.ties.ServiceError;
import registration.ws.ties.ServiceError_Exception;
import registration.ws.ties.TotalVotersRequestType;
import registration.ws.ties.TotalVotersResponseType;
import sirs.framework.config.Config;
import sirs.framework.criptography.CriptoUtils;
import trustedcenter.exceptions.TrustedCenterException;
import trustedcenter.ws.client.service.ValidateCertificateService;

@javax.jws.WebService (endpointInterface="registration.ws.ties.RegistrationPortType")
public class RegistrationServiceImpl implements RegistrationPortType {

	private RegistrationApp app;
	
	public RegistrationServiceImpl(){
		app = new RegistrationApp();
	}
	
	@Override
	public BlindSignatureResponseType blindSignature(BlindSignatureRequestType parameters) 
	throws ServiceError_Exception, RegistrationFault_Exception{
		BlindSignatureResponseType response = new BlindSignatureResponseType();
		try{
			//acquire shared symmetric key based off voterId
			Long voterId = new Long(parameters.getVoterId());
			SecretKey sharedKey = app.getSharedKey(voterId);
			
			//confidentiality:
			//deciphering arguments with shared key
			byte[] blindedMessage = CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(parameters.getBlindedMessage()), sharedKey);
			BigInteger certSerialNumber = new BigInteger(CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(parameters.getCertificateSerialNumber()), sharedKey));
			String credentials = new String(CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(parameters.getCredentials()), sharedKey));
			
			//integrity and authenticity:
			//acquiring voter public key based off his certificate serial number (also validates it)
			PublicKey voterPubKey = null;
			try {
				voterPubKey = new ValidateCertificateService(
						Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
						app.getTCPubKey(), app.getRegKeys().getPrivate(), app.getSerialNumber(),
						certSerialNumber, "").execute();
			} catch(TrustedCenterException e){
				throw new ErrorValidatingVoterException(e);
			}
			//recomputing hash of deciphered arguments blindedMessage, certSerialNumber,
			//the plain argument voterId and the deciphered credentials
			//deciphering received digital signature with acquired voter public key
			//verifying that both hashes are equal
			byte[] computedHash = CriptoUtils.computeDigest(blindedMessage, certSerialNumber.toByteArray(), 
					voterId.toString().getBytes(), credentials.getBytes());
			byte[] receivedHash = CriptoUtils.decipherWithPublicKey(
					CriptoUtils.base64decode(parameters.getDigitalSignature()), voterPubKey);
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: exchangeSecret server side");
			}
			
			//confirm voting ID and credentials it in local DB, if already voted, send sent token
			byte[] blindedSignedToken = null;
			if(!app.validateVoterAttempt(voterId, credentials)){
				//user already requested a vote, thus send him the previously registered token
				blindedSignedToken = app.getRegisteredToken(voterId);
			}
			else{
				//sign blinded text with private key
				blindedSignedToken = app.blindSignature(blindedMessage); 
				//store the token associated with the user ID
				app.registerSignedToken(voterId, blindedSignedToken);
			}
			//confidentiality:
			//cipher the argument with the shared key with the voter
			response.setBlindedMessageSigned(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(blindedSignedToken, sharedKey)));
			
			//integrity and authenticity:
			//computing hash of unciphered argument blindedSignedToken
			//ciphering it with registration private key. encoding result for sending
			response.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
					CriptoUtils.computeDigest(blindedSignedToken), app.getRegKeys().getPrivate())));
			
			return response;
			
		}catch(RegistrationException e) {
			RegistrationFault rf = new RegistrationFault();
			rf.setFaultType(e.getClass().getName());
			throw new RegistrationFault_Exception(e.getMessage(), rf, e);
		}catch (Exception e) {
			throw new ServiceError_Exception("Service currently unavailable. Please try again later.",
					new ServiceError(), e);
		}
	}

	@Override
	public ExchangeSecretResponseType exchangeSecret(ExchangeSecretRequestType parameters)
			throws RegistrationFault_Exception, ServiceError_Exception {
		try{
			//confidentiality:
			//deciphering arguments with self private key
			BigInteger voterSerialNumber = new BigInteger(CriptoUtils.decipherWithPrivateKey(
					CriptoUtils.base64decode(parameters.getCertificateSerialNumber()), app.getRegKeys().getPrivate()));
			Long voterId = new Long(new String(CriptoUtils.decipherWithPrivateKey(
					CriptoUtils.base64decode(parameters.getVoterId()), app.getRegKeys().getPrivate())));
			String credentials = new String(CriptoUtils.decipherWithPrivateKey(
					CriptoUtils.base64decode(parameters.getCredentials()), app.getRegKeys().getPrivate()));
			
			//integrity and authenticity:
			//acquiring voter public key based off his certificate serial number (also validates it)
			PublicKey voterPubKey = null;
			try{
				voterPubKey = new ValidateCertificateService(
						Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
						app.getTCPubKey(), app.getRegKeys().getPrivate(), app.getSerialNumber(), 
						voterSerialNumber, "").execute();
			} catch(TrustedCenterException e){
				throw new ErrorValidatingVoterException(e);
			}
			//recomputing hash of deciphered arguments voterSerialNumber, voterId, credentials
			//deciphering received digital signature with acquired voter public key
			//verifying that both hashes are equal
			byte[] computedHash = CriptoUtils.computeDigest(voterSerialNumber.toByteArray(), 
					voterId.toString().getBytes(), credentials.getBytes());
			byte[] receivedHash = CriptoUtils.decipherWithPublicKey(
					CriptoUtils.base64decode(parameters.getDigitalSignature()), voterPubKey);
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: RegexchangeSecret server side");
			}
			
			SecretKey key = CriptoUtils.generateJavaSymKey(128);
			//save the new shared key with this voter
			app.registerSharedKey(voterId, credentials, key);
			
			ExchangeSecretResponseType response = new ExchangeSecretResponseType();
			
			//confidentiality:
			//cipher the argument sharedKey with the voter Public Key
			response.setSharedKey(CriptoUtils.base64encode(
					CriptoUtils.cipherWithPublicKey(key.getEncoded(), voterPubKey)));
			
			//integrity and authenticity:
			//computing hash of unciphered argument sharedKey
			//ciphering it with registration private key. encoding result for sending
			response.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
					CriptoUtils.computeDigest(key.getEncoded()), app.getRegKeys().getPrivate())));
			return response;
		}catch(RegistrationException e) {
			RegistrationFault rf = new RegistrationFault();
			rf.setFaultType(e.getClass().getName());
			throw new RegistrationFault_Exception(e.getMessage(), rf, e);
		}catch (Exception e) {
			throw new ServiceError_Exception("Service currently unavailable. Please try again later.",
					new ServiceError(), e);
		}
	}

	@Override
	public TotalVotersResponseType totalVoters(TotalVotersRequestType parameters)
			throws RegistrationFault_Exception, ServiceError_Exception {
		//confidentiality:
		//deciphering arguments with self private key
		BigInteger requesterSerialNumber = new BigInteger(CriptoUtils.decipherWithPrivateKey(
				CriptoUtils.base64decode(parameters.getCertificateSerialNumber()), app.getRegKeys().getPrivate()));
		
		//integrity and authenticity:
		//acquiring requester public key based off his certificate serial number (also validates it)
		PublicKey requesterPubKey = null;
		try{
			requesterPubKey = new ValidateCertificateService(
					Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
					app.getTCPubKey(), app.getRegKeys().getPrivate(), app.getSerialNumber(), 
					requesterSerialNumber, "").execute();
		} catch(TrustedCenterException e){
			throw new ErrorValidatingVoterException(e);
		}
		//recomputing hash of deciphered argument requesterSerialNumber
		//deciphering received digital signature with acquired requesterSerialNumber
		//verifying that both hashes are equal
		byte[] computedHash = CriptoUtils.computeDigest(requesterSerialNumber.toByteArray());
		byte[] receivedHash = CriptoUtils.decipherWithPublicKey(
				CriptoUtils.base64decode(parameters.getDigitalSignature()), requesterPubKey);
		if(!Arrays.areEqual(computedHash, receivedHash)){
			throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: totalVotes server side");
		}
		
		TotalVotersResponseType response = new TotalVotersResponseType();
		
		//confidenciality:
		//this number is public, no need to cipher
		response.setNumberVotersRegistered(app.getNumberVotersAllowed());
		
		//integrity and authenticity:
		//computing hash of argument NumberVotersRegistered
		//ciphering it with registration private key. encoding result for sending
		response.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
				CriptoUtils.computeDigest(
						new Integer(response.getNumberVotersRegistered()).toString().getBytes()),
						app.getRegKeys().getPrivate())));
		
		return response;
	}


}
