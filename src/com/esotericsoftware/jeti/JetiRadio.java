
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.Result.*;
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
public class JetiRadio extends Device<JetiRadioLibrary> {
	private final float[] cri = new float[17];

	private JetiRadio (Pointer handle) {
		super(JetiRadioLibrary.INSTANCE, handle, JetiRadioLibrary.INSTANCE::JETI_CloseRadio, 0, 1, 1, 3, 0, 0);
	}

	public Result<Boolean> measure () {
		return result(lib().JETI_Measure(handle));
	}

	public Result<Boolean> measureWithAdaptation () {
		return result(lib().JETI_MeasureAdapt(handle));
	}

	public Result<Boolean> prepareMeasurement () {
		return result(lib().JETI_PrepareMeasure(handle));
	}

	public Result<Boolean> getMeasurementStatus () {
		int result = lib().JETI_MeasureStatus(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public Result<AdaptationStatus> getAdaptationStatus () {
		int result = lib().JETI_MeasureAdaptStatus(handle, f[0], s[0], i[0]);
		if (result != SUCCESS) return error(result);
		return success(new AdaptationStatus(f[0].getValue(), s[0].getValue(), i[0].getValue() != 0));
	}

	public Result<Boolean> breakMeasurement () {
		return result(lib().JETI_MeasureBreak(handle));
	}

	public Result<float[]> getSpectralRadiance () {
		var spectralData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_SpecRad(handle, spectralData);
		if (result != SUCCESS) return error(result);
		return success(spectralData);
	}

	public Result<Float> getRadiometricValue () {
		int result = lib().JETI_Radio(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public Result<Float> getPhotometricValue () {
		int result = lib().JETI_Photo(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public Result<XY> getChromaXY () {
		int result = lib().JETI_Chromxy(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new XY(f[0].getValue(), f[1].getValue()));
	}

	public Result<XY10> getChromaXY10 () {
		int result = lib().JETI_Chromxy10(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new XY10(f[0].getValue(), f[1].getValue()));
	}

	public Result<UV> getChromaUV () {
		int result = lib().JETI_Chromuv(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new UV(f[0].getValue(), f[1].getValue()));
	}

	public Result<XYZ> getXYZ () {
		int result = lib().JETI_ChromXYZ(handle, f[0], f[1], f[2]);
		if (result != SUCCESS) return error(result);
		return success(new XYZ(f[0].getValue(), f[1].getValue(), f[2].getValue()));
	}

	public Result<DominantWavelength> getDominantWavelength () {
		int result = lib().JETI_DWLPE(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new DominantWavelength(f[0].getValue(), f[1].getValue()));
	}

	public Result<Float> getCCT () {
		int result = lib().JETI_CCT(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public Result<Float> getDuv () {
		int result = lib().JETI_Duv(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public Result<CRI> getCRI () {
		int result = lib().JETI_CRI(handle, cri);
		if (result != SUCCESS) return error(result);
		float[] samples = new float[15];
		System.arraycopy(cri, 2, samples, 0, 15);
		return success(new CRI(cri[0], cri[0] / 0.0054f, cri[1], samples));
	}

	public Result<Float> getIntegrationTime () {
		int result = lib().JETI_RadioTint(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public Result<Boolean> setMeasurementDistance (int mm) {
		return result(lib().JETI_SetMeasDist(handle, mm));
	}

	public Result<Integer> getMeasurementDistance () {
		int result = lib().JETI_GetMeasDist(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue());
	}

	static public Result<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_GetNumRadio(count);
		if (result != SUCCESS) return error(result);
		return success(count.getValue());
	}

	static public Result<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = JetiRadioLibrary.INSTANCE.JETI_GetSerialRadio(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result != SUCCESS) return error(result);
		return success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
	}

	static public Result<JetiRadio> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_OpenRadio(deviceNumber, handle);
		if (result != SUCCESS) return error(result);
		return success(new JetiRadio(handle.getValue()));
	}

	static public Result<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_GetRadioDLLVersion(major, minor, build);
		if (result != SUCCESS) return error(result);
		return success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
	}
}
