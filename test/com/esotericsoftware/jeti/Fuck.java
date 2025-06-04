
package com.esotericsoftware.jeti;

import java.util.Scanner;

import com.esotericsoftware.jeti.JetiSDK;
import com.esotericsoftware.jeti.JetiSDK.CRI;
import com.esotericsoftware.jeti.JetiSDK.DeviceSerials;
import com.esotericsoftware.jeti.JetiSDK.TM30;
import com.esotericsoftware.jeti.JetiSDK.XY;
import com.esotericsoftware.jeti.RadioEx;
import com.esotericsoftware.jeti.Result;

public class Fuck extends JetiTest {
	static public void main (String[] args) {
		JetiSDK.initialize();

		result(RadioEx.getDeviceCount());
		RadioEx radioEx = result(RadioEx.openDevice(0));
		result(radioEx.measure(0, 1, 5));

		while (result(radioEx.getMeasurementStatus()))
			sleep(100);

		Float result = result(radioEx.getRadiometricValue(380, 780));
		System.out.println(result);

		radioEx.close();
	}
}
