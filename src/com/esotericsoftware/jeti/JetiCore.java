
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

	private final FloatByReference floatRef = new FloatByReference();
	private final IntByReference intRef = new IntByReference();
	private final ByteByReference byteRef1 = new ByteByReference(), byteRef2 = new ByteByReference();

	private JetiCore (Pointer deviceHandle) {
		Objects.requireNonNull(deviceHandle);
		this.deviceHandle = deviceHandle;
	}

	public JetiResult<Boolean> reset () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_Reset(deviceHandle);
		return JetiResult.result(result);
	}

	public JetiResult<Boolean> hardReset () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_HardReset(deviceHandle);
		return JetiResult.result(result);
	}

	public JetiResult<Boolean> sendBreak () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_Break(deviceHandle);
		return JetiResult.result(result);
	}

	public JetiResult<Boolean> initMeasurement () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_InitMeasure(deviceHandle);
		return JetiResult.result(result);
	}

	public JetiResult<Boolean> preTriggerMeasurement () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_PreTrigMeasure(deviceHandle);
		return JetiResult.result(result);
	}

	public JetiResult<Boolean> getMeasurementStatus () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_MeasureStatusCore(deviceHandle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue() != 0);
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
		int result = JetiCoreLibrary.INSTANCE.JETI_GetDeviceType(deviceHandle, byteRef1);
		if (result == SUCCESS) return JetiResult.success(DeviceType.values[byteRef1.getValue()]);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getBatteryVoltage () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetBatteryStat(deviceHandle, floatRef, byteRef1, byteRef2);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Integer> getBatteryPercent () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetBatteryStat(deviceHandle, floatRef, byteRef1, byteRef2);
		if (result == SUCCESS) return JetiResult.success((int)byteRef1.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Boolean> isBatteryCharging () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetBatteryStat(deviceHandle, floatRef, byteRef1, byteRef2);
		if (result == SUCCESS) return JetiResult.success(byteRef2.getValue() != 0);
		return JetiResult.error(result);
	}

	public JetiResult<Float> getIntegrationTime () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetTint(deviceHandle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Integer> getPixelCount () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetPixel(deviceHandle, intRef);
		if (result == SUCCESS) return JetiResult.success(intRef.getValue());
		return JetiResult.error(result);
	}

	public JetiResult<Float> getTemperature () {
		ensureOpen();
		int result = JetiCoreLibrary.INSTANCE.JETI_GetTemperature(deviceHandle, floatRef);
		if (result == SUCCESS) return JetiResult.success(floatRef.getValue());
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
		if (deviceHandle == null) throw new IllegalStateException("Core device is closed.");
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
		return JetiResult.result(result);
	}

	static public JetiResult<Boolean> importStraylightMatrix (String matrixFile) {
		int result = JetiCoreLibrary.INSTANCE.JETI_ImportSLM(matrixFile);
		return JetiResult.result(result);
	}

	static public JetiResult<Boolean> ignoreStraylightMatrix (boolean ignore) {
		int result = JetiCoreLibrary.INSTANCE.JETI_IgnoreSLM((byte)(ignore ? 1 : 0));
		return JetiResult.result(result);
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
				string(deviceSerial), (int)comPortNr.getValue(), baudrate.getValue(), string(ipAddress), string(usbSerial),
				btAddress.getValue(), new String(btleDevicePath).trim());
			return JetiResult.success(info);
		}
		return JetiResult.error(result);
	}

	static public JetiResult<DeviceInfo[]> getAllDevices () {
		JetiResult<Integer> numDevicesResult = getDeviceCount();
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
