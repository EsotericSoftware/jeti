
package com.esotericsoftware.jeti;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;
import com.sun.jna.win32.StdCallLibrary;

/** @author Nathan Sweet <misc@n4te.com> */
public interface SpectroExLibrary extends StdCallLibrary {
	final SpectroExLibrary INSTANCE = Native.load("jeti_spectro_ex64", SpectroExLibrary.class);

	// Device discovery and connection

	int JETI_GetNumSpectroEx (IntByReference dwNumDevices);

	int JETI_GetSerialSpectroEx (int dwDeviceNum, byte[] cBoardSerialNr, byte[] cSpecSerialNr, byte[] cDeviceSerialNr);

	int JETI_OpenSpectroEx (int dwDeviceNum, PointerByReference dwDevice);

	int JETI_CloseSpectroEx (Pointer dwDevice);

	// Dark measurement functions

	int JETI_StartDarkEx (Pointer dwDevice, float fTint, short wAver);

	int JETI_DarkPixEx (Pointer dwDevice, int[] iDark);

	int JETI_DarkWaveEx (Pointer dwDevice, int dwBeg, int dwEnd, float fStep, float[] fDark);

	// Light measurement functions

	int JETI_StartLightEx (Pointer dwDevice, float fTint, short wAver);

	int JETI_PrepareLightEx (Pointer dwDevice, float fTint, short wAver);

	int JETI_LightPixEx (Pointer dwDevice, int[] iLight);

	int JETI_LightWaveEx (Pointer dwDevice, int dwBeg, int dwEnd, float fStep, float[] fLight);

	// Reference measurement functions

	int JETI_StartReferEx (Pointer dwDevice, float fTint, short wAver);

	int JETI_PrepareReferEx (Pointer dwDevice, float fTint, short wAver);

	int JETI_ReferPixEx (Pointer dwDevice, int[] iRefer);

	int JETI_ReferWaveEx (Pointer dwDevice, int dwBeg, int dwEnd, float fStep, float[] fRefer);

	// Transmission/Reflection measurement functions

	int JETI_StartTransReflEx (Pointer dwDevice, float fTint, short wAver);

	int JETI_PrepareTransReflEx (Pointer dwDevice, float fTint, short wAver);

	int JETI_TransReflPixEx (Pointer dwDevice, int[] iTransRefl);

	int JETI_TransReflWaveEx (Pointer dwDevice, int dwBeg, int dwEnd, float fStep, float[] fTransRefl);

	// Image measurement functions

	int JETI_StartDarkImageEx (Pointer dwDevice, float fTint);

	int JETI_DarkImageEx (Pointer dwDevice, short[] wDarkImage);

	int JETI_StartLightImageEx (Pointer dwDevice, float fTint);

	int JETI_LightImageEx (Pointer dwDevice, short[] wLightImage);

	// Channel measurement functions

	int JETI_StartChannelDarkEx (Pointer dwDevice, float fTint, short wAver);

	int JETI_ChannelDarkEx (Pointer dwDevice, short[] wDark);

	int JETI_StartChannelLightEx (Pointer dwDevice, float fTint, short wAver);

	int JETI_ChannelLightEx (Pointer dwDevice, short[] wLight);

	// Continuous measurement functions

	int JETI_StartContDarkEx (Pointer dwDevice, float fInterval, int dwCount);

	int JETI_ContDarkEx (Pointer dwDevice, short[] wDark);

	int JETI_StartContLightEx (Pointer dwDevice, float fInterval, int dwCount);

	int JETI_ContLightEx (Pointer dwDevice, short[] wLight);

	int JETI_StartContChannelDarkEx (Pointer dwDevice, float fInterval, int dwCount);

	int JETI_ContChannelDarkEx (Pointer dwDevice, short[] wDark);

	int JETI_StartContChannelLightEx (Pointer dwDevice, float fInterval, int dwCount);

	int JETI_ContChannelLightEx (Pointer dwDevice, short[] wLight);

	// Device status and control

	int JETI_SpectroStatusEx (Pointer dwDevice, IntByReference boIsBusy);

	int JETI_SpectroBreakEx (Pointer dwDevice);

	// Device parameters

	int JETI_PixelCountEx (Pointer dwDevice, IntByReference dwPixel);

	int JETI_SpectroTintEx (Pointer dwDevice, FloatByReference fTint);

	// Version information

	int JETI_GetSpectroExDLLVersion (ShortByReference wMajorVersion, ShortByReference wMinorVersion,
		ShortByReference wBuildNumber);
}
