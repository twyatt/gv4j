package com.brewengine.gv4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Settings {

	private List<Phone> phones = new ArrayList<>();
	private List<Phone> unmodifiablePhones = Collections.unmodifiableList(phones);

	public List<Phone> getPhones() {
		return unmodifiablePhones;
	}

	public static Settings valueOf(GVJson json) {
		List<Integer> disabledList = json.settingsResponse.userPreferences.defaultCallSettings.disabledForwardingId;

		Settings settings = new Settings();
		for (GVJson.Forward forward : json.settingsResponse.userPreferences.forwarding) {
			Phone phone = new Phone();

			phone.id = forward.id;
			phone.name = forward.name;
			phone.type = forward.type;
			phone.phoneNumber = forward.phoneNumber;
			phone.isEnabled = !disabledList.contains(phone.id);
			phone.isSmsEnabled = forward.smsEnabled;
			phone.policyBitmask = forward.policyBitmask;
			phone.behaviorOnRedirect = forward.behaviorOnRedirect;

			settings.phones.add(phone);
		}

		return settings;
	}

}
