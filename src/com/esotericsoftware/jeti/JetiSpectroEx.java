
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiException.*;
import static com.esotericsoftware.jeti.JetiSDK.*;

import java.util.Objects;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiSpectroEx implements AutoCloseable {
	private Pointer deviceHandle;

	private JetiSpectroEx (Pointer deviceHandle) {
		Objects.requireNonNull(deviceHandle);
		this.deviceHandle = deviceHandle;
	}

	// Dark measurement functions
	public JetiResult<Boolean> startDarkMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		if (integrationTime < 0) return JetiResult.error(invalidArgument);
		if (averageCount <= 0) return JetiResult.error(invalidArgument);
		int result = JetiSpectroExLibrary.INSTANCE.JETI_StartDarkEx(deviceHandle, integrationTime, (short)averageCount);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<int[]> getDarkPixelData () {
		ensureOpen();
		var darkData = new int[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_DarkPixEx(deviceHandle, darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getDarkWaveData (int beginWavelength, int endWavelength, float stepSize) {
		ensureOpen();
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize) + 1;
		var darkData = new float[dataSize];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_DarkWaveEx(deviceHandle, beginWavelength, endWavelength, stepSize,
			darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	// Light measurement functions
	public JetiResult<Boolean> startLightMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		if (integrationTime < 0) return JetiResult.error(invalidArgument);
		if (averageCount <= 0) return JetiResult.error(invalidArgument);
		int result = JetiSpectroExLibrary.INSTANCE.JETI_StartLightEx(deviceHandle, integrationTime, (short)averageCount);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> prepareLightMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		if (integrationTime < 0) return JetiResult.error(invalidArgument);
		if (averageCount <= 0) return JetiResult.error(invalidArgument);
		int result = JetiSpectroExLibrary.INSTANCE.JETI_PrepareLightEx(deviceHandle, integrationTime, (short)averageCount);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<int[]> getLightPixelData () {
		ensureOpen();
		var lightData = new int[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_LightPixEx(deviceHandle, lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getLightWaveData (int beginWavelength, int endWavelength, float stepSize) {
		ensureOpen();
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize) + 1;
		var lightData = new float[dataSize];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_LightWaveEx(deviceHandle, beginWavelength, endWavelength, stepSize,
			lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	// Reference measurement functions
	public JetiResult<Boolean> startReferenceMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_StartReferEx(deviceHandle, integrationTime, (short)averageCount);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> prepareReferenceMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_PrepareReferEx(deviceHandle, integrationTime, (short)averageCount);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<int[]> getReferencePixelData () {
		ensureOpen();
		var referenceData = new int[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ReferPixEx(deviceHandle, referenceData);
		if (result == SUCCESS) return JetiResult.success(referenceData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getReferenceWaveData (int beginWavelength, int endWavelength, float stepSize) {
		ensureOpen();
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize) + 1;
		var referenceData = new float[dataSize];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ReferWaveEx(deviceHandle, beginWavelength, endWavelength, stepSize,
			referenceData);
		if (result == SUCCESS) return JetiResult.success(referenceData);
		return JetiResult.error(result);
	}

	// Transmission/Reflection measurement functions
	public JetiResult<Boolean> startTransmissionReflectionMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_StartTransReflEx(deviceHandle, integrationTime, (short)averageCount);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> prepareTransmissionReflectionMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_PrepareTransReflEx(deviceHandle, integrationTime, (short)averageCount);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<int[]> getTransmissionReflectionPixelData () {
		ensureOpen();
		var transReflData = new int[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_TransReflPixEx(deviceHandle, transReflData);
		if (result == SUCCESS) return JetiResult.success(transReflData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getTransmissionReflectionWaveData (int beginWavelength, int endWavelength, float stepSize) {
		ensureOpen();
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize) + 1;
		var transReflData = new float[dataSize];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_TransReflWaveEx(deviceHandle, beginWavelength, endWavelength, stepSize,
			transReflData);
		if (result == SUCCESS) return JetiResult.success(transReflData);
		return JetiResult.error(result);
	}

	// Image measurement functions
	public JetiResult<Boolean> startDarkImageMeasurement (float integrationTime) {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_StartDarkImageEx(deviceHandle, integrationTime);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<short[]> getDarkImageData () {
		ensureOpen();
		var darkImageData = new short[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_DarkImageEx(deviceHandle, darkImageData);
		if (result == SUCCESS) return JetiResult.success(darkImageData);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> startLightImageMeasurement (float integrationTime) {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_StartLightImageEx(deviceHandle, integrationTime);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<short[]> getLightImageData () {
		ensureOpen();
		var lightImageData = new short[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_LightImageEx(deviceHandle, lightImageData);
		if (result == SUCCESS) return JetiResult.success(lightImageData);
		return JetiResult.error(result);
	}

	// Channel measurement functions
	public JetiResult<Boolean> startChannelDarkMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_StartChannelDarkEx(deviceHandle, integrationTime, (short)averageCount);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<short[]> getChannelDarkData () {
		ensureOpen();
		var darkData = new short[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ChannelDarkEx(deviceHandle, darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> startChannelLightMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_StartChannelLightEx(deviceHandle, integrationTime, (short)averageCount);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<short[]> getChannelLightData () {
		ensureOpen();
		var lightData = new short[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ChannelLightEx(deviceHandle, lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	// Continuous measurement functions
	public JetiResult<Boolean> startContinuousDarkMeasurement (float interval, int count) {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_StartContDarkEx(deviceHandle, interval, count);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<short[]> getContinuousDarkData () {
		ensureOpen();
		var darkData = new short[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ContDarkEx(deviceHandle, darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> startContinuousLightMeasurement (float interval, int count) {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_StartContLightEx(deviceHandle, interval, count);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<short[]> getContinuousLightData () {
		ensureOpen();
		var lightData = new short[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ContLightEx(deviceHandle, lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> startContinuousChannelDarkMeasurement (float interval, int count) {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_StartContChannelDarkEx(deviceHandle, interval, count);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<short[]> getContinuousChannelDarkData () {
		ensureOpen();
		var darkData = new short[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ContChannelDarkEx(deviceHandle, darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> startContinuousChannelLightMeasurement (float interval, int count) {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_StartContChannelLightEx(deviceHandle, interval, count);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<short[]> getContinuousChannelLightData () {
		ensureOpen();
		var lightData = new short[SPECTRAL_DATA_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ContChannelLightEx(deviceHandle, lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	// Device status and control
	public JetiResult<Boolean> getSpectroStatus () {
		ensureOpen();
		var status = new IntByReference();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_SpectroStatusEx(deviceHandle, status);
		if (result == SUCCESS) return JetiResult.success(status.getValue() != 0);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> breakMeasurement () {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_SpectroBreakEx(deviceHandle);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	// Device parameters
	public JetiResult<Integer> getPixelCount () {
		ensureOpen();
		var pixelCount = new IntByReference();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_PixelCountEx(deviceHandle, pixelCount);
		if (result == SUCCESS) return JetiResult.success(pixelCount.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getSpectroIntegrationTime () {
		ensureOpen();
		var tint = new FloatByReference();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_SpectroTintEx(deviceHandle, tint);
		if (result == SUCCESS) return JetiResult.success(tint.getValue());
		return JetiResult.error(result);
	}

	private void ensureOpen () {
		if (deviceHandle == null) throw new IllegalStateException("Spectro ex device is closed.");
	}

	public void close () {
		if (deviceHandle != null) {
			try {
				int result = JetiSpectroExLibrary.INSTANCE.JETI_CloseSpectroEx(deviceHandle);
				if (result != SUCCESS) Log.warn("Unable to close spectro ex device: 0x" + Integer.toHexString(result));
			} catch (Throwable ex) {
				Log.warn("Unable to close spectro ex device.", ex);
			} finally {
				deviceHandle = null;
			}
		}
	}

	public boolean isClosed () {
		return deviceHandle == null;
	}

	static public JetiResult<Integer> getSpectroExDeviceCount () {
		var numDevices = new IntByReference();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_GetNumSpectroEx(numDevices);
		if (result == SUCCESS) return JetiResult.success(numDevices.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<String[]> getSpectroExDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_BUFFER_SIZE];
		var specSerial = new byte[STRING_BUFFER_SIZE];
		var deviceSerial = new byte[STRING_BUFFER_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_GetSerialSpectroEx(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result == SUCCESS) {
			String[] serials = {string(boardSerial), string(specSerial), string(deviceSerial)};
			return JetiResult.success(serials);
		}
		return JetiResult.error(result);
	}

	static public JetiResult<JetiSpectroEx> openSpectroExDevice (int deviceNumber) {
		var deviceHandle = new PointerByReference();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_OpenSpectroEx(deviceNumber, deviceHandle);
		if (result == SUCCESS) return JetiResult.success(new JetiSpectroEx(deviceHandle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<String> getSpectroExDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_GetSpectroExDLLVersion(major, minor, build);
		if (result == SUCCESS) return JetiResult.success(major.getValue() + "." + minor.getValue() + "." + build.getValue());
		return JetiResult.error(result);
	}
}
