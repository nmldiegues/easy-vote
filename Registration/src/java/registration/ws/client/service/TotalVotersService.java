package registration.ws.client.service;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.xml.ws.WebServiceException;

import org.bouncycastle.util.Arrays;

import registration.exceptions.InvalidDigitalSignatureException;
import registration.exceptions.RegistrationException;
import registration.ws.client.RegistrationStubFactory;
import registration.ws.ties.RegistrationFault_Exception;
import registration.ws.ties.RegistrationPortType;
import registration.ws.ties.ServiceError_Exception;
import registration.ws.ties.TotalVotersRequestType;
import registration.ws.ties.TotalVotersResponseType;
import sirs.framework.criptography.CriptoUtils;
import sirs.framework.exception.ExceptionParser;
import sirs.framework.exception.RemoteException;
import sirs.framework.ws.StubFactoryException;

public class TotalVotersService {

	private String endpoint;
	private PublicKey regPubKey;
	private PrivateKey requesterPrivKey;
	private BigInteger serialNumber;
	
	public TotalVotersService(String endpoint, PublicKey regPubKey, PrivateKey requesterPrivKey, BigInteger serialNumber){
		this.endpoint = endpoint;
		this.regPubKey = regPubKey;
		this.requesterPrivKey = requesterPrivKey;
		this.serialNumber = serialNumber;
	}

	public int execute() throws RegistrationException, RemoteException{
		try {
			RegistrationPortType port = RegistrationStubFactory.getInstance().getPort(this.endpoint);
			TotalVotersRequestType request = new TotalVotersRequestType();

			//integrity and authenticity:
			//computing hash of the serialNumber
			//ciphering it with voter private key. encoding result for sending
			request.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
					CriptoUtils.computeDigest(this.serialNumber.toByteArray()), this.requesterPrivKey)));
			
			//confidenciality:
			//ciphering argument serialNumber with registration Public Key
			//encoding result for sending
			request.setCertificateSerialNumber(CriptoUtils.base64encode(CriptoUtils.cipherWithPublicKey(
					this.serialNumber.toByteArray(), this.regPubKey)));
			
			TotalVotersResponseType response = port.totalVoters(request);
			
			//confidentiality:
			//number of voters is public, thus unciphered
			Integer numberVoters = response.getNumberVotersRegistered();
			
			//integrity and authenticity:
			//recomputing hash of deciphered argument sharedKey
			//deciphering received digital signature with registration Public Key
			//verifying that both hashes are equal
			byte[] computedHash = CriptoUtils.computeDigest(numberVoters.toString().getBytes());
			byte[] receivedHash = CriptoUtils.decipherWithPublicKey(CriptoUtils.base64decode(response.getDigitalSignature()), this.regPubKey);
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: " + this.getClass().getName());
			}
			
			return numberVoters;

		} catch(RegistrationFault_Exception e) {
			// remote domain exception
			RegistrationException ex = ExceptionParser.parse(e.getFaultInfo()
					.getFaultType(), e.getMessage());
			throw ex;
		}catch (ServiceError_Exception e) {
			// remote service error
			throw new RemoteException(e);
		}catch (StubFactoryException e) {
			throw new RemoteException(e);
		}catch (WebServiceException e) {
			throw new RemoteException(e);
		}
	}

}
