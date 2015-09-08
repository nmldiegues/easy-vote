package trustedcenter.ws.client.service;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.xml.ws.WebServiceException;

import org.bouncycastle.util.Arrays;

import sirs.framework.criptography.CriptoUtils;
import sirs.framework.exception.ExceptionParser;
import sirs.framework.exception.RemoteException;
import sirs.framework.ws.StubFactoryException;
import trustedcenter.exceptions.InvalidDigitalSignatureException;
import trustedcenter.exceptions.TrustedCenterException;
import trustedcenter.ws.client.TrustedCenterStubFactory;
import trustedcenter.ws.ties.BlindSignatureRequestType;
import trustedcenter.ws.ties.BlindSignatureResponseType;
import trustedcenter.ws.ties.ServiceError_Exception;
import trustedcenter.ws.ties.TrustedCenterFault_Exception;
import trustedcenter.ws.ties.TrustedCenterPortType;

public class BlindSignatureService {

	private String endpoint;
	private SecretKey sharedKeyWithTc;
	private PublicKey tcPubKey;
	private PrivateKey voterPrivKey;
	private byte[] blindedToken;
	private BigInteger certSerialNumber;
	private Long voterId;
	
	public BlindSignatureService(String endpoint, SecretKey sharedKeyWithTc, PublicKey tcPubKey, PrivateKey voterPrivKey, byte[] blindedToken, BigInteger certSerialNumber, Long voterId){
		this.endpoint = endpoint;
		this.sharedKeyWithTc = sharedKeyWithTc;
		this.tcPubKey = tcPubKey;
		this.voterPrivKey = voterPrivKey;
		this.blindedToken = blindedToken;
		this.certSerialNumber = certSerialNumber;
		this.voterId = voterId;
	}
	
	public byte[] execute() throws TrustedCenterException, RemoteException {
		try {
			TrustedCenterPortType port = TrustedCenterStubFactory.getInstance().getPort(this.endpoint);
			BlindSignatureRequestType param = new BlindSignatureRequestType();
			
			//integrity and authenticity:
			//computing hash of unciphered arguments blindedMessage, certificateSerialNumber and voterID
			//ciphering it with voter private key. encoding result for sending
			param.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
					CriptoUtils.computeDigest(this.blindedToken, this.certSerialNumber.toByteArray(),
							this.voterId.toString().getBytes()), this.voterPrivKey)));
			
			//confidenciality:
			//ciphering symetrically arguments blindedMessage and certSerialNumber
			//with shared key with trusted center
			//NOT ciphering voterId so that TC will be able to fetch the shared key based off that voterId
			//encoding result for sending
			param.setBlindedMessage(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(this.blindedToken, this.sharedKeyWithTc)));
			param.setCertificateSerialNumber(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(this.certSerialNumber.toByteArray(), this.sharedKeyWithTc)));
			param.setVoterId(this.voterId.toString());

			BlindSignatureResponseType response = port.blindSignature(param);
			
			//confidenciality:
			//deciphering argument blindedMessageSigned with shared key with trusted center
			byte[] blindedSignedToken = CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(response.getBlindedMessageSigned()), this.sharedKeyWithTc);
			
			//integrity and authenticity:
			//recomputing hash of deciphered argument blindedMessageSigned
			//deciphering received digital signature with trusted center Public Key
			//verifying that both hashes are equal
			byte[] computedHash = CriptoUtils.computeDigest(blindedSignedToken);
			byte[] receivedHash = CriptoUtils.decipherWithPublicKey(CriptoUtils.base64decode(response.getDigitalSignature()), this.tcPubKey);
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: " + this.getClass().getName());
			}
			
			return blindedSignedToken;
			
		} catch (TrustedCenterFault_Exception e) {
			// remote domain exception
			TrustedCenterException ex = ExceptionParser.parse(e.getFaultInfo()
					.getFaultType(), e.getMessage());
			throw ex;
		}
		catch (ServiceError_Exception e) {
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
