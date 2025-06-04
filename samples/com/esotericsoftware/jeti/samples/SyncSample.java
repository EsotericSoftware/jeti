
package com.esotericsoftware.jeti.samples;

import java.util.Scanner;

import com.esotericsoftware.jeti.Core;
import com.esotericsoftware.jeti.Core.FlickerFrequency;
import com.esotericsoftware.jeti.JetiSDK;
import com.esotericsoftware.jeti.Radio;
import com.esotericsoftware.jeti.Result;

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
		Result<FlickerFrequency> flickerResult = core.getFlickerFrequency();
		if (flickerResult.isError() || flickerResult.getValue().frequency() == 0.0f) {
			System.out.print("Could not determine flicker frequency!\nEnter a sync frequency in Hz: ");
			syncFreq = scanner.nextFloat();
		} else {
			syncFreq = flickerResult.getValue().frequency();
			System.out.println(
				String.format("Detected flicker frequency: %.2f Hz, warning: %s", syncFreq, flickerResult.getValue().warning()));
		}

		// Set sync mode
		Result<Boolean> syncModeResult = core.setSyncMode(true);
		if (syncModeResult.isError()) {
			System.out.println("Could not set sync mode! Error: " + syncModeResult);
			return;
		}

		// Set sync frequency in Hz
		Result<Boolean> syncFreqResult = core.setSyncFrequency(syncFreq);
		if (syncFreqResult.isError()) {
			System.out.println("Could not set sync frequency! Error: " + syncFreqResult);
			return;
		}

		// Start measurement
		Result<Boolean> measureResult = radio.measure();
		if (measureResult.isError()) {
			System.out.println("Could not start measurement! Error: " + measureResult);
			return;
		}
		System.out.println("Measurement started. Please wait...");

		// Wait until measurement is finished
		boolean measuring = true;
		while (measuring) {
			Result<Boolean> statusResult = radio.getMeasurementStatus();
			if (statusResult.isError()) {
				System.out.println("Could not determine measurement status! Error: " + statusResult);
				return;
			}
			measuring = statusResult.getValue();
			System.out.print(".");

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		System.out.println("\n");

		// Read radiometric value
		Result<Float> radioResult = radio.getRadiometricValue();
		if (radioResult.isSuccess()) {
			System.out.println(String.format("Radiometric value: %.3E", radioResult.getValue()));
		}

		// Show info about the sync frequency
		System.out.println(String.format("Sync frequency [Hz]: %.2f", syncFreq));

		// Set sync mode back to use integration time in ms
		core.setSyncMode(false);
	}
}
