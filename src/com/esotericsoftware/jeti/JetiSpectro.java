
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import java.util.Objects;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiSpectro implements AutoCloseable {
	private Pointer deviceHandle;

	private JetiSpectro (Pointer deviceHandle) {
		Objects.nonNull(deviceHandle);
		this.deviceHandle = deviceHandle;
	}

	public JetiResult<float[]> measureDarkSpectrum (float integrationTime) {
		ensureOpen();
		var darkData = new float[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_DarkSpec(deviceHandle, integrationTime, darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> measureLightSpectrum (float integrationTime) {
		ensureOpen();
		var lightData = new float[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_LightSpec(deviceHandle, integrationTime, lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> measureReferenceSpectrum (float integrationTime) {
		ensureOpen();
		var referenceData = new float[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_ReferSpec(deviceHandle, integrationTime, referenceData);
		if (result == SUCCESS) return JetiResult.success(referenceData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> measureTransmissionReflectionSpectrum (float integrationTime) {
		ensureOpen();
		var transReflData = new float[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_TransReflSpec(deviceHandle, integrationTime, transReflData);
		if (result == SUCCESS) return JetiResult.success(transReflData);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getSpectroIntegrationTime () {
		ensureOpen();
		var tint = new float[1];
		int result = JetiSpectroLibrary.INSTANCE.JETI_SpectroTint(deviceHandle, tint);
		if (result == SUCCESS) return JetiResult.success(tint[0]);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> measureSpectrum (MeasurementType type, float integrationTime) {
		return switch (type) {
		case DARK -> measureDarkSpectrum(integrationTime);
		case LIGHT -> measureLightSpectrum(integrationTime);
		case REFERENCE -> measureReferenceSpectrum(integrationTime);
		case TRANSMISSION_REFLECTION -> measureTransmissionReflectionSpectrum(integrationTime);
		};
	}

	public JetiResult<SpectroscopicData> measureAllSpectra (float integrationTime) {
		JetiResult<float[]> darkResult = measureDarkSpectrum(integrationTime);
		if (darkResult.isError()) return JetiResult.error(darkResult.getErrorCode());

		JetiResult<float[]> lightResult = measureLightSpectrum(integrationTime);
		if (lightResult.isError()) return JetiResult.error(lightResult.getErrorCode());

		JetiResult<float[]> referenceResult = measureReferenceSpectrum(integrationTime);
		if (referenceResult.isError()) return JetiResult.error(referenceResult.getErrorCode());

		JetiResult<float[]> transReflResult = measureTransmissionReflectionSpectrum(integrationTime);
		if (transReflResult.isError()) return JetiResult.error(transReflResult.getErrorCode());

		JetiResult<Float> tintResult = getSpectroIntegrationTime();
		float actualIntegrationTime = tintResult.isSuccess() ? tintResult.getValue() : integrationTime;

		var data = new SpectroscopicData(darkResult.getValue(), lightResult.getValue(), referenceResult.getValue(),
			transReflResult.getValue(), actualIntegrationTime);

		return JetiResult.success(data);
	}

	public JetiResult<float[]> calculateTransmittance (float[] lightSpectrum, float[] darkSpectrum, float[] referenceSpectrum) {
		if (lightSpectrum.length != darkSpectrum.length || lightSpectrum.length != referenceSpectrum.length)
			throw new IllegalArgumentException("All spectra must have the same length.");

		var transmittance = new float[lightSpectrum.length];
		for (int i = 0; i < lightSpectrum.length; i++) {
			float correctedLight = lightSpectrum[i] - darkSpectrum[i];
			float correctedReference = referenceSpectrum[i] - darkSpectrum[i];
			if (correctedReference > 0)
				transmittance[i] = correctedLight / correctedReference;
			else
				transmittance[i] = 0.0f;
		}

		return JetiResult.success(transmittance);
	}

	public JetiResult<float[]> calculateReflectance (float[] lightSpectrum, float[] darkSpectrum, float[] referenceSpectrum) {
		return calculateTransmittance(lightSpectrum, darkSpectrum, referenceSpectrum);
	}

	public JetiResult<float[]> calculateAbsorbance (float[] lightSpectrum, float[] darkSpectrum, float[] referenceSpectrum) {
		JetiResult<float[]> transmittanceResult = calculateTransmittance(lightSpectrum, darkSpectrum, referenceSpectrum);
		if (transmittanceResult.isError()) return JetiResult.error(transmittanceResult.getErrorCode());

		float[] transmittance = transmittanceResult.getValue();
		var absorbance = new float[transmittance.length];

		for (int i = 0; i < transmittance.length; i++)
			if (transmittance[i] > 0)
				absorbance[i] = (float)-Math.log10(transmittance[i]);
			else
				absorbance[i] = Float.POSITIVE_INFINITY;

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

	static public JetiResult<Integer> getNumberOfSpectroDevices () {
		var numDevices = new IntByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetNumSpectro(numDevices);
		if (result == SUCCESS) return JetiResult.success(numDevices.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<String[]> getSpectroDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_BUFFER_SIZE];
		var specSerial = new byte[STRING_BUFFER_SIZE];
		var deviceSerial = new byte[STRING_BUFFER_SIZE];

		int result = JetiSpectroLibrary.INSTANCE.JETI_GetSerialSpectro(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result == SUCCESS) {
			String[] serials = {bytesToString(boardSerial), bytesToString(specSerial), bytesToString(deviceSerial)};
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

	static public enum MeasurementType {
		DARK, LIGHT, REFERENCE, TRANSMISSION_REFLECTION
	}

	static public class SpectroscopicData {
		private final float[] darkSpectrum;
		private final float[] lightSpectrum;
		private final float[] referenceSpectrum;
		private final float[] transmissionReflectionSpectrum;
		private final float integrationTime;

		public SpectroscopicData (float[] darkSpectrum, float[] lightSpectrum, float[] referenceSpectrum,
			float[] transmissionReflectionSpectrum, float integrationTime) {
			this.darkSpectrum = darkSpectrum;
			this.lightSpectrum = lightSpectrum;
			this.referenceSpectrum = referenceSpectrum;
			this.transmissionReflectionSpectrum = transmissionReflectionSpectrum;
			this.integrationTime = integrationTime;
		}

		public float[] getDarkSpectrum () {
			return darkSpectrum;
		}

		public float[] getLightSpectrum () {
			return lightSpectrum;
		}

		public float[] getReferenceSpectrum () {
			return referenceSpectrum;
		}

		public float[] getTransmissionReflectionSpectrum () {
			return transmissionReflectionSpectrum;
		}

		public float getIntegrationTime () {
			return integrationTime;
		}
	}
}
