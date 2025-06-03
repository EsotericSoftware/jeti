
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;
import static com.esotericsoftware.jeti.Result.*;

import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class SpectroEx extends Device<SpectroExLibrary> {
	private SpectroEx (Pointer handle) {
		super(SpectroExLibrary.INSTANCE, handle, SpectroExLibrary.INSTANCE::JETI_CloseSpectroEx, 0, 0, 1, 1, 0, 0);
	}

	// Dark measurement functions
	public Result<Boolean> startDarkMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_StartDarkEx(handle, integrationTime, (short)averageCount));
	}

	public Result<int[]> getDarkPixelData () {
		var darkData = new int[SPECTRUM_SIZE];
		int result = lib().JETI_DarkPixEx(handle, darkData);
		if (result != SUCCESS) return error(result);
		return success(darkData);
	}

	public Result<float[]> getDarkWaveData (int beginWavelength, int endWavelength, float stepSize) {
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var darkData = new float[dataSize];
		int result = lib().JETI_DarkWaveEx(handle, beginWavelength, endWavelength, stepSize, darkData);
		if (result != SUCCESS) return error(result);
		return success(darkData);
	}

	// Light measurement functions
	public Result<Boolean> startLightMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_StartLightEx(handle, integrationTime, (short)averageCount));
	}

	public Result<Boolean> prepareLightMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_PrepareLightEx(handle, integrationTime, (short)averageCount));
	}

	public Result<int[]> getLightPixelData () {
		var lightData = new int[SPECTRUM_SIZE];
		int result = lib().JETI_LightPixEx(handle, lightData);
		if (result != SUCCESS) return error(result);
		return success(lightData);
	}

	public Result<float[]> getLightWaveData (int beginWavelength, int endWavelength, float stepSize) {
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var lightData = new float[dataSize];
		int result = lib().JETI_LightWaveEx(handle, beginWavelength, endWavelength, stepSize, lightData);
		if (result != SUCCESS) return error(result);
		return success(lightData);
	}

	// Reference measurement functions
	public Result<Boolean> startReferenceMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_StartReferEx(handle, integrationTime, (short)averageCount));
	}

	public Result<Boolean> prepareReferenceMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_PrepareReferEx(handle, integrationTime, (short)averageCount));
	}

	public Result<int[]> getReferencePixelData () {
		var referenceData = new int[SPECTRUM_SIZE];
		int result = lib().JETI_ReferPixEx(handle, referenceData);
		if (result != SUCCESS) return error(result);
		return success(referenceData);
	}

	public Result<float[]> getReferenceWaveData (int beginWavelength, int endWavelength, float stepSize) {
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var referenceData = new float[dataSize];
		int result = lib().JETI_ReferWaveEx(handle, beginWavelength, endWavelength, stepSize, referenceData);
		if (result != SUCCESS) return error(result);
		return success(referenceData);
	}

	// Transmission/Reflection measurement functions
	public Result<Boolean> startSampleMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_StartTransReflEx(handle, integrationTime, (short)averageCount));
	}

	public Result<Boolean> prepareSampleMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_PrepareTransReflEx(handle, integrationTime, (short)averageCount));
	}

	public Result<int[]> getSamplePixelData () {
		var transReflData = new int[SPECTRUM_SIZE];
		int result = lib().JETI_TransReflPixEx(handle, transReflData);
		if (result != SUCCESS) return error(result);
		return success(transReflData);
	}

	public Result<float[]> getSampleWaveData (int beginWavelength, int endWavelength, float stepSize) {
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var transReflData = new float[dataSize];
		int result = lib().JETI_TransReflWaveEx(handle, beginWavelength, endWavelength, stepSize, transReflData);
		if (result != SUCCESS) return error(result);
		return success(transReflData);
	}

	// Image measurement functions
	public Result<Boolean> startDarkImageMeasurement (float integrationTime) {
		return result(lib().JETI_StartDarkImageEx(handle, integrationTime));
	}

	public Result<short[]> getDarkImageData () {
		var darkImageData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_DarkImageEx(handle, darkImageData);
		if (result != SUCCESS) return error(result);
		return success(darkImageData);
	}

	public Result<Boolean> startLightImageMeasurement (float integrationTime) {
		return result(lib().JETI_StartLightImageEx(handle, integrationTime));
	}

	public Result<short[]> getLightImageData () {
		var lightImageData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_LightImageEx(handle, lightImageData);
		if (result != SUCCESS) return error(result);
		return success(lightImageData);
	}

	// Channel measurement functions
	public Result<Boolean> startChannelDarkMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_StartChannelDarkEx(handle, integrationTime, (short)averageCount));
	}

	public Result<short[]> getChannelDarkData () {
		var darkData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_ChannelDarkEx(handle, darkData);
		if (result != SUCCESS) return error(result);
		return success(darkData);
	}

	public Result<Boolean> startChannelLightMeasurement (float integrationTime, int averageCount) {
		return result(lib().JETI_StartChannelLightEx(handle, integrationTime, (short)averageCount));
	}

	public Result<short[]> getChannelLightData () {
		var lightData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_ChannelLightEx(handle, lightData);
		if (result != SUCCESS) return error(result);
		return success(lightData);
	}

	// Continuous measurement functions
	public Result<Boolean> startContinuousDarkMeasurement (float interval, int count) {
		return result(lib().JETI_StartContDarkEx(handle, interval, count));
	}

	public Result<short[]> getContinuousDarkData () {
		var darkData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_ContDarkEx(handle, darkData);
		if (result != SUCCESS) return error(result);
		return success(darkData);
	}

	public Result<Boolean> startContinuousLightMeasurement (float interval, int count) {
		return result(lib().JETI_StartContLightEx(handle, interval, count));
	}

	public Result<short[]> getContinuousLightData () {
		var lightData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_ContLightEx(handle, lightData);
		if (result != SUCCESS) return error(result);
		return success(lightData);
	}

	public Result<Boolean> startContinuousChannelDarkMeasurement (float interval, int count) {
		return result(lib().JETI_StartContChannelDarkEx(handle, interval, count));
	}

	public Result<short[]> getContinuousChannelDarkData () {
		var darkData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_ContChannelDarkEx(handle, darkData);
		if (result != SUCCESS) return error(result);
		return success(darkData);
	}

	public Result<Boolean> startContinuousChannelLightMeasurement (float interval, int count) {
		return result(lib().JETI_StartContChannelLightEx(handle, interval, count));
	}

	public Result<short[]> getContinuousChannelLightData () {
		var lightData = new short[SPECTRUM_SIZE];
		int result = lib().JETI_ContChannelLightEx(handle, lightData);
		if (result != SUCCESS) return error(result);
		return success(lightData);
	}

	// Device status and control
	public Result<Boolean> getMeasurementStatus () {
		int result = lib().JETI_SpectroStatusEx(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public Result<Boolean> breakMeasurement () {
		return result(lib().JETI_SpectroBreakEx(handle));
	}

	// Device parameters
	public Result<Integer> getPixelCount () {
		int result = lib().JETI_PixelCountEx(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue());
	}

	public Result<Float> getIntegrationTime () {
		int result = lib().JETI_SpectroTintEx(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	static public Result<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = SpectroExLibrary.INSTANCE.JETI_GetNumSpectroEx(count);
		if (result != SUCCESS) return error(result);
		return success(count.getValue());
	}

	static public Result<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = SpectroExLibrary.INSTANCE.JETI_GetSerialSpectroEx(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result != SUCCESS) return error(result);
		return success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
	}

	static public Result<SpectroEx> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = SpectroExLibrary.INSTANCE.JETI_OpenSpectroEx(deviceNumber, handle);
		if (result != SUCCESS) return error(result);
		return success(new SpectroEx(handle.getValue()));
	}

	static public Result<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = SpectroExLibrary.INSTANCE.JETI_GetSpectroExDLLVersion(major, minor, build);
		if (result != SUCCESS) return error(result);
		return success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
	}
}
