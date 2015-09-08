package ballotserver.ws.client.service;

import java.security.PublicKey;

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
import ballotserver.ws.ties.ExchangeSecretRequestType;
import ballotserver.ws.ties.ServiceError_Exception;

public class ExchangeSecretService {

	private String endpoint;
	private PublicKey bsPubKey;
	private SecretKey sharedKey;
	private byte[] regToken;
	private byte[] tcToken;
	
	public ExchangeSecretService(String endpoint, PublicKey bsPubKey, SecretKey sharedKey, byte[] regToken, byte[] tcToken){
		this.endpoint = endpoint;
		this.bsPubKey = bsPubKey;
		this.sharedKey = sharedKey;
		this.regToken = regToken;
		this.tcToken = tcToken;
	}
	
	public void execute() throws BallotServerException, RemoteException{
		try {
			BallotServerPortType port = BallotServerStubFactory.getInstance().getPort(this.endpoint);
			ExchangeSecretRequestType request = new ExchangeSecretRequestType();
			
			//confidenciality:
			//ciphering arguments regToken, tcToken and sharedKey with BallotServer Public Key
			//encoding result for sending
			request.setRegToken(CriptoUtils.base64encode(CriptoUtils.cipherWithPublicKey(
					this.regToken, this.bsPubKey)));
			request.setTcToken(CriptoUtils.base64encode(CriptoUtils.cipherWithPublicKey(
					this.tcToken, this.bsPubKey)));
			request.setSharedKey(CriptoUtils.base64encode(CriptoUtils.cipherWithPublicKey(
					this.sharedKey.getEncoded(), this.bsPubKey)));
			
			port.exchangeSecret(request);
			
		} catch(BallotServerFault_Exception e) {
			// remote domain exception
			BallotServerException ex = ExceptionParser.parse(e.getFaultInfo()
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
