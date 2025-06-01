
package com.esotericsoftware.jeti.samples;

import static com.esotericsoftware.jeti.JetiSDKTest.*;

import java.util.Scanner;

import com.esotericsoftware.jeti.JetiRadio;
import com.esotericsoftware.jeti.JetiResult;
import com.esotericsoftware.jeti.JetiSDK;

public class RadioExSample {
	static private final Scanner scanner = new Scanner(System.in);

	static public void main (String[] args) {
		JetiRadio radioDevice = null;

		try {
			// Initialize the SDK
			System.out.println("Initializing JETI SDK...");
			JetiSDK.initialize();

			// Connect to TCP device - use simplified approach
			System.out.printf("Attempting to connect to TCP device at %s...%n", IP);

			// For TCP devices, try to open radio device directly after setting up TCP connection
			// The JETI SDK should handle the TCP connection internally when we try to open devices
			JetiResult<Integer> numRadioDevicesResult = JetiSDK.getNumberOfRadioDevices();
			if (numRadioDevicesResult.isError() || numRadioDevicesResult.getValue() == 0) {
				System.out.printf("""
					No radio devices found!
					This may mean:
					1. No device at IP %s
					2. Device not configured for TCP
					3. Network connectivity issue
					Error code: 0x%08X
					""", IP, numRadioDevicesResult.getErrorCode());
				return;
			}

			System.out.printf("Found %d radio device(s)%n", numRadioDevicesResult.getValue());

			// Open the first found device (zero-based index)
			JetiResult<JetiRadio> deviceResult = JetiSDK.openRadioDevice(0);
			if (deviceResult.isError()) {
				System.out.printf("Could not open radio device!%nError code: 0x%08X%n", deviceResult.getErrorCode());
				return;
			}

			radioDevice = deviceResult.getValue();
			System.out.printf("Successfully connected to radio device via TCP at %s%n", IP);

			char choice;
			do {
				System.out.println("""

					Please select:
					--------------

					1) perform radiometric measurement...
					2) get device information...

					*********************
					* Single Operations *
					*********************

					a) start radiometric measurement...
					b) break measurement...
					c) get measurement status...
					d) get the radiometric value...
					e) get the photometric value...
					f) get chromaticity coordinates x and y...
					g) get correlated color temperature CCT...
					h) get color rendering index CRI...
					0) exit

					""");

				String input = scanner.nextLine().trim();
				choice = input.isEmpty() ? '0' : input.charAt(0);

				switch (choice) {
				case '1':
					performCompleteMeasurement(radioDevice);
					break;
				case '2':
					getDeviceInformation();
					break;
				case 'a':
					startMeasurement(radioDevice);
					break;
				case 'b':
					breakMeasurement(radioDevice);
					break;
				case 'c':
					getMeasurementStatus(radioDevice);
					break;
				case 'd':
					getRadiometricValue(radioDevice);
					break;
				case 'e':
					getPhotometricValue(radioDevice);
					break;
				case 'f':
					getChromaticityCoordinates(radioDevice);
					break;
				case 'g':
					getCorrelatedColorTemperature(radioDevice);
					break;
				case 'h':
					getColorRenderingIndex(radioDevice);
					break;
				case '0':
					break;
				default:
					break;
				}
			} while (choice != '0');
		} catch (Throwable ex) {
			System.err.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			// Close the connection to the device
			if (radioDevice != null && !radioDevice.isClosed()) radioDevice.close();
		}
	}

	static private void performCompleteMeasurement (JetiRadio device) {
		try {
			System.out.println("Performing measurement with adaptation. Please wait...\n");

			// Start measurement with automatic adaptation
			JetiResult<Boolean> measureResult = device.measureWithAdaptation();
			if (measureResult.isError()) {
				System.out.printf("Could not start measurement!%nError code: 0x%08X%n", measureResult.getErrorCode());
				return;
			}

			// Wait for measurement to complete
			boolean measuring = true;
			while (measuring) {
				Thread.sleep(100);
				JetiResult<Boolean> statusResult = device.getMeasurementStatus();
				if (statusResult.isSuccess())
					measuring = statusResult.getValue();
				else {
					System.out.printf("Could not determine measurement status!%nError code: 0x%08X%n", statusResult.getErrorCode());
					return;
				}
			}

			// Get all measurement data
			JetiResult<JetiRadio.RadiometricData> dataResult = device.getAllMeasurementData();
			if (dataResult.isError())
				System.out.printf("Could not get measurement data!%nError code: 0x%08X%n", dataResult.getErrorCode());
			else {
				JetiRadio.RadiometricData data = dataResult.getValue();
				System.out.printf("Radiometric measurement completed:%n");
				System.out.printf("- Chromaticity x: %.4f%n", data.chromaticityX()[0]);
				System.out.printf("- Chromaticity y: %.4f%n", data.chromaticityY()[0]);
				System.out.printf("- CCT: %.1f K%n", data.correlatedColorTemperature()[0]);
				System.out.printf("- CRI: %.1f (may be 0 if skipped for stability)%n", data.colorRenderingIndex()[0]);
			}
		} catch (Throwable ex) {
			System.err.println("Error during measurement: " + ex.getMessage());
		}
	}

