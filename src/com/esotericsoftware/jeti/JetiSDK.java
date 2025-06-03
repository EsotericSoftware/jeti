
package com.esotericsoftware.jeti;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiSDK {
	static public String[] libraries = { //
		"jeti_core64.dll", //
		"jeti_radio64.dll", //
		"jeti_spectro64.dll", //
		"jeti_radio_ex64.dll", //
		"jeti_spectro_ex64.dll"};

	static public final int SUCCESS = 0;
	static public final int INVALID_ARGUMENT = 0x0000000D;
	static public final int INVALID_DEVICE_NUMBER = 0x00000013;

	static final int STRING_SIZE = 16;
	static final int SPECTRUM_SIZE = 81;

	static private volatile boolean initialized;

	private JetiSDK () {
	}

	static public void initialize () {
		if (initialized) return;
		synchronized (JetiSDK.class) {
			if (initialized) return;
			Path dir = null;
			try {
				dir = Paths.get(System.getProperty("java.io.tmpdir"), "jeti");
				Files.createDirectory(dir);
				extractLibraries(dir);
			} catch (Throwable ignored) {
				deleteLibraries(dir);
				try {
					dir = Files.createTempDirectory("jeti-");
					extractLibraries(dir);
				} catch (Throwable ex) {
					deleteLibraries(dir);
					throw new RuntimeException("Unable to initialize the JETI SDK.", ex);
				}
			}
			initialized = true;
			Log.debug("JETI SDK initialized.");
		}
	}

	static public void initialize (Path dir) {
		if (initialized) return;
		synchronized (JetiSDK.class) {
			if (initialized) return;
			try {
				extractLibraries(dir);
			} catch (Throwable ex) {
				deleteLibraries(dir);
				throw new RuntimeException("Unable to initialize the JETI SDK.", ex);
			}
			initialized = true;
			Log.debug("JETI SDK initialized.");
		}
	}

	static private void extractLibraries (Path dir) throws IOException {
		for (String library : libraries) {
			try (InputStream input = JetiSDK.class.getResourceAsStream("/" + library)) {
				if (input != null) {
					Path path = dir.resolve(library);
					Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING);
					path.toFile().deleteOnExit();
					Log.debug("Extracted native library: " + path);
				} else
					Log.warn("Native library not found: " + library);
			}
		}

		System.setProperty("jna.library.path", dir.toString());
		Log.debug("Native library path: " + dir);
	}

	static private void deleteLibraries (Path dir) {
		for (String library : libraries)
			delete(dir.resolve(library));
		delete(dir);
	}

	static private void delete (Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException ex) {
		}
	}

	static String string (byte[] bytes) {
		if (bytes == null) return "";
		int length = 0;
		while (length < bytes.length && bytes[length] != 0)
			length++;
		if (length == 0) return "";
		return new String(bytes, 0, length, StandardCharsets.UTF_8).trim();
	}

	static public String getErrorMessage (int errorCode) {
		return switch (errorCode) {
		case SUCCESS -> "No error occurred";
		case 0x00000001 -> "Device already open";
		case 0x00000002 -> "Could not open COM-port";
		case 0x00000003 -> "Could not set COM-port settings";
		case 0x00000004 -> "Could not set buffer size of COM-port";
		case 0x00000005 -> "Could not purge buffers of COM-port";
		case 0x00000006 -> "Could not set COM-port timeout";
		case 0x00000007 -> "Could not send to device";
		case 0x00000008 -> "Timeout error";
		case 0x00000009 -> "Break";
		case 0x0000000A -> "Could not receive from device";
		case 0x0000000B -> "Command not supported or invalid argument";
		case 0x0000000C -> "Could not convert received data";
		case INVALID_ARGUMENT -> "Invalid argument";
		case 0x0000000E -> "Device busy";
		case 0x00000011 -> "Invalid checksum of received data";
		case 0x00000012 -> "Invalid stepwidth";
		case INVALID_DEVICE_NUMBER -> "Invalid device number";
		case 0x00000014 -> "Device not connected";
		case 0x00000015 -> "Invalid device handle";
		case 0x00000016 -> "Invalid calibration file number";
		case 0x00000017 -> "Calibration data not read";
		case 0x00000020 -> "Overexposure";
		case 0x00000022 -> "Measurement failed";
		case 0x00000023 -> "Adaption failed";
		case 0x00000050 -> "Straylight file not found";
		case 0x00000051 -> "Could not find or create straylight directory";
		case 0x00000052 -> "No straylight file";
		case 0x00000053 -> "Not enough memory for straylight matrix";
		case 0x00000054 -> "Could not read serial number";
		case 0x00000080 -> "Internal DLL error";
		case 0x00000081 -> "Calculation error";
		case 0x00000100 -> "Could not read from COM port";
		case 0x000000FF -> "Fatal communication error";
		default -> "Unknown error code: 0x" + Integer.toHexString(errorCode);
		};
	}

	static public record DeviceSerials (String electronics, String spectrometer, String device) {}

	static public record DllVersion (short major, short minor, short build) {}
}
