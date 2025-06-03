
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiRadio extends Device {
	private final FloatByReference floatRef2 = new FloatByReference(), floatRef3 = new FloatByReference();
	private final ShortByReference shortRef = new ShortByReference();

	private JetiRadio (Pointer handle) {
		super(handle);
	}

	public JetiResult<Boolean> measure () {
		ensureOpen();
		return JetiResult.result(JetiRadioLibrary.INSTANCE.JETI_Measure(handle));
	}

	public JetiResult<Boolean> measureWithAdaptation () {
		ensureOpen();
		return JetiResult.result(JetiRadioLibrary.INSTANCE.JETI_MeasureAdapt(handle));
	}

	public JetiResult<Boolean> prepareMeasurement () {
		ensureOpen();
		return JetiResult.result(JetiRadioLibrary.INSTANCE.JETI_PrepareMeasure(handle));
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_MeasureStatus(handle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue() != 0);
		return JetiResult.error(result);
	}

	public JetiResult<AdaptationStatus> getAdaptationStatus () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_MeasureAdaptStatus(handle, floatRef, shortRef, intRef);
		if (result == SUCCESS)
			return JetiResult.success(new AdaptationStatus(floatRef.getValue(), shortRef.getValue(), intRef.getValue() != 0));
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> breakMeasurement () {
		ensureOpen();
		return JetiResult.result(JetiRadioLibrary.INSTANCE.JETI_MeasureBreak(handle));
	}

	public JetiResult<float[]> getSpectralRadiance () {
		ensureOpen();
		var spectralData = new float[SPECTRUM_SIZE];
		int result = JetiRadioLibrary.INSTANCE.JETI_SpecRad(handle, spectralData);
		if (result == SUCCESS) return JetiResult.success(spectralData);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getRadiometricValue () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Radio(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getPhotometricValue () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Photo(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<XY> getChromaticityXY () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Chromxy(handle, floatRef, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new XY(floatRef.getValue(), floatRef2.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<XY10> getChromaticityXY10 () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Chromxy10(handle, floatRef, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new XY10(floatRef.getValue(), floatRef2.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<UV> getChromaticityUV () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Chromuv(handle, floatRef, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new UV(floatRef.getValue(), floatRef2.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<XYZ> getXYZ () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_ChromXYZ(handle, floatRef, floatRef2, floatRef3);
		if (result == SUCCESS) return JetiResult.success(new XYZ(floatRef.getValue(), floatRef2.getValue(), floatRef3.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<DominantWavelength> getDominantWavelength () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_DWLPE(handle, floatRef, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new DominantWavelength(floatRef.getValue(), floatRef2.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<Float> getCCT () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_CCT(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getDuv () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Duv(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getCRI () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_CRI(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_RadioTint(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> setMeasurementDistance (int mm) {
		ensureOpen();
		return JetiResult.result(JetiRadioLibrary.INSTANCE.JETI_SetMeasDist(handle, mm));
	}

	public JetiResult<Integer> getMeasurementDistance () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_GetMeasDist(handle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_GetNumRadio(count);
		if (result == SUCCESS) return JetiResult.success(count.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = JetiRadioLibrary.INSTANCE.JETI_GetSerialRadio(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result == SUCCESS)
			return JetiResult.success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiRadio> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_OpenRadio(deviceNumber, handle);
		if (result == SUCCESS) return JetiResult.success(new JetiRadio(handle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_GetRadioDLLVersion(major, minor, build);
		if (result == SUCCESS) return JetiResult.success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
		return JetiResult.error(result);
	}

	public record AdaptationStatus (float integrationTime, int averageCount, boolean complete) {}

	public record XY (float x, float y) {}

	public record XY10 (float x, float y) {}

	public record UV (float u, float v) {}

	public record XYZ (float x, float y, float z) {}

	public record DominantWavelength (float wavelength, float purity) {}
}
