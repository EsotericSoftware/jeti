
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

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

	public float[] measureDarkSpectrum (float integrationTime) {
		var darkData = new float[SPECTRUM_SIZE];
		check(lib().JETI_DarkSpec(handle, integrationTime, darkData));
		return darkData;
	}

	public float[] measureLightSpectrum (float integrationTime) {
		var lightData = new float[SPECTRUM_SIZE];
		check(lib().JETI_LightSpec(handle, integrationTime, lightData));
		return lightData;
	}

	public float[] measureReferenceSpectrum (float integrationTime) {
		var referenceData = new float[SPECTRUM_SIZE];
		check(lib().JETI_ReferSpec(handle, integrationTime, referenceData));
		return referenceData;
	}

	public float[] measureSampleSpectrum (float integrationTime) {
		var transReflData = new float[SPECTRUM_SIZE];
		check(lib().JETI_TransReflSpec(handle, integrationTime, transReflData));
		return transReflData;
	}

	public float getIntegrationTime () {
		check(lib().JETI_SpectroTint(handle, f[0]));
		return f[0].getValue();
	}

	static public int getDeviceCount () {
		var count = new IntByReference();
		check(SpectroLibrary.INSTANCE.JETI_GetNumSpectro(count));
		return count.getValue();
	}

	static public DeviceSerials getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		check(SpectroLibrary.INSTANCE.JETI_GetSerialSpectro(deviceNumber, boardSerial, specSerial, deviceSerial));
		return new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial));
	}

	static public Spectro openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		check(SpectroLibrary.INSTANCE.JETI_OpenSpectro(deviceNumber, handle));
		return new Spectro(handle.getValue());
	}

	static public DllVersion getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		check(SpectroLibrary.INSTANCE.JETI_GetSpectroDLLVersion(major, minor, build));
		return new DllVersion(major.getValue(), minor.getValue(), build.getValue());
	}
}
