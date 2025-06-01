
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.jeti.JetiRadioEx.BlueMeasurementData;
import com.esotericsoftware.jeti.JetiRadioEx.CRIData;
import com.esotericsoftware.jeti.JetiRadioEx.PeakFWHMData;
import com.esotericsoftware.jeti.JetiRadioEx.TM30Data;

@DisplayName("JetiRadioEx Integration Tests")
public class JetiRadioExIntegrationTest {
	private JetiRadioEx radioEx;

	@BeforeEach
	void setUp () {
		JetiSDK.initialize();

		JetiResult<Integer> deviceCount = JetiRadioEx.getRadioExDeviceCount();
		assumeTrue(deviceCount.isSuccess() && deviceCount.getValue() > 0, "No radio ex devices available for testing");

		JetiResult<JetiRadioEx> deviceResult = JetiRadioEx.openRadioExDevice(0);
		assumeTrue(deviceResult.isSuccess(), "Could not open radio ex device");

		radioEx = deviceResult.getValue();
	}

	@AfterEach
	void tearDown () {
		if (radioEx != null && !radioEx.isClosed()) radioEx.close();
	}

	@Test
	@DisplayName("Get device information")
	void testGetDeviceInfo () {
		JetiResult<String[]> serialsResult = JetiRadioEx.getRadioExDeviceSerials(0);
		if (serialsResult.isSuccess()) {
			String[] serials = serialsResult.getValue();
			assertEquals(3, serials.length);
			assertNotNull(serials[0]); // Board serial
			assertNotNull(serials[1]); // Spectrometer serial
			assertNotNull(serials[2]); // Device serial
		}

		JetiResult<String> versionResult = JetiRadioEx.getRadioExDllVersion();
		assertTrue(versionResult.isSuccess(), versionResult.toString());
		assertNotNull(versionResult.getValue());
		assertTrue(versionResult.getValue().contains("."));
	}

	@Test
	@DisplayName("Perform measurement with parameters")
	void testMeasurementWithParameters () {
		float integrationTime = 100.0f;
		int averageCount = 3;
		int stepWidth = 5;

		JetiResult<Boolean> measureResult = radioEx.measure(integrationTime, averageCount, stepWidth);
		assertTrue(measureResult.isSuccess(), measureResult.toString());

		// Wait for measurement to complete
		waitForMeasurementCompletion();

		// Get radiometric value with wavelength range
		JetiResult<Float> radioResult = radioEx.getRadiometricValue(380, 780);
		assertTrue(radioResult.isSuccess(), radioResult.toString());
		assertTrue(radioResult.getValue() >= 0);

		// Get photometric value
		JetiResult<Float> photoResult = radioEx.getPhotometricValue();
		assertTrue(photoResult.isSuccess(), photoResult.toString());
		assertTrue(photoResult.getValue() >= 0);
	}

	@Test
	@DisplayName("Get spectral data with wavelength range")
	void testSpectralDataWithRange () {
		int step = 5;
		performMeasurementAndWait(100.0f, 1, step);

		int beginWavelength = 400;
		int endWavelength = 700;
		int expectedSize = (endWavelength - beginWavelength) / step + 1;
		JetiResult<float[]> spectralResult = radioEx.getSpectralRadiance(beginWavelength, endWavelength, step);
		assertTrue(spectralResult.isSuccess(), spectralResult.toString());
		assertEquals(expectedSize, spectralResult.getValue().length);

		// Test high resolution spectral data
		expectedSize = (int)((endWavelength - beginWavelength) / 0.1f + 1);
		JetiResult<float[]> hiResResult = radioEx.getSpectralRadianceHiRes(beginWavelength, endWavelength);
		assertTrue(hiResResult.isSuccess(), hiResResult.toString());
		assertEquals(expectedSize, hiResResult.getValue().length);
	}

	@Test
	@DisplayName("Get color rendering index with CCT")
	void testColorRenderingIndexWithCCT () {
		performMeasurementAndWait(100.0f, 1, 5);

		JetiResult<Float> cctResult = radioEx.getCorrelatedColorTemperature();
		assertTrue(cctResult.isSuccess(), cctResult.toString());
		float cct = cctResult.getValue();

		// Get CRI data using the measured CCT
		JetiResult<CRIData> criResult = radioEx.getColorRenderingIndex(cct);
		assertTrue(criResult.isSuccess(), criResult.toString());

		CRIData criData = criResult.getValue();
		assertNotNull(criData);
		assertTrue(criData.ra() >= 0 && criData.ra() <= 100);
		assertEquals(15, criData.specialIndices().length);

		// Check that special indices are reasonable
		for (float index : criData.specialIndices()) {
			assertTrue(index >= -100 && index <= 100, "CRI special index should be between -100 and 100, got: " + index);
		}
	}

