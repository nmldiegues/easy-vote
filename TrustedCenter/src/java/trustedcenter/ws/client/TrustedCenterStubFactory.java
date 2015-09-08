package trustedcenter.ws.client;

import sirs.framework.config.Config;
import sirs.framework.ws.StubFactory;
import sirs.framework.ws.StubFactoryException;
import trustedcenter.ws.ties.TrustedCenterPortType;
import trustedcenter.ws.ties.TrustedCenterService;

/**
 * Registration stub factory
 *
 * Besides the base getService() and getPort() factory methods it also supports
 * getPortUsingConfig() that reads the endpoint address from a configuration
 * file. See ENDPOINT_ADDRESS_CONFIG_PARAMETER_NAME for property name.
 *
 * It could also be extended with a getPortUsingRegistry() method to look-up the
 * server location in a Web Services Registry.
 *
 */
public class TrustedCenterStubFactory extends
		StubFactory<TrustedCenterService, TrustedCenterPortType> {

	//
	// Singleton
	//

	/** Single instance created upon class loading. */
	private static final TrustedCenterStubFactory fINSTANCE = new TrustedCenterStubFactory();

	/** Return singleton instance. */
	public static synchronized TrustedCenterStubFactory getInstance() {
		return fINSTANCE;
	}

	/** Private constructor prevents construction outside this class. */
	private TrustedCenterStubFactory() {
	}

	//
	// base factory methods
	//
	public TrustedCenterService getService() {
		return new TrustedCenterService();
	}

	public TrustedCenterPortType getPort() {
		return this.getService().getTrustedCenterPort();
	}

	//
	// config-based factory methods
	//
	public final static String ENDPOINT_ADDRESS_CONFIG_PARAMETER_NAME = "registration.ws.EndpointAddress";

	public TrustedCenterPortType getPortUsingConfig() throws StubFactoryException {
		Config config = Config.getInstance();
		String endpointAddress = config
				.getInitParameter(ENDPOINT_ADDRESS_CONFIG_PARAMETER_NAME);

		if (endpointAddress != null)
			// set endpoint address to configuration parameter value
			return getPort(endpointAddress);
		else
			// return default port
			return getPort();
	}

}
