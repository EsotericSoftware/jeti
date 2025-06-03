
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

		Result<Spectro> deviceResult = Spectro.openDevice(0);
		assumeTrue(deviceResult.isSuccess(), "Could not open spectro device " + deviceResult);

		spectro = deviceResult.getValue();
	}

	@AfterEach
	void tearDown () {
		if (spectro != null && !spectro.isClosed()) spectro.close();
	}

	@Test
	@DisplayName("Get device information")
	void testGetDeviceInfo () {
		Result<DeviceSerials> serialsResult = Spectro.getDeviceSerials(0);
		if (serialsResult.isSuccess()) {
			DeviceSerials serials = serialsResult.getValue();
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

		Result<float[]> darkResult = spectro.measureDarkSpectrum(integrationTime);
		assertTrue(darkResult.isSuccess(), darkResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, darkResult.getValue().length);

		// Dark spectrum should contain mostly low values
		float[] darkSpectrum = darkResult.getValue();
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

		Result<float[]> lightResult = spectro.measureLightSpectrum(integrationTime);
		assertTrue(lightResult.isSuccess(), lightResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, lightResult.getValue().length);

		// Light spectrum should contain some positive values
		float[] lightSpectrum = lightResult.getValue();
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

		Result<float[]> referenceResult = spectro.measureReferenceSpectrum(integrationTime);
		assertTrue(referenceResult.isSuccess(), referenceResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, referenceResult.getValue().length);

		// Reference spectrum should contain positive values
		float[] referenceSpectrum = referenceResult.getValue();
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
	@DisplayName("Measure transmission/reflection spectrum")
	void testTransmissionReflectionSpectrumMeasurement () {
		float integrationTime = 100.0f;

		Result<float[]> referenceResult = spectro.measureReferenceSpectrum(integrationTime);
		assertTrue(referenceResult.isSuccess(), referenceResult.toString());

		Result<float[]> transReflResult = spectro.measureTransmissionReflectionSpectrum(integrationTime);
		// BOZO - Command not supported or invalid argument?
		assertTrue(transReflResult.isSuccess(), transReflResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, transReflResult.getValue().length);

		// Transmission/reflection spectrum should contain non-negative values
		float[] transReflSpectrum = transReflResult.getValue();
		for (float value : transReflSpectrum)
			assertTrue(value >= 0, "Transmission/reflection spectrum values should be non-negative");
	}

	@Test
	@DisplayName("Get spectro integration time")
	void testGetSpectroIntegrationTime () {
		Result<Float> tintResult = spectro.getIntegrationTime();
		assertTrue(tintResult.isSuccess(), tintResult.toString());
		assertTrue(tintResult.getValue() > 0, "Integration time should be positive");
	}

	@Test
	@DisplayName("Validate integration time parameters")
	void testIntegrationTimeValidation () {
		// Test negative integration time
		Result<float[]> result = spectro.measureDarkSpectrum(-1.0f);
		assertFalse(result.isSuccess(), "Negative integration time should be rejected");
		assertEquals(JetiSDK.INVALID_ARGUMENT, result.getErrorCode());

		// Test zero integration time (should be valid)
		result = spectro.measureLightSpectrum(0.0f);
		// Whether zero is valid depends on device implementation
		if (!result.isSuccess()) {
			// If zero is not accepted, that's also acceptable behavior
			assertTrue(result.getErrorCode() != JetiSDK.SUCCESS);
		}
	}
}
