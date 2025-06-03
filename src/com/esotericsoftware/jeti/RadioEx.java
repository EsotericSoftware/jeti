
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;
import static com.esotericsoftware.jeti.Result.*;

import com.esotericsoftware.jeti.JetiSDK.AdaptationStatus;
import com.esotericsoftware.jeti.JetiSDK.BlueMeasurement;
import com.esotericsoftware.jeti.JetiSDK.CRI;
import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;
import com.esotericsoftware.jeti.JetiSDK.DominantWavelength;
import com.esotericsoftware.jeti.JetiSDK.PeakFWHM;
import com.esotericsoftware.jeti.JetiSDK.TM30;
import com.esotericsoftware.jeti.JetiSDK.UV;
import com.esotericsoftware.jeti.JetiSDK.XY;
import com.esotericsoftware.jeti.JetiSDK.XY10;
import com.esotericsoftware.jeti.JetiSDK.XYZ;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class RadioEx extends Device<RadioExLibrary> {
	private final float[] cri = new float[17];

	private RadioEx (Pointer handle) {
		super(RadioExLibrary.INSTANCE, handle, RadioExLibrary.INSTANCE::JETI_CloseRadioEx, 0, 1, 1, 6, 4, 0);
	}

	// Measurement functions

	public Result<Boolean> measure (float integrationTime, int averageCount, int step) {
		return result(lib().JETI_MeasureEx(handle, integrationTime, (short)averageCount, step));
	}

	public Result<Boolean> measureWithAdaptation (int averageCount, int step) {
		return result(lib().JETI_MeasureAdaptEx(handle, (short)averageCount, step));
	}

	public Result<Boolean> prepareMeasurement (float integrationTime, int averageCount, int step) {
		return result(lib().JETI_PrepareMeasureEx(handle, integrationTime, (short)averageCount, step));
	}

	public Result<Boolean> getMeasurementStatus () {
		int result = lib().JETI_MeasureStatusEx(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public Result<AdaptationStatus> getAdaptationStatus () {
		int result = lib().JETI_MeasureAdaptStatusEx(handle, f[0], s[0], i[0]);
		if (result != SUCCESS) return error(result);
		return success(new AdaptationStatus(f[0].getValue(), s[0].getValue(), i[0].getValue() != 0));
	}

	public Result<Boolean> breakMeasurement () {
		return result(lib().JETI_MeasureBreakEx(handle));
	}

	// Spectral data functions

	public Result<float[]> getSpectralRadiance (int beginWavelength, int endWavelength, float step) {
		int dataSize = (int)((endWavelength - beginWavelength) / step + 1);
		var spectralData = new float[dataSize];
		int result = lib().JETI_SpecRadEx(handle, beginWavelength, endWavelength, spectralData);
		if (result != SUCCESS) return error(result);
		return success(spectralData);
	}

	public Result<float[]> getSpectralRadianceHiRes (int beginWavelength, int endWavelength) {
		int dataSize = (int)((endWavelength - beginWavelength) / 0.1f + 1);
		var spectralData = new float[dataSize];
		int result = lib().JETI_SpecRadHiResEx(handle, beginWavelength, endWavelength, spectralData);
		if (result != SUCCESS) return error(result);
		return success(spectralData);
	}

	public Result<Boolean> saveSpectralRadianceSPC (int beginWavelength, int endWavelength, String filePath, String operator,
		String memo) {
		return result(lib().JETI_SaveSpecRadSPCEx(handle, beginWavelength, endWavelength, filePath, operator, memo));
	}

	public Result<Boolean> saveSpectralRadianceCSV (int beginWavelength, int endWavelength, String pathName, String operator,
		String memo) {
		return result(lib().JETI_SaveSpecRadCSVEx(handle, beginWavelength, endWavelength, pathName, operator, memo));
	}

	// Measurement data functions

	public Result<Float> getRadiometricValue (int beginWavelength, int endWavelength) {
		int result = lib().JETI_RadioEx(handle, beginWavelength, endWavelength, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public Result<Float> getPhotometricValue () {
		int result = lib().JETI_PhotoEx(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public Result<XY> getChromaXY () {
		int result = lib().JETI_ChromxyEx(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new XY(f[0].getValue(), f[1].getValue()));
	}

	public Result<XY10> getChromaXY10 () {
		int result = lib().JETI_Chromxy10Ex(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new XY10(f[0].getValue(), f[1].getValue()));
	}

	public Result<UV> getChromaUV () {
		int result = lib().JETI_ChromuvEx(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new UV(f[0].getValue(), f[1].getValue()));
	}

	public Result<XYZ> getXYZ () {
		int result = lib().JETI_ChromXYZEx(handle, f[0], f[1], f[2]);
		if (result != SUCCESS) return error(result);
		return success(new XYZ(f[0].getValue(), f[1].getValue(), f[2].getValue()));
	}

	public Result<DominantWavelength> getDominantWavelength () {
		int result = lib().JETI_DWLPEEx(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new DominantWavelength(f[0].getValue(), f[1].getValue()));
	}

	public Result<Float> getCCT () {
		int result = lib().JETI_CCTEx(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public Result<Float> getDuv () {
		int result = lib().JETI_DuvEx(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public Result<CRI> getCRI (float cct) {
		int result = lib().JETI_CRIEx(handle, cct, cri);
		if (result != SUCCESS) return error(result);
		float[] samples = new float[15];
		System.arraycopy(cri, 2, samples, 0, 15);
		return success(new CRI(cri[0], cri[0] / 0.0054f, cri[1], samples));
	}

	public Result<TM30> getTM30 (boolean useTM3015) {
		var rfi = new double[16];
		var rfces = new double[99];
		int result = lib().JETI_TM30Ex(handle, (byte)(useTM3015 ? 1 : 0), d[0], d[1], d[2], d[3], rfi, rfces);
		if (result != SUCCESS) return error(result);
		return success(new TM30(d[0].getValue(), d[1].getValue(), d[2].getValue(), d[3].getValue(), rfi, rfces));
	}

	public Result<PeakFWHM> getPeakFWHM (float threshold) {
		int result = lib().JETI_PeakFWHMEx(handle, threshold, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new PeakFWHM(f[0].getValue(), f[1].getValue()));
	}

	public Result<BlueMeasurement> getBlueMeasurement () {
		int result = lib().JETI_BlueMeasurementEx(handle, f[0], f[1], f[2], f[3], f[4], f[5]);
		if (result != SUCCESS) return error(result);
		return success(new BlueMeasurement(f[0].getValue(), f[1].getValue(), f[2].getValue(), f[3].getValue(), f[4].getValue(),
			f[5].getValue()));
	}

	public Result<Float> getIntegrationTime () {
		int result = lib().JETI_RadioTintEx(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public Result<Boolean> setMeasurementDistance (int distance) {
		return result(lib().JETI_SetMeasDistEx(handle, distance));
	}

	public Result<Integer> getMeasurementDistance () {
		int result = lib().JETI_GetMeasDistEx(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue());
	}

	static public Result<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = RadioExLibrary.INSTANCE.JETI_GetNumRadioEx(count);
		if (result != SUCCESS) return error(result);
		return success(count.getValue());
	}

	static public Result<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = RadioExLibrary.INSTANCE.JETI_GetSerialRadioEx(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result != SUCCESS) return error(result);
		return success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
	}

	static public Result<RadioEx> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = RadioExLibrary.INSTANCE.JETI_OpenRadioEx(deviceNumber, handle);
		if (result != SUCCESS) return error(result);
		return success(new RadioEx(handle.getValue()));
	}

	static public Result<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = RadioExLibrary.INSTANCE.JETI_GetRadioExDLLVersion(major, minor, build);
		if (result != SUCCESS) return error(result);
		return success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
	}
}
