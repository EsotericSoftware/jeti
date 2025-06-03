
package com.esotericsoftware.jeti;

import java.util.Objects;

/** @author Nathan Sweet <misc@n4te.com> */
public class Spectra {
	private Spectra () {
	}

	static public float[] transmittance (float[] lightSpectrum, float[] darkSpectrum, float[] referenceSpectrum) {
		Objects.requireNonNull(lightSpectrum, "lightSpectrum");
		Objects.requireNonNull(darkSpectrum, "darkSpectrum");
		Objects.requireNonNull(referenceSpectrum, "referenceSpectrum");
		if (lightSpectrum.length != darkSpectrum.length || lightSpectrum.length != referenceSpectrum.length)
			throw new IllegalArgumentException("All spectra must have the same length.");

		var transmittance = new float[lightSpectrum.length];
		for (int i = 0; i < lightSpectrum.length; i++) {
			float correctedLight = lightSpectrum[i] - darkSpectrum[i];
			float correctedReference = referenceSpectrum[i] - darkSpectrum[i];
			transmittance[i] = correctedReference > 0 ? correctedLight / correctedReference : 0;
		}
		return transmittance;
	}

	static public float[] reflectance (float[] lightSpectrum, float[] darkSpectrum, float[] referenceSpectrum) {
		return transmittance(lightSpectrum, darkSpectrum, referenceSpectrum);
	}

	static public float[] absorbance (float[] lightSpectrum, float[] darkSpectrum, float[] referenceSpectrum) {
		float[] transmittance = transmittance(lightSpectrum, darkSpectrum, referenceSpectrum);
		var absorbance = new float[transmittance.length];
		for (int i = 0; i < transmittance.length; i++)
			absorbance[i] = transmittance[i] > 0 ? (float)-Math.log10(transmittance[i]) : Float.POSITIVE_INFINITY;
		return absorbance;
	}
}
