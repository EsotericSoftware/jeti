
package com.esotericsoftware.jeti;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JetiCoreTest {
	static public final String IP = "10.1.0.55";

	static private String getTestLicenseKey () {
		String licenseKey = System.getenv("JETI_LICENSE_KEY");
		if (licenseKey != null && !licenseKey.trim().isEmpty()) return licenseKey.trim();

		licenseKey = System.getProperty("jeti.license.key");
		if (licenseKey != null && !licenseKey.trim().isEmpty()) return licenseKey.trim();

		return null;
	}

	@Test
	@Order(1)
	@DisplayName("Initialize SDK")
	void testInitialization () {
		Log.TRACE();
		assertDoesNotThrow( () -> JetiSDK.initialize());
	}
}
