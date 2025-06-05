
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.esotericsoftware.jeti.JetiSDK.CRI;
import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.DllVersion;

public class RadioTest extends JetiTest {
	private Radio radio;

	@BeforeEach
	void setUp () {
		assumeTrue(Radio.getDeviceCount() > 0, "No radio devices available for testing");
		radio = Radio.openDevice(0);
	}

	@AfterEach
	void tearDown () {
		if (radio != null && !radio.isClosed()) radio.close();
	}

	@Test
	@DisplayName("Get device information")
	void testGetDeviceInfo () {
		DeviceSerials serials = Radio.getDeviceSerials(0);
		assertNotNull(serials.electronics());
		assertNotNull(serials.spectrometer());
		assertNotNull(serials.device());

		DllVersion version = Radio.getDllVersion();
		assertNotNull(version);
	}

	@Test
	@DisplayName("Perform measurement with parameters")
	void testMeasurementWithParameters () {
		radio.measure();
		waitForMeasurementCompletion();
		assertTrue(radio.getRadiometricValue() >= 0);
		assertTrue(radio.getPhotometricValue() >= 0);
	}

	@Test
	@DisplayName("Get spectral data")
	void testSpectralData () {
		performMeasurementAndWait();
		assertEquals(81, radio.getSpectralRadiance().length); // (780-380)/5 + 1
	}

	@Test
	@DisplayName("Get chromaticity values")
	void testChromaticityValues () {
		performMeasurementAndWait();
		assertNotNull(radio.getChromaXY());
		assertNotNull(radio.getChromaXY10());
		assertNotNull(radio.getChromaUV());
		assertNotNull(radio.getXYZ());
		assertNotNull(radio.getDominantWavelength());
	}

	@Test
	@DisplayName("Get CCT and CRI")
	void testCCTandCRI () {
		performMeasurementAndWait();
		CRI cri = radio.getCRI();
		assertNotNull(cri);
		assertTrue(cri.ra() >= 0 && cri.ra() <= 100);
		assertEquals(15, cri.samples().length);
	}

	@Test
	@DisplayName("Perform measurement with adaptation")
	void testMeasurementWithAdaptation () {
		radio.measureWithAdaptation();

		int attempts = 0;
		while (radio.getAdaptationStatus().measuring() && attempts++ < 100) {
			System.out.print(".");
			sleep(100);
		}
		System.out.println();
		assertFalse(attempts >= 100, "Measurement should complete within timeout");
		assertFalse(radio.getAdaptationStatus().measuring());
	}

	@Test
	@DisplayName("Prepare measurement")
	void testPrepareMeasurement () {
		radio.prepareMeasurement();
	}

	@Test
	@DisplayName("Get integration time")
	void testGetIntegrationTime () {
		assertTrue(radio.getIntegrationTime() > 0);
	}

	@Test
	@DisplayName("Break measurement")
	void testCancelMeasurement () {
		radio.measure();
		radio.cancelMeasurement();
		assertFalse(radio.isMeasuring());
	}

	@Test
	@DisplayName("Get Duv")
	void testDuv () {
		performMeasurementAndWait();
		assertNotNull(radio.getDuv());
	}

	private void performMeasurementAndWait () {
		radio.measure();
		waitForMeasurementCompletion();
	}

	private void waitForMeasurementCompletion () {
		int attempts = 0;
		while (radio.isMeasuring() && attempts++ < 100) {
			System.out.print(".");
			sleep(100);
		}
		System.out.println();
		assertFalse(attempts >= 100, "Measurement should complete within timeout");
	}
}
