
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.esotericsoftware.jeti.JetiSDK.CRI;
import com.esotericsoftware.jeti.JetiSDK.TM30;

public class TM30Test extends JetiTest {
	@Test
	void testRadioEx () {
		result(RadioEx.getDeviceCount());
		try (RadioEx radioEx = result(RadioEx.openDevice(0))) {
			sleep(500);

			result(radioEx.measure(0, 1, 5));

			for (int i = 0;; i++) {
				sleep(100);
				if (result(radioEx.getMeasurementStatus())) break;
				if (i > 100) fail("Measurement timed out.");
			}

			sleep(500);

			Float result = result(radioEx.getRadiometricValue(380, 780));
			System.out.println(result);

// CRI cri = result(radioEx.getCRI(0));
// System.out.println(cri.ra());
//
// TM30 tm30 = result(radioEx.getTM30(false));
// assertTrue(tm30.rf() >= 0 && tm30.rf() <= 200);
// assertTrue(tm30.rg() >= 0 && tm30.rg() <= 200);
		}
	}

// @Test
// void testRadio () {
// result(RadioEx.getDeviceCount());
// try (Radio radio = result(Radio.openDevice(0))) {
// result(radio.measureWithAdaptation());
//
// boolean adapting = true;
// int attempts = 0;
// while (adapting && attempts < 200) {
// sleep(100);
// AdaptationStatus status = result(radio.getAdaptationStatus());
// adapting = !status.complete();
// attempts++;
// }
//
// assertFalse(adapting, "Adaptation should complete within timeout");
//
// // Get final adaptation status
// AdaptationStatus status = result(radio.getAdaptationStatus());
// assertTrue(status.complete());
// }
// }
}
