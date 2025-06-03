
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import java.util.Objects;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;

/** @author Nathan Sweet <misc@n4te.com> */
abstract public class Device<L extends Library> implements AutoCloseable {
	private final L library;
	Pointer handle;

	final FloatByReference floatRef = new FloatByReference();
	final IntByReference intRef = new IntByReference();

	public Device (L library, Pointer handle) {
		Objects.requireNonNull(library);
		Objects.requireNonNull(handle);
		this.library = library;
		this.handle = handle;
	}

	L lib () {
		if (handle == null) throw new IllegalStateException("Device is closed.");
		return library;
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
