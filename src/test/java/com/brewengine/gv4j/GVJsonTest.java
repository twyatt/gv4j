package com.brewengine.gv4j;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class GVJsonTest {

    @Test
    public void parseJsonResponseTest() {
        String string =
            "{" +
                "\"app_version\":13," +
                "\"settings_response\":{" +
                    "\"user_preferences\":{" +
                        "\"default_call_settings\":{" +
                            "\"disabled_forwarding_id\":[2,4]" +
                        "}," +
                        "\"forwarding\":[" +
                            "{" +
                                "\"id\":1," +
                                "\"phone_number\":\"+15555551212\"" +
                            "}," +
                            "{" +
                                "\"id\":4," +
                                "\"phone_number\":\"+15555550000\"" +
                            "}," +
                            "{" +
                                "\"id\":8," +
                                "\"phone_number\":\"example@gmail.com\"" +
                            "}" +
                        "]" +
                    "}" +
                "}" +
            "}";

        Gson gson = new Gson();
        GVJson json = gson.fromJson(string, GVJson.class);

        assertNotNull(json);
        assertNotNull(json.settingsResponse);
        assertNotNull(json.settingsResponse.userPreferences);
        assertNotNull(json.settingsResponse.userPreferences.defaultCallSettings);
        assertNotNull(json.settingsResponse.userPreferences.defaultCallSettings.disabledForwardingId);
        assertNotNull(json.settingsResponse.userPreferences.forwarding);

        List<GVJson.Forward> forwarding = json.settingsResponse.userPreferences.forwarding;
        assertFalse(forwarding.isEmpty());

        List<Integer> disabled_forwarding_id = json.settingsResponse.userPreferences.defaultCallSettings.disabledForwardingId;
        assertFalse(disabled_forwarding_id.isEmpty());

        GVJson.Forward forward1 = findForwardById(forwarding, 1);
        assertNotNull(forward1);
        assertEquals("+15555551212", forward1.phoneNumber);

        GVJson.Forward forward2 = findForwardById(forwarding, 2);
        assertNull(forward2);

        GVJson.Forward forward4 = findForwardById(forwarding, 4);
        assertNotNull(forward4);
        assertEquals("+15555550000", forward4.phoneNumber);

        GVJson.Forward forward8 = findForwardById(forwarding, 8);
        assertNotNull(forward8);
        assertEquals("example@gmail.com", forward8.phoneNumber);
    }

    @Test
    public void settingsFromJsonTest() {
        String string =
            "{" +
                "\"app_version\":13," +
                "\"settings_response\":{" +
                    "\"user_preferences\":{" +
                        "\"default_call_settings\":{" +
                            "\"disabled_forwarding_id\":[2,8]" +
                        "}," +
                        "\"forwarding\":[" +
                            "{" +
                                "\"behavior_on_redirect\":1," +
                                "\"id\":1," +
                                "\"name\":\"Phone 1\"," +
                                "\"phone_number\":\"+15555551212\"," +
                                "\"policy_bitmask\":3," +
                                "\"sms_enabled\":false," +
                                "\"type\":2" +
                            "}," +
                            "{" +
                                "\"behavior_on_redirect\":1," +
                                "\"id\":4," +
                                "\"name\":\"Phone 2\"," +
                                "\"phone_number\":\"+15555550000\"," +
                                "\"policy_bitmask\":3," +
                                "\"sms_enabled\":false," +
                                "\"type\":2" +
                            "}," +
                            "{" +
                                "\"behavior_on_redirect\":1," +
                                "\"id\":8," +
                                "\"name\":\"Google Talk\"," +
                                "\"phone_number\":\"example@gmail.com\"," +
                                "\"policy_bitmask\":3," +
                                "\"sms_enabled\":false," +
                                "\"type\":3" +
                            "}" +
                        "]" +
                    "}" +
                "}" +
            "}";

        Gson gson = new Gson();
        GVJson json = gson.fromJson(string, GVJson.class);
        Settings settings = Settings.valueOf(json);

        Phone phone1 = findPhoneById(settings.getPhones(), 1);
        assertNotNull(phone1);
        assertEquals(1, phone1.getBehaviorOnRedirect());
        assertEquals("Phone 1",      phone1.getName());
        assertEquals("+15555551212", phone1.getPhoneNumber());
        assertEquals(3,              phone1.getPolicyBitmask());
        assertEquals(false,          phone1.isSmsEnabled());
        assertEquals(2, phone1.getType());
        assertTrue(phone1.isEnabled());

        assertNull(findPhoneById(settings.getPhones(), 2));
        assertNull(findPhoneById(settings.getPhones(), 3));

        Phone phone4 = findPhoneById(settings.getPhones(), 4);
        assertNotNull(phone4);
        assertEquals(1,              phone4.getBehaviorOnRedirect());
        assertEquals("Phone 2",      phone4.getName());
        assertEquals("+15555550000", phone4.getPhoneNumber());
        assertEquals(3,              phone4.getPolicyBitmask());
        assertEquals(false,          phone4.isSmsEnabled());
        assertEquals(2,              phone4.getType());
        assertTrue(phone4.isEnabled());

        assertNull(findPhoneById(settings.getPhones(), 5));
        assertNull(findPhoneById(settings.getPhones(), 6));
        assertNull(findPhoneById(settings.getPhones(), 7));

        Phone phone8 = findPhoneById(settings.getPhones(), 8);
        assertNotNull(phone8);
        assertEquals(1,                   phone8.getBehaviorOnRedirect());
        assertEquals("Google Talk",       phone8.getName());
        assertEquals("example@gmail.com", phone8.getPhoneNumber());
        assertEquals(3,                   phone8.getPolicyBitmask());
        assertEquals(false,               phone8.isSmsEnabled());
        assertEquals(3,                   phone8.getType());
        assertFalse(phone8.isEnabled());

        assertNull(findPhoneById(settings.getPhones(), 9));
    }

    @Test
    public void settingsFromJsonNoDisabledForwardingIdsTest() {
        String string =
            "{" +
                "\"app_version\":13," +
                "\"settings_response\":{" +
                    "\"user_preferences\":{" +
                        "\"default_call_settings\":{" +
                        "}," +
                        "\"forwarding\":[" +
                            "{" +
                                "\"behavior_on_redirect\":1," +
                                "\"id\":1," +
                                "\"name\":\"Phone 1\"," +
                                "\"phone_number\":\"+15555551212\"," +
                                "\"policy_bitmask\":3," +
                                "\"sms_enabled\":false," +
                                "\"type\":2" +
                            "}," +
                            "{" +
                                "\"behavior_on_redirect\":1," +
                                "\"id\":4," +
                                "\"name\":\"Phone 2\"," +
                                "\"phone_number\":\"+15555550000\"," +
                                "\"policy_bitmask\":3," +
                                "\"sms_enabled\":false," +
                                "\"type\":2" +
                            "}," +
                            "{" +
                                "\"behavior_on_redirect\":1," +
                                "\"id\":8," +
                                "\"name\":\"Google Talk\"," +
                                "\"phone_number\":\"example@gmail.com\"," +
                                "\"policy_bitmask\":3," +
                                "\"sms_enabled\":false," +
                                "\"type\":3" +
                            "}" +
                        "]" +
                    "}" +
                "}" +
            "}";

        Gson gson = new Gson();
        GVJson json = gson.fromJson(string, GVJson.class);
        Settings settings = Settings.valueOf(json);

        Phone phone1 = findPhoneById(settings.getPhones(), 1);
        assertNotNull(phone1);
        assertEquals(1, phone1.getBehaviorOnRedirect());
        assertEquals("Phone 1",      phone1.getName());
        assertEquals("+15555551212", phone1.getPhoneNumber());
        assertEquals(3,              phone1.getPolicyBitmask());
        assertEquals(false,          phone1.isSmsEnabled());
        assertEquals(2, phone1.getType());
        assertTrue(phone1.isEnabled());

        assertNull(findPhoneById(settings.getPhones(), 2));
        assertNull(findPhoneById(settings.getPhones(), 3));

        Phone phone4 = findPhoneById(settings.getPhones(), 4);
        assertNotNull(phone4);
        assertEquals(1,              phone4.getBehaviorOnRedirect());
        assertEquals("Phone 2",      phone4.getName());
        assertEquals("+15555550000", phone4.getPhoneNumber());
        assertEquals(3,              phone4.getPolicyBitmask());
        assertEquals(false,          phone4.isSmsEnabled());
        assertEquals(2,              phone4.getType());
        assertTrue(phone4.isEnabled());

        assertNull(findPhoneById(settings.getPhones(), 5));
        assertNull(findPhoneById(settings.getPhones(), 6));
        assertNull(findPhoneById(settings.getPhones(), 7));

        Phone phone8 = findPhoneById(settings.getPhones(), 8);
        assertNotNull(phone8);
        assertEquals(1,                   phone8.getBehaviorOnRedirect());
        assertEquals("Google Talk",       phone8.getName());
        assertEquals("example@gmail.com", phone8.getPhoneNumber());
        assertEquals(3,                   phone8.getPolicyBitmask());
        assertEquals(false,               phone8.isSmsEnabled());
        assertEquals(3,                   phone8.getType());
        assertTrue(phone8.isEnabled());

        assertNull(findPhoneById(settings.getPhones(), 9));
    }

    private static GVJson.Forward findForwardById(List<GVJson.Forward> forwarding, int id) {
        for (GVJson.Forward forward : forwarding) {
            if (forward.id == id) {
                return forward;
            }
        }
        return null;
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
