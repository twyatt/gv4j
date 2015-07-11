package com.brewengine.gv4j;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Used in the conversion of GV JSON to native Java objects.
 */
public class GVJson {

    @SerializedName("settings_response")
    public SettingsResponse settingsResponse;

    public class SettingsResponse {
        @SerializedName("user_preferences")
        public UserPreferences userPreferences;
    }

    public class UserPreferences {
        @SerializedName("default_call_settings")
        public DefaultCallSettings defaultCallSettings;

        public List<Forward> forwarding;
    }

    public class DefaultCallSettings {
        @SerializedName("disabled_forwarding_id")
        public List<Integer> disabledForwardingId;
    }

    public class Forward {
        public int id;
        public String name;
        public int type;

        @SerializedName("phone_number")
        public String phoneNumber;

        @SerializedName("behavior_on_redirect")
        public int behaviorOnRedirect;

        @SerializedName("policy_bitmask")
        public int policyBitmask;

        @SerializedName("sms_enabled")
        public boolean smsEnabled;
    }

}
