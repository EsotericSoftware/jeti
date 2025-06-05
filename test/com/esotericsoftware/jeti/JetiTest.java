
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;

public class JetiTest {
	static public final String IP = "10.1.0.55";

	@BeforeAll
	static void init () {
		Log.TRACE();
		JetiSDK.initialize();
	}

	static void sleep (int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
		}
	}

}
