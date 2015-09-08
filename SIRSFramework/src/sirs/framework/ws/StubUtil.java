package sirs.framework.ws;

import javax.xml.ws.BindingProvider;

/**
 * This class contains Web Service client stub-related utility methods.
 */
public class StubUtil {

	/** Set endpoint address of port to specified URL */
	public static void setPortEndpointAddress(Object port, String url) {
		BindingProvider bindingProvider = (BindingProvider) port;
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
	}

	/** Get endpoint address of port */
	public static String getPortEndpointAddress(Object port) {
		BindingProvider bindingProvider = (BindingProvider) port;
		return (String) bindingProvider.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
	}

} 