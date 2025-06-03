
package com.esotericsoftware.jeti;

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
public class JetiCore extends Device {
	private final ByteByReference byteRef1 = new ByteByReference(), byteRef2 = new ByteByReference();

	private JetiCore (Pointer handle) {
		super(handle);
	}

	public JetiResult<Boolean> reset () {
		ensureOpen();
		return JetiResult.result(JetiCoreLibrary.INSTANCE.JETI_Reset(handle));
	}

	public JetiResult<Boolean> hardReset () {
		ensureOpen();
		return JetiResult.result(JetiCoreLibrary.INSTANCE.JETI_HardReset(handle));
	}

	public JetiResult<Boolean> breakMeasurement () {
		ensureOpen();
		return JetiResult.result(JetiCoreLibrary.INSTANCE.JETI_Break(handle));
	}

	public JetiResult<Boolean> measure () {
		ensureOpen();
		return JetiResult.result(JetiCoreLibrary.INSTANCE.JETI_InitMeasure(handle));
	}

	public JetiResult<Boolean> prepareMeasurement () {
		ensureOpen();
		return JetiResult.result(JetiCoreLibrary.INSTANCE.JETI_PreTrigMeasure(handle));
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_MeasureStatusCore(handle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue() != 0);
		return JetiResult.error(result);
	}

	public JetiResult<String> getFirmwareVersion () {
		ensureOpen();
		var versionBuffer = new byte[256];
		int result = JetiCoreLibrary.INSTANCE.JETI_GetFirmwareVersion(handle, versionBuffer);
		if (result == SUCCESS) return JetiResult.success(new String(versionBuffer, StandardCharsets.UTF_8).trim());
		return JetiResult.error(result);
	}

	public JetiResult<DeviceType> getDeviceType () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetDeviceType(handle, byteRef1);
		if (result == SUCCESS) return JetiResult.success(DeviceType.values[byteRef1.getValue()]);
		return JetiResult.error(result);
	}

	public JetiResult<BatteryInfo> getBatteryInfo () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetBatteryStat(handle, floatRef, byteRef1, byteRef2);
		if (result == SUCCESS)
			return JetiResult.success(new BatteryInfo(floatRef.getValue(), byteRef1.getValue(), byteRef2.getValue() != 0));
		return JetiResult.error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetTint(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Integer> getPixelCount () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetPixel(handle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getTemperature () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetTemperature(handle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<String> sendCommand (String command) {
		ensureOpen();
		var answer = new byte[1024]; // Enough?
		int result = JetiCoreLibrary.INSTANCE.JETI_ArbitraryCommand(handle, command, answer);
		if (result == SUCCESS) return JetiResult.success(new String(answer, StandardCharsets.UTF_8).trim());
		return JetiResult.error(result);
	}

	static public JetiResult<Boolean> setLicenseKey (String licenseKey) {
		return JetiResult.result(JetiCoreLibrary.INSTANCE.JETI_SetLicKey(licenseKey));
	}

	static public JetiResult<Boolean> importStraylightMatrix (String matrixFile) {
		return JetiResult.result(JetiCoreLibrary.INSTANCE.JETI_ImportSLM(matrixFile));
	}

	static public JetiResult<Boolean> ignoreStraylightMatrix (boolean ignore) {
		return JetiResult.result(JetiCoreLibrary.INSTANCE.JETI_IgnoreSLM((byte)(ignore ? 1 : 0)));
	}

	static public JetiResult<Integer> getDeviceCount () {
		var count = new IntByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetNumDevices(count);
		if (result == SUCCESS) return JetiResult.success(count.getValue());
		return JetiResult.error(result);
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
		if (result == SUCCESS) {
			var info = new DeviceInfo(ConnectionType.values[connType.getValue()], DeviceType.values[deviceType.getValue()],
				string(deviceSerial), (int)comPortNr.getValue(), baudrate.getValue(), string(ipAddress), string(usbSerial),
				btAddress.getValue());
			return JetiResult.success(info);
		}
		return JetiResult.error(result);
	}

	static public JetiResult<DeviceInfo[]> getDeviceInfo () {
		JetiResult<Integer> countResult = getDeviceCount();
		if (countResult.isError()) return JetiResult.error(countResult.getErrorCode());
		int count = countResult.getValue();
		var devices = new ArrayList<DeviceInfo>(count);
		for (int i = 0; i < count; i++) {
			JetiResult<DeviceInfo> deviceResult = getDeviceInfo(i);
			if (deviceResult.isSuccess()) devices.add(deviceResult.getValue());
		}
		return JetiResult.success(devices.toArray(DeviceInfo[]::new));
	}

	static public JetiResult<JetiCore> openDevice (int deviceNumber) {
		var handle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenDevice(deviceNumber, handle);
		if (result == SUCCESS) return JetiResult.success(new JetiCore(handle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiCore> openComDevice (int comPort, int baudrate) {
		var handle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenCOMDevice(comPort, baudrate, handle);
		if (result == SUCCESS) return JetiResult.success(new JetiCore(handle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiCore> openTcpDevice (String ipAddress) {
		var handle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenTCPDevice(ipAddress, handle);
		if (result == SUCCESS) return JetiResult.success(new JetiCore(handle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiCore> openUsbDevice (String usbSerial) {
		var handle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenFTDIDevice(usbSerial, handle);
		if (result == SUCCESS) return JetiResult.success(new JetiCore(handle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiCore> openBluetoothDevice (long btAddress) {
		var handle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenBTDevice(btAddress, handle);
		if (result == SUCCESS) return JetiResult.success(new JetiCore(handle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiCore> openBluetoothLeDevice (String devicePath) {
		var handle = new PointerByReference();
		char[] pathChars = devicePath.toCharArray();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenBTLEDevice(pathChars, handle);
		if (result == SUCCESS) return JetiResult.success(new JetiCore(handle.getValue()));
		return JetiResult.error(result);
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
