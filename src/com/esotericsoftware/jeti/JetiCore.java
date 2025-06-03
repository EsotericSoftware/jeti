
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiResult.*;
import static com.esotericsoftware.jeti.JetiSDK.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
public class JetiCore extends Device<JetiCoreLibrary> {
	private JetiCore (Pointer handle) {
		super(JetiCoreLibrary.INSTANCE, handle, JetiCoreLibrary.INSTANCE::JETI_CloseDevice, 3, 2, 4, 8, 4, 1);
	}

	// Device info

	public JetiResult<DeviceType> getDeviceType () {
		int result = lib().JETI_GetDeviceType(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(DeviceType.values[b[0].getValue()]);
	}

	public JetiResult<String> getFirmwareVersion () {
		var versionBuffer = new byte[256];
		int result = lib().JETI_GetFirmwareVersion(handle, versionBuffer);
		if (result != SUCCESS) return error(result);
		return success(new String(versionBuffer, StandardCharsets.UTF_8).trim());
	}

	public JetiResult<Float> getTemperature () {
		int result = lib().JETI_GetTemperature(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<BatteryInfo> getBatteryInfo () {
		int result = lib().JETI_GetBatteryStat(handle, f[0], b[0], b[1]);
		if (result != SUCCESS) return error(result);
		return success(new BatteryInfo(f[0].getValue(), b[0].getValue(), b[1].getValue() != 0));
	}

	public JetiResult<Pointer> getComPortHandle () {
		int result = lib().JETI_GetComPortHandle(handle, p[0]);
		if (result != SUCCESS) return error(result);
		return success(p[0].getValue());
	}

	public JetiResult<Short> measureADC1 () {
		int result = lib().JETI_MeasureADC1(handle, s[0]);
		if (result != SUCCESS) return error(result);
		return success(s[0].getValue());
	}

	public JetiResult<Short> measureADC2 () {
		int result = lib().JETI_MeasureADC2(handle, s[0]);
		if (result != SUCCESS) return error(result);
		return success(s[0].getValue());
	}

	public JetiResult<byte[]> readUserData (int start, int end) {
		var data = new byte[64]; // Size?
		int result = lib().JETI_ReadUserData64(handle, data, start, end);
		if (result != SUCCESS) return error(result);
		return success(data);
	}

	public JetiResult<Boolean> writeUserData (byte[] data, int block) {
		return result(lib().JETI_WriteUserData64(handle, data, block));
	}

	public JetiResult<Integer> getLastError () {
		int result = lib().JETI_GetLastError(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue());
	}

	public JetiResult<Integer> getEnquiry () {
		int result = lib().JETI_GetEnquiry(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue());
	}

	public JetiResult<Boolean> setCallbackFunction (byte eventChar, WinDef.HWND mainWindow) {
		return result(lib().JETI_SetCallbackFunction(handle, eventChar, mainWindow));
	}

	// Device communication

	public JetiResult<Boolean> reset () {
		return result(lib().JETI_Reset(handle));
	}

	public JetiResult<Boolean> hardReset () {
		return result(lib().JETI_HardReset(handle));
	}

	public JetiResult<Boolean> deviceWrite (String command, int bytesToWrite, int timeout) {
		return result(lib().JETI_DeviceWrite(handle, command, bytesToWrite, timeout));
	}

	public JetiResult<DeviceRead> deviceRead (int bytesToRead, int timeout) {
		var response = new byte[bytesToRead];
		int result = lib().JETI_DeviceRead(handle, response, bytesToRead, i[0], timeout);
		if (result != SUCCESS) return error(result);
		return success(new DeviceRead(response, i[0].getValue()));
	}

	public JetiResult<String> deviceReadTerminated (int maxBytes, int timeout) {
		var response = new byte[maxBytes];
		int result = lib().JETI_DeviceReadTerm(handle, response, maxBytes, timeout);
		if (result != SUCCESS) return error(result);
		return success(string(response));
	}

	public JetiResult<Integer> dataReceived (int maxLength) {
		return success(lib().JETI_DataReceived(handle, maxLength));
	}

	public JetiResult<String> sendCommand (String command) {
		var answer = new byte[1024]; // Size?
		int result = lib().JETI_ArbitraryCommand(handle, command, answer);
		if (result != SUCCESS) return error(result);
		return success(new String(answer, StandardCharsets.UTF_8).trim());
	}

	// Measurement

	public JetiResult<Boolean> measure () {
		return result(lib().JETI_InitMeasure(handle));
	}

	public JetiResult<Boolean> prepareMeasurement () {
		return result(lib().JETI_PreTrigMeasure(handle));
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		int result = lib().JETI_MeasureStatusCore(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> breakMeasurement () {
		return result(lib().JETI_Break(handle));
	}

	public JetiResult<Float> getIntegrationTime () {
		int result = lib().JETI_GetTint(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<int[]> waitReadTrigger (int timeout) {
		var spec = new int[1024]; // Size?
		int result = lib().JETI_WaitReadTrigger(handle, i[0], timeout);
		if (result != SUCCESS) return error(result);
		return success(spec);
	}

	public JetiResult<Boolean> startAdaptation (boolean reference) {
		return result(lib().JETI_StartAdaption(handle, reference ? 1 : 0));
	}

	public JetiResult<AdaptationStatus> checkAdaptationStatus () {
		int result = lib().JETI_CheckAdaptionStat(handle, f[0], s[0], i[0]);
		if (result != SUCCESS) return error(result);
		return success(new AdaptationStatus(f[0].getValue(), s[0].getValue(), i[0].getValue() != 0));
	}

	// Calibration

	public JetiResult<CalibrationData> readCalibration (int calibrationNumber) {
		var mode = new byte[16]; // Size?
		var remark = new byte[64]; // Size?
		var values = new double[2048]; // Size?
		int result = lib().JETI_ReadCalib(handle, calibrationNumber, mode, remark, i[0], i[1], i[2], i[3], values);
		if (result != SUCCESS) return error(result);
		return success(new CalibrationData(string(mode), string(remark), i[0].getValue(), i[1].getValue(), i[2].getValue(),
			i[3].getValue(), values));
	}

	public JetiResult<Boolean> writeCalibration (int calibrationNumber, String mode, String remark, int begin, int end, int step,
		int integrationTime, double[] values) {
		return result(lib().JETI_WriteCalib(handle, calibrationNumber, mode, remark, begin, end, step, integrationTime, values));
	}

	public JetiResult<Boolean> deleteCalibration (int calibrationNumber) {
		return result(lib().JETI_DeleteCalib(handle, calibrationNumber));
	}

	public JetiResult<WavelengthRange> getCalibrationRange () {
		int result = lib().JETI_GetCalibRange(handle, i[0], i[1], i[2]);
		if (result != SUCCESS) return error(result);
		return success(new WavelengthRange(i[0].getValue(), i[1].getValue(), i[2].getValue()));
	}

	public JetiResult<Boolean> setCalibration (byte calibrationNumber) {
		return result(lib().JETI_SetCalib(handle, calibrationNumber));
	}

	public JetiResult<Byte> getCalibration () {
		int result = lib().JETI_GetCalib(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(b[0].getValue());
	}

	public JetiResult<Boolean> measureCompensationDark () {
		return result(lib().JETI_MeasCompDark(handle));
	}

	// Parameter functions

	public JetiResult<Integer> getPixelCount () {
		int result = lib().JETI_GetPixel(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue());
	}

	public JetiResult<Byte> getPixelBinning () {
		int result = lib().JETI_GetPixelBinning(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(b[0].getValue());
	}

	public JetiResult<Float> getFit () {
		int result = lib().JETI_GetFit(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Integer> getScanDelay () {
		int result = lib().JETI_GetSDelay(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue());
	}

	public JetiResult<Boolean> setScanDelay (int delay) {
		return result(lib().JETI_SetSDelay(handle, delay));
	}

	public JetiResult<Byte> getADCResolution () {
		int result = lib().JETI_GetADCRes(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(b[0].getValue());
	}

	public JetiResult<Integer> getSplitTime () {
		int result = lib().JETI_GetSplitTime(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue());
	}

	public JetiResult<Border> getBorder () {
		int result = lib().JETI_GetBorder(handle, b[0], b[1]);
		if (result != SUCCESS) return error(result);
		return success(new Border(b[0].getValue(), b[1].getValue()));
	}

	public JetiResult<Integer> getDistance () {
		int result = lib().JETI_GetDistance(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue());
	}

	public JetiResult<Boolean> setDistance (int distance) {
		return result(lib().JETI_SetDistance(handle, distance));
	}

	public JetiResult<byte[]> getParameterBlock () {
		var params = new byte[256]; // Size?
		int result = lib().JETI_GetParamBlock(handle, params);
		if (result != SUCCESS) return error(result);
		return success(params);
	}

	public JetiResult<Boolean> setParameterBlock (byte[] params) {
		return result(lib().JETI_SetParamBlock(handle, params));
	}

	public JetiResult<Boolean> getOpticalTrigger () {
		int result = lib().JETI_GetOptTrigg(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> setLaserIntensity (int intensity, int modulation) {
		return result(lib().JETI_SetLaserIntensity(handle, intensity, modulation));
	}

	public JetiResult<Boolean> setTrigger (int triggerMode) {
		return result(lib().JETI_SetTrigger(handle, triggerMode));
	}

	public JetiResult<Short> getTriggerTimeout () {
		int result = lib().JETI_GetTrigTimeout(handle, s[0]);
		if (result != SUCCESS) return error(result);
		return success(s[0].getValue());
	}

	public JetiResult<Boolean> setTriggerTimeout (short timeout) {
		return result(lib().JETI_SetTrigTimeout(handle, timeout));
	}

	public JetiResult<Boolean> setFlashMode (boolean flashMode) {
		return result(lib().JETI_SetFlashMode(handle, flashMode ? 1 : 0));
	}

	public JetiResult<Boolean> setFlashCycle (int flashCycle) {
		return result(lib().JETI_SetFlashCycle(handle, flashCycle));
	}

	public JetiResult<Boolean> getCorrectionStatus () {
		int result = lib().JETI_GetCorrectionStat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> setCorrectionStatus (boolean enabled) {
		return result(lib().JETI_SetCorrectionStat(handle, enabled ? 1 : 0));
	}

	public JetiResult<CorrectionRange> getCorrectionRange () {
		int result = lib().JETI_GetCorrectionRange(handle, i[0], i[1]);
		if (result != SUCCESS) return error(result);
		return success(new CorrectionRange(i[0].getValue(), i[1].getValue()));
	}

	public JetiResult<Boolean> setCorrectionRange (int start, int end) {
		return result(lib().JETI_SetCorrectionRange(handle, start, end));
	}

	public JetiResult<CorrectionRange> getOffsetCorrectionRange () {
		int result = lib().JETI_GetOffsetCorrRange(handle, i[0], i[1]);
		if (result != SUCCESS) return error(result);
		return success(new CorrectionRange(i[0].getValue(), i[1].getValue()));
	}

	public JetiResult<Boolean> setOffsetCorrectionRange (int start, int end) {
		return result(lib().JETI_SetOffsetCorrRange(handle, start, end));
	}

	public JetiResult<float[]> getCorrectionCoefficients () {
		var coefficients = new float[256]; // Size?
		int result = lib().JETI_GetCorrectionCoeff(handle, coefficients);
		if (result != SUCCESS) return error(result);
		return success(coefficients);
	}

	public JetiResult<Boolean> setCorrectionCoefficients (float[] coefficients) {
		return result(lib().JETI_SetCorrectionCoeff(handle, coefficients));
	}

	public JetiResult<Boolean> getCutoffStatus () {
		int result = lib().JETI_GetCutoffStat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> setCutoffStatus (boolean enabled) {
		return result(lib().JETI_SetCutoffStat(handle, enabled ? 1 : 0));
	}

	public JetiResult<Integer> getBaudrate () {
		int result = lib().JETI_GetBaudrate(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue());
	}

	public JetiResult<Boolean> getStrayLightMatrixEnabled () {
		int result = lib().JETI_GetSLMEnable(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(b[0].getValue() != 0);
	}

	public JetiResult<Boolean> setStrayLightMatrixEnabled (boolean enabled) {
		return result(lib().JETI_SetSLMEnable(handle, (byte)(enabled ? 1 : 0)));
	}

	public JetiResult<Boolean> setChannelConfig (String configuration) {
		return result(lib().JETI_SetChannelConf(handle, configuration));
	}

	public JetiResult<String> getChannelConfig () {
		var config = new byte[256]; // Size?
		int result = lib().JETI_GetChannelConf(handle, config);
		if (result != SUCCESS) return error(result);
		return success(string(config));
	}

	public JetiResult<Boolean> setLampMode (byte mode) {
		return result(lib().JETI_SetLampMode(handle, mode));
	}

	public JetiResult<Byte> getLampMode () {
		int result = lib().JETI_GetLampMode(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(b[0].getValue());
	}

	public JetiResult<Boolean> setFlash (float interval, float pulseLength) {
		return result(lib().JETI_SetFlash(handle, interval, pulseLength));
	}

	public JetiResult<FlashSettings> getFlash () {
		int result = lib().JETI_GetFlash(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new FlashSettings(f[0].getValue(), f[1].getValue()));
	}

	// Control functions

	public JetiResult<Boolean> getLaserStatus () {
		int result = lib().JETI_GetLaserStat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> setLaserStatus (boolean enabled) {
		return result(lib().JETI_SetLaserStat(handle, enabled ? 1 : 0));
	}

	public JetiResult<Boolean> getShutterStatus () {
		int result = lib().JETI_GetShutterStat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> setShutterStatus (boolean open) {
		return result(lib().JETI_SetShutterStat(handle, open ? 1 : 0));
	}

	public JetiResult<Byte> getMeasurementHead () {
		int result = lib().JETI_GetMeasHead(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(b[0].getValue());
	}

	public JetiResult<Boolean> getAux1Status () {
		int result = lib().JETI_GetAux1Stat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> setAux1Status (boolean enabled) {
		return result(lib().JETI_SetAux1Stat(handle, enabled ? 1 : 0));
	}

	public JetiResult<Boolean> getAux2Status () {
		int result = lib().JETI_GetAux2Stat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> setAux2Status (boolean enabled) {
		return result(lib().JETI_SetAux2Stat(handle, enabled ? 1 : 0));
	}

	public JetiResult<Boolean> setAuxOut1 (boolean state) {
		return result(lib().JETI_AuxOut1(handle, state ? 1 : 0));
	}

	public JetiResult<Boolean> getAuxOut1Status () {
		int result = lib().JETI_AuxOut1Stat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> setAuxOut2 (boolean state) {
		return result(lib().JETI_AuxOut2(handle, state ? 1 : 0));
	}

	public JetiResult<Boolean> getAuxOut2Status () {
		int result = lib().JETI_AuxOut2Stat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> setAuxOut3 (boolean state) {
		return result(lib().JETI_AuxOut3(handle, state ? 1 : 0));
	}

	public JetiResult<Boolean> getAuxOut3Status () {
		int result = lib().JETI_AuxOut3Stat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> setAuxOut4 (boolean state) {
		return result(lib().JETI_AuxOut4(handle, state ? 1 : 0));
	}

	public JetiResult<Boolean> getAuxOut4Status () {
		int result = lib().JETI_AuxOut4Stat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> setAuxOut5 (boolean state) {
		return result(lib().JETI_AuxOut5(handle, state ? 1 : 0));
	}

	public JetiResult<Boolean> getAuxOut5Status () {
		int result = lib().JETI_AuxOut5Stat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> getAuxIn1Status () {
		int result = lib().JETI_AuxIn1Stat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<Boolean> getAuxIn2Status () {
		int result = lib().JETI_AuxIn2Stat(handle, i[0]);
		if (result != SUCCESS) return error(result);
		return success(i[0].getValue() != 0);
	}

	public JetiResult<FlickerFrequency> getFlickerFrequency () {
		int result = lib().JETI_GetFlickerFreq(handle, f[0], i[0]);
		if (result != SUCCESS) return error(result);
		return success(new FlickerFrequency(f[0].getValue(), i[0].getValue()));
	}

	public JetiResult<Boolean> setSyncFrequency (float frequency) {
		return result(lib().JETI_SetSyncFreq(handle, frequency));
	}

	public JetiResult<Float> getSyncFrequency () {
		int result = lib().JETI_GetSyncFreq(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Boolean> setSyncMode (byte mode) {
		return result(lib().JETI_SetSyncMode(handle, mode));
	}

	public JetiResult<Byte> getSyncMode () {
		int result = lib().JETI_GetSyncMode(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(b[0].getValue());
	}

	public JetiResult<Byte> getDigitalIOInput () {
		int result = lib().JETI_GetDIOIn(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(b[0].getValue());
	}

	public JetiResult<Byte> getDigitalIOOutput () {
		int result = lib().JETI_GetDIOOut(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(b[0].getValue());
	}

	public JetiResult<Boolean> setDigitalIOOutput (byte value) {
		return result(lib().JETI_SetDIOOut(handle, value));
	}

	public JetiResult<Boolean> setDigitalIOOutputPin (byte pinNumber, boolean state) {
		return result(lib().JETI_SetDIOOutPin(handle, pinNumber, state ? 1 : 0));
	}

	// Configuration functions

	public JetiResult<Byte> getDarkModeConfig () {
		int result = lib().JETI_GetDarkmodeConf(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(b[0].getValue());
	}

	public JetiResult<Boolean> setDarkModeConfig (byte mode) {
		return result(lib().JETI_SetDarkmodeConf(handle, mode));
	}

	public JetiResult<Byte> getExposureConfig () {
		int result = lib().JETI_GetExposureConf(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(b[0].getValue());
	}

	public JetiResult<Boolean> setExposureConfig (byte mode) {
		return result(lib().JETI_SetExposureConf(handle, mode));
	}

	public JetiResult<FunctionConfig> getFunctionConfig () {
		int result = lib().JETI_GetFunctionConf(handle, b[0], b[1]);
		if (result != SUCCESS) return error(result);
		return success(new FunctionConfig(b[0].getValue(), b[1].getValue()));
	}

	public JetiResult<Boolean> setFunctionConfig (byte function) {
		return result(lib().JETI_SetFunctionConf(handle, function));
	}

	public JetiResult<FormatConfig> getFormatConfig () {
		int result = lib().JETI_GetFormatConf(handle, b[0], b[1]);
		if (result != SUCCESS) return error(result);
		return success(new FormatConfig(b[0].getValue(), b[1].getValue()));
	}

	public JetiResult<Boolean> setFormatConfig (byte format) {
		return result(lib().JETI_SetFormatConf(handle, format));
	}

	public JetiResult<IntegrationTimeConfig> getIntegrationTimeConfig () {
		int result = lib().JETI_GetTintConf(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new IntegrationTimeConfig(f[0].getValue(), f[1].getValue()));
	}

	public JetiResult<Boolean> setIntegrationTimeConfig (float integrationTime) {
		return result(lib().JETI_SetTintConf(handle, integrationTime));
	}

	public JetiResult<Float> getMaxIntegrationTimeConfig () {
		int result = lib().JETI_GetMaxTintConf(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Boolean> setMaxIntegrationTimeConfig (float maxIntegrationTime) {
		return result(lib().JETI_SetMaxTintConf(handle, maxIntegrationTime));
	}

	public JetiResult<Short> getMaxAverageConfig () {
		int result = lib().JETI_GetMaxAverConf(handle, s[0]);
		if (result != SUCCESS) return error(result);
		return success(s[0].getValue());
	}

	public JetiResult<Boolean> setMaxAverageConfig (short maxAverage) {
		return result(lib().JETI_SetMaxAverConf(handle, maxAverage));
	}

	public JetiResult<Float> getMinIntegrationTimeConfig () {
		int result = lib().JETI_GetMinTintConf(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Float> getImageMinIntegrationTimeConfig () {
		int result = lib().JETI_GetImageMinTintConf(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Float> getChannelMinIntegrationTimeConfig () {
		int result = lib().JETI_GetChanMinTintConf(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Float> getContinuousMinIntegrationTimeConfig () {
		int result = lib().JETI_GetContMinTintConf(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Float> getContinuousChannelMinIntegrationTimeConfig () {
		int result = lib().JETI_GetContChanMinTintConf(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<AverageConfig> getAverageConfig () {
		int result = lib().JETI_GetAverConf(handle, s[0], s[1]);
		if (result != SUCCESS) return error(result);
		return success(new AverageConfig(s[0].getValue(), s[1].getValue()));
	}

	public JetiResult<Boolean> setAverageConfig (short average) {
		return result(lib().JETI_SetAverConf(handle, average));
	}

	public JetiResult<Byte> getAdaptationConfig () {
		int result = lib().JETI_GetAdaptConf(handle, b[0]);
		if (result != SUCCESS) return error(result);
		return success(b[0].getValue());
	}

	public JetiResult<Boolean> setAdaptationConfig (byte mode) {
		return result(lib().JETI_SetAdaptConf(handle, mode));
	}

	public JetiResult<WavelengthRange> getWavelengthRangeConfig () {
		int result = lib().JETI_GetWranConf(handle, i[0], i[1], i[2]);
		if (result != SUCCESS) return error(result);
		return success(new WavelengthRange(i[0].getValue(), i[1].getValue(), i[2].getValue()));
	}

	public JetiResult<Boolean> setWavelengthRangeConfig (int begin, int end, int step) {
		return result(lib().JETI_SetWranConf(handle, begin, end, step));
	}

	public JetiResult<PDARowConfig> getPDARowConfig () {
		int result = lib().JETI_GetPDARowConf(handle, i[0], i[1]);
		if (result != SUCCESS) return error(result);
		return success(new PDARowConfig(i[0].getValue(), i[1].getValue()));
	}

	public JetiResult<Boolean> setPDARowConfig (int pdaRow, int rowNumber) {
		return result(lib().JETI_SetPDARowConf(handle, pdaRow, rowNumber));
	}

	public JetiResult<Boolean> setDefault () {
		return result(lib().JETI_SetDefault(handle));
	}

	public JetiResult<Level> getLevel () {
		int result = lib().JETI_GetLevel(handle, i[0], i[1]);
		if (result != SUCCESS) return error(result);
		return success(new Level(i[0].getValue(), i[1].getValue()));
	}

	// Fetch functions

	public JetiResult<int[]> fetchDark (int pixelCount) {
		var dark = new int[pixelCount];
		int result = lib().JETI_FetchDark(handle, dark);
		if (result != SUCCESS) return error(result);
		return success(dark);
	}

	public JetiResult<int[]> fetchLight (int pixelCount) {
		var light = new int[pixelCount];
		int result = lib().JETI_FetchLight(handle, light);
		if (result != SUCCESS) return error(result);
		return success(light);
	}

	public JetiResult<int[]> fetchReference (int pixelCount) {
		var reference = new int[pixelCount];
		int result = lib().JETI_FetchRefer(handle, reference);
		if (result != SUCCESS) return error(result);
		return success(reference);
	}

	public JetiResult<int[]> fetchTransmissionReflection (int pixelCount) {
		var transRefl = new int[pixelCount];
		int result = lib().JETI_FetchTransRefl(handle, transRefl);
		if (result != SUCCESS) return error(result);
		return success(transRefl);
	}

	public JetiResult<float[]> fetchSpectralRadiance (int begin, int end, float step) {
		var spectralRadiance = new float[(int)((end - begin) / step) + 1];
		int result = lib().JETI_FetchSprad(handle, spectralRadiance);
		if (result != SUCCESS) return error(result);
		return success(spectralRadiance);
	}

	public JetiResult<float[]> fetchSpectralRadianceHiRes (int begin, int end) {
		var spectralRadiance = new float[(int)((end - begin) / 0.1f) + 1];
		int result = lib().JETI_FetchSpradHiRes(handle, spectralRadiance);
		if (result != SUCCESS) return error(result);
		return success(spectralRadiance);
	}

	public JetiResult<Float> fetchRadiometricValue () {
		int result = lib().JETI_FetchRadio(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Float> fetchPhotometricValue () {
		int result = lib().JETI_FetchPhoto(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<XY> fetchChromaXY () {
		int result = lib().JETI_FetchChromxy(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new XY(f[0].getValue(), f[1].getValue()));
	}

	public JetiResult<UV> fetchChromaUV () {
		int result = lib().JETI_FetchChromuv(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new UV(f[0].getValue(), f[1].getValue()));
	}

	public JetiResult<DominantWavelength> fetchDominantWavelength () {
		int result = lib().JETI_FetchDWLPE(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new DominantWavelength(f[0].getValue(), f[1].getValue()));
	}

	public JetiResult<Float> fetchCCT () {
		int result = lib().JETI_FetchCCT(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Float> fetchDuv () {
		int result = lib().JETI_FetchDuv(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Float> fetchCRI () {
		int result = lib().JETI_FetchCRI(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<XYZ> fetchXYZ () {
		int result = lib().JETI_FetchXYZ(handle, f[0], f[1], f[2]);
		if (result != SUCCESS) return error(result);
		return success(new XYZ(f[0].getValue(), f[1].getValue(), f[2].getValue()));
	}

	public JetiResult<Float> fetchAdaptationIntegrationTime () {
		int result = lib().JETI_FetchTiAdapt(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Short> fetchAdaptationAverage () {
		int result = lib().JETI_FetchAverAdapt(handle, s[0]);
		if (result != SUCCESS) return error(result);
		return success(s[0].getValue());
	}

	// Calculate functions

	public JetiResult<float[]> calculateLinearDark (int begin, int end, float step) {
		var dark = new float[(int)((end - begin) / step) + 1];
		int result = lib().JETI_CalcLintDark(handle, begin, end, step, dark);
		if (result != SUCCESS) return error(result);
		return success(dark);
	}

	public JetiResult<float[]> calculateSplineDark (int begin, int end, float step) {
		var dark = new float[(int)((end - begin) / step) + 1];
		int result = lib().JETI_CalcSplinDark(handle, begin, end, step, dark);
		if (result != SUCCESS) return error(result);
		return success(dark);
	}

	public JetiResult<float[]> calculateLinearLight (int begin, int end, float step) {
		var light = new float[(int)((end - begin) / step) + 1];
		int result = lib().JETI_CalcLintLight(handle, begin, end, step, light);
		if (result != SUCCESS) return error(result);
		return success(light);
	}

	public JetiResult<float[]> calculateSplineLight (int begin, int end, float step) {
		var light = new float[(int)((end - begin) / step) + 1];
		int result = lib().JETI_CalcSplinLight(handle, begin, end, step, light);
		if (result != SUCCESS) return error(result);
		return success(light);
	}

	public JetiResult<float[]> calculateLinearReference (int begin, int end, float step) {
		var reference = new float[(int)((end - begin) / step) + 1];
		int result = lib().JETI_CalcLintRefer(handle, begin, end, step, reference);
		if (result != SUCCESS) return error(result);
		return success(reference);
	}

	public JetiResult<float[]> calculateSplineReference (int begin, int end, float step) {
		var reference = new float[(int)((end - begin) / step) + 1];
		int result = lib().JETI_CalcSplinRefer(handle, begin, end, step, reference);
		if (result != SUCCESS) return error(result);
		return success(reference);
	}

	public JetiResult<float[]> calculateLinearTransmissionReflection (int begin, int end, float step) {
		var transRefl = new float[(int)((end - begin) / step) + 1];
		int result = lib().JETI_CalcLintTransRefl(handle, begin, end, step, transRefl);
		if (result != SUCCESS) return error(result);
		return success(transRefl);
	}

	public JetiResult<float[]> calculateSplineTransmissionReflection (int begin, int end, float step) {
		var transRefl = new float[(int)((end - begin) / step) + 1];
		int result = lib().JETI_CalcSplinTransRefl(handle, begin, end, step, transRefl);
		if (result != SUCCESS) return error(result);
		return success(transRefl);
	}

	public JetiResult<Float> calculateRadiometricValue (int begin, int end) {
		int result = lib().JETI_CalcRadio(handle, begin, end, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Float> calculatePhotometricValue () {
		int result = lib().JETI_CalcPhoto(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<XY> calculateChromaXY () {
		int result = lib().JETI_CalcChromxy(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new XY(f[0].getValue(), f[1].getValue()));
	}

	public JetiResult<XY10> calculateChromaXY10 () {
		int result = lib().JETI_CalcChromxy10(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new XY10(f[0].getValue(), f[1].getValue()));
	}

	public JetiResult<UV> calculateChromaUV () {
		int result = lib().JETI_CalcChromuv(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new UV(f[0].getValue(), f[1].getValue()));
	}

	public JetiResult<DominantWavelength> calculateDominantWavelength () {
		int result = lib().JETI_CalcDWLPE(handle, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new DominantWavelength(f[0].getValue(), f[1].getValue()));
	}

	public JetiResult<Float> calculateCCT () {
		int result = lib().JETI_CalcCCT(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Float> calculateDuv () {
		int result = lib().JETI_CalcDuv(handle, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<Float> calculateCRI (float cct) {
		int result = lib().JETI_CalcCRI(handle, cct, f[0]);
		if (result != SUCCESS) return error(result);
		return success(f[0].getValue());
	}

	public JetiResult<XYZ> calculateXYZ () {
		int result = lib().JETI_CalcXYZ(handle, f[0], f[1], f[2]);
		if (result != SUCCESS) return error(result);
		return success(new XYZ(f[0].getValue(), f[1].getValue(), f[2].getValue()));
	}

	public JetiResult<AllValues> calculateAllValues (int begin, int end) {
		int result = lib().JETI_CalcAllValue(handle, begin, end, f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7]);
		if (result != SUCCESS) return error(result);
		return success(new AllValues(f[0].getValue(), f[1].getValue(), new XY(f[2].getValue(), f[3].getValue()),
			new UV(f[4].getValue(), f[5].getValue()), new DominantWavelength(f[6].getValue(), f[7].getValue())));
	}

	public JetiResult<TM30> calculateTM30 (boolean useTM3015) {
		var rfi = new double[16];
		var rfces = new double[99];
		int result = lib().JETI_CalcTM30(handle, (byte)(useTM3015 ? 1 : 0), d[0], d[1], d[2], d[3], rfi, rfces);
		if (result != SUCCESS) return error(result);
		return success(new TM30(d[0].getValue(), d[1].getValue(), d[2].getValue(), d[3].getValue(), rfi, rfces));
	}

	public JetiResult<PeakFWHM> calculatePeakFWHM (float threshold) {
		int result = lib().JETI_CalcPeakFWHM(handle, threshold, f[0], f[1]);
		if (result != SUCCESS) return error(result);
		return success(new PeakFWHM(f[0].getValue(), f[1].getValue()));
	}

	public JetiResult<BlueMeasurement> calculateBlueMeasurement () {
		int result = lib().JETI_CalcBlueMeasurement(handle, f[0], f[1], f[2], f[3], f[4], f[5]);
		if (result != SUCCESS) return error(result);
		return success(new BlueMeasurement(f[0].getValue(), f[1].getValue(), f[2].getValue(), f[3].getValue(), f[4].getValue(),
			f[5].getValue()));
	}

	static public JetiResult<Boolean> setLicenseKey (String licenseKey) {
		return result(JetiCoreLibrary.INSTANCE.JETI_SetLicKey(licenseKey));
	}

	static public JetiResult<Boolean> importStraylightMatrix (String matrixFile) {
		return result(JetiCoreLibrary.INSTANCE.JETI_ImportSLM(matrixFile));
	}

	static public JetiResult<Boolean> ignoreStraylightMatrix (boolean ignore) {
		return result(JetiCoreLibrary.INSTANCE.JETI_IgnoreSLM((byte)(ignore ? 1 : 0)));
	}

	static public JetiResult<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetNumDevices(count);
		if (result != SUCCESS) return error(result);
		return success(count.getValue());
	}

	static public JetiResult<DeviceSerials> getDeviceSerials (int deviceNumber) {
		var boardSerial = new byte[STRING_SIZE];
		var specSerial = new byte[STRING_SIZE];
		var deviceSerial = new byte[STRING_SIZE];
		int result = JetiCoreLibrary.INSTANCE.JETI_GetSerialDevice(deviceNumber, boardSerial, specSerial, deviceSerial);
		if (result != SUCCESS) return error(result);
		return success(new DeviceSerials(string(boardSerial), string(specSerial), string(deviceSerial)));
	}

	static public JetiResult<DeviceInfo> getDeviceInfo (int deviceNumber) {
		var connType = new ByteByReference();
		var deviceType = new ByteByReference();
		var deviceSerial = new byte[STRING_SIZE];
		var comPortNr = new ShortByReference();
		var baudrate = new IntByReference();
		var ipAddress = new byte[STRING_SIZE];
		var usbSerial = new byte[STRING_SIZE];
		var btAddress = new LongByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetDeviceInfo(deviceNumber, connType, deviceType, deviceSerial, comPortNr,
			baudrate, ipAddress, usbSerial, btAddress);
		if (result != SUCCESS) return error(result);
		return success(new DeviceInfo(ConnectionType.values[connType.getValue()], DeviceType.values[deviceType.getValue()],
			string(deviceSerial), (int)comPortNr.getValue(), baudrate.getValue(), string(ipAddress), string(usbSerial),
			btAddress.getValue(), null));
	}

	static public JetiResult<DeviceInfo> getDeviceInfoEx (int deviceNumber) {
		var connType = new ByteByReference();
		var deviceType = new ByteByReference();
		var deviceSerial = new byte[STRING_SIZE];
		var comPortNr = new ShortByReference();
		var baudrate = new IntByReference();
		var ipAddress = new byte[STRING_SIZE];
		var usbSerial = new byte[STRING_SIZE];
		var btAddress = new LongByReference();
		var btleDevicePath = new char[256]; // Size?
		int result = JetiCoreLibrary.INSTANCE.JETI_GetDeviceInfoEx(deviceNumber, connType, deviceType, deviceSerial, comPortNr,
			baudrate, ipAddress, usbSerial, btAddress, btleDevicePath);
		if (result != SUCCESS) return error(result);
		return success(new DeviceInfo(ConnectionType.values[connType.getValue()], DeviceType.values[deviceType.getValue()],
			string(deviceSerial), (int)comPortNr.getValue(), baudrate.getValue(), string(ipAddress), string(usbSerial),
			btAddress.getValue(), new String(btleDevicePath).trim()));
	}

	static public JetiResult<DeviceInfo[]> getAllDeviceInfo () {
		JetiResult<Integer> countResult = getDeviceCount();
		if (countResult.isError()) return error(countResult.getErrorCode());
		int count = countResult.getValue();
		var devices = new ArrayList<DeviceInfo>(count);
		for (int i = 0; i < count; i++) {
			JetiResult<DeviceInfo> deviceResult = getDeviceInfo(i);
			if (deviceResult.isSuccess()) devices.add(deviceResult.getValue());
		}
		return success(devices.toArray(DeviceInfo[]::new));
	}

	static public JetiResult<JetiCore> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenDevice(deviceNumber, handle);
		if (result != SUCCESS) return error(result);
		return success(new JetiCore(handle.getValue()));
	}

	static public JetiResult<JetiCore> openComDevice (int comPort, int baudrate) {
		var handle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenCOMDevice(comPort, baudrate, handle);
		if (result != SUCCESS) return error(result);
		return success(new JetiCore(handle.getValue()));
	}

	static public JetiResult<JetiCore> openTcpDevice (String ipAddress) {
		var handle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenTCPDevice(ipAddress, handle);
		if (result != SUCCESS) return error(result);
		return success(new JetiCore(handle.getValue()));
	}

	static public JetiResult<JetiCore> openUsbDevice (String usbSerial) {
		var handle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenFTDIDevice(usbSerial, handle);
		if (result != SUCCESS) return error(result);
		return success(new JetiCore(handle.getValue()));
	}

	static public JetiResult<JetiCore> openBluetoothDevice (long btAddress) {
		var handle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenBTDevice(btAddress, handle);
		if (result != SUCCESS) return error(result);
		return success(new JetiCore(handle.getValue()));
	}

	static public JetiResult<JetiCore> openBluetoothLeDevice (String devicePath) {
		var handle = new PointerByReference();
		char[] pathChars = devicePath.toCharArray();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenBTLEDevice(pathChars, handle);
		if (result != SUCCESS) return error(result);
		return success(new JetiCore(handle.getValue()));
	}

	static public JetiResult<DllVersion> getDllVersion () {
		var major = new ShortByReference();
		var minor = new ShortByReference();
		var build = new ShortByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetCoreDLLVersion(major, minor, build);
		if (result != SUCCESS) return error(result);
		return success(new DllVersion(major.getValue(), minor.getValue(), build.getValue()));
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
		Integer comPortNumber,
		Integer baudrate,
		String ipAddress,
		String usbSerial,
		Long bluetoothAddress,
		String bluetoothLeDevicePath) {}

	public record BatteryInfo (float voltage, int percent, boolean charging) {}

	public record DeviceRead (byte[] data, int bytesRead) {}

	public record CalibrationData (
		String mode,
		String remark,
		int begin,
		int end,
		int step,
		int integrationTime,
		double[] values) {}

	public record WavelengthRange (int begin, int end, int step) {}

	public record Border (byte min, byte max) {}

	public record CorrectionRange (int start, int end) {}

	public record FlashSettings (float interval, float pulseLength) {}

	public record FlickerFrequency (float frequency, int warning) {}

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
