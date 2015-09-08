package registration.ws.client;

import registration.ws.ties.RegistrationPortType;
import registration.ws.ties.RegistrationService;
import sirs.framework.config.Config;
import sirs.framework.ws.StubFactory;
import sirs.framework.ws.StubFactoryException;

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
public class RegistrationStubFactory extends
		StubFactory<RegistrationService, RegistrationPortType> {

	//
	// Singleton
	//

	/** Single instance created upon class loading. */
	private static final RegistrationStubFactory fINSTANCE = new RegistrationStubFactory();

	/** Return singleton instance. */
	public static synchronized RegistrationStubFactory getInstance() {
		return fINSTANCE;
	}

	/** Private constructor prevents construction outside this class. */
	private RegistrationStubFactory() {
	}

	//
	// base factory methods
	//
	public RegistrationService getService() {
		return new RegistrationService();
	}

	public RegistrationPortType getPort() {
		return this.getService().getRegistrationPort();
	}

	//
	// config-based factory methods
	//
	public final static String ENDPOINT_ADDRESS_CONFIG_PARAMETER_NAME = "registration.ws.EndpointAddress";

	public RegistrationPortType getPortUsingConfig() throws StubFactoryException {
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
