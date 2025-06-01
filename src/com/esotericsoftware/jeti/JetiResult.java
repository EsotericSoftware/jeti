
package com.esotericsoftware.jeti;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiResult<T> {
	private final T value;
	private final int errorCode;
	private final boolean success;

	private JetiResult (T value, int errorCode) {
		this.value = value;
		this.errorCode = errorCode;
		success = errorCode == 0;
	}

	public boolean isSuccess () {
		return success;
	}

	public boolean isError () {
		return !success;
	}

	public T getValue () {
		return value;
	}

	public T getValueOrNull () {
		return success ? value : null;
	}

	public T getValueOrThrow () throws JetiException {
		if (success) return value;
		throw new JetiException(errorCode);
	}

	public T getValueOrDefault (T defaultValue) {
		return success ? value : defaultValue;
	}

	public int getErrorCode () {
		return errorCode;
	}

	public JetiException getException () {
		return success ? null : new JetiException(errorCode);
	}

	public String toString () {
		if (success) return "JetiResult{success, value=" + value + "}";
		return "JetiResult{error, errorCode=0x" + Integer.toHexString(errorCode) + "}";
	}

	static public <T> JetiResult<T> success (T value) {
		return new JetiResult<>(value, 0);
	}

	static public <T> JetiResult<T> error (int errorCode) {
		return new JetiResult<>(null, errorCode);
	}

	static public <T> JetiResult<T> fromErrorCode (T value, int errorCode) {
		return new JetiResult<>(value, errorCode);
	}
}
