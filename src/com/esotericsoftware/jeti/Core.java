
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import com.esotericsoftware.jeti.JetiSDK.AdaptationStatus;
import com.esotericsoftware.jeti.JetiSDK.BlueMeasurement;
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
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class Core extends Device<CoreLibrary> {
	public Core (Device device) {
		this(device.handle);
	}

	private Core (Pointer handle) {
		super(CoreLibrary.INSTANCE, handle, CoreLibrary.INSTANCE::JETI_CloseDevice, 3, 2, 4, 8, 4, 1);
	}

	// Device info

	public DeviceType getDeviceType () {
		check(lib().JETI_GetDeviceType(handle, b[0]));
		return DeviceType.values[b[0].getValue()];
	}

	public String getFirmwareVersion () {
		var versionBuffer = new byte[256];
		check(lib().JETI_GetFirmwareVersion(handle, versionBuffer));
		return string(versionBuffer);
	}

	public float getTemperature () {
		check(lib().JETI_GetTemperature(handle, f[0]));
		return f[0].getValue();
	}

	public BatteryInfo getBatteryInfo () {
		check(lib().JETI_GetBatteryStat(handle, f[0], b[0], b[1]));
		return new BatteryInfo(f[0].getValue(), b[0].getValue(), b[1].getValue() != 0);
	}

	public Pointer getComPortHandle () {
		check(lib().JETI_GetComPortHandle(handle, p[0]));
		return p[0].getValue();
	}

	public short measureADC1 () {
		check(lib().JETI_MeasureADC1(handle, s[0]));
		return s[0].getValue();
	}

	public short measureADC2 () {
		check(lib().JETI_MeasureADC2(handle, s[0]));
		return s[0].getValue();
	}

	public byte[] readUserData (int start, int end) {
		var data = new byte[64]; // BOZO - Size?
		check(lib().JETI_ReadUserData64(handle, data, start, end));
		return data;
	}

	public void writeUserData (byte[] data, int block) {
		check(lib().JETI_WriteUserData64(handle, data, block));
	}

	public int getLastError () {
		check(lib().JETI_GetLastError(handle, i[0]));
		return i[0].getValue();
	}

	public int getEnquiry () {
		check(lib().JETI_GetEnquiry(handle, i[0]));
		return i[0].getValue();
	}

	public void setCallbackFunction (byte eventChar, WinDef.HWND mainWindow) {
		if (mainWindow == null) throw new IllegalArgumentException();
		check(lib().JETI_SetCallbackFunction(handle, eventChar, mainWindow));
	}

	// Device communication

	public void reset () {
		check(lib().JETI_Reset(handle));
	}

	public void hardReset () {
		check(lib().JETI_HardReset(handle));
	}

	public void deviceWrite (String command, int bytesToWrite, int timeout) {
		check(lib().JETI_DeviceWrite(handle, command, bytesToWrite, timeout));
	}

	public DeviceRead deviceRead (int bytesToRead, int timeout) {
		var response = new byte[bytesToRead];
		check(lib().JETI_DeviceRead(handle, response, bytesToRead, i[0], timeout));
		return new DeviceRead(response, i[0].getValue());
	}

	public String deviceReadTerminated (int maxBytes, int timeout) {
		var response = new byte[maxBytes];
		check(lib().JETI_DeviceReadTerm(handle, response, maxBytes, timeout));
		return string(response);
	}

	public void dataReceived (int maxLength) {
		check(lib().JETI_DataReceived(handle, maxLength));
	}

	public String sendCommand (String command) {
		var answer = new byte[1024]; // BOZO - Size?
		check(lib().JETI_ArbitraryCommand(handle, command, answer));
		return string(answer);
	}

	// Measurement

	public void measure () {
		check(lib().JETI_InitMeasure(handle));
	}

	public void prepareTriggeredMeasurement () {
		check(lib().JETI_PreTrigMeasure(handle));
	}

	public boolean isMeasuring () {
		check(lib().JETI_MeasureStatusCore(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void cancelMeasurement () {
		check(lib().JETI_Break(handle));
	}

	public float getIntegrationTime () {
		check(lib().JETI_GetTint(handle, f[0]));
		return f[0].getValue();
	}

	public int[] waitReadTrigger (int timeout) {
		var spec = new int[1024 * 10]; // BOZO - Size?
		check(lib().JETI_WaitReadTrigger(handle, spec, timeout));
		return spec;
	}

	public void startAdaptation (boolean reference) {
		check(lib().JETI_StartAdaption(handle, reference));
	}

	public AdaptationStatus getAdaptationStatus () {
		check(lib().JETI_CheckAdaptionStat(handle, f[0], s[0], i[0]));
		return new AdaptationStatus(f[0].getValue(), s[0].getValue(), i[0].getValue() != 0);
	}

	// Calibration

	public Calibration readCalibration (int calibrationNumber) {
		var mode = new byte[16]; // BOZO - Size?
		var remark = new byte[64]; // BOZO - Size?
		var values = new double[2048]; // BOZO - Size?
		check(lib().JETI_ReadCalib(handle, calibrationNumber, mode, remark, i[0], i[1], i[2], i[3], values));
		return new Calibration(string(mode), string(remark), i[0].getValue(), i[1].getValue(), i[2].getValue(), i[3].getValue(),
			values);
	}

	public void writeCalibration (int calibrationNumber, String mode, String remark, int begin, int end, int step,
		int integrationTime, double[] values) {
		check(lib().JETI_WriteCalib(handle, calibrationNumber, mode, remark, begin, end, step, integrationTime, values));
	}

	public void deleteCalibration (int calibrationNumber) {
		check(lib().JETI_DeleteCalib(handle, calibrationNumber));
	}

	public WavelengthRange getCalibrationRange () {
		check(lib().JETI_GetCalibRange(handle, i[0], i[1], i[2]));
		return new WavelengthRange(i[0].getValue(), i[1].getValue(), i[2].getValue());
	}

	public void setCalibration (byte calibrationNumber) {
		check(lib().JETI_SetCalib(handle, calibrationNumber));
	}

	public byte getCalibration () {
		check(lib().JETI_GetCalib(handle, b[0]));
		return b[0].getValue();
	}

	public void measureCompensationDark () {
		check(lib().JETI_MeasCompDark(handle));
	}

	// Parameter functions

	public int getPixelCount () {
		check(lib().JETI_GetPixel(handle, i[0]));
		return i[0].getValue();
	}

	public byte getPixelBinning () {
		check(lib().JETI_GetPixelBinning(handle, b[0]));
		return b[0].getValue();
	}

	public float getFit () {
		check(lib().JETI_GetFit(handle, f[0]));
		return f[0].getValue();
	}

	public int getScanDelay () {
		check(lib().JETI_GetSDelay(handle, i[0]));
		return i[0].getValue();
	}

	public void setScanDelay (int delay) {
		check(lib().JETI_SetSDelay(handle, delay));
	}

	public byte getADCResolution () {
		check(lib().JETI_GetADCRes(handle, b[0]));
		return b[0].getValue();
	}

	public int getSplitTime () {
		check(lib().JETI_GetSplitTime(handle, i[0]));
		return i[0].getValue();
	}

	public Border getBorder () {
		check(lib().JETI_GetBorder(handle, b[0], b[1]));
		return new Border(b[0].getValue(), b[1].getValue());
	}

	public int getDistance () {
		check(lib().JETI_GetDistance(handle, i[0]));
		return i[0].getValue();
	}

	public void setDistance (int distance) {
		check(lib().JETI_SetDistance(handle, distance));
	}

	public byte[] getParameterBlock () {
		var params = new byte[256]; // BOZO - Size?
		check(lib().JETI_GetParamBlock(handle, params));
		return params;
	}

	public void setParameterBlock (byte[] params) {
		check(lib().JETI_SetParamBlock(handle, params));
	}

	public boolean getOpticalTrigger () {
		check(lib().JETI_GetOptTrigg(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void setLaserIntensity (int intensity, int modulation) {
		check(lib().JETI_SetLaserIntensity(handle, intensity, modulation));
	}

	public void setTrigger (int triggerMode) {
		check(lib().JETI_SetTrigger(handle, triggerMode));
	}

	public short getTriggerTimeout () {
		check(lib().JETI_GetTrigTimeout(handle, s[0]));
		return s[0].getValue();
	}

	public void setTriggerTimeout (short timeout) {
		check(lib().JETI_SetTrigTimeout(handle, timeout));
	}

	public void setFlashMode (boolean flashMode) {
		check(lib().JETI_SetFlashMode(handle, flashMode));
	}

	public void setFlashCycle (int flashCycle) {
		check(lib().JETI_SetFlashCycle(handle, flashCycle));
	}

	public boolean getCorrectionStatus () {
		check(lib().JETI_GetCorrectionStat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void setCorrectionStatus (boolean enabled) {
		check(lib().JETI_SetCorrectionStat(handle, enabled));
	}

	public CorrectionRange getCorrectionRange () {
		check(lib().JETI_GetCorrectionRange(handle, i[0], i[1]));
		return new CorrectionRange(i[0].getValue(), i[1].getValue());
	}

	public void setCorrectionRange (int start, int end) {
		check(lib().JETI_SetCorrectionRange(handle, start, end));
	}

	public CorrectionRange getOffsetCorrectionRange () {
		check(lib().JETI_GetOffsetCorrRange(handle, i[0], i[1]));
		return new CorrectionRange(i[0].getValue(), i[1].getValue());
	}

	public void setOffsetCorrectionRange (int start, int end) {
		check(lib().JETI_SetOffsetCorrRange(handle, start, end));
	}

	public float[] getCorrectionCoefficients () {
		var coefficients = new float[256]; // BOZO - Size?
		check(lib().JETI_GetCorrectionCoeff(handle, coefficients));
		return coefficients;
	}

	public void setCorrectionCoefficients (float[] coefficients) {
		check(lib().JETI_SetCorrectionCoeff(handle, coefficients));
	}

	public boolean getCutoffStatus () {
		check(lib().JETI_GetCutoffStat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void setCutoffStatus (boolean enabled) {
		check(lib().JETI_SetCutoffStat(handle, enabled));
	}

	public int getBaudrate () {
		check(lib().JETI_GetBaudrate(handle, i[0]));
		return i[0].getValue();
	}

	public boolean getStrayLightMatrixEnabled () {
		check(lib().JETI_GetSLMEnable(handle, b[0]));
		return b[0].getValue() != 0;
	}

	public void setStrayLightMatrixEnabled (boolean enabled) {
		check(lib().JETI_SetSLMEnable(handle, (byte)(enabled ? 1 : 0)));
	}

	public void setChannelConfig (String configuration) {
		check(lib().JETI_SetChannelConf(handle, configuration));
	}

	public String getChannelConfig () {
		var config = new byte[256]; // BOZO - Size?
		check(lib().JETI_GetChannelConf(handle, config));
		return string(config);
	}

	public void setLampMode (byte mode) {
		check(lib().JETI_SetLampMode(handle, mode));
	}

	public byte getLampMode () {
		check(lib().JETI_GetLampMode(handle, b[0]));
		return b[0].getValue();
	}

	public void setFlash (float interval, float pulseLength) {
		check(lib().JETI_SetFlash(handle, interval, pulseLength));
	}

	public FlashSettings getFlash () {
		check(lib().JETI_GetFlash(handle, f[0], f[1]));
		return new FlashSettings(f[0].getValue(), f[1].getValue());
	}

	// Control functions

	public boolean getLaserStatus () {
		check(lib().JETI_GetLaserStat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void setLaserStatus (boolean enabled) {
		check(lib().JETI_SetLaserStat(handle, enabled));
	}

	public boolean getShutterStatus () {
		check(lib().JETI_GetShutterStat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void setShutterStatus (boolean open) {
		check(lib().JETI_SetShutterStat(handle, open));
	}

	public byte getMeasurementHead () {
		check(lib().JETI_GetMeasHead(handle, b[0]));
		return b[0].getValue();
	}

	public boolean getAux1Status () {
		check(lib().JETI_GetAux1Stat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void setAux1Status (boolean enabled) {
		check(lib().JETI_SetAux1Stat(handle, enabled));
	}

	public boolean getAux2Status () {
		check(lib().JETI_GetAux2Stat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void setAux2Status (boolean enabled) {
		check(lib().JETI_SetAux2Stat(handle, enabled));
	}

	public void setAuxOut1 (boolean state) {
		check(lib().JETI_AuxOut1(handle, state));
	}

	public boolean getAuxOut1Status () {
		check(lib().JETI_AuxOut1Stat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void setAuxOut2 (boolean state) {
		check(lib().JETI_AuxOut2(handle, state));
	}

	public boolean getAuxOut2Status () {
		check(lib().JETI_AuxOut2Stat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void setAuxOut3 (boolean state) {
		check(lib().JETI_AuxOut3(handle, state));
	}

	public boolean getAuxOut3Status () {
		check(lib().JETI_AuxOut3Stat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void setAuxOut4 (boolean state) {
		check(lib().JETI_AuxOut4(handle, state));
	}

	public boolean getAuxOut4Status () {
		check(lib().JETI_AuxOut4Stat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public void setAuxOut5 (boolean state) {
		check(lib().JETI_AuxOut5(handle, state));
	}

	public boolean getAuxOut5Status () {
		check(lib().JETI_AuxOut5Stat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public boolean getAuxIn1Status () {
		check(lib().JETI_AuxIn1Stat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public boolean getAuxIn2Status () {
		check(lib().JETI_AuxIn2Stat(handle, i[0]));
		return i[0].getValue() != 0;
	}

	public FlickerFrequency getFlickerFrequency () {
		check(lib().JETI_GetFlickerFreq(handle, f[0], i[0]));
		FlickerWarning warning = switch (i[0].getValue()) {
		case 11 -> FlickerWarning.noModulation;
		case 12 -> FlickerWarning.fuzzyModulation;
		default -> FlickerWarning.none;
		};
		return new FlickerFrequency(f[0].getValue(), warning);
	}

	public void setSyncFrequency (float frequency) {
		check(lib().JETI_SetSyncFreq(handle, frequency));
	}

	public float getSyncFrequency () {
		check(lib().JETI_GetSyncFreq(handle, f[0]));
		return f[0].getValue();
	}

	public void setSyncMode (boolean mode) {
		check(lib().JETI_SetSyncMode(handle, mode ? (byte)1 : 0));
	}

	public boolean getSyncMode () {
		check(lib().JETI_GetSyncMode(handle, b[0]));
		return b[0].getValue() != 0;
	}

	public byte getDigitalIOInput () {
		check(lib().JETI_GetDIOIn(handle, b[0]));
		return b[0].getValue();
	}

	public byte getDigitalIOOutput () {
		check(lib().JETI_GetDIOOut(handle, b[0]));
		return b[0].getValue();
	}

	public void setDigitalIOOutput (byte value) {
		check(lib().JETI_SetDIOOut(handle, value));
	}

	public void setDigitalIOOutputPin (byte pinNumber, boolean state) {
		check(lib().JETI_SetDIOOutPin(handle, pinNumber, state));
	}

	// Configuration functions

	public byte getDarkModeConfig () {
		check(lib().JETI_GetDarkmodeConf(handle, b[0]));
		return b[0].getValue();
	}

	public void setDarkModeConfig (byte mode) {
		check(lib().JETI_SetDarkmodeConf(handle, mode));
	}

	public byte getExposureConfig () {
		check(lib().JETI_GetExposureConf(handle, b[0]));
		return b[0].getValue();
	}

	public void setExposureConfig (byte mode) {
		check(lib().JETI_SetExposureConf(handle, mode));
	}

	public FunctionConfig getFunctionConfig () {
		check(lib().JETI_GetFunctionConf(handle, b[0], b[1]));
		return new FunctionConfig(b[0].getValue(), b[1].getValue());
	}

	public void setFunctionConfig (byte function) {
		check(lib().JETI_SetFunctionConf(handle, function));
	}

	public FormatConfig getFormatConfig () {
		check(lib().JETI_GetFormatConf(handle, b[0], b[1]));
		return new FormatConfig(b[0].getValue(), b[1].getValue());
	}

	public void setFormatConfig (byte format) {
		check(lib().JETI_SetFormatConf(handle, format));
	}

	public IntegrationTimeConfig getIntegrationTimeConfig () {
		check(lib().JETI_GetTintConf(handle, f[0], f[1]));
		return new IntegrationTimeConfig(f[0].getValue(), f[1].getValue());
	}

	public void setIntegrationTimeConfig (float integrationTime) {
		check(lib().JETI_SetTintConf(handle, integrationTime));
	}

	public float getMaxIntegrationTimeConfig () {
		check(lib().JETI_GetMaxTintConf(handle, f[0]));
		return f[0].getValue();
	}

	public void setMaxIntegrationTimeConfig (float maxIntegrationTime) {
		check(lib().JETI_SetMaxTintConf(handle, maxIntegrationTime));
	}

	public short getMaxAverageConfig () {
		check(lib().JETI_GetMaxAverConf(handle, s[0]));
		return s[0].getValue();
	}

	public void setMaxAverageConfig (short maxAverage) {
		check(lib().JETI_SetMaxAverConf(handle, maxAverage));
	}

	public float getMinIntegrationTimeConfig () {
		check(lib().JETI_GetMinTintConf(handle, f[0]));
		return f[0].getValue();
	}

	public float getImageMinIntegrationTimeConfig () {
		check(lib().JETI_GetImageMinTintConf(handle, f[0]));
		return f[0].getValue();
	}

	public float getChannelMinIntegrationTimeConfig () {
		check(lib().JETI_GetChanMinTintConf(handle, f[0]));
		return f[0].getValue();
	}

	public float getContinuousMinIntegrationTimeConfig () {
		check(lib().JETI_GetContMinTintConf(handle, f[0]));
		return f[0].getValue();
	}

	public float getContinuousChannelMinIntegrationTimeConfig () {
		check(lib().JETI_GetContChanMinTintConf(handle, f[0]));
		return f[0].getValue();
	}

	public AverageConfig getAverageConfig () {
		check(lib().JETI_GetAverConf(handle, s[0], s[1]));
		return new AverageConfig(s[0].getValue(), s[1].getValue());
	}

	public void setAverageConfig (short average) {
		check(lib().JETI_SetAverConf(handle, average));
	}

	public byte getAdaptationConfig () {
		check(lib().JETI_GetAdaptConf(handle, b[0]));
		return b[0].getValue();
	}

	public void setAdaptationConfig (byte mode) {
		check(lib().JETI_SetAdaptConf(handle, mode));
	}

	public WavelengthRange getWavelengthRangeConfig () {
		check(lib().JETI_GetWranConf(handle, i[0], i[1], i[2]));
		return new WavelengthRange(i[0].getValue(), i[1].getValue(), i[2].getValue());
	}

	public void setWavelengthRangeConfig (int begin, int end, int step) {
		check(lib().JETI_SetWranConf(handle, begin, end, step));
	}

	public PDARowConfig getPDARowConfig () {
		check(lib().JETI_GetPDARowConf(handle, i[0], i[1]));
		return new PDARowConfig(i[0].getValue(), i[1].getValue());
	}

	public void setPDARowConfig (int pdaRow, int rowNumber) {
		check(lib().JETI_SetPDARowConf(handle, pdaRow, rowNumber));
	}

	public void setDefault () {
		check(lib().JETI_SetDefault(handle));
	}

	public Level getLevel () {
		check(lib().JETI_GetLevel(handle, i[0], i[1]));
		return new Level(i[0].getValue(), i[1].getValue());
	}

	// Fetch functions

	public int[] fetchDark (int pixelCount) {
		var dark = new int[pixelCount];
		check(lib().JETI_FetchDark(handle, dark));
		return dark;
	}

	public int[] fetchLight (int pixelCount) {
		var light = new int[pixelCount];
		check(lib().JETI_FetchLight(handle, light));
		return light;
	}

	public int[] fetchReference (int pixelCount) {
		var reference = new int[pixelCount];
		check(lib().JETI_FetchRefer(handle, reference));
		return reference;
	}

	public int[] fetchSample (int pixelCount) {
		var transRefl = new int[pixelCount];
		check(lib().JETI_FetchTransRefl(handle, transRefl));
		return transRefl;
	}

	public float[] fetchSpectralRadiance (int begin, int end, float step) {
		var spectralRadiance = new float[(int)((end - begin) / step) + 1];
		check(lib().JETI_FetchSprad(handle, spectralRadiance));
		return spectralRadiance;
	}

	public float[] fetchSpectralRadianceHiRes (int begin, int end) {
		var spectralRadiance = new float[(int)((end - begin) / 0.1f) + 1];
		check(lib().JETI_FetchSpradHiRes(handle, spectralRadiance));
		return spectralRadiance;
	}

	public float fetchRadiometricValue () {
		check(lib().JETI_FetchRadio(handle, f[0]));
		return f[0].getValue();
	}

	public float fetchPhotometricValue () {
		check(lib().JETI_FetchPhoto(handle, f[0]));
		return f[0].getValue();
	}

	public XY fetchChromaXY () {
		check(lib().JETI_FetchChromxy(handle, f[0], f[1]));
		return new XY(f[0].getValue(), f[1].getValue());
	}

	public UV fetchChromaUV () {
		check(lib().JETI_FetchChromuv(handle, f[0], f[1]));
		return new UV(f[0].getValue(), f[1].getValue());
	}

	public DominantWavelength fetchDominantWavelength () {
		check(lib().JETI_FetchDWLPE(handle, f[0], f[1]));
		return new DominantWavelength(f[0].getValue(), f[1].getValue());
	}

	public float fetchCCT () {
		check(lib().JETI_FetchCCT(handle, f[0]));
		return f[0].getValue();
	}

	public float fetchDuv () {
		check(lib().JETI_FetchDuv(handle, f[0]));
		return f[0].getValue();
	}

	public float fetchCRI () {
		check(lib().JETI_FetchCRI(handle, f[0]));
		return f[0].getValue();
	}

	public XYZ fetchXYZ () {
		check(lib().JETI_FetchXYZ(handle, f[0], f[1], f[2]));
		return new XYZ(f[0].getValue(), f[1].getValue(), f[2].getValue());
	}

	public float fetchAdaptationIntegrationTime () {
		check(lib().JETI_FetchTiAdapt(handle, f[0]));
		return f[0].getValue();
	}

	public short fetchAdaptationAverage () {
		check(lib().JETI_FetchAverAdapt(handle, s[0]));
		return s[0].getValue();
	}

	// Calculate functions

	public float[] calculateLinearDark (int begin, int end, float step) {
		var dark = new float[(int)((end - begin) / step) + 1];
		check(lib().JETI_CalcLintDark(handle, begin, end, step, dark));
		return dark;
	}

	public float[] calculateSplineDark (int begin, int end, float step) {
		var dark = new float[(int)((end - begin) / step) + 1];
		check(lib().JETI_CalcSplinDark(handle, begin, end, step, dark));
		return dark;
	}

	public float[] calculateLinearLight (int begin, int end, float step) {
		var light = new float[(int)((end - begin) / step) + 1];
		check(lib().JETI_CalcLintLight(handle, begin, end, step, light));
		return light;
	}

	public float[] calculateSplineLight (int begin, int end, float step) {
		var light = new float[(int)((end - begin) / step) + 1];
		check(lib().JETI_CalcSplinLight(handle, begin, end, step, light));
		return light;
	}

	public float[] calculateLinearReference (int begin, int end, float step) {
		var reference = new float[(int)((end - begin) / step) + 1];
		check(lib().JETI_CalcLintRefer(handle, begin, end, step, reference));
		return reference;
	}

	public float[] calculateSplineReference (int begin, int end, float step) {
		var reference = new float[(int)((end - begin) / step) + 1];
		check(lib().JETI_CalcSplinRefer(handle, begin, end, step, reference));
		return reference;
	}

	public float[] calculateLinearSample (int begin, int end, float step) {
		var transRefl = new float[(int)((end - begin) / step) + 1];
		check(lib().JETI_CalcLintTransRefl(handle, begin, end, step, transRefl));
		return transRefl;
	}

	public float[] calculateSplineSample (int begin, int end, float step) {
		var transRefl = new float[(int)((end - begin) / step) + 1];
		check(lib().JETI_CalcSplinTransRefl(handle, begin, end, step, transRefl));
		return transRefl;
	}

	public float calculateRadiometricValue (int begin, int end) {
		check(lib().JETI_CalcRadio(handle, begin, end, f[0]));
		return f[0].getValue();
	}

	public float calculatePhotometricValue () {
		check(lib().JETI_CalcPhoto(handle, f[0]));
		return f[0].getValue();
	}

	public XY calculateChromaXY () {
		check(lib().JETI_CalcChromxy(handle, f[0], f[1]));
		return new XY(f[0].getValue(), f[1].getValue());
	}

	public XY10 calculateChromaXY10 () {
		check(lib().JETI_CalcChromxy10(handle, f[0], f[1]));
		return new XY10(f[0].getValue(), f[1].getValue());
	}

	public UV calculateChromaUV () {
		check(lib().JETI_CalcChromuv(handle, f[0], f[1]));
		return new UV(f[0].getValue(), f[1].getValue());
	}

	public DominantWavelength calculateDominantWavelength () {
		check(lib().JETI_CalcDWLPE(handle, f[0], f[1]));
		return new DominantWavelength(f[0].getValue(), f[1].getValue());
	}

	public float calculateCCT () {
		check(lib().JETI_CalcCCT(handle, f[0]));
		return f[0].getValue();
	}

	public float calculateDuv () {
		check(lib().JETI_CalcDuv(handle, f[0]));
		return f[0].getValue();
	}

	public float calculateCRI (float cct) {
		check(lib().JETI_CalcCRI(handle, cct, f[0]));
		return f[0].getValue();
	}

	public XYZ calculateXYZ () {
		check(lib().JETI_CalcXYZ(handle, f[0], f[1], f[2]));
		return new XYZ(f[0].getValue(), f[1].getValue(), f[2].getValue());
	}

	public AllValues calculateAllValues (int begin, int end) {
		check(lib().JETI_CalcAllValue(handle, begin, end, f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7]));
		return new AllValues(f[0].getValue(), f[1].getValue(), new XY(f[2].getValue(), f[3].getValue()),
			new UV(f[4].getValue(), f[5].getValue()), new DominantWavelength(f[6].getValue(), f[7].getValue()));
	}

	public TM30 calculateTM30 (boolean useTM3015) {
		var rfi = new double[useTM3015 ? 15 : 16];
		var rfces = new double[99];
		check(lib().JETI_CalcTM30(handle, (byte)(useTM3015 ? 1 : 0), d[0], d[1], d[2], d[3], rfi, rfces));
		return new TM30(d[0].getValue(), d[1].getValue(), d[2].getValue(), d[3].getValue(), rfi, rfces);
	}

	public PeakFWHM calculatePeakFWHM (float threshold) {
		check(lib().JETI_CalcPeakFWHM(handle, threshold, f[0], f[1]));
		return new PeakFWHM(f[0].getValue(), f[1].getValue());
	}

	public BlueMeasurement calculateBlueMeasurement () {
		check(lib().JETI_CalcBlueMeasurement(handle, f[0], f[1], f[2], f[3], f[4], f[5]));
		return new BlueMeasurement(f[0].getValue(), f[1].getValue(), f[2].getValue(), f[3].getValue(), f[4].getValue(),
			f[5].getValue());
	}

	static public void setLicenseKey (String licenseKey) {
		check(CoreLibrary.INSTANCE.JETI_SetLicKey(licenseKey));
	}

	static public void importStraylightMatrix (String matrixFile) {
		check(CoreLibrary.INSTANCE.JETI_ImportSLM(matrixFile));
	}

	static public void ignoreStraylightMatrix (boolean ignore) {
		check(CoreLibrary.INSTANCE.JETI_IgnoreSLM((byte)(ignore ? 1 : 0)));
	}

	static public int getDeviceCount () {
		var count = new IntByReference();
		check(CoreLibrary.INSTANCE.JETI_GetNumDevices(count));
		return count.getValue();
	}

	static public DeviceSerials getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		check(CoreLibrary.INSTANCE.JETI_GetSerialDevice(deviceNumber, boardSerial, specSerial, deviceSerial));
		return new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial));
	}

	static public DeviceInfo getDeviceInfo (int deviceNumber) {
		var connType = new ByteByReference();
		var deviceType = new ByteByReference();
		var deviceSerial = new byte[STRING_SIZE];
		var comPortNr = new ShortByReference();
		var baudrate = new IntByReference();
		var ipAddress = new byte[STRING_SIZE];
		var usbSerial = new byte[STRING_SIZE];
		var btAddress = new LongByReference();
		check(CoreLibrary.INSTANCE.JETI_GetDeviceInfo(deviceNumber, connType, deviceType, deviceSerial, comPortNr, baudrate,
			ipAddress, usbSerial, btAddress));
		return new DeviceInfo(ConnectionType.values[connType.getValue()], DeviceType.values[deviceType.getValue()],
			string(deviceSerial), (int)comPortNr.getValue(), baudrate.getValue(), string(ipAddress), string(usbSerial),
			btAddress.getValue(), null);
	}

	static public DeviceInfo getDeviceInfoEx (int deviceNumber) {
		var connType = new ByteByReference();
		var deviceType = new ByteByReference();
		var deviceSerial = new byte[STRING_SIZE];
		var comPortNr = new ShortByReference();
		var baudrate = new IntByReference();
		var ipAddress = new byte[STRING_SIZE];
		var usbSerial = new byte[STRING_SIZE];
		var btAddress = new LongByReference();
		var btleDevicePath = new char[256]; // BOZO - Size?
		check(CoreLibrary.INSTANCE.JETI_GetDeviceInfoEx(deviceNumber, connType, deviceType, deviceSerial, comPortNr, baudrate,
			ipAddress, usbSerial, btAddress, btleDevicePath));
		return new DeviceInfo(ConnectionType.values[connType.getValue()], DeviceType.values[deviceType.getValue()],
			string(deviceSerial), (int)comPortNr.getValue(), baudrate.getValue(), string(ipAddress), string(usbSerial),
			btAddress.getValue(), new String(btleDevicePath).trim());
	}

	static public DeviceInfo[] getAllDeviceInfo () {
		int count = getDeviceCount();
		var devices = new DeviceInfo[count];
		for (int i = 0; i < count; i++)
			devices[i] = getDeviceInfo(i);
		return devices;
	}

	static public Core openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		check(CoreLibrary.INSTANCE.JETI_OpenDevice(deviceNumber, handle));
		return new Core(handle.getValue());
	}

	static public Core openComDevice (int comPort, int baudrate) {
		var handle = new PointerByReference();
		check(CoreLibrary.INSTANCE.JETI_OpenCOMDevice(comPort, baudrate, handle));
		return new Core(handle.getValue());
	}

	static public Core openTcpDevice (String ipAddress) {
		var handle = new PointerByReference();
		check(CoreLibrary.INSTANCE.JETI_OpenTCPDevice(ipAddress, handle));
		return new Core(handle.getValue());
	}

	static public Core openUsbDevice (String usbSerial) {
		var handle = new PointerByReference();
		check(CoreLibrary.INSTANCE.JETI_OpenFTDIDevice(usbSerial, handle));
		return new Core(handle.getValue());
	}

	static public Core openBluetoothDevice (long btAddress) {
		var handle = new PointerByReference();
		check(CoreLibrary.INSTANCE.JETI_OpenBTDevice(btAddress, handle));
		return new Core(handle.getValue());
	}

	static public Core openBluetoothLeDevice (String devicePath) {
		var handle = new PointerByReference();
		char[] pathChars = devicePath.toCharArray();
		check(CoreLibrary.INSTANCE.JETI_OpenBTLEDevice(pathChars, handle));
		return new Core(handle.getValue());
	}

	static public DllVersion getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		check(CoreLibrary.INSTANCE.JETI_GetCoreDLLVersion(major, minor, build));
		return new DllVersion(major.getValue(), minor.getValue(), build.getValue());
	}

	public enum ConnectionType {
		com, usb, tcp, bluetooth;

		static public final ConnectionType[] values = values();
	}

	public enum DeviceType {
		generic, specbos_xx01, specbos_1211, spectraval_1501_1511_sdcm3, sdcm4_pe602_pe65, specbos_25x1;

		static public final DeviceType[] values = values();
	}

	public record DeviceInfo (
		ConnectionType connectionType,
		DeviceType deviceType,
		String deviceSerial,
		int comPortNumber,
		int baudrate,
		String ipAddress,
		String usbSerial,
		long bluetoothAddress,
		String bluetoothLeDevicePath) {}

	public record BatteryInfo (float voltage, int percent, boolean charging) {}

	public record DeviceRead (byte[] data, int bytesRead) {}

	public record Calibration (String mode, String remark, int begin, int end, int step, int integrationTime, double[] values) {}

	public record WavelengthRange (int begin, int end, int step) {}

	public record Border (byte min, byte max) {}

	public record CorrectionRange (int start, int end) {}

	public record FlashSettings (float interval, float pulseLength) {}

	public enum FlickerWarning {
		none, noModulation, fuzzyModulation
	}

	public record FlickerFrequency (float frequency, FlickerWarning warning) {}

	public record FunctionConfig (byte previous, byte configured) {}

	public record FormatConfig (byte previous, byte configured) {}

	public record IntegrationTimeConfig (float previous, float configured) {}

	public record AverageConfig (short previous, short configured) {}

	public record PDARowConfig (int pdaRow, int rowNumber) {}

	public record Level (int counts, int percent) {}

	public record AllValues (
		float radiometric,
		float photometric,
		XY chromaXY,
		UV chromaUV,
		DominantWavelength dominantWavelength) {}
}
