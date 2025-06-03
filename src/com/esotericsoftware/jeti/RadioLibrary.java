
package com.esotericsoftware.jeti;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public interface RadioLibrary extends Library {
	final RadioLibrary INSTANCE = Native.load("jeti_radio64", RadioLibrary.class);

	// Device discovery and connection
	int JETI_GetNumRadio (IntByReference dwNumDevices);

	int JETI_GetSerialRadio (int dwDeviceNum, byte[] cBoardSerialNr, byte[] cSpecSerialNr, byte[] cDeviceSerialNr);

	int JETI_OpenRadio (int dwDeviceNum, PointerByReference dwDevice);

	int JETI_CloseRadio (Pointer dwDevice);

	// Measurement functions
	int JETI_Measure (Pointer dwDevice);

	int JETI_MeasureAdapt (Pointer dwDevice);

	int JETI_PrepareMeasure (Pointer dwDevice);

	int JETI_MeasureStatus (Pointer dwDevice, IntByReference boStatus);

	int JETI_MeasureAdaptStatus (Pointer dwDevice, FloatByReference fTint, ShortByReference wAverage, IntByReference boStatus);

	int JETI_MeasureBreak (Pointer dwDevice);

	// Measurement results
	int JETI_SpecRad (Pointer dwDevice, float[] fSprad);

	int JETI_Radio (Pointer dwDevice, FloatByReference fRadio);

	int JETI_Photo (Pointer dwDevice, FloatByReference fPhoto);

	int JETI_Chromxy (Pointer dwDevice, FloatByReference fChromx, FloatByReference fChromy);

	int JETI_Chromxy10 (Pointer dwDevice, FloatByReference fChromx10, FloatByReference fChromy10);

	int JETI_Chromuv (Pointer dwDevice, FloatByReference fChromu, FloatByReference fChromv);

	int JETI_ChromXYZ (Pointer dwDevice, FloatByReference fX, FloatByReference fY, FloatByReference fZ);

	int JETI_DWLPE (Pointer dwDevice, FloatByReference fDWL, FloatByReference fPE);

	int JETI_CCT (Pointer dwDevice, FloatByReference fCCT);

	int JETI_Duv (Pointer dwDevice, FloatByReference fDuv);

	int JETI_CRI (Pointer dwDevice, float[] fCRI);

	int JETI_RadioTint (Pointer dwDevice, FloatByReference fTint);

	// Configuration
	int JETI_SetMeasDist (Pointer dwDevice, int dwDistance);

	int JETI_GetMeasDist (Pointer dwDevice, IntByReference dwDistance);

	// Version information
	int JETI_GetRadioDLLVersion (ShortByReference wMajorVersion, ShortByReference wMinorVersion, ShortByReference wBuildNumber);
}
