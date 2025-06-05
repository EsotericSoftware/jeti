
package com.esotericsoftware.jeti.samples;

import java.util.Scanner;

import com.esotericsoftware.jeti.JetiSDK;
import com.esotericsoftware.jeti.JetiSDK.CRI;
import com.esotericsoftware.jeti.JetiSDK.XY;
import com.esotericsoftware.jeti.Radio;
import com.esotericsoftware.jeti.RadioEx;

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
		int count = RadioEx.getDeviceCount();
		if (count == 0) {
			System.out.println("No radio ex devices found!");
			return;
		}
		System.out.println("Radio devices: " + count);

		// Open the first found device (zero-based index)
		radio = Radio.openDevice(0);
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
			case 'b' -> cancelMeasurement();
			case 'c' -> isMeasuring();
			case 'd' -> getRadiometricValue();
			case 'e' -> getPhotometricValue();
			case 'f' -> getChromaticityCoordinates();
			case 'g' -> getCCT();
			case 'h' -> getCRI();
			}
		} while (choice != '0');

		radio.close();
	}

	private void performCompleteMeasurement () {
		// Start a radiometric measurement in the range of 380 to 780 nm with a step-width of 5nm.
		// The integration time will be determined automatically.
		System.out.println("Performing measurement...\n");

		radio.measure();

		boolean measuring = true;
		while (measuring) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
			}
			measuring = radio.isMeasuring();
		}

		System.out.println(String.format("Radiometric value: %.3E", radio.getRadiometricValue()));
	}

	private void startMeasurement () {
		// Start a radiometric measurement in the range of 380 to 780 nm with a step-width of 5nm.
		// The integration time will be determined automatically.
		radio.measure();
		System.out.println("Measurement started.");
	}

	private void cancelMeasurement () {
		// Cancels an initiated measurement
		radio.cancelMeasurement();
		System.out.println("Measurement cancelled.");
	}

	private void isMeasuring () {
		// Returns the status of the initiated measurement
		if (radio.isMeasuring())
			System.out.println("Measurement in progress.");
		else
			System.out.println("No measurement in progress.");
	}

	private void getRadiometricValue () {
		// Returns the radiometric value determined by the last measurement
		System.out.println(String.format("Radiometric value: %.3E", radio.getRadiometricValue()));
	}

	private void getPhotometricValue () {
		// Returns the photometric value determined by the last measurement
		System.out.println(String.format("Photometric value: %.3E", radio.getPhotometricValue()));
	}

	private void getChromaticityCoordinates () {
		// Returns the CIE-1931 chromaticity coordinates xy determined by the last measurement
		XY xy = radio.getChromaXY();
		System.out.println(String.format("""
			Chromaticity coordinates:
			x: %.4f
			y: %.4f""", xy.x(), xy.y()));
	}

	private void getCCT () {
		// Returns the CCT determined by the last measurement
		System.out.println(String.format("CCT: %.1f", radio.getCCT()));
	}

	private void getCRI () {
		// Returns the color rendering indices according to the CIE 13.3-1995 publication
		CRI cri = radio.getCRI();
		System.out.println(String.format("DC: %.1E", cri.dcError())); // chromaticity difference
		System.out.println(String.format("Inaccuracy: %.1f%%", cri.inaccuracyPercent()));
		System.out.println(String.format("Ra: %.2f", cri.ra())); // general color rendering index
		float[] samples = cri.samples();
		for (int i = 0; i < samples.length; i++) {
			System.out.println(String.format("R%d: %.1f", i + 1, samples[i])); // special color rendering indices
		}
	}
}
