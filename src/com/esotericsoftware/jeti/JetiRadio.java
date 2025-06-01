
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
		Objects.requireNonNull(deviceHandle);
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

	public JetiResult<Float> getRadiometricValue () {
		ensureOpen();
		var radioValue = new FloatByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_Radio(deviceHandle, radioValue);
		if (result == SUCCESS) return JetiResult.success(radioValue.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getPhotometricValue () {
		ensureOpen();
		var photoValue = new FloatByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_Photo(deviceHandle, photoValue);
		if (result == SUCCESS) return JetiResult.success(photoValue.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityXY () {
		ensureOpen();
		var chromX = new FloatByReference();
		var chromY = new FloatByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_Chromxy(deviceHandle, chromX, chromY);
		if (result == SUCCESS) return JetiResult.success(new float[] {chromX.getValue(), chromY.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityXY10 () {
		ensureOpen();
		var chromX10 = new FloatByReference();
		var chromY10 = new FloatByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_Chromxy10(deviceHandle, chromX10, chromY10);
		if (result == SUCCESS) return JetiResult.success(new float[] {chromX10.getValue(), chromY10.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityUV () {
		ensureOpen();
		var chromU = new FloatByReference();
		var chromV = new FloatByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_Chromuv(deviceHandle, chromU, chromV);
		if (result == SUCCESS) return JetiResult.success(new float[] {chromU.getValue(), chromV.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getXYZValues () {
		ensureOpen();
		var X = new FloatByReference();
		var Y = new FloatByReference();
		var Z = new FloatByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_ChromXYZ(deviceHandle, X, Y, Z);
		if (result == SUCCESS) return JetiResult.success(new float[] {X.getValue(), Y.getValue(), Z.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getDominantWavelengthAndPurity () {
		ensureOpen();
		var dwl = new FloatByReference();
		var pe = new FloatByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_DWLPE(deviceHandle, dwl, pe);
		if (result == SUCCESS) return JetiResult.success(new float[] {dwl.getValue(), pe.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<Float> getCorrelatedColorTemperature () {
		ensureOpen();
		var cct = new FloatByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_CCT(deviceHandle, cct);
		if (result == SUCCESS) return JetiResult.success(cct.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getDuv () {
		ensureOpen();
		var duv = new FloatByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_Duv(deviceHandle, duv);
		if (result == SUCCESS) return JetiResult.success(duv.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getColorRenderingIndex () {
		ensureOpen();
		var cri = new FloatByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_CRI(deviceHandle, cri);
		if (result == SUCCESS) return JetiResult.success(cri.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		ensureOpen();
		var tint = new FloatByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_RadioTint(deviceHandle, tint);
		if (result == SUCCESS) return JetiResult.success(tint.getValue());
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

	private void ensureOpen () {
		if (deviceHandle == null) throw new IllegalStateException("Radio device is closed.");
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

	static public JetiResult<Integer> getRadioDeviceCount () {
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
			String[] serials = {string(boardSerial), string(specSerial), string(deviceSerial)};
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

	public record AdaptationStatus (float integrationTime, int averageCount, boolean isComplete) {}
}
