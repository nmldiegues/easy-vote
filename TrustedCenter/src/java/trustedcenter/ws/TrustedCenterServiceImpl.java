package trustedcenter.ws;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;

import org.bouncycastle.util.Arrays;

import sirs.framework.config.Config;
import sirs.framework.criptography.CriptoUtils;
import trustedcenter.exceptions.CertificateDoesNotExistException;
import trustedcenter.exceptions.InvalidCertificateException;
import trustedcenter.exceptions.InvalidDigitalSignatureException;
import trustedcenter.exceptions.TCCriptographicException;
import trustedcenter.exceptions.TrustedCenterException;
import trustedcenter.server.TrustedCenterApp;
import trustedcenter.ws.client.service.ValidateCertificateService;
import trustedcenter.ws.ties.BlindSignatureRequestType;
import trustedcenter.ws.ties.BlindSignatureResponseType;
import trustedcenter.ws.ties.ExchangeSecretRequestType;
import trustedcenter.ws.ties.ExchangeSecretResponseType;
import trustedcenter.ws.ties.GenerateCertificateRequest;
import trustedcenter.ws.ties.GenerateCertificateResponse;
import trustedcenter.ws.ties.GetPublicKeyRequest;
import trustedcenter.ws.ties.GetPublicKeyResponse;
import trustedcenter.ws.ties.ServiceError;
import trustedcenter.ws.ties.ServiceError_Exception;
import trustedcenter.ws.ties.TrustedCenterFault;
import trustedcenter.ws.ties.TrustedCenterFault_Exception;
import trustedcenter.ws.ties.TrustedCenterPortType;
import trustedcenter.ws.ties.ValidateCertificateRequest;
import trustedcenter.ws.ties.ValidateCertificateResponse;

@javax.jws.WebService (endpointInterface="trustedcenter.ws.ties.TrustedCenterPortType")
public class TrustedCenterServiceImpl implements TrustedCenterPortType{

	private TrustedCenterApp app;
	
	public TrustedCenterServiceImpl() {
		app = new TrustedCenterApp();
	}
	
	@Override
	public GenerateCertificateResponse generateCertificate(GenerateCertificateRequest generateCertificateRequest)
	throws ServiceError_Exception, TrustedCenterFault_Exception{
		try{
			GenerateCertificateResponse response = new GenerateCertificateResponse();
			String distinguishedName = generateCertificateRequest.getDistinguishedName();

			//recreating pubKey from encoded string
			PublicKey publicKey = CriptoUtils.recreatePublicKey(CriptoUtils.base64decode((generateCertificateRequest.getPublicKey())));
			//generating certificate
			X509Certificate cert = app.generateCertificate(distinguishedName, publicKey);

			response.setSerialNumber(cert.getSerialNumber().toString());
			return response;
		}
		catch(TrustedCenterException e){
			TrustedCenterFault ff = new TrustedCenterFault();
			ff.setFaultType(e.getClass().getName());
			throw new TrustedCenterFault_Exception(e.getMessage(), ff, e);
		}
		catch (Exception e) {
			throw new ServiceError_Exception("Service currently unavailable. Please try again later.",
					new ServiceError(), e);
		}
	}

