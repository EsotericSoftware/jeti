
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import java.util.Objects;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiRadio implements AutoCloseable {
	private Pointer deviceHandle;

	private JetiRadio (Pointer deviceHandle) {
		Objects.nonNull(deviceHandle);
		this.deviceHandle = deviceHandle;
	}

	public JetiResult<Boolean> measure () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Measure(deviceHandle);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> measureWithAdaptation () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_MeasureAdapt(deviceHandle);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> prepareMeasurement () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_PrepareMeasure(deviceHandle);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		ensureOpen();
		var status = new IntByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_MeasureStatus(deviceHandle, status);
		if (result == SUCCESS) return JetiResult.success(status.getValue() != 0);
		return JetiResult.error(result);
	}

	public JetiResult<AdaptationStatus> getAdaptationStatus () {
		ensureOpen();
		var tint = new FloatByReference();
		var average = new ShortByReference();
		var status = new IntByReference();

		int result = JetiRadioLibrary.INSTANCE.JETI_MeasureAdaptStatus(deviceHandle, tint, average, status);
		if (result == SUCCESS)
			return JetiResult.success(new AdaptationStatus(tint.getValue(), average.getValue(), status.getValue() != 0));
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> breakMeasurement () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_MeasureBreak(deviceHandle);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<float[]> getSpectralRadiance () {
		ensureOpen();
		var spectralData = new float[SPECTRAL_DATA_SIZE];
		int result = JetiRadioLibrary.INSTANCE.JETI_SpecRad(deviceHandle, spectralData);
		if (result == SUCCESS) return JetiResult.success(spectralData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getRadiometricValues () {
		ensureOpen();
		var radioData = new float[SPECTRAL_DATA_SIZE];
		int result = JetiRadioLibrary.INSTANCE.JETI_Radio(deviceHandle, radioData);
		if (result == SUCCESS) return JetiResult.success(radioData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getPhotometricValues () {
		ensureOpen();
		var photoData = new float[SPECTRAL_DATA_SIZE];
		int result = JetiRadioLibrary.INSTANCE.JETI_Photo(deviceHandle, photoData);
		if (result == SUCCESS) return JetiResult.success(photoData);
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityXY () {
		ensureOpen();
		var chromX = new float[1];
		var chromY = new float[1];
		int result = JetiRadioLibrary.INSTANCE.JETI_Chromxy(deviceHandle, chromX, chromY);
		if (result == SUCCESS) return JetiResult.success(new float[] {chromX[0], chromY[0]});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityXY10 () {
		ensureOpen();
		var chromX10 = new float[1];
		var chromY10 = new float[1];
		int result = JetiRadioLibrary.INSTANCE.JETI_Chromxy10(deviceHandle, chromX10, chromY10);
		if (result == SUCCESS) return JetiResult.success(new float[] {chromX10[0], chromY10[0]});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityUV () {
		ensureOpen();
		var chromU = new float[1];
		var chromV = new float[1];
		int result = JetiRadioLibrary.INSTANCE.JETI_Chromuv(deviceHandle, chromU, chromV);
		if (result == SUCCESS) return JetiResult.success(new float[] {chromU[0], chromV[0]});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getXYZValues () {
		ensureOpen();
		var X = new float[1];
		var Y = new float[1];
		var Z = new float[1];
		int result = JetiRadioLibrary.INSTANCE.JETI_ChromXYZ(deviceHandle, X, Y, Z);
		if (result == SUCCESS) return JetiResult.success(new float[] {X[0], Y[0], Z[0]});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getDominantWavelengthAndPurity () {
		ensureOpen();
		var dwl = new float[1];
		var pe = new float[1];
		int result = JetiRadioLibrary.INSTANCE.JETI_DWLPE(deviceHandle, dwl, pe);
		if (result == SUCCESS) return JetiResult.success(new float[] {dwl[0], pe[0]});
		return JetiResult.error(result);
	}

	public JetiResult<Float> getCorrelatedColorTemperature () {
		ensureOpen();
		var cct = new float[1];
		int result = JetiRadioLibrary.INSTANCE.JETI_CCT(deviceHandle, cct);
		if (result == SUCCESS) return JetiResult.success(cct[0]);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getDuv () {
		ensureOpen();
		var duv = new float[1];
		int result = JetiRadioLibrary.INSTANCE.JETI_Duv(deviceHandle, duv);
		if (result == SUCCESS) return JetiResult.success(duv[0]);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getColorRenderingIndex () {
		ensureOpen();
		var cri = new float[1];
		int result = JetiRadioLibrary.INSTANCE.JETI_CRI(deviceHandle, cri);
		if (result == SUCCESS) return JetiResult.success(cri[0]);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		ensureOpen();
		var tint = new float[1];
		int result = JetiRadioLibrary.INSTANCE.JETI_RadioTint(deviceHandle, tint);
		if (result == SUCCESS) return JetiResult.success(tint[0]);
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> setMeasurementDistance (int distance) {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_SetMeasDist(deviceHandle, distance);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Integer> getMeasurementDistance () {
		ensureOpen();
		var distance = new IntByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_GetMeasDist(deviceHandle, distance);
		if (result == SUCCESS) return JetiResult.success(distance.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<RadiometricData> getAllMeasurementData () {
		JetiResult<float[]> specRadResult = getSpectralRadiance();
		if (specRadResult.isError()) return JetiResult.error(specRadResult.getErrorCode());

		JetiResult<float[]> radioResult = getRadiometricValues();
		if (radioResult.isError()) return JetiResult.error(radioResult.getErrorCode());

		JetiResult<float[]> photoResult = getPhotometricValues();
		if (photoResult.isError()) return JetiResult.error(photoResult.getErrorCode());

		JetiResult<float[]> chromXYResult = getChromaticityXY();
		if (chromXYResult.isError()) return JetiResult.error(chromXYResult.getErrorCode());

		JetiResult<float[]> chromUVResult = getChromaticityUV();
		if (chromUVResult.isError()) return JetiResult.error(chromUVResult.getErrorCode());

		JetiResult<float[]> xyzResult = getXYZValues();
		if (xyzResult.isError()) return JetiResult.error(xyzResult.getErrorCode());

		JetiResult<float[]> dwlPeResult = getDominantWavelengthAndPurity();
		if (dwlPeResult.isError()) return JetiResult.error(dwlPeResult.getErrorCode());

		JetiResult<Float> cctResult = getCorrelatedColorTemperature();
		if (cctResult.isError()) return JetiResult.error(cctResult.getErrorCode());

		JetiResult<Float> duvResult = getDuv();
		if (duvResult.isError()) return JetiResult.error(duvResult.getErrorCode());

		JetiResult<Float> criResult = getColorRenderingIndex();
		if (criResult.isError()) return JetiResult.error(duvResult.getErrorCode());

		JetiResult<Float> tintResult = getIntegrationTime();
		if (tintResult.isError()) return JetiResult.error(tintResult.getErrorCode());

		float[] chromXY = chromXYResult.getValue();
		float[] chromUV = chromUVResult.getValue();
		float[] xyz = xyzResult.getValue();
		float[] dwlPe = dwlPeResult.getValue();

		var data = new RadiometricData(specRadResult.getValue(), radioResult.getValue(), photoResult.getValue(),
			new float[] {chromXY[0]}, new float[] {chromXY[1]}, new float[] {chromUV[0]}, new float[] {chromUV[1]},
			new float[] {xyz[0]}, new float[] {xyz[1]}, new float[] {xyz[2]}, new float[] {dwlPe[0]}, new float[] {dwlPe[1]},
			new float[] {cctResult.getValue()}, new float[] {duvResult.getValue()}, new float[] {criResult.getValue()},
			new float[] {tintResult.getValue()});

		return JetiResult.success(data);
	}

	private void ensureOpen () {
		if (deviceHandle != null) throw new IllegalStateException("Radio device is closed.");
	}

	public void close () {
		if (deviceHandle != null) {
			try {
				int result = JetiRadioLibrary.INSTANCE.JETI_CloseRadio(deviceHandle);
				if (result != SUCCESS) Log.warn("Unable to close radio device: 0x" + Integer.toHexString(result));
			} catch (Throwable ex) {
				Log.warn("Unable to close radio device.", ex);
			} finally {
				deviceHandle = null;
			}
		}
	}

	public boolean isClosed () {
		return deviceHandle == null;
	}

	static public JetiResult<Integer> getNumberOfRadioDevices () {
		var numDevices = new IntByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_GetNumRadio(numDevices);
		if (result == SUCCESS) return JetiResult.success(numDevices.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<String[]> getRadioDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_BUFFER_SIZE];
		var specSerial = new byte[STRING_BUFFER_SIZE];
		var deviceSerial = new byte[STRING_BUFFER_SIZE];

		int result = JetiRadioLibrary.INSTANCE.JETI_GetSerialRadio(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result == SUCCESS) {
			String[] serials = {bytesToString(boardSerial), bytesToString(specSerial), bytesToString(deviceSerial)};
			return JetiResult.success(serials);
		}
		return JetiResult.error(result);
	}

	static public JetiResult<JetiRadio> openRadioDevice (int deviceNumber) {
		var deviceHandle = new PointerByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_OpenRadio(deviceNumber, deviceHandle);

		if (result == SUCCESS) return JetiResult.success(new JetiRadio(deviceHandle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<String> getRadioDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();

		int result = JetiRadioLibrary.INSTANCE.JETI_GetRadioDLLVersion(major, minor, build);
		if (result == SUCCESS) return JetiResult.success(major.getValue() + "." + minor.getValue() + "." + build.getValue());
		return JetiResult.error(result);
	}

	public record RadiometricData (
		float[] spectralRadiance,
		float[] radiometricValues,
		float[] photometricValues,
		float[] chromaticityX,
		float[] chromaticityY,
		float[] chromaticityU,
		float[] chromaticityV,
		float[] chromaticityXYZ_X,
		float[] chromaticityXYZ_Y,
		float[] chromaticityXYZ_Z,
		float[] dominantWavelength,
		float[] purityExcitation,
		float[] correlatedColorTemperature,
		float[] duv,
		float[] colorRenderingIndex,
		float[] integrationTime) {}

	public record AdaptationStatus (float integrationTime, int averageCount, boolean isComplete) {}
}
