
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.esotericsoftware.jeti.JetiSDK.DllVersion;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CoreTest extends JetiTest {
	@Test
	@Order(1)
	@DisplayName("Set license key")
	void testSetLicenseKey () {
		String licenseKey = getTestLicenseKey();
		assumeFalse(licenseKey == null, "JETI license key not configured");
		Core.setLicenseKey(licenseKey);
	}

	@Test
	@Order(2)
	@DisplayName("Get DLL version")
	void testGetDllVersion () {
		DllVersion version = Core.getDllVersion();
		assertNotNull(version);
		System.out.println("Core DLL version: " + version.major() + "." + version.minor() + "." + version.build());
	}

	@Test
	@Order(3)
	@DisplayName("Get device count")
	void testGetDeviceCount () {
		int count = Core.getDeviceCount();
		assertTrue(count >= 0);
		System.out.println("Found " + count + " Core devices");
	}

	@Test
	@Order(4)
	@DisplayName("Get device info")
	void testGetDeviceInfo () {
		Core.DeviceInfo[] devices = Core.getAllDeviceInfo();
		assertNotNull(devices);
		for (Core.DeviceInfo device : devices)
			System.out.println("Device: " + device.deviceType() + " - " + device.deviceSerial());
	}

	// BOZO - Times out.
	// @Test
	// @Order(5)
	// @DisplayName("Measure ADC1")
	// void testMeasureADC1 () {
	// Core core = Core.openTcpDevice(IP);
	// assertNotNull(core);
	// try (core) {
	// assertTrue(core.measureADC1() >= 0);
	// }
	// }

	// BOZO - Times out.
	// @Test
	// @Order(6)
	// @DisplayName("Measure ADC2")
	// void testMeasureADC2 () {
	// Core core = Core.openTcpDevice(IP);
	// assertNotNull(core);
	// try (core) {
	// assertTrue(core.measureADC2() >= 0);
	// }
	// }

	@Test
	@Order(7)
	@DisplayName("Open TCP device")
	void testOpenTcpDevice () {
		Core core = Core.openTcpDevice(IP);
		assertNotNull(core);
		try (core) {
			testDeviceOperations(core);
		}
	}

	private void testDeviceOperations (Core core) {
		System.out.println("Firmware version: " + core.getFirmwareVersion());
		System.out.println("Device type: " + core.getDeviceType());
		System.out.println("Battery: " + core.getBatteryInfo());
		System.out.println("Temperature: " + core.getTemperature() + " C");
		System.out.println("Integration time: " + core.getIntegrationTime() + " ms");
		System.out.println("Pixel count: " + core.getPixelCount());

		testMeasurement(core);
		testParameterFunctions(core);
		testConfigurationFunctions(core);
	}

	private void testMeasurement (Core core) {
		// BOZO - Not supported?
		// core.prepareTriggeredMeasurement();

		core.measure();
		int attempts = 0;
		while (core.isMeasuring() && attempts++ < 100) {
			System.out.print(".");
			sleep(100);
		}
		System.out.println();
		assertFalse(attempts >= 100, "Measurement should complete within timeout");
	}

	private void testParameterFunctions (Core core) {
		System.out.println("Pixel binning: " + core.getPixelBinning());
		System.out.println("Scan delay: " + core.getScanDelay());

		// BOZO - Not supported?
		// System.out.println("Distance: " + core.getDistance());
		// System.out.println("Trigger timeout: " + core.getTriggerTimeout());
		System.out.println("Baudrate: " + core.getBaudrate());
	}

	private void testConfigurationFunctions (Core core) {
		System.out.println("Dark mode: " + core.getDarkModeConfig());
		System.out.println("Integration time config: " + core.getIntegrationTimeConfig());
		System.out.println("Wavelength range: " + core.getWavelengthRangeConfig());
		System.out.println("Level: " + core.getLevel());
	}

	@Test
	@Order(9)
	@DisplayName("Test straylight matrix functions")
	void testStrayLightMatrix () {
		Core.ignoreStraylightMatrix(true);
		Core.ignoreStraylightMatrix(false);
	}

	@Test
	@Order(10)
	@DisplayName("Test all Core functions for coverage")
	void testAllCoreFunctions () {
		testDeviceDiscoveryFunctions();
		try (Core core = Core.openTcpDevice(IP)) {
			testAllDeviceFunctions(core);
		}
	}

	private void testDeviceDiscoveryFunctions () {
		int deviceCount = Core.getDeviceCount();
		if (deviceCount > 0) {
			assertNotNull(Core.getDeviceSerials(0));
			assertNotNull(Core.getDeviceInfo(0));
			assertNotNull(Core.getDeviceInfoEx(0));
		}

		Core.openDevice(0).close();

		// Test other connection methods (these will likely fail but we're testing they're callable)
		try {
			Core.openComDevice(1, 115200).close();
			Core.openUsbDevice("TEST").close();
			Core.openBluetoothDevice(123456789L).close();
			Core.openBluetoothLeDevice("TEST_PATH").close();
		} catch (JetiException ignored) {
		}
	}

	private void testAllDeviceFunctions (Core core) {
		testControlFunctions(core);
		testDeviceCommunication(core);
		testMeasurementFunctions(core);
		testCalibrationFunctions(core);
		testAllParameterFunctions(core);
		testAllConfigurationFunctions(core);
		testFetchFunctions(core);
		testCalculationFunctions(core);

		// Not tested.
		// core.reset();
		// core.hardReset();
	}

	private void testDeviceCommunication (Core core) {
		assertNotNull(core.sendCommand("*contr:laser 1\r"));
		boolean laserStatus = core.getLaserStatus();
		assertTrue(core.getLaserStatus());
		assertNotNull(core.sendCommand("*contr:laser 0\r"));
		assertFalse(core.getLaserStatus());

		// BOZO - Causes other functions to return not supported (11).
		// core.deviceWrite("TEST", 4, 100);

		// BOZO - Times out.
		// assertNotNull(core.deviceRead(1, 4));
		// assertNotNull(core.deviceReadTerminated(1, 10));

		// core.dataReceived(100);

		try {
			assertNotNull(core.getComPortHandle());
		} catch (JetiException ignored) { // Not connected.
		}

		// BOZO - Times out.
		// assertNotNull(core.readUserData(0, 2));

		// BOZO - Crashes.
		// core.writeUserData(new byte[2], 0);

		// BOZO - Not supported?
		// assertTrue(core.getLastError() >= 0);
		// assertTrue(core.getEnquiry() >= 0);

		// Not tested (needs window, undocumented).
		// core.setCallbackFunction((byte)'E', null);
	}

	private void testMeasurementFunctions (Core core) {
		core.cancelMeasurement();

		// BOZO - Times out.
		// assertNotNull(core.waitReadTrigger(100));

		// BOZO - Not supported?
		// core.startAdaptation(false);
		// int attempts = 0;
		// while (!core.checkAdaptationStatus().complete() && attempts++ < 100) {
		// System.out.print(".");
		// sleep(100);
		// }
		// System.out.println();
		// assertFalse(attempts >= 100, "Measurement should complete within timeout");
	}

	private void testCalibrationFunctions (Core core) {
		// BOZO - Not supported?
		// assertNotNull(core.getCalibrationRange());

		// BOZO - Crashes.
		// assertNotNull(core.readCalibration(0));

		// Skip.
		// core.writeCalibration(0, "TEST", "Test calibration", 380, 780, 5, 100, new double[100]);
		//
		// core.deleteCalibration(0);
		//
		// core.setCalibration((byte)0);
		//
		// byte calibIndex = core.getCalibration();
		// assertTrue(calibIndex >= 0);

		// BOZO - Not supported?
		// core.measureCompensationDark();
	}

	private void testAllParameterFunctions (Core core) {
		// BOZO - Not supported?
		// assertTrue(core.getFit() >= 0);
		// core.setScanDelay(100);
		// assertTrue(core.getADCResolution() > 0);
		// assertTrue(core.getSplitTime() >= 0);
		// assertNotNull(core.getBorder());
		// core.setDistance(100);

		// BOZO - Times out.
		// core.setParameterBlock(core.getParameterBlock());

		// BOZO - Not supported?
		// assertNotNull(core.getOpticalTrigger());
		// core.setLaserIntensity(50, 100);
		// core.setTrigger(0);
		// core.setTriggerTimeout((short)1000);
		// core.setFlashMode(false);
		// core.setFlashCycle(1);
		// assertNotNull(core.getCorrectionStatus());
		// core.setCorrectionStatus(true);
		// assertNotNull(core.getCorrectionRange());

		// Skip.
		// core.setCorrectionRange(380, 780);
		// assertNotNull(core.getOffsetCorrectionRange());
		// core.setOffsetCorrectionRange(380, 780);
		// core.setCorrectionCoefficients(core.getCorrectionCoefficients());
		// core.getCutoffStatus();
		// core.setCutoffStatus(false);
		// core.getStrayLightMatrixEnabled();
		// core.setStrayLightMatrixEnabled(false);
		// assertNotNull(core.getChannelConfig());
		// core.setChannelConfig("TEST");
		// assertNotNull(core.getLampMode());
		// core.setLampMode((byte)0);
		// assertNotNull(core.getFlash());
		// core.setFlash(100.0f, 10.0f);
	}

	private void testControlFunctions (Core core) {
		core.setLaserStatus(true);
		assertTrue(core.getLaserStatus());
		core.setLaserStatus(false);
		assertFalse(core.getLaserStatus());
		core.getShutterStatus();

		// BOZO - Not supported?
		// core.setShutterStatus(true);

		core.getMeasurementHead();

		// BOZO - Not supported?
		// core.getAux1Status();
		// core.setAux1Status(false);
		// core.getAux2Status();
		// core.setAux2Status(false);
		// core.setAuxOut1(false);
		// core.getAuxOut1Status();
		// core.setAuxOut2(false);
		// core.getAuxOut2Status();
		// core.setAuxOut3(false);
		// core.getAuxOut3Status();
		// core.setAuxOut4(false);
		// core.getAuxOut4Status();
		// core.setAuxOut5(false);
		// core.getAuxOut5Status();
		// core.getAuxIn1Status();
		// core.getAuxIn2Status();

		assertNotNull(core.getFlickerFrequency());

		core.setSyncFrequency(50.0f);
		assertTrue(core.getSyncFrequency() >= 0);
		core.setSyncMode(false);
		core.getSyncMode();

		// BOZO - Not supported?
		// core.getDigitalIOInput();
		// core.getDigitalIOOutput();
		// core.setDigitalIOOutput((byte)0);
		// core.setDigitalIOOutputPin((byte)0, false);
	}

	private void testAllConfigurationFunctions (Core core) {
		// BOZO - Not supported?
		// core.getExposureConfig();
		// core.setExposureConfig((byte)0);
		// core.setDarkModeConfig((byte)0);
		// assertNotNull(core.getFunctionConfig());
		// core.setFunctionConfig((byte)0);
		// assertNotNull(core.getFormatConfig());
		// core.setFormatConfig((byte)0);
		// core.setIntegrationTimeConfig(100.0f);
		// assertTrue(core.getMaxIntegrationTimeConfig() > 0);
		// core.setMaxIntegrationTimeConfig(1000.0f);
		// assertTrue(core.getMaxAverageConfig() > 0);
		// core.setMaxAverageConfig((short)10);
		// assertTrue(core.getMinIntegrationTimeConfig() >= 0);
		// assertTrue(core.getImageMinIntegrationTimeConfig() >= 0);
		// assertTrue(core.getChannelMinIntegrationTimeConfig() >= 0);
		// assertTrue(core.getContinuousMinIntegrationTimeConfig() >= 0);
		// assertTrue(core.getContinuousChannelMinIntegrationTimeConfig() >= 0);
		// assertNotNull(core.getAverageConfig());
		// core.setAverageConfig((short)1);
		// core.getAdaptationConfig();
		// core.setAdaptationConfig((byte)0);
		// core.setWavelengthRangeConfig(380, 780, 5);
		// assertNotNull(core.getPDARowConfig());
		// core.setPDARowConfig(0, 0);
		// core.setDefault();
	}

	private void testFetchFunctions (Core core) {
		int pixelCount = core.getPixelCount();
		assertEquals(pixelCount, core.fetchDark(pixelCount).length);
		assertEquals(pixelCount, core.fetchLight(pixelCount).length);
		assertEquals(pixelCount, core.fetchReference(pixelCount).length);

		// BOZO - Times out.
		// assertEquals(pixelCount, core.fetchSample(pixelCount).length);

		assertEquals(SPECTRUM_SIZE, core.fetchSpectralRadiance(380, 780, 5).length);
		assertNotNull(core.fetchSpectralRadianceHiRes(380, 780));
		assertTrue(core.fetchRadiometricValue() >= 0);
		assertTrue(core.fetchPhotometricValue() >= 0);
		assertNotNull(core.fetchChromaXY());
		assertNotNull(core.fetchChromaUV());
		assertNotNull(core.fetchDominantWavelength());
		assertTrue(core.fetchCCT() >= 0);
		core.fetchDuv();

		float cri = core.fetchCRI();
		assertTrue(cri >= -100 && cri <= 100);

		assertNotNull(core.fetchXYZ());
		assertTrue(core.fetchAdaptationIntegrationTime() >= 0);
		assertTrue(core.fetchAdaptationAverage() >= 0);
	}

	private void testCalculationFunctions (Core core) {
		// BOZO - Not supported? Some crash.
		// assertNotNull(core.calculateSplineDark(380, 780, 5.0f));
		// assertNotNull(core.calculateSplineLight(380, 780, 5.0f));
		// assertNotNull(core.calculateSplineReference(380, 780, 5.0f));
		// assertNotNull(core.calculateSplineSample(380, 780, 5.0f));
		// assertNotNull(core.calculateLinearDark(380, 780, 5.0f));
		// assertNotNull(core.calculateLinearLight(380, 780, 5.0f));
		// assertNotNull(core.calculateLinearReference(380, 780, 5.0f));
		// assertNotNull(core.calculateLinearSample(380, 780, 5.0f));

		assertTrue(core.calculateRadiometricValue(380, 780) >= 0);
		assertTrue(core.calculatePhotometricValue() >= 0);
		assertNotNull(core.calculateChromaXY());
		assertNotNull(core.calculateChromaXY10());
		assertNotNull(core.calculateChromaUV());
		assertNotNull(core.calculateDominantWavelength());
		assertTrue(core.calculateCCT() >= 0);
		core.calculateDuv();
		assertNotNull(core.calculateXYZ());

		float cri = core.calculateCRI(5000.0f);
		assertTrue(cri >= -100 && cri <= 100);

		// BOZO - Not supported?
		// assertNotNull(core.calculateAllValues(380, 780));

		assertNotNull(core.calculateTM30(true));
		assertNotNull(core.calculatePeakFWHM(0.5f));
		assertNotNull(core.calculateBlueMeasurement());
	}

	@Test
	@Order(8)
	@DisplayName("Test import straylight matrix")
	void testImportStraylightMatrix () {
		// This will likely fail without a valid matrix file, but we're testing it's callable
		Core.importStraylightMatrix("test_matrix.slm");
	}

	static private String getTestLicenseKey () {
		String licenseKey = System.getenv("JETI_LICENSE_KEY");
		if (licenseKey != null && !licenseKey.trim().isEmpty()) return licenseKey.trim();

		licenseKey = System.getProperty("jeti.license.key");
		if (licenseKey != null && !licenseKey.trim().isEmpty()) return licenseKey.trim();

		return null;
	}
}
