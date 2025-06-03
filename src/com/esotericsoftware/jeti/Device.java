
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import java.util.Objects;
import java.util.function.Function;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
abstract public class Device<L extends Library> implements AutoCloseable {
	private final L library;
	private final Function<Pointer, Integer> close;
	Pointer handle;

	final ByteByReference[] b;
	final ShortByReference[] s;
	final IntByReference[] i;
	final FloatByReference[] f;
	final DoubleByReference[] d;
	final PointerByReference[] p;

	Device (L library, Pointer handle, Function<Pointer, Integer> close, int byteCount, int shortCount, int intCount,
		int floatCount, int doubleCount, int pointerCount) {

		Objects.requireNonNull(library);
		Objects.requireNonNull(handle);
		Objects.requireNonNull(close);
		this.library = library;
		this.handle = handle;
		this.close = close;

		b = new ByteByReference[shortCount];
		s = new ShortByReference[shortCount];
		i = new IntByReference[intCount];
		f = new FloatByReference[floatCount];
		d = new DoubleByReference[shortCount];
		p = new PointerByReference[shortCount];
	}

	L lib () {
		if (handle == null) throw new IllegalStateException("Device is closed.");
		return library;
	}

	public void close () {
		if (handle != null) {
			int result = close.apply(handle);
			if (result != SUCCESS) new RuntimeException("Unable to close device: 0x" + Integer.toHexString(result));
			handle = null;
		}
	}

	public boolean isClosed () {
		return handle == null;
	}
}
