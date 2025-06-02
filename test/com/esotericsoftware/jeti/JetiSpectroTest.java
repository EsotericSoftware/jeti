
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JetiSpectro Integration Tests")
public class JetiSpectroTest {
	private JetiSpectro spectro;

	@BeforeEach
	void setUp () {
		JetiSDK.initialize();

		JetiResult<Integer> deviceCount = JetiSpectro.getSpectroDeviceCount();
		assumeTrue(deviceCount.isSuccess() && deviceCount.getValue() > 0,
			"No spectro devices available for testing " + deviceCount);

		JetiResult<JetiSpectro> deviceResult = JetiSpectro.openSpectroDevice(0);
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
		JetiResult<String[]> serialsResult = JetiSpectro.getSpectroDeviceSerials(0);
		if (serialsResult.isSuccess()) {
			String[] serials = serialsResult.getValue();
			assertEquals(3, serials.length);
			assertNotNull(serials[0]); // Board serial
			assertNotNull(serials[1]); // Spectrometer serial
			assertNotNull(serials[2]); // Device serial
		}

		JetiResult<String> versionResult = JetiSpectro.getSpectroDllVersion();
		assertTrue(versionResult.isSuccess(), versionResult.toString());
		assertNotNull(versionResult.getValue());
		assertTrue(versionResult.getValue().contains("."));
	}

	@Test
	@DisplayName("Measure dark spectrum")
	void testDarkSpectrumMeasurement () {
		float integrationTime = 100.0f;

		JetiResult<float[]> darkResult = spectro.measureDarkSpectrum(integrationTime);
		assertTrue(darkResult.isSuccess(), darkResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, darkResult.getValue().length);

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

		JetiResult<float[]> lightResult = spectro.measureLightSpectrum(integrationTime);
		assertTrue(lightResult.isSuccess(), lightResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, lightResult.getValue().length);

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

		JetiResult<float[]> referenceResult = spectro.measureReferenceSpectrum(integrationTime);
		assertTrue(referenceResult.isSuccess(), referenceResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, referenceResult.getValue().length);

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

		JetiResult<float[]> referenceResult = spectro.measureReferenceSpectrum(integrationTime);
		assertTrue(referenceResult.isSuccess(), referenceResult.toString());

		JetiResult<float[]> transReflResult = spectro.measureTransmissionReflectionSpectrum(integrationTime);
		// BOZO - Command not supported or invalid argument?
		assertTrue(transReflResult.isSuccess(), transReflResult.toString());
		assertEquals(JetiSDK.SPECTRAL_DATA_SIZE, transReflResult.getValue().length);

		// Transmission/reflection spectrum should contain non-negative values
		float[] transReflSpectrum = transReflResult.getValue();
		for (float value : transReflSpectrum)
			assertTrue(value >= 0, "Transmission/reflection spectrum values should be non-negative");
	}

	@Test
	@DisplayName("Get spectro integration time")
	void testGetSpectroIntegrationTime () {
		JetiResult<Float> tintResult = spectro.getSpectroIntegrationTime();
		assertTrue(tintResult.isSuccess(), tintResult.toString());
		assertTrue(tintResult.getValue() > 0, "Integration time should be positive");
	}

	@Test
	@DisplayName("Calculate transmittance with valid spectra")
	void testCalculateTransmittance () {
		// Create sample spectra for calculation
		float[] lightSpectrum = new float[10];
		float[] darkSpectrum = new float[10];
		float[] referenceSpectrum = new float[10];

		// Fill with sample data
		for (int i = 0; i < 10; i++) {
			darkSpectrum[i] = 10.0f; // Dark current
			referenceSpectrum[i] = 100.0f; // Reference measurement
			lightSpectrum[i] = 60.0f; // Sample measurement
		}

		JetiResult<float[]> transmittanceResult = spectro.calculateTransmittance(lightSpectrum, darkSpectrum, referenceSpectrum);
		assertTrue(transmittanceResult.isSuccess(), transmittanceResult.toString());
		assertEquals(10, transmittanceResult.getValue().length);

		// Expected transmittance = (60-10)/(100-10) = 50/90 ≈ 0.556
		float[] transmittance = transmittanceResult.getValue();
		for (float value : transmittance) {
			assertTrue(value >= 0 && value <= 1, "Transmittance values should be between 0 and 1");
		}
	}

