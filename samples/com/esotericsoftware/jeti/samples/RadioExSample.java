
package com.esotericsoftware.jeti.samples;

import java.util.Scanner;

import com.esotericsoftware.jeti.JetiSDK;
import com.esotericsoftware.jeti.JetiSDK.CRI;
import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.TM30;
import com.esotericsoftware.jeti.JetiSDK.XY;
import com.esotericsoftware.jeti.RadioEx;
import com.esotericsoftware.jeti.Result;

public class RadioExSample {
	private RadioEx radioEx;
	private final Scanner scanner = new Scanner(System.in);

	static public void main (String[] args) {
		new RadioExSample().run();
	}

	public void run () {
		System.out.println("Initializing the JETI SDK...");
		JetiSDK.initialize();

		System.out.println("Searching for devices...");
		Result<Integer> deviceCount = RadioEx.getDeviceCount();
		if (deviceCount.isError() || deviceCount.getValue() == 0) {
			System.out.println("No radio ex devices found! Error: " + deviceCount);
			return;
		}
		System.out.println("Radio ex devices: " + deviceCount.getValue());

		Result<RadioEx> deviceResult = RadioEx.openDevice(0);
		if (deviceResult.isError()) {
			System.out.println("Could not open radio ex device! Error: " + deviceResult);
			return;
		}
		radioEx = deviceResult.getValue();
		System.out.println("Connected.");

		char choice;
		do {
			System.out.println("""

				Select:
				--------------
				1) Perform radiometric measurement...
				2) Get serial numbers...

				Single operations:
				------------------
				a) Start radiometric measurement...
				b) Break measurement...
				c) Get measurement status...
				d) Get the radiometric value...
				e) Get the photometric value...
				f) Get chromaticity coordinates x and y...
				g) Get CCT...
				h) Get CRI...
				i) Get TM-30...
				j) Get all measurements...
				0) Exit
				""");
			System.out.print("Choose: [0] ");
			String input = scanner.nextLine().trim();
			choice = input.isEmpty() ? '0' : input.charAt(0);
			System.out.println("*** " + choice + " ***\n");

			switch (choice) {
			case '1' -> performRadioMeasurement();
			case '2' -> getDeviceInfo();
			case 'a' -> startMeasurement();
			case 'b' -> breakMeasurement();
			case 'c' -> getMeasurementStatus();
			case 'd' -> getRadiometricValue();
			case 'e' -> getPhotometricValue();
			case 'f' -> getChromaticityCoordinates();
			case 'g' -> getCCT();
			case 'h' -> getCRI();
			case 'i' -> getTM30();
			case 'j' -> {
				getRadiometricValue();
				getPhotometricValue();
				getChromaticityCoordinates();
				getCCT();
				getCRI();
				getTM30();
			}
			case '0' -> {
				return;
			}
			default -> System.out.println("Invalid selection.");
			}
		} while (choice != '0');

		radioEx.close();
	}

	/** Start a radiometric measurement in the range of 380 to 780 nm. */
	private void performRadioMeasurement () {
		try {
			float integrationTime = promptIntegrationTime(scanner);
			int averageCount = promptAveragingCount(scanner);
			int stepWidth = promptStepWidth(scanner);

			System.out.println("Performing measurement...\n");

			// Start measurement.
			Result<Boolean> measureResult = radioEx.measure(integrationTime, averageCount, stepWidth);
			if (measureResult.isError()) {
				System.out.println("Could not start measurement! Error: " + measureResult);
				return;
			}

			// Wait for completion.
			boolean measuring = true;
			while (measuring) {
				Thread.sleep(100);
				Result<Boolean> statusResult = radioEx.getMeasurementStatus();
				if (statusResult.isSuccess())
					measuring = statusResult.getValue();
				else {
					System.out.println("Could not determine measurement status! Error: " + statusResult);
					return;
				}
			}

			// Get radiometric value.
			Result<Float> radioResult = radioEx.getRadiometricValue(380, 780);
			if (radioResult.isError())
				System.out.println("Could not get radiometric value! Error: " + radioResult);
			else
				System.out.println(String.format("Radiometric value: %.3E", radioResult.getValue()));
		} catch (Throwable ex) {
			System.err.println("Error during measurement: " + ex.getMessage());
		}
	}

	/** Get serial numbers from the first found device */
	private void getDeviceInfo () {
		try {
			Result<DeviceSerials> serialsResult = RadioEx.getDeviceSerials(0);
			if (serialsResult.isError())
				System.out.println("Could not get device serial information (normal for TCP devices) Error: " + serialsResult);
			else {
				DeviceSerials serials = serialsResult.getValue();
				System.out.println("Electronics serial number: " + serials.electronics());
				System.out.println("Spectrometer serial number: " + serials.spectrometer());
				System.out.println("Device serial number: " + serials.device());
			}
		} catch (Throwable ex) {
			System.err.println("Error getting device info: " + ex.getMessage());
		}
	}

	/** Start a new measurement. */
	private void startMeasurement () {
		try {
			float integrationTime = promptIntegrationTime(scanner);
			int averageCount = promptAveragingCount(scanner);
			int stepWidth = promptStepWidth(scanner);

			Result<Boolean> result = radioEx.measure(integrationTime, averageCount, stepWidth);
			if (result.isError())
				System.out.println("Could not start measurement! Error: " + result);
			else
				System.out.println("Measurement successfully started.");
		} catch (Throwable ex) {
			System.err.println("Error starting measurement: " + ex.getMessage());
		}
	}