	@Override
	public ValidateCertificateResponse validateCertificate(ValidateCertificateRequest validateCertificateRequest) 
	throws ServiceError_Exception, TrustedCenterFault_Exception{
		try{
			//confidentiality:
			//no need, it's public information
			BigInteger serialNumber = new BigInteger(validateCertificateRequest.getSerialNumber());
			String distinguishedName = validateCertificateRequest.getDistinguishedName();
			BigInteger requesterSerialNumber = new BigInteger(validateCertificateRequest.getRequesterSerialNumber());
			
			//integrity and authenticity:
			//acquiring requester public key based off his certificate serial number (also validates it)
			PublicKey requesterPubKey = app.getPublicKey(requesterSerialNumber);
			//recomputing hash arguments serialNumber, distinguishedName and requesterSerialNumber
			//deciphering received digital signature with acquired requester public key
			//verifying that both hashes are equal
			byte[] computedHash = CriptoUtils.computeDigest(serialNumber.toByteArray(), 
					distinguishedName.getBytes(), requesterSerialNumber.toByteArray());
			byte[] receivedHash = CriptoUtils.decipherWithPublicKey(
					CriptoUtils.base64decode(validateCertificateRequest.getDigitalSignature()), requesterPubKey);
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: RegexchangeSecret server side");
			}
			
			ValidateCertificateResponse response = new ValidateCertificateResponse();
			PublicKey pubKey = null;
			try{
				if(!(serialNumber.equals(new BigInteger("-1")))){
					pubKey = app.getPublicKey(serialNumber);
				}
				else{
					pubKey = app.getPublicKey(distinguishedName);
				}
				response.setValid("valid");
				response.setPublicKey(CriptoUtils.base64encode(pubKey.getEncoded()));
			} catch(InvalidCertificateException e){
				response.setValid("invalid");
				response.setPublicKey("");
			} catch(CertificateDoesNotExistException e){
				response.setValid("invalid");
				response.setPublicKey("");
			}
			
			//integrity and authenticity:
			//computing hash of arguments valid and publicKey
			//ciphering it with trustedcenter private key. encoding result for sending
			response.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
					CriptoUtils.computeDigest(response.getValid().getBytes(),
							response.getPublicKey().getBytes()), app.getTrustedServerPrivateKey())));
			
			return response;
		}
		catch(TrustedCenterException e){
			TrustedCenterFault ff = new TrustedCenterFault();
			ff.setFaultType(e.getClass().getName());
			throw new TrustedCenterFault_Exception(e.getMessage(), ff, e);
		}
		catch (Exception e) {
			throw new ServiceError_Exception("Service currently unavailable. Please try again later.",
					new ServiceError(), e);
		}
	}

	@Override
	public GetPublicKeyResponse getPublicKey(GetPublicKeyRequest getPublicKeyRequest) 
	throws ServiceError_Exception, TrustedCenterFault_Exception{
		try{
			GetPublicKeyResponse response = new GetPublicKeyResponse();
			response.setPublicKey(CriptoUtils.base64encode(app.getTrustedServerKey().getEncoded()));
		
			return response;
		}
		catch(TrustedCenterException e){
			TrustedCenterFault ff = new TrustedCenterFault();
			ff.setFaultType(e.getClass().getName());
			throw new TrustedCenterFault_Exception(e.getMessage(), ff, e);
		}
		catch (Exception e) {
			throw new ServiceError_Exception("Service currently unavailable. Please try again later.",
					new ServiceError(), e);
		}
	}

	@Override
	public BlindSignatureResponseType blindSignature(BlindSignatureRequestType parameters) 
	throws ServiceError_Exception, TrustedCenterFault_Exception{
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
			
			//integrity and authenticity:
			//acquiring voter public key based off his certificate serial number (also validates it)
			PublicKey voterPubKey = app.getPublicKey(certSerialNumber);
			//recomputing hash of deciphered arguments blindedMessage, certSerialNumber,
			//the plain argument voterId and the deciphered credentials
			//deciphering received digital signature with acquired voter public key
			//verifying that both hashes are equal
			byte[] computedHash = CriptoUtils.computeDigest(blindedMessage, certSerialNumber.toByteArray(), 
					voterId.toString().getBytes());
			byte[] receivedHash = CriptoUtils.decipherWithPublicKey(
					CriptoUtils.base64decode(parameters.getDigitalSignature()), voterPubKey);
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: exchangeSecret server side");
			}
			
			byte[] blindedSignedToken = null;
			//confirm voting ID and credentials it in local DB, if already voted, send sent token
			//for that, we verify that the requester's certificate distinguished name
			//matches the given voter ID
			if(!app.getDistinguishedName(certSerialNumber).equals("voter"+new Long(parameters.getVoterId()))){
				throw new InvalidCertificateException();
			}
			if(!app.hasUserRequestedToken(new Long(parameters.getVoterId()))){
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
			//computing hash of unciphered argument sharedKey
			//ciphering it with trusted center private key. encoding result for sending
			response.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
					CriptoUtils.computeDigest(blindedSignedToken), app.getTrustedServerPrivateKey())));
			
			return response;
		}
		catch(TrustedCenterException e){
			TrustedCenterFault ff = new TrustedCenterFault();
			ff.setFaultType(e.getClass().getName());
			throw new TrustedCenterFault_Exception(e.getMessage(), ff, e);
		}
		catch (Exception e) {
			throw new ServiceError_Exception("Service currently unavailable. Please try again later.",
					new ServiceError(), e);
		}
	}

	@Override
	public ExchangeSecretResponseType exchangeSecret(ExchangeSecretRequestType parameters)
			throws ServiceError_Exception, TrustedCenterFault_Exception {
		try{
			//confidentiality:
			//deciphering arguments with self private key
			BigInteger voterSerialNumber = new BigInteger(CriptoUtils.decipherWithPrivateKey(
					CriptoUtils.base64decode(parameters.getCertificateSerialNumber()), app.getTrustedServerPrivateKey()));
			Long voterId = new Long(new String(CriptoUtils.decipherWithPrivateKey(
					CriptoUtils.base64decode(parameters.getVoterId()), app.getTrustedServerPrivateKey())));
			
			//integrity and authenticity:
			//acquiring voter public key based off his certificate serial number (also validates it)
			PublicKey voterPubKey = app.getPublicKey(voterSerialNumber);
			
			//recomputing hash of deciphered arguments voterSerialNumber, voterId, credentials
			//deciphering received digital signature with acquired voter public key
			//verifying that both hashes are equal
			byte[] computedHash = CriptoUtils.computeDigest(voterSerialNumber.toByteArray(), 
					voterId.toString().getBytes());
			byte[] receivedHash = CriptoUtils.decipherWithPublicKey(
					CriptoUtils.base64decode(parameters.getDigitalSignature()), voterPubKey);
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: TCexchangeSecret server side");
			}
			
			SecretKey key = CriptoUtils.generateJavaSymKey(128);
			//save the new shared key with this voter
			app.registerSharedKey(voterId, key);
			
			ExchangeSecretResponseType response = new ExchangeSecretResponseType();
			
			//confidentiality:
			//cipher the argument sharedKey with the voter Public Key
			response.setSharedKey(CriptoUtils.base64encode(
					CriptoUtils.cipherWithPublicKey(key.getEncoded(), voterPubKey)));
			
			//integrity and authenticity:
			//computing hash of unciphered argument sharedKey
			//ciphering it with voter private key. encoding result for sending
			response.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
					CriptoUtils.computeDigest(key.getEncoded()), app.getTrustedServerPrivateKey())));
			return response;
		}catch(TrustedCenterException e) {
			TrustedCenterFault rf = new TrustedCenterFault();
			rf.setFaultType(e.getClass().getName());
			throw new TrustedCenterFault_Exception(e.getMessage(), rf, e);
		}catch (Exception e) {
			throw new ServiceError_Exception("Service currently unavailable. Please try again later.",
					new ServiceError(), e);
		}
	}

}
