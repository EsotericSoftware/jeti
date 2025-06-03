
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiSpectroEx extends Device {
	private JetiSpectroEx (Pointer handle) {
		super(handle);
	}

	// Dark measurement functions
	public JetiResult<Boolean> startDarkMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_StartDarkEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<int[]> getDarkPixelData () {
		ensureOpen();
		var darkData = new int[SPECTRUM_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_DarkPixEx(handle, darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getDarkWaveData (int beginWavelength, int endWavelength, float stepSize) {
		ensureOpen();
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var darkData = new float[dataSize];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_DarkWaveEx(handle, beginWavelength, endWavelength, stepSize, darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	// Light measurement functions
	public JetiResult<Boolean> startLightMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_StartLightEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<Boolean> prepareLightMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_PrepareLightEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<int[]> getLightPixelData () {
		ensureOpen();
		var lightData = new int[SPECTRUM_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_LightPixEx(handle, lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getLightWaveData (int beginWavelength, int endWavelength, float stepSize) {
		ensureOpen();
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var lightData = new float[dataSize];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_LightWaveEx(handle, beginWavelength, endWavelength, stepSize, lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	// Reference measurement functions
	public JetiResult<Boolean> startReferenceMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_StartReferEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<Boolean> prepareReferenceMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_PrepareReferEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<int[]> getReferencePixelData () {
		ensureOpen();
		var referenceData = new int[SPECTRUM_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ReferPixEx(handle, referenceData);
		if (result == SUCCESS) return JetiResult.success(referenceData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getReferenceWaveData (int beginWavelength, int endWavelength, float stepSize) {
		ensureOpen();
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var referenceData = new float[dataSize];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ReferWaveEx(handle, beginWavelength, endWavelength, stepSize,
			referenceData);
		if (result == SUCCESS) return JetiResult.success(referenceData);
		return JetiResult.error(result);
	}

	// Transmission/Reflection measurement functions
	public JetiResult<Boolean> startSampleMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_StartTransReflEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<Boolean> prepareSampleMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		return JetiResult
			.result(JetiSpectroExLibrary.INSTANCE.JETI_PrepareTransReflEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<int[]> getSamplePixelData () {
		ensureOpen();
		var transReflData = new int[SPECTRUM_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_TransReflPixEx(handle, transReflData);
		if (result == SUCCESS) return JetiResult.success(transReflData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getSampleWaveData (int beginWavelength, int endWavelength, float stepSize) {
		ensureOpen();
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var transReflData = new float[dataSize];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_TransReflWaveEx(handle, beginWavelength, endWavelength, stepSize,
			transReflData);
		if (result == SUCCESS) return JetiResult.success(transReflData);
		return JetiResult.error(result);
	}

	// Image measurement functions
	public JetiResult<Boolean> startDarkImageMeasurement (float integrationTime) {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_StartDarkImageEx(handle, integrationTime));
	}

	public JetiResult<short[]> getDarkImageData () {
		ensureOpen();
		var darkImageData = new short[SPECTRUM_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_DarkImageEx(handle, darkImageData);
		if (result == SUCCESS) return JetiResult.success(darkImageData);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> startLightImageMeasurement (float integrationTime) {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_StartLightImageEx(handle, integrationTime));
	}

	public JetiResult<short[]> getLightImageData () {
		ensureOpen();
		var lightImageData = new short[SPECTRUM_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_LightImageEx(handle, lightImageData);
		if (result == SUCCESS) return JetiResult.success(lightImageData);
		return JetiResult.error(result);
	}

	// Channel measurement functions
	public JetiResult<Boolean> startChannelDarkMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		return JetiResult
			.result(JetiSpectroExLibrary.INSTANCE.JETI_StartChannelDarkEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<short[]> getChannelDarkData () {
		ensureOpen();
		var darkData = new short[SPECTRUM_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ChannelDarkEx(handle, darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> startChannelLightMeasurement (float integrationTime, int averageCount) {
		ensureOpen();
		return JetiResult
			.result(JetiSpectroExLibrary.INSTANCE.JETI_StartChannelLightEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<short[]> getChannelLightData () {
		ensureOpen();
		var lightData = new short[SPECTRUM_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ChannelLightEx(handle, lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	// Continuous measurement functions
	public JetiResult<Boolean> startContinuousDarkMeasurement (float interval, int count) {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_StartContDarkEx(handle, interval, count));
	}

	public JetiResult<short[]> getContinuousDarkData () {
		ensureOpen();
		var darkData = new short[SPECTRUM_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ContDarkEx(handle, darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> startContinuousLightMeasurement (float interval, int count) {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_StartContLightEx(handle, interval, count));
	}

	public JetiResult<short[]> getContinuousLightData () {
		ensureOpen();
		var lightData = new short[SPECTRUM_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ContLightEx(handle, lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> startContinuousChannelDarkMeasurement (float interval, int count) {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_StartContChannelDarkEx(handle, interval, count));
	}

	public JetiResult<short[]> getContinuousChannelDarkData () {
		ensureOpen();
		var darkData = new short[SPECTRUM_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ContChannelDarkEx(handle, darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> startContinuousChannelLightMeasurement (float interval, int count) {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_StartContChannelLightEx(handle, interval, count));
	}

	public JetiResult<short[]> getContinuousChannelLightData () {
		ensureOpen();
		var lightData = new short[SPECTRUM_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_ContChannelLightEx(handle, lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	// Device status and control
	public JetiResult<Boolean> getMeasurementStatus () {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_SpectroStatusEx(handle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue() != 0);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> breakMeasurement () {
		ensureOpen();
		return JetiResult.result(JetiSpectroExLibrary.INSTANCE.JETI_SpectroBreakEx(handle));
	}

	// Device parameters
	public JetiResult<Integer> getPixelCount () {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_PixelCountEx(handle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		ensureOpen();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_SpectroTintEx(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_GetNumSpectroEx(count);
		if (result == SUCCESS) return JetiResult.success(count.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_GetSerialSpectroEx(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result == SUCCESS)
			return JetiResult.success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiSpectroEx> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_OpenSpectroEx(deviceNumber, handle);
		if (result == SUCCESS) return JetiResult.success(new JetiSpectroEx(handle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_GetSpectroExDLLVersion(major, minor, build);
		if (result == SUCCESS) return JetiResult.success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
		return JetiResult.error(result);
	}
}
