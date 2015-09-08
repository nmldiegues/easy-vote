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
import registration.ws.ties.ExchangeSecretRequestType;
import registration.ws.ties.ExchangeSecretResponseType;
import registration.ws.ties.RegistrationFault_Exception;
import registration.ws.ties.RegistrationPortType;
import registration.ws.ties.ServiceError_Exception;
import sirs.framework.criptography.CriptoUtils;
import sirs.framework.exception.ExceptionParser;
import sirs.framework.exception.RemoteException;
import sirs.framework.ws.StubFactoryException;

public class ExchangeSecretService {

	private String endpoint;
	private PublicKey regPubKey;
	private PrivateKey voterPrivKey;
	private BigInteger serialNumber;
	private Long voterId;
	private String credentials;
	
	public ExchangeSecretService(String endpoint, PublicKey regPubKey, PrivateKey voterPrivKey, BigInteger serialNumber, Long voterId, String credentials){
		this.endpoint = endpoint;
		this.regPubKey = regPubKey;
		this.voterPrivKey = voterPrivKey;
		this.serialNumber = serialNumber;
		this.voterId = voterId;
		this.credentials = credentials;
	}
	
	public SecretKey execute() throws RegistrationException, RemoteException{
		try {
			RegistrationPortType port = RegistrationStubFactory.getInstance().getPort(this.endpoint);
			ExchangeSecretRequestType request = new ExchangeSecretRequestType();
			
			//integrity and authenticity:
			//computing hash of unciphered arguments serialNumber, voterId and credentials
			//ciphering it with voter private key. encoding result for sending
			request.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
					CriptoUtils.computeDigest(this.serialNumber.toByteArray(), this.voterId.toString().getBytes(),
							this.credentials.getBytes()), this.voterPrivKey)));
			
			//confidenciality:
			//ciphering arguments serialNumber, voterId and credentials with registration Public Key
			//encoding result for sending
			request.setCertificateSerialNumber(CriptoUtils.base64encode(CriptoUtils.cipherWithPublicKey(
					this.serialNumber.toByteArray(), this.regPubKey)));
			request.setVoterId(CriptoUtils.base64encode(CriptoUtils.cipherWithPublicKey(
					this.voterId.toString().getBytes(), this.regPubKey)));
			request.setCredentials(CriptoUtils.base64encode(CriptoUtils.cipherWithPublicKey(
					this.credentials.getBytes(), this.regPubKey)));
			
			ExchangeSecretResponseType response = port.exchangeSecret(request);
			
			//confidenciality:
			//deciphering argument sharedKey with voter private key
			SecretKey key = CriptoUtils.recreateAESKey(CriptoUtils.decipherWithPrivateKey(CriptoUtils.base64decode(response.getSharedKey()), this.voterPrivKey));
			
			//integrity and authenticity:
			//recomputing hash of deciphered argument sharedKey
			//deciphering received digital signature with registration Public Key
			//verifying that both hashes are equal
			byte[] computedHash = CriptoUtils.computeDigest(key.getEncoded());
			byte[] receivedHash = CriptoUtils.decipherWithPublicKey(CriptoUtils.base64decode(response.getDigitalSignature()), this.regPubKey);
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: " + this.getClass().getName());
			}
			return key;
			
		} catch(RegistrationFault_Exception e) {
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
