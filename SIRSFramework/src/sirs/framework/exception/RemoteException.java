package sirs.framework.exception;

import java.io.Serializable;

public class RemoteException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1L;

    public RemoteException() { }

    public RemoteException(String arg0) {
        super(arg0); }

    public RemoteException(Throwable cause) {
        super(cause); }

    public RemoteException(String message, Throwable cause) {
        super(message, cause);
    }


}
