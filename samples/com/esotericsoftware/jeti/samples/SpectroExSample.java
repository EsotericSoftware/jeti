
package com.esotericsoftware.jeti.samples;

import static com.esotericsoftware.jeti.samples.RadioExSample.*;

import java.util.Scanner;

import com.esotericsoftware.jeti.JetiResult;
import com.esotericsoftware.jeti.JetiSDK;
import com.esotericsoftware.jeti.JetiSpectroEx;

public class SpectroExSample {
	static private final Scanner scanner = new Scanner(System.in);

	static public void main (String[] args) {
		JetiSpectroEx spectroDevice = null;

		try {
			System.out.println("Initializing the JETI SDK...");
			JetiSDK.initialize();

			System.out.println("Searching for devices...");
			JetiResult<Integer> deviceCount = JetiSDK.getNumberOfSpectroExDevices();
			if (deviceCount.isError() || deviceCount.getValue() == 0) {
				System.out.printf("No spectro ex devices found! Error code: 0x%08X", deviceCount.getErrorCode());
				return;
			}
			System.out.printf("Spectro ex devices: %d%n", deviceCount.getValue());

			JetiResult<JetiSpectroEx> deviceResult = JetiSDK.openSpectroExDevice(0);
			if (deviceResult.isError()) {
				System.out.printf("Could not open spectro ex device!%nError code: 0x%08X%n", deviceResult.getErrorCode());
				return;
			}

			spectroDevice = deviceResult.getValue();
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
				case '1' -> performSpectroMeasurement(spectroDevice);
				case '2' -> getDeviceInfo();
				case 'a' -> startMeasurement(spectroDevice);
				case 'b' -> breakMeasurement(spectroDevice);
				case 'c' -> getMeasurementStatus(spectroDevice);
				case 'd' -> getSpectrumWavelength(spectroDevice);
				case 'e' -> getSpectrumPixel(spectroDevice);
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
			if (spectroDevice != null && !spectroDevice.isClosed()) spectroDevice.close();
		}
	}

	static private void performSpectroMeasurement (JetiSpectroEx device) {
		try {
			float integrationTime = promptIntegrationTime();
			int averageCount = promptAveragingCount();
			int stepWidth = promptStepWidth();

			System.out.println("Performing measurement...\n");

			// Start measurement.
			JetiResult<Boolean> startResult = device.startLightMeasurement(integrationTime, averageCount);
			if (startResult.isError()) {
				System.out.printf("Could not start measurement!%nError code: 0x%08X%n", startResult.getErrorCode());
				return;
			}

			// Wait for completion.
			boolean measuring = true;
			while (measuring) {
				Thread.sleep(100);
				JetiResult<Boolean> statusResult = device.getSpectroStatus();
				if (statusResult.isSuccess())
					measuring = statusResult.getValue();
				else {
					System.out.printf("Could not determine measurement status!%nError code: 0x%08X%n", statusResult.getErrorCode());
					return;
				}
			}

			// Read the light spectrum (380-780nm, 1nm step size).
			JetiResult<float[]> spectrumResult = device.getLightWaveData(380, 780, stepWidth);
			if (spectrumResult.isError())
				System.out.printf("Could not read light spectrum!%nError code: 0x%08X%n", spectrumResult.getErrorCode());
			else {
				float[] spectrum = spectrumResult.getValue();
				for (int i = 0; i < spectrum.length; i++)
					System.out.printf("wl[nm]: %d    cts: %.6f%n", i + 380, spectrum[i]);
			}
		} catch (Throwable ex) {
			System.err.println("Error during spectrum measurement: " + ex.getMessage());
		}
	}

	/** Get serial numbers from the first device found. */
	static private void getDeviceInfo () {
		try {
			JetiResult<String[]> serialsResult = JetiSpectroEx.getSpectroExDeviceSerials(0);
			if (serialsResult.isError())
				System.out.printf("Could not get serial numbers!%nError code: 0x%08X%n", serialsResult.getErrorCode());
			else {
				String[] serials = serialsResult.getValue();
				System.out.printf("electronics serial number: %s%nspectrometer serial number: %s%ndevice serial number: %s%n",
					serials[0], serials[1], serials[2]);
			}
		} catch (Throwable ex) {
			System.err.println("Error getting device info: " + ex.getMessage());
		}
	}

	/** Start a new measurement. */
	static private void startMeasurement (JetiSpectroEx device) {
		try {
			float integrationTime = promptIntegrationTime();
			int averageCount = promptAveragingCount();

			JetiResult<Boolean> result = device.startLightMeasurement(integrationTime, averageCount);
			if (result.isError())
				System.out.printf("Could not start measurement!%nError code: 0x%08X%n", result.getErrorCode());
			else
				System.out.println("Measurement successfully started.");
		} catch (Throwable ex) {
			System.err.println("Error starting measurement: " + ex.getMessage());
		}
	}

	/** Cancels an initiated measurement. */
	static private void breakMeasurement (JetiSpectroEx device) {
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
	static private void getMeasurementStatus (JetiSpectroEx device) {
		try {
			JetiResult<Boolean> result = device.getSpectroStatus();
			if (result.isError())
				System.out.printf("Could not determine measurement status!%nError code: 0x%08X%n", result.getErrorCode());
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
	static private void getSpectrumWavelength (JetiSpectroEx device) {
		try {
			JetiResult<float[]> result = device.getLightWaveData(380, 780, 1.0f);
			if (result.isError())
				System.out.printf("Could not read light spectrum!%nError code: 0x%08X%n", result.getErrorCode());
			else {
				float[] spectrum = result.getValue();
				for (int i = 0; i < spectrum.length; i++)
					System.out.printf("wl[nm]: %d    cts: %.6f%n", i + 380, spectrum[i]);
			}
		} catch (Throwable ex) {
			System.err.println("Error reading spectrum wave data: " + ex.getMessage());
		}
	}

	/** Read the light spectrum, pixel based. */
	static private void getSpectrumPixel (JetiSpectroEx device) {
		try {
			JetiResult<int[]> result = device.getLightPixelData();
			if (result.isError())
				System.out.printf("Could not read light spectrum!%nError code: 0x%08X%n", result.getErrorCode());
			else {
				int[] spectrum = result.getValue();
				for (int i = 0; i < spectrum.length; i++)
					System.out.printf("pix: %d    cts: %d%n", i, spectrum[i]);
			}
		} catch (Throwable ex) {
			System.err.println("Error reading spectrum pixel data: " + ex.getMessage());
		}
	}
}
