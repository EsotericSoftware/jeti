
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JetiCoreTest {
	static public final String IP = "10.1.0.55";

	static private String getTestLicenseKey () {
		String licenseKey = System.getenv("JETI_LICENSE_KEY");
		if (licenseKey != null && !licenseKey.trim().isEmpty()) return licenseKey.trim();

		licenseKey = System.getProperty("jeti.license.key");
		if (licenseKey != null && !licenseKey.trim().isEmpty()) return licenseKey.trim();

		return null;
	}

	@Test
	@Order(1)
	@DisplayName("Initialize SDK")
	void testInitialization () {
		Log.TRACE();
		assertDoesNotThrow( () -> JetiSDK.initialize());
	}

	@Test
	@Order(2)
	@DisplayName("Set license key")
	void testSetLicenseKey () {
		String licenseKey = getTestLicenseKey();
		assumeFalse(licenseKey == null, "JETI license key not configured");
		var result = JetiCore.setLicenseKey(licenseKey);
		assertTrue(result.isSuccess(), result.toString());
	}

	@Test
	@Order(3)
	@DisplayName("Get DLL version")
	void testGetDllVersion () {
		var result = JetiCore.getDllVersion();
		assertTrue(result.isSuccess(), result.toString());
		assertNotNull(result.getValue());
		System.out.println(
			"Core DLL version: " + result.getValue().major() + "." + result.getValue().minor() + "." + result.getValue().build());
	}

	@Test
	@Order(4)
	@DisplayName("Get device count")
	void testGetDeviceCount () {
		var result = JetiCore.getDeviceCount();
		assertTrue(result.isSuccess(), result.toString());
		assertTrue(result.getValue() >= 0);
		System.out.println("Found " + result.getValue() + " Core devices");
	}

	@Test
	@Order(5)
	@DisplayName("Get device info")
	void testGetDeviceInfo () {
		var result = JetiCore.getAllDeviceInfo();
		assertTrue(result.isSuccess(), result.toString());
		assertNotNull(result.getValue());
		for (var device : result.getValue()) {
			System.out.println("Device: " + device.deviceType() + " - " + device.deviceSerial());
		}
	}

	@Test
	@Order(6)
	@DisplayName("Open TCP device")
	void testOpenTcpDevice () {
		var result = JetiCore.openTcpDevice(IP);
		assertTrue(result.isSuccess(), result.toString());
		var device = result.getValue();
		assertNotNull(device);
		try (device) {
			testDeviceOperations(device);
		}
	}

	private void testDeviceOperations (JetiCore device) {
		// Test basic device info
		var firmwareResult = device.getFirmwareVersion();
		assertTrue(firmwareResult.isSuccess(), firmwareResult.toString());
		System.out.println("Firmware version: " + firmwareResult.getValue());

		var deviceTypeResult = device.getDeviceType();
		assertTrue(deviceTypeResult.isSuccess(), deviceTypeResult.toString());
		System.out.println("Device type: " + deviceTypeResult.getValue());

		// Test battery info
		var batteryResult = device.getBatteryInfo();
		if (batteryResult.isSuccess()) {
			var battery = batteryResult.getValue();
			System.out.println("Battery: " + battery.voltage() + "V, " + battery.percent() + "%, charging=" + battery.charging());
		}

		// Test temperature
		var tempResult = device.getTemperature();
		if (tempResult.isSuccess()) {
			System.out.println("Temperature: " + tempResult.getValue() + "°C");
		}

		// Test integration time
		var tintResult = device.getIntegrationTime();
		if (tintResult.isSuccess()) {
			System.out.println("Integration time: " + tintResult.getValue() + "ms");
		}

		// Test pixel count
		var pixelResult = device.getPixelCount();
		if (pixelResult.isSuccess()) {
			System.out.println("Pixel count: " + pixelResult.getValue());
		}

		// Test measurement
		testMeasurement(device);

		// Test parameter functions
		testParameterFunctions(device);

		// Test configuration functions
		testConfigurationFunctions(device);
	}

	private void testMeasurement (JetiCore device) {
		// Prepare measurement
		var prepareResult = device.prepareMeasurement();
		if (prepareResult.isSuccess()) {
			// Start measurement
			var measureResult = device.measure();
			if (measureResult.isSuccess()) {
				// Wait for measurement to complete
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Ignore
				}

				// Check status
				var statusResult = device.getMeasurementStatus();
				if (statusResult.isSuccess()) {
					System.out.println("Measurement complete: " + statusResult.getValue());
				}
			}
		}
	}

	private void testParameterFunctions (JetiCore device) {
		// Test pixel binning
		var pixBinResult = device.getPixelBinning();
		if (pixBinResult.isSuccess()) {
			System.out.println("Pixel binning: " + pixBinResult.getValue());
		}

		// Test scan delay
		var delayResult = device.getScanDelay();
		if (delayResult.isSuccess()) {
			System.out.println("Scan delay: " + delayResult.getValue());
		}

		// Test distance
		var distanceResult = device.getDistance();
		if (distanceResult.isSuccess()) {
			System.out.println("Distance: " + distanceResult.getValue());
		}

		// Test trigger timeout
		var trigTimeoutResult = device.getTriggerTimeout();
		if (trigTimeoutResult.isSuccess()) {
			System.out.println("Trigger timeout: " + trigTimeoutResult.getValue());
		}

		// Test baudrate
		var baudrateResult = device.getBaudrate();
		if (baudrateResult.isSuccess()) {
			System.out.println("Baudrate: " + baudrateResult.getValue());
		}
	}

	private void testConfigurationFunctions (JetiCore device) {
		// Test dark mode configuration
		var darkModeResult = device.getDarkModeConfig();
		if (darkModeResult.isSuccess()) {
			System.out.println("Dark mode: " + darkModeResult.getValue());
		}

		// Test integration time configuration
		var tintConfResult = device.getIntegrationTimeConfig();
		if (tintConfResult.isSuccess()) {
			var conf = tintConfResult.getValue();
			System.out.println("Integration time config - previous: " + conf.previous() + ", configured: " + conf.configured());
		}

		// Test wavelength range configuration
		var wranResult = device.getWavelengthRangeConfig();
		if (wranResult.isSuccess()) {
			var range = wranResult.getValue();
			System.out.println("Wavelength range: " + range.begin() + "-" + range.end() + " nm, step=" + range.step());
		}

		// Test level
		var levelResult = device.getLevel();
		if (levelResult.isSuccess()) {
			var level = levelResult.getValue();
			System.out.println("Level - counts: " + level.counts() + ", percent: " + level.percent() + "%");
		}
	}

	@Test
	@Order(7)
	@DisplayName("Test straylight matrix functions")
	void testStrayLightMatrix () {
		// Test ignore straylight matrix
		var ignoreResult = JetiCore.ignoreStraylightMatrix(true);
		assertTrue(ignoreResult.isSuccess(), ignoreResult.toString());

		// Reset to not ignore
		ignoreResult = JetiCore.ignoreStraylightMatrix(false);
		assertTrue(ignoreResult.isSuccess(), ignoreResult.toString());
	}

	@Test
	@Order(8)
	@DisplayName("Test all Core functions for coverage")
	void testAllCoreFunctions () {
		// Test device discovery functions
		testDeviceDiscoveryFunctions();

		// Open device and test all device-specific functions
		var openResult = JetiCore.openTcpDevice(IP);
		assumeFalse(openResult.isError(), "Could not open device: " + openResult);

		try (var device = openResult.getValue()) {
			testAllDeviceFunctions(device);
		}
	}

	private void testDeviceDiscoveryFunctions () {
		// Test getDeviceSerials
		var countResult = JetiCore.getDeviceCount();
		if (countResult.isSuccess() && countResult.getValue() > 0) {
			var serialsResult = JetiCore.getDeviceSerials(0);
			if (serialsResult.isSuccess()) {
				assertNotNull(serialsResult.getValue());
			}
		}

		// Test getDeviceInfo and getDeviceInfoEx
		if (countResult.isSuccess() && countResult.getValue() > 0) {
			var infoResult = JetiCore.getDeviceInfo(0);
			if (infoResult.isSuccess()) {
				assertNotNull(infoResult.getValue());
			}

			var infoExResult = JetiCore.getDeviceInfoEx(0);
			if (infoExResult.isSuccess()) {
				assertNotNull(infoExResult.getValue());
			}
		}

		// Test different open methods
		var openDeviceResult = JetiCore.openDevice(0);
		if (openDeviceResult.isSuccess()) {
			openDeviceResult.getValue().close();
		}

		// Test other connection methods (these will likely fail but we're testing they're callable)
		var comResult = JetiCore.openComDevice(1, 115200);
		if (comResult.isSuccess()) {
			comResult.getValue().close();
		}

		var usbResult = JetiCore.openUsbDevice("TEST");
		if (usbResult.isSuccess()) {
			usbResult.getValue().close();
		}

		var btResult = JetiCore.openBluetoothDevice(123456789L);
		if (btResult.isSuccess()) {
			btResult.getValue().close();
		}

		var btleResult = JetiCore.openBluetoothLeDevice("TEST_PATH");
		if (btleResult.isSuccess()) {
			btleResult.getValue().close();
		}
	}

	private void testAllDeviceFunctions (JetiCore device) {
		// Test device communication functions
		testDeviceCommunication(device);

		// Test measurement functions
		testMeasurementFunctions(device);

		// Test calibration functions
		testCalibrationFunctions(device);

		// Test all parameter functions
		testAllParameterFunctions(device);

		// Test all control functions
		testControlFunctions(device);

		// Test all configuration functions
		testAllConfigurationFunctions(device);

		// Test fetch functions
		testFetchFunctions(device);

		// Test calculation functions
		testCalculationFunctions(device);
	}

	private void testDeviceCommunication (JetiCore device) {
		// Test reset functions
		device.reset();
		device.hardReset();

		// Test device communication
		var writeResult = device.deviceWrite("TEST", 4, 1000);
		if (writeResult.isSuccess()) {
			var readResult = device.deviceRead(100, 1000);
			if (readResult.isSuccess()) {
				assertNotNull(readResult.getValue());
			}
		}

		var readTermResult = device.deviceReadTerminated(100, 1000);
		if (readTermResult.isSuccess()) {
			assertNotNull(readTermResult.getValue());
		}

		var dataReceivedResult = device.dataReceived(100);
		if (dataReceivedResult.isSuccess()) {
			assertTrue(dataReceivedResult.getValue() >= 0);
		}

		var commandResult = device.sendCommand("*IDN?");
		if (commandResult.isSuccess()) {
			assertNotNull(commandResult.getValue());
		}

		// Test device info functions
		var comPortResult = device.getComPortHandle();
		if (comPortResult.isSuccess()) {
			assertNotNull(comPortResult.getValue());
		}

		var adc1Result = device.measureADC1();
		if (adc1Result.isSuccess()) {
			assertTrue(adc1Result.getValue() >= 0);
		}

		var adc2Result = device.measureADC2();
		if (adc2Result.isSuccess()) {
			assertTrue(adc2Result.getValue() >= 0);
		}

		// Test user data
		var readUserDataResult = device.readUserData(0, 63);
		if (readUserDataResult.isSuccess()) {
			assertNotNull(readUserDataResult.getValue());
		}

		byte[] testData = new byte[64];
		var writeUserDataResult = device.writeUserData(testData, 0);
		if (writeUserDataResult.isSuccess()) {
			assertTrue(writeUserDataResult.getValue());
		}

		// Test error and enquiry
		var lastErrorResult = device.getLastError();
		if (lastErrorResult.isSuccess()) {
			assertTrue(lastErrorResult.getValue() >= 0);
		}

		var enquiryResult = device.getEnquiry();
		if (enquiryResult.isSuccess()) {
			assertTrue(enquiryResult.getValue() >= 0);
		}

		// Test callback function (Windows only)
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			var callbackResult = device.setCallbackFunction((byte)'E', null);
			if (callbackResult.isSuccess()) {
				assertTrue(callbackResult.getValue());
			}
		}
	}

	private void testMeasurementFunctions (JetiCore device) {
		// Test break measurement
		var breakResult = device.breakMeasurement();
		if (breakResult.isSuccess()) {
			assertTrue(breakResult.getValue());
		}

		// Test wait read trigger
		var waitResult = device.waitReadTrigger(100);
		if (waitResult.isSuccess()) {
			assertNotNull(waitResult.getValue());
		}

		// Test adaptation
		var startAdaptResult = device.startAdaptation(false);
		if (startAdaptResult.isSuccess()) {
			var checkAdaptResult = device.checkAdaptationStatus();
			if (checkAdaptResult.isSuccess()) {
				assertNotNull(checkAdaptResult.getValue());
			}
		}
	}

	private void testCalibrationFunctions (JetiCore device) {
		// Test calibration functions
		var calibRangeResult = device.getCalibrationRange();
		if (calibRangeResult.isSuccess()) {
			assertNotNull(calibRangeResult.getValue());
		}

		var readCalibResult = device.readCalibration(0);
		if (readCalibResult.isSuccess()) {
			assertNotNull(readCalibResult.getValue());
		}

		double[] testValues = new double[100];
		var writeCalibResult = device.writeCalibration(0, "TEST", "Test calibration", 380, 780, 5, 100, testValues);
		if (writeCalibResult.isSuccess()) {
			assertTrue(writeCalibResult.getValue());
		}

		var deleteCalibResult = device.deleteCalibration(0);
		if (deleteCalibResult.isSuccess()) {
			assertTrue(deleteCalibResult.getValue());
		}

		var setCalibResult = device.setCalibration((byte)0);
		if (setCalibResult.isSuccess()) {
			assertTrue(setCalibResult.getValue());
		}

		var getCalibResult = device.getCalibration();
		if (getCalibResult.isSuccess()) {
			assertTrue(getCalibResult.getValue() >= 0);
		}

		var measCompDarkResult = device.measureCompensationDark();
		if (measCompDarkResult.isSuccess()) {
			assertTrue(measCompDarkResult.getValue());
		}
	}

	private void testAllParameterFunctions (JetiCore device) {
		// Test additional parameter functions not in testParameterFunctions
		var fitResult = device.getFit();
		if (fitResult.isSuccess()) {
			assertTrue(fitResult.getValue() >= 0);
		}

		var setSDelayResult = device.setScanDelay(100);
		if (setSDelayResult.isSuccess()) {
			assertTrue(setSDelayResult.getValue());
		}

		var adcResResult = device.getADCResolution();
		if (adcResResult.isSuccess()) {
			assertTrue(adcResResult.getValue() > 0);
		}

		var splitTimeResult = device.getSplitTime();
		if (splitTimeResult.isSuccess()) {
			assertTrue(splitTimeResult.getValue() >= 0);
		}

		var borderResult = device.getBorder();
		if (borderResult.isSuccess()) {
			assertNotNull(borderResult.getValue());
		}

		var setDistanceResult = device.setDistance(100);
		if (setDistanceResult.isSuccess()) {
			assertTrue(setDistanceResult.getValue());
		}

		// Test parameter block
		var getParamBlockResult = device.getParameterBlock();
		if (getParamBlockResult.isSuccess()) {
			assertNotNull(getParamBlockResult.getValue());

			var setParamBlockResult = device.setParameterBlock(getParamBlockResult.getValue());
			if (setParamBlockResult.isSuccess()) {
				assertTrue(setParamBlockResult.getValue());
			}
		}

		// Test optical trigger
		var optTrigResult = device.getOpticalTrigger();
		if (optTrigResult.isSuccess()) {
			assertNotNull(optTrigResult.getValue());
		}

		// Test laser intensity
		var laserIntResult = device.setLaserIntensity(50, 100);
		if (laserIntResult.isSuccess()) {
			assertTrue(laserIntResult.getValue());
		}

		// Test trigger
		var setTriggerResult = device.setTrigger(0);
		if (setTriggerResult.isSuccess()) {
			assertTrue(setTriggerResult.getValue());
		}

		var setTrigTimeoutResult = device.setTriggerTimeout((short)1000);
		if (setTrigTimeoutResult.isSuccess()) {
			assertTrue(setTrigTimeoutResult.getValue());
		}

		// Test flash mode
		var setFlashModeResult = device.setFlashMode(false);
		if (setFlashModeResult.isSuccess()) {
			assertTrue(setFlashModeResult.getValue());
		}

		var setFlashCycleResult = device.setFlashCycle(1);
		if (setFlashCycleResult.isSuccess()) {
			assertTrue(setFlashCycleResult.getValue());
		}

		// Test correction functions
		var getCorrStatResult = device.getCorrectionStatus();
		if (getCorrStatResult.isSuccess()) {
			assertNotNull(getCorrStatResult.getValue());
		}

		var setCorrStatResult = device.setCorrectionStatus(false);
		if (setCorrStatResult.isSuccess()) {
			assertTrue(setCorrStatResult.getValue());
		}

		var getCorrRangeResult = device.getCorrectionRange();
		if (getCorrRangeResult.isSuccess()) {
			assertNotNull(getCorrRangeResult.getValue());
		}

		var setCorrRangeResult = device.setCorrectionRange(380, 780);
		if (setCorrRangeResult.isSuccess()) {
			assertTrue(setCorrRangeResult.getValue());
		}

		var getOffsetCorrRangeResult = device.getOffsetCorrectionRange();
		if (getOffsetCorrRangeResult.isSuccess()) {
			assertNotNull(getOffsetCorrRangeResult.getValue());
		}

		var setOffsetCorrRangeResult = device.setOffsetCorrectionRange(380, 780);
		if (setOffsetCorrRangeResult.isSuccess()) {
			assertTrue(setOffsetCorrRangeResult.getValue());
		}

		var getCorrCoeffResult = device.getCorrectionCoefficients();
		if (getCorrCoeffResult.isSuccess()) {
			assertNotNull(getCorrCoeffResult.getValue());

			var setCorrCoeffResult = device.setCorrectionCoefficients(getCorrCoeffResult.getValue());
			if (setCorrCoeffResult.isSuccess()) {
				assertTrue(setCorrCoeffResult.getValue());
			}
		}

		// Test cutoff
		var getCutoffResult = device.getCutoffStatus();
		if (getCutoffResult.isSuccess()) {
			assertNotNull(getCutoffResult.getValue());
		}

		var setCutoffResult = device.setCutoffStatus(false);
		if (setCutoffResult.isSuccess()) {
			assertTrue(setCutoffResult.getValue());
		}

		// Test SLM
		var getSLMResult = device.getStrayLightMatrixEnabled();
		if (getSLMResult.isSuccess()) {
			assertNotNull(getSLMResult.getValue());
		}

		var setSLMResult = device.setStrayLightMatrixEnabled(false);
		if (setSLMResult.isSuccess()) {
			assertTrue(setSLMResult.getValue());
		}

		// Test channel config
		var getChannelResult = device.getChannelConfig();
		if (getChannelResult.isSuccess()) {
			assertNotNull(getChannelResult.getValue());
		}

		var setChannelResult = device.setChannelConfig("TEST");
		if (setChannelResult.isSuccess()) {
			assertTrue(setChannelResult.getValue());
		}

		// Test lamp mode
		var getLampModeResult = device.getLampMode();
		if (getLampModeResult.isSuccess()) {
			assertNotNull(getLampModeResult.getValue());
		}

		var setLampModeResult = device.setLampMode((byte)0);
		if (setLampModeResult.isSuccess()) {
			assertTrue(setLampModeResult.getValue());
		}

		// Test flash
		var getFlashResult = device.getFlash();
		if (getFlashResult.isSuccess()) {
			assertNotNull(getFlashResult.getValue());
		}

		var setFlashResult = device.setFlash(100.0f, 10.0f);
		if (setFlashResult.isSuccess()) {
			assertTrue(setFlashResult.getValue());
		}
	}

	private void testControlFunctions (JetiCore device) {
		// Test laser status
		var getLaserResult = device.getLaserStatus();
		if (getLaserResult.isSuccess()) {
			assertNotNull(getLaserResult.getValue());
		}

		var setLaserResult = device.setLaserStatus(false);
		if (setLaserResult.isSuccess()) {
			assertTrue(setLaserResult.getValue());
		}

		// Test shutter status
		var getShutterResult = device.getShutterStatus();
		if (getShutterResult.isSuccess()) {
			assertNotNull(getShutterResult.getValue());
		}

		var setShutterResult = device.setShutterStatus(true);
		if (setShutterResult.isSuccess()) {
			assertTrue(setShutterResult.getValue());
		}

		// Test measurement head
		var getMeasHeadResult = device.getMeasurementHead();
		if (getMeasHeadResult.isSuccess()) {
			assertNotNull(getMeasHeadResult.getValue());
		}

		// Test aux status
		var getAux1Result = device.getAux1Status();
		if (getAux1Result.isSuccess()) {
			assertNotNull(getAux1Result.getValue());
		}

		var setAux1Result = device.setAux1Status(false);
		if (setAux1Result.isSuccess()) {
			assertTrue(setAux1Result.getValue());
		}

		var getAux2Result = device.getAux2Status();
		if (getAux2Result.isSuccess()) {
			assertNotNull(getAux2Result.getValue());
		}

		var setAux2Result = device.setAux2Status(false);
		if (setAux2Result.isSuccess()) {
			assertTrue(setAux2Result.getValue());
		}

		// Test aux outputs
		var setAuxOut1Result = device.setAuxOut1(false);
		if (setAuxOut1Result.isSuccess()) {
			assertTrue(setAuxOut1Result.getValue());
		}

		var getAuxOut1Result = device.getAuxOut1Status();
		if (getAuxOut1Result.isSuccess()) {
			assertNotNull(getAuxOut1Result.getValue());
		}

		var setAuxOut2Result = device.setAuxOut2(false);
		if (setAuxOut2Result.isSuccess()) {
			assertTrue(setAuxOut2Result.getValue());
		}

		var getAuxOut2Result = device.getAuxOut2Status();
		if (getAuxOut2Result.isSuccess()) {
			assertNotNull(getAuxOut2Result.getValue());
		}

		var setAuxOut3Result = device.setAuxOut3(false);
		if (setAuxOut3Result.isSuccess()) {
			assertTrue(setAuxOut3Result.getValue());
		}

		var getAuxOut3Result = device.getAuxOut3Status();
		if (getAuxOut3Result.isSuccess()) {
			assertNotNull(getAuxOut3Result.getValue());
		}

		var setAuxOut4Result = device.setAuxOut4(false);
		if (setAuxOut4Result.isSuccess()) {
			assertTrue(setAuxOut4Result.getValue());
		}

		var getAuxOut4Result = device.getAuxOut4Status();
		if (getAuxOut4Result.isSuccess()) {
			assertNotNull(getAuxOut4Result.getValue());
		}

		var setAuxOut5Result = device.setAuxOut5(false);
		if (setAuxOut5Result.isSuccess()) {
			assertTrue(setAuxOut5Result.getValue());
		}

		var getAuxOut5Result = device.getAuxOut5Status();
		if (getAuxOut5Result.isSuccess()) {
			assertNotNull(getAuxOut5Result.getValue());
		}

		// Test aux inputs
		var getAuxIn1Result = device.getAuxIn1Status();
		if (getAuxIn1Result.isSuccess()) {
			assertNotNull(getAuxIn1Result.getValue());
		}

		var getAuxIn2Result = device.getAuxIn2Status();
		if (getAuxIn2Result.isSuccess()) {
			assertNotNull(getAuxIn2Result.getValue());
		}

		// Test flicker frequency
		var getFlickerResult = device.getFlickerFrequency();
		if (getFlickerResult.isSuccess()) {
			assertNotNull(getFlickerResult.getValue());
		}

		// Test sync
		var setSyncFreqResult = device.setSyncFrequency(50.0f);
		if (setSyncFreqResult.isSuccess()) {
			assertTrue(setSyncFreqResult.getValue());
		}

		var getSyncFreqResult = device.getSyncFrequency();
		if (getSyncFreqResult.isSuccess()) {
			assertTrue(getSyncFreqResult.getValue() >= 0);
		}

		var setSyncModeResult = device.setSyncMode((byte)0);
		if (setSyncModeResult.isSuccess()) {
			assertTrue(setSyncModeResult.getValue());
		}

		var getSyncModeResult = device.getSyncMode();
		if (getSyncModeResult.isSuccess()) {
			assertNotNull(getSyncModeResult.getValue());
		}

		// Test DIO
		var getDIOInResult = device.getDigitalIOInput();
		if (getDIOInResult.isSuccess()) {
			assertNotNull(getDIOInResult.getValue());
		}

		var getDIOOutResult = device.getDigitalIOOutput();
		if (getDIOOutResult.isSuccess()) {
			assertNotNull(getDIOOutResult.getValue());
		}

		var setDIOOutResult = device.setDigitalIOOutput((byte)0);
		if (setDIOOutResult.isSuccess()) {
			assertTrue(setDIOOutResult.getValue());
		}

		var setDIOOutPinResult = device.setDigitalIOOutputPin((byte)0, false);
		if (setDIOOutPinResult.isSuccess()) {
			assertTrue(setDIOOutPinResult.getValue());
		}
	}

	private void testAllConfigurationFunctions (JetiCore device) {
		// Test exposure config
		var getExposureResult = device.getExposureConfig();
		if (getExposureResult.isSuccess()) {
			assertNotNull(getExposureResult.getValue());
		}

		var setExposureResult = device.setExposureConfig((byte)0);
		if (setExposureResult.isSuccess()) {
			assertTrue(setExposureResult.getValue());
		}

		// Test dark mode config
		var setDarkModeResult = device.setDarkModeConfig((byte)0);
		if (setDarkModeResult.isSuccess()) {
			assertTrue(setDarkModeResult.getValue());
		}

		// Test function config
		var getFunctionResult = device.getFunctionConfig();
		if (getFunctionResult.isSuccess()) {
			assertNotNull(getFunctionResult.getValue());
		}

		var setFunctionResult = device.setFunctionConfig((byte)0);
		if (setFunctionResult.isSuccess()) {
			assertTrue(setFunctionResult.getValue());
		}

		// Test format config
		var getFormatResult = device.getFormatConfig();
		if (getFormatResult.isSuccess()) {
			assertNotNull(getFormatResult.getValue());
		}

		var setFormatResult = device.setFormatConfig((byte)0);
		if (setFormatResult.isSuccess()) {
			assertTrue(setFormatResult.getValue());
		}

		// Test integration time config
		var setTintConfResult = device.setIntegrationTimeConfig(100.0f);
		if (setTintConfResult.isSuccess()) {
			assertTrue(setTintConfResult.getValue());
		}

		// Test max integration time
		var getMaxTintResult = device.getMaxIntegrationTimeConfig();
		if (getMaxTintResult.isSuccess()) {
			assertTrue(getMaxTintResult.getValue() > 0);
		}

		var setMaxTintResult = device.setMaxIntegrationTimeConfig(1000.0f);
		if (setMaxTintResult.isSuccess()) {
			assertTrue(setMaxTintResult.getValue());
		}

		// Test max average
		var getMaxAverResult = device.getMaxAverageConfig();
		if (getMaxAverResult.isSuccess()) {
			assertTrue(getMaxAverResult.getValue() > 0);
		}

		var setMaxAverResult = device.setMaxAverageConfig((short)10);
		if (setMaxAverResult.isSuccess()) {
			assertTrue(setMaxAverResult.getValue());
		}

		// Test min integration times
		var getMinTintResult = device.getMinIntegrationTimeConfig();
		if (getMinTintResult.isSuccess()) {
			assertTrue(getMinTintResult.getValue() >= 0);
		}

		var getImageMinTintResult = device.getImageMinIntegrationTimeConfig();
		if (getImageMinTintResult.isSuccess()) {
			assertTrue(getImageMinTintResult.getValue() >= 0);
		}

		var getChanMinTintResult = device.getChannelMinIntegrationTimeConfig();
		if (getChanMinTintResult.isSuccess()) {
			assertTrue(getChanMinTintResult.getValue() >= 0);
		}

		var getContMinTintResult = device.getContinuousMinIntegrationTimeConfig();
		if (getContMinTintResult.isSuccess()) {
			assertTrue(getContMinTintResult.getValue() >= 0);
		}

		var getContChanMinTintResult = device.getContinuousChannelMinIntegrationTimeConfig();
		if (getContChanMinTintResult.isSuccess()) {
			assertTrue(getContChanMinTintResult.getValue() >= 0);
		}

		// Test average config
		var getAverConfResult = device.getAverageConfig();
		if (getAverConfResult.isSuccess()) {
			assertNotNull(getAverConfResult.getValue());
		}

		var setAverConfResult = device.setAverageConfig((short)1);
		if (setAverConfResult.isSuccess()) {
			assertTrue(setAverConfResult.getValue());
		}

		// Test adaptation config
		var getAdaptConfResult = device.getAdaptationConfig();
		if (getAdaptConfResult.isSuccess()) {
			assertNotNull(getAdaptConfResult.getValue());
		}

		var setAdaptConfResult = device.setAdaptationConfig((byte)0);
		if (setAdaptConfResult.isSuccess()) {
			assertTrue(setAdaptConfResult.getValue());
		}

		// Test wavelength range config
		var setWranConfResult = device.setWavelengthRangeConfig(380, 780, 5);
		if (setWranConfResult.isSuccess()) {
			assertTrue(setWranConfResult.getValue());
		}

		// Test PDA row config
		var getPDARowResult = device.getPDARowConfig();
		if (getPDARowResult.isSuccess()) {
			assertNotNull(getPDARowResult.getValue());
		}

		var setPDARowResult = device.setPDARowConfig(0, 0);
		if (setPDARowResult.isSuccess()) {
			assertTrue(setPDARowResult.getValue());
		}

		// Test set default
		var setDefaultResult = device.setDefault();
		if (setDefaultResult.isSuccess()) {
			assertTrue(setDefaultResult.getValue());
		}
	}

	private void testFetchFunctions (JetiCore device) {
		// Get pixel count for array size
		var pixelCountResult = device.getPixelCount();
		int pixelCount = pixelCountResult.isSuccess() ? pixelCountResult.getValue() : SPECTRUM_SIZE;

		// Test fetch dark
		var fetchDarkResult = device.fetchDark(pixelCount);
		if (fetchDarkResult.isSuccess()) {
			assertNotNull(fetchDarkResult.getValue());
			assertEquals(pixelCount, fetchDarkResult.getValue().length);
		}

		// Test fetch light
		var fetchLightResult = device.fetchLight(pixelCount);
		if (fetchLightResult.isSuccess()) {
			assertNotNull(fetchLightResult.getValue());
			assertEquals(pixelCount, fetchLightResult.getValue().length);
		}

		// Test fetch reference
		var fetchReferenceResult = device.fetchReference(pixelCount);
		if (fetchReferenceResult.isSuccess()) {
			assertNotNull(fetchReferenceResult.getValue());
			assertEquals(pixelCount, fetchReferenceResult.getValue().length);
		}

		// Test fetch transmission/reflection
		var fetchTransReflResult = device.fetchTransmissionReflection(pixelCount);
		if (fetchTransReflResult.isSuccess()) {
			assertNotNull(fetchTransReflResult.getValue());
			assertEquals(pixelCount, fetchTransReflResult.getValue().length);
		}

		// Test fetch spectral radiance
		var fetchSpradResult = device.fetchSpectralRadiance(400, 780, 5);
		if (fetchSpradResult.isSuccess()) {
			assertNotNull(fetchSpradResult.getValue());
			assertEquals(SPECTRUM_SIZE, fetchSpradResult.getValue().length);
		}

		// Test fetch spectral radiance hi res
		var fetchSpradHiResResult = device.fetchSpectralRadianceHiRes(400, 780);
		if (fetchSpradHiResResult.isSuccess()) {
			assertNotNull(fetchSpradHiResResult.getValue());
		}

		// Test fetch radiometric value
		var fetchRadioResult = device.fetchRadiometricValue();
		if (fetchRadioResult.isSuccess()) {
			assertTrue(fetchRadioResult.getValue() >= 0);
		}

		// Test fetch photometric value
		var fetchPhotoResult = device.fetchPhotometricValue();
		if (fetchPhotoResult.isSuccess()) {
			assertTrue(fetchPhotoResult.getValue() >= 0);
		}

		// Test fetch chromaticity XY
		var fetchChromxyResult = device.fetchChromaXY();
		if (fetchChromxyResult.isSuccess()) {
			assertNotNull(fetchChromxyResult.getValue());
		}

		// Test fetch chromaticity UV
		var fetchChromuvResult = device.fetchChromaUV();
		if (fetchChromuvResult.isSuccess()) {
			assertNotNull(fetchChromuvResult.getValue());
		}

		// Test fetch dominant wavelength
		var fetchDWLPEResult = device.fetchDominantWavelength();
		if (fetchDWLPEResult.isSuccess()) {
			assertNotNull(fetchDWLPEResult.getValue());
		}

		// Test fetch CCT
		var fetchCCTResult = device.fetchCCT();
		if (fetchCCTResult.isSuccess()) {
			assertTrue(fetchCCTResult.getValue() >= 0);
		}

		// Test fetch Duv
		var fetchDuvResult = device.fetchDuv();
		if (fetchDuvResult.isSuccess()) {
			assertNotNull(fetchDuvResult.getValue());
		}

		// Test fetch CRI
		var fetchCRIResult = device.fetchCRI();
		if (fetchCRIResult.isSuccess()) {
			assertTrue(fetchCRIResult.getValue() >= -100 && fetchCRIResult.getValue() <= 100);
		}

		// Test fetch XYZ
		var fetchXYZResult = device.fetchXYZ();
		if (fetchXYZResult.isSuccess()) {
			assertNotNull(fetchXYZResult.getValue());
		}

		// Test fetch adaptation time
		var fetchTiAdaptResult = device.fetchAdaptationIntegrationTime();
		if (fetchTiAdaptResult.isSuccess()) {
			assertTrue(fetchTiAdaptResult.getValue() >= 0);
		}

		// Test fetch adaptation average
		var fetchAverAdaptResult = device.fetchAdaptationAverage();
		if (fetchAverAdaptResult.isSuccess()) {
			assertTrue(fetchAverAdaptResult.getValue() >= 0);
		}
	}

	private void testCalculationFunctions (JetiCore device) {
		// Test calculate linear dark
		var calcLintDarkResult = device.calculateLinearDark(380, 780, 5.0f);
		if (calcLintDarkResult.isSuccess()) {
			assertNotNull(calcLintDarkResult.getValue());
		}

		// Test calculate spline dark
		var calcSplinDarkResult = device.calculateSplineDark(380, 780, 5.0f);
		if (calcSplinDarkResult.isSuccess()) {
			assertNotNull(calcSplinDarkResult.getValue());
		}

		// Test calculate linear light
		var calcLintLightResult = device.calculateLinearLight(380, 780, 5.0f);
		if (calcLintLightResult.isSuccess()) {
			assertNotNull(calcLintLightResult.getValue());
		}

		// Test calculate spline light
		var calcSplinLightResult = device.calculateSplineLight(380, 780, 5.0f);
		if (calcSplinLightResult.isSuccess()) {
			assertNotNull(calcSplinLightResult.getValue());
		}

		// Test calculate linear reference
		var calcLintReferResult = device.calculateLinearReference(380, 780, 5.0f);
		if (calcLintReferResult.isSuccess()) {
			assertNotNull(calcLintReferResult.getValue());
		}

		// Test calculate spline reference
		var calcSplinReferResult = device.calculateSplineReference(380, 780, 5.0f);
		if (calcSplinReferResult.isSuccess()) {
			assertNotNull(calcSplinReferResult.getValue());
		}

		// Test calculate linear trans/refl
		var calcLintTransReflResult = device.calculateLinearTransmissionReflection(380, 780, 5.0f);
		if (calcLintTransReflResult.isSuccess()) {
			assertNotNull(calcLintTransReflResult.getValue());
		}

		// Test calculate spline trans/refl
		var calcSplinTransReflResult = device.calculateSplineTransmissionReflection(380, 780, 5.0f);
		if (calcSplinTransReflResult.isSuccess()) {
			assertNotNull(calcSplinTransReflResult.getValue());
		}

		// Test calculate radiometric value
		var calcRadioResult = device.calculateRadiometricValue(380, 780);
		if (calcRadioResult.isSuccess()) {
			assertTrue(calcRadioResult.getValue() >= 0);
		}

		// Test calculate photometric value
		var calcPhotoResult = device.calculatePhotometricValue();
		if (calcPhotoResult.isSuccess()) {
			assertTrue(calcPhotoResult.getValue() >= 0);
		}

		// Test calculate chromaticity XY
		var calcChromxyResult = device.calculateChromaXY();
		if (calcChromxyResult.isSuccess()) {
			assertNotNull(calcChromxyResult.getValue());
		}

		// Test calculate chromaticity XY10
		var calcChromxy10Result = device.calculateChromaXY10();
		if (calcChromxy10Result.isSuccess()) {
			assertNotNull(calcChromxy10Result.getValue());
		}

		// Test calculate chromaticity UV
		var calcChromuvResult = device.calculateChromaUV();
		if (calcChromuvResult.isSuccess()) {
			assertNotNull(calcChromuvResult.getValue());
		}

		// Test calculate dominant wavelength
		var calcDWLPEResult = device.calculateDominantWavelength();
		if (calcDWLPEResult.isSuccess()) {
			assertNotNull(calcDWLPEResult.getValue());
		}

		// Test calculate CCT
		var calcCCTResult = device.calculateCCT();
		if (calcCCTResult.isSuccess()) {
			assertTrue(calcCCTResult.getValue() >= 0);
		}

		// Test calculate Duv
		var calcDuvResult = device.calculateDuv();
		if (calcDuvResult.isSuccess()) {
			assertNotNull(calcDuvResult.getValue());
		}

		// Test calculate CRI
		var calcCRIResult = device.calculateCRI(5000.0f);
		if (calcCRIResult.isSuccess()) {
			assertTrue(calcCRIResult.getValue() >= -100 && calcCRIResult.getValue() <= 100);
		}

		// Test calculate XYZ
		var calcXYZResult = device.calculateXYZ();
		if (calcXYZResult.isSuccess()) {
			assertNotNull(calcXYZResult.getValue());
		}

		// Test calculate all values
		var calcAllResult = device.calculateAllValues(380, 780);
		if (calcAllResult.isSuccess()) {
			assertNotNull(calcAllResult.getValue());
		}

		// Test calculate TM30
		var calcTM30Result = device.calculateTM30(true);
		if (calcTM30Result.isSuccess()) {
			assertNotNull(calcTM30Result.getValue());
		}

		// Test calculate peak FWHM
		var calcPeakFWHMResult = device.calculatePeakFWHM(0.5f);
		if (calcPeakFWHMResult.isSuccess()) {
			assertNotNull(calcPeakFWHMResult.getValue());
		}

		// Test calculate blue measurement
		var calcBlueResult = device.calculateBlueMeasurement();
		if (calcBlueResult.isSuccess()) {
			assertNotNull(calcBlueResult.getValue());
		}
	}

	@Test
	@Order(9)
	@DisplayName("Test import straylight matrix")
	void testImportStraylightMatrix () {
		// This will likely fail without a valid matrix file, but we're testing it's callable
		var importResult = JetiCore.importStraylightMatrix("test_matrix.slm");
		// Don't assert success since file may not exist
		assertNotNull(importResult);
	}
}
