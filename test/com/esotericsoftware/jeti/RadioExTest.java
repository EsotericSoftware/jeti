
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.jeti.JetiSDK.AdaptationStatus;
import com.esotericsoftware.jeti.JetiSDK.CRI;
import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.PeakFWHM;
import com.esotericsoftware.jeti.JetiSDK.TM30;

public class RadioExTest extends JetiTest {
	private RadioEx radioEx;

	@BeforeEach
	void setUp () {
		int deviceCount = RadioEx.getDeviceCount();
		assumeTrue(deviceCount > 0, "No radio ex devices available for testing");

		radioEx = RadioEx.openDevice(0);
	}

	@AfterEach
	void tearDown () {
		if (radioEx != null && !radioEx.isClosed()) radioEx.close();
	}

	@Test
	@DisplayName("Get device information")
	void testGetDeviceInfo () {
		DeviceSerials serials = RadioEx.getDeviceSerials(0);
		assertNotNull(serials.electronics());
		assertNotNull(serials.spectrometer());
		assertNotNull(serials.device());

		assertNotNull(RadioEx.getDllVersion());
	}

	@Test
	@DisplayName("Perform measurement with parameters")
	void testMeasurementWithParameters () {
		float integrationTime = 100.0f;
		int averageCount = 3;
		int stepWidth = 5;
		radioEx.measure(integrationTime, averageCount, stepWidth);
		waitForMeasurementCompletion();
		assertTrue(radioEx.getRadiometricValue(380, 780) >= 0);
		assertTrue(radioEx.getPhotometricValue() >= 0);
	}

	@Test
	@DisplayName("Get spectral data with wavelength range")
	void testSpectralDataWithRange () {
		int step = 5;
		performMeasurementAndWait(0, 1, step);
		int beginWavelength = 400;
		int endWavelength = 700;
		int expectedSize = (endWavelength - beginWavelength) / step + 1;
		float[] spectralData = radioEx.getSpectralRadiance(beginWavelength, endWavelength, step);
		assertEquals(expectedSize, spectralData.length);

		// Test high resolution spectral data
		expectedSize = (int)((endWavelength - beginWavelength) / 0.1f + 1);
		float[] hiResData = radioEx.getSpectralRadianceHiRes(beginWavelength, endWavelength);
		assertEquals(expectedSize, hiResData.length);
	}

	@Test
	@DisplayName("Get color rendering index with CCT")
	void testCRIwithCCT () {
		performMeasurementAndWait(0, 1, 5);
		float cct = radioEx.getCCT();
		CRI criData = radioEx.getCRI(cct);
		assertNotNull(criData);
		assertTrue(criData.ra() >= 0 && criData.ra() <= 100);
		assertEquals(15, criData.samples().length);
		for (float index : criData.samples())
			assertTrue(index >= -100 && index <= 100, "CRI special index should be between -100 and 100, got: " + index);
	}

	@Test
	@DisplayName("Get TM30 data")
	void testTM30 () {
		radioEx.measureWithAdaptation(1, 1); // 1nm step required for TM-30.
		while (radioEx.getAdaptationStatus().measuring())
			sleep(100);

		TM30 tm30 = radioEx.getTM30(false);
		assertTrue(tm30.rf() >= 0 && tm30.rf() <= 200);
		assertTrue(tm30.rg() >= 0 && tm30.rg() <= 200);
		assertEquals(16, tm30.hueAngleBins().length);
		assertEquals(99, tm30.colorSamples().length);

		TM30 tm30_15 = radioEx.getTM30(true);
		assertTrue(tm30_15.rf() >= 0 && tm30_15.rf() <= 200);
		assertTrue(tm30_15.rg() >= 0 && tm30_15.rg() <= 200);
		assertEquals(15, tm30_15.hueAngleBins().length);
		assertEquals(99, tm30_15.colorSamples().length);
	}

	@Test
	@DisplayName("Get peak FWHM data")
	void testPeakFWHMData () {
		performMeasurementAndWait(100.0f, 1, 1);

		float threshold = 0.5f;
		PeakFWHM data = radioEx.getPeakFWHM(threshold);
		assertTrue(data.peak() >= 380 && data.peak() <= 780, "Peak wavelength should be in visible range: " + data.peak());
		assertTrue(data.fwhm() > 0, "FWHM should be positive: " + data.fwhm());
	}

