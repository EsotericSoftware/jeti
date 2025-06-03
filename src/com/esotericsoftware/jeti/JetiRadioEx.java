
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiResult.*;
import static com.esotericsoftware.jeti.JetiSDK.*;

import com.esotericsoftware.jeti.JetiRadio.AdaptationStatus;
import com.esotericsoftware.jeti.JetiRadio.DominantWavelength;
import com.esotericsoftware.jeti.JetiRadio.UV;
import com.esotericsoftware.jeti.JetiRadio.XY;
import com.esotericsoftware.jeti.JetiRadio.XY10;
import com.esotericsoftware.jeti.JetiRadio.XYZ;
import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiRadioEx extends Device<JetiRadioExLibrary> {
	private final FloatByReference floatRef2 = new FloatByReference(), floatRef3 = new FloatByReference(),
		floatRef4 = new FloatByReference(), floatRef5 = new FloatByReference(), floatRef6 = new FloatByReference();
	private final DoubleByReference doubleRef = new DoubleByReference(), doubleRef2 = new DoubleByReference(),
		doubleRef3 = new DoubleByReference(), doubleRef4 = new DoubleByReference();
	private final ShortByReference shortRef = new ShortByReference();
	private final float[] cri = new float[17];

	private JetiRadioEx (Pointer handle) {
		super(JetiRadioExLibrary.INSTANCE, handle);
	}

	// Measurement functions
	public JetiResult<Boolean> measure (float integrationTime, int averageCount, int step) {
		return result(lib().JETI_MeasureEx(handle, integrationTime, (short)averageCount, step));
	}

	public JetiResult<Boolean> measureWithAdaptation (int averageCount, int step) {
		return result(lib().JETI_MeasureAdaptEx(handle, (short)averageCount, step));
	}

	public JetiResult<Boolean> prepareMeasurement (float integrationTime, int averageCount, int step) {
		return result(lib().JETI_PrepareMeasureEx(handle, integrationTime, (short)averageCount, step));
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		int result = lib().JETI_MeasureStatusEx(handle, intRef);
		if (result != SUCCESS) return error(result);
		return success(intRef.getValue() != 0);
	}

	public JetiResult<AdaptationStatus> getAdaptationStatus () {
		int result = lib().JETI_MeasureAdaptStatusEx(handle, floatRef, shortRef, intRef);
		if (result != SUCCESS) return error(result);
		return success(new AdaptationStatus(floatRef.getValue(), shortRef.getValue(), intRef.getValue() != 0));
	}

	public JetiResult<Boolean> breakMeasurement () {
		return result(lib().JETI_MeasureBreakEx(handle));
	}

	// Spectral data functions
	public JetiResult<float[]> getSpectralRadiance (int beginWavelength, int endWavelength, float step) {
		int dataSize = (int)((endWavelength - beginWavelength) / step + 1);
		var spectralData = new float[dataSize];
		int result = lib().JETI_SpecRadEx(handle, beginWavelength, endWavelength, spectralData);
		if (result != SUCCESS) return error(result);
		return success(spectralData);
	}

	public JetiResult<float[]> getSpectralRadianceHiRes (int beginWavelength, int endWavelength) {
		int dataSize = (int)((endWavelength - beginWavelength) / 0.1f + 1);
		var spectralData = new float[dataSize];
		int result = lib().JETI_SpecRadHiResEx(handle, beginWavelength, endWavelength, spectralData);
		if (result != SUCCESS) return error(result);
		return success(spectralData);
	}

	public JetiResult<Boolean> saveSpectralRadianceSPC (int beginWavelength, int endWavelength, String filePath, String operator,
		String memo) {
		return result(lib().JETI_SaveSpecRadSPCEx(handle, beginWavelength, endWavelength, filePath, operator, memo));
	}

	public JetiResult<Boolean> saveSpectralRadianceCSV (int beginWavelength, int endWavelength, String pathName, String operator,
		String memo) {
		return result(lib().JETI_SaveSpecRadCSVEx(handle, beginWavelength, endWavelength, pathName, operator, memo));
	}

	// Measurement data functions
	public JetiResult<Float> getRadiometricValue (int beginWavelength, int endWavelength) {
		int result = lib().JETI_RadioEx(handle, beginWavelength, endWavelength, floatRef);
		if (result != SUCCESS) return error(result);
		return success(floatRef.getValue());
	}

	public JetiResult<Float> getPhotometricValue () {
		int result = lib().JETI_PhotoEx(handle, floatRef);
		if (result != SUCCESS) return error(result);
		return success(floatRef.getValue());
	}

	public JetiResult<XY> getChromaticityXY () {
		int result = lib().JETI_ChromxyEx(handle, floatRef, floatRef2);
		if (result != SUCCESS) return error(result);
		return success(new XY(floatRef.getValue(), floatRef2.getValue()));
	}

	public JetiResult<XY10> getChromaticityXY10 () {
		int result = lib().JETI_Chromxy10Ex(handle, floatRef, floatRef2);
		if (result != SUCCESS) return error(result);
		return success(new XY10(floatRef.getValue(), floatRef2.getValue()));
	}

	public JetiResult<UV> getChromaticityUV () {
		int result = lib().JETI_ChromuvEx(handle, floatRef, floatRef2);
		if (result != SUCCESS) return error(result);
		return success(new UV(floatRef.getValue(), floatRef2.getValue()));
	}

	public JetiResult<XYZ> getXYZ () {
		int result = lib().JETI_ChromXYZEx(handle, floatRef, floatRef2, floatRef3);
		if (result != SUCCESS) return error(result);
		return success(new XYZ(floatRef.getValue(), floatRef2.getValue(), floatRef3.getValue()));
	}

	public JetiResult<DominantWavelength> getDominantWavelength () {
		int result = lib().JETI_DWLPEEx(handle, floatRef, floatRef2);
		if (result != SUCCESS) return error(result);
		return success(new DominantWavelength(floatRef.getValue(), floatRef2.getValue()));
	}

	public JetiResult<Float> getCCT () {
		int result = lib().JETI_CCTEx(handle, floatRef);
		if (result != SUCCESS) return error(result);
		return success(floatRef.getValue());
	}

	public JetiResult<Float> getDuv () {
		int result = lib().JETI_DuvEx(handle, floatRef);
		if (result != SUCCESS) return error(result);
		return success(floatRef.getValue());
	}

	public JetiResult<CRIData> getCRI (float cct) {
		int result = lib().JETI_CRIEx(handle, cct, cri);
		if (result != SUCCESS) return error(result);
		float[] samples = new float[15];
		System.arraycopy(cri, 2, samples, 0, 15);
		return success(new CRIData(cri[0], cri[0] / 0.0054f, cri[1], samples));
	}

	public JetiResult<TM30Data> getTM30 (boolean useTM3015) {
		var rfi = new double[16];
		var rfces = new double[99];
		int result = lib().JETI_TM30Ex(handle, (byte)(useTM3015 ? 1 : 0), doubleRef, doubleRef2, doubleRef3, doubleRef4, rfi,
			rfces);
		if (result != SUCCESS) return error(result);
		return success(
			new TM30Data(doubleRef.getValue(), doubleRef2.getValue(), doubleRef3.getValue(), doubleRef4.getValue(), rfi, rfces));
	}

	public JetiResult<PeakFWHMData> getPeakFWHM (float threshold) {
		int result = lib().JETI_PeakFWHMEx(handle, threshold, floatRef, floatRef2);
		if (result != SUCCESS) return error(result);
		return success(new PeakFWHMData(floatRef.getValue(), floatRef2.getValue()));
	}

	public JetiResult<BlueMeasurementData> getBlueMeasurement () {
		int result = lib().JETI_BlueMeasurementEx(handle, floatRef, floatRef2, floatRef3, floatRef4, floatRef5, floatRef6);
		if (result != SUCCESS) return error(result);
		return success(new BlueMeasurementData(floatRef.getValue(), floatRef2.getValue(), floatRef3.getValue(),
			floatRef4.getValue(), floatRef5.getValue(), floatRef6.getValue()));
	}

	public JetiResult<Float> getIntegrationTime () {
		int result = lib().JETI_RadioTintEx(handle, floatRef);
		if (result != SUCCESS) return error(result);
		return success(floatRef.getValue());
	}

	public JetiResult<Boolean> setMeasurementDistance (int distance) {
		return result(lib().JETI_SetMeasDistEx(handle, distance));
	}

	public JetiResult<Integer> getMeasurementDistance () {
		int result = lib().JETI_GetMeasDistEx(handle, intRef);
		if (result != SUCCESS) return error(result);
		return success(intRef.getValue());
	}

	static public JetiResult<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_GetNumRadioEx(count);
		if (result != SUCCESS) return error(result);
		return success(count.getValue());
	}

	static public JetiResult<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = JetiRadioExLibrary.INSTANCE.JETI_GetSerialRadioEx(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result != SUCCESS) return error(result);
		return success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
	}

	static public JetiResult<JetiRadioEx> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_OpenRadioEx(deviceNumber, handle);
		if (result != SUCCESS) return error(result);
		return success(new JetiRadioEx(handle.getValue()));
	}

	static public JetiResult<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_GetRadioExDLLVersion(major, minor, build);
		if (result != SUCCESS) return error(result);
		return success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
	}

	static public record TM30Data (
		/** Fidelity index (0-100, like CRI but better). */
		double rf,
		/** Gamut index (color saturation, <100 = less saturated, >100 = more). */
		double rg,
		double avgChromaShift,
		double avgHueShift,
		/** Fidelity indices for 99 color samples (Rf,CES1 through Rf,CES99). */
		double[] colorSamples,
		/** Color fidelity by hue angle bins (15 or 16 bins). */
		double[] hueAngleBins) {}

	static public record PeakFWHMData (float peak, float fwhm) {}

	static public record BlueMeasurementData (
		float hazardRadiance,
		float hazardEfficacy,
		float circadianEfficacy,
		float bluePeakRatio,
		/** 415-455nm vs 400-500nm */
		float blueContentRatio,
		float nonBluePeakRatio) {}

	static public record CRIData (float dcError, float inaccuracyPercent, float ra, float[] samples) {}
}
