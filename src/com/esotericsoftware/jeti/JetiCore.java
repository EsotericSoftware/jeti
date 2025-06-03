
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiResult.*;
import static com.esotericsoftware.jeti.JetiSDK.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiCore extends Device<JetiCoreLibrary> {
	private final ByteByReference byteRef1 = new ByteByReference(), byteRef2 = new ByteByReference();

	private JetiCore (Pointer handle) {
		super(JetiCoreLibrary.INSTANCE, handle);
	}

	public JetiResult<Boolean> reset () {
		return result(lib().JETI_Reset(handle));
	}

	public JetiResult<Boolean> hardReset () {
		return result(lib().JETI_HardReset(handle));
	}

	public JetiResult<Boolean> breakMeasurement () {
		return result(lib().JETI_Break(handle));
	}

	public JetiResult<Boolean> measure () {
		return result(lib().JETI_InitMeasure(handle));
	}

	public JetiResult<Boolean> prepareMeasurement () {
		return result(lib().JETI_PreTrigMeasure(handle));
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		int result = lib().JETI_MeasureStatusCore(handle, intRef);
		if (result != SUCCESS) return error(result);
		return success(intRef.getValue() != 0);
	}

	public JetiResult<String> getFirmwareVersion () {
		var versionBuffer = new byte[256];
		int result = lib().JETI_GetFirmwareVersion(handle, versionBuffer);
		if (result != SUCCESS) return error(result);
		return success(new String(versionBuffer, StandardCharsets.UTF_8).trim());
	}

	public JetiResult<DeviceType> getDeviceType () {
		int result = lib().JETI_GetDeviceType(handle, byteRef1);
		if (result != SUCCESS) return error(result);
		return success(DeviceType.values[byteRef1.getValue()]);
	}

	public JetiResult<BatteryInfo> getBatteryInfo () {
		int result = lib().JETI_GetBatteryStat(handle, floatRef, byteRef1, byteRef2);
		if (result != SUCCESS) return error(result);
		return success(new BatteryInfo(floatRef.getValue(), byteRef1.getValue(), byteRef2.getValue() != 0));
	}

	public JetiResult<Float> getIntegrationTime () {
		int result = lib().JETI_GetTint(handle, floatRef);
		if (result != SUCCESS) return error(result);
		return success(floatRef.getValue());
	}

	public JetiResult<Integer> getPixelCount () {
		int result = lib().JETI_GetPixel(handle, intRef);
		if (result != SUCCESS) return error(result);
		return success(intRef.getValue());
	}

	public JetiResult<Float> getTemperature () {
		int result = lib().JETI_GetTemperature(handle, floatRef);
		if (result != SUCCESS) return error(result);
		return success(floatRef.getValue());
	}

	public JetiResult<String> sendCommand (String command) {
		var answer = new byte[1024]; // Enough?
		int result = lib().JETI_ArbitraryCommand(handle, command, answer);
		if (result != SUCCESS) return error(result);
		return success(new String(answer, StandardCharsets.UTF_8).trim());
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
			btAddress.getValue()));
	}

	static public JetiResult<DeviceInfo[]> getDeviceInfo () {
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

	static public enum ConnectionType {
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
		Long bluetoothAddress) {}

	public record BatteryInfo (float voltage, int percent, boolean charging) {}
}