	@Test
	@DisplayName("Get blue measurement data")
	void testBlueMeasurementData () {
		performMeasurementAndWait(100.0f, 1, 5);
		assertNotNull(radioEx.getBlueMeasurement());
	}

	@Test
	@DisplayName("Save spectral data to files")
	void testSaveSpectralData () {
		performMeasurementAndWait(100.0f, 1, 1);
		int beginWavelength = 380;
		int endWavelength = 780;
		String operator = "Test Operator";
		String memo = "Integration test measurement";
		String tempDir = System.getProperty("java.io.tmpdir");
		String spcPath = new File(tempDir, "test_measurement.spc").getAbsolutePath();
		String csvPath = new File(tempDir, "test_measurement.csv").getAbsolutePath();

		// BOZO - Internal DLL error?
		radioEx.saveSpectralRadianceSPC(beginWavelength, endWavelength, spcPath, operator, memo);

		radioEx.saveSpectralRadianceCSV(beginWavelength, endWavelength, csvPath, operator, memo);

		new File(spcPath).delete();
		new File(csvPath).delete();
	}

	@Test
	@DisplayName("Perform measurement with adaptation")
	void testMeasurementWithAdaptation () {
		int averageCount = 1;
		int stepWidth = 5;
		radioEx.measureWithAdaptation(averageCount, stepWidth);

		int attempts = 0;
		while (radioEx.getAdaptationStatus().measuring() && attempts++ < 100) {
			System.out.print(".");
			sleep(100);
		}
		System.out.println();
		assertFalse(attempts >= 100, "Measurement should complete within timeout");

		AdaptationStatus status = radioEx.getAdaptationStatus();
		assertFalse(status.measuring(), status.toString());
	}

	@Test
	@DisplayName("Prepare measurement with parameters")
	void testPrepareMeasurementWithParameters () {
		float integrationTime = 150.0f;
		int averageCount = 5;
		int stepWidth = 1;
		radioEx.prepareMeasurement(integrationTime, averageCount, stepWidth);
	}

	@Test
	@DisplayName("Set and get measurement distance")
	void testMeasurementDistance () {
		int testDistance = 200;

		// BOZO - Not supported?
		radioEx.setMeasurementDistance(testDistance);

		int distance = radioEx.getMeasurementDistance();
		assertEquals(testDistance, distance);
	}

	@Test
	@DisplayName("Get integration time")
	void testGetIntegrationTime () {
		float integrationTime = radioEx.getIntegrationTime();
		assertTrue(integrationTime > 0);
	}

	@Test
	@DisplayName("Get chromaticity values")
	void testChromaticityValues () {
		performMeasurementAndWait(100, 1, 5);
		assertNotNull(radioEx.getChromaXY());
		assertNotNull(radioEx.getChromaXY10());
		assertNotNull(radioEx.getChromaUV());
		assertNotNull(radioEx.getXYZ());
		assertNotNull(radioEx.getDominantWavelength());
	}

	@Test
	@DisplayName("Cancel measurement")
	void testcancelMeasurement () {
		radioEx.measure(100.0f, 3, 5);
		radioEx.cancelMeasurement();
		assertFalse(radioEx.isMeasuring());
	}

	@Test
	@DisplayName("Test all RadioEx functions for coverage")
	void testAllRadioExFunctions () {
		radioEx.measureWithAdaptation(1, 5);
		radioEx.cancelMeasurement();
		assertNotNull(radioEx.getAdaptationStatus());
		performMeasurementAndWait(100.0f, 1, 5);
		assertNotNull(radioEx.getDuv());
		assertTrue(radioEx.getCCT() >= 0);
	}

	private void performMeasurementAndWait (float integrationTime, int averageCount, int stepWidth) {
		radioEx.measure(integrationTime, averageCount, stepWidth);
		waitForMeasurementCompletion();
	}

	private void waitForMeasurementCompletion () {
		int attempts = 0;
		while (radioEx.isMeasuring() && attempts++ < 100) {
			System.out.print(".");
			sleep(100);
		}
		System.out.println();
		assertFalse(attempts >= 100, "Measurement should complete within timeout");
	}
}
