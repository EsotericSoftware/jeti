
package com.esotericsoftware.jeti;

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

	private final FloatByReference floatRef1 = new FloatByReference(), floatRef2 = new FloatByReference(),
		floatRef3 = new FloatByReference();
	private final IntByReference intRef = new IntByReference();
	private final ShortByReference shortRef = new ShortByReference();
	private final float[] floatArray2 = new float[2], floatArray3 = new float[3];
	private final float[] criArray = new float[17];
	private final double[] tm30RfiArray = new double[TM30_RFI_SIZE], tm30RfcesArray = new double[TM30_RFCES_SIZE];

	private JetiRadioEx (Pointer deviceHandle) {
		Objects.requireNonNull(deviceHandle);
		this.deviceHandle = deviceHandle;
	}

	// Measurement functions
	public JetiResult<Boolean> measure (float integrationTime, int averageCount, int step) {
		ensureOpen();
		if (integrationTime < 0) return JetiResult.error(INVALID_ARGUMENT);
		if (averageCount <= 0) return JetiResult.error(INVALID_ARGUMENT);
		if (step <= 0) return JetiResult.error(INVALID_ARGUMENT);
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureEx(deviceHandle, integrationTime, (short)averageCount, step);
		return JetiResult.result(result);
	}

	public JetiResult<Boolean> measureWithAdaptation (int averageCount, int step) {
		ensureOpen();
		if (averageCount <= 0) return JetiResult.error(INVALID_ARGUMENT);
		if (step <= 0) return JetiResult.error(INVALID_ARGUMENT);
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureAdaptEx(deviceHandle, (short)averageCount, step);
		return JetiResult.result(result);
	}

	public JetiResult<Boolean> prepareMeasurement (float integrationTime, int averageCount, int step) {
		ensureOpen();
		if (integrationTime < 0) return JetiResult.error(INVALID_ARGUMENT);
		if (averageCount <= 0) return JetiResult.error(INVALID_ARGUMENT);
		if (step <= 0) return JetiResult.error(INVALID_ARGUMENT);
		int result = JetiRadioExLibrary.INSTANCE.JETI_PrepareMeasureEx(deviceHandle, integrationTime, (short)averageCount, step);
		return JetiResult.result(result);
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureStatusEx(deviceHandle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue() != 0);
		return JetiResult.error(result);
	}

	public JetiResult<AdaptationStatus> getAdaptationStatus () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureAdaptStatusEx(deviceHandle, floatRef1, shortRef, intRef);
		if (result == SUCCESS)
			return JetiResult.success(new AdaptationStatus(floatRef1.getValue(), shortRef.getValue(), intRef.getValue() != 0));
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> breakMeasurement () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureBreakEx(deviceHandle);
		return JetiResult.result(result);
	}

	// Spectral data functions
	public JetiResult<float[]> getSpectralRadiance (int beginWavelength, int endWavelength, float step) {
		ensureOpen();
		int dataSize = (int)((endWavelength - beginWavelength) / step + 1);
		var spectralData = new float[dataSize];
		int result = JetiRadioExLibrary.INSTANCE.JETI_SpecRadEx(deviceHandle, beginWavelength, endWavelength, spectralData);
		if (result == SUCCESS) return JetiResult.success(spectralData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getSpectralRadianceHiRes (int beginWavelength, int endWavelength) {
		ensureOpen();
		int dataSize = (int)((endWavelength - beginWavelength) / 0.1f + 1);
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
		return JetiResult.result(result);
	}

	public JetiResult<Boolean> saveSpectralRadianceCSV (int beginWavelength, int endWavelength, String pathName, String operator,
		String memo) {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_SaveSpecRadCSVEx(deviceHandle, beginWavelength, endWavelength, pathName,
			operator, memo);
		return JetiResult.result(result);
	}

	// Measurement data functions
	public JetiResult<Float> getRadiometricValue (int beginWavelength, int endWavelength) {
		ensureOpen();
		if (beginWavelength < 0 || endWavelength < 0) return JetiResult.error(INVALID_ARGUMENT);
		if (beginWavelength >= endWavelength) return JetiResult.error(INVALID_ARGUMENT);
		int result = JetiRadioExLibrary.INSTANCE.JETI_RadioEx(deviceHandle, beginWavelength, endWavelength, floatRef1);
		if (result == SUCCESS) return JetiResult.success(floatRef1.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getPhotometricValue () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_PhotoEx(deviceHandle, floatRef1);
		if (result == SUCCESS) return JetiResult.success(floatRef1.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityXY () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_ChromxyEx(deviceHandle, floatRef1, floatRef2);
		if (result == SUCCESS) {
			floatArray2[0] = floatRef1.getValue();
			floatArray2[1] = floatRef2.getValue();
			return JetiResult.success(floatArray2.clone());
		}
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityXY10 () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_Chromxy10Ex(deviceHandle, floatRef1, floatRef2);
		if (result == SUCCESS) {
			floatArray2[0] = floatRef1.getValue();
			floatArray2[1] = floatRef2.getValue();
			return JetiResult.success(floatArray2.clone());
		}
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityUV () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_ChromuvEx(deviceHandle, floatRef1, floatRef2);
		if (result == SUCCESS) {
			floatArray2[0] = floatRef1.getValue();
			floatArray2[1] = floatRef2.getValue();
			return JetiResult.success(floatArray2.clone());
		}
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getXYZValues () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_ChromXYZEx(deviceHandle, floatRef1, floatRef2, floatRef3);
		if (result == SUCCESS) {
			floatArray3[0] = floatRef1.getValue();
			floatArray3[1] = floatRef2.getValue();
			floatArray3[2] = floatRef3.getValue();
			return JetiResult.success(floatArray3.clone());
		}
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getDominantWavelengthAndPurity () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_DWLPEEx(deviceHandle, floatRef1, floatRef2);
		if (result == SUCCESS) {
			floatArray2[0] = floatRef1.getValue();
			floatArray2[1] = floatRef2.getValue();
			return JetiResult.success(floatArray2.clone());
		}
		return JetiResult.error(result);
	}

	public JetiResult<Float> getCorrelatedColorTemperature () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_CCTEx(deviceHandle, floatRef1);
		if (result == SUCCESS) return JetiResult.success(floatRef1.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getDuv () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_DuvEx(deviceHandle, floatRef1);
		if (result == SUCCESS) return JetiResult.success(floatRef1.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<CRIData> getColorRenderingIndex (float cct) {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_CRIEx(deviceHandle, cct, criArray);
		if (result == SUCCESS) {
			float[] specialIndices = new float[15];
			System.arraycopy(criArray, 2, specialIndices, 0, 15);
			return JetiResult.success(new CRIData(criArray[0], criArray[1], specialIndices));
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
		int result = JetiRadioExLibrary.INSTANCE.JETI_PeakFWHMEx(deviceHandle, threshold, floatRef1, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new PeakFWHMData(floatRef1.getValue(), floatRef2.getValue()));
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
		int result = JetiRadioExLibrary.INSTANCE.JETI_RadioTintEx(deviceHandle, floatRef1);
		if (result == SUCCESS) return JetiResult.success(floatRef1.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> setMeasurementDistance (int distance) {
		ensureOpen();
		if (distance < 0) return JetiResult.error(INVALID_ARGUMENT);
		int result = JetiRadioExLibrary.INSTANCE.JETI_SetMeasDistEx(deviceHandle, distance);
		return JetiResult.result(result);
	}

	public JetiResult<Integer> getMeasurementDistance () {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_GetMeasDistEx(deviceHandle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue());
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

	static public JetiResult<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_GetNumRadioEx(count);
		if (result == SUCCESS) return JetiResult.success(count.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<String[]> getDeviceSerials (int deviceNumber) {
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

	static public JetiResult<JetiRadioEx> openDevice (int deviceNumber) {
		var deviceHandle = new PointerByReference();
		int result = JetiRadioExLibrary.INSTANCE.JETI_OpenRadioEx(deviceNumber, deviceHandle);

		if (result == SUCCESS) return JetiResult.success(new JetiRadioEx(deviceHandle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<String> getDllVersion () {
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
