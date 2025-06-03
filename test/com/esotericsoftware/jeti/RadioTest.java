
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.jeti.JetiSDK.AdaptationStatus;
import com.esotericsoftware.jeti.JetiSDK.CRI;
import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;
import com.esotericsoftware.jeti.JetiSDK.DominantWavelength;
import com.esotericsoftware.jeti.JetiSDK.UV;
import com.esotericsoftware.jeti.JetiSDK.XY;
import com.esotericsoftware.jeti.JetiSDK.XY10;
import com.esotericsoftware.jeti.JetiSDK.XYZ;

@DisplayName("JetiRadio Integration Tests")
public class RadioTest {
	private Radio radio;

	@BeforeEach
	void setUp () {
		JetiSDK.initialize();

		Result<Integer> deviceCount = Radio.getDeviceCount();
		assumeTrue(deviceCount.isSuccess() && deviceCount.getValue() > 0, "No radio devices available for testing " + deviceCount);

		Result<Radio> result = Radio.openDevice(0);
		assumeTrue(result.isSuccess(), "Could not open radio device " + result);

		radio = result.getValue();
	}

	@AfterEach
	void tearDown () {
		if (radio != null && !radio.isClosed()) radio.close();
	}

	@Test
	@DisplayName("Get device information")
	void testGetDeviceInfo () {
		Result<DeviceSerials> result = Radio.getDeviceSerials(0);
		if (result.isSuccess()) {
			DeviceSerials serials = result.getValue();
			assertNotNull(serials.electronics());
			assertNotNull(serials.spectrometer());
			assertNotNull(serials.device());
		}

		Result<DllVersion> versionResult = Radio.getDllVersion();
		assertTrue(versionResult.isSuccess(), versionResult.toString());
		assertNotNull(versionResult.getValue());
	}

	@Test
	@DisplayName("Perform basic measurement cycle")
	void testBasicMeasurementCycle () {
		// Start measurement
		Result<Boolean> result = radio.measure();
		assertTrue(result.isSuccess(), result.toString());

		// Check measurement status
		boolean measuring = true;
		int attempts = 0;
		while (measuring && attempts < 100) { // Timeout after 10 seconds
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
			}

			result = radio.getMeasurementStatus();
			assertTrue(result.isSuccess(), result.toString());
			measuring = result.getValue();
			attempts++;
		}

		assertFalse(measuring, "Measurement should complete within timeout");

		// Get measurement results
		Result<Float> floatResult = radio.getRadiometricValue();
		assertTrue(floatResult.isSuccess(), floatResult.toString());
		assertTrue(floatResult.getValue() >= 0);

