
package com.esotericsoftware.jeti.samples;

import java.util.Scanner;

import com.esotericsoftware.jeti.JetiRadio;
import com.esotericsoftware.jeti.JetiRadioEx;
import com.esotericsoftware.jeti.JetiResult;
import com.esotericsoftware.jeti.JetiSDK;
import com.esotericsoftware.jeti.JetiSDK.CRI;
import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.XY;

public class RadioExSample {
	static private final Scanner scanner = new Scanner(System.in);

	static public void main (String[] args) {
		JetiRadioEx radioDevice = null;

		try {
			System.out.println("Initializing the JETI SDK...");
			JetiSDK.initialize();

			System.out.println("Searching for devices...");
			JetiResult<Integer> deviceCount = JetiRadio.getDeviceCount();
			if (deviceCount.isError() || deviceCount.getValue() == 0) {
				System.out.printf("No radio ex devices found! Error code: 0x%08X", deviceCount.getErrorCode());
				return;
			}
			System.out.printf("Radio ex devices: %d%n", deviceCount.getValue());

			JetiResult<JetiRadioEx> deviceResult = JetiRadioEx.openDevice(0);
			if (deviceResult.isError()) {
				System.out.printf("Could not open radio ex device!%nError code: 0x%08X%n", deviceResult.getErrorCode());
				return;
			}

			radioDevice = deviceResult.getValue();
			System.out.printf("Connected.");

			char choice;
			do {
				System.out.println("""

					Select:
					--------------
					1) Perform radiometric measurement...
					2) Get device information...

					Single operations:
					------------------
					a) Start radiometric measurement...
					b) Break measurement...
					c) Get measurement status...
					d) Get the radiometric value...
					e) Get the photometric value...
					f) Get chromaticity coordinates x and y...
					g) Get correlated color temperature CCT...
					h) Get color rendering index CRI...
					i) Get all measurements...
					0) Exit
					""");

				System.out.print("Choose: [0] ");
				String input = scanner.nextLine().trim();
				choice = input.isEmpty() ? '0' : input.charAt(0);
				System.out.println("*** " + choice + " ***\n");

				switch (choice) {
				case '1' -> performRadioMeasurement(radioDevice);
				case '2' -> getDeviceInfo();
				case 'a' -> startMeasurement(radioDevice);
				case 'b' -> breakMeasurement(radioDevice);
				case 'c' -> getMeasurementStatus(radioDevice);
				case 'd' -> getRadiometricValue(radioDevice);
				case 'e' -> getPhotometricValue(radioDevice);
				case 'f' -> getChromaticityCoordinates(radioDevice);
				case 'g' -> getCorrelatedColorTemperature(radioDevice);
				case 'h' -> getColorRenderingIndex(radioDevice);
				case 'i' -> {
					getRadiometricValue(radioDevice);
					getPhotometricValue(radioDevice);
					getChromaticityCoordinates(radioDevice);
					getCorrelatedColorTemperature(radioDevice);
					getColorRenderingIndex(radioDevice);
				}
				case '0' -> {
					return;
				}
				default -> System.out.println("Invalid selection.");
				}
			} while (choice != '0');
		} catch (Throwable ex) {
			System.err.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			if (radioDevice != null && !radioDevice.isClosed()) radioDevice.close();
		}
	}

	/** Start a radiometric measurement in the range of 380 to 780 nm. */
	static private void performRadioMeasurement (JetiRadioEx device) {
		try {
			float integrationTime = promptIntegrationTime();
			int averageCount = promptAveragingCount();
			int stepWidth = promptStepWidth();

			System.out.println("Performing measurement...\n");

			// Start measurement.
			JetiResult<Boolean> measureResult = device.measure(integrationTime, averageCount, stepWidth);
			if (measureResult.isError()) {
				System.out.printf("Could not start measurement!%nError code: 0x%08X%n", measureResult.getErrorCode());
				return;
			}

			// Wait for completion.
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

			// Get radiometric value.
			JetiResult<Float> radioResult = device.getRadiometricValue(380, 780);
			if (radioResult.isError())
				System.out.printf("Could not get radiometric value!%nError code: 0x%08X%n", radioResult.getErrorCode());
			else
				System.out.printf("Radiometric value: %.3E%n", radioResult.getValue());
		} catch (Throwable ex) {
			System.err.println("Error during measurement: " + ex.getMessage());
		}
	}

	/** Get serial numbers from the first found device */
	static private void getDeviceInfo () {
		try {
			JetiResult<DeviceSerials> serialsResult = JetiRadioEx.getDeviceSerials(0);
			if (serialsResult.isError())
				System.out.printf("Could not get device serial information (normal for TCP devices)%nError code: 0x%08X%n",
					serialsResult.getErrorCode());
			else {
				DeviceSerials serials = serialsResult.getValue();
				System.out.printf("Electronics serial number: %s%n", serials.electronics());
				System.out.printf("Spectrometer serial number: %s%n", serials.spectrometer());
				System.out.printf("Device serial number: %s%n", serials.device());
			}
		} catch (Throwable ex) {
			System.err.println("Error getting device info: " + ex.getMessage());
		}
	}

