
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
public class JetiRadioTest {
	private JetiRadio radio;

	@BeforeEach
	void setUp () {
		JetiSDK.initialize();

		JetiResult<Integer> deviceCount = JetiRadio.getDeviceCount();
		assumeTrue(deviceCount.isSuccess() && deviceCount.getValue() > 0, "No radio devices available for testing " + deviceCount);

		JetiResult<JetiRadio> deviceResult = JetiRadio.openDevice(0);
		assumeTrue(deviceResult.isSuccess(), "Could not open radio device " + deviceResult);

		radio = deviceResult.getValue();
	}

	@AfterEach
	void tearDown () {
		if (radio != null && !radio.isClosed()) radio.close();
	}

	@Test
	@DisplayName("Get device information")
	void testGetDeviceInfo () {
		JetiResult<DeviceSerials> serialsResult = JetiRadio.getDeviceSerials(0);
		if (serialsResult.isSuccess()) {
			DeviceSerials serials = serialsResult.getValue();
			assertNotNull(serials.electronics());
			assertNotNull(serials.spectrometer());
			assertNotNull(serials.device());
		}

		JetiResult<DllVersion> versionResult = JetiRadio.getDllVersion();
		assertTrue(versionResult.isSuccess(), versionResult.toString());
		assertNotNull(versionResult.getValue());
	}

	@Test
	@DisplayName("Perform basic measurement cycle")
	void testBasicMeasurementCycle () {
		// Start measurement
		JetiResult<Boolean> measureResult = radio.measure();
		assertTrue(measureResult.isSuccess(), measureResult.toString());

		// Check measurement status
		boolean measuring = true;
		int attempts = 0;
		while (measuring && attempts < 100) { // Timeout after 10 seconds
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("Test interrupted");
			}

			JetiResult<Boolean> statusResult = radio.getMeasurementStatus();
			assertTrue(statusResult.isSuccess(), statusResult.toString());
			measuring = statusResult.getValue();
			attempts++;
		}

		assertFalse(measuring, "Measurement should complete within timeout");

		// Get measurement results
		JetiResult<Float> radioResult = radio.getRadiometricValue();
		assertTrue(radioResult.isSuccess(), radioResult.toString());
		assertTrue(radioResult.getValue() >= 0);

