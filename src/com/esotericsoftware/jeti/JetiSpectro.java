
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
		if (result != SUCCESS) return error(result);
		return success(darkData);
	}

	public JetiResult<float[]> measureLightSpectrum (float integrationTime) {
		var lightData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_LightSpec(handle, integrationTime, lightData);
		if (result != SUCCESS) return error(result);
		return success(lightData);
	}

	public JetiResult<float[]> measureReferenceSpectrum (float integrationTime) {
		var referenceData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_ReferSpec(handle, integrationTime, referenceData);
		if (result != SUCCESS) return error(result);
		return success(referenceData);
	}

	public JetiResult<float[]> measureTransmissionReflectionSpectrum (float integrationTime) {
		var transReflData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_TransReflSpec(handle, integrationTime, transReflData);
		if (result != SUCCESS) return error(result);
		return success(transReflData);
	}

	public JetiResult<Float> getIntegrationTime () {
		int result = lib().JETI_SpectroTint(handle, floatRef);
		if (result != SUCCESS) return error(result);
		return success(floatRef.getValue());
	}

	static public JetiResult<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetNumSpectro(count);
		if (result != SUCCESS) return error(result);
		return success(count.getValue());
	}

	static public JetiResult<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetSerialSpectro(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result != SUCCESS) return error(result);
		return success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
	}

	static public JetiResult<JetiSpectro> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_OpenSpectro(deviceNumber, handle);

		if (result != SUCCESS) return error(result);
		return success(new JetiSpectro(handle.getValue()));
	}

	static public JetiResult<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetSpectroDLLVersion(major, minor, build);
		if (result != SUCCESS) return error(result);
		return success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
	}
}
