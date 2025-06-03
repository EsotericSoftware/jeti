
package com.esotericsoftware.jeti;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public interface RadioExLibrary extends Library {
	final RadioExLibrary INSTANCE = Native.load("jeti_radio_ex64", RadioExLibrary.class);

	// Device discovery and connection
	int JETI_GetNumRadioEx (IntByReference dwNumDevices);

	int JETI_GetSerialRadioEx (int dwDeviceNum, byte[] cBoardSerialNr, byte[] cSpecSerialNr, byte[] cDeviceSerialNr);

	int JETI_OpenRadioEx (int dwDeviceNum, PointerByReference dwDevice);

	int JETI_CloseRadioEx (Pointer dwDevice);

	// Measurement functions
	int JETI_MeasureEx (Pointer dwDevice, float fTint, short wAver, int dwStep);

	int JETI_MeasureAdaptEx (Pointer dwDevice, short wAver, int dwStep);

	int JETI_PrepareMeasureEx (Pointer dwDevice, float fTint, short wAver, int dwStep);

	int JETI_MeasureStatusEx (Pointer dwDevice, IntByReference boStatus);

	int JETI_MeasureAdaptStatusEx (Pointer dwDevice, FloatByReference fTint, ShortByReference wAverage, IntByReference boStatus);

	int JETI_MeasureBreakEx (Pointer dwDevice);

	// Spectral data functions
	int JETI_SpecRadEx (Pointer dwDevice, int dwBeg, int dwEnd, float[] fSprad);

	int JETI_SpecRadHiResEx (Pointer dwDevice, int dwBeg, int dwEnd, float[] fSprad);

	int JETI_SaveSpecRadSPCEx (Pointer dwDevice, int dwBeg, int dwEnd, String cPathName, String cOperator, String cMemo);

	int JETI_SaveSpecRadCSVEx (Pointer dwDevice, int dwBeg, int dwEnd, String cPathName, String cOperator, String cMemo);

	// Measurement data functions
	int JETI_RadioEx (Pointer dwDevice, int dwBeg, int dwEnd, FloatByReference fRadio);

	int JETI_PhotoEx (Pointer dwDevice, FloatByReference fPhoto);

	int JETI_ChromxyEx (Pointer dwDevice, FloatByReference fChromx, FloatByReference fChromy);

	int JETI_Chromxy10Ex (Pointer dwDevice, FloatByReference fChromx10, FloatByReference fChromy10);

	int JETI_ChromuvEx (Pointer dwDevice, FloatByReference fChromu, FloatByReference fChromv);

	int JETI_ChromXYZEx (Pointer dwDevice, FloatByReference fX, FloatByReference fY, FloatByReference fZ);

	int JETI_DWLPEEx (Pointer dwDevice, FloatByReference fDWL, FloatByReference fPE);

	int JETI_CCTEx (Pointer dwDevice, FloatByReference fCCT);

	int JETI_DuvEx (Pointer dwDevice, FloatByReference fDuv);

	int JETI_CRIEx (Pointer dwDevice, float fCCT, float[] fCRI);

	int JETI_TM30Ex (Pointer dwDevice, byte bUseTM3015, DoubleByReference dRf, DoubleByReference dRg, DoubleByReference dChroma,
		DoubleByReference dHue, double[] dRfi, double[] dRfces);

	int JETI_PeakFWHMEx (Pointer dwDevice, float fThreshold, FloatByReference fPeak, FloatByReference fFWHM);

	int JETI_BlueMeasurementEx (Pointer dwDevice, FloatByReference fLb, FloatByReference fKbv, FloatByReference fKc,
		FloatByReference fRbpfs, FloatByReference fRlbtb, FloatByReference fRnbpbp);

	// Device parameter functions
	int JETI_RadioTintEx (Pointer dwDevice, FloatByReference fTint);

	int JETI_SetMeasDistEx (Pointer dwDevice, int dwDistance);

	int JETI_GetMeasDistEx (Pointer dwDevice, IntByReference dwDistance);

	// Version information
	int JETI_GetRadioExDLLVersion (ShortByReference wMajorVersion, ShortByReference wMinorVersion, ShortByReference wBuildNumber);
}
