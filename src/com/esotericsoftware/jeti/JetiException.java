
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiException extends RuntimeException {
	private final int errorCode;

	public JetiException (int errorCode) {
		super(getErrorMessage(errorCode));
		this.errorCode = errorCode;
	}

	public JetiException (int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public JetiException (int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode () {
		return errorCode;
	}
}
