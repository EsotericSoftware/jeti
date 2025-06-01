
package com.esotericsoftware.jeti;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.esotericsoftware.jeti.JetiCore.DeviceInfo;
import com.esotericsoftware.jeti.JetiRadio.RadiometricData;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiSDK {
	static final int SUCCESS = 0;
	static final int STRING_BUFFER_SIZE = 256;
	static final int SPECTRAL_DATA_SIZE = 2048;

	static private volatile boolean initialized;
	static private final Object initLock = new Object();

	private JetiSDK () {
	}

	static public void initialize () throws JetiException {
		if (!initialized) {
			synchronized (initLock) {
				if (!initialized) {
					try {
						loadNativeLibraries();
						initialized = true;
						Log.debug("JETI SDK initialized.");
					} catch (Throwable ex) {
						throw new JetiException(-1, "Unable to initialize JETI SDK.", ex);
					}
				}
			}
		}
	}

	static public boolean isInitialized () {
		return initialized;
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
		return JetiResult.error(0x00000013); // JETI_INVALID_NUMBER
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
		return JetiRadio.getNumberOfRadioDevices();
	}

	static public JetiResult<Integer> getNumberOfSpectroDevices () {
		ensureInitialized();
		return JetiSpectro.getNumberOfSpectroDevices();
	}

	static public JetiResult<Integer> getNumberOfRadioExDevices () {
		ensureInitialized();
		return JetiRadioEx.getNumberOfRadioExDevices();
	}

	static public JetiResult<Integer> getNumberOfSpectroExDevices () {
		ensureInitialized();
		return JetiSpectroEx.getNumberOfSpectroExDevices();
	}

	static public class QuickMeasurement {
		static public JetiResult<JetiRadio.RadiometricData> measureRadiometric (int deviceNumber) {
			JetiResult<JetiRadio> deviceResult = openRadioDevice(deviceNumber);
			if (deviceResult.isError()) return JetiResult.error(deviceResult.getErrorCode());

			try (JetiRadio radio = deviceResult.getValue()) {
				JetiResult<Boolean> measureResult = radio.measureWithAdaptation();
				if (measureResult.isError()) return JetiResult.error(measureResult.getErrorCode());

				// Wait for measurement to complete
				boolean measuring = true;
				int timeout = 0;
				while (measuring && timeout < 100) { // 10 second timeout
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return JetiResult.error(-1);
					}

					JetiResult<Boolean> statusResult = radio.getMeasurementStatus();
					if (statusResult.isSuccess()) measuring = statusResult.getValue();
					timeout++;
				}

				if (measuring) return JetiResult.error(0x00000008); // JETI_TIMEOUT

				return radio.getAllMeasurementData();
			}
		}

		static public JetiResult<JetiSpectro.SpectroscopicData> measureSpectroscopic (int deviceNumber, float integrationTime) {
			JetiResult<JetiSpectro> deviceResult = openSpectroDevice(deviceNumber);
			if (deviceResult.isError()) return JetiResult.error(deviceResult.getErrorCode());

			try (JetiSpectro spectro = deviceResult.getValue()) {
				return spectro.measureAllSpectra(integrationTime);
			}
		}

		static public JetiResult<RadiometricData> measureRadiometricEx (int deviceNumber, short averageCount, int step) {
			JetiResult<JetiRadioEx> deviceResult = openRadioExDevice(deviceNumber);
			if (deviceResult.isError()) return JetiResult.error(deviceResult.getErrorCode());

			try (JetiRadioEx radioEx = deviceResult.getValue()) {
				JetiResult<Boolean> measureResult = radioEx.measureWithAdaptation(averageCount, step);
				if (measureResult.isError()) return JetiResult.error(measureResult.getErrorCode());

				// Wait for measurement to complete
				boolean measuring = true;
				int timeout = 0;
				while (measuring && timeout < 100) { // 10 second timeout
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return JetiResult.error(-1);
					}

					JetiResult<Boolean> statusResult = radioEx.getMeasurementStatus();
					if (statusResult.isSuccess()) measuring = statusResult.getValue();
					timeout++;
				}

				if (measuring) return JetiResult.error(0x00000008); // JETI_TIMEOUT

				// For now, return a simple measurement - this would need to be implemented
				// based on specific requirements for the RadiometricExData structure
				return JetiResult.error(0x00000080); // Not implemented yet
			}
		}
	}

	static private void loadNativeLibraries () throws IOException {
		Path tempDir = Files.createTempDirectory("jeti-");
		tempDir.toFile().deleteOnExit();

		String[] libraries = {"jeti_core64.dll", "jeti_radio64.dll", "jeti_spectro64.dll", "jeti_radio_ex64.dll",
			"jeti_spectro_ex64.dll"};
		for (String library : libraries)
			try (InputStream input = JetiSDK.class.getResourceAsStream("/" + library)) {
				if (input != null) {
					Path path = tempDir.resolve(library);
					Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING);
					path.toFile().deleteOnExit();
					Log.debug("Extracted native library: " + path);
				} else
					Log.warn("Native library not found: " + library);
			}

		// Set JNA library path to include our temp directory
		String existingPath = System.getProperty("jna.library.path", "");
		String newPath = existingPath.isEmpty() ? tempDir.toString() : existingPath + File.pathSeparator + tempDir.toString();
		System.setProperty("jna.library.path", newPath);

		Log.debug("Native library path: " + newPath);
	}

	static private void ensureInitialized () {
		if (!initialized) {
			try {
				initialize();
			} catch (JetiException e) {
				throw new RuntimeException("JETI SDK not initialized", e);
			}
		}
	}

	static String bytesToString (byte[] bytes) {
		if (bytes == null) return "";

		// Find the null terminator.
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
