package trustedcenter.ws.client.service;

import java.security.PublicKey;

import javax.xml.ws.WebServiceException;

import sirs.framework.criptography.CriptoUtils;
import sirs.framework.exception.ExceptionParser;
import sirs.framework.exception.RemoteException;
import sirs.framework.ws.StubFactoryException;
import trustedcenter.exceptions.TrustedCenterException;
import trustedcenter.ws.client.TrustedCenterStubFactory;
import trustedcenter.ws.ties.GetPublicKeyRequest;
import trustedcenter.ws.ties.GetPublicKeyResponse;
import trustedcenter.ws.ties.ServiceError_Exception;
import trustedcenter.ws.ties.TrustedCenterFault_Exception;
import trustedcenter.ws.ties.TrustedCenterPortType;

public class GetPublicKeyService {
	
	private String endpoint;
	
	public GetPublicKeyService(String endpoint){
		this.endpoint = endpoint;
	}
	
	public PublicKey execute() throws TrustedCenterException, RemoteException {
		try {
			TrustedCenterPortType port = TrustedCenterStubFactory.getInstance().getPort(this.endpoint);
			GetPublicKeyRequest request = new GetPublicKeyRequest();
			GetPublicKeyResponse response = port.getPublicKey(request);
			return CriptoUtils.recreatePublicKey(CriptoUtils.base64decode(response.getPublicKey()));
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
