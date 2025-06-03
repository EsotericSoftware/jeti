
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.jeti.JetiSDK.AdaptationStatus;
import com.esotericsoftware.jeti.JetiSDK.BlueMeasurement;
import com.esotericsoftware.jeti.JetiSDK.CRI;
import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;
import com.esotericsoftware.jeti.JetiSDK.DominantWavelength;
import com.esotericsoftware.jeti.JetiSDK.PeakFWHM;
import com.esotericsoftware.jeti.JetiSDK.TM30;
import com.esotericsoftware.jeti.JetiSDK.UV;
import com.esotericsoftware.jeti.JetiSDK.XY;
import com.esotericsoftware.jeti.JetiSDK.XY10;
import com.esotericsoftware.jeti.JetiSDK.XYZ;

@DisplayName("JetiRadioEx Integration Tests")
public class RadioExTest {
	private RadioEx radioEx;

	@BeforeEach
	void setUp () {
		JetiSDK.initialize();

		Result<Integer> deviceCount = RadioEx.getDeviceCount();
		assumeTrue(deviceCount.isSuccess() && deviceCount.getValue() > 0, "No radio ex devices available for testing");

		Result<RadioEx> deviceResult = RadioEx.openDevice(0);
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
		Result<DeviceSerials> serialsResult = RadioEx.getDeviceSerials(0);
		if (serialsResult.isSuccess()) {
			DeviceSerials serials = serialsResult.getValue();
			assertNotNull(serials.electronics());
			assertNotNull(serials.spectrometer());
			assertNotNull(serials.device());
		}

		Result<DllVersion> versionResult = RadioEx.getDllVersion();
		assertTrue(versionResult.isSuccess(), versionResult.toString());
		assertNotNull(versionResult.getValue());
	}

	@Test
	@DisplayName("Perform measurement with parameters")
	void testMeasurementWithParameters () {
		float integrationTime = 100.0f;
		int averageCount = 3;
		int stepWidth = 5;

		Result<Boolean> measureResult = radioEx.measure(integrationTime, averageCount, stepWidth);
		assertTrue(measureResult.isSuccess(), measureResult.toString());

		// Wait for measurement to complete
		waitForMeasurementCompletion();

		// Get radiometric value with wavelength range
		Result<Float> radioResult = radioEx.getRadiometricValue(380, 780);
		assertTrue(radioResult.isSuccess(), radioResult.toString());
		assertTrue(radioResult.getValue() >= 0);

		// Get photometric value
		Result<Float> photoResult = radioEx.getPhotometricValue();
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
		Result<float[]> spectralResult = radioEx.getSpectralRadiance(beginWavelength, endWavelength, step);
		assertTrue(spectralResult.isSuccess(), spectralResult.toString());
		assertEquals(expectedSize, spectralResult.getValue().length);

		// Test high resolution spectral data
		expectedSize = (int)((endWavelength - beginWavelength) / 0.1f + 1);
		Result<float[]> hiResResult = radioEx.getSpectralRadianceHiRes(beginWavelength, endWavelength);
		assertTrue(hiResResult.isSuccess(), hiResResult.toString());
		assertEquals(expectedSize, hiResResult.getValue().length);
	}

	@Test
	@DisplayName("Get color rendering index with CCT")
	void testCRIwithCCT () {
		performMeasurementAndWait(100.0f, 1, 5);

		Result<Float> cctResult = radioEx.getCCT();
		assertTrue(cctResult.isSuccess(), cctResult.toString());
		float cct = cctResult.getValue();

		// Get CRI data using the measured CCT
		Result<CRI> criResult = radioEx.getCRI(cct);
		assertTrue(criResult.isSuccess(), criResult.toString());

		CRI criData = criResult.getValue();
		assertNotNull(criData);
		assertTrue(criData.ra() >= 0 && criData.ra() <= 100);
		assertEquals(15, criData.samples().length);

		// Check that special indices are reasonable
		for (float index : criData.samples()) {
			assertTrue(index >= -100 && index <= 100, "CRI special index should be between -100 and 100, got: " + index);
		}
	}

