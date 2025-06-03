
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;

@DisplayName("JetiSpectro Integration Tests")
public class SpectroTest {
	private Spectro spectro;

	@BeforeEach
	void setUp () {
		JetiSDK.initialize();

		Result<Integer> deviceCount = Spectro.getDeviceCount();
		assumeTrue(deviceCount.isSuccess() && deviceCount.getValue() > 0,
			"No spectro devices available for testing " + deviceCount);

		Result<Spectro> result = Spectro.openDevice(0);
		assumeTrue(result.isSuccess(), "Could not open spectro device " + result);

		spectro = result.getValue();
	}

	@AfterEach
	void tearDown () {
		if (spectro != null && !spectro.isClosed()) spectro.close();
	}

	@Test
	@DisplayName("Get device information")
	void testGetDeviceInfo () {
		Result<DeviceSerials> result = Spectro.getDeviceSerials(0);
		if (result.isSuccess()) {
			DeviceSerials serials = result.getValue();
			assertNotNull(serials.electronics());
			assertNotNull(serials.spectrometer());
			assertNotNull(serials.device());
		}

		Result<DllVersion> versionResult = Spectro.getDllVersion();
		assertTrue(versionResult.isSuccess(), versionResult.toString());
		assertNotNull(versionResult.getValue());
	}

	@Test
	@DisplayName("Measure dark spectrum")
	void testDarkSpectrumMeasurement () {
		float integrationTime = 100.0f;

		Result<float[]> result = spectro.measureDarkSpectrum(integrationTime);
		assertTrue(result.isSuccess(), result.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, result.getValue().length);

		// Dark spectrum should contain mostly low values
		float[] darkSpectrum = result.getValue();
		double averageValue = 0;
		for (float value : darkSpectrum) {
			averageValue += value;
		}
		averageValue /= darkSpectrum.length;
		// Dark spectrum average should be relatively low (but not necessarily zero due to noise)
		assertTrue(averageValue >= 0, "Dark spectrum values should be non-negative");
	}

	@Test
	@DisplayName("Measure light spectrum")
	void testLightSpectrumMeasurement () {
		float integrationTime = 100.0f;

		Result<float[]> result = spectro.measureLightSpectrum(integrationTime);
		assertTrue(result.isSuccess(), result.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, result.getValue().length);

		// Light spectrum should contain some positive values
		float[] lightSpectrum = result.getValue();
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

		Result<float[]> result = spectro.measureReferenceSpectrum(integrationTime);
		assertTrue(result.isSuccess(), result.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, result.getValue().length);

		// Reference spectrum should contain positive values
		float[] referenceSpectrum = result.getValue();
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

		Result<float[]> result = spectro.measureReferenceSpectrum(integrationTime);
		assertTrue(result.isSuccess(), result.toString());

		result = spectro.measureSampleSpectrum(integrationTime);
		// BOZO - Command not supported or invalid argument?
		assertTrue(result.isSuccess(), result.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, result.getValue().length);

		// Sample spectrum should contain non-negative values
		float[] transReflSpectrum = result.getValue();
		for (float value : transReflSpectrum)
			assertTrue(value >= 0, "Sample spectrum values should be non-negative");
	}

	@Test
	@DisplayName("Get spectro integration time")
	void testGetSpectroIntegrationTime () {
		Result<Float> result = spectro.getIntegrationTime();
		assertTrue(result.isSuccess(), result.toString());
		assertTrue(result.getValue() > 0, "Integration time should be positive");
	}
}
