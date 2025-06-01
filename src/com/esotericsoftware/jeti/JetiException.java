
package com.esotericsoftware.jeti;

/** @author Nathan Sweet <misc@n4te.com> */
public class JetiException extends Exception {
	private final int errorCode;

	public JetiException (int errorCode) {
		super(getErrorMessage(errorCode));
		this.errorCode = errorCode;
	}

	public JetiException (int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public JetiException (int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode () {
		return errorCode;
	}

	static private String getErrorMessage (int errorCode) {
		return switch (errorCode) {
		case 0x00000000 -> "No error occurred";
		case 0x00000001 -> "Device already open";
		case 0x00000002 -> "Could not open COM-port";
		case 0x00000003 -> "Could not set COM-port settings";
		case 0x00000004 -> "Could not set buffer size of COM-port";
		case 0x00000005 -> "Could not purge buffers of COM-port";
		case 0x00000006 -> "Could not set COM-port timeout";
		case 0x00000007 -> "Could not send to device";
		case 0x00000008 -> "Timeout error";
		case 0x00000009 -> "Break";
		case 0x0000000A -> "Could not receive from device";
		case 0x0000000B -> "Command not supported or invalid argument";
		case 0x0000000C -> "Could not convert received data";
		case 0x0000000D -> "Invalid argument";
		case 0x0000000E -> "Device busy";
		case 0x00000011 -> "Invalid checksum of received data";
		case 0x00000012 -> "Invalid stepwidth";
		case 0x00000013 -> "Invalid device number";
		case 0x00000014 -> "Device not connected";
		case 0x00000015 -> "Invalid device handle";
		case 0x00000016 -> "Invalid calibration file number";
		case 0x00000017 -> "Calibration data not read";
		case 0x00000020 -> "Overexposure";
		case 0x00000022 -> "Measurement failed";
		case 0x00000023 -> "Adaption failed";
		case 0x00000050 -> "Straylight file not found";
		case 0x00000051 -> "Could not find or create straylight directory";
		case 0x00000052 -> "No straylight file";
		case 0x00000053 -> "Not enough memory for straylight matrix";
		case 0x00000054 -> "Could not read serial number";
		case 0x00000080 -> "Internal DLL error";
		case 0x00000081 -> "Calculation error";
		case 0x00000100 -> "Could not read from COM port";
		case 0x000000FF -> "Fatal communication error";
		default -> "Unknown error code: 0x" + Integer.toHexString(errorCode);
		};
	}
}