	@Test
	@DisplayName("Get TM30 data")
	void testTM30Data () {
		performMeasurementAndWait(100.0f, 1, 5);

		// Test both TM30-15 and TM30-18
		JetiResult<TM30Data> tm30_15Result = radioEx.getTM30(true);
		if (tm30_15Result.isSuccess()) {
			TM30Data tm30Data = tm30_15Result.getValue();
			assertTrue(tm30Data.rf() >= 0 && tm30Data.rf() <= 200);
			assertTrue(tm30Data.rg() >= 0 && tm30Data.rg() <= 200);
			assertEquals(15, tm30Data.rfi().length);
			assertEquals(99, tm30Data.rfces().length);
		}

		JetiResult<TM30Data> tm30_18Result = radioEx.getTM30(false);
		if (tm30_18Result.isSuccess()) {
			TM30Data tm30Data = tm30_18Result.getValue();
			assertTrue(tm30Data.rf() >= 0 && tm30Data.rf() <= 200);
			assertTrue(tm30Data.rg() >= 0 && tm30Data.rg() <= 200);
		}
	}

	@Test
	@DisplayName("Get peak FWHM data")
	void testPeakFWHMData () {
		performMeasurementAndWait(100.0f, 1, 1);

		float threshold = 0.5f;
		JetiResult<PeakFWHMData> result = radioEx.getPeakFWHM(threshold);
		if (result.isSuccess()) {
			PeakFWHMData data = result.getValue();
			assertTrue(data.peak() >= 380 && data.peak() <= 780, "Peak wavelength should be in visible range: " + data.peak());
			assertTrue(data.fwhm() > 0, "FWHM should be positive: " + data.fwhm());
		}
	}

	@Test
	@DisplayName("Get blue measurement data")
	void testBlueMeasurementData () {
		performMeasurementAndWait(100.0f, 1, 5);

		JetiResult<BlueMeasurementData> result = radioEx.getBlueMeasurement();
		if (result.isSuccess()) {
			BlueMeasurementData data = result.getValue();
			// Blue measurement values can vary widely, just check they're finite
			assertTrue(Float.isFinite(data.lb()));
			assertTrue(Float.isFinite(data.kbv()));
			assertTrue(Float.isFinite(data.kc()));
			assertTrue(Float.isFinite(data.rbpfs()));
			assertTrue(Float.isFinite(data.rlbtb()));
			assertTrue(Float.isFinite(data.rnbpbp()));
		}
	}

	@Test
	@DisplayName("Save spectral data to files")
	void testSaveSpectralData () {
		performMeasurementAndWait(100.0f, 1, 1);

		int beginWavelength = 380;
		int endWavelength = 780;
		String operator = "Test Operator";
		String memo = "Integration test measurement";

		// Test SPC format
		String spcPath = "test_measurement.spc";
		JetiResult<Boolean> spcResult = radioEx.saveSpectralRadianceSPC(beginWavelength, endWavelength, spcPath, operator, memo);
		// Internal DLL error?!
		assertTrue(spcResult.isSuccess(), spcResult.toString());

		// Test CSV format
		String csvPath = "test_measurement.csv";
		JetiResult<Boolean> csvResult = radioEx.saveSpectralRadianceCSV(beginWavelength, endWavelength, csvPath, operator, memo);
		assertTrue(csvResult.isSuccess(), csvResult.toString());
	}

	// Causes subsequent tests to fail!?
	// @Test
	// @DisplayName("Perform measurement with adaptation")
	// void testMeasurementWithAdaptation () {
	// int averageCount = 1;
	// int stepWidth = 5;
	//
	// JetiResult<Boolean> measureResult = radioEx.measureWithAdaptation(averageCount, stepWidth);
	// assertTrue(measureResult.isSuccess(), measureResult.toString());
	//
	// // Wait for adaptation to complete
	// boolean adapting = true;
	// int attempts = 0;
	// while (adapting && attempts < 200) { // Longer timeout for adaptation
	// try {
	// Thread.sleep(100);
	// } catch (InterruptedException e) {
	// Thread.currentThread().interrupt();
	// fail("Test interrupted");
	// }
	//
	// JetiResult<JetiRadio.AdaptationStatus> statusResult = radioEx.getAdaptationStatus();
	// assertTrue(statusResult.isSuccess(), statusResult.toString());
	// adapting = !statusResult.getValue().isComplete();
	// attempts++;
	// }
	//
	// assertFalse(adapting, "Adaptation should complete within timeout");
	//
	// // Get final adaptation status
	// JetiResult<JetiRadio.AdaptationStatus> finalStatus = radioEx.getAdaptationStatus();
	// assertTrue(finalStatus.isSuccess(), finalStatus.toString());
	// JetiRadio.AdaptationStatus status = finalStatus.getValue();
	// assertTrue(status.isComplete(), status.toString());
	// // Why do these fail?
	// assertTrue(status.integrationTime() > 0);
	// assertTrue(status.averageCount() > 0);
	// }

