
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JetiSpectro Integration Tests")
public class SpectraTest {

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

		float[] transmittance = Spectra.transmittance(lightSpectrum, darkSpectrum, referenceSpectrum);
		// Expected transmittance = (60-10)/(100-10) = 50/90 ≈ 0.556
		for (float value : transmittance)
			assertTrue(value >= 0 && value <= 1, "Transmittance values should be between 0 and 1");
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

		float[] reflectance = Spectra.reflectance(lightSpectrum, darkSpectrum, referenceSpectrum);
		for (float value : reflectance)
			assertTrue(value >= 0, "Reflectance values should be non-negative");
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

		float[] absorbance = Spectra.absorbance(lightSpectrum, darkSpectrum, referenceSpectrum);
		// Expected absorbance = -log10(0.1) = 1
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
		float[] transmittance = Spectra.transmittance(lightSpectrum, darkSpectrum, referenceSpectrum);
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
			Spectra.transmittance(null, validSpectrum, validSpectrum);
		});

		// Test null dark spectrum
		assertThrows(NullPointerException.class, () -> {
			Spectra.reflectance(validSpectrum, null, validSpectrum);
		});

		// Test null reference spectrum
		assertThrows(NullPointerException.class, () -> {
			Spectra.absorbance(validSpectrum, validSpectrum, null);
		});
	}

	@Test
	@DisplayName("Handle mismatched array lengths in calculation methods")
	void testCalculationMethodsWithMismatchedArrays () {
		float[] spectrum1 = {1.0f, 2.0f, 3.0f};
		float[] spectrum2 = {1.0f, 2.0f}; // Different length

		assertThrows(IllegalArgumentException.class, () -> {
			Spectra.transmittance(spectrum1, spectrum2, spectrum1);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			Spectra.reflectance(spectrum1, spectrum1, spectrum2);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			Spectra.absorbance(spectrum2, spectrum1, spectrum1);
		});
	}
}