	static private void getDeviceInformation () {
		try {
			// Display TCP device information
			System.out.printf("TCP Device IP: %s%n", IP);
			System.out.printf("Connection Type: TCP%n");

			// Try to get device serials for additional info
			JetiResult<String[]> serialsResult = JetiRadio.getRadioDeviceSerials(0);
			if (serialsResult.isError())
				System.out.printf("Could not get device serial information (normal for TCP devices)%nError code: 0x%08X%n",
					serialsResult.getErrorCode());
			else {
				String[] serials = serialsResult.getValue();
				System.out.printf("Electronics serial number: %s%n", serials[0]);
				System.out.printf("Spectrometer serial number: %s%n", serials[1]);
				System.out.printf("Device serial number: %s%n", serials[2]);
			}
		} catch (Throwable ex) {
			System.err.println("Error getting device info: " + ex.getMessage());
		}
	}

	static private void startMeasurement (JetiRadio device) {
		try {
			JetiResult<Boolean> result = device.measureWithAdaptation();
			if (result.isError())
				System.out.printf("Could not start measurement!%nError code: 0x%08X%n", result.getErrorCode());
			else
				System.out.println("Measurement successfully started.");
		} catch (Throwable ex) {
			System.err.println("Error starting measurement: " + ex.getMessage());
		}
	}

	static private void breakMeasurement (JetiRadio device) {
		try {
			JetiResult<Boolean> result = device.breakMeasurement();
			if (result.isError())
				System.out.printf("Could not break measurement!%nError code: 0x%08X%n", result.getErrorCode());
			else
				System.out.println("Measurement cancelled.");
		} catch (Throwable ex) {
			System.err.println("Error breaking measurement: " + ex.getMessage());
		}
	}

	static private void getMeasurementStatus (JetiRadio device) {
		try {
			JetiResult<Boolean> result = device.getMeasurementStatus();
			if (result.isError())
				System.out.printf("Could not determine measurement status!%nError code: 0x%08X%n", result.getErrorCode());
			else if (result.getValue())
				System.out.println("Measurement in progress.");
			else
				System.out.println("Measurement ready / Device idle.");
		} catch (Throwable ex) {
			System.err.println("Error getting measurement status: " + ex.getMessage());
		}
	}

	static private void getRadiometricValue (JetiRadio device) {
		try {
			JetiResult<float[]> result = device.getRadiometricValues();
			if (result.isError())
				System.out.printf("Could not get radiometric value!%nError code: 0x%08X%n", result.getErrorCode());
			else {
				// Calculate integrated radiometric value (simplified)
				float[] values = result.getValue();
				float integratedValue = 0;
				for (float value : values)
					integratedValue += value;
				System.out.printf("Radiometric value: %.3E%n", integratedValue);
			}
		} catch (Throwable ex) {
			System.err.println("Error getting radiometric value: " + ex.getMessage());
		}
	}

	static private void getPhotometricValue (JetiRadio device) {
		try {
			JetiResult<float[]> result = device.getPhotometricValues();
			if (result.isError())
				System.out.printf("Could not get photometric value!%nError code: 0x%08X%n", result.getErrorCode());
			else {
				// Calculate integrated photometric value (simplified)
				float[] values = result.getValue();
				float integratedValue = 0;
				for (float value : values)
					integratedValue += value;
				System.out.printf("Photometric value: %.3E%n", integratedValue);
			}
		} catch (Throwable ex) {
			System.err.println("Error getting photometric value: " + ex.getMessage());
		}
	}

	static private void getChromaticityCoordinates (JetiRadio device) {
		try {
			JetiResult<float[]> result = device.getChromaticityXY();
			if (result.isError())
				System.out.printf("Could not get chromaticity coordinates x and y!%nError code: 0x%08X%n", result.getErrorCode());
			else {
				float[] coordinates = result.getValue();
				System.out.printf("Chromaticity coordinates:%nx: %.4f%ny: %.4f%n", coordinates[0], coordinates[1]);
			}
		} catch (Throwable ex) {
			System.err.println("Error getting chromaticity coordinates: " + ex.getMessage());
		}
	}

	static private void getCorrelatedColorTemperature (JetiRadio device) {
		try {
			JetiResult<Float> result = device.getCorrelatedColorTemperature();
			if (result.isError())
				System.out.printf("Could not get correlated color temperature!%nError code: 0x%08X%n", result.getErrorCode());
			else
				System.out.printf("Correlated color temperature CCT: %.1f K%n", result.getValue());
		} catch (Throwable ex) {
			System.err.println("Error getting CCT: " + ex.getMessage());
		}
	}

	static private void getColorRenderingIndex (JetiRadio device) {
		try {
			System.out.print("Please enter the correlated color temperature of the reference source (K): ");
			String input = scanner.nextLine();

			try {
				Float.parseFloat(input);
				JetiResult<Float> result = device.getColorRenderingIndex();
				if (result.isError())
					System.out.printf("Could not get color rendering index!%nError code: 0x%08X%n", result.getErrorCode());
				else
					System.out.printf("Color rendering index CRI: %.1f%n", result.getValue());
			} catch (NumberFormatException e) {
				System.out.printf("'%s' is an invalid color temperature.%n", input);
			}
		} catch (Throwable ex) {
			System.err.println("Error getting CRI: " + ex.getMessage());
		}
	}

}
