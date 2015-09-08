package ballotserver.ws.client.service;

import javax.crypto.SecretKey;
import javax.xml.ws.WebServiceException;

import org.bouncycastle.util.Arrays;

import sirs.framework.criptography.CriptoUtils;
import sirs.framework.exception.ExceptionParser;
import sirs.framework.exception.RemoteException;
import sirs.framework.ws.StubFactoryException;

import ballotserver.exceptions.BallotServerException;
import ballotserver.exceptions.InvalidMACException;
import ballotserver.ws.client.BallotServerStubFactory;
import ballotserver.ws.ties.BallotServerFault_Exception;
import ballotserver.ws.ties.BallotServerPortType;
import ballotserver.ws.ties.ServiceError_Exception;
import ballotserver.ws.ties.VerifyVoteRequestType;
import ballotserver.ws.ties.VerifyVoteResponseType;

public class VerifyVoteService {
	
	private String endpoint;
	private SecretKey specialKey;
	private byte[] regSignedToken;
	private byte[] regToken;
	private byte[] tcSignedToken;
	private byte[] tcToken;
	private byte[] safeOffset;
	

	public VerifyVoteService(String endpoint, SecretKey specialKey,
			byte[] regSignedToken, byte[] regToken, byte[] tcSignedToken,
			byte[] tcToken, byte[] safeOffset) {
		this.endpoint = endpoint;
		this.specialKey = specialKey;
		this.regSignedToken = regSignedToken;
		this.regToken = regToken;
		this.tcSignedToken = tcSignedToken;
		this.tcToken = tcToken;
		this.safeOffset = safeOffset;
	}
	
	public Integer execute() throws BallotServerException, RemoteException{
		try {
			BallotServerPortType port = BallotServerStubFactory.getInstance().getPort(this.endpoint);
			VerifyVoteRequestType request = new VerifyVoteRequestType();
			
			request.setMac(CriptoUtils.base64encode(CriptoUtils.computeDigest(
					this.regSignedToken, this.regToken, this.tcSignedToken, this.tcToken,
					this.safeOffset, this.specialKey.getEncoded())));
			
			
			
			request.setRegSignedToken(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(this.regSignedToken, this.specialKey)));
			request.setRegToken(CriptoUtils.base64encode(this.regToken));
			request.setTcSignedToken(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(this.tcSignedToken, this.specialKey)));
			request.setTcToken(CriptoUtils.base64encode(this.tcToken));
			request.setSafeOffset(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(this.safeOffset, this.specialKey)));
			
			VerifyVoteResponseType response = port.verifyVote(request);
			
			Integer squareChosen = new Integer(new String(CriptoUtils.decipherWithSymKey(
					CriptoUtils.base64decode(response.getChosenSquare()), this.specialKey)));
			
			byte[] computedHash = CriptoUtils.computeDigest(
					squareChosen.toString().getBytes(), this.specialKey.getEncoded());
			byte[] receivedHash = CriptoUtils.base64decode(response.getMac());
			if(!Arrays.areEqual(computedHash, receivedHash)){
				throw new InvalidMACException("Invalid MAC in webservice client: " + this.getClass().getName());
			}
			
			return squareChosen;
		
		}catch (BallotServerFault_Exception e) {
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
