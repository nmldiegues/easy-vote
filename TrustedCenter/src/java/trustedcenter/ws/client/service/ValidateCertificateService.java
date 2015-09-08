package trustedcenter.ws.client.service;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;
import javax.xml.ws.WebServiceException;

import org.bouncycastle.util.Arrays;


import sirs.framework.criptography.CriptoUtils;
import sirs.framework.exception.ExceptionParser;
import sirs.framework.exception.RemoteException;
import sirs.framework.ws.StubFactoryException;
import trustedcenter.exceptions.InvalidCertificateException;
import trustedcenter.exceptions.InvalidDigitalSignatureException;
import trustedcenter.exceptions.TCCriptographicException;
import trustedcenter.exceptions.TrustedCenterException;
import trustedcenter.ws.client.TrustedCenterStubFactory;
import trustedcenter.ws.ties.ServiceError_Exception;
import trustedcenter.ws.ties.TrustedCenterFault_Exception;
import trustedcenter.ws.ties.TrustedCenterPortType;
import trustedcenter.ws.ties.ValidateCertificateRequest;
import trustedcenter.ws.ties.ValidateCertificateResponse;

public class ValidateCertificateService {

	/* service members */
	private String endpoint;
	private PublicKey tcPubKey;
	private PrivateKey requesterPrivKey;
	private BigInteger requesterSerialNumber;
	private String distinguishedName;
	private BigInteger serialNumber;
	
	
	/* constructor */
	public ValidateCertificateService(String endpoint, PublicKey tcPubKey, PrivateKey requesterPrivKey, BigInteger requesterSerialNumber, BigInteger serialNumber, String distinguishedName) {
		this.serialNumber = serialNumber;
		this.tcPubKey = tcPubKey;
		this.requesterPrivKey = requesterPrivKey;
		this.requesterSerialNumber = requesterSerialNumber;
		this.endpoint = endpoint;
		this.distinguishedName = distinguishedName;
	}
	
	public PublicKey execute() throws TrustedCenterException, RemoteException{
		try {
			TrustedCenterPortType port = TrustedCenterStubFactory.getInstance().getPort(this.endpoint);
			ValidateCertificateRequest request = new ValidateCertificateRequest();
			
			//integrity and authenticity:
			//computing hash of arguments serialNumber, distinguishedName and requesterSerialNumber
			//ciphering it with requester private key. encoding result for sending
			request.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
					CriptoUtils.computeDigest(this.serialNumber.toByteArray(), this.distinguishedName.getBytes(), 
							this.requesterSerialNumber.toByteArray()), this.requesterPrivKey)));
			
			//confidenciality:
			//no need to cipher, it's a simple validation request for a PUBLIC key
			request.setSerialNumber(serialNumber.toString());
			request.setDistinguishedName(distinguishedName);
			request.setRequesterSerialNumber(requesterSerialNumber.toString());
			
			ValidateCertificateResponse response = port.validateCertificate(request);
			
			//confidenciality:
			//no need to decipher, it's public information			
			String obtainedPubKey = response.getPublicKey();
			String isValid = response.getValid();
			
			//integrity and authenticity:
			//recomputing hash of deciphered argument blindedMessageSigned
			//deciphering received digital signature with registration Public Key
			//verifying that both hashes are equal
			byte[] computedHash = CriptoUtils.computeDigest(isValid.getBytes(), obtainedPubKey.getBytes());
			byte[] receivedHash = CriptoUtils.decipherWithPublicKey(CriptoUtils.base64decode(response.getDigitalSignature()), this.tcPubKey);
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: " + this.getClass().getName());
			}
			
			if(isValid.equals("valid")){
				PublicKey pubKey = CriptoUtils.recreatePublicKey(CriptoUtils.base64decode(obtainedPubKey));
				return pubKey;
			}
			else{
				throw new InvalidCertificateException(this.distinguishedName, this.serialNumber);
			}
			
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
