
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JetiSpectroEx Integration Tests")
public class JetiSpectroExTest {
	private JetiSpectroEx spectroEx;

	@BeforeEach
	void setUp () {
		JetiSDK.initialize();

		JetiResult<Integer> deviceCount = JetiSpectroEx.getDeviceCount();
		assumeTrue(deviceCount.isSuccess() && deviceCount.getValue() > 0,
			"No spectro ex devices available for testing " + deviceCount);

		JetiResult<JetiSpectroEx> deviceResult = JetiSpectroEx.openDevice(0);
		assumeTrue(deviceResult.isSuccess(), "Could not open spectro ex device " + deviceResult);

		spectroEx = deviceResult.getValue();
	}

	@AfterEach
	void tearDown () {
		if (spectroEx != null && !spectroEx.isClosed()) spectroEx.close();
	}

	@Test
	@DisplayName("Get device information")
	void testGetDeviceInfo () {
		JetiResult<String[]> serialsResult = JetiSpectroEx.getDeviceSerials(0);
		if (serialsResult.isSuccess()) {
			String[] serials = serialsResult.getValue();
			assertEquals(3, serials.length);
			assertNotNull(serials[0]); // Board serial
			assertNotNull(serials[1]); // Spectrometer serial
			assertNotNull(serials[2]); // Device serial
		}

		JetiResult<String> versionResult = JetiSpectroEx.getDllVersion();
		assertTrue(versionResult.isSuccess(), versionResult.toString());
		assertNotNull(versionResult.getValue());
		assertTrue(versionResult.getValue().contains("."));
	}

	@Test
	@DisplayName("Test dark measurement cycle")
	void testDarkMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 3;

		// Start dark measurement
		JetiResult<Boolean> startResult = spectroEx.startDarkMeasurement(integrationTime, averageCount);
		assertTrue(startResult.isSuccess(), startResult.toString());

		// Wait for measurement to complete
		waitForMeasurementCompletion();

		// Get dark pixel data
		JetiResult<int[]> pixelResult = spectroEx.getDarkPixelData();
		assertTrue(pixelResult.isSuccess(), pixelResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, pixelResult.getValue().length);

		// Get dark wave data
		int beginWavelength = 400;
		int endWavelength = 700;
		float stepSize = 5.0f;
		int expectedSize = (int)((endWavelength - beginWavelength) / stepSize + 1);

		JetiResult<float[]> waveResult = spectroEx.getDarkWaveData(beginWavelength, endWavelength, stepSize);
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
		JetiResult<Boolean> startResult = spectroEx.startLightMeasurement(integrationTime, averageCount);
		assertTrue(startResult.isSuccess(), startResult.toString());

		// Wait for measurement to complete
		waitForMeasurementCompletion();

		// Get light pixel data
		JetiResult<int[]> pixelResult = spectroEx.getLightPixelData();
		assertTrue(pixelResult.isSuccess(), pixelResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, pixelResult.getValue().length);

		// Get light wave data
		int beginWavelength = 380;
		int endWavelength = 780;
		float stepSize = 1.0f;
		int expectedSize = (int)((endWavelength - beginWavelength) / stepSize + 1);

		JetiResult<float[]> waveResult = spectroEx.getLightWaveData(beginWavelength, endWavelength, stepSize);
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

