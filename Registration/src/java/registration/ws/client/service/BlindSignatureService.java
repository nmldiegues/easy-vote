package registration.ws.client.service;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.xml.ws.WebServiceException;

import org.bouncycastle.util.Arrays;

import registration.exceptions.InvalidDigitalSignatureException;
import registration.exceptions.RegistrationException;
import registration.ws.client.RegistrationStubFactory;
import registration.ws.ties.BlindSignatureRequestType;
import registration.ws.ties.BlindSignatureResponseType;
import registration.ws.ties.RegistrationFault_Exception;
import registration.ws.ties.RegistrationPortType;
import registration.ws.ties.ServiceError_Exception;
import sirs.framework.criptography.CriptoUtils;
import sirs.framework.exception.ExceptionParser;
import sirs.framework.exception.RemoteException;
import sirs.framework.ws.StubFactoryException;

public class BlindSignatureService {

	private String endpoint;
	private SecretKey sharedKeyWithReg;
	private PublicKey regPubKey;
	private PrivateKey voterPrivKey;
	private byte[] blindedToken;
	private BigInteger certSerialNumber;
	private Long voterId;
	private String credentials;
	
	public BlindSignatureService(String endpoint, SecretKey sharedKeyWithReg, PublicKey regPubKey, PrivateKey voterPrivKey, byte[] blindedToken, BigInteger certSerialNumber, Long voterId, String credentials){
		this.endpoint = endpoint;
		this.sharedKeyWithReg = sharedKeyWithReg;
		this.regPubKey = regPubKey;
		this.voterPrivKey = voterPrivKey;
		this.blindedToken = blindedToken;
		this.certSerialNumber = certSerialNumber;
		this.voterId = voterId;
		this.credentials = credentials;
	}
	
	public byte[] execute() throws RegistrationException, RemoteException{
		try{
			RegistrationPortType port = RegistrationStubFactory.getInstance().getPort(this.endpoint);
			BlindSignatureRequestType param = new BlindSignatureRequestType();
			
			//integrity and authenticity:
			//computing hash of unciphered arguments blindedMessage, certificateSerialNumber, voterID and credentials
			//ciphering it with voter private key. encoding result for sending
			param.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
					CriptoUtils.computeDigest(this.blindedToken, this.certSerialNumber.toByteArray(),
							this.voterId.toString().getBytes(), this.credentials.getBytes()), this.voterPrivKey)));
			
			//confidenciality:
			//ciphering symetrically arguments blindedMessage, 
			//certificateSerialNumber credentials with shared key with registration
			//NOT ciphering voterId so that Registration will be able to fetch the shared key based off that voterId
			//encoding result for sending
			param.setBlindedMessage(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(this.blindedToken, this.sharedKeyWithReg)));
			param.setCertificateSerialNumber(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(this.certSerialNumber.toByteArray(), this.sharedKeyWithReg)));
			param.setVoterId(this.voterId.toString());
			param.setCredentials(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(this.credentials.getBytes(), this.sharedKeyWithReg)));
			
			
			BlindSignatureResponseType response = port.blindSignature(param);
			
			//confidenciality:
			//deciphering argument blindedMessageSigned with shared key with registration
			byte[] blindedSignedToken = CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(response.getBlindedMessageSigned()), this.sharedKeyWithReg);

			//integrity and authenticity:
			//recomputing hash of deciphered argument blindedMessageSigned
			//deciphering received digital signature with registration Public Key
			//verifying that both hashes are equal
			byte[] computedHash = CriptoUtils.computeDigest(blindedSignedToken);
			byte[] receivedHash = CriptoUtils.decipherWithPublicKey(CriptoUtils.base64decode(response.getDigitalSignature()), this.regPubKey);
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: " + this.getClass().getName());
			}
			return blindedSignedToken;
		
		}catch(RegistrationFault_Exception e) {
			// remote domain exception
			RegistrationException ex = ExceptionParser.parse(e.getFaultInfo()
					.getFaultType(), e.getMessage());
			throw ex;
		}catch (ServiceError_Exception e) {
			// remote service error
			throw new RemoteException(e);
		}
		catch (StubFactoryException e) {
			throw new RemoteException(e);
		}
		catch (WebServiceException e) {
			throw new RemoteException(e);
		}
	}
}