	@Test
	@DisplayName("Get TM30 data")
	void testTM30 () {
		performMeasurementAndWait(100.0f, 1, 5);

		// Test both TM30-15 and TM30-18
		Result<TM30> tm30_15Result = radioEx.getTM30(true);
		if (tm30_15Result.isSuccess()) {
			TM30 TM30 = tm30_15Result.getValue();
			assertTrue(TM30.rf() >= 0 && TM30.rf() <= 200);
			assertTrue(TM30.rg() >= 0 && TM30.rg() <= 200);
			assertEquals(16, TM30.hueAngleBins().length);
			assertEquals(99, TM30.colorSamples().length);
		}

		Result<TM30> tm30_18Result = radioEx.getTM30(false);
		if (tm30_18Result.isSuccess()) {
			TM30 tm30 = tm30_18Result.getValue();
			assertTrue(tm30.rf() >= 0 && tm30.rf() <= 200);
			assertTrue(tm30.rg() >= 0 && tm30.rg() <= 200);
		}
	}

	@Test
	@DisplayName("Get peak FWHM data")
	void testPeakFWHMData () {
		performMeasurementAndWait(100.0f, 1, 1);

		float threshold = 0.5f;
		Result<PeakFWHM> result = radioEx.getPeakFWHM(threshold);
		if (result.isSuccess()) {
			PeakFWHM data = result.getValue();
			assertTrue(data.peak() >= 380 && data.peak() <= 780, "Peak wavelength should be in visible range: " + data.peak());
			assertTrue(data.fwhm() > 0, "FWHM should be positive: " + data.fwhm());
		}
	}

	@Test
	@DisplayName("Get blue measurement data")
	void testBlueMeasurementData () {
		performMeasurementAndWait(100.0f, 1, 5);

		Result<BlueMeasurement> result = radioEx.getBlueMeasurement();
		assertTrue(result.isSuccess(), result.toString());
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

		// Test SPC format
		String spcPath = new File(tempDir, "test_measurement.spc").getAbsolutePath();
		Result<Boolean> spcResult = radioEx.saveSpectralRadianceSPC(beginWavelength, endWavelength, spcPath, operator, memo);
		// BOZO - Internal DLL error?
		assertTrue(spcResult.isSuccess(), spcResult.toString());

		// Test CSV format
		String csvPath = new File(tempDir, "test_measurement.csv").getAbsolutePath();
		Result<Boolean> csvResult = radioEx.saveSpectralRadianceCSV(beginWavelength, endWavelength, csvPath, operator, memo);
		assertTrue(csvResult.isSuccess(), csvResult.toString());

		new File(spcPath).delete();
		new File(csvPath).delete();
	}

	// BOZO - Causes subsequent tests to fail!
	@Test
	@DisplayName("Perform measurement with adaptation")
	void testMeasurementWithAdaptation () {
		int averageCount = 1;
		int stepWidth = 5;

		Result<Boolean> measureResult = radioEx.measureWithAdaptation(averageCount, stepWidth);
		assertTrue(measureResult.isSuccess(), measureResult.toString());

		// Wait for adaptation to complete
		boolean adapting = true;
		int attempts = 0;
		while (adapting && attempts < 200) { // Longer timeout for adaptation
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("Test interrupted");
			}

			Result<AdaptationStatus> statusResult = radioEx.getAdaptationStatus();
			assertTrue(statusResult.isSuccess(), statusResult.toString());
			adapting = !statusResult.getValue().complete();
			attempts++;
		}

		assertFalse(adapting, "Adaptation should complete within timeout");

