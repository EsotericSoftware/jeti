
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiResult.*;
import static com.esotericsoftware.jeti.JetiSDK.*;

import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiSpectroEx extends Device<JetiSpectroExLibrary> {
	private JetiSpectroEx (Pointer handle) {
		super(JetiSpectroExLibrary.INSTANCE, handle);
	}

	// Dark measurement functions
	public JetiResult<Boolean> startDarkMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_StartDarkEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<int[]> getDarkPixelData () {
		var darkData = new int[SPECTRUM_SIZE];
		int result = lib().JETI_DarkPixEx(handle, darkData);
		if (result == SUCCESS) return success(darkData);
		return error(result);
	}

	public JetiResult<float[]> getDarkWaveData (int beginWavelength, int endWavelength, float stepSize) {
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var darkData = new float[dataSize];
		int result = lib().JETI_DarkWaveEx(handle, beginWavelength, endWavelength, stepSize, darkData);
		if (result == SUCCESS) return success(darkData);
		return error(result);
	}

	// Light measurement functions
	public JetiResult<Boolean> startLightMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_StartLightEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<Boolean> prepareLightMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_PrepareLightEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<int[]> getLightPixelData () {
		var lightData = new int[SPECTRUM_SIZE];
		int result = lib().JETI_LightPixEx(handle, lightData);
		if (result == SUCCESS) return success(lightData);
		return error(result);
	}

	public JetiResult<float[]> getLightWaveData (int beginWavelength, int endWavelength, float stepSize) {
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var lightData = new float[dataSize];
		int result = lib().JETI_LightWaveEx(handle, beginWavelength, endWavelength, stepSize, lightData);
		if (result == SUCCESS) return success(lightData);
		return error(result);
	}

	// Reference measurement functions
	public JetiResult<Boolean> startReferenceMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_StartReferEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<Boolean> prepareReferenceMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_PrepareReferEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<int[]> getReferencePixelData () {
		var referenceData = new int[SPECTRUM_SIZE];
		int result = lib().JETI_ReferPixEx(handle, referenceData);
		if (result == SUCCESS) return success(referenceData);
		return error(result);
	}

	public JetiResult<float[]> getReferenceWaveData (int beginWavelength, int endWavelength, float stepSize) {
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var referenceData = new float[dataSize];
		int result = lib().JETI_ReferWaveEx(handle, beginWavelength, endWavelength, stepSize, referenceData);
		if (result == SUCCESS) return success(referenceData);
		return error(result);
	}

	// Transmission/Reflection measurement functions
	public JetiResult<Boolean> startSampleMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_StartTransReflEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<Boolean> prepareSampleMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_PrepareTransReflEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<int[]> getSamplePixelData () {
		var transReflData = new int[SPECTRUM_SIZE];
		int result = lib().JETI_TransReflPixEx(handle, transReflData);
		if (result == SUCCESS) return success(transReflData);
		return error(result);
	}

	public JetiResult<float[]> getSampleWaveData (int beginWavelength, int endWavelength, float stepSize) {
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var transReflData = new float[dataSize];
		int result = lib().JETI_TransReflWaveEx(handle, beginWavelength, endWavelength, stepSize, transReflData);
		if (result == SUCCESS) return success(transReflData);
		return error(result);
	}

	// Image measurement functions
	public JetiResult<Boolean> startDarkImageMeasurement (float integrationTime) {
		return result(lib().JETI_StartDarkImageEx(handle, integrationTime));
	}

	public JetiResult<short[]> getDarkImageData () {
		var darkImageData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_DarkImageEx(handle, darkImageData);
		if (result == SUCCESS) return success(darkImageData);
		return error(result);
	}

	public JetiResult<Boolean> startLightImageMeasurement (float integrationTime) {
		return result(lib().JETI_StartLightImageEx(handle, integrationTime));
	}

	public JetiResult<short[]> getLightImageData () {
		var lightImageData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_LightImageEx(handle, lightImageData);
		if (result == SUCCESS) return success(lightImageData);
		return error(result);
	}

	// Channel measurement functions
	public JetiResult<Boolean> startChannelDarkMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_StartChannelDarkEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<short[]> getChannelDarkData () {
		var darkData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_ChannelDarkEx(handle, darkData);
		if (result == SUCCESS) return success(darkData);
		return error(result);
	}

	public JetiResult<Boolean> startChannelLightMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_StartChannelLightEx(handle, integrationTime, (short)averageCount));
	}

	public JetiResult<short[]> getChannelLightData () {
		var lightData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_ChannelLightEx(handle, lightData);
		if (result == SUCCESS) return success(lightData);
		return error(result);
	}

	// Continuous measurement functions
	public JetiResult<Boolean> startContinuousDarkMeasurement (float interval, int count) {
		return result(lib().JETI_StartContDarkEx(handle, interval, count));
	}

	public JetiResult<short[]> getContinuousDarkData () {
		var darkData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_ContDarkEx(handle, darkData);
		if (result == SUCCESS) return success(darkData);
		return error(result);
	}

	public JetiResult<Boolean> startContinuousLightMeasurement (float interval, int count) {
		return result(lib().JETI_StartContLightEx(handle, interval, count));
	}

	public JetiResult<short[]> getContinuousLightData () {
		var lightData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_ContLightEx(handle, lightData);
		if (result == SUCCESS) return success(lightData);
		return error(result);
	}

	public JetiResult<Boolean> startContinuousChannelDarkMeasurement (float interval, int count) {
		return result(lib().JETI_StartContChannelDarkEx(handle, interval, count));
	}

	public JetiResult<short[]> getContinuousChannelDarkData () {
		var darkData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_ContChannelDarkEx(handle, darkData);
		if (result == SUCCESS) return success(darkData);
		return error(result);
	}

	public JetiResult<Boolean> startContinuousChannelLightMeasurement (float interval, int count) {
		return result(lib().JETI_StartContChannelLightEx(handle, interval, count));
	}

	public JetiResult<short[]> getContinuousChannelLightData () {
		var lightData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_ContChannelLightEx(handle, lightData);
		if (result == SUCCESS) return success(lightData);
		return error(result);
	}

	// Device status and control
	public JetiResult<Boolean> getMeasurementStatus () {
		int result = lib().JETI_SpectroStatusEx(handle, intRef);
		if (result == SUCCESS) return success(intRef.getValue() != 0);
		return error(result);
	}

	public JetiResult<Boolean> breakMeasurement () {
		return result(lib().JETI_SpectroBreakEx(handle));
	}

	// Device parameters
	public JetiResult<Integer> getPixelCount () {
		int result = lib().JETI_PixelCountEx(handle, intRef);
		if (result == SUCCESS) return success(intRef.getValue());
		return error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		int result = lib().JETI_SpectroTintEx(handle, floatRef);
		if (result == SUCCESS) return success(floatRef.getValue());
		return error(result);
	}

	static public JetiResult<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_GetNumSpectroEx(count);
		if (result == SUCCESS) return success(count.getValue());
		return error(result);
	}

	static public JetiResult<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = JetiSpectroExLibrary.INSTANCE.JETI_GetSerialSpectroEx(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result == SUCCESS) return success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
		return error(result);
	}

	static public JetiResult<JetiSpectroEx> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_OpenSpectroEx(deviceNumber, handle);
		if (result == SUCCESS) return success(new JetiSpectroEx(handle.getValue()));
		return error(result);
	}

	static public JetiResult<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiSpectroExLibrary.INSTANCE.JETI_GetSpectroExDLLVersion(major, minor, build);
		if (result == SUCCESS) return success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
		return error(result);
	}
}
