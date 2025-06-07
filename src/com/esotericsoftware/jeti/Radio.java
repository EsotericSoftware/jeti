
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import com.esotericsoftware.jeti.JetiSDK.AdaptationStatus;
import com.esotericsoftware.jeti.JetiSDK.CRI;
import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;
import com.esotericsoftware.jeti.JetiSDK.DominantWavelength;
import com.esotericsoftware.jeti.JetiSDK.UV;
import com.esotericsoftware.jeti.JetiSDK.XY;
import com.esotericsoftware.jeti.JetiSDK.XY10;
import com.esotericsoftware.jeti.JetiSDK.XYZ;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class Radio extends Device<RadioLibrary> {
	private final float[] cri = new float[17];

	private Radio (Pointer handle) {
		super(RadioLibrary.INSTANCE, handle, RadioLibrary.INSTANCE::JETI_CloseRadio, 0, 1, 1, 3, 0, 0);
	}

	public void measure () {
		check(lib().JETI_Measure(handle));
	}

	public void measureWithAdaptation () {
		check(lib().JETI_MeasureAdapt(handle));
	}

	public void prepareMeasurement () {
		check(lib().JETI_PrepareMeasure(handle));
	}

	public boolean isMeasuring () {
		check(lib().JETI_MeasureStatus(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public AdaptationStatus getAdaptationStatus () {
		check(lib().JETI_MeasureAdaptStatus(handle, f[0], s[0], i[0]));
		return new AdaptationStatus(f[0].getValue(), s[0].getValue(), i[0].getValue() != 0);
	}

	public void cancelMeasurement () {
		check(lib().JETI_MeasureBreak(handle));
	}

	public float[] getSpectralRadiance () {
		var spectralData = new float[SPECTRUM_SIZE];
		check(lib().JETI_SpecRad(handle, spectralData));
		return spectralData;
	}

	public float getRadiometricValue () {
		check(lib().JETI_Radio(handle, f[0]));
		return f[0].getValue();
	}

	public float getPhotometricValue () {
		check(lib().JETI_Photo(handle, f[0]));
		return f[0].getValue();
	}

	public XY getChromaXY () {
		check(lib().JETI_Chromxy(handle, f[0], f[1]));
		return new XY(f[0].getValue(), f[1].getValue());
	}

	public XY10 getChromaXY10 () {
		check(lib().JETI_Chromxy10(handle, f[0], f[1]));
		return new XY10(f[0].getValue(), f[1].getValue());
	}

	public UV getChromaUV () {
		check(lib().JETI_Chromuv(handle, f[0], f[1]));
		return new UV(f[0].getValue(), f[1].getValue());
	}

	public XYZ getXYZ () {
		check(lib().JETI_ChromXYZ(handle, f[0], f[1], f[2]));
		return new XYZ(f[0].getValue(), f[1].getValue(), f[2].getValue());
	}

	public DominantWavelength getDominantWavelength () {
		check(lib().JETI_DWLPE(handle, f[0], f[1]));
		return new DominantWavelength(f[0].getValue(), f[1].getValue());
	}

	public float getCCT () {
		check(lib().JETI_CCT(handle, f[0]));
		return f[0].getValue();
	}

	public float getDuv () {
		check(lib().JETI_Duv(handle, f[0]));
		return f[0].getValue();
	}

	public CRI getCRI () {
		check(lib().JETI_CRI(handle, cri));
		float[] samples = new float[15];
		System.arraycopy(cri, 2, samples, 0, 15);
		return new CRI(cri[0], cri[0] / 0.0054f, cri[1], samples);
	}

	public float getIntegrationTime () {
		check(lib().JETI_RadioTint(handle, f[0]));
		return f[0].getValue();
	}

	public void setMeasurementDistance (int mm) {
		check(lib().JETI_SetMeasDist(handle, mm));
	}

	public int getMeasurementDistance () {
		check(lib().JETI_GetMeasDist(handle, i[0]));
		return i[0].getValue();
	}

	static public int getDeviceCount () {
		var count = new IntByReference();
		check(RadioLibrary.INSTANCE.JETI_GetNumRadio(count));
		return count.getValue();
	}

	static public DeviceSerials getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		check(RadioLibrary.INSTANCE.JETI_GetSerialRadio(deviceNumber, boardSerial, specSerial, deviceSerial));
		return new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial));
	}

	static public Radio openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		check(RadioLibrary.INSTANCE.JETI_OpenRadio(deviceNumber, handle));
		return new Radio(handle.getValue());
	}

	static public Radio openDevice () {
		if (getDeviceCount() <= 0) throw new JetiException(INVALID_DEVICE_NUMBER, "No Radio device found.");
		return openDevice(0);
	}

	static public DllVersion getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		check(RadioLibrary.INSTANCE.JETI_GetRadioDLLVersion(major, minor, build));
		return new DllVersion(major.getValue(), minor.getValue(), build.getValue());
	}
}
