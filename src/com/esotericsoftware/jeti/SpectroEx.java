
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

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

	public void startDarkMeasurement (float integrationTime, int averageCount) {
		check(lib().JETI_StartDarkEx(handle, integrationTime, (short)averageCount));
	}

	public int[] getDarkPixelData (int pixelCount) {
		var darkData = new int[pixelCount];
		check(lib().JETI_DarkPixEx(handle, darkData));
		return darkData;
	}

	public float[] getDarkWaveData (int beginWavelength, int endWavelength, float stepSize) {
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var darkData = new float[dataSize];
		check(lib().JETI_DarkWaveEx(handle, beginWavelength, endWavelength, stepSize, darkData));
		return darkData;
	}

	// Light measurement functions

	public void startLightMeasurement (float integrationTime, int averageCount) {
		check(lib().JETI_StartLightEx(handle, integrationTime, (short)averageCount));
	}

	public void prepareLightMeasurement (float integrationTime, int averageCount) {
		check(lib().JETI_PrepareLightEx(handle, integrationTime, (short)averageCount));
	}

	public int[] getLightPixelData (int pixelCount) {
		var lightData = new int[pixelCount];
		check(lib().JETI_LightPixEx(handle, lightData));
		return lightData;
	}

	public float[] getLightWaveData (int beginWavelength, int endWavelength, float stepSize) {
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var lightData = new float[dataSize];
		check(lib().JETI_LightWaveEx(handle, beginWavelength, endWavelength, stepSize, lightData));
		return lightData;
	}

	// Reference measurement functions

	public void startReferenceMeasurement (float integrationTime, int averageCount) {
		check(lib().JETI_StartReferEx(handle, integrationTime, (short)averageCount));
	}

	public void prepareReferenceMeasurement (float integrationTime, int averageCount) {
		check(lib().JETI_PrepareReferEx(handle, integrationTime, (short)averageCount));
	}

	public int[] getReferencePixelData (int pixelCount) {
		var referenceData = new int[pixelCount];
		check(lib().JETI_ReferPixEx(handle, referenceData));
		return referenceData;
	}

	public float[] getReferenceWaveData (int beginWavelength, int endWavelength, float stepSize) {
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var referenceData = new float[dataSize];
		check(lib().JETI_ReferWaveEx(handle, beginWavelength, endWavelength, stepSize, referenceData));
		return referenceData;
	}

	// Sample measurement functions

	public void startSampleMeasurement (float integrationTime, int averageCount) {
		check(lib().JETI_StartTransReflEx(handle, integrationTime, (short)averageCount));
	}

	public void prepareSampleMeasurement (float integrationTime, int averageCount) {
		check(lib().JETI_PrepareTransReflEx(handle, integrationTime, (short)averageCount));
	}

	public int[] getSamplePixelData (int pixelCount) {
		var transReflData = new int[pixelCount];
		check(lib().JETI_TransReflPixEx(handle, transReflData));
		return transReflData;
	}

	public float[] getSampleWaveData (int beginWavelength, int endWavelength, float stepSize) {
		int dataSize = (int)((endWavelength - beginWavelength) / stepSize + 1);
		var transReflData = new float[dataSize];
		check(lib().JETI_TransReflWaveEx(handle, beginWavelength, endWavelength, stepSize, transReflData));
		return transReflData;
	}

	// Image measurement functions

	public void startDarkImageMeasurement (float integrationTime) {
		check(lib().JETI_StartDarkImageEx(handle, integrationTime));
	}

	public short[] getDarkImageData () {
		var darkImageData = new short[SPECTRUM_SIZE]; // BOZO - Size?
		check(lib().JETI_DarkImageEx(handle, darkImageData));
		return darkImageData;
	}

	public void startLightImageMeasurement (float integrationTime) {
		check(lib().JETI_StartLightImageEx(handle, integrationTime));
	}

	public short[] getLightImageData () {
		var lightImageData = new short[SPECTRUM_SIZE];
		check(lib().JETI_LightImageEx(handle, lightImageData));
		return lightImageData;
	}

	// Channel measurement functions

	public void startChannelDarkMeasurement (float integrationTime, int averageCount) {
		check(lib().JETI_StartChannelDarkEx(handle, integrationTime, (short)averageCount));
	}

	public short[] getChannelDarkData () {
		var darkData = new short[SPECTRUM_SIZE]; // BOZO - Size?
		check(lib().JETI_ChannelDarkEx(handle, darkData));
		return darkData;
	}

	public void startChannelLightMeasurement (float integrationTime, int averageCount) {
		check(lib().JETI_StartChannelLightEx(handle, integrationTime, (short)averageCount));
	}

	public short[] getChannelLightData () {
		var lightData = new short[SPECTRUM_SIZE]; // BOZO - Size?
		check(lib().JETI_ChannelLightEx(handle, lightData));
		return lightData;
	}

	// Continuous measurement functions

	public void startContinuousDarkMeasurement (float interval, int count) {
		check(lib().JETI_StartContDarkEx(handle, interval, count));
	}

	public short[] getContinuousDarkData () {
		var darkData = new short[SPECTRUM_SIZE]; // BOZO - Size?
		check(lib().JETI_ContDarkEx(handle, darkData));
		return darkData;
	}

	public void startContinuousLightMeasurement (float interval, int count) {
		check(lib().JETI_StartContLightEx(handle, interval, count));
	}

	public short[] getContinuousLightData () {
		var lightData = new short[SPECTRUM_SIZE]; // BOZO - Size?
		check(lib().JETI_ContLightEx(handle, lightData));
		return lightData;
	}

	public void startContinuousChannelDarkMeasurement (float interval, int count) {
		check(lib().JETI_StartContChannelDarkEx(handle, interval, count));
	}

	public short[] getContinuousChannelDarkData () {
		var darkData = new short[SPECTRUM_SIZE]; // BOZO - Size?
		check(lib().JETI_ContChannelDarkEx(handle, darkData));
		return darkData;
	}

	public void startContinuousChannelLightMeasurement (float interval, int count) {
		check(lib().JETI_StartContChannelLightEx(handle, interval, count));
	}

	public short[] getContinuousChannelLightData () {
		var lightData = new short[SPECTRUM_SIZE]; // BOZO - Size?
		check(lib().JETI_ContChannelLightEx(handle, lightData));
		return lightData;
	}

	// Device status and control

	public boolean isMeasuring () {
		check(lib().JETI_SpectroStatusEx(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void cancelMeasurement () {
		check(lib().JETI_SpectroBreakEx(handle));
	}

	// Device parameters

	public int getPixelCount () {
		check(lib().JETI_PixelCountEx(handle, i[0]));
		return i[0].getValue();
	}

	public float getIntegrationTime () {
		check(lib().JETI_SpectroTintEx(handle, f[0]));
		return f[0].getValue();
	}

	static public int getDeviceCount () {
		var count = new IntByReference();
		check(SpectroExLibrary.INSTANCE.JETI_GetNumSpectroEx(count));
		return count.getValue();
	}

	static public DeviceSerials getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		check(SpectroExLibrary.INSTANCE.JETI_GetSerialSpectroEx(deviceNumber, boardSerial, specSerial, deviceSerial));
		return new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial));
	}

	static public SpectroEx openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		check(SpectroExLibrary.INSTANCE.JETI_OpenSpectroEx(deviceNumber, handle));
		return new SpectroEx(handle.getValue());
	}

	static public DllVersion getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		check(SpectroExLibrary.INSTANCE.JETI_GetSpectroExDLLVersion(major, minor, build));
		return new DllVersion(major.getValue(), minor.getValue(), build.getValue());
	}
}
