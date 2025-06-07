
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

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
		super(RadioExLibrary.INSTANCE, handle, RadioExLibrary.INSTANCE::JETI_CloseRadioEx, 0, 1, 1, 6, 2, 0);
	}

	// Measurement functions

	public void measure (float integrationTime, int averageCount, int step) {
		check(lib().JETI_MeasureEx(handle, integrationTime, (short)averageCount, step));
	}

	public void measureWithAdaptation (int averageCount, int step) {
		check(lib().JETI_MeasureAdaptEx(handle, (short)averageCount, step));
	}

	public void prepareMeasurement (float integrationTime, int averageCount, int step) {
		check(lib().JETI_PrepareMeasureEx(handle, integrationTime, (short)averageCount, step));
	}

	public boolean isMeasuring () {
		check(lib().JETI_MeasureStatusEx(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public AdaptationStatus getAdaptationStatus () {
		check(lib().JETI_MeasureAdaptStatusEx(handle, f[0], s[0], i[0]));
		return new AdaptationStatus(f[0].getValue(), s[0].getValue(), i[0].getValue() != 0);
	}

	public void cancelMeasurement () {
		check(lib().JETI_MeasureBreakEx(handle));
	}

	// Spectral data functions

	/** @param step Must match last measurement. */
	public float[] getSpectralRadiance (int beginWavelength, int endWavelength, float step) {
		int dataSize = (int)((endWavelength - beginWavelength) / step + 1);
		var spectralData = new float[dataSize];
		check(lib().JETI_SpecRadEx(handle, beginWavelength, endWavelength, spectralData));
		return spectralData;
	}

	public float[] getSpectralRadianceHiRes (int beginWavelength, int endWavelength) {
		int dataSize = (int)((endWavelength - beginWavelength) / 0.1f + 1);
		var spectralData = new float[dataSize];
		check(lib().JETI_SpecRadHiResEx(handle, beginWavelength, endWavelength, spectralData));
		return spectralData;
	}

	public void saveSpectralRadianceSPC (int beginWavelength, int endWavelength, String filePath, String operator, String memo) {
		check(lib().JETI_SaveSpecRadSPCEx(handle, beginWavelength, endWavelength, filePath, operator, memo));
	}

	public void saveSpectralRadianceCSV (int beginWavelength, int endWavelength, String pathName, String operator, String memo) {
		check(lib().JETI_SaveSpecRadCSVEx(handle, beginWavelength, endWavelength, pathName, operator, memo));
	}

	// Measurement data functions

	public float getRadiometricValue (int beginWavelength, int endWavelength) {
		check(lib().JETI_RadioEx(handle, beginWavelength, endWavelength, f[0]));
		return f[0].getValue();
	}

	public float getPhotometricValue () {
		check(lib().JETI_PhotoEx(handle, f[0]));
		return f[0].getValue();
	}

	public XY getChromaXY () {
		check(lib().JETI_ChromxyEx(handle, f[0], f[1]));
		return new XY(f[0].getValue(), f[1].getValue());
	}

	public XY10 getChromaXY10 () {
		check(lib().JETI_Chromxy10Ex(handle, f[0], f[1]));
		return new XY10(f[0].getValue(), f[1].getValue());
	}

	public UV getChromaUV () {
		check(lib().JETI_ChromuvEx(handle, f[0], f[1]));
		return new UV(f[0].getValue(), f[1].getValue());
	}

	public XYZ getXYZ () {
		check(lib().JETI_ChromXYZEx(handle, f[0], f[1], f[2]));
		return new XYZ(f[0].getValue(), f[1].getValue(), f[2].getValue());
	}

	public DominantWavelength getDominantWavelength () {
		check(lib().JETI_DWLPEEx(handle, f[0], f[1]));
		return new DominantWavelength(f[0].getValue(), f[1].getValue());
	}

	public float getCCT () {
		check(lib().JETI_CCTEx(handle, f[0]));
		return f[0].getValue();
	}

	public float getDuv () {
		check(lib().JETI_DuvEx(handle, f[0]));
		return f[0].getValue();
	}

	public CRI getCRI (float cct) {
		check(lib().JETI_CRIEx(handle, cct, cri));
		float[] samples = new float[15];
		System.arraycopy(cri, 2, samples, 0, 15);
		return new CRI(cri[0], cri[0] / 0.0054f, cri[1], samples);
	}

	public TM30 getTM30 (boolean useTM3015) {
		var dChroma = new double[16];
		var dHue = new double[16];
		var rfi = new double[16];
		var rfces = new double[99];
		check(lib().JETI_TM30Ex(handle, (byte)(useTM3015 ? 1 : 0), d[0], d[1], dChroma, dHue, rfi, rfces));
		return new TM30(d[0].getValue(), d[1].getValue(), dChroma, dHue, rfi, rfces);
	}

	public PeakFWHM getPeakFWHM (float threshold) {
		check(lib().JETI_PeakFWHMEx(handle, threshold, f[0], f[1]));
		return new PeakFWHM(f[0].getValue(), f[1].getValue());
	}

	public BlueMeasurement getBlueMeasurement () {
		check(lib().JETI_BlueMeasurementEx(handle, f[0], f[1], f[2], f[3], f[4], f[5]));
		return new BlueMeasurement(f[0].getValue(), f[1].getValue(), f[2].getValue(), f[3].getValue(), f[4].getValue(),
			f[5].getValue());
	}

	public float getIntegrationTime () {
		check(lib().JETI_RadioTintEx(handle, f[0]));
		return f[0].getValue();
	}

	public void setMeasurementDistance (int distance) {
		check(lib().JETI_SetMeasDistEx(handle, distance));
	}

	public int getMeasurementDistance () {
		check(lib().JETI_GetMeasDistEx(handle, i[0]));
		return i[0].getValue();
	}

	static public int getDeviceCount () {
		var count = new IntByReference();
		check(RadioExLibrary.INSTANCE.JETI_GetNumRadioEx(count));
		return count.getValue();
	}

	static public DeviceSerials getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		check(RadioExLibrary.INSTANCE.JETI_GetSerialRadioEx(deviceNumber, boardSerial, specSerial, deviceSerial));
		return new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial));
	}

	static public RadioEx openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		check(RadioExLibrary.INSTANCE.JETI_OpenRadioEx(deviceNumber, handle));
		return new RadioEx(handle.getValue());
	}

	static public RadioEx openDevice () {
		if (getDeviceCount() <= 0) throw new JetiException(INVALID_DEVICE_NUMBER, "No RadioEx device found.");
		return openDevice(0);
	}

	static public DllVersion getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		check(RadioExLibrary.INSTANCE.JETI_GetRadioExDLLVersion(major, minor, build));
		return new DllVersion(major.getValue(), minor.getValue(), build.getValue());
	}
}
