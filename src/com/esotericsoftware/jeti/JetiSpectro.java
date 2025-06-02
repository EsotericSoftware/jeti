
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import java.util.Objects;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiSpectro implements AutoCloseable {
	private Pointer deviceHandle;

	private final FloatByReference floatRef = new FloatByReference();
	private final IntByReference intRef = new IntByReference();

	private JetiSpectro (Pointer deviceHandle) {
		Objects.requireNonNull(deviceHandle);
		this.deviceHandle = deviceHandle;
	}

	public JetiResult<float[]> measureDarkSpectrum (float integrationTime) {
		if (integrationTime < 0) return JetiResult.error(INVALID_ARGUMENT);
		ensureOpen();
		var darkData = new float[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_DarkSpec(deviceHandle, integrationTime, darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> measureLightSpectrum (float integrationTime) {
		if (integrationTime < 0) return JetiResult.error(INVALID_ARGUMENT);
		ensureOpen();
		var lightData = new float[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_LightSpec(deviceHandle, integrationTime, lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> measureReferenceSpectrum (float integrationTime) {
		if (integrationTime < 0) return JetiResult.error(INVALID_ARGUMENT);
		ensureOpen();
		var referenceData = new float[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_ReferSpec(deviceHandle, integrationTime, referenceData);
		if (result == SUCCESS) return JetiResult.success(referenceData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> measureTransmissionReflectionSpectrum (float integrationTime) {
		if (integrationTime < 0) return JetiResult.error(INVALID_ARGUMENT);
		ensureOpen();
		var transReflData = new float[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_TransReflSpec(deviceHandle, integrationTime, transReflData);
		if (result == SUCCESS) return JetiResult.success(transReflData);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getSpectroIntegrationTime () {
		ensureOpen();
		int result = JetiSpectroLibrary.INSTANCE.JETI_SpectroTint(deviceHandle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<float[]> calculateTransmittance (float[] lightSpectrum, float[] darkSpectrum, float[] referenceSpectrum) {
		Objects.requireNonNull(lightSpectrum, "lightSpectrum");
		Objects.requireNonNull(darkSpectrum, "darkSpectrum");
		Objects.requireNonNull(referenceSpectrum, "referenceSpectrum");
		if (lightSpectrum.length != darkSpectrum.length || lightSpectrum.length != referenceSpectrum.length)
			throw new IllegalArgumentException("All spectra must have the same length.");

		var transmittance = new float[lightSpectrum.length];
		for (int i = 0; i < lightSpectrum.length; i++) {
			float correctedLight = lightSpectrum[i] - darkSpectrum[i];
			float correctedReference = referenceSpectrum[i] - darkSpectrum[i];
			if (correctedReference > 0)
				transmittance[i] = correctedLight / correctedReference;
			else
				transmittance[i] = 0;
		}
		return JetiResult.success(transmittance);
	}

	public JetiResult<float[]> calculateReflectance (float[] lightSpectrum, float[] darkSpectrum, float[] referenceSpectrum) {
		Objects.requireNonNull(lightSpectrum, "lightSpectrum");
		Objects.requireNonNull(darkSpectrum, "darkSpectrum");
		Objects.requireNonNull(referenceSpectrum, "referenceSpectrum");
		return calculateTransmittance(lightSpectrum, darkSpectrum, referenceSpectrum);
	}

	public JetiResult<float[]> calculateAbsorbance (float[] lightSpectrum, float[] darkSpectrum, float[] referenceSpectrum) {
		Objects.requireNonNull(lightSpectrum, "lightSpectrum");
		Objects.requireNonNull(darkSpectrum, "darkSpectrum");
		Objects.requireNonNull(referenceSpectrum, "referenceSpectrum");
		JetiResult<float[]> transmittanceResult = calculateTransmittance(lightSpectrum, darkSpectrum, referenceSpectrum);
		if (transmittanceResult.isError()) return JetiResult.error(transmittanceResult.getErrorCode());

		float[] transmittance = transmittanceResult.getValue();
		var absorbance = new float[transmittance.length];
		for (int i = 0; i < transmittance.length; i++) {
			if (transmittance[i] > 0)
				absorbance[i] = (float)-Math.log10(transmittance[i]);
			else
				absorbance[i] = Float.POSITIVE_INFINITY;
		}
		return JetiResult.success(absorbance);
	}

	private void ensureOpen () {
		if (deviceHandle == null) throw new IllegalStateException("Spectro device is closed.");
	}

	public void close () {
		if (deviceHandle != null) {
			try {
				int result = JetiSpectroLibrary.INSTANCE.JETI_CloseSpectro(deviceHandle);
				if (result != SUCCESS) Log.warn("Unable to close spectro device: 0x" + Integer.toHexString(result));
			} catch (Throwable ex) {
				Log.warn("Unable to close spectro device.", ex);
			} finally {
				deviceHandle = null;
			}
		}
	}

	public boolean isClosed () {
		return deviceHandle == null;
	}

	static public JetiResult<Integer> getSpectroDeviceCount () {
		var count = new IntByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetNumSpectro(count);
		if (result == SUCCESS) return JetiResult.success(count.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<String[]> getSpectroDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_BUFFER_SIZE];
		var specSerial = new byte[STRING_BUFFER_SIZE];
		var deviceSerial = new byte[STRING_BUFFER_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetSerialSpectro(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result == SUCCESS) {
			String[] serials = {string(boardSerial), string(specSerial), string(deviceSerial)};
			return JetiResult.success(serials);
		}
		return JetiResult.error(result);
	}

	static public JetiResult<JetiSpectro> openSpectroDevice (int deviceNumber) {
		var deviceHandle = new PointerByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_OpenSpectro(deviceNumber, deviceHandle);

		if (result == SUCCESS) return JetiResult.success(new JetiSpectro(deviceHandle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<String> getSpectroDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetSpectroDLLVersion(major, minor, build);
		if (result == SUCCESS) return JetiResult.success(major.getValue() + "." + minor.getValue() + "." + build.getValue());
		return JetiResult.error(result);
	}
}
