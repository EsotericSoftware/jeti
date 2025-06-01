
package com.esotericsoftware.jeti;

import static com.esotericsoftware.jeti.JetiSDK.*;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiCore implements AutoCloseable {
	private Pointer deviceHandle;

	private JetiCore (Pointer deviceHandle) {
		Objects.nonNull(deviceHandle);
		this.deviceHandle = deviceHandle;
	}

	public JetiResult<Boolean> reset () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_Reset(deviceHandle);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> hardReset () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_HardReset(deviceHandle);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> sendBreak () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_Break(deviceHandle);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> initMeasurement () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_InitMeasure(deviceHandle);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> preTriggerMeasurement () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_PreTrigMeasure(deviceHandle);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		ensureOpen();
		var status = new IntByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_MeasureStatusCore(deviceHandle, status);
		if (result == SUCCESS) return JetiResult.success(status.getValue() != 0);
		return JetiResult.error(result);
	}

	public JetiResult<String> getFirmwareVersion () {
		ensureOpen();
		var versionBuffer = new byte[STRING_BUFFER_SIZE];
		int result = JetiCoreLibrary.INSTANCE.JETI_GetFirmwareVersion(deviceHandle, versionBuffer);
		if (result == SUCCESS) return JetiResult.success(new String(versionBuffer, StandardCharsets.UTF_8).trim());
		return JetiResult.error(result);
	}

	public JetiResult<DeviceType> getDeviceType () {
		ensureOpen();
		var deviceType = new ByteByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetDeviceType(deviceHandle, deviceType);
		if (result == SUCCESS) return JetiResult.success(DeviceType.values[deviceType.getValue()]);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getBatteryVoltage () {
		ensureOpen();
		var voltage = new FloatByReference();
		var percent = new ByteByReference();
		var isCharging = new ByteByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetBatteryStat(deviceHandle, voltage, percent, isCharging);
		if (result == SUCCESS) return JetiResult.success(voltage.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Integer> getBatteryPercent () {
		ensureOpen();
		var voltage = new FloatByReference();
		var percent = new ByteByReference();
		var isCharging = new ByteByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetBatteryStat(deviceHandle, voltage, percent, isCharging);
		if (result == SUCCESS) return JetiResult.success((int)percent.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> isBatteryCharging () {
		ensureOpen();
		var voltage = new FloatByReference();
		var percent = new ByteByReference();
		var isCharging = new ByteByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetBatteryStat(deviceHandle, voltage, percent, isCharging);
		if (result == SUCCESS) return JetiResult.success(isCharging.getValue() != 0);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		ensureOpen();
		var tint = new FloatByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetTint(deviceHandle, tint);
		if (result == SUCCESS) return JetiResult.success(tint.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Integer> getPixelCount () {
		ensureOpen();
		var pixelCount = new IntByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetPixel(deviceHandle, pixelCount);
		if (result == SUCCESS) return JetiResult.success(pixelCount.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getTemperature () {
		ensureOpen();
		var temperature = new FloatByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetTemperature(deviceHandle, temperature);
		if (result == SUCCESS) return JetiResult.success(temperature.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<String> sendArbitraryCommand (String command) {
		ensureOpen();
		var answer = new byte[STRING_BUFFER_SIZE];
		int result = JetiCoreLibrary.INSTANCE.JETI_ArbitraryCommand(deviceHandle, command, answer);
		if (result == SUCCESS) return JetiResult.success(new String(answer, StandardCharsets.UTF_8).trim());
		return JetiResult.error(result);
	}

	private void ensureOpen () {
		if (deviceHandle != null) throw new IllegalStateException("Core device is closed.");
	}

	public void close () {
		if (deviceHandle != null) {
			try {
				int result = JetiCoreLibrary.INSTANCE.JETI_CloseDevice(deviceHandle);
				if (result != SUCCESS) Log.warn("Unable to close core device: 0x" + Integer.toHexString(result));
			} catch (Throwable ex) {
				Log.warn("Unable to close core device.", ex);
			} finally {
				deviceHandle = null;
			}
		}
	}

	public boolean isClosed () {
		return deviceHandle == null;
	}

	static public JetiResult<Boolean> setLicenseKey (String licenseKey) {
		int result = JetiCoreLibrary.INSTANCE.JETI_SetLicKey(licenseKey);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	static public JetiResult<Boolean> importStraylightMatrix (String matrixFile) {
		int result = JetiCoreLibrary.INSTANCE.JETI_ImportSLM(matrixFile);
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	static public JetiResult<Boolean> ignoreStraylightMatrix (boolean ignore) {
		int result = JetiCoreLibrary.INSTANCE.JETI_IgnoreSLM((byte)(ignore ? 1 : 0));
		return JetiResult.fromErrorCode(result == SUCCESS, result);
	}

	static public JetiResult<Integer> getNumberOfDevices () {
		var numDevices = new IntByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetNumDevices(numDevices);
		if (result == SUCCESS) return JetiResult.success(numDevices.getValue());
		return JetiResult.error(result);
	}

	static public JetiResult<DeviceInfo> getDeviceInfo (int deviceNumber) {
		var connType = new ByteByReference();
		var deviceType = new ByteByReference();
		var deviceSerial = new byte[STRING_BUFFER_SIZE];
		var comPortNr = new ShortByReference();
		var baudrate = new IntByReference();
		var ipAddress = new byte[STRING_BUFFER_SIZE];
		var usbSerial = new byte[STRING_BUFFER_SIZE];
		var btAddress = new LongByReference();
		var btleDevicePath = new char[STRING_BUFFER_SIZE];

		int result = JetiCoreLibrary.INSTANCE.JETI_GetDeviceInfoEx(deviceNumber, connType, deviceType, deviceSerial, comPortNr,
			baudrate, ipAddress, usbSerial, btAddress, btleDevicePath);

		if (result == SUCCESS) {
			var info = new DeviceInfo(ConnectionType.values[connType.getValue()], DeviceType.values[deviceType.getValue()],
				bytesToString(deviceSerial), (int)comPortNr.getValue(), baudrate.getValue(), bytesToString(ipAddress),
				bytesToString(usbSerial), btAddress.getValue(), new String(btleDevicePath).trim());
			return JetiResult.success(info);
		}
		return JetiResult.error(result);
	}

	static public JetiResult<DeviceInfo[]> getAllDevices () {
		JetiResult<Integer> numDevicesResult = getNumberOfDevices();
		if (numDevicesResult.isError()) return JetiResult.error(numDevicesResult.getErrorCode());

		int numDevices = numDevicesResult.getValue();
		var devices = new DeviceInfo[numDevices];
		for (int i = 0; i < numDevices; i++) {
			JetiResult<DeviceInfo> deviceResult = getDeviceInfo(i);
			if (deviceResult.isSuccess()) devices[i] = deviceResult.getValue();
		}
		return JetiResult.success(devices);
	}

	static public JetiResult<JetiCore> openDevice (int deviceNumber) {
		var deviceHandle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenDevice(deviceNumber, deviceHandle);

		if (result == SUCCESS) return JetiResult.success(new JetiCore(deviceHandle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiCore> openComDevice (int comPort, int baudrate) {
		var deviceHandle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenCOMDevice(comPort, baudrate, deviceHandle);

		if (result == SUCCESS) return JetiResult.success(new JetiCore(deviceHandle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiCore> openTcpDevice (String ipAddress) {
		var deviceHandle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenTCPDevice(ipAddress, deviceHandle);

		if (result == SUCCESS) return JetiResult.success(new JetiCore(deviceHandle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiCore> openUsbDevice (String usbSerial) {
		var deviceHandle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenFTDIDevice(usbSerial, deviceHandle);

		if (result == SUCCESS) return JetiResult.success(new JetiCore(deviceHandle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiCore> openBluetoothDevice (long btAddress) {
		var deviceHandle = new PointerByReference();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenBTDevice(btAddress, deviceHandle);

		if (result == SUCCESS) return JetiResult.success(new JetiCore(deviceHandle.getValue()));
		return JetiResult.error(result);
	}

	static public JetiResult<JetiCore> openBluetoothLeDevice (String devicePath) {
		var deviceHandle = new PointerByReference();
		char[] pathChars = devicePath.toCharArray();
		int result = JetiCoreLibrary.INSTANCE.JETI_OpenBTLEDevice(pathChars, deviceHandle);

		if (result == SUCCESS) return JetiResult.success(new JetiCore(deviceHandle.getValue()));
		return JetiResult.error(result);
	}

	static public enum ConnectionType {
		COM, TCP, USB, BLUETOOTH, BLUETOOTH_LE;

		static public ConnectionType[] values = values();
	}

	public enum DeviceType {
		UNKNOWN, SPECBOS_1211, SPECBOS_1201, SPECBOS_1511, SPECBOS_1501, SPECBOS_1401, SPECBOS_1501_BT, SPECBOS_1501_BTLE;

		static public DeviceType[] values = values();
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
		String btleDevicePath) {}
}
