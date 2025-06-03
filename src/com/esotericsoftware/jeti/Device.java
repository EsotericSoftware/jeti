
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import java.util.Objects;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;

/** @author Nathan Sweet <misc@n4te.com> */
abstract public class Device implements AutoCloseable {
	Pointer handle;

	final FloatByReference floatRef = new FloatByReference();
	final IntByReference intRef = new IntByReference();

	public Device (Pointer handle) {
		Objects.requireNonNull(handle);
		this.handle = handle;
	}

	void ensureOpen () {
		if (handle == null) throw new IllegalStateException("Device is closed.");
	}

	public void close () {
		if (handle != null) {
			try {
				int result = JetiSpectroExLibrary.INSTANCE.JETI_CloseSpectroEx(handle);
				if (result != SUCCESS) new RuntimeException("Unable to close device: 0x" + Integer.toHexString(result));
			} catch (Throwable ex) {
				Log.warn("Unable to close device.", ex);
			} finally {
				handle = null;
			}
		}
	}

	public boolean isClosed () {
		return handle == null;
	}
}
