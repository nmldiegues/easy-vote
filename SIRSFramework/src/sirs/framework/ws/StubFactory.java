package sirs.framework.ws;

/**
 * This abstract class specifies the functionality of a
 * Web Service client stub factory.
 *
 * A stub is a class that represents the remote service in the local program.
 *
 * Each Web Service client should create a factory that extends this class and
 * specifies the parametric types S (service class) and PT (port type).
 * The subclass factory may also decide to become a singleton.
 */
public abstract class StubFactory<S,PT> {

    /** This method should return a default service implementation */
    public abstract S getService() throws StubFactoryException;

    /** This method should return a default port type implementation */
    public abstract PT getPort() throws StubFactoryException;

    /** This method returns a port type implementation configured to
        the endpoint URL string specified as argument */
    public PT getPort(String url) throws StubFactoryException {
        PT port = this.getPort();
        StubUtil.setPortEndpointAddress(port, url);
        return port;
    }

}
