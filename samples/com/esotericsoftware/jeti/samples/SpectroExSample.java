
package com.esotericsoftware.jeti.samples;

import static com.esotericsoftware.jeti.JetiSDKTest.*;

import java.util.Scanner;

import com.esotericsoftware.jeti.JetiResult;
import com.esotericsoftware.jeti.JetiSDK;
import com.esotericsoftware.jeti.JetiSpectro;

public class SpectroExSample {
	static private final Scanner scanner = new Scanner(System.in);

	static public void main (String[] args) {
		JetiSpectro spectroDevice = null;

		try {
			// Initialize the SDK
			System.out.println("Initializing JETI SDK...");
			JetiSDK.initialize();

			// Connect to TCP device - use simplified approach
			System.out.printf("Connecting: %s%n", IP);

			// For TCP devices, try to open spectro device directly after setting up TCP connection
			// The JETI SDK should handle the TCP connection internally when we try to open devices
			JetiResult<Integer> numSpectroDevicesResult = JetiSDK.getNumberOfSpectroDevices();
			if (numSpectroDevicesResult.isError() || numSpectroDevicesResult.getValue() == 0) {
				System.out.printf("""
					No spectro devices found!
					This may mean:
					1. No device at IP %s
					2. Device not configured for TCP
					3. Network connectivity issue
					Error code: 0x%08X
					""", IP, numSpectroDevicesResult.getErrorCode());
				return;
			}

			System.out.printf("Spectro devices: %d%n", numSpectroDevicesResult.getValue());

			// Open the first found device (zero-based index)
			JetiResult<JetiSpectro> deviceResult = JetiSDK.openSpectroDevice(0);
			if (deviceResult.isError()) {
				System.out.printf("Could not open spectro device!%nError code: 0x%08X%n", deviceResult.getErrorCode());
				return;
			}

			spectroDevice = deviceResult.getValue();
			System.out.printf("Connected: %s%n", IP);

			char choice;
			do {
				System.out.println("""

					Select:
					--------------
					1) perform light measurement...
					2) get device information...
					3) perform all spectrum measurements...

					Single operations:
					------------------
					a) measure dark spectrum...
					b) measure light spectrum...
					c) measure reference spectrum...
					d) measure transmission/reflection spectrum...
					e) calculate transmittance...
					f) calculate absorbance...
					0) exit
					""");

				String input = scanner.nextLine().trim();
				choice = input.isEmpty() ? '0' : input.charAt(0);

				switch (choice) {
				case '1':
					performLightMeasurement(spectroDevice);
					break;
				case '2':
					getDeviceInformation();
					break;
				case '3':
					performAllSpectraMeasurement(spectroDevice);
					break;
				case 'a':
					measureDarkSpectrum(spectroDevice);
					break;
				case 'b':
					measureLightSpectrum(spectroDevice);
					break;
				case 'c':
					measureReferenceSpectrum(spectroDevice);
					break;
				case 'd':
					measureTransmissionReflectionSpectrum(spectroDevice);
					break;
				case 'e':
					calculateTransmittance(spectroDevice);
					break;
				case 'f':
					calculateAbsorbance(spectroDevice);
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
			if (spectroDevice != null && !spectroDevice.isClosed()) spectroDevice.close();
		}
	}

	static private float getIntegrationTime () {
		System.out.print("Please enter integration time in ms (0 for auto): ");
		String input = scanner.nextLine().trim();
		try {
			float time = Float.parseFloat(input);
			return time <= 0 ? 100.0f : time; // Default to 100ms if 0 or invalid
		} catch (NumberFormatException e) {
			System.out.printf("'%s' is invalid, using 100ms default%n", input);
			return 100.0f;
		}
	}

	static private void performLightMeasurement (JetiSpectro device) {
		try {
			float integrationTime = getIntegrationTime();

			System.out.println("Performing light measurement. Please wait...\n");

			JetiResult<float[]> result = device.measureLightSpectrum(integrationTime);
			if (result.isError())
				System.out.printf("Could not perform light measurement!%nError code: 0x%08X%n", result.getErrorCode());
			else {
				float[] spectrum = result.getValue();
				System.out.printf("Light spectrum measured (%d data points)%n", spectrum.length);

				// Show first few and last few values
				System.out.println("Sample spectrum data:");
				for (int i = 0; i < Math.min(5, spectrum.length); i++)
					System.out.printf("Point %d: %.2f%n", i, spectrum[i]);
				if (spectrum.length > 10) {
					System.out.println("...");
					for (int i = spectrum.length - 5; i < spectrum.length; i++)
						System.out.printf("Point %d: %.2f%n", i, spectrum[i]);
				}
			}
		} catch (Throwable ex) {
			System.err.println("Error during light measurement: " + ex.getMessage());
		}
	}

	static private void performAllSpectraMeasurement (JetiSpectro device) {
		try {
			float integrationTime = getIntegrationTime();

			System.out.println("Performing all spectrum measurements. Please wait...\n");

			JetiResult<JetiSpectro.SpectroscopicData> result = device.measureAllSpectra(integrationTime);
			if (result.isError())
				System.out.printf("Could not perform spectrum measurements!%nError code: 0x%08X%n", result.getErrorCode());
			else {
				JetiSpectro.SpectroscopicData data = result.getValue();
				System.out.printf("""
					All spectrum measurements completed:
					- Dark spectrum: %d points
					- Light spectrum: %d points
					- Reference spectrum: %d points
					- Transmission/Reflection spectrum: %d points
					- Integration time: %.1f ms
					""", data.getDarkSpectrum().length, data.getLightSpectrum().length, data.getReferenceSpectrum().length,
					data.getTransmissionReflectionSpectrum().length, data.getIntegrationTime());

				// Calculate and show transmittance
				JetiResult<float[]> transmittanceResult = device.calculateTransmittance(data.getLightSpectrum(),
					data.getDarkSpectrum(), data.getReferenceSpectrum());
				if (transmittanceResult.isSuccess()) {
					float[] transmittance = transmittanceResult.getValue();
					float avgTransmittance = 0;
					for (float t : transmittance)
						avgTransmittance += t;
					avgTransmittance /= transmittance.length;
					System.out.printf("- Average transmittance: %.4f%n", avgTransmittance);
				}
			}
		} catch (Throwable ex) {
			System.err.println("Error during spectrum measurements: " + ex.getMessage());
		}
	}

	static private void getDeviceInformation () {
		try {
			// Display TCP device information
			System.out.printf("""
				TCP Device IP: %s
				Connection Type: TCP
				""", IP);

			// Try to get device serials for additional info
			JetiResult<String[]> serialsResult = JetiSpectro.getSpectroDeviceSerials(0);
			if (serialsResult.isError())
				System.out.printf("""
					Could not get device serial information (normal for TCP devices)
					Error code: 0x%08X
					""", serialsResult.getErrorCode());
			else {
				String[] serials = serialsResult.getValue();
				System.out.printf("""
					Electronics serial number: %s
					Spectrometer serial number: %s
					Device serial number: %s
					""", serials[0], serials[1], serials[2]);
			}
		} catch (Throwable ex) {
			System.err.println("Error getting device info: " + ex.getMessage());
		}
	}

	static private void measureDarkSpectrum (JetiSpectro device) {
		try {
			float integrationTime = getIntegrationTime();

			System.out.println("Measuring dark spectrum. Please wait...");
			JetiResult<float[]> result = device.measureDarkSpectrum(integrationTime);
			if (result.isError())
				System.out.printf("Could not measure dark spectrum!%nError code: 0x%08X%n", result.getErrorCode());
			else {
				float[] spectrum = result.getValue();
				System.out.printf("Dark spectrum measured (%d data points)%n", spectrum.length);
			}
		} catch (Throwable ex) {
			System.err.println("Error measuring dark spectrum: " + ex.getMessage());
		}
	}

	static private void measureLightSpectrum (JetiSpectro device) {
		try {
			float integrationTime = getIntegrationTime();

			System.out.println("Measuring light spectrum. Please wait...");
			JetiResult<float[]> result = device.measureLightSpectrum(integrationTime);
			if (result.isError())
				System.out.printf("Could not measure light spectrum!%nError code: 0x%08X%n", result.getErrorCode());
			else {
				float[] spectrum = result.getValue();
				System.out.printf("Light spectrum measured (%d data points)%n", spectrum.length);

				// Show some sample data
				System.out.println("Sample spectrum values:");
				for (int i = 0; i < Math.min(10, spectrum.length); i++)
					System.out.printf("  Point %d: %.2f%n", i, spectrum[i]);
			}
		} catch (Throwable ex) {
			System.err.println("Error measuring light spectrum: " + ex.getMessage());
		}
	}

	static private void measureReferenceSpectrum (JetiSpectro device) {
		try {
			float integrationTime = getIntegrationTime();

			System.out.println("Measuring reference spectrum. Please wait...");
			JetiResult<float[]> result = device.measureReferenceSpectrum(integrationTime);
			if (result.isError())
				System.out.printf("Could not measure reference spectrum!%nError code: 0x%08X%n", result.getErrorCode());
			else {
				float[] spectrum = result.getValue();
				System.out.printf("Reference spectrum measured (%d data points)%n", spectrum.length);
			}
		} catch (Throwable ex) {
			System.err.println("Error measuring reference spectrum: " + ex.getMessage());
		}
	}

	static private void measureTransmissionReflectionSpectrum (JetiSpectro device) {
		try {
			float integrationTime = getIntegrationTime();

			System.out.println("Measuring transmission/reflection spectrum. Please wait...");
			JetiResult<float[]> result = device.measureTransmissionReflectionSpectrum(integrationTime);
			if (result.isError())
				System.out.printf("Could not measure transmission/reflection spectrum!%nError code: 0x%08X%n", result.getErrorCode());
			else {
				float[] spectrum = result.getValue();
				System.out.printf("Transmission/reflection spectrum measured (%d data points)%n", spectrum.length);
			}
		} catch (Throwable ex) {
			System.err.println("Error measuring transmission/reflection spectrum: " + ex.getMessage());
		}
	}

	static private void calculateTransmittance (JetiSpectro device) {
		try {
			float integrationTime = getIntegrationTime();

			System.out.println("Measuring spectra for transmittance calculation...");

			// Measure all required spectra
			JetiResult<float[]> darkResult = device.measureDarkSpectrum(integrationTime);
			if (darkResult.isError()) {
				System.out.printf("Could not measure dark spectrum!%nError code: 0x%08X%n", darkResult.getErrorCode());
				return;
			}

			JetiResult<float[]> lightResult = device.measureLightSpectrum(integrationTime);
			if (lightResult.isError()) {
				System.out.printf("Could not measure light spectrum!%nError code: 0x%08X%n", lightResult.getErrorCode());
				return;
			}

			JetiResult<float[]> refResult = device.measureReferenceSpectrum(integrationTime);
			if (refResult.isError()) {
				System.out.printf("Could not measure reference spectrum!%nError code: 0x%08X%n", refResult.getErrorCode());
				return;
			}

			// Calculate transmittance
			JetiResult<float[]> transmittanceResult = device.calculateTransmittance(lightResult.getValue(), darkResult.getValue(),
				refResult.getValue());

			if (transmittanceResult.isError())
				System.out.printf("Could not calculate transmittance!%nError code: 0x%08X%n", transmittanceResult.getErrorCode());
			else {
				float[] transmittance = transmittanceResult.getValue();
				System.out.printf("Transmittance calculated (%d data points)%n", transmittance.length);

				// Show some sample transmittance values
				System.out.println("Sample transmittance values:");
				for (int i = 0; i < Math.min(10, transmittance.length); i++)
					System.out.printf("  Point %d: %.4f%n", i, transmittance[i]);

				// Calculate average transmittance
				float avgTransmittance = 0;
				for (float t : transmittance)
					avgTransmittance += t;
				avgTransmittance /= transmittance.length;
				System.out.printf("Average transmittance: %.4f%n", avgTransmittance);
			}
		} catch (Throwable ex) {
			System.err.println("Error calculating transmittance: " + ex.getMessage());
		}
	}

	static private void calculateAbsorbance (JetiSpectro device) {
		try {
			float integrationTime = getIntegrationTime();

			System.out.println("Measuring spectra for absorbance calculation...");

			// Measure all required spectra
			JetiResult<float[]> darkResult = device.measureDarkSpectrum(integrationTime);
			if (darkResult.isError()) {
				System.out.printf("Could not measure dark spectrum!%nError code: 0x%08X%n", darkResult.getErrorCode());
				return;
			}

			JetiResult<float[]> lightResult = device.measureLightSpectrum(integrationTime);
			if (lightResult.isError()) {
				System.out.printf("Could not measure light spectrum!%nError code: 0x%08X%n", lightResult.getErrorCode());
				return;
			}

			JetiResult<float[]> refResult = device.measureReferenceSpectrum(integrationTime);
			if (refResult.isError()) {
				System.out.printf("Could not measure reference spectrum!%nError code: 0x%08X%n", refResult.getErrorCode());
				return;
			}

			// Calculate absorbance
			JetiResult<float[]> absorbanceResult = device.calculateAbsorbance(lightResult.getValue(), darkResult.getValue(),
				refResult.getValue());

			if (absorbanceResult.isError())
				System.out.printf("Could not calculate absorbance!%nError code: 0x%08X%n", absorbanceResult.getErrorCode());
			else {
				float[] absorbance = absorbanceResult.getValue();
				System.out.printf("Absorbance calculated (%d data points)%n", absorbance.length);

				// Show some sample absorbance values
				System.out.println("Sample absorbance values:");
				for (int i = 0; i < Math.min(10, absorbance.length); i++)
					if (Float.isFinite(absorbance[i]))
						System.out.printf("  Point %d: %.4f%n", i, absorbance[i]);
					else
						System.out.printf("  Point %d: Infinite%n", i);

				// Calculate average absorbance (excluding infinite values)
				float avgAbsorbance = 0;
				int validPoints = 0;
				for (float a : absorbance)
					if (Float.isFinite(a)) {
						avgAbsorbance += a;
						validPoints++;
					}
				if (validPoints > 0) {
					avgAbsorbance /= validPoints;
					System.out.printf("Average absorbance: %.4f (based on %d valid points)%n", avgAbsorbance, validPoints);
				}
			}
		} catch (Throwable ex) {
			System.err.println("Error calculating absorbance: " + ex.getMessage());
		}
	}
}