	/** Start a new measurement. */
	static private void startMeasurement (JetiRadioEx device) {
		try {
			float integrationTime = promptIntegrationTime();
			int averageCount = promptAveragingCount();
			int stepWidth = promptStepWidth();

			JetiResult<Boolean> result = device.measure(integrationTime, averageCount, stepWidth);
			if (result.isError())
				System.out.printf("Could not start measurement!%nError code: 0x%08X%n", result.getErrorCode());
			else
				System.out.println("Measurement successfully started.");
		} catch (Throwable ex) {
			System.err.println("Error starting measurement: " + ex.getMessage());
		}
	}

	/** Cancels an initiated measurement. */
	static private void breakMeasurement (JetiRadioEx device) {
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

	/** Returns the status of any current measurement. */
	static private void getMeasurementStatus (JetiRadioEx device) {
		try {
			JetiResult<Boolean> result = device.getMeasurementStatus();
			if (result.isError())
				System.out.printf("Could not determine measurement status!%nError code: 0x%08X%n", result.getErrorCode());
			else if (result.getValue())
				System.out.println("Measurement in progress.");
			else
				System.out.println("No measurement in progress.");
		} catch (Throwable ex) {
			System.err.println("Error getting measurement status: " + ex.getMessage());
		}
	}

	/** Returns the radiometric value determined by the last measurement. */
	static private void getRadiometricValue (JetiRadioEx device) {
		try {
			JetiResult<Float> result = device.getRadiometricValue(380, 780);
			if (result.isError())
				System.out.printf("Could not get radiometric value!%nError code: 0x%08X%n", result.getErrorCode());
			else
				System.out.printf("Radiometric value: %.3E%n", result.getValue());
		} catch (Throwable ex) {
			System.err.println("Error getting radiometric value: " + ex.getMessage());
		}
	}

	/** Returns the photometric value determined by the last measuement. */
	static private void getPhotometricValue (JetiRadioEx device) {
		try {
			JetiResult<Float> result = device.getPhotometricValue();
			if (result.isError())
				System.out.printf("Could not get photometric value!%nError code: 0x%08X%n", result.getErrorCode());
			else
				System.out.printf("Photometric value: %.3E%n", result.getValue());
		} catch (Throwable ex) {
			System.err.println("Error getting photometric value: " + ex.getMessage());
		}
	}

	/** Returns the CIE-1931 chromaticity coordinates xy determined by the last measurement. */
	static private void getChromaticityCoordinates (JetiRadioEx device) {
		try {
			JetiResult<XY> result = device.getChromaXY();
			if (result.isError())
				System.out.printf("Could not get chromaticity coordinates x and y!%nError code: 0x%08X%n", result.getErrorCode());
			else {
				XY xy = result.getValue();
				System.out.printf("Chromaticity coordinates:%nx: %.4f %ny: %.4f%n", xy.x(), xy.y());
			}
		} catch (Throwable ex) {
			System.err.println("Error getting chromaticity coordinates: " + ex.getMessage());
		}
	}

	/** Returns the correlated color temperature determined by the last measurement. */
	static private void getCorrelatedColorTemperature (JetiRadioEx device) {
		try {
			JetiResult<Float> result = device.getCCT();
			if (result.isError())
				System.out.printf("Could not get correlated color temperature!%nError code: 0x%08X%n", result.getErrorCode());
			else
				System.out.printf("Correlated color temperature CCT: %.1f%n", result.getValue());
		} catch (Throwable ex) {
			System.err.println("Error getting CCT: " + ex.getMessage());
		}
	}

	/** Returns the color rendering indices according to the CIE 13.3-1995 publication. */
	static private void getColorRenderingIndex (JetiRadioEx device) {
		try {
			System.out.print("Enter the CCT of the reference source (or 0): [0] ");
			String input = scanner.nextLine();
			if (input.isEmpty()) input = "0";
			try {
				float cct = Float.parseFloat(input);
				JetiResult<CRI> result = device.getCRI(cct);
				if (result.isError())
					System.out.printf("Could not get colour rendering indizes!%nError code: 0x%08X%n", result.getErrorCode());
				else {
					CRI criData = result.getValue();
					System.out.printf("DC: %.1E%n", criData.dcError());
					System.out.printf("Ra: %.2f%n", criData.ra());
					float[] specialIndices = criData.samples();
					for (int i = 0; i < specialIndices.length; i++)
						System.out.printf("R%d: %.1f%n", i + 1, specialIndices[i]);
				}
			} catch (NumberFormatException e) {
				throw new RuntimeException("Invalid color temperature: " + input);
			}
		} catch (Throwable ex) {
			System.err.println("Error getting CRI: " + ex.getMessage());
		}
	}

	static float promptIntegrationTime () {
		System.out.print("Enter the integration time (0 for adaptation): [0] ");
		String input = scanner.nextLine().trim();
		if (input.isEmpty()) return 0;
		try {
			return Float.parseFloat(input);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Invalid integration time: " + input);
		}
	}

	static int promptAveragingCount () {
		System.out.print("Enter the measurement count for averaging: [1] ");
		String input = scanner.nextLine().trim();
		if (input.isEmpty()) return 1;
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Invalid count of measurement for averaging: " + input);
		}
	}

	static int promptStepWidth () {
		System.out.print("Enter the step width in nm: [5] ");
		String input = scanner.nextLine().trim();
		if (input.isEmpty()) return 5;
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Invalid step width: " + input);
		}
	}
}