	@Test
	@DisplayName("Calculate reflectance with valid spectra")
	void testCalculateReflectance () {
		// Create sample spectra for calculation
		float[] lightSpectrum = new float[5];
		float[] darkSpectrum = new float[5];
		float[] referenceSpectrum = new float[5];

		// Fill with sample data
		for (int i = 0; i < 5; i++) {
			darkSpectrum[i] = 5.0f;
			referenceSpectrum[i] = 50.0f;
			lightSpectrum[i] = 30.0f;
		}

		JetiResult<float[]> reflectanceResult = spectro.calculateReflectance(lightSpectrum, darkSpectrum, referenceSpectrum);
		assertTrue(reflectanceResult.isSuccess(), reflectanceResult.toString());
		assertEquals(5, reflectanceResult.getValue().length);

		float[] reflectance = reflectanceResult.getValue();
		for (float value : reflectance) {
			assertTrue(value >= 0, "Reflectance values should be non-negative");
		}
	}

	@Test
	@DisplayName("Calculate absorbance with valid spectra")
	void testCalculateAbsorbance () {
		// Create sample spectra for calculation
		float[] lightSpectrum = new float[5];
		float[] darkSpectrum = new float[5];
		float[] referenceSpectrum = new float[5];

		// Fill with sample data to get reasonable transmittance values
		for (int i = 0; i < 5; i++) {
			darkSpectrum[i] = 0.0f;
			referenceSpectrum[i] = 100.0f;
			lightSpectrum[i] = 10.0f; // 10% transmission
		}

		JetiResult<float[]> absorbanceResult = spectro.calculateAbsorbance(lightSpectrum, darkSpectrum, referenceSpectrum);
		assertTrue(absorbanceResult.isSuccess(), absorbanceResult.toString());
		assertEquals(5, absorbanceResult.getValue().length);

		// Expected absorbance = -log10(0.1) = 1
		float[] absorbance = absorbanceResult.getValue();
		for (float value : absorbance) {
			assertTrue(Float.isFinite(value), "Absorbance values should be finite");
			assertTrue(value >= 0, "Absorbance values should be non-negative for these test conditions");
		}
	}

	@Test
	@DisplayName("Calculate transmittance with edge cases")
	void testCalculateTransmittanceEdgeCases () {
		// Test with zero reference values (should result in zero transmittance)
		float[] lightSpectrum = {10.0f, 20.0f};
		float[] darkSpectrum = {5.0f, 10.0f};
		float[] referenceSpectrum = {5.0f, 10.0f}; // Same as dark, so corrected reference = 0

		JetiResult<float[]> result = spectro.calculateTransmittance(lightSpectrum, darkSpectrum, referenceSpectrum);
		assertTrue(result.isSuccess(), result.toString());

		float[] transmittance = result.getValue();
		for (float value : transmittance) {
			assertEquals(0.0f, value, "Transmittance should be zero when corrected reference is zero");
		}
	}

	@Test
	@DisplayName("Handle null arrays in calculation methods")
	void testCalculationMethodsWithNullArrays () {
		float[] validSpectrum = {1.0f, 2.0f, 3.0f};

		// Test null light spectrum
		assertThrows(NullPointerException.class, () -> {
			spectro.calculateTransmittance(null, validSpectrum, validSpectrum);
		});

		// Test null dark spectrum
		assertThrows(NullPointerException.class, () -> {
			spectro.calculateReflectance(validSpectrum, null, validSpectrum);
		});

		// Test null reference spectrum
		assertThrows(NullPointerException.class, () -> {
			spectro.calculateAbsorbance(validSpectrum, validSpectrum, null);
		});
	}

	@Test
	@DisplayName("Handle mismatched array lengths in calculation methods")
	void testCalculationMethodsWithMismatchedArrays () {
		float[] spectrum1 = {1.0f, 2.0f, 3.0f};
		float[] spectrum2 = {1.0f, 2.0f}; // Different length

		assertThrows(IllegalArgumentException.class, () -> {
			spectro.calculateTransmittance(spectrum1, spectrum2, spectrum1);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			spectro.calculateReflectance(spectrum1, spectrum1, spectrum2);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			spectro.calculateAbsorbance(spectrum2, spectrum1, spectrum1);
		});
	}

	@Test
	@DisplayName("Validate integration time parameters")
	void testIntegrationTimeValidation () {
		// Test negative integration time
		JetiResult<float[]> result = spectro.measureDarkSpectrum(-1.0f);
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