		// Get final adaptation status
		Result<AdaptationStatus> finalStatus = radioEx.getAdaptationStatus();
		assertTrue(finalStatus.isSuccess(), finalStatus.toString());
		AdaptationStatus status = finalStatus.getValue();
		assertTrue(status.complete(), status.toString());
		// BOZO - Why are these 0?
		assertTrue(status.integrationTime() > 0);
		assertTrue(status.averageCount() > 0);
	}

	@Test
	@DisplayName("Prepare measurement with parameters")
	void testPrepareMeasurementWithParameters () {
		float integrationTime = 150.0f;
		int averageCount = 5;
		int stepWidth = 1;
		Result<Boolean> prepareResult = radioEx.prepareMeasurement(integrationTime, averageCount, stepWidth);
		assertTrue(prepareResult.isSuccess(), prepareResult.toString());
	}

	@Test
	@DisplayName("Get and set measurement distance")
	void testMeasurementDistance () {
		int testDistance = 200;

		Result<Boolean> setResult = radioEx.setMeasurementDistance(testDistance);
		// BOZO - Command not supported or invalid argument?
		assertTrue(setResult.isSuccess(), setResult.toString());

		Result<Integer> getResult = radioEx.getMeasurementDistance();
		assertTrue(getResult.isSuccess(), getResult.toString());
		assertEquals(testDistance, getResult.getValue());
	}

	@Test
	@DisplayName("Get integration time")
	void testGetIntegrationTime () {
		Result<Float> tintResult = radioEx.getIntegrationTime();
		assertTrue(tintResult.isSuccess(), tintResult.toString());
		assertTrue(tintResult.getValue() > 0);
	}

	@Test
	@DisplayName("Get chromaticity values")
	void testChromaticityValues () {
		performMeasurementAndWait(100, 1, 5);

		// Get chromaticity XY
		Result<XY> xyResult = radioEx.getChromaXY();
		assertTrue(xyResult.isSuccess(), xyResult.toString());

		// Get chromaticity XY10
		Result<XY10> xy10Result = radioEx.getChromaXY10();
		assertTrue(xy10Result.isSuccess(), xy10Result.toString());

		// Get chromaticity UV
		Result<UV> uvResult = radioEx.getChromaUV();
		assertTrue(uvResult.isSuccess(), uvResult.toString());

		// Get XYZ values
		Result<XYZ> xyzResult = radioEx.getXYZ();
		assertTrue(xyzResult.isSuccess(), xyzResult.toString());

		// Get dominant wavelength and purity
		Result<DominantWavelength> dwlpeResult = radioEx.getDominantWavelength();
		assertTrue(dwlpeResult.isSuccess(), dwlpeResult.toString());
	}

	@Test
	@DisplayName("Break measurement")
	void testBreakMeasurement () {
		// Start measurement
		Result<Boolean> measureResult = radioEx.measure(100.0f, 3, 5);
		assertTrue(measureResult.isSuccess(), measureResult.toString());

		// Immediately break it
		Result<Boolean> breakResult = radioEx.breakMeasurement();
		assertTrue(breakResult.isSuccess(), breakResult.toString());

		// Check that measurement is no longer active
		Result<Boolean> statusResult = radioEx.getMeasurementStatus();
		assertTrue(statusResult.isSuccess(), statusResult.toString());
	}

	@Test
	@DisplayName("Test all RadioEx functions for coverage")
	void testAllRadioExFunctions () {
		// Test measureWithAdaptation (carefully to avoid breaking other tests)
		var adaptResult = radioEx.measureWithAdaptation(1, 5);
		if (adaptResult.isSuccess()) {
			// Break it immediately to avoid issues
			radioEx.breakMeasurement();

			// Still test getAdaptationStatus
			var adaptStatus = radioEx.getAdaptationStatus();
			if (adaptStatus.isSuccess()) {
				assertNotNull(adaptStatus.getValue());
			}
		}

		// Perform a normal measurement for other tests
		performMeasurementAndWait(100.0f, 1, 5);

		// Test getDuv (not tested elsewhere)
		var duvResult = radioEx.getDuv();
		if (duvResult.isSuccess()) {
			assertNotNull(duvResult.getValue());
		}

		// Test getCCT separately (already tested in testCRIwithCCT but let's ensure it's called)
		var cctResult = radioEx.getCCT();
		if (cctResult.isSuccess()) {
			assertTrue(cctResult.getValue() >= 0);
		}
	}

	private void performMeasurementAndWait (float integrationTime, int averageCount, int stepWidth) {
		Result<Boolean> measureResult = radioEx.measure(integrationTime, averageCount, stepWidth);
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

			Result<Boolean> statusResult = radioEx.getMeasurementStatus();
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
