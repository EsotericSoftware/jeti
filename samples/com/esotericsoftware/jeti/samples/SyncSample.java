
package com.esotericsoftware.jeti.samples;

import java.util.Scanner;

import com.esotericsoftware.jeti.Core;
import com.esotericsoftware.jeti.JetiSDK;
import com.esotericsoftware.jeti.Radio;
import com.esotericsoftware.jeti.RadioEx;

/** This sample shows how to make synchronized measurements with the use of an optical trigger and the cycle mode.
 * 
 * This is a port of the SyncSample.c to Java, demonstrating synchronized measurements with the JETI SDK.
 * 
 * @author Nathan Sweet <misc@n4te.com> */
public class SyncSample {
	private Radio radio;
	private Core core;
	private final Scanner scanner = new Scanner(System.in);

	public static void main (String[] args) {
		new SyncSample().run();
	}

	public void run () {
		System.out.println("Initializing the JETI SDK...");
		JetiSDK.initialize();

		System.out.println("Searching for devices...");
		int count = RadioEx.getDeviceCount();
		if (count == 0) {
			System.out.println("No radio devices found!");
			return;
		}
		System.out.println("Radio devices: " + count);

		// Open the first found device (zero-based index)
		radio = Radio.openDevice(0);
		core = new Core(radio);
		System.out.println("Connected.");

		// Main menu loop
		char choice;
		do {
			System.out.println("""

				Select:
				--------------
				1) Perform radiometric measurement...
				0) Exit
				""");
			System.out.print("Choose: [0] ");
			String input = scanner.nextLine().trim();
			choice = input.isEmpty() ? '0' : input.charAt(0);
			System.out.println("*** " + choice + " ***\n");

			switch (choice) {
			case '1' -> performMeasurement();
			}
		} while (choice != '0');

		radio.close();
	}

	private void performMeasurement () {
		float syncFreq = 0;

		// Try to determine flicker frequency automatically
		System.out.println("Try to determine flicker frequency...");
		syncFreq = core.getFlickerFrequency().frequency();
		System.out.println(
			String.format("Detected flicker frequency: %.2f Hz, warning: %s", syncFreq, core.getFlickerFrequency().warning()));

		core.setSyncMode(true);
		core.setSyncFrequency(syncFreq);

		// Start measurement
		radio.measure();
		System.out.println("Measurement started. Please wait...");

		boolean measuring = true;
		while (measuring) {
			measuring = radio.isMeasuring();
			System.out.print(".");
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
			}
		}
		System.out.println("\n");

		// Read radiometric value
		System.out.println(String.format("Radiometric value: %.3E", radio.getRadiometricValue()));

		// Show info about the sync frequency
		System.out.println(String.format("Sync frequency [Hz]: %.2f", syncFreq));

		// Set sync mode back to use integration time in ms
		core.setSyncMode(false);
	}
}