	@Test
	@DisplayName("Prepare measurement with parameters")
	void testPrepareMeasurementWithParameters () {
		float integrationTime = 150.0f;
		int averageCount = 5;
		int stepWidth = 1;
		JetiResult<Boolean> prepareResult = radioEx.prepareMeasurement(integrationTime, averageCount, stepWidth);
		assertTrue(prepareResult.isSuccess());
	}

	// Unsupported by specbos 2501?
	@Test
	@DisplayName("Get and set measurement distance")
	void testMeasurementDistance () {
		int testDistance = 200;

		JetiResult<Boolean> setResult = radioEx.setMeasurementDistance(testDistance);
		assertTrue(setResult.isSuccess(), setResult.toString());

		JetiResult<Integer> getResult = radioEx.getMeasurementDistance();
		assertTrue(getResult.isSuccess(), getResult.toString());
		assertEquals(testDistance, getResult.getValue());
	}

	@Test
	@DisplayName("Get integration time")
	void testGetIntegrationTime () {
		JetiResult<Float> tintResult = radioEx.getIntegrationTime();
		assertTrue(tintResult.isSuccess(), tintResult.toString());
		assertTrue(tintResult.getValue() > 0);
	}

	@Test
	@DisplayName("Get chromaticity values")
	void testChromaticityValues () {
		performMeasurementAndWait(100, 1, 5);

		// Get chromaticity XY
		JetiResult<float[]> xyResult = radioEx.getChromaticityXY();
		assertTrue(xyResult.isSuccess(), xyResult.toString());
		assertEquals(2, xyResult.getValue().length);

		// Get chromaticity XY10
		JetiResult<float[]> xy10Result = radioEx.getChromaticityXY10();
		assertTrue(xy10Result.isSuccess(), xy10Result.toString());
		assertEquals(2, xy10Result.getValue().length);

		// Get chromaticity UV
		JetiResult<float[]> uvResult = radioEx.getChromaticityUV();
		assertTrue(uvResult.isSuccess(), uvResult.toString());
		assertEquals(2, uvResult.getValue().length);

		// Get XYZ values
		JetiResult<float[]> xyzResult = radioEx.getXYZValues();
		assertTrue(xyzResult.isSuccess(), xyzResult.toString());
		assertEquals(3, xyzResult.getValue().length);

		// Get dominant wavelength and purity
		JetiResult<float[]> dwlpeResult = radioEx.getDominantWavelengthAndPurity();
		assertTrue(dwlpeResult.isSuccess(), dwlpeResult.toString());
		assertEquals(2, dwlpeResult.getValue().length);
	}

	@Test
	@DisplayName("Break measurement")
	void testBreakMeasurement () {
		// Start measurement
		JetiResult<Boolean> measureResult = radioEx.measure(100.0f, 3, 5);
		assertTrue(measureResult.isSuccess(), measureResult.toString());

		// Immediately break it
		JetiResult<Boolean> breakResult = radioEx.breakMeasurement();
		assertTrue(breakResult.isSuccess(), breakResult.toString());

		// Check that measurement is no longer active
		JetiResult<Boolean> statusResult = radioEx.getMeasurementStatus();
		assertTrue(statusResult.isSuccess(), statusResult.toString());
		// Note: Status might still be true briefly after break
	}

	private void performMeasurementAndWait (float integrationTime, int averageCount, int stepWidth) {
		JetiResult<Boolean> measureResult = radioEx.measure(integrationTime, averageCount, stepWidth);
		assertTrue(measureResult.isSuccess(), measureResult.toString());
		waitForMeasurementCompletion();
	}

	private void waitForMeasurementCompletion () {
		boolean measuring = true;
		int attempts = 0;
		while (measuring && attempts < 100) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				fail("Test interrupted");
			}

			JetiResult<Boolean> statusResult = radioEx.getMeasurementStatus();
			assertTrue(statusResult.isSuccess(), statusResult.toString());
			measuring = statusResult.getValue();
			attempts++;
		}

		assertFalse(measuring, "Measurement should complete within timeout");
	}
}
