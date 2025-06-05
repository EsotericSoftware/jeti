
package com.esotericsoftware.jeti;

public class TM30Test extends JetiTest {
	static public void main (String[] args) {
		JetiSDK.initialize();

		RadioEx.getDeviceCount();
		RadioEx radioEx = RadioEx.openDevice(0);
		radioEx.measureWithAdaptation(1, 1);

		while (radioEx.getAdaptationStatus().measuring())
			sleep(100);

		System.out.println(radioEx.getRadiometricValue(380, 780)); // Works.
		float cct = radioEx.getCCT(); // Works.
		System.out.println(cct + "K");
		System.out.println(radioEx.getCRI(cct)); // Works.
		System.out.println(radioEx.getTM30(false)); // BOZO - Crashes.

		radioEx.close();
	}
}
