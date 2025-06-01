
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

	private JetiRadioEx (Pointer deviceHandle) {
		Objects.nonNull(deviceHandle);
		this.deviceHandle = deviceHandle;
	}

	// Measurement functions
	public JetiResult<Boolean> measure (float integrationTime, short averageCount, int step) {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureEx(deviceHandle, integrationTime, averageCount, step);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> measureWithAdaptation (short averageCount, int step) {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_MeasureAdaptEx(deviceHandle, averageCount, step);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> prepareMeasurement (float integrationTime, short averageCount, int step) {
		ensureOpen();
		int result = JetiRadioExLibrary.INSTANCE.JETI_PrepareMeasureEx(deviceHandle, integrationTime, averageCount, step);
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
	public JetiResult<float[]> getRadiometricValues (int beginWavelength, int endWavelength) {
		ensureOpen();
		int dataSize = endWavelength - beginWavelength + 1;
		var radioData = new float[dataSize];
		int result = JetiRadioExLibrary.INSTANCE.JETI_RadioEx(deviceHandle, beginWavelength, endWavelength, radioData);
		if (result == SUCCESS) return JetiResult.success(radioData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getPhotometricValues () {
		ensureOpen();
		var photoData = new float[SPECTRAL_DATA_SIZE];
		int result = JetiRadioExLibrary.INSTANCE.JETI_PhotoEx(deviceHandle, photoData);
		if (result == SUCCESS) return JetiResult.success(photoData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityXY () {
		ensureOpen();
		var chromX = new float[1];
		var chromY = new float[1];
		int result = JetiRadioExLibrary.INSTANCE.JETI_ChromxyEx(deviceHandle, chromX, chromY);
		if (result == SUCCESS) return JetiResult.success(new float[] {chromX[0], chromY[0]});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityXY10 () {
		ensureOpen();
		var chromX10 = new float[1];
		var chromY10 = new float[1];
		int result = JetiRadioExLibrary.INSTANCE.JETI_Chromxy10Ex(deviceHandle, chromX10, chromY10);
		if (result == SUCCESS) return JetiResult.success(new float[] {chromX10[0], chromY10[0]});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityUV () {
		ensureOpen();
		var chromU = new float[1];
		var chromV = new float[1];
		int result = JetiRadioExLibrary.INSTANCE.JETI_ChromuvEx(deviceHandle, chromU, chromV);
		if (result == SUCCESS) return JetiResult.success(new float[] {chromU[0], chromV[0]});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getXYZValues () {
		ensureOpen();
		var X = new float[1];
		var Y = new float[1];
		var Z = new float[1];
		int result = JetiRadioExLibrary.INSTANCE.JETI_ChromXYZEx(deviceHandle, X, Y, Z);
		if (result == SUCCESS) return JetiResult.success(new float[] {X[0], Y[0], Z[0]});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getDominantWavelengthAndPurity () {
		ensureOpen();
		var dwl = new float[1];
		var pe = new float[1];
		int result = JetiRadioExLibrary.INSTANCE.JETI_DWLPEEx(deviceHandle, dwl, pe);
		if (result == SUCCESS) return JetiResult.success(new float[] {dwl[0], pe[0]});
		return JetiResult.error(result);
	}

	public JetiResult<Float> getCorrelatedColorTemperature () {
		ensureOpen();
		var cct = new float[1];
		int result = JetiRadioExLibrary.INSTANCE.JETI_CCTEx(deviceHandle, cct);
		if (result == SUCCESS) return JetiResult.success(cct[0]);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getDuv () {
		ensureOpen();
		var duv = new float[1];
		int result = JetiRadioExLibrary.INSTANCE.JETI_DuvEx(deviceHandle, duv);
		if (result == SUCCESS) return JetiResult.success(duv[0]);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getColorRenderingIndex (float cct) {
		ensureOpen();
		var cri = new float[1];
		int result = JetiRadioExLibrary.INSTANCE.JETI_CRIEx(deviceHandle, cct, cri);
		if (result == SUCCESS) return JetiResult.success(cri[0]);
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
		var tint = new float[1];
		int result = JetiRadioExLibrary.INSTANCE.JETI_RadioTintEx(deviceHandle, tint);
		if (result == SUCCESS) return JetiResult.success(tint[0]);
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
		if (deviceHandle != null) throw new IllegalStateException("Radio ex device is closed");
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

	static public JetiResult<Integer> getNumberOfRadioExDevices () {
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
			String[] serials = {bytesToString(boardSerial), bytesToString(specSerial), bytesToString(deviceSerial)};
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

	static public class TM30Data {
		private final double rf;
		private final double rg;
		private final double chroma;
		private final double hue;
		private final double[] rfi;
		private final double[] rfces;

		public TM30Data (double rf, double rg, double chroma, double hue, double[] rfi, double[] rfces) {
			this.rf = rf;
			this.rg = rg;
			this.chroma = chroma;
			this.hue = hue;
			this.rfi = rfi;
			this.rfces = rfces;
		}

		public double getRf () {
			return rf;
		}

		public double getRg () {
			return rg;
		}

		public double getChroma () {
			return chroma;
		}

		public double getHue () {
			return hue;
		}

		public double[] getRfi () {
			return rfi;
		}

		public double[] getRfces () {
			return rfces;
		}
	}

	static public class PeakFWHMData {
		private final float peak;
		private final float fwhm;

		public PeakFWHMData (float peak, float fwhm) {
			this.peak = peak;
			this.fwhm = fwhm;
		}

		public float getPeak () {
			return peak;
		}

		public float getFwhm () {
			return fwhm;
		}
	}

	static public class BlueMeasurementData {
		private final float lb;
		private final float kbv;
		private final float kc;
		private final float rbpfs;
		private final float rlbtb;
		private final float rnbpbp;

		public BlueMeasurementData (float lb, float kbv, float kc, float rbpfs, float rlbtb, float rnbpbp) {
			this.lb = lb;
			this.kbv = kbv;
			this.kc = kc;
			this.rbpfs = rbpfs;
			this.rlbtb = rlbtb;
			this.rnbpbp = rnbpbp;
		}

		public float getLb () {
			return lb;
		}

		public float getKbv () {
			return kbv;
		}

		public float getKc () {
			return kc;
		}

		public float getRbpfs () {
			return rbpfs;
		}

		public float getRlbtb () {
			return rlbtb;
		}

		public float getRnbpbp () {
			return rnbpbp;
		}
	}
}
