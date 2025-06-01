
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiException.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.esotericsoftware.jeti.JetiCore.DeviceInfo;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JetiSDKTest {
	static public final String IP = "10.1.0.55";

	static private String getTestLicenseKey () {
		// Check environment variable first
		String licenseKey = System.getenv("JETI_LICENSE_KEY");
		if (licenseKey != null && !licenseKey.trim().isEmpty()) return licenseKey.trim();

		// Check system property second
		licenseKey = System.getProperty("jeti.license.key");
		if (licenseKey != null && !licenseKey.trim().isEmpty()) return licenseKey.trim();

		// Return null if no license key is configured
		return null;
	}

	@Test
	@Order(1)
	@DisplayName("SDK should initialize successfully")
	void testInitialization () {
		Log.TRACE();
		assertDoesNotThrow( () -> {
			JetiSDK.initialize();
			assertTrue(JetiSDK.isInitialized());
		});
	}

	@Test
	@Order(2)
	@DisplayName("Should get SDK version information")
	void testGetSDKVersion () {
		JetiResult<JetiSDK.SDKVersion> versionResult = JetiSDK.getSDKVersion();
		assertTrue(versionResult.isSuccess());

		JetiSDK.SDKVersion version = versionResult.getValue();
		assertNotNull(version);
		assertNotNull(version.coreVersion());
		assertNotNull(version.radioVersion());
		assertNotNull(version.spectroVersion());

		System.out.println("SDK Version: " + version);
	}

	@Test
	@Order(3)
	@DisplayName("Should discover devices without error")
	void testDeviceDiscovery () {
		JetiResult<DeviceInfo[]> devicesResult = JetiSDK.discoverDevices();

		// Should succeed even if no devices are connected
		assertTrue(devicesResult.isSuccess());
		assertNotNull(devicesResult.getValue());

		System.out.println("Found " + devicesResult.getValue().length + " devices");

		for (DeviceInfo device : devicesResult.getValue())
			System.out.println("Device: " + device);
	}

	@Test
	@Order(4)
	@DisplayName("Should get number of devices")
	void testGetNumberOfDevices () {
		JetiResult<Integer> coreDevicesResult = JetiSDK.getNumberOfCoreDevices();
		assertTrue(coreDevicesResult.isSuccess());
		assertTrue(coreDevicesResult.getValue() >= 0);

		JetiResult<Integer> radioDevicesResult = JetiSDK.getNumberOfRadioDevices();
		assertTrue(radioDevicesResult.isSuccess());
		assertTrue(radioDevicesResult.getValue() >= 0);

		JetiResult<Integer> spectroDevicesResult = JetiSDK.getNumberOfSpectroDevices();
		assertTrue(spectroDevicesResult.isSuccess());
		assertTrue(spectroDevicesResult.getValue() >= 0);

		System.out.println("Core devices: " + coreDevicesResult.getValue());
		System.out.println("Radio devices: " + radioDevicesResult.getValue());
		System.out.println("Spectro devices: " + spectroDevicesResult.getValue());
	}

	@Test
	@Order(5)
	@DisplayName("License key")
	void testLicenseKey () {
		String testLicenseKey = getTestLicenseKey();

		if (testLicenseKey != null) {
			// Test with real license key if provided
			System.out.println("Testing with provided license key");
			JetiResult<Boolean> result = JetiSDK.setLicenseKey(testLicenseKey);
			assertNotNull(result);
			if (result.isError())
				System.out.println("License key failed with error: 0x" + Integer.toHexString(result.getErrorCode()));
			else
				System.out.println("License key accepted successfully");
		} else {
			// Test with dummy values when no real license key is provided
			System.out.println("No license key provided, testing with dummy values");

			// Test with empty license key (should not crash)
			JetiResult<Boolean> result = JetiSDK.setLicenseKey("");
			assertNotNull(result);

			// Test with dummy license key
			result = JetiSDK.setLicenseKey("DUMMY-LICENSE-KEY");
			assertNotNull(result);
		}
	}

	@Test
	@Order(6)
	@DisplayName("Straylight matrix operations")
	void testStraylightMatrix () {
		// Test ignoring straylight matrix
		JetiResult<Boolean> ignoreResult = JetiSDK.ignoreStraylightMatrix(true);
		assertNotNull(ignoreResult);

		// Test importing non-existent file (behavior may vary - just ensure it doesn't crash)
		JetiResult<Boolean> importResult = JetiSDK.importStraylightMatrix("non-existent-file.slm");
		assertNotNull(importResult);
		// Note: Some implementations may return success even for non-existent files
		// The important thing is that it doesn't crash the application
	}

	@Test
	@Order(7)
	@DisplayName("TCP device connection")
	void testTcpDeviceConnection () {
		System.out.println("Testing TCP connection to: " + IP);

		// Test TCP device connection - should handle connection gracefully
		JetiResult<JetiCore> coreResult = JetiSDK.openTcpDevice(IP);
		if (coreResult.isError()) {
			System.out.println(
				"TCP device connection failed (expected if no device at IP): 0x" + Integer.toHexString(coreResult.getErrorCode()));
			assertTrue(coreResult.getErrorCode() != 0);
		} else {
			System.out.println("TCP device connected successfully");
			// If successful, close the device
			coreResult.getValue().close();
		}
	}

	@Test
	@Order(8)
	@DisplayName("Invalid device serial")
	void testOpenDeviceByInvalidSerial () {
		JetiResult<JetiCore> result = JetiSDK.openCoreDeviceBySerial("INVALID-SERIAL");
		assertTrue(result.isError());
		assertEquals(invalidDeviceNumber, result.getErrorCode());
	}

	@Test
	@Order(9)
	@DisplayName("Device opening when no devices present")
	void testDeviceOpeningWithoutDevices () {
		// These should fail gracefully when no devices are connected via discovery
		JetiResult<JetiCore> coreResult = JetiSDK.openCoreDevice(0);
		if (coreResult.isError())
			assertTrue(coreResult.getErrorCode() != 0);
		else
			// If successful, close the device
			coreResult.getValue().close();

		JetiResult<JetiRadio> radioResult = JetiSDK.openRadioDevice(0);
		if (radioResult.isError())
			assertTrue(radioResult.getErrorCode() != 0);
		else
			radioResult.getValue().close();

		JetiResult<JetiSpectro> spectroResult = JetiSDK.openSpectroDevice(0);
		if (spectroResult.isError())
			assertTrue(spectroResult.getErrorCode() != 0);
		else
			spectroResult.getValue().close();
	}
}
