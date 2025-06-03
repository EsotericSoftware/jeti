
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

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

		b = array(ByteByReference[]::new, ByteByReference::new, byteCount);
		s = array(ShortByReference[]::new, ShortByReference::new, shortCount);
		i = array(IntByReference[]::new, IntByReference::new, intCount);
		f = array(FloatByReference[]::new, FloatByReference::new, floatCount);
		d = array(DoubleByReference[]::new, DoubleByReference::new, doubleCount);
		p = array(PointerByReference[]::new, PointerByReference::new, pointerCount);
	}

	private <T> T[] array (IntFunction<T[]> arraySupplier, Supplier<T> entrySupplier, int count) {
		T[] array = arraySupplier.apply(count);
		for (int i = 0; i < count; i++)
			array[i] = entrySupplier.get();
		return array;
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
