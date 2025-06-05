
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;

public class SpectroTest extends JetiTest {
	private Spectro spectro;

	@BeforeEach
	void setUp () {
		assumeTrue(Spectro.getDeviceCount() > 0, "No spectro devices available for testing");
		spectro = Spectro.openDevice(0);
	}

	@AfterEach
	void tearDown () {
		if (spectro != null && !spectro.isClosed()) spectro.close();
	}

	@Test
	@DisplayName("Get device information")
	void testGetDeviceInfo () {
		DeviceSerials serials = Spectro.getDeviceSerials(0);
		assertNotNull(serials.electronics());
		assertNotNull(serials.spectrometer());
		assertNotNull(serials.device());

		DllVersion version = Spectro.getDllVersion();
		assertNotNull(version);
	}

	@Test
	@DisplayName("Measure dark spectrum")
	void testDarkSpectrumMeasurement () {
		float integrationTime = 100.0f;
		float[] darkSpectrum = spectro.measureDarkSpectrum(integrationTime);
		assertEquals(JetiSDK.SPECTRUM_SIZE, darkSpectrum.length);

		double averageValue = 0;
		for (float value : darkSpectrum)
			averageValue += value;
		averageValue /= darkSpectrum.length;
		assertTrue(averageValue >= 0, "Dark spectrum values should be non-negative");
	}

	@Test
	@DisplayName("Measure light spectrum")
	void testLightSpectrumMeasurement () {
		float integrationTime = 100.0f;
		float[] lightSpectrum = spectro.measureLightSpectrum(integrationTime);
		assertEquals(JetiSDK.SPECTRUM_SIZE, lightSpectrum.length);

		boolean hasPositiveValues = false;
		for (float value : lightSpectrum) {
			if (value > 0) {
				hasPositiveValues = true;
				break;
			}
		}
		assertTrue(hasPositiveValues, "Light spectrum should contain some positive values");
	}

	@Test
	@DisplayName("Measure reference spectrum")
	void testReferenceSpectrumMeasurement () {
		float integrationTime = 100.0f;
		float[] referenceSpectrum = spectro.measureReferenceSpectrum(integrationTime);
		assertEquals(JetiSDK.SPECTRUM_SIZE, referenceSpectrum.length);

		boolean hasPositiveValues = false;
		for (float value : referenceSpectrum) {
			if (value > 0) {
				hasPositiveValues = true;
				break;
			}
		}
		assertTrue(hasPositiveValues, "Reference spectrum should contain some positive values");
	}

	@Test
	@DisplayName("Measure sample spectrum")
	void testSampleSpectrumMeasurement () {
		float integrationTime = 100.0f;
		spectro.measureReferenceSpectrum(integrationTime);

		// BOZO - Not supported?
		float[] sampleSpectrum = spectro.measureSampleSpectrum(integrationTime);
		assertEquals(JetiSDK.SPECTRUM_SIZE, sampleSpectrum.length);

		for (float value : sampleSpectrum)
			assertTrue(value >= 0, "Sample spectrum values should be non-negative");
	}

	@Test
	@DisplayName("Get spectro integration time")
	void testGetSpectroIntegrationTime () {
		float integrationTime = spectro.getIntegrationTime();
		assertTrue(integrationTime > 0, "Integration time should be positive");
	}
}
