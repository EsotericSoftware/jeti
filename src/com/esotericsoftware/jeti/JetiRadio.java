
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiResult.*;
import static com.esotericsoftware.jeti.JetiSDK.*;

import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiRadio extends Device<JetiRadioLibrary> {
	private final FloatByReference floatRef2 = new FloatByReference(), floatRef3 = new FloatByReference();
	private final ShortByReference shortRef = new ShortByReference();

	private JetiRadio (Pointer handle) {
		super(JetiRadioLibrary.INSTANCE, handle);
	}

	public JetiResult<Boolean> measure () {
		return result(lib().JETI_Measure(handle));
	}

	public JetiResult<Boolean> measureWithAdaptation () {
		return result(lib().JETI_MeasureAdapt(handle));
	}

	public JetiResult<Boolean> prepareMeasurement () {
		return result(lib().JETI_PrepareMeasure(handle));
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		int result = lib().JETI_MeasureStatus(handle, intRef);
		if (result == SUCCESS) return success(intRef.getValue() != 0);
		return error(result);
	}

	public JetiResult<AdaptationStatus> getAdaptationStatus () {
		int result = lib().JETI_MeasureAdaptStatus(handle, floatRef, shortRef, intRef);
		if (result == SUCCESS)
			return success(new AdaptationStatus(floatRef.getValue(), shortRef.getValue(), intRef.getValue() != 0));
		return error(result);
	}

	public JetiResult<Boolean> breakMeasurement () {
		return result(lib().JETI_MeasureBreak(handle));
	}

	public JetiResult<float[]> getSpectralRadiance () {
		var spectralData = new float[SPECTRUM_SIZE];
		int result = lib().JETI_SpecRad(handle, spectralData);
		if (result == SUCCESS) return success(spectralData);
		return error(result);
	}

	public JetiResult<Float> getRadiometricValue () {
		int result = lib().JETI_Radio(handle, floatRef);
		if (result == SUCCESS) return success(floatRef.getValue());
		return error(result);
	}

	public JetiResult<Float> getPhotometricValue () {
		int result = lib().JETI_Photo(handle, floatRef);
		if (result == SUCCESS) return success(floatRef.getValue());
		return error(result);
	}

	public JetiResult<XY> getChromaticityXY () {
		int result = lib().JETI_Chromxy(handle, floatRef, floatRef2);
		if (result == SUCCESS) return success(new XY(floatRef.getValue(), floatRef2.getValue()));
		return error(result);
	}

	public JetiResult<XY10> getChromaticityXY10 () {
		int result = lib().JETI_Chromxy10(handle, floatRef, floatRef2);
		if (result == SUCCESS) return success(new XY10(floatRef.getValue(), floatRef2.getValue()));
		return error(result);
	}

	public JetiResult<UV> getChromaticityUV () {
		int result = lib().JETI_Chromuv(handle, floatRef, floatRef2);
		if (result == SUCCESS) return success(new UV(floatRef.getValue(), floatRef2.getValue()));
		return error(result);
	}

	public JetiResult<XYZ> getXYZ () {
		int result = lib().JETI_ChromXYZ(handle, floatRef, floatRef2, floatRef3);
		if (result == SUCCESS) return success(new XYZ(floatRef.getValue(), floatRef2.getValue(), floatRef3.getValue()));
		return error(result);
	}

	public JetiResult<DominantWavelength> getDominantWavelength () {
		int result = lib().JETI_DWLPE(handle, floatRef, floatRef2);
		if (result == SUCCESS) return success(new DominantWavelength(floatRef.getValue(), floatRef2.getValue()));
		return error(result);
	}

	public JetiResult<Float> getCCT () {
		int result = lib().JETI_CCT(handle, floatRef);
		if (result == SUCCESS) return success(floatRef.getValue());
		return error(result);
	}

	public JetiResult<Float> getDuv () {
		int result = lib().JETI_Duv(handle, floatRef);
		if (result == SUCCESS) return success(floatRef.getValue());
		return error(result);
	}

	public JetiResult<Float> getCRI () {
		int result = lib().JETI_CRI(handle, floatRef);
		if (result == SUCCESS) return success(floatRef.getValue());
		return error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		int result = lib().JETI_RadioTint(handle, floatRef);
		if (result == SUCCESS) return success(floatRef.getValue());
		return error(result);
	}

	public JetiResult<Boolean> setMeasurementDistance (int mm) {
		return result(lib().JETI_SetMeasDist(handle, mm));
	}

	public JetiResult<Integer> getMeasurementDistance () {
		int result = lib().JETI_GetMeasDist(handle, intRef);
		if (result == SUCCESS) return success(intRef.getValue());
		return error(result);
	}

	static public JetiResult<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_GetNumRadio(count);
		if (result == SUCCESS) return success(count.getValue());
		return error(result);
	}

	static public JetiResult<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = JetiRadioLibrary.INSTANCE.JETI_GetSerialRadio(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result == SUCCESS) return success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
		return error(result);
	}

	static public JetiResult<JetiRadio> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_OpenRadio(deviceNumber, handle);
		if (result == SUCCESS) return success(new JetiRadio(handle.getValue()));
		return error(result);
	}

	static public JetiResult<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_GetRadioDLLVersion(major, minor, build);
		if (result == SUCCESS) return success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
		return error(result);
	}

	public record AdaptationStatus (float integrationTime, int averageCount, boolean complete) {}

	public record XY (float x, float y) {}

	public record XY10 (float x, float y) {}

	public record UV (float u, float v) {}

	public record XYZ (float x, float y, float z) {}

	public record DominantWavelength (float wavelength, float purity) {}
}
