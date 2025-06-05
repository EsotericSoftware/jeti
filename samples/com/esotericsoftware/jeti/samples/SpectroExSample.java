package com.esotericsoftware.jeti.samples;

import static com.esotericsoftware.jeti.samples.RadioExSample.*;

import java.util.Scanner;

import com.esotericsoftware.jeti.JetiException;
import com.esotericsoftware.jeti.JetiSDK;
import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.SpectroEx;

public class SpectroExSample {
	private SpectroEx spectroEx;
	private final Scanner scanner = new Scanner(System.in);

	static public void main (String[] args) {
		new SpectroExSample().run();
	}

	public void run () {
		System.out.println("Initializing the JETI SDK...");
		JetiSDK.initialize();

		System.out.println("Searching for devices...");
		try {
			int count = SpectroEx.getDeviceCount();
			if (count == 0) {
				System.out.println("No SpectroEx devices found!");
				return;
			}
			System.out.println("SpectroEx devices: " + count);

			spectroEx = SpectroEx.openDevice(0);
			System.out.println("Connected.");
		} catch (JetiException e) {
			System.err.println("Error initializing device: " + e.getMessage());
			return;
		}

		char choice;
		do {
			System.out.println("""

				Select:
				--------------
				1) Perform light measurement...
				2) Get serial numbers...

				Single operations:
				------------------
				a) Start light measurement...
				b) Break measurement...
				c) Get measurement status...
				d) Get the light spectrum (wavelength based)...
				e) Get the light spectrum (pixel based)...
				0) Exit
				""");

			System.out.print("Choose: [0] ");
			String input = scanner.nextLine().trim();
			choice = input.isEmpty() ? '0' : input.charAt(0);
			System.out.println("*** " + choice + " ***\n");

			switch (choice) {
			case '1' -> performSpectroMeasurement();
			case '2' -> getDeviceInfo();
			case 'a' -> startMeasurement();
			case 'b' -> cancelMeasurement();
			case 'c' -> isMeasuring();
			case 'd' -> getLightSpectrumWavelength();
			case 'e' -> getLightSpectrumPixel();
			case '0' -> {
				return;
			}
			default -> System.out.println("Invalid selection.");
			}
		} while (choice != '0');

		spectroEx.close();
	}

	private void performSpectroMeasurement () {
		try {
			float integrationTime = promptIntegrationTime(scanner);
			int averageCount = promptAveragingCount(scanner);
			int stepWidth = promptStepWidth(scanner);

			System.out.println("Performing measurement...\n");

			spectroEx.startLightMeasurement(integrationTime, averageCount);

			boolean measuring = true;
			while (measuring) {
				Thread.sleep(100);
				measuring = spectroEx.isMeasuring();
			}

			// Read the light spectrum (380-780nm, 1nm step size).
			float[] spectrum = spectroEx.getLightWaveData(380, 780, stepWidth);
			for (int i = 0; i < spectrum.length; i++)
				System.out.println(String.format("wl[nm]: %d    cts: %.6f", i + 380, spectrum[i]));
		} catch (Throwable ex) {
			System.err.println("Error during light spectrum measurement: " + ex.getMessage());
		}
	}

	/** Get serial numbers from the first device found. */
	private void getDeviceInfo () {
		try {
			DeviceSerials serials = SpectroEx.getDeviceSerials(0);
			System.out.println("Electronics serial number: " + serials.electronics());
			System.out.println("Spectrometer serial number: " + serials.spectrometer());
			System.out.println("Device serial number: " + serials.device());
		} catch (Throwable ex) {
			System.err.println("Error getting device info: " + ex.getMessage());
		}
	}

	/** Start a new measurement. */
	private void startMeasurement () {
		try {
			float integrationTime = promptIntegrationTime(scanner);
			int averageCount = promptAveragingCount(scanner);

			spectroEx.startLightMeasurement(integrationTime, averageCount);
			System.out.println("Measurement started.");
		} catch (Throwable ex) {
			System.err.println("Error starting measurement: " + ex.getMessage());
		}
	}

	/** Cancels an initiated measurement. */
	private void cancelMeasurement () {
		try {
			spectroEx.cancelMeasurement();
			System.out.println("Measurement cancelled.");
		} catch (Throwable ex) {
			System.err.println("Error breaking measurement: " + ex.getMessage());
		}
	}

	/** Returns the status of any current measurement. */
	private void isMeasuring () {
		try {
			if (spectroEx.isMeasuring())
				System.out.println("Measurement in progress.");
			else
				System.out.println("No measurement in progress.");
		} catch (Throwable ex) {
			System.err.println("Error getting measurement status: " + ex.getMessage());
		}
	}

	/** Read the light spectrum, 380-780nm. */
	private void getLightSpectrumWavelength () {
		try {
			float[] spectrum = spectroEx.getLightWaveData(380, 780, 1.0f);
			for (int i = 0; i < spectrum.length; i++)
				System.out.println(String.format("wl[nm]: %d    cts: %.6f", i + 380, spectrum[i]));
		} catch (Throwable ex) {
			System.err.println("Error reading light spectrum wave data: " + ex.getMessage());
		}
	}

	/** Read the light spectrum, pixel based. */
	private void getLightSpectrumPixel () {
		try {
			int pixelCount = spectroEx.getPixelCount();
			int[] spectrum = spectroEx.getLightPixelData(pixelCount);
			for (int i = 0; i < spectrum.length; i++)
				System.out.println(String.format("pix: %d    cts: %d", i, spectrum[i]));
		} catch (Throwable ex) {
			System.err.println("Error reading light spectrum pixel data: " + ex.getMessage());
		}
	}
}