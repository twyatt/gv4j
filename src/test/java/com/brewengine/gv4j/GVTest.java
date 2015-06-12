package com.brewengine.gv4j;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class GVTest {

	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";

	/**
	 * Disables and re-enables first GV phone.
	 *
	 * @throws IOException
	 */
	@Test
	public void disablePhoneTest() throws IOException {
		if ("username".equalsIgnoreCase(USERNAME)) {
			fail("Configure GV username/password before running this test.");
		}

		GV gv = new GV();
		gv.login(USERNAME, PASSWORD);

		Settings settingsBefore = gv.fetchSettings();
		assertFalse(settingsBefore.getPhones().isEmpty());

		Phone phoneBefore = settingsBefore.getPhones().get(0);
		int id = phoneBefore.getId();
		assertTrue(phoneBefore.isEnabled());
		System.out.println("Disabling " + phoneBefore);
		gv.disablePhone(phoneBefore);
		assertFalse(phoneBefore.isEnabled());

		Settings settingsAfter = gv.fetchSettings();
		assertFalse(settingsAfter.getPhones().isEmpty());

		Phone phoneAfter = findPhoneById(settingsAfter.getPhones(), id);
		assertFalse(phoneAfter.isEnabled());
		System.out.println("Enabling " + phoneAfter);
		gv.enablePhone(phoneAfter);
		assertTrue(phoneAfter.isEnabled());
	}

	private static Phone findPhoneById(List<Phone> phones, int id) {
		for (Phone phone : phones) {
			if (phone.getId() == id) {
				return phone;
			}
		}
		return null;
	}

}
