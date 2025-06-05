
package com.esotericsoftware.jeti;

import com.esotericsoftware.jeti.JetiSDK.AdaptationStatus;
import com.esotericsoftware.jeti.JetiSDK.TM30;

public class TM30Test extends JetiTest {
	static public void main (String[] args) {
		JetiSDK.initialize();

		RadioEx.getDeviceCount();
		RadioEx radioEx = RadioEx.openDevice(0);

		radioEx.measureWithAdaptation(1, 1);
		float integrationTime = 0;
		while (true) {
			AdaptationStatus status = radioEx.getAdaptationStatus();
			if (status.integrationTime() != 0) integrationTime = status.integrationTime();
			if (!status.measuring()) break;
			sleep(100);
		}
		System.out.println(integrationTime + " ms");

		System.out.println(radioEx.getPhotometricValue() + " cd/m²");
		float cct = radioEx.getCCT();
		System.out.println(cct + " K");
		System.out.println(radioEx.getCRI(cct));

		TM30 tm30 = radioEx.getTM30(false);
		System.out.println(tm30);
		if (false) {
			for (int i = 0, n = tm30.hueAngleBins().length; i < n; i++)
				System.out.println(i + ": " + tm30.hueAngleBins()[i]);
			System.out.println();
			for (int i = 0, n = tm30.colorSamples().length; i < n; i++)
				System.out.println(i + ": " + tm30.colorSamples()[i]);
		}

		radioEx.close();
	}
}
