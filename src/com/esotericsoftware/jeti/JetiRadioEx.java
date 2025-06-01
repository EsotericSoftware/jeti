
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiException.*;
import static com.esotericsoftware.jeti.JetiSDK.*;

import java.util.Objects;

import com.esotericsoftware.jeti.JetiRadio.AdaptationStatus;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiRadioEx implements AutoCloseable {
	static private final int TM30_RFI_SIZE = 15;
	static private final int TM30_RFCES_SIZE = 99;

	private Pointer deviceHandle;

	private JetiRadioEx (Pointer deviceHandle) {
		Objects.requireNonNull(deviceHandle);
		this.deviceHandle = deviceHandle;
	}

	// Measurement functions
	public JetiResult<Boolean> measure (float integrationTime, int averageCount, int step) {
		ensureOpen();
		if (integrationTime < 0) return JetiResult.error(invalidArgument);
		if (averageCount <= 0) return JetiResult.error(invalidArgument);
		if (step <= 0) return JetiResult.error(invalidArgument);
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureEx(deviceHandle, integrationTime, (short)averageCount, step);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> measureWithAdaptation (int averageCount, int step) {
		ensureOpen();
		if (averageCount <= 0) return JetiResult.error(invalidArgument);
		if (step <= 0) return JetiResult.error(invalidArgument);
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureAdaptEx(deviceHandle, (short)averageCount, step);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> prepareMeasurement (float integrationTime, int averageCount, int step) {
		ensureOpen();
		if (integrationTime < 0) return JetiResult.error(invalidArgument);
		if (averageCount <= 0) return JetiResult.error(invalidArgument);
		if (step <= 0) return JetiResult.error(invalidArgument);
		int result = JetiRadioExLibrary.INSTANCE.JETI_PrepareMeasureEx(deviceHandle, integrationTime, (short)averageCount, step);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		ensureOpen();
		var status = new IntByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureStatusEx(deviceHandle, status);
		if (result == SUCCESS) return JetiResult.success(status.getValue() != 0);
		return JetiResult.error(result);
	}

	public JetiResult<AdaptationStatus> getAdaptationStatus () {
		ensureOpen();
		var tint = new FloatByReference();
		var average = new ShortByReference();
		var status = new IntByReference(4);
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureAdaptStatusEx(deviceHandle, tint, average, status);
		if (result == SUCCESS)
			return JetiResult.success(new AdaptationStatus(tint.getValue(), average.getValue(), status.getValue() != 0));
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> breakMeasurement () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureBreakEx(deviceHandle);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	// Spectral data functions
	public JetiResult<float[]> getSpectralRadiance (int beginWavelength, int endWavelength) {
		ensureOpen();
		int dataSize = endWavelength - beginWavelength + 1;
		var spectralData = new float[dataSize];
		int result = JetiRadioExLibrary.INSTANCE.JETI_SpecRadEx(deviceHandle, beginWavelength, endWavelength, spectralData);
		if (result == SUCCESS) return JetiResult.success(spectralData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getSpectralRadianceHiRes (int beginWavelength, int endWavelength) {
		ensureOpen();
		int dataSize = endWavelength - beginWavelength + 1;
		var spectralData = new float[dataSize];
		int result = JetiRadioExLibrary.INSTANCE.JETI_SpecRadHiResEx(deviceHandle, beginWavelength, endWavelength, spectralData);
		if (result == SUCCESS) return JetiResult.success(spectralData);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> saveSpectralRadianceSPC (int beginWavelength, int endWavelength, String pathName, String operator,
		String memo) {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_SaveSpecRadSPCEx(deviceHandle, beginWavelength, endWavelength, pathName,
			operator, memo);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> saveSpectralRadianceCSV (int beginWavelength, int endWavelength, String pathName, String operator,
		String memo) {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_SaveSpecRadCSVEx(deviceHandle, beginWavelength, endWavelength, pathName,
			operator, memo);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	// Measurement data functions
	public JetiResult<Float> getRadiometricValue (int beginWavelength, int endWavelength) {
		ensureOpen();
		if (beginWavelength < 0 || endWavelength < 0) return JetiResult.error(invalidArgument);
		if (beginWavelength >= endWavelength) return JetiResult.error(invalidArgument);
		var radioValue = new FloatByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_RadioEx(deviceHandle, beginWavelength, endWavelength, radioValue);
		if (result == SUCCESS) return JetiResult.success(radioValue.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getPhotometricValue () {
		ensureOpen();
		var photoValue = new FloatByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_PhotoEx(deviceHandle, photoValue);
		if (result == SUCCESS) return JetiResult.success(photoValue.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityXY () {
		ensureOpen();
		var chromX = new FloatByReference();
		var chromY = new FloatByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_ChromxyEx(deviceHandle, chromX, chromY);
		if (result == SUCCESS) return JetiResult.success(new float[] {chromX.getValue(), chromY.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityXY10 () {
		ensureOpen();
		var chromX10 = new FloatByReference();
		var chromY10 = new FloatByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_Chromxy10Ex(deviceHandle, chromX10, chromY10);
		if (result == SUCCESS) return JetiResult.success(new float[] {chromX10.getValue(), chromY10.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityUV () {
		ensureOpen();
		var chromU = new FloatByReference();
		var chromV = new FloatByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_ChromuvEx(deviceHandle, chromU, chromV);
		if (result == SUCCESS) return JetiResult.success(new float[] {chromU.getValue(), chromV.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getXYZValues () {
		ensureOpen();
		var X = new FloatByReference();
		var Y = new FloatByReference();
		var Z = new FloatByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_ChromXYZEx(deviceHandle, X, Y, Z);
		if (result == SUCCESS) return JetiResult.success(new float[] {X.getValue(), Y.getValue(), Z.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getDominantWavelengthAndPurity () {
		ensureOpen();
		var dwl = new FloatByReference();
		var pe = new FloatByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_DWLPEEx(deviceHandle, dwl, pe);
		if (result == SUCCESS) return JetiResult.success(new float[] {dwl.getValue(), pe.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<Float> getCorrelatedColorTemperature () {
		ensureOpen();
		var cct = new FloatByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_CCTEx(deviceHandle, cct);
		if (result == SUCCESS) return JetiResult.success(cct.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getDuv () {
		ensureOpen();
		var duv = new FloatByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_DuvEx(deviceHandle, duv);
		if (result == SUCCESS) return JetiResult.success(duv.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<CRIData> getColorRenderingIndex (float cct) {
		ensureOpen();
		var criValues = new float[17];
		int result = JetiRadioExLibrary.INSTANCE.JETI_CRIEx(deviceHandle, cct, criValues);
		if (result == SUCCESS) {
			return JetiResult.success(new CRIData(criValues[0], criValues[1],
				new float[] {criValues[2], criValues[3], criValues[4], criValues[5], criValues[6], criValues[7], criValues[8],
					criValues[9], criValues[10], criValues[11], criValues[12], criValues[13], criValues[14], criValues[15],
					criValues[16]}));
		}
		return JetiResult.error(result);
	}

	public JetiResult<TM30Data> getTM30 (boolean useTM3015) {
		ensureOpen();
		var rf = new DoubleByReference();
		var rg = new DoubleByReference();
		var chroma = new DoubleByReference();
		var hue = new DoubleByReference();
		var rfi = new double[TM30_RFI_SIZE];
		var rfces = new double[TM30_RFCES_SIZE];
		int result = JetiRadioExLibrary.INSTANCE.JETI_TM30Ex(deviceHandle, (byte)(useTM3015 ? 1 : 0), rf, rg, chroma, hue, rfi,
			rfces);
		if (result == SUCCESS)
			return JetiResult.success(new TM30Data(rf.getValue(), rg.getValue(), chroma.getValue(), hue.getValue(), rfi, rfces));
		return JetiResult.error(result);
	}

	public JetiResult<PeakFWHMData> getPeakFWHM (float threshold) {
		ensureOpen();
		var peak = new FloatByReference();
		var fwhm = new FloatByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_PeakFWHMEx(deviceHandle, threshold, peak, fwhm);
		if (result == SUCCESS) return JetiResult.success(new PeakFWHMData(peak.getValue(), fwhm.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<BlueMeasurementData> getBlueMeasurement () {
		ensureOpen();
		var lb = new FloatByReference();
		var kbv = new FloatByReference();
		var kc = new FloatByReference();
		var rbpfs = new FloatByReference();
		var rlbtb = new FloatByReference();
		var rnbpbp = new FloatByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_BlueMeasurementEx(deviceHandle, lb, kbv, kc, rbpfs, rlbtb, rnbpbp);
		if (result == SUCCESS) return JetiResult.success(new BlueMeasurementData(lb.getValue(), kbv.getValue(), kc.getValue(),
			rbpfs.getValue(), rlbtb.getValue(), rnbpbp.getValue()));
		return JetiResult.error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		ensureOpen();
		var tint = new FloatByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_RadioTintEx(deviceHandle, tint);
		if (result == SUCCESS) return JetiResult.success(tint.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> setMeasurementDistance (int distance) {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_SetMeasDistEx(deviceHandle, distance);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Integer> getMeasurementDistance () {
		ensureOpen();
		var distance = new IntByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_GetMeasDistEx(deviceHandle, distance);
		if (result == SUCCESS) return JetiResult.success(distance.getValue());
		return JetiResult.error(result);
	}

	private void ensureOpen () {
		if (deviceHandle == null) throw new IllegalStateException("Radio ex device is closed.");
	}

	public void close () {
		if (deviceHandle != null) {
			try {
				int result = JetiRadioExLibrary.INSTANCE.JETI_CloseRadioEx(deviceHandle);
				if (result != SUCCESS) Log.warn("Unable to close radio ex device: 0x" + Integer.toHexString(result));
			} catch (Throwable ex) {
				Log.warn("Unable to close radio ex device.", ex);
			} finally {
				deviceHandle = null;
			}
		}
	}

	public boolean isClosed () {
		return deviceHandle == null;
	}

	static public JetiResult<Integer> getRadioExDeviceCount () {
		var numDevices = new IntByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_GetNumRadioEx(numDevices);
		if (result == SUCCESS) return JetiResult.success(numDevices.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<String[]> getRadioExDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_BUFFER_SIZE];
		var specSerial = new byte[STRING_BUFFER_SIZE];
		var deviceSerial = new byte[STRING_BUFFER_SIZE];

		int result = JetiRadioExLibrary.INSTANCE.JETI_GetSerialRadioEx(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result == SUCCESS) {
			String[] serials = {string(boardSerial), string(specSerial), string(deviceSerial)};
			return JetiResult.success(serials);
		}
		return JetiResult.error(result);
	}

	static public JetiResult<JetiRadioEx> openRadioExDevice (int deviceNumber) {
		var deviceHandle = new PointerByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_OpenRadioEx(deviceNumber, deviceHandle);

		if (result == SUCCESS) return JetiResult.success(new JetiRadioEx(deviceHandle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<String> getRadioExDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();

		int result = JetiRadioExLibrary.INSTANCE.JETI_GetRadioExDLLVersion(major, minor, build);
		if (result == SUCCESS) return JetiResult.success(major.getValue() + "." + minor.getValue() + "." + build.getValue());
		return JetiResult.error(result);
	}

	static public record TM30Data (double rf, double rg, double chroma, double hue, double[] rfi, double[] rfces) {}

	static public record PeakFWHMData (float peak, float fwhm) {}

	static public record BlueMeasurementData (float lb, float kbv, float kc, float rbpfs, float rlbtb, float rnbpbp) {}

	static public record CRIData (float dc, float ra, float[] specialIndices) {}
}
