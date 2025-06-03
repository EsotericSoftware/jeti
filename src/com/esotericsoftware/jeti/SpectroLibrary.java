
package com.esotericsoftware.jeti;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public interface SpectroLibrary extends Library {
	final SpectroLibrary INSTANCE = Native.load("jeti_spectro64", SpectroLibrary.class);

	// Device discovery and connection
	int JETI_GetNumSpectro (IntByReference dwNumDevices);

	int JETI_GetSerialSpectro (int dwDeviceNum, byte[] cBoardSerialNr, byte[] cSpecSerialNr, byte[] cDeviceSerialNr);

	int JETI_OpenSpectro (int dwDeviceNum, PointerByReference dwDevice);

	int JETI_CloseSpectro (Pointer dwDevice);

	// Spectroscopic measurements
	int JETI_DarkSpec (Pointer dwDevice, float fTint, float[] fDark);

	int JETI_LightSpec (Pointer dwDevice, float fTint, float[] fLight);

	int JETI_ReferSpec (Pointer dwDevice, float fTint, float[] fRefer);

	int JETI_TransReflSpec (Pointer dwDevice, float fTint, float[] fTransRefl);

	// Configuration
	int JETI_SpectroTint (Pointer dwDevice, FloatByReference fTint);

	// Version information
	int JETI_GetSpectroDLLVersion (ShortByReference wMajorVersion, ShortByReference wMinorVersion, ShortByReference wBuildNumber);
}
