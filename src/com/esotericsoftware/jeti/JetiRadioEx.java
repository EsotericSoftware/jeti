
package com.esotericsoftware.jeti;

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
public class JetiRadioEx extends Device {
	private final FloatByReference floatRef2 = new FloatByReference(), floatRef3 = new FloatByReference(),
		floatRef4 = new FloatByReference(), floatRef5 = new FloatByReference(), floatRef6 = new FloatByReference();
	private final DoubleByReference doubleRef = new DoubleByReference(), doubleRef2 = new DoubleByReference(),
		doubleRef3 = new DoubleByReference(), doubleRef4 = new DoubleByReference();
	private final ShortByReference shortRef = new ShortByReference();
	private final float[] cri = new float[17];

	private JetiRadioEx (Pointer handle) {
		super(handle);
	}

	// Measurement functions
	public JetiResult<Boolean> measure (float integrationTime, int averageCount, int step) {
		ensureOpen();
		return JetiResult.result(JetiRadioExLibrary.INSTANCE.JETI_MeasureEx(handle, integrationTime, (short)averageCount, step));
	}

	public JetiResult<Boolean> measureWithAdaptation (int averageCount, int step) {
		ensureOpen();
		return JetiResult.result(JetiRadioExLibrary.INSTANCE.JETI_MeasureAdaptEx(handle, (short)averageCount, step));
	}

	public JetiResult<Boolean> prepareMeasurement (float integrationTime, int averageCount, int step) {
		ensureOpen();
		return JetiResult
			.result(JetiRadioExLibrary.INSTANCE.JETI_PrepareMeasureEx(handle, integrationTime, (short)averageCount, step));
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureStatusEx(handle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue() != 0);
		return JetiResult.error(result);
	}

	public JetiResult<AdaptationStatus> getAdaptationStatus () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureAdaptStatusEx(handle, floatRef, shortRef, intRef);
		if (result == SUCCESS)
			return JetiResult.success(new AdaptationStatus(floatRef.getValue(), shortRef.getValue(), intRef.getValue() != 0));
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> breakMeasurement () {
		ensureOpen();
		return JetiResult.result(JetiRadioExLibrary.INSTANCE.JETI_MeasureBreakEx(handle));
	}

	// Spectral data functions
	public JetiResult<float[]> getSpectralRadiance (int beginWavelength, int endWavelength, float step) {
		ensureOpen();
		int dataSize = (int)((endWavelength - beginWavelength) / step + 1);
		var spectralData = new float[dataSize];
		int result = JetiRadioExLibrary.INSTANCE.JETI_SpecRadEx(handle, beginWavelength, endWavelength, spectralData);
		if (result == SUCCESS) return JetiResult.success(spectralData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getSpectralRadianceHiRes (int beginWavelength, int endWavelength) {
		ensureOpen();
		int dataSize = (int)((endWavelength - beginWavelength) / 0.1f + 1);
		var spectralData = new float[dataSize];
		int result = JetiRadioExLibrary.INSTANCE.JETI_SpecRadHiResEx(handle, beginWavelength, endWavelength, spectralData);
		if (result == SUCCESS) return JetiResult.success(spectralData);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> saveSpectralRadianceSPC (int beginWavelength, int endWavelength, String filePath, String operator,
		String memo) {
		ensureOpen();
		return JetiResult.result(
			JetiRadioExLibrary.INSTANCE.JETI_SaveSpecRadSPCEx(handle, beginWavelength, endWavelength, filePath, operator, memo));
	}

	public JetiResult<Boolean> saveSpectralRadianceCSV (int beginWavelength, int endWavelength, String pathName, String operator,
		String memo) {
		ensureOpen();
		return JetiResult.result(
			JetiRadioExLibrary.INSTANCE.JETI_SaveSpecRadCSVEx(handle, beginWavelength, endWavelength, pathName, operator, memo));
	}

	// Measurement data functions
	public JetiResult<Float> getRadiometricValue (int beginWavelength, int endWavelength) {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_RadioEx(handle, beginWavelength, endWavelength, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getPhotometricValue () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_PhotoEx(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<XY> getChromaticityXY () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_ChromxyEx(handle, floatRef, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new XY(floatRef.getValue(), floatRef2.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<XY10> getChromaticityXY10 () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_Chromxy10Ex(handle, floatRef, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new XY10(floatRef.getValue(), floatRef2.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<UV> getChromaticityUV () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_ChromuvEx(handle, floatRef, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new UV(floatRef.getValue(), floatRef2.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<XYZ> getXYZ () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_ChromXYZEx(handle, floatRef, floatRef2, floatRef3);
		if (result == SUCCESS) return JetiResult.success(new XYZ(floatRef.getValue(), floatRef2.getValue(), floatRef3.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<DominantWavelength> getDominantWavelength () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_DWLPEEx(handle, floatRef, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new DominantWavelength(floatRef.getValue(), floatRef2.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<Float> getCCT () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_CCTEx(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getDuv () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_DuvEx(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<CRIData> getCRI (float cct) {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_CRIEx(handle, cct, cri);
		if (result == SUCCESS) {
			float[] samples = new float[15];
			System.arraycopy(cri, 2, samples, 0, 15);
			return JetiResult.success(new CRIData(cri[0], cri[0] / 0.0054f, cri[1], samples));
		}
		return JetiResult.error(result);
	}

	public JetiResult<TM30Data> getTM30 (boolean useTM3015) {
		ensureOpen();
		var rfi = new double[16];
		var rfces = new double[99];
		int result = JetiRadioExLibrary.INSTANCE.JETI_TM30Ex(handle, (byte)(useTM3015 ? 1 : 0), doubleRef, doubleRef2, doubleRef3,
			doubleRef4, rfi, rfces);
		if (result == SUCCESS) {
			return JetiResult.success(
				new TM30Data(doubleRef.getValue(), doubleRef2.getValue(), doubleRef3.getValue(), doubleRef4.getValue(), rfi, rfces));
		}
		return JetiResult.error(result);
	}

	public JetiResult<PeakFWHMData> getPeakFWHM (float threshold) {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_PeakFWHMEx(handle, threshold, floatRef, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new PeakFWHMData(floatRef.getValue(), floatRef2.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<BlueMeasurementData> getBlueMeasurement () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_BlueMeasurementEx(handle, floatRef, floatRef2, floatRef3, floatRef4,
			floatRef5, floatRef6);
		if (result == SUCCESS) return JetiResult.success(new BlueMeasurementData(floatRef.getValue(), floatRef2.getValue(),
			floatRef3.getValue(), floatRef4.getValue(), floatRef5.getValue(), floatRef6.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_RadioTintEx(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> setMeasurementDistance (int distance) {
		ensureOpen();
		return JetiResult.result(JetiRadioExLibrary.INSTANCE.JETI_SetMeasDistEx(handle, distance));
	}

	public JetiResult<Integer> getMeasurementDistance () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_GetMeasDistEx(handle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_GetNumRadioEx(count);
		if (result == SUCCESS) return JetiResult.success(count.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = JetiRadioExLibrary.INSTANCE.JETI_GetSerialRadioEx(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result == SUCCESS)
			return JetiResult.success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiRadioEx> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_OpenRadioEx(deviceNumber, handle);
		if (result == SUCCESS) return JetiResult.success(new JetiRadioEx(handle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_GetRadioExDLLVersion(major, minor, build);
		if (result == SUCCESS) return JetiResult.success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
		return JetiResult.error(result);
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