	/** Cancels an initiated measurement. */
	private void breakMeasurement () {
		try {
			Result<Boolean> result = radioEx.breakMeasurement();
			if (result.isError())
				System.out.println("Could not break measurement! Error: " + result);
			else
				System.out.println("Measurement cancelled.");
		} catch (Throwable ex) {
			System.err.println("Error breaking measurement: " + ex.getMessage());
		}
	}

	/** Returns the status of any current measurement. */
	private void getMeasurementStatus () {
		try {
			Result<Boolean> result = radioEx.getMeasurementStatus();
			if (result.isError())
				System.out.println("Could not determine measurement status! Error: " + result);
			else if (result.getValue())
				System.out.println("Measurement in progress.");
			else
				System.out.println("No measurement in progress.");
		} catch (Throwable ex) {
			System.err.println("Error getting measurement status: " + ex.getMessage());
		}
	}

	/** Returns the radiometric value determined by the last measurement. */
	private void getRadiometricValue () {
		try {
			Result<Float> result = radioEx.getRadiometricValue(380, 780);
			if (result.isError())
				System.out.println("Could not get radiometric value! Error: " + result);
			else
				System.out.println(String.format("Radiometric value: %.3E", result.getValue()));
		} catch (Throwable ex) {
			System.err.println("Error getting radiometric value: " + ex.getMessage());
		}
	}

	/** Returns the photometric value determined by the last measuement. */
	private void getPhotometricValue () {
		try {
			Result<Float> result = radioEx.getPhotometricValue();
			if (result.isError())
				System.out.println("Could not get photometric value! Error: " + result);
			else
				System.out.println(String.format("Photometric value: %.3E", result.getValue()));
		} catch (Throwable ex) {
			System.err.println("Error getting photometric value: " + ex.getMessage());
		}
	}

	/** Returns the CIE-1931 chromaticity coordinates xy determined by the last measurement. */
	private void getChromaticityCoordinates () {
		try {
			Result<XY> result = radioEx.getChromaXY();
			if (result.isError())
				System.out.println("Could not get chromaticity coordinates x and y! Error: " + result);
			else {
				XY xy = result.getValue();
				System.out.println(String.format("Chromaticity coordinates:\nx: %.4f\ny: %.4f", xy.x(), xy.y()));
			}
		} catch (Throwable ex) {
			System.err.println("Error getting chromaticity coordinates: " + ex.getMessage());
		}
	}

	/** Returns the CCT determined by the last measurement. */
	private void getCCT () {
		try {
			Result<Float> result = radioEx.getCCT();
			if (result.isError())
				System.out.println("Could not get CCT! Error: " + result);
			else
				System.out.println(String.format("CCT: %.1f", result.getValue()));
		} catch (Throwable ex) {
			System.err.println("Error getting CCT: " + ex.getMessage());
		}
	}

	/** Returns the color rendering indices according to the CIE 13.3-1995 publication. */
	private void getCRI () {
		try {
			System.out.print("Enter the CCT of the reference source (or 0): [0] ");
			String input = scanner.nextLine();
			if (input.isEmpty()) input = "0";
			try {
				float cct = Float.parseFloat(input);
				Result<CRI> result = radioEx.getCRI(cct);
				if (result.isError())
					System.out.println("Could not get colour rendering indices! Error: " + result);
				else {
					CRI cri = result.getValue();
					System.out.println(String.format("DC: %.1E", cri.dcError()));
					System.out.println(String.format("Inaccuracy: %.1f%%", cri.inaccuracyPercent()));
					System.out.println(String.format("Ra: %.2f", cri.ra()));
					float[] samples = cri.samples();
					for (int i = 0; i < samples.length; i++)
						System.out.println(String.format("R%d: %.1f", i + 1, samples[i]));
				}
			} catch (NumberFormatException ex) {
				throw new RuntimeException("Invalid color temperature: " + input);
			}
		} catch (Throwable ex) {
			System.err.println("Error getting CRI: " + ex.getMessage());
		}
	}

	private void getTM30 () {
		try {
			Result<TM30> result = radioEx.getTM30(false);
			if (result.isError())
				System.out.println("Could not get TM-30! Error: " + result);
			else {
				TM30 tm30 = result.getValue();
				System.out.println(String.format("Fidelity Rf: %.2f", tm30.rf()));
				System.out.println(String.format("Color saturation Rg: %.2f", tm30.rg()));
			}
		} catch (Throwable ex) {
			System.err.println("Error getting TM-30: " + ex.getMessage());
		}
	}

	static float promptIntegrationTime (Scanner scanner) {
		System.out.print("Enter the integration time (0 for adaptation): [0] ");
		String input = scanner.nextLine().trim();
		if (input.isEmpty()) return 0;
		try {
			return Float.parseFloat(input);
		} catch (NumberFormatException ex) {
			throw new RuntimeException("Invalid integration time: " + input);
		}
	}

	static int promptAveragingCount (Scanner scanner) {
		System.out.print("Enter the measurement count for averaging: [1] ");
		String input = scanner.nextLine().trim();
		if (input.isEmpty()) return 1;
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException ex) {
			throw new RuntimeException("Invalid count of measurement for averaging: " + input);
		}
	}

	static int promptStepWidth (Scanner scanner) {
		System.out.print("Enter the step width in nm: [5] ");
		String input = scanner.nextLine().trim();
		if (input.isEmpty()) return 5;
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException ex) {
			throw new RuntimeException("Invalid step width: " + input);
		}
	}
}
