
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;

@DisplayName("JetiSpectroEx Integration Tests")
public class SpectroExTest {
	private SpectroEx spectroEx;

	@BeforeEach
	void setUp () {
		JetiSDK.initialize();

		Result<Integer> deviceCount = SpectroEx.getDeviceCount();
		assumeTrue(deviceCount.isSuccess() && deviceCount.getValue() > 0,
			"No spectro ex devices available for testing " + deviceCount);

		Result<SpectroEx> result = SpectroEx.openDevice(0);
		assumeTrue(result.isSuccess(), "Could not open spectro ex device " + result);

		spectroEx = result.getValue();
	}

	@AfterEach
	void tearDown () {
		if (spectroEx != null && !spectroEx.isClosed()) spectroEx.close();
	}

	@Test
	@DisplayName("Get device information")
	void testGetDeviceInfo () {
		Result<DeviceSerials> result = SpectroEx.getDeviceSerials(0);
		if (result.isSuccess()) {
			DeviceSerials serials = result.getValue();
			assertNotNull(serials.electronics());
			assertNotNull(serials.spectrometer());
			assertNotNull(serials.device());
		}

		Result<DllVersion> versionResult = SpectroEx.getDllVersion();
		assertTrue(versionResult.isSuccess(), versionResult.toString());
		assertNotNull(versionResult.getValue());
	}

	@Test
	@DisplayName("Test dark measurement cycle")
	void testDarkMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 3;

		// Start dark measurement
		Result<Boolean> result = spectroEx.startDarkMeasurement(integrationTime, averageCount);
		assertTrue(result.isSuccess(), result.toString());

		// Wait for measurement to complete
		waitForMeasurementCompletion();

		// Get dark pixel data
		Result<int[]> pixelResult = spectroEx.getDarkPixelData();
		assertTrue(pixelResult.isSuccess(), pixelResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, pixelResult.getValue().length);

		// Get dark wave data
		int beginWavelength = 400;
		int endWavelength = 700;
		float stepSize = 5.0f;
		int expectedSize = (int)((endWavelength - beginWavelength) / stepSize + 1);

		Result<float[]> waveResult = spectroEx.getDarkWaveData(beginWavelength, endWavelength, stepSize);
		assertTrue(waveResult.isSuccess(), waveResult.toString());
		assertEquals(expectedSize, waveResult.getValue().length);

