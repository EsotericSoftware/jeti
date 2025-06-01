
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiException.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.esotericsoftware.jeti.JetiCore.DeviceInfo;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiSDK {
	static public String[] libraries = {"jeti_core64.dll", "jeti_radio64.dll", "jeti_spectro64.dll", "jeti_radio_ex64.dll",
		"jeti_spectro_ex64.dll"};

	static final int SUCCESS = 0;
	static final int STRING_BUFFER_SIZE = 256;
	static final int SPECTRAL_DATA_SIZE = 2048;

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

	static public boolean isInitialized () {
		return initialized;
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

	static public JetiResult<Boolean> setLicenseKey (String licenseKey) {
		ensureInitialized();
		return JetiCore.setLicenseKey(licenseKey);
	}

	static public JetiResult<Boolean> importStraylightMatrix (String matrixFile) {
		ensureInitialized();
		return JetiCore.importStraylightMatrix(matrixFile);
	}

	static public JetiResult<Boolean> ignoreStraylightMatrix (boolean ignore) {
		ensureInitialized();
		return JetiCore.ignoreStraylightMatrix(ignore);
	}

	static public JetiResult<DeviceInfo[]> discoverDevices () {
		ensureInitialized();
		return JetiCore.getAllDevices();
	}

	static public JetiResult<JetiCore> openCoreDevice (int deviceNumber) {
		ensureInitialized();
		return JetiCore.openDevice(deviceNumber);
	}

	static public JetiResult<JetiCore> openCoreDeviceBySerial (String deviceSerial) {
		ensureInitialized();
		JetiResult<DeviceInfo[]> devicesResult = discoverDevices();
		if (devicesResult.isError()) return JetiResult.error(devicesResult.getErrorCode());

		DeviceInfo[] devices = devicesResult.getValue();
		for (int i = 0, n = devices.length; i < n; i++) {
			DeviceInfo device = devices[i];
			if (deviceSerial.equals(device.deviceSerial())) return JetiCore.openDevice(i);
		}
		return JetiResult.error(invalidDeviceNumber);
	}

	static public JetiResult<JetiCore> openComDevice (int comPort, int baudrate) {
		ensureInitialized();
		return JetiCore.openComDevice(comPort, baudrate);
	}

	static public JetiResult<JetiCore> openTcpDevice (String ipAddress) {
		ensureInitialized();
		return JetiCore.openTcpDevice(ipAddress);
	}

	static public JetiResult<JetiCore> openUsbDevice (String usbSerial) {
		ensureInitialized();
		return JetiCore.openUsbDevice(usbSerial);
	}

	static public JetiResult<JetiCore> openBluetoothDevice (long btAddress) {
		ensureInitialized();
		return JetiCore.openBluetoothDevice(btAddress);
	}

	static public JetiResult<JetiCore> openBluetoothLeDevice (String devicePath) {
		ensureInitialized();
		return JetiCore.openBluetoothLeDevice(devicePath);
	}

	static public JetiResult<JetiRadio> openRadioDevice (int deviceNumber) {
		ensureInitialized();
		return JetiRadio.openRadioDevice(deviceNumber);
	}

	static public JetiResult<JetiSpectro> openSpectroDevice (int deviceNumber) {
		ensureInitialized();
		return JetiSpectro.openSpectroDevice(deviceNumber);
	}

	static public JetiResult<JetiRadioEx> openRadioExDevice (int deviceNumber) {
		ensureInitialized();
		return JetiRadioEx.openRadioExDevice(deviceNumber);
	}

	static public JetiResult<JetiSpectroEx> openSpectroExDevice (int deviceNumber) {
		ensureInitialized();
		return JetiSpectroEx.openSpectroExDevice(deviceNumber);
	}

	static public JetiResult<SDKVersion> getSDKVersion () {
		ensureInitialized();

		JetiResult<String> radioVersionResult = JetiRadio.getRadioDllVersion();
		JetiResult<String> spectroVersionResult = JetiSpectro.getSpectroDllVersion();
		JetiResult<String> radioExVersionResult = JetiRadioEx.getRadioExDllVersion();
		JetiResult<String> spectroExVersionResult = JetiSpectroEx.getSpectroExDllVersion();

		String coreVersion = "Unknown";
		String radioVersion = radioVersionResult.isSuccess() ? radioVersionResult.getValue() : "Unknown";
		String spectroVersion = spectroVersionResult.isSuccess() ? spectroVersionResult.getValue() : "Unknown";
		String radioExVersion = radioExVersionResult.isSuccess() ? radioExVersionResult.getValue() : "Unknown";
		String spectroExVersion = spectroExVersionResult.isSuccess() ? spectroExVersionResult.getValue() : "Unknown";

		return JetiResult.success(new SDKVersion(coreVersion, radioVersion, spectroVersion, radioExVersion, spectroExVersion));
	}

	static public JetiResult<Integer> getNumberOfCoreDevices () {
		ensureInitialized();
		return JetiCore.getNumberOfDevices();
	}

	static public JetiResult<Integer> getNumberOfRadioDevices () {
		ensureInitialized();
		return JetiRadio.getRadioDeviceCount();
	}

	static public JetiResult<Integer> getNumberOfSpectroDevices () {
		ensureInitialized();
		return JetiSpectro.getSpectroDeviceCount();
	}

	static public JetiResult<Integer> getNumberOfRadioExDevices () {
		ensureInitialized();
		return JetiRadioEx.getRadioExDeviceCount();
	}

	static public JetiResult<Integer> getNumberOfSpectroExDevices () {
		ensureInitialized();
		return JetiSpectroEx.getSpectroExDeviceCount();
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

	static private void ensureInitialized () {
		if (!initialized) initialize();
	}

	static String string (byte[] bytes) {
		if (bytes == null) return "";
		int length = 0;
		while (length < bytes.length && bytes[length] != 0)
			length++;
		if (length == 0) return "";
		return new String(bytes, 0, length, StandardCharsets.UTF_8).trim();
	}

	public record SDKVersion (
		String coreVersion,
		String radioVersion,
		String spectroVersion,
		String radioExVersion,
		String spectroExVersion) {}
}
