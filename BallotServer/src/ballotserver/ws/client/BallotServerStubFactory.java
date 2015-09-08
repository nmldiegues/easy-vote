package ballotserver.ws.client;

import ballotserver.ws.ties.BallotServerPortType;
import ballotserver.ws.ties.BallotServerService;
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
public class BallotServerStubFactory extends
		StubFactory<BallotServerService, BallotServerPortType> {

	//
	// Singleton
	//

	/** Single instance created upon class loading. */
	private static final BallotServerStubFactory fINSTANCE = new BallotServerStubFactory();

	/** Return singleton instance. */
	public static synchronized BallotServerStubFactory getInstance() {
		return fINSTANCE;
	}

	/** Private constructor prevents construction outside this class. */
	private BallotServerStubFactory() {
	}

	//
	// base factory methods
	//
	public BallotServerService getService() {
		return new BallotServerService();
	}

	public BallotServerPortType getPort() {
		return this.getService().getBallotServerPort();
	}

	//
	// config-based factory methods
	//
	public final static String ENDPOINT_ADDRESS_CONFIG_PARAMETER_NAME = "registration.ws.EndpointAddress";

	public BallotServerPortType getPortUsingConfig() throws StubFactoryException {
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
