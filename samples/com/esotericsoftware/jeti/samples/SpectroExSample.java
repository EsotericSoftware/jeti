
package com.esotericsoftware.jeti.samples;

import static com.esotericsoftware.jeti.samples.RadioExSample.*;

import java.util.Scanner;

import com.esotericsoftware.jeti.JetiSDK;
import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.Result;
import com.esotericsoftware.jeti.SpectroEx;

public class SpectroExSample {
	static private final Scanner scanner = new Scanner(System.in);

	static public void main (String[] args) {
		SpectroEx spectroEx = null;

		try {
			System.out.println("Initializing the JETI SDK...");
			JetiSDK.initialize();

			System.out.println("Searching for devices...");
			Result<Integer> deviceCount = SpectroEx.getDeviceCount();
			if (deviceCount.isError() || deviceCount.getValue() == 0) {
				System.out.println(String.format("No spectro ex devices found! Error code: 0x%08X", deviceCount));
				return;
			}
			System.out.println("Spectro ex devices: " + deviceCount.getValue());

			Result<SpectroEx> deviceResult = SpectroEx.openDevice(0);
			if (deviceResult.isError()) {
				System.out.println("Could not open spectro ex device! Error: " + deviceResult);
				return;
			}

			spectroEx = deviceResult.getValue();
			System.out.println("Connected.");

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
				case '1' -> performSpectroMeasurement(spectroEx);
				case '2' -> getDeviceInfo();
				case 'a' -> startMeasurement(spectroEx);
				case 'b' -> breakMeasurement(spectroEx);
				case 'c' -> getMeasurementStatus(spectroEx);
				case 'd' -> getLightSpectrumWavelength(spectroEx);
				case 'e' -> getLightSpectrumPixel(spectroEx);
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
			spectroEx.close();
		}
	}

	static private void performSpectroMeasurement (SpectroEx device) {
		try {
			float integrationTime = promptIntegrationTime();
			int averageCount = promptAveragingCount();
			int stepWidth = promptStepWidth();

			System.out.println("Performing measurement...\n");

			// Start measurement.
			Result<Boolean> startResult = device.startLightMeasurement(integrationTime, averageCount);
			if (startResult.isError()) {
				System.out.println("Could not start measurement! Error: " + startResult);
				return;
			}

			// Wait for completion.
			boolean measuring = true;
			while (measuring) {
				Thread.sleep(100);
				Result<Boolean> statusResult = device.getMeasurementStatus();
				if (statusResult.isSuccess())
					measuring = statusResult.getValue();
				else {
					System.out.println("Could not determine measurement status! Error: " + statusResult);
					return;
				}
			}

			// Read the light spectrum (380-780nm, 1nm step size).
			Result<float[]> spectrumResult = device.getLightWaveData(380, 780, stepWidth);
			if (spectrumResult.isError())
				System.out.println("Could not read light spectrum! Error: " + spectrumResult);
			else {
				float[] spectrum = spectrumResult.getValue();
				for (int i = 0; i < spectrum.length; i++)
					System.out.println(String.format("wl[nm]: %d    cts: %.6f", i + 380, spectrum[i]));
			}
		} catch (Throwable ex) {
			System.err.println("Error during light spectrum measurement: " + ex.getMessage());
		}
	}

	/** Get serial numbers from the first device found. */
	static private void getDeviceInfo () {
		try {
			Result<DeviceSerials> serialsResult = SpectroEx.getDeviceSerials(0);
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
	static private void startMeasurement (SpectroEx device) {
		try {
			float integrationTime = promptIntegrationTime();
			int averageCount = promptAveragingCount();

			Result<Boolean> result = device.startLightMeasurement(integrationTime, averageCount);
			if (result.isError())
				System.out.println("Could not start measurement! Error: " + result);
			else
				System.out.println("Measurement successfully started.");
		} catch (Throwable ex) {
			System.err.println("Error starting measurement: " + ex.getMessage());
		}
	}

	/** Cancels an initiated measurement. */
	static private void breakMeasurement (SpectroEx device) {
		try {
			Result<Boolean> result = device.breakMeasurement();
			if (result.isError())
				System.out.println("Could not break measurement! Error: " + result);
			else
				System.out.println("Measurement cancelled.");
		} catch (Throwable ex) {
			System.err.println("Error breaking measurement: " + ex.getMessage());
		}
	}

	/** Returns the status of any current measurement. */
	static private void getMeasurementStatus (SpectroEx device) {
		try {
			Result<Boolean> result = device.getMeasurementStatus();
			if (result.isError())
				System.out.println("Could not determine measurement status! Error: " + result);
			else {
				if (result.getValue())
					System.out.println("Measurement in progress.");
				else
					System.out.println("No measurement in progress.");
			}
		} catch (Throwable ex) {
			System.err.println("Error getting measurement status: " + ex.getMessage());
		}
	}

	/** Read the light spectrum, 380-780nm. */
	static private void getLightSpectrumWavelength (SpectroEx device) {
		try {
			Result<float[]> result = device.getLightWaveData(380, 780, 1.0f);
			if (result.isError())
				System.out.println("Could not read light spectrum! Error: " + result);
			else {
				float[] spectrum = result.getValue();
				for (int i = 0; i < spectrum.length; i++)
					System.out.println(String.format("wl[nm]: %d    cts: %.6f", i + 380, spectrum[i]));
			}
		} catch (Throwable ex) {
			System.err.println("Error reading light spectrum wave data: " + ex.getMessage());
		}
	}

	/** Read the light spectrum, pixel based. */
	static private void getLightSpectrumPixel (SpectroEx device) {
		try {
			Result<Integer> pixelCountResult = device.getPixelCount();
			if (pixelCountResult.isError()) throw new RuntimeException("Could not get pixel count! Error: " + pixelCountResult);
			int pixelCount = pixelCountResult.getValue();

			Result<int[]> result = device.getLightPixelData(pixelCount);
			if (result.isError()) throw new RuntimeException("Could not read light spectrum! Error: " + result);
			int[] spectrum = result.getValue();
			for (int i = 0; i < spectrum.length; i++)
				System.out.println(String.format("pix: %d    cts: %d", i, spectrum[i]));
		} catch (Throwable ex) {
			System.err.println("Error reading light spectrum pixel data: " + ex.getMessage());
		}
	}
}