		JetiResult<Float> photoResult = radio.getPhotometricValue();
		assertTrue(photoResult.isSuccess(), photoResult.toString());
		assertTrue(photoResult.getValue() >= 0);
	}

	@Test
	@DisplayName("Get chromaticity and color values")
	void testChromaticityAndColorValues () {
		performMeasurementAndWait();

		// Get chromaticity XY
		JetiResult<XY> xyResult = radio.getChromaXY();
		assertTrue(xyResult.isSuccess(), xyResult.toString());
		assertTrue(xyResult.getValue().x() >= 0 && xyResult.getValue().x() <= 1);
		assertTrue(xyResult.getValue().y() >= 0 && xyResult.getValue().y() <= 1);

		// Get chromaticity XY10
		JetiResult<XY10> xy10Result = radio.getChromaXY10();
		assertTrue(xy10Result.isSuccess(), xy10Result.toString());

		// Get chromaticity UV
		JetiResult<UV> uvResult = radio.getChromaUV();
		assertTrue(uvResult.isSuccess(), uvResult.toString());

		// Get XYZ values
		JetiResult<XYZ> xyzResult = radio.getXYZ();
		assertTrue(xyzResult.isSuccess(), xyzResult.toString());

		// Get dominant wavelength and purity
		JetiResult<DominantWavelength> dwlResult = radio.getDominantWavelength();
		assertTrue(dwlResult.isSuccess(), dwlResult.toString());

		// Get CCT
		JetiResult<Float> cctResult = radio.getCCT();
		assertTrue(cctResult.isSuccess(), cctResult.toString());
		assertTrue(cctResult.getValue() > 0);

		// Get Duv
		JetiResult<Float> duvResult = radio.getDuv();
		assertTrue(duvResult.isSuccess(), duvResult.toString());

		// Get CRI
		JetiResult<CRI> criResult = radio.getCRI();
		assertTrue(criResult.isSuccess(), criResult.toString());
		assertTrue(criResult.getValue().ra() >= 0 && criResult.getValue().ra() <= 100);
	}

	@Test
	@DisplayName("Get spectral data")
	void testSpectralData () {
		performMeasurementAndWait();

		JetiResult<float[]> spectralResult = radio.getSpectralRadiance();
		assertTrue(spectralResult.isSuccess(), spectralResult.toString());
		assertEquals(JetiSDK.SPECTRUM_SIZE, spectralResult.getValue().length);

		// Check that spectral data contains reasonable values
		float[] spectralData = spectralResult.getValue();
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
		JetiResult<Float> tintResult = radio.getIntegrationTime();
		assertTrue(tintResult.isSuccess(), tintResult.toString());
		assertTrue(tintResult.getValue() > 0);
	}

	@Test
	@DisplayName("Should get and set measurement distance")
	void testMeasurementDistance () {
		int testDistance = 10;

		JetiResult<Boolean> setResult = radio.setMeasurementDistance(testDistance);
		// BOZO - Command not supported or invalid argument?
		assertTrue(setResult.isSuccess(), setResult.toString());

		JetiResult<Integer> getResult = radio.getMeasurementDistance();
		assertTrue(getResult.isSuccess(), getResult.toString());
		assertEquals(testDistance, getResult.getValue());
	}

	@Test
	@DisplayName("Handle measurement with adaptation")
	void testMeasurementWithAdaptation () {
		JetiResult<Boolean> measureResult = radio.measureWithAdaptation();
		assertTrue(measureResult.isSuccess(), measureResult.toString());

		// Wait for adaptation to complete
		boolean adapting = true;
		int attempts = 0;
		while (adapting && attempts < 200) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("Test interrupted");
			}

			JetiResult<AdaptationStatus> statusResult = radio.getAdaptationStatus();
			assertTrue(statusResult.isSuccess(), statusResult.toString());
			adapting = !statusResult.getValue().complete();
			attempts++;
		}

		assertFalse(adapting, "Adaptation should complete within timeout");

		// Get final adaptation status
		JetiResult<AdaptationStatus> finalStatus = radio.getAdaptationStatus();
		assertTrue(finalStatus.isSuccess(), finalStatus.toString());
		AdaptationStatus status = finalStatus.getValue();
		assertTrue(status.complete());
		// BOZO - Why are these 0?
		assertTrue(status.integrationTime() > 0);
		assertTrue(status.averageCount() > 0);
	}

	@Test
	@DisplayName("Break measurement")
	void testBreakMeasurement () {
		// Start measurement
		JetiResult<Boolean> measureResult = radio.measure();
		assertTrue(measureResult.isSuccess(), measureResult.toString());

		// Immediately break it
		JetiResult<Boolean> breakResult = radio.breakMeasurement();
		assertTrue(breakResult.isSuccess(), breakResult.toString());

		try {
			Thread.sleep(250);
		} catch (InterruptedException ex) {
		}

		// Check that measurement is no longer active
		JetiResult<Boolean> statusResult = radio.getMeasurementStatus();
		assertTrue(statusResult.isSuccess(), statusResult.toString());
		assertFalse(statusResult.getValue(), statusResult.toString());
	}

	@Test
	@DisplayName("Prepare measurement")
	void testPrepareMeasurement () {
		JetiResult<Boolean> prepareResult = radio.prepareMeasurement();
		assertTrue(prepareResult.isSuccess(), prepareResult.toString());
	}

	private void performMeasurementAndWait () {
		JetiResult<Boolean> measureResult = radio.measure();
		assertTrue(measureResult.isSuccess(), measureResult.toString());

		boolean measuring = true;
		int attempts = 0;
		while (measuring && attempts < 100) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("Test interrupted");
			}

			JetiResult<Boolean> statusResult = radio.getMeasurementStatus();
			assertTrue(statusResult.isSuccess(), statusResult.toString());
			measuring = statusResult.getValue();
			attempts++;
		}

		assertFalse(measuring, "Measurement should complete within timeout");

		try {
			Thread.sleep(100);
		} catch (InterruptedException ignored) {
		}
	}
}