		floatResult = radio.getPhotometricValue();
		assertTrue(floatResult.isSuccess(), floatResult.toString());
		assertTrue(floatResult.getValue() >= 0);
	}

	@Test
	@DisplayName("Get chromaticity and color values")
	void testChromaticityAndColorValues () {
		performMeasurementAndWait();

		// Get chromaticity XY
		Result<XY> xyResult = radio.getChromaXY();
		assertTrue(xyResult.isSuccess(), xyResult.toString());
		assertTrue(xyResult.getValue().x() >= 0 && xyResult.getValue().x() <= 1);
		assertTrue(xyResult.getValue().y() >= 0 && xyResult.getValue().y() <= 1);

		// Get chromaticity XY10
		Result<XY10> xy10Result = radio.getChromaXY10();
		assertTrue(xy10Result.isSuccess(), xy10Result.toString());

		// Get chromaticity UV
		Result<UV> uvResult = radio.getChromaUV();
		assertTrue(uvResult.isSuccess(), uvResult.toString());

		// Get XYZ values
		Result<XYZ> xyzResult = radio.getXYZ();
		assertTrue(xyzResult.isSuccess(), xyzResult.toString());

		// Get dominant wavelength and purity
		Result<DominantWavelength> dwlResult = radio.getDominantWavelength();
		assertTrue(dwlResult.isSuccess(), dwlResult.toString());

		// Get CCT
		Result<Float> cctResult = radio.getCCT();
		assertTrue(cctResult.isSuccess(), cctResult.toString());
		assertTrue(cctResult.getValue() > 0);

		// Get Duv
		Result<Float> duvResult = radio.getDuv();
		assertTrue(duvResult.isSuccess(), duvResult.toString());

		// Get CRI
		Result<CRI> criResult = radio.getCRI();
		assertTrue(criResult.isSuccess(), criResult.toString());
		assertTrue(criResult.getValue().ra() >= 0 && criResult.getValue().ra() <= 100);
	}

	@Test
	@DisplayName("Get spectral data")
	void testSpectralData () {
		performMeasurementAndWait();

		Result<float[]> result = radio.getSpectralRadiance();
		assertTrue(result.isSuccess(), result.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, result.getValue().length);

		// Check that spectral data contains reasonable values
		float[] spectralData = result.getValue();
		boolean hasPositiveValues = false;
		for (float value : spectralData) {
			if (value > 0) {
				hasPositiveValues = true;
				break;
			}
		}
		assertTrue(hasPositiveValues, "Spectral data should contain some positive values");
	}

	@Test
	@DisplayName("Get integration time")
	void testGetIntegrationTime () {
		Result<Float> result = radio.getIntegrationTime();
		assertTrue(result.isSuccess(), result.toString());
		assertTrue(result.getValue() > 0);
	}

	@Test
	@DisplayName("Set and get measurement distance")
	void testMeasurementDistance () {
		int distance = 10;

		Result<Boolean> setResult = radio.setMeasurementDistance(distance);
		// BOZO - Command not supported or invalid argument?
		assertTrue(setResult.isSuccess(), setResult.toString());

		Result<Integer> getResult = radio.getMeasurementDistance();
		assertTrue(getResult.isSuccess(), getResult.toString());
		assertEquals(distance, getResult.getValue());
	}

	@Test
	@DisplayName("Handle measurement with adaptation")
	void testMeasurementWithAdaptation () {
		Result<Boolean> result = radio.measureWithAdaptation();
		assertTrue(result.isSuccess(), result.toString());

		// Wait for adaptation to complete
		boolean adapting = true;
		int attempts = 0;
		while (adapting && attempts < 200) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
			}

			Result<AdaptationStatus> adaptResult = radio.getAdaptationStatus();
			assertTrue(adaptResult.isSuccess(), adaptResult.toString());
			adapting = !adaptResult.getValue().complete();
			attempts++;
		}

		assertFalse(adapting, "Adaptation should complete within timeout");

		// Get final adaptation status
		Result<AdaptationStatus> adaptResult = radio.getAdaptationStatus();
		assertTrue(adaptResult.isSuccess(), adaptResult.toString());
		AdaptationStatus status = adaptResult.getValue();
		assertTrue(status.complete());
	}

	@Test
	@DisplayName("Break measurement")
	void testBreakMeasurement () {
		// Start measurement
		Result<Boolean> result = radio.measure();
		assertTrue(result.isSuccess(), result.toString());

		// Immediately break it
		result = radio.breakMeasurement();
		assertTrue(result.isSuccess(), result.toString());

		try {
			Thread.sleep(250);
		} catch (InterruptedException ignored) {
		}

		// Check that measurement is no longer active
		result = radio.getMeasurementStatus();
		assertTrue(result.isSuccess(), result.toString());
		assertFalse(result.getValue(), result.toString());
	}

	@Test
	@DisplayName("Prepare measurement")
	void testPrepareMeasurement () {
		Result<Boolean> result = radio.prepareMeasurement();
		assertTrue(result.isSuccess(), result.toString());
	}

	private void performMeasurementAndWait () {
		Result<Boolean> result = radio.measure();
		assertTrue(result.isSuccess(), result.toString());

		boolean measuring = true;
		int attempts = 0;
		while (measuring && attempts < 100) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
			}

			result = radio.getMeasurementStatus();
			assertTrue(result.isSuccess(), result.toString());
			measuring = result.getValue();
			attempts++;
		}

		assertFalse(measuring, "Measurement should complete within timeout");

		try {
			Thread.sleep(100);
		} catch (InterruptedException ignored) {
		}
	}
}
