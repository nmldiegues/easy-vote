package sirs.framework.exception;

import java.lang.reflect.Constructor;

public class ExceptionParser {

    @SuppressWarnings("unchecked")
	public static <T> T parse(String faultType, String faultMessage) {
	Class<? extends T> domainExceptionClass = null;
	// get the exception class
	try {
	    domainExceptionClass = (Class<? extends T>) Class.forName(faultType);
	} catch (Exception e) { // either ClassNotFoundException or ClasCastException
	    throw new RuntimeException("Could not find class for exception: '" + faultType + "'");
	}

	T domainException = null;
	// instantiate the exception.  If there is a message, use the appropriate constructor
	try {
	    domainException = instantiateException(domainExceptionClass, faultMessage);
	} catch (Exception e) {
	    throw new RuntimeException("Could not instantiate exception: '" + domainExceptionClass + "'");
	}
	return domainException;
    }


    private static <T> T instantiateException(Class<? extends T> domainExceptionClass)
	throws InstantiationException, IllegalAccessException, NoSuchMethodException {
	return domainExceptionClass.newInstance();
    }

    private static <T> T instantiateException(Class<? extends T> domainExceptionClass, String exceptionMessage)
	throws InstantiationException, IllegalAccessException, NoSuchMethodException {
	try {
	    Constructor<? extends T> c = domainExceptionClass.getConstructor(String.class);
	    return c.newInstance(exceptionMessage);
	} catch (Exception e) {
	    // try the no args constructor
	    return instantiateException(domainExceptionClass);
	}
    }

}