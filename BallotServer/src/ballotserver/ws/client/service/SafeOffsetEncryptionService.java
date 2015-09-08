package ballotserver.ws.client.service;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.xml.ws.WebServiceException;

import org.bouncycastle.util.Arrays;

import sirs.framework.criptography.CriptoUtils;
import sirs.framework.exception.ExceptionParser;
import sirs.framework.exception.RemoteException;
import sirs.framework.ws.StubFactoryException;
import ballotserver.exceptions.BallotServerException;
import ballotserver.exceptions.InvalidDigitalSignatureException;
import ballotserver.ws.client.BallotServerStubFactory;
import ballotserver.ws.ties.BallotServerFault_Exception;
import ballotserver.ws.ties.BallotServerPortType;
import ballotserver.ws.ties.SafeOffsetEncryptionRequestType;
import ballotserver.ws.ties.SafeOffsetEncryptionResponseType;
import ballotserver.ws.ties.ServiceError_Exception;

public class SafeOffsetEncryptionService {

	private String endpoint;
	private PublicKey destinationPubKey;
	private PrivateKey orginPrivKey;
	private BigInteger selfCertNumber;
	private byte[] safeOffset;
	private String operation;

	public SafeOffsetEncryptionService(String endpoint, PublicKey destinationPubKey, PrivateKey originPrivKey, BigInteger selfCertNumber, byte[] safeOffset, String operation){
		this.endpoint = endpoint;
		this.destinationPubKey = destinationPubKey;
		this.orginPrivKey = originPrivKey;
		this.selfCertNumber = selfCertNumber;
		this.safeOffset = safeOffset;
		this.operation = operation;
	}

	public byte[] execute(){
		try{
			BallotServerPortType port = BallotServerStubFactory.getInstance().getPort(this.endpoint);
			SafeOffsetEncryptionRequestType request = new SafeOffsetEncryptionRequestType();
			
			//integrity and authenticity:
			//digital signature with self private key
			request.setDigitalSignature(CriptoUtils.base64encode(CriptoUtils.cipherWithPrivateKey(
							CriptoUtils.computeDigest(operation.getBytes(), safeOffset, 
									selfCertNumber.toByteArray()), orginPrivKey)));
			
			//confidenciality:
			//ciphering certificateNumber with destinationPubKey to avoid an attacker
			//from changing the contents, adjusting the digital signature with his own
			//key and sending his certificate number instead
			//encoding result for sending	
			request.setOperation(operation);
			request.setSafeOffset(CriptoUtils.base64encode(safeOffset));
			request.setCertNumber(CriptoUtils.base64encode(
					CriptoUtils.cipherWithPublicKey(selfCertNumber.toByteArray(), destinationPubKey)));
			
			SafeOffsetEncryptionResponseType response = port.safeOffsetEncryption(request);
			
			byte[] receivedSafeOffset = CriptoUtils.base64decode(response.getSafeOffset());
			
			//integrity and authenticity:
			//checking that digital signature is valid
			byte[] computedHash = CriptoUtils.computeDigest(receivedSafeOffset);
			byte[] receivedHash = CriptoUtils.decipherWithPublicKey(CriptoUtils.base64decode(response.getDigitalSignature()), destinationPubKey);
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidDigitalSignatureException("Invalid digital signature in webservice: " + this.getClass().getName());
			}
			
			return receivedSafeOffset;
		} catch (BallotServerFault_Exception e) {
			// remote domain exception
			BallotServerException ex = ExceptionParser.parse(e.getFaultInfo()
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
