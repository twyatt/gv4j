package com.brewengine.gv4j;

public class Phone {

	int id;
	String name;
	String phoneNumber;
	int type;
	boolean isEnabled;
	boolean isSmsEnabled;
	int policyBitmask;
	int behaviorOnRedirect;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public int getType() {
		return type;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public boolean isSmsEnabled() {
		return isSmsEnabled;
	}

	public int getPolicyBitmask() {
		return policyBitmask;
	}

	public int getBehaviorOnRedirect() {
		return behaviorOnRedirect;
	}

	@Override
	public String toString() {
		return "Phone{id=" + id + ", name=" + name + ", number=" + phoneNumber + ", enabled=" + isEnabled + "}";
	}
}