		JetiResult<Boolean> prepareResult = spectroEx.prepareLightMeasurement(integrationTime, averageCount);
		assertTrue(prepareResult.isSuccess(), prepareResult.toString());
	}

	@Test
	@DisplayName("Test reference measurement cycle")
	void testReferenceMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 1;

		// Start reference measurement
		JetiResult<Boolean> startResult = spectroEx.startReferenceMeasurement(integrationTime, averageCount);
		assertTrue(startResult.isSuccess(), startResult.toString());

		// Wait for measurement to complete
		waitForMeasurementCompletion();

		// Get reference pixel data
		JetiResult<int[]> pixelResult = spectroEx.getReferencePixelData();
		assertTrue(pixelResult.isSuccess(), pixelResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, pixelResult.getValue().length);

		// Get reference wave data
		int beginWavelength = 450;
		int endWavelength = 650;
		float stepSize = 2.0f;
		int expectedSize = (int)((endWavelength - beginWavelength) / stepSize + 1);

		JetiResult<float[]> waveResult = spectroEx.getReferenceWaveData(beginWavelength, endWavelength, stepSize);
		// BOZO - Command not supported or invalid argument?
		assertTrue(waveResult.isSuccess(), waveResult.toString());
		assertEquals(expectedSize, waveResult.getValue().length);
	}

	@Test
	@DisplayName("Test prepare reference measurement")
	void testPrepareReferenceMeasurement () {
		float integrationTime = 75.0f;
		int averageCount = 5;

		JetiResult<Boolean> prepareResult = spectroEx.prepareReferenceMeasurement(integrationTime, averageCount);
		assertTrue(prepareResult.isSuccess(), prepareResult.toString());
	}

	@Test
	@DisplayName("Test transmission/reflection measurement cycle")
	void testTransmissionReflectionMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 1;

		// Start transmission/reflection measurement
		JetiResult<Boolean> startResult = spectroEx.startTransmissionReflectionMeasurement(integrationTime, averageCount);
		// BOZO - Command not supported or invalid argument?
		assertTrue(startResult.isSuccess(), startResult.toString());

		// Wait for measurement to complete
		waitForMeasurementCompletion();

		// Get transmission/reflection pixel data
		JetiResult<int[]> pixelResult = spectroEx.getTransmissionReflectionPixelData();
		assertTrue(pixelResult.isSuccess(), pixelResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, pixelResult.getValue().length);

		// Get transmission/reflection wave data
		int beginWavelength = 400;
		int endWavelength = 700;
		float stepSize = 5.0f;
		int expectedSize = (int)((endWavelength - beginWavelength) / stepSize + 1);

		JetiResult<float[]> waveResult = spectroEx.getTransmissionReflectionWaveData(beginWavelength, endWavelength, stepSize);
		assertTrue(waveResult.isSuccess(), waveResult.toString());
		assertEquals(expectedSize, waveResult.getValue().length);
	}

	@Test
	@DisplayName("Test prepare transmission/reflection measurement")
	void testPrepareTransmissionReflectionMeasurement () {
		float integrationTime = 150.0f;
		int averageCount = 3;

		JetiResult<Boolean> prepareResult = spectroEx.prepareTransmissionReflectionMeasurement(integrationTime, averageCount);
		// BOZO - Command not supported or invalid argument?
		assertTrue(prepareResult.isSuccess(), prepareResult.toString());
	}

	@Test
	@DisplayName("Test image measurement cycle")
	void testImageMeasurementCycle () {
		float integrationTime = 100.0f;

		// Test dark image measurement
		JetiResult<Boolean> startDarkResult = spectroEx.startDarkImageMeasurement(integrationTime);
		// BOZO - Command not supported or invalid argument?
		assertTrue(startDarkResult.isSuccess(), startDarkResult.toString());

		waitForMeasurementCompletion();

		JetiResult<short[]> darkImageResult = spectroEx.getDarkImageData();
		assertTrue(darkImageResult.isSuccess(), darkImageResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, darkImageResult.getValue().length);

		// Test light image measurement
		JetiResult<Boolean> startLightResult = spectroEx.startLightImageMeasurement(integrationTime);
		assertTrue(startLightResult.isSuccess(), startLightResult.toString());

		waitForMeasurementCompletion();

		JetiResult<short[]> lightImageResult = spectroEx.getLightImageData();
		assertTrue(lightImageResult.isSuccess(), lightImageResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, lightImageResult.getValue().length);

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
		JetiResult<Boolean> startDarkResult = spectroEx.startChannelDarkMeasurement(integrationTime, averageCount);
		// BOZO - Command not supported or invalid argument?
		assertTrue(startDarkResult.isSuccess(), startDarkResult.toString());

		waitForMeasurementCompletion();

		JetiResult<short[]> darkChannelResult = spectroEx.getChannelDarkData();
		assertTrue(darkChannelResult.isSuccess(), darkChannelResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, darkChannelResult.getValue().length);

		// Test channel light measurement
		JetiResult<Boolean> startLightResult = spectroEx.startChannelLightMeasurement(integrationTime, averageCount);
		assertTrue(startLightResult.isSuccess(), startLightResult.toString());

		waitForMeasurementCompletion();

		JetiResult<short[]> lightChannelResult = spectroEx.getChannelLightData();
		assertTrue(lightChannelResult.isSuccess(), lightChannelResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, lightChannelResult.getValue().length);

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
		JetiResult<Boolean> startDarkResult = spectroEx.startContinuousDarkMeasurement(interval, count);
		// BOZO - Command not supported or invalid argument?
		assertTrue(startDarkResult.isSuccess(), startDarkResult.toString());

		// Wait longer for continuous measurement
		waitForMeasurementCompletionLong();

		JetiResult<short[]> darkResult = spectroEx.getContinuousDarkData();
		assertTrue(darkResult.isSuccess(), darkResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, darkResult.getValue().length);

		// Test continuous light measurement
		JetiResult<Boolean> startLightResult = spectroEx.startContinuousLightMeasurement(interval, count);
		assertTrue(startLightResult.isSuccess(), startLightResult.toString());

		waitForMeasurementCompletionLong();

		JetiResult<short[]> lightResult = spectroEx.getContinuousLightData();
		assertTrue(lightResult.isSuccess(), lightResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, lightResult.getValue().length);
	}

	@Test
	@DisplayName("Test continuous channel measurement cycle")
	void testContinuousChannelMeasurementCycle () {
		float interval = 300.0f;
		int count = 1;

		// Test continuous channel dark measurement
		JetiResult<Boolean> startDarkResult = spectroEx.startContinuousChannelDarkMeasurement(interval, count);
		// BOZO - Command not supported or invalid argument?
		assertTrue(startDarkResult.isSuccess(), startDarkResult.toString());

		waitForMeasurementCompletionLong();

		JetiResult<short[]> darkResult = spectroEx.getContinuousChannelDarkData();
		assertTrue(darkResult.isSuccess(), darkResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, darkResult.getValue().length);

		// Test continuous channel light measurement
		JetiResult<Boolean> startLightResult = spectroEx.startContinuousChannelLightMeasurement(interval, count);
		assertTrue(startLightResult.isSuccess(), startLightResult.toString());

		waitForMeasurementCompletionLong();

		JetiResult<short[]> lightResult = spectroEx.getContinuousChannelLightData();
		assertTrue(lightResult.isSuccess(), lightResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, lightResult.getValue().length);
	}

	@Test
	@DisplayName("Test device status and control")
	void testDeviceStatusAndControl () {
		// Test getting spectro status
		JetiResult<Boolean> statusResult = spectroEx.getMeasurementStatus();
		assertTrue(statusResult.isSuccess(), statusResult.toString());

		// Start a measurement and then break it
		JetiResult<Boolean> startResult = spectroEx.startDarkMeasurement(100.0f, 1);
		assertTrue(startResult.isSuccess(), startResult.toString());

		// Break the measurement
		JetiResult<Boolean> breakResult = spectroEx.breakMeasurement();
		assertTrue(breakResult.isSuccess(), breakResult.toString());

		// Check status after break
		JetiResult<Boolean> statusAfterBreak = spectroEx.getMeasurementStatus();
		assertTrue(statusAfterBreak.isSuccess(), statusAfterBreak.toString());
	}

	@Test
	@DisplayName("Test device parameters")
	void testDeviceParameters () {
		// Test getting pixel count
		JetiResult<Integer> pixelCountResult = spectroEx.getPixelCount();
		assertTrue(pixelCountResult.isSuccess(), pixelCountResult.toString());
		assertTrue(pixelCountResult.getValue() > 0, "Pixel count should be positive");

		// Test getting spectro integration time
		JetiResult<Float> tintResult = spectroEx.getIntegrationTime();
		assertTrue(tintResult.isSuccess(), tintResult.toString());
		assertTrue(tintResult.getValue() > 0, "Integration time should be positive");
	}

	@Test
	@DisplayName("Test wave data with different wavelength ranges and step sizes")
	void testWaveDataVariations () {
		// Start a light measurement first
		JetiResult<Boolean> startResult = spectroEx.startLightMeasurement(100.0f, 1);
		assertTrue(startResult.isSuccess(), startResult.toString());
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

			JetiResult<float[]> waveResult = spectroEx.getLightWaveData(begin, end, step);
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
		JetiResult<Boolean> result = spectroEx.startDarkMeasurement(-1.0f, 1);
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
		JetiResult<float[]> waveResult = spectroEx.getDarkWaveData(-1, 700, 5.0f);
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

			JetiResult<Boolean> statusResult = spectroEx.getMeasurementStatus();
			if (statusResult.isSuccess()) {
				measuring = statusResult.getValue();
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

			JetiResult<Boolean> statusResult = spectroEx.getMeasurementStatus();
			if (statusResult.isSuccess()) {
				measuring = statusResult.getValue();
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