		// Dark data should be relatively low values
		int[] pixelData = pixelResult.getValue();
		for (int value : pixelData) {
			assertTrue(value >= 0, "Dark pixel values should be non-negative");
		}
	}

	@Test
	@DisplayName("Test light measurement cycle")
	void testLightMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 1;

		// Start light measurement
		Result<Boolean> result = spectroEx.startLightMeasurement(integrationTime, averageCount);
		assertTrue(result.isSuccess(), result.toString());

		// Wait for measurement to complete
		waitForMeasurementCompletion();

		// Get light pixel data
		Result<int[]> pixelResult = spectroEx.getLightPixelData();
		assertTrue(pixelResult.isSuccess(), pixelResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, pixelResult.getValue().length);

		// Get light wave data
		int beginWavelength = 380;
		int endWavelength = 780;
		float stepSize = 1.0f;
		int expectedSize = (int)((endWavelength - beginWavelength) / stepSize + 1);

		Result<float[]> waveResult = spectroEx.getLightWaveData(beginWavelength, endWavelength, stepSize);
		assertTrue(waveResult.isSuccess(), waveResult.toString());
		assertEquals(expectedSize, waveResult.getValue().length);

		// Light data should contain some positive values
		float[] waveData = waveResult.getValue();
		boolean hasPositiveValues = false;
		for (float value : waveData) {
			if (value > 0) {
				hasPositiveValues = true;
				break;
			}
		}
		assertTrue(hasPositiveValues, "Light wave data should contain some positive values");
	}

	@Test
	@DisplayName("Test prepare light measurement")
	void testPrepareLightMeasurement () {
		float integrationTime = 50.0f;
		int averageCount = 2;

		Result<Boolean> result = spectroEx.prepareLightMeasurement(integrationTime, averageCount);
		assertTrue(result.isSuccess(), result.toString());
	}

	@Test
	@DisplayName("Test reference measurement cycle")
	void testReferenceMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 1;

		// Start reference measurement
		Result<Boolean> result = spectroEx.startReferenceMeasurement(integrationTime, averageCount);
		assertTrue(result.isSuccess(), result.toString());

		// Wait for measurement to complete
		waitForMeasurementCompletion();

		// Get reference pixel data
		Result<int[]> pixelResult = spectroEx.getReferencePixelData();
		assertTrue(pixelResult.isSuccess(), pixelResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, pixelResult.getValue().length);

		// Get reference wave data
		int beginWavelength = 450;
		int endWavelength = 650;
		float stepSize = 2.0f;
		int expectedSize = (int)((endWavelength - beginWavelength) / stepSize + 1);

		Result<float[]> waveResult = spectroEx.getReferenceWaveData(beginWavelength, endWavelength, stepSize);
		// BOZO - Command not supported or invalid argument?
		assertTrue(waveResult.isSuccess(), waveResult.toString());
		assertEquals(expectedSize, waveResult.getValue().length);
	}

	@Test
	@DisplayName("Test prepare reference measurement")
	void testPrepareReferenceMeasurement () {
		float integrationTime = 75.0f;
		int averageCount = 5;

		Result<Boolean> result = spectroEx.prepareReferenceMeasurement(integrationTime, averageCount);
		assertTrue(result.isSuccess(), result.toString());
	}

	@Test
	@DisplayName("Test transmission/reflection measurement cycle")
	void testTransmissionReflectionMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 1;

		// Start transmission/reflection measurement
		Result<Boolean> result = spectroEx.startSampleMeasurement(integrationTime, averageCount);
		// BOZO - Command not supported or invalid argument?
		assertTrue(result.isSuccess(), result.toString());

		// Wait for measurement to complete
		waitForMeasurementCompletion();

		// Get transmission/reflection pixel data
		Result<int[]> pixelResult = spectroEx.getSamplePixelData();
		assertTrue(pixelResult.isSuccess(), pixelResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, pixelResult.getValue().length);

		// Get transmission/reflection wave data
		int beginWavelength = 400;
		int endWavelength = 700;
		float stepSize = 5.0f;
		int expectedSize = (int)((endWavelength - beginWavelength) / stepSize + 1);

		Result<float[]> waveResult = spectroEx.getSampleWaveData(beginWavelength, endWavelength, stepSize);
		assertTrue(waveResult.isSuccess(), waveResult.toString());
		assertEquals(expectedSize, waveResult.getValue().length);
	}

	@Test
	@DisplayName("Test prepare transmission/reflection measurement")
	void testPrepareTransmissionReflectionMeasurement () {
		float integrationTime = 150.0f;
		int averageCount = 3;

		Result<Boolean> result = spectroEx.prepareSampleMeasurement(integrationTime, averageCount);
		// BOZO - Command not supported or invalid argument?
		assertTrue(result.isSuccess(), result.toString());
	}

	@Test
	@DisplayName("Test image measurement cycle")
	void testImageMeasurementCycle () {
		float integrationTime = 100.0f;

		// Test dark image measurement
		Result<Boolean> result = spectroEx.startDarkImageMeasurement(integrationTime);
		// BOZO - Command not supported or invalid argument?
		assertTrue(result.isSuccess(), result.toString());

		waitForMeasurementCompletion();

		Result<short[]> darkImageResult = spectroEx.getDarkImageData();
		assertTrue(darkImageResult.isSuccess(), darkImageResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, darkImageResult.getValue().length);

		// Test light image measurement
		result = spectroEx.startLightImageMeasurement(integrationTime);
		assertTrue(result.isSuccess(), result.toString());

		waitForMeasurementCompletion();

		Result<short[]> lightImageResult = spectroEx.getLightImageData();
		assertTrue(lightImageResult.isSuccess(), lightImageResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, lightImageResult.getValue().length);

		// Light image should generally have higher values than dark image
		short[] darkData = darkImageResult.getValue();
		short[] lightData = lightImageResult.getValue();

		for (int i = 0; i < darkData.length; i++) {
			assertTrue(darkData[i] >= 0, "Dark image values should be non-negative");
			assertTrue(lightData[i] >= 0, "Light image values should be non-negative");
		}
	}

	@Test
	@DisplayName("Test channel measurement cycle")
	void testChannelMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 2;

		// Test channel dark measurement
		Result<Boolean> result = spectroEx.startChannelDarkMeasurement(integrationTime, averageCount);
		// BOZO - Command not supported or invalid argument?
		assertTrue(result.isSuccess(), result.toString());

		waitForMeasurementCompletion();

		Result<short[]> darkChannelResult = spectroEx.getChannelDarkData();
		assertTrue(darkChannelResult.isSuccess(), darkChannelResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, darkChannelResult.getValue().length);

		// Test channel light measurement
		result = spectroEx.startChannelLightMeasurement(integrationTime, averageCount);
		assertTrue(result.isSuccess(), result.toString());

		waitForMeasurementCompletion();

		Result<short[]> lightChannelResult = spectroEx.getChannelLightData();
		assertTrue(lightChannelResult.isSuccess(), lightChannelResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, lightChannelResult.getValue().length);

		// Verify data integrity
		short[] darkData = darkChannelResult.getValue();
		short[] lightData = lightChannelResult.getValue();
		for (int i = 0; i < darkData.length; i++) {
			assertTrue(darkData[i] >= 0, "Channel dark values should be non-negative");
			assertTrue(lightData[i] >= 0, "Channel light values should be non-negative");
		}
	}

	@Test
	@DisplayName("Test continuous measurement cycle")
	void testContinuousMeasurementCycle () {
		float interval = 500.0f; // ms
		int count = 2;

		// Test continuous dark measurement
		Result<Boolean> result = spectroEx.startContinuousDarkMeasurement(interval, count);
		// BOZO - Command not supported or invalid argument?
		assertTrue(result.isSuccess(), result.toString());

		// Wait longer for continuous measurement
		waitForMeasurementCompletionLong();

		Result<short[]> dataResult = spectroEx.getContinuousDarkData();
		assertTrue(dataResult.isSuccess(), dataResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, dataResult.getValue().length);

		// Test continuous light measurement
		Result<Boolean> startResult = spectroEx.startContinuousLightMeasurement(interval, count);
		assertTrue(startResult.isSuccess(), startResult.toString());

		waitForMeasurementCompletionLong();

		dataResult = spectroEx.getContinuousLightData();
		assertTrue(dataResult.isSuccess(), dataResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, dataResult.getValue().length);
	}

	@Test
	@DisplayName("Test continuous channel measurement cycle")
	void testContinuousChannelMeasurementCycle () {
		float interval = 300.0f;
		int count = 1;

		// Test continuous channel dark measurement
		Result<Boolean> result = spectroEx.startContinuousChannelDarkMeasurement(interval, count);
		// BOZO - Command not supported or invalid argument?
		assertTrue(result.isSuccess(), result.toString());

		waitForMeasurementCompletionLong();

		Result<short[]> dataResult = spectroEx.getContinuousChannelDarkData();
		assertTrue(dataResult.isSuccess(), dataResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, dataResult.getValue().length);

		// Test continuous channel light measurement
		Result<Boolean> startResult = spectroEx.startContinuousChannelLightMeasurement(interval, count);
		assertTrue(startResult.isSuccess(), startResult.toString());

		waitForMeasurementCompletionLong();

		dataResult = spectroEx.getContinuousChannelLightData();
		assertTrue(dataResult.isSuccess(), dataResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, dataResult.getValue().length);
	}

	@Test
	@DisplayName("Test device status and control")
	void testDeviceStatusAndControl () {
		// Test getting spectro status
		Result<Boolean> result = spectroEx.getMeasurementStatus();
		assertTrue(result.isSuccess(), result.toString());

		// Start a measurement and then break it
		Result<Boolean> startResult = spectroEx.startDarkMeasurement(100.0f, 1);
		assertTrue(startResult.isSuccess(), startResult.toString());

		// Break the measurement
		Result<Boolean> breakResult = spectroEx.breakMeasurement();
		assertTrue(breakResult.isSuccess(), breakResult.toString());

		// Check status after break
		Result<Boolean> statusAfterBreak = spectroEx.getMeasurementStatus();
		assertTrue(statusAfterBreak.isSuccess(), statusAfterBreak.toString());
	}

	@Test
	@DisplayName("Test device parameters")
	void testDeviceParameters () {
		// Test getting pixel count
		Result<Integer> result = spectroEx.getPixelCount();
		assertTrue(result.isSuccess(), result.toString());
		assertTrue(result.getValue() > 0, "Pixel count should be positive");

		// Test getting spectro integration time
		Result<Float> tintResult = spectroEx.getIntegrationTime();
		assertTrue(tintResult.isSuccess(), tintResult.toString());
		assertTrue(tintResult.getValue() > 0, "Integration time should be positive");
	}

	@Test
	@DisplayName("Test wave data with different wavelength ranges and step sizes")
	void testWaveDataVariations () {
		// Start a light measurement first
		Result<Boolean> result = spectroEx.startLightMeasurement(100.0f, 1);
		assertTrue(result.isSuccess(), result.toString());
		waitForMeasurementCompletion();

		// Test different wavelength ranges
		int[] beginWavelengths = {380, 400};
		int[] endWavelengths = {780, 700};
		float[] stepSizes = {1.0f, 5.0f};

		for (int i = 0; i < beginWavelengths.length; i++) {
			int begin = beginWavelengths[i];
			int end = endWavelengths[i];
			float step = stepSizes[i];
			int expectedSize = (int)((end - begin) / step + 1);

			Result<float[]> waveResult = spectroEx.getLightWaveData(begin, end, step);
			assertTrue(waveResult.isSuccess(),
				"Failed for range " + begin + "-" + end + " step " + step + ": " + waveResult.toString());
			assertEquals(expectedSize, waveResult.getValue().length,
				"Wrong array size for range " + begin + "-" + end + " step " + step);
		}
	}

	@Test
	@DisplayName("Test parameter validation for measurement methods")
	void testParameterValidation () {
		// Test negative integration time
		Result<Boolean> result = spectroEx.startDarkMeasurement(-1.0f, 1);
		assertFalse(result.isSuccess(), "Negative integration time should be rejected");
		assertEquals(JetiSDK.INVALID_ARGUMENT, result.getErrorCode());

		// Test invalid average count
		result = spectroEx.startLightMeasurement(100.0f, 0);
		assertFalse(result.isSuccess(), "Zero average count should be rejected");
		assertEquals(JetiSDK.INVALID_ARGUMENT, result.getErrorCode());

		result = spectroEx.startReferenceMeasurement(100.0f, -1);
		assertFalse(result.isSuccess(), "Negative average count should be rejected");
		assertEquals(JetiSDK.INVALID_ARGUMENT, result.getErrorCode());

		// Test invalid continuous measurement parameters
		result = spectroEx.startContinuousDarkMeasurement(0.0f, 1);
		assertFalse(result.isSuccess(), "Zero interval should be rejected");
		assertEquals(JetiSDK.INVALID_ARGUMENT, result.getErrorCode());

		result = spectroEx.startContinuousLightMeasurement(100.0f, 0);
		assertFalse(result.isSuccess(), "Zero count should be rejected");
		assertEquals(JetiSDK.INVALID_ARGUMENT, result.getErrorCode());

		// Test invalid wavelength parameters
		Result<float[]> waveResult = spectroEx.getDarkWaveData(-1, 700, 5.0f);
		assertFalse(waveResult.isSuccess(), "Negative begin wavelength should be rejected");
		assertEquals(JetiSDK.INVALID_ARGUMENT, waveResult.getErrorCode());

		waveResult = spectroEx.getLightWaveData(700, 400, 5.0f);
		assertFalse(waveResult.isSuccess(), "Begin wavelength >= end wavelength should be rejected");
		assertEquals(JetiSDK.INVALID_ARGUMENT, waveResult.getErrorCode());

		waveResult = spectroEx.getReferenceWaveData(400, 700, 0.0f);
		assertFalse(waveResult.isSuccess(), "Zero step size should be rejected");
		assertEquals(JetiSDK.INVALID_ARGUMENT, waveResult.getErrorCode());
	}

	private void waitForMeasurementCompletion () {
		boolean measuring = true;
		int attempts = 0;
		while (measuring && attempts < 100) { // 10 second timeout
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("Test interrupted");
			}

			Result<Boolean> result = spectroEx.getMeasurementStatus();
			if (result.isSuccess()) {
				measuring = result.getValue();
			} else {
				// If status check fails, assume measurement is complete
				measuring = false;
			}
			attempts++;
		}

		if (measuring) {
			// Try to break the measurement if it's stuck
			spectroEx.breakMeasurement();
			fail("Measurement should complete within timeout");
		}
	}

	private void waitForMeasurementCompletionLong () {
		boolean measuring = true;
		int attempts = 0;
		while (measuring && attempts < 200) { // 20 second timeout for continuous measurements
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("Test interrupted");
			}

			Result<Boolean> result = spectroEx.getMeasurementStatus();
			if (result.isSuccess()) {
				measuring = result.getValue();
			} else {
				measuring = false;
			}
			attempts++;
		}

		if (measuring) {
			spectroEx.breakMeasurement();
			fail("Long measurement should complete within timeout");
		}
	}
}
