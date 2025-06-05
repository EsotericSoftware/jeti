
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;

public class SpectroExTest extends JetiTest {
	private SpectroEx spectroEx;

	@BeforeEach
	void setUp () {
		assumeTrue(SpectroEx.getDeviceCount() > 0, "No spectro ex devices available for testing");
		spectroEx = SpectroEx.openDevice(0);
	}

	@AfterEach
	void tearDown () {
		if (spectroEx != null && !spectroEx.isClosed()) spectroEx.close();
	}

	@Test
	@DisplayName("Get device information")
	void testGetDeviceInfo () {
		DeviceSerials serials = SpectroEx.getDeviceSerials(0);
		assertNotNull(serials.electronics());
		assertNotNull(serials.spectrometer());
		assertNotNull(serials.device());

		DllVersion version = SpectroEx.getDllVersion();
		assertNotNull(version);
	}

	@Test
	@DisplayName("Test dark measurement cycle")
	void testDarkMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 3;
		spectroEx.startDarkMeasurement(integrationTime, averageCount);
		waitForMeasurementCompletion();

		int pixelCount = spectroEx.getPixelCount();
		int[] pixelData = spectroEx.getDarkPixelData(pixelCount);
		assertEquals(pixelCount, pixelData.length);

		int beginWavelength = 400;
		int endWavelength = 700;
		float stepSize = 5.0f;
		int expectedSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		float[] waveData = spectroEx.getDarkWaveData(beginWavelength, endWavelength, stepSize);
		assertEquals(expectedSize, waveData.length);

