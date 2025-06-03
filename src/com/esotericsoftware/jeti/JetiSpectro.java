
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
public class JetiSpectro extends Device<JetiSpectroLibrary> {
	private JetiSpectro (Pointer handle) {
		super(JetiSpectroLibrary.INSTANCE, handle);
	}

	public JetiResult<float[]> measureDarkSpectrum (float integrationTime) {
		var darkData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_DarkSpec(handle, integrationTime, darkData);
		if (result == SUCCESS) return success(darkData);
		return error(result);
	}

	public JetiResult<float[]> measureLightSpectrum (float integrationTime) {
		var lightData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_LightSpec(handle, integrationTime, lightData);
		if (result == SUCCESS) return success(lightData);
		return error(result);
	}

	public JetiResult<float[]> measureReferenceSpectrum (float integrationTime) {
		var referenceData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_ReferSpec(handle, integrationTime, referenceData);
		if (result == SUCCESS) return success(referenceData);
		return error(result);
	}

	public JetiResult<float[]> measureTransmissionReflectionSpectrum (float integrationTime) {
		var transReflData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_TransReflSpec(handle, integrationTime, transReflData);
		if (result == SUCCESS) return success(transReflData);
		return error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		int result = lib().JETI_SpectroTint(handle, floatRef);
		if (result == SUCCESS) return success(floatRef.getValue());
		return error(result);
	}

	static public JetiResult<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetNumSpectro(count);
		if (result == SUCCESS) return success(count.getValue());
		return error(result);
	}

	static public JetiResult<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetSerialSpectro(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result == SUCCESS) return success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
		return error(result);
	}

	static public JetiResult<JetiSpectro> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_OpenSpectro(deviceNumber, handle);

		if (result == SUCCESS) return success(new JetiSpectro(handle.getValue()));
		return error(result);
	}

	static public JetiResult<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetSpectroDLLVersion(major, minor, build);
		if (result == SUCCESS) return success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
		return error(result);
	}
}
