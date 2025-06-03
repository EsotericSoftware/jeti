
package com.esotericsoftware.jeti.samples;

import java.util.Scanner;

import com.esotericsoftware.jeti.JetiSDK;
import com.esotericsoftware.jeti.JetiSDK.CRI;
import com.esotericsoftware.jeti.JetiSDK.XY;
import com.esotericsoftware.jeti.Radio;
import com.esotericsoftware.jeti.Result;

/** This sample shows how to perform basic radiometric measurements using the JETI SDK.
 * 
 * This is a port of the RadioSample.c to Java, demonstrating the use of the Radio wrapper.
 * 
 * @author Nathan Sweet <misc@n4te.com> */
public class RadioSample {
	private Radio radio;
	private final Scanner scanner = new Scanner(System.in);

	public static void main (String[] args) {
		new RadioSample().run();
	}

	public void run () {
		System.out.println("Initializing the JETI SDK...");
		JetiSDK.initialize();

		// Determines the number of connected JETI devices
		System.out.println("Searching for devices...");
		Result<Integer> deviceCount = Radio.getDeviceCount();
		if (deviceCount.isError() || deviceCount.getValue() == 0) {
			System.out.println("No radio devices found! Error: " + deviceCount);
			return;
		}
		System.out.println("Radio devices: " + deviceCount.getValue());

		// Open the first found device (zero-based index)
		Result<Radio> result = Radio.openDevice(0);
		if (result.isError()) {
			System.out.println("Could not open radio device! Error: " + result);
			return;
		}
		radio = result.getValue();
		System.out.println("Connected.");

		// Main menu loop
		char choice;
		do {
			System.out.println("""

				Select:
				--------------
				1) Perform radiometric measurement...

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
				0) Exit
				""");
			System.out.print("Choose: [0] ");
			String input = scanner.nextLine().trim();
			choice = input.isEmpty() ? '0' : input.charAt(0);
			System.out.println("*** " + choice + " ***\n");

			switch (choice) {
			case '1' -> performCompleteMeasurement();
			case 'a' -> startMeasurement();
			case 'b' -> breakMeasurement();
			case 'c' -> getMeasurementStatus();
			case 'd' -> getRadiometricValue();
			case 'e' -> getPhotometricValue();
			case 'f' -> getChromaticityCoordinates();
			case 'g' -> getCCT();
			case 'h' -> getCRI();
			}
		} while (choice != '0');

		// Close the connection to the device
		radio.close();
	}

	private void performCompleteMeasurement () {
		// Start a radiometric measurement in the range of 380 to 780 nm with a step-width of 5nm.
		// The integration time will be determined automatically.
		System.out.println("Performing measurement...\n");

		Result<Boolean> result = radio.measure();
		if (result.isError()) {
			System.out.println("Could not start measurement! Error: " + result);
			return;
		}

		// Check the measurement status until measurement has finished
		boolean measuring = true;
		while (measuring) {
			Result<Boolean> statusResult = radio.getMeasurementStatus();
			if (statusResult.isError()) {
				System.out.println("Could not determine measurement status! Error: " + statusResult);
				return;
			}
			measuring = statusResult.getValue();

			// Small delay to avoid busy waiting
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Ignore
			}
		}

		// Returns the radiometric value
		Result<Float> radioResult = radio.getRadiometricValue();
		if (radioResult.isError()) {
			System.out.println("Could not get radiometric value! Error: " + radioResult);
		} else {
			System.out.println(String.format("Radiometric value: %.3E", radioResult.getValue()));
		}
	}

	private void startMeasurement () {
		// Start a radiometric measurement in the range of 380 to 780 nm with a step-width of 5nm.
		// The integration time will be determined automatically.
		Result<Boolean> result = radio.measure();
		if (result.isError())
			System.out.println("Could not start measurement! Error: " + result);
		else
			System.out.println("Measurement successfully started.");
	}

	private void breakMeasurement () {
		// Cancels an initiated measurement
		Result<Boolean> result = radio.breakMeasurement();
		if (result.isError())
			System.out.println("Could not break measurement! Error: " + result);
		else
			System.out.println("Measurement cancelled.");
	}

	private void getMeasurementStatus () {
		// Returns the status of the initiated measurement
		Result<Boolean> result = radio.getMeasurementStatus();
		if (result.isError())
			System.out.println("Could not determine measurement status! Error: " + result);
		else if (result.getValue())
			System.out.println("Measurement in progress.");
		else
			System.out.println("No measurement in progress.");
	}

	private void getRadiometricValue () {
		// Returns the radiometric value determined by the last measurement
		Result<Float> result = radio.getRadiometricValue();
		if (result.isError()) {
			System.out.println("Could not get radiometric value! Error: " + result);
		} else {
			System.out.println(String.format("Radiometric value: %.3E", result.getValue()));
		}
	}

	private void getPhotometricValue () {
		// Returns the photometric value determined by the last measurement
		Result<Float> result = radio.getPhotometricValue();
		if (result.isError()) {
			System.out.println("Could not get photometric value! Error: " + result);
		} else {
			System.out.println(String.format("Photometric value: %.3E", result.getValue()));
		}
	}

	private void getChromaticityCoordinates () {
		// Returns the CIE-1931 chromaticity coordinates xy determined by the last measurement
		Result<XY> result = radio.getChromaXY();
		if (result.isError()) {
			System.out.println("Could not get chromaticity coordinates x and y! Error: " + result);
		} else {
			XY xy = result.getValue();
			System.out.println(String.format("""
				Chromaticity coordinates:
				x: %.4f
				y: %.4f""", xy.x(), xy.y()));
		}
	}

	private void getCCT () {
		// Returns the CCT determined by the last measurement
		Result<Float> result = radio.getCCT();
		if (result.isError()) {
			System.out.println("Could not get CCT! Error: " + result);
		} else {
			System.out.println(String.format("CCT: %.1f", result.getValue()));
		}
	}

	private void getCRI () {
		// Returns the color rendering indices according to the CIE 13.3-1995 publication
		Result<CRI> result = radio.getCRI();
		if (result.isError()) {
			System.out.println("Could not get colour rendering indices! Error: " + result);
		} else {
			CRI cri = result.getValue();
			System.out.println(String.format("DC: %.1E", cri.dcError())); // chromaticity difference
			System.out.println(String.format("Inaccuracy: %.1f%%", cri.inaccuracyPercent()));
			System.out.println(String.format("Ra: %.2f", cri.ra())); // general color rendering index
			float[] samples = cri.samples();
			for (int i = 0; i < samples.length; i++) {
				System.out.println(String.format("R%d: %.1f", i + 1, samples[i])); // special color rendering indices
			}
		}
	}
}
