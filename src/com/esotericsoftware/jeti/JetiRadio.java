
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

	private final FloatByReference floatRef1 = new FloatByReference(), floatRef2 = new FloatByReference(),
		floatRef3 = new FloatByReference();
	private final IntByReference intRef = new IntByReference();
	private final ShortByReference shortRef = new ShortByReference();
	private final float[] floatArray2 = new float[2], floatArray3 = new float[3];

	private JetiRadio (Pointer deviceHandle) {
		Objects.requireNonNull(deviceHandle);
		this.deviceHandle = deviceHandle;
	}

	public JetiResult<Boolean> measure () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Measure(deviceHandle);
		return JetiResult.result(result);
	}

	public JetiResult<Boolean> measureWithAdaptation () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_MeasureAdapt(deviceHandle);
		return JetiResult.result(result);
	}

	public JetiResult<Boolean> prepareMeasurement () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_PrepareMeasure(deviceHandle);
		return JetiResult.result(result);
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_MeasureStatus(deviceHandle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue() != 0);
		return JetiResult.error(result);
	}

	public JetiResult<AdaptationStatus> getAdaptationStatus () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_MeasureAdaptStatus(deviceHandle, floatRef1, shortRef, intRef);
		if (result == SUCCESS) {
			return JetiResult.success(new AdaptationStatus(floatRef1.getValue(), shortRef.getValue(), intRef.getValue() != 0));
		}
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> breakMeasurement () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_MeasureBreak(deviceHandle);
		return JetiResult.result(result);
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
		int result = JetiRadioLibrary.INSTANCE.JETI_Radio(deviceHandle, floatRef1);
		if (result == SUCCESS) return JetiResult.success(floatRef1.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getPhotometricValue () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Photo(deviceHandle, floatRef1);
		if (result == SUCCESS) return JetiResult.success(floatRef1.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityXY () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Chromxy(deviceHandle, floatRef1, floatRef2);
		if (result == SUCCESS) {
			floatArray2[0] = floatRef1.getValue();
			floatArray2[1] = floatRef2.getValue();
			return JetiResult.success(floatArray2.clone());
		}
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityXY10 () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Chromxy10(deviceHandle, floatRef1, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new float[] {floatRef1.getValue(), floatRef2.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getChromaticityUV () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Chromuv(deviceHandle, floatRef1, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new float[] {floatRef1.getValue(), floatRef2.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getXYZValues () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_ChromXYZ(deviceHandle, floatRef1, floatRef2, floatRef3);
		if (result == SUCCESS)
			return JetiResult.success(new float[] {floatRef1.getValue(), floatRef2.getValue(), floatRef3.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<float[]> getDominantWavelengthAndPurity () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_DWLPE(deviceHandle, floatRef1, floatRef2);
		if (result == SUCCESS) return JetiResult.success(new float[] {floatRef1.getValue(), floatRef2.getValue()});
		return JetiResult.error(result);
	}

	public JetiResult<Float> getCorrelatedColorTemperature () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_CCT(deviceHandle, floatRef1);
		if (result == SUCCESS) return JetiResult.success(floatRef1.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getDuv () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_Duv(deviceHandle, floatRef1);
		if (result == SUCCESS) return JetiResult.success(floatRef1.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getColorRenderingIndex () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_CRI(deviceHandle, floatRef1);
		if (result == SUCCESS) return JetiResult.success(floatRef1.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_RadioTint(deviceHandle, floatRef1);
		if (result == SUCCESS) return JetiResult.success(floatRef1.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> setMeasurementDistance (int mm) {
		ensureOpen();
		if (mm < 0) return JetiResult.error(INVALID_ARGUMENT);
		int result = JetiRadioLibrary.INSTANCE.JETI_SetMeasDist(deviceHandle, mm);
		return JetiResult.result(result);
	}

	public JetiResult<Integer> getMeasurementDistance () {
		ensureOpen();
		int result = JetiRadioLibrary.INSTANCE.JETI_GetMeasDist(deviceHandle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue());
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
		var count = new IntByReference();
		int result = JetiRadioLibrary.INSTANCE.JETI_GetNumRadio(count);
		if (result == SUCCESS) return JetiResult.success(count.getValue());
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
