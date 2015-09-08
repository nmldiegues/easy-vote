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
import trustedcenter.ws.ties.ExchangeSecretRequestType;
import trustedcenter.ws.ties.ExchangeSecretResponseType;
import trustedcenter.ws.ties.ServiceError_Exception;
import trustedcenter.ws.ties.TrustedCenterFault_Exception;
import trustedcenter.ws.ties.TrustedCenterPortType;

public class ExchangeSecretService {

	private String endpoint;
	private PublicKey regPubKey;
	private PrivateKey voterPrivKey;
	private BigInteger serialNumber;
	private Long voterId;
	
	public ExchangeSecretService(String endpoint, PublicKey regPubKey, PrivateKey voterPrivKey, BigInteger serialNumber, Long voterId){
		this.endpoint = endpoint;
		this.regPubKey = regPubKey;
		this.voterPrivKey = voterPrivKey;
		this.serialNumber = serialNumber;
		this.voterId = voterId;
	}
	
	public SecretKey execute() throws TrustedCenterException, RemoteException{
		try {
			TrustedCenterPortType port = TrustedCenterStubFactory.getInstance().getPort(this.endpoint);
			ExchangeSecretRequestType request = new ExchangeSecretRequestType();
			
			//integrity and authenticity:
			//computing hash of unciphered arguments serialNumber, voterId and credentials
			//ciphering it with voter private key. encoding result for sending
			request.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
					CriptoUtils.computeDigest(this.serialNumber.toByteArray(), this.voterId.toString().getBytes()),
					this.voterPrivKey)));
			
			//confidenciality:
			//ciphering arguments serialNumber and voterId with TrustedCenter Public Key
			//encoding result for sending
			request.setCertificateSerialNumber(CriptoUtils.base64encode(CriptoUtils.cipherWithPublicKey(
					this.serialNumber.toByteArray(), this.regPubKey)));
			request.setVoterId(CriptoUtils.base64encode(CriptoUtils.cipherWithPublicKey(
					this.voterId.toString().getBytes(), this.regPubKey)));
			
			ExchangeSecretResponseType response = port.exchangeSecret(request);
			
			//confidenciality:
			//deciphering argument sharedKey with voter private key
			SecretKey key = CriptoUtils.recreateAESKey(CriptoUtils.decipherWithPrivateKey(CriptoUtils.base64decode(response.getSharedKey()), this.voterPrivKey));
			
			//integrity and authenticity:
			//recomputing hash of deciphered argument sharedKey
			//deciphering received digital signature with TrustedCenter Public Key
			//verifying that both hashes are equal
			byte[] computedHash = CriptoUtils.computeDigest(key.getEncoded());
			byte[] receivedHash = CriptoUtils.decipherWithPublicKey(CriptoUtils.base64decode(response.getDigitalSignature()), this.regPubKey);
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: " + this.getClass().getName());
			}
			return key;
			
		} catch(TrustedCenterFault_Exception e) {
			// remote domain exception
			TrustedCenterException ex = ExceptionParser.parse(e.getFaultInfo()
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
