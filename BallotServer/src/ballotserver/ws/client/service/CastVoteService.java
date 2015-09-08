package ballotserver.ws.client.service;

import javax.crypto.SecretKey;
import javax.xml.ws.WebServiceException;

import sirs.framework.criptography.CriptoUtils;
import sirs.framework.exception.ExceptionParser;
import sirs.framework.exception.RemoteException;
import sirs.framework.ws.StubFactoryException;

import ballotserver.exceptions.BallotServerException;
import ballotserver.ws.client.BallotServerStubFactory;
import ballotserver.ws.ties.BallotServerFault_Exception;
import ballotserver.ws.ties.BallotServerPortType;
import ballotserver.ws.ties.CastVoteRequestType;
import ballotserver.ws.ties.ServiceError_Exception;

public class CastVoteService {

	private String endpoint;
	private SecretKey specialKey;
	private byte[] regSignedToken;
	private byte[] regToken;
	private byte[] tcSignedToken;
	private byte[] tcToken;
	private Integer chosenSquare;
	private Boolean auditing;
	
	public CastVoteService(String endpoint, SecretKey specialKey, byte[] regSignedToken, 
			byte[] regToken, byte[] tcSignedToken, byte[] tcToken, Integer chosenSquare, Boolean auditing){
		this.endpoint = endpoint;
		this.specialKey = specialKey;
		this.regSignedToken = regSignedToken;
		this.regToken = regToken;
		this.tcSignedToken = tcSignedToken;
		this.tcToken = tcToken;
		this.chosenSquare = chosenSquare;
		this.auditing = auditing;
	}
	
	public void execute() throws BallotServerException, RemoteException{
		try{
			BallotServerPortType port = BallotServerStubFactory.getInstance().getPort(this.endpoint);
			CastVoteRequestType request = new CastVoteRequestType();
			
			//integrity and authenticity:
			//using a MAC with the shared key and the SIRSFramework hashing function
			//that means hashing the message (parameters) along with the shared secret
			//encoding result for sending
			request.setMac(CriptoUtils.base64encode(CriptoUtils.computeDigest(
					this.regSignedToken, this.regToken, this.tcSignedToken, 
					this.tcToken, this.chosenSquare.toString().getBytes(),
					this.auditing.toString().getBytes(), this.specialKey.getEncoded())));
			
			//confidenciality:
			//ciphering regSignedToken, tcSignedToken and the chosenSquare with shared symmetrical key
			//regToken and tcToken will be sent plain so that the BallotServer can
			//fetch the previously traded shared secret key
			//encoding result for sending	
			
			request.setRegSignedToken(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(this.regSignedToken, this.specialKey)));
			request.setRegToken(CriptoUtils.base64encode(this.regToken));
			request.setTcSignedToken(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(this.tcSignedToken, this.specialKey)));
			request.setTcToken(CriptoUtils.base64encode(this.tcToken));
			request.setChosenSquare(CriptoUtils.base64encode(
					CriptoUtils.cipherWithSymKey(this.chosenSquare.toString().getBytes(), this.specialKey)));
			request.setAuditing(CriptoUtils.base64encode(CriptoUtils.cipherWithSymKey(
					this.auditing.toString().getBytes(), this.specialKey)));
			
			port.castVote(request);
			
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
