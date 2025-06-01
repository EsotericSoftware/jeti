
package com.esotericsoftware.jeti;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public interface JetiCoreLibrary extends Library {
	final JetiCoreLibrary INSTANCE = Native.load("jeti_core64", JetiCoreLibrary.class);

	// License and setup
	int JETI_SetLicKey (String cLicKey);

	int JETI_ImportSLM (String cMatrixFile);

	int JETI_IgnoreSLM (byte bIgnore);

	// Device discovery and connection
	int JETI_GetNumDevices (IntByReference dwNumDevices);

	int JETI_GetSerialDevice (int dwDeviceNum, byte[] cBoardSerialNr, byte[] cSpecSerialNr, byte[] cDeviceSerialNr);

	int JETI_GetDeviceInfo (int dwDeviceNum, ByteByReference bConnType, ByteByReference bDeviceType, byte[] cDeviceSerial,
		ShortByReference wComPortNr, IntByReference dwBaudrate, byte[] cIPAddress, byte[] cUSBSerial, LongByReference btAddress);

	int JETI_GetDeviceInfoEx (int dwDeviceNum, ByteByReference bConnType, ByteByReference bDeviceType, byte[] cDeviceSerial,
		ShortByReference wComPortNr, IntByReference dwBaudrate, byte[] cIPAddress, byte[] cUSBSerial, LongByReference btAddress,
		char[] wcBTLEDevicePath);

	// Device connection methods
	int JETI_OpenDevice (int dwDeviceNum, PointerByReference dwDevice);

	int JETI_OpenCOMDevice (int dwComPort, int dwBaudrate, PointerByReference dwDevice);

	int JETI_OpenTCPDevice (String cIPAddr, PointerByReference dwDevice);

	int JETI_OpenFTDIDevice (String cUSBSerial, PointerByReference dwDevice);

	int JETI_OpenBTDevice (long btAddress, PointerByReference dwDevice);

	int JETI_OpenBTLEDevice (char[] wcBTLEDevicePath, PointerByReference dwDevice);

	int JETI_CloseDevice (Pointer dwDevice);

	// Device communication
	int JETI_ArbitraryCommand (Pointer dwDevice, String cCommand, byte[] cAnswer);

	int JETI_DeviceWrite (Pointer dwDevice, String cCommand, int dwBytesToWrite, int dwTimeout);

	int JETI_DeviceRead (Pointer dwDevice, byte[] cResponse, int dwBytesToRead, IntByReference dwBytesRead, int dwTimeout);

	int JETI_DeviceReadTerm (Pointer dwDevice, byte[] cResponse, int dwMaxBytes, int dwTimeout);

	int JETI_DataReceived (Pointer dwDevice, int iMaxLength);

	// Device control
	int JETI_Reset (Pointer dwDevice);

	int JETI_HardReset (Pointer dwDevice);

	int JETI_Break (Pointer dwDevice);

	int JETI_InitMeasure (Pointer dwDevice);

	int JETI_PreTrigMeasure (Pointer dwDevice);

	int JETI_MeasureStatusCore (Pointer dwDevice, IntByReference boStatus);

	int JETI_WaitReadTrigger (Pointer dwDevice, IntByReference iSpec, int dwTimeout);

	// Calibration
	int JETI_StartAdaption (Pointer dwDevice, int boReference);

	int JETI_CheckAdaptionStat (Pointer dwDevice, FloatByReference fTint, ShortByReference wAverage, IntByReference boStatus);

	int JETI_ReadCalib (Pointer dwDevice, int dwCalibNr, byte[] cMode, byte[] cRemark, IntByReference dwBegin,
		IntByReference dwEnd, IntByReference dwStep, IntByReference dwTint, double[] dValue);

	int JETI_WriteCalib (Pointer dwDevice, int dwCalibNr, String cMode, String cRemark, int dwBegin, int dwEnd, int dwStep,
		int dwTint, double[] dValue);

	int JETI_DeleteCalib (Pointer dwDevice, int dwCalibNr);

	int JETI_GetCalibRange (Pointer dwDevice, IntByReference dwBegin, IntByReference dwEnd, IntByReference dwStep);

	int JETI_SetCalib (Pointer dwDevice, byte bCalibNr);

	int JETI_GetCalib (Pointer dwDevice, ByteByReference bCalibNr);

	int JETI_MeasCompDark (Pointer dwDevice);

	// Device information
	int JETI_GetComPortHandle (Pointer dwDevice, PointerByReference hComPortHandle);

	int JETI_MeasureADC1 (Pointer dwDevice, ShortByReference wADC1);

	int JETI_MeasureADC2 (Pointer dwDevice, ShortByReference wADC2);

	int JETI_ReadUserData64 (Pointer dwDevice, byte[] bData, int dwStart, int dwEnd);

	int JETI_WriteUserData64 (Pointer dwDevice, byte[] bData, int dwBlock);

	int JETI_GetCoreDLLVersion (ShortByReference wMajorVersion, ShortByReference wMinorVersion, ShortByReference wBuildNumber);

	int JETI_GetFirmwareVersion (Pointer dwDevice, byte[] cVersionString);

	int JETI_GetDeviceType (Pointer dwDevice, ByteByReference bDeviceType);

	int JETI_GetBatteryStat (Pointer dwDevice, FloatByReference fBattVolt, ByteByReference bBattPercent,
		ByteByReference bIsBattLoading);

	int JETI_GetLastError (Pointer dwDevice, IntByReference dwErrorCode);

	int JETI_GetEnquiry (Pointer dwDevice, IntByReference dwEnquiry);

	int JETI_SetCallbackFunction (Pointer dwDevice, byte bEventChar, WinDef.HWND hMainWnd);

	// Parameter functions
	int JETI_GetPixel (Pointer dwDevice, IntByReference dwPixel);

	int JETI_GetPixelBinning (Pointer dwDevice, ByteByReference bPixBin);

	int JETI_GetFit (Pointer dwDevice, FloatByReference fFit);

	int JETI_GetSDelay (Pointer dwDevice, IntByReference dwSDelay);

	int JETI_SetSDelay (Pointer dwDevice, int dwSDelay);

	int JETI_GetTint (Pointer dwDevice, FloatByReference fTint);

	int JETI_GetADCRes (Pointer dwDevice, ByteByReference bADCRes);

	int JETI_GetSplitTime (Pointer dwDevice, IntByReference dwSplitTime);

	int JETI_GetBorder (Pointer dwDevice, ByteByReference bBorderMin, ByteByReference bBorderMax);

	int JETI_GetDistance (Pointer dwDevice, IntByReference dwDistance);

	int JETI_SetDistance (Pointer dwDevice, int dwDistance);

	int JETI_GetParamBlock (Pointer dwDevice, byte[] bParam);

	int JETI_SetParamBlock (Pointer dwDevice, byte[] bParam);

	int JETI_GetOptTrigg (Pointer dwDevice, IntByReference boOptTrigg);

	int JETI_SetLaserIntensity (Pointer dwDevice, int dwIntensity, int dwModulation);

	int JETI_SetTrigger (Pointer dwDevice, int dwTriggerMode);

	int JETI_GetTrigTimeout (Pointer dwDevice, ShortByReference wTimeout);

	int JETI_SetTrigTimeout (Pointer dwDevice, short wTimeout);

	int JETI_SetFlashMode (Pointer dwDevice, int boFlashMode);

	int JETI_SetFlashCycle (Pointer dwDevice, int dwFlashCycle);

	int JETI_GetCorrectionStat (Pointer dwDevice, IntByReference boCorrStat);

	int JETI_SetCorrectionStat (Pointer dwDevice, int boCorrStat);

	int JETI_GetCorrectionRange (Pointer dwDevice, IntByReference dwCorrStart, IntByReference dwCorrEnd);

	int JETI_SetCorrectionRange (Pointer dwDevice, int dwCorrStart, int dwCorrEnd);

	int JETI_GetOffsetCorrRange (Pointer dwDevice, IntByReference dwCorrStart, IntByReference dwCorrEnd);

	int JETI_SetOffsetCorrRange (Pointer dwDevice, int dwCorrStart, int dwCorrEnd);

	int JETI_GetCorrectionCoeff (Pointer dwDevice, float[] fCorrCoeff);

	int JETI_SetCorrectionCoeff (Pointer dwDevice, float[] fCorrCoeff);

	int JETI_GetCutoffStat (Pointer dwDevice, IntByReference boCutoffStat);

	int JETI_SetCutoffStat (Pointer dwDevice, int boCutoffStat);

	int JETI_GetBaudrate (Pointer dwDevice, IntByReference dwBaudrate);

	int JETI_GetSLMEnable (Pointer dwDevice, ByteByReference bSLMEnable);

	int JETI_SetSLMEnable (Pointer dwDevice, byte bSLMEnable);

	int JETI_SetChannelConf (Pointer dwDevice, String cChannelConf);

	int JETI_GetChannelConf (Pointer dwDevice, byte[] cChannelConf);

	int JETI_SetLampMode (Pointer dwDevice, byte bLampMode);

	int JETI_GetLampMode (Pointer dwDevice, ByteByReference bLampMode);

	int JETI_SetFlash (Pointer dwDevice, float fInterval, float fPulselen);

	int JETI_GetFlash (Pointer dwDevice, FloatByReference fInterval, FloatByReference fPulselen);

	// Control functions
	int JETI_GetLaserStat (Pointer dwDevice, IntByReference boLaserStat);

	int JETI_SetLaserStat (Pointer dwDevice, int boLaserStat);

	int JETI_GetShutterStat (Pointer dwDevice, IntByReference boShutterStat);

	int JETI_SetShutterStat (Pointer dwDevice, int boShutterStat);

	int JETI_GetMeasHead (Pointer dwDevice, ByteByReference bMeasHead);

	int JETI_GetAux1Stat (Pointer dwDevice, IntByReference boAuxStat);

	int JETI_SetAux1Stat (Pointer dwDevice, int boAuxStat);

	int JETI_GetAux2Stat (Pointer dwDevice, IntByReference boAuxStat);

	int JETI_SetAux2Stat (Pointer dwDevice, int boAuxStat);

	int JETI_AuxOut1 (Pointer dwDevice, int boAux1);

	int JETI_AuxOut1Stat (Pointer dwDevice, IntByReference boAux1Stat);

	int JETI_AuxOut2 (Pointer dwDevice, int boAux2);

	int JETI_AuxOut2Stat (Pointer dwDevice, IntByReference boAux2Stat);

	int JETI_AuxOut3 (Pointer dwDevice, int boAux3);

	int JETI_AuxOut3Stat (Pointer dwDevice, IntByReference boAux3Stat);

	int JETI_AuxOut4 (Pointer dwDevice, int boAux4);

	int JETI_AuxOut4Stat (Pointer dwDevice, IntByReference boAux4Stat);

	int JETI_AuxOut5 (Pointer dwDevice, int boAux5);

	int JETI_AuxOut5Stat (Pointer dwDevice, IntByReference boAux5Stat);

	int JETI_AuxIn1Stat (Pointer dwDevice, IntByReference boAuxIn1Stat);

	int JETI_AuxIn2Stat (Pointer dwDevice, IntByReference boAuxIn2Stat);

	int JETI_GetFlickerFreq (Pointer dwDevice, FloatByReference fFlickerFreq, IntByReference dwWarning);

	int JETI_SetSyncFreq (Pointer dwDevice, float fSyncFreq);

	int JETI_GetSyncFreq (Pointer dwDevice, FloatByReference fSyncFreq);

	int JETI_SetSyncMode (Pointer dwDevice, byte bSyncMode);

	int JETI_GetSyncMode (Pointer dwDevice, ByteByReference bSyncMode);

	int JETI_GetDIOIn (Pointer dwDevice, ByteByReference bDIOIn);

	int JETI_GetDIOOut (Pointer dwDevice, ByteByReference bDIOOut);

	int JETI_SetDIOOut (Pointer dwDevice, byte bDIOOut);

	int JETI_SetDIOOutPin (Pointer dwDevice, byte bPinNr, int boDIOOut);

	int JETI_GetTemperature (Pointer dwDevice, FloatByReference fTemperature);

	// Configuration functions
	int JETI_GetDarkmodeConf (Pointer dwDevice, ByteByReference bDarkmode);

	int JETI_SetDarkmodeConf (Pointer dwDevice, byte bDarkmode);

	int JETI_GetExposureConf (Pointer dwDevice, ByteByReference bExpmode);

	int JETI_SetExposureConf (Pointer dwDevice, byte bExpmode);

	int JETI_GetFunctionConf (Pointer dwDevice, ByteByReference bPrevFunc, ByteByReference bConfFunc);

	int JETI_SetFunctionConf (Pointer dwDevice, byte bFunction);

	int JETI_GetFormatConf (Pointer dwDevice, ByteByReference bPrevForm, ByteByReference bConfForm);

	int JETI_SetFormatConf (Pointer dwDevice, byte bFormat);

	int JETI_GetTintConf (Pointer dwDevice, FloatByReference fPrevTint, FloatByReference fConfTint);

	int JETI_SetTintConf (Pointer dwDevice, float fTint);

	int JETI_GetMaxTintConf (Pointer dwDevice, FloatByReference fMaxTint);

	int JETI_SetMaxTintConf (Pointer dwDevice, float fMaxTint);

	int JETI_GetMaxAverConf (Pointer dwDevice, ShortByReference wMaxAver);

	int JETI_SetMaxAverConf (Pointer dwDevice, short wMaxAver);

	int JETI_GetMinTintConf (Pointer dwDevice, FloatByReference fMinTint);

	int JETI_GetImageMinTintConf (Pointer dwDevice, FloatByReference fMinTint);

	int JETI_GetChanMinTintConf (Pointer dwDevice, FloatByReference fMinTint);

	int JETI_GetContMinTintConf (Pointer dwDevice, FloatByReference fMinTint);

	int JETI_GetContChanMinTintConf (Pointer dwDevice, FloatByReference fMinTint);

	int JETI_GetAverConf (Pointer dwDevice, ShortByReference wPrevAver, ShortByReference wConfAver);

	int JETI_SetAverConf (Pointer dwDevice, short wAver);

	int JETI_GetAdaptConf (Pointer dwDevice, ByteByReference bAdaptmode);

	int JETI_SetAdaptConf (Pointer dwDevice, byte bAdaptmode);

	int JETI_GetWranConf (Pointer dwDevice, IntByReference dwBeg, IntByReference dwEnd, IntByReference dwStep);

	int JETI_SetWranConf (Pointer dwDevice, int dwBeg, int dwEnd, int dwStep);

	int JETI_GetPDARowConf (Pointer dwDevice, IntByReference dwPDARow, IntByReference dwRowNumber);

	int JETI_SetPDARowConf (Pointer dwDevice, int dwPDARow, int dwRowNumber);

	int JETI_SetDefault (Pointer dwDevice);

	int JETI_GetLevel (Pointer dwDevice, IntByReference dwLevelCounts, IntByReference dwLevelPercent);

	// Fetch functions
	int JETI_FetchDark (Pointer dwDevice, int[] iDark);

	int JETI_FetchLight (Pointer dwDevice, int[] iLight);

	int JETI_FetchRefer (Pointer dwDevice, int[] iRefer);

	int JETI_FetchTransRefl (Pointer dwDevice, int[] iTransRefl);

	int JETI_FetchSprad (Pointer dwDevice, float[] fSprad);

	int JETI_FetchSpradHiRes (Pointer dwDevice, float[] fSprad);

	int JETI_FetchRadio (Pointer dwDevice, FloatByReference fRadio);

	int JETI_FetchPhoto (Pointer dwDevice, FloatByReference fPhoto);

	int JETI_FetchChromxy (Pointer dwDevice, FloatByReference fChromx, FloatByReference fChromy);

	int JETI_FetchChromuv (Pointer dwDevice, FloatByReference fChromu, FloatByReference fChromv);

	int JETI_FetchDWLPE (Pointer dwDevice, FloatByReference fDWL, FloatByReference fPE);

	int JETI_FetchCCT (Pointer dwDevice, FloatByReference fCCT);

	int JETI_FetchDuv (Pointer dwDevice, FloatByReference fDuv);

	int JETI_FetchCRI (Pointer dwDevice, FloatByReference fCRI);

	int JETI_FetchXYZ (Pointer dwDevice, FloatByReference fX, FloatByReference fY, FloatByReference fZ);

	int JETI_FetchTiAdapt (Pointer dwDevice, FloatByReference fTiAdapt);

	int JETI_FetchAverAdapt (Pointer dwDevice, ShortByReference wAverAdapt);

	// Calculation functions
	int JETI_CalcLintDark (Pointer dwDevice, int dwBeg, int dwEnd, float fStep, float[] fDark);

	int JETI_CalcSplinDark (Pointer dwDevice, int dwBeg, int dwEnd, float fStep, float[] fDark);

	int JETI_CalcLintLight (Pointer dwDevice, int dwBeg, int dwEnd, float fStep, float[] fLight);

	int JETI_CalcSplinLight (Pointer dwDevice, int dwBeg, int dwEnd, float fStep, float[] fLight);

	int JETI_CalcLintRefer (Pointer dwDevice, int dwBeg, int dwEnd, float fStep, float[] fRefer);

	int JETI_CalcSplinRefer (Pointer dwDevice, int dwBeg, int dwEnd, float fStep, float[] fRefer);

	int JETI_CalcLintTransRefl (Pointer dwDevice, int dwBeg, int dwEnd, float fStep, float[] fTransRefl);

	int JETI_CalcSplinTransRefl (Pointer dwDevice, int dwBeg, int dwEnd, float fStep, float[] fTransRefl);

	int JETI_CalcRadio (Pointer dwDevice, int dwBeg, int dwEnd, FloatByReference fRadio);

	int JETI_CalcPhoto (Pointer dwDevice, FloatByReference fPhoto);

	int JETI_CalcChromxy (Pointer dwDevice, FloatByReference fChromx, FloatByReference fChromy);

	int JETI_CalcChromxy10 (Pointer dwDevice, FloatByReference fChromx, FloatByReference fChromy);

	int JETI_CalcChromuv (Pointer dwDevice, FloatByReference fChromu, FloatByReference fChromv);

	int JETI_CalcDWLPE (Pointer dwDevice, FloatByReference fDWL, FloatByReference fPE);

	int JETI_CalcCCT (Pointer dwDevice, FloatByReference fCCT);

	int JETI_CalcDuv (Pointer dwDevice, FloatByReference fDuv);

	int JETI_CalcCRI (Pointer dwDevice, float fCCT, FloatByReference fCRI);

	int JETI_CalcXYZ (Pointer dwDevice, FloatByReference fX, FloatByReference fY, FloatByReference fZ);

	int JETI_CalcAllValue (Pointer dwDevice, int dwBeg, int dwEnd, FloatByReference fRadio, FloatByReference fPhoto,
		FloatByReference fChromx, FloatByReference fChromy, FloatByReference fChromu, FloatByReference fChromv,
		FloatByReference fDWL, FloatByReference fPE);

	int JETI_CalcTM30 (Pointer dwDevice, byte bUseTM3015, DoubleByReference dRf, DoubleByReference dRg, DoubleByReference dChroma,
		DoubleByReference dHue, double[] dRfi, double[] dRfces);

	int JETI_CalcPeakFWHM (Pointer dwDevice, float fThreshold, FloatByReference fPeak, FloatByReference fFWHM);

	int JETI_CalcBlueMeasurement (Pointer dwDevice, FloatByReference fLb, FloatByReference fKbv, FloatByReference fKc,
		FloatByReference fRbpfs, FloatByReference fRlbtb, FloatByReference fRnbpbp);
}
