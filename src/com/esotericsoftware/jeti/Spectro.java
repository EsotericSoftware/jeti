
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
public class Spectro extends Device<SpectroLibrary> {
	private Spectro (Pointer handle) {
		super(SpectroLibrary.INSTANCE, handle, SpectroLibrary.INSTANCE::JETI_CloseSpectro, 0, 0, 0, 1, 0, 0);
	}

	public Result<float[]> measureDarkSpectrum (float integrationTime) {
		var darkData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_DarkSpec(handle, integrationTime, darkData);
		if (result != SUCCESS) return error(result);
		return success(darkData);
	}

	public Result<float[]> measureLightSpectrum (float integrationTime) {
		var lightData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_LightSpec(handle, integrationTime, lightData);
		if (result != SUCCESS) return error(result);
		return success(lightData);
	}

	public Result<float[]> measureReferenceSpectrum (float integrationTime) {
		var referenceData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_ReferSpec(handle, integrationTime, referenceData);
		if (result != SUCCESS) return error(result);
		return success(referenceData);
	}

	public Result<float[]> measureTransmissionReflectionSpectrum (float integrationTime) {
		var transReflData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_TransReflSpec(handle, integrationTime, transReflData);
		if (result != SUCCESS) return error(result);
		return success(transReflData);
	}

	public Result<Float> getIntegrationTime () {
		int result = lib().JETI_SpectroTint(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	static public Result<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = SpectroLibrary.INSTANCE.JETI_GetNumSpectro(count);
		if (result != SUCCESS) return error(result);
		return success(count.getValue());
	}

	static public Result<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = SpectroLibrary.INSTANCE.JETI_GetSerialSpectro(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result != SUCCESS) return error(result);
		return success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
	}

	static public Result<Spectro> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = SpectroLibrary.INSTANCE.JETI_OpenSpectro(deviceNumber, handle);

		if (result != SUCCESS) return error(result);
		return success(new Spectro(handle.getValue()));
	}

	static public Result<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = SpectroLibrary.INSTANCE.JETI_GetSpectroDLLVersion(major, minor, build);
		if (result != SUCCESS) return error(result);
		return success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
	}
}