		for (int value : pixelData)
			assertTrue(value >= 0, "Dark pixel values should be non-negative");
	}

	@Test
	@DisplayName("Test light measurement cycle")
	void testLightMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 1;
		spectroEx.startLightMeasurement(integrationTime, averageCount);
		waitForMeasurementCompletion();

		int pixelCount = spectroEx.getPixelCount();
		int[] pixelData = spectroEx.getLightPixelData(pixelCount);
		assertEquals(pixelCount, pixelData.length);

		int beginWavelength = 380;
		int endWavelength = 780;
		float stepSize = 1.0f;
		int expectedSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		float[] waveData = spectroEx.getLightWaveData(beginWavelength, endWavelength, stepSize);
		assertEquals(expectedSize, waveData.length);

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
		spectroEx.prepareLightMeasurement(integrationTime, averageCount);
	}

	@Test
	@DisplayName("Test reference measurement cycle")
	void testReferenceMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 1;
		spectroEx.startReferenceMeasurement(integrationTime, averageCount);
		waitForMeasurementCompletion();

		int pixelCount = spectroEx.getPixelCount();
		int[] pixelData = spectroEx.getReferencePixelData(pixelCount);
		assertEquals(pixelCount, pixelData.length);

		int beginWavelength = 450;
		int endWavelength = 650;
		float stepSize = 2.0f;
		int expectedSize = (int)((endWavelength - beginWavelength) / stepSize + 1);

		// BOZO - Not supported?
		float[] waveData = spectroEx.getReferenceWaveData(beginWavelength, endWavelength, stepSize);
		assertEquals(expectedSize, waveData.length);
	}

	@Test
	@DisplayName("Test prepare reference measurement")
	void testPrepareReferenceMeasurement () {
		float integrationTime = 75.0f;
		int averageCount = 5;
		spectroEx.prepareReferenceMeasurement(integrationTime, averageCount);
	}

	@Test
	@DisplayName("Test sample measurement cycle")
	void testSampleMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 1;

		// BOZO - Not supported?
		spectroEx.startSampleMeasurement(integrationTime, averageCount);
		waitForMeasurementCompletion();

		int pixelCount = spectroEx.getPixelCount();
		int[] pixelData = spectroEx.getSamplePixelData(pixelCount);
		assertEquals(pixelCount, pixelData.length);

		// Get sample wave data
		int beginWavelength = 400;
		int endWavelength = 700;
		float stepSize = 5.0f;
		int expectedSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		float[] waveData = spectroEx.getSampleWaveData(beginWavelength, endWavelength, stepSize);
		assertEquals(expectedSize, waveData.length);
	}

	@Test
	@DisplayName("Test prepare sample measurement")
	void testPrepareSampleMeasurement () {
		float integrationTime = 150.0f;
		int averageCount = 3;

		// BOZO - Not supported?
		spectroEx.prepareSampleMeasurement(integrationTime, averageCount);
	}

	@Test
	@DisplayName("Test image measurement cycle")
	void testImageMeasurementCycle () {
		float integrationTime = 100.0f;

		// BOZO - Not supported?
		spectroEx.startDarkImageMeasurement(integrationTime);

		waitForMeasurementCompletion();
		short[] darkImageData = spectroEx.getDarkImageData();
		assertEquals(JetiSDK.SPECTRUM_SIZE, darkImageData.length);

		spectroEx.startLightImageMeasurement(integrationTime);
		waitForMeasurementCompletion();
		short[] lightImageData = spectroEx.getLightImageData();
		assertEquals(JetiSDK.SPECTRUM_SIZE, lightImageData.length);

		for (int i = 0; i < darkImageData.length; i++) {
			assertTrue(darkImageData[i] >= 0, "Dark image values should be non-negative");
			assertTrue(lightImageData[i] >= 0, "Light image values should be non-negative");
		}
	}

	@Test
	@DisplayName("Test channel measurement cycle")
	void testChannelMeasurementCycle () {
		float integrationTime = 100.0f;
		int averageCount = 2;

		// BOZO - Not supported?
		spectroEx.startChannelDarkMeasurement(integrationTime, averageCount);

		waitForMeasurementCompletion();
		short[] darkData = spectroEx.getChannelDarkData();
		assertEquals(JetiSDK.SPECTRUM_SIZE, darkData.length);

		spectroEx.startChannelLightMeasurement(integrationTime, averageCount);
		waitForMeasurementCompletion();
		short[] lightData = spectroEx.getChannelLightData();
		assertEquals(JetiSDK.SPECTRUM_SIZE, lightData.length);

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

		// BOZO - Not supported?
		spectroEx.startContinuousDarkMeasurement(interval, count);

		waitForMeasurementCompletionLong();
		short[] darkData = spectroEx.getContinuousDarkData();
		assertEquals(JetiSDK.SPECTRUM_SIZE, darkData.length);

		spectroEx.startContinuousLightMeasurement(interval, count);
		waitForMeasurementCompletionLong();
		short[] lightData = spectroEx.getContinuousLightData();
		assertEquals(JetiSDK.SPECTRUM_SIZE, lightData.length);
	}

	@Test
	@DisplayName("Test continuous channel measurement cycle")
	void testContinuousChannelMeasurementCycle () {
		float interval = 300.0f;
		int count = 1;

		// BOZO - Not supported?
		spectroEx.startContinuousChannelDarkMeasurement(interval, count);

		waitForMeasurementCompletionLong();
		short[] darkData = spectroEx.getContinuousChannelDarkData();
		assertEquals(JetiSDK.SPECTRUM_SIZE, darkData.length);

		spectroEx.startContinuousChannelLightMeasurement(interval, count);
		waitForMeasurementCompletionLong();
		short[] lightData = spectroEx.getContinuousChannelLightData();
		assertEquals(JetiSDK.SPECTRUM_SIZE, lightData.length);
	}

	@Test
	@DisplayName("Test device status and control")
	void testDeviceStatusAndControl () {
		boolean status = spectroEx.isMeasuring();
		spectroEx.startDarkMeasurement(100.0f, 1);
		spectroEx.cancelMeasurement();
		assertFalse(spectroEx.isMeasuring());
	}

	@Test
	@DisplayName("Test device parameters")
	void testDeviceParameters () {
		int pixelCount = spectroEx.getPixelCount();
		assertTrue(pixelCount > 0, "Pixel count should be positive");

		float integrationTime = spectroEx.getIntegrationTime();
		assertTrue(integrationTime > 0, "Integration time should be positive");
	}

	@Test
	@DisplayName("Test wave data with different wavelength ranges and step sizes")
	void testWaveDataVariations () {
		spectroEx.startLightMeasurement(100.0f, 1);
		waitForMeasurementCompletion();

		int[] beginWavelengths = {380, 400};
		int[] endWavelengths = {780, 700};
		float[] stepSizes = {1.0f, 5.0f};
		for (int i = 0; i < beginWavelengths.length; i++) {
			int begin = beginWavelengths[i];
			int end = endWavelengths[i];
			float step = stepSizes[i];
			int expectedSize = (int)((end - begin) / step + 1);
			float[] waveData = spectroEx.getLightWaveData(begin, end, step);
			assertEquals(expectedSize, waveData.length, "Wrong array size for range " + begin + "-" + end + " step " + step);
		}
	}

	private void waitForMeasurementCompletion () {
		int attempts = 0;
		while (spectroEx.isMeasuring() && attempts++ < 100) {
			System.out.print(".");
			sleep(100);
		}
		System.out.println();
		assertFalse(attempts >= 100, "Measurement should complete within timeout");
	}

	private void waitForMeasurementCompletionLong () {
		int attempts = 0;
		while (spectroEx.isMeasuring() && attempts++ < 200) {
			System.out.print(".");
			sleep(100);
		}
		System.out.println();
		assertFalse(attempts >= 200, "Measurement should complete within timeout");
	}
}
