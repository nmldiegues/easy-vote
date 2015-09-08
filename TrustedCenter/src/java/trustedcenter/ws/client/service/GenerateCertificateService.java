package trustedcenter.ws.client.service;

import java.math.BigInteger;
import java.security.PublicKey;

import javax.xml.ws.WebServiceException;

import sirs.framework.criptography.CriptoUtils;
import sirs.framework.exception.ExceptionParser;
import sirs.framework.exception.RemoteException;
import sirs.framework.ws.StubFactoryException;
import trustedcenter.exceptions.TrustedCenterException;
import trustedcenter.ws.client.TrustedCenterStubFactory;
import trustedcenter.ws.ties.GenerateCertificateRequest;
import trustedcenter.ws.ties.GenerateCertificateResponse;
import trustedcenter.ws.ties.ServiceError_Exception;
import trustedcenter.ws.ties.TrustedCenterFault_Exception;
import trustedcenter.ws.ties.TrustedCenterPortType;



public class GenerateCertificateService {

	/* service members */
	private String distinguishedName;
	private PublicKey publicKey;
	private String endpoint;

	/* constructor */
	public GenerateCertificateService(String endpoint, String distinguishedName, PublicKey publicKey) {
		this.distinguishedName = distinguishedName;
		this.publicKey = publicKey;
		this.endpoint = endpoint;
	}

	public BigInteger execute() throws TrustedCenterException, RemoteException {
		try {
			TrustedCenterPortType port = TrustedCenterStubFactory.getInstance().getPort(this.endpoint);
			GenerateCertificateRequest request = new GenerateCertificateRequest();
			request.setDistinguishedName(this.distinguishedName);
			request.setPublicKey(CriptoUtils.base64encode(this.publicKey.getEncoded()));
			GenerateCertificateResponse response = port.generateCertificate(request);
			return new BigInteger(response.getSerialNumber());
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
