
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiSpectro extends Device {
	private JetiSpectro (Pointer handle) {
		super(handle);
	}

	public JetiResult<float[]> measureDarkSpectrum (float integrationTime) {
		ensureOpen();
		var darkData = new float[SPECTRUM_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_DarkSpec(handle, integrationTime, darkData);
		if (result == SUCCESS) return JetiResult.success(darkData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> measureLightSpectrum (float integrationTime) {
		ensureOpen();
		var lightData = new float[SPECTRUM_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_LightSpec(handle, integrationTime, lightData);
		if (result == SUCCESS) return JetiResult.success(lightData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> measureReferenceSpectrum (float integrationTime) {
		ensureOpen();
		var referenceData = new float[SPECTRUM_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_ReferSpec(handle, integrationTime, referenceData);
		if (result == SUCCESS) return JetiResult.success(referenceData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> measureTransmissionReflectionSpectrum (float integrationTime) {
		ensureOpen();
		var transReflData = new float[SPECTRUM_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_TransReflSpec(handle, integrationTime, transReflData);
		if (result == SUCCESS) return JetiResult.success(transReflData);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		ensureOpen();
		int result = JetiSpectroLibrary.INSTANCE.JETI_SpectroTint(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetNumSpectro(count);
		if (result == SUCCESS) return JetiResult.success(count.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetSerialSpectro(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result == SUCCESS)
			return JetiResult.success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiSpectro> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_OpenSpectro(deviceNumber, handle);

		if (result == SUCCESS) return JetiResult.success(new JetiSpectro(handle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiSpectroLibrary.INSTANCE.JETI_GetSpectroDLLVersion(major, minor, build);
		if (result == SUCCESS) return JetiResult.success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
		return JetiResult.error(result);
	}
}
