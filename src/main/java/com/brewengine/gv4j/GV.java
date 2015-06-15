package com.brewengine.gv4j;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.gson.Gson;
import com.squareup.okhttp.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class GV {

	public final int API_VERSION = 13;

	private final OkHttpClient client = new OkHttpClient();
	private final CookieManager cookieManager;

	private final Gson gson = new Gson();

	public GV() {
		this(new CookieManager());
	}

	public GV(CookieManager cookieManager) {
		this.cookieManager = cookieManager;

		// http://stackoverflow.com/a/24267060/196486
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		client.setCookieHandler(cookieManager);

//		client.networkInterceptors().add(new LoggingInterceptor());
	}

	/**
	 * Attempts to login to GV using the provided credentials.
	 *
	 * @param username
	 * @param password
	 * @throws IOException
	 */
	public void login(String username, String password) throws IOException {
		checkNotNull(username);
		checkNotNull(password);

		Request request = new Request.Builder()
				.url("https://accounts.google.com/ServiceLogin?service=grandcentral&continue=https://www.google.com/voice/m?initialauth&followup=https://www.google.com/voice/m?initialauth")
				.build();

		Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) {
			throw new IOException("Unexpected response: " + response);
		}

		String body = response.body().string();
		Document document = Jsoup.parse(body);
		Element loginForm = getLoginFormElement(document);
		if (loginForm == null) {
			throw new IOException("Failed to find login form element.");
		}

		login(loginForm, username, password);
	}

	private void login(Element form, String username, String password) throws IOException {
		String action = form.attr("action");
		Elements inputs = form.getElementsByTag("input");
		if (inputs == null || inputs.size() == 0) {
			throw new IOException("Login form inputs not found.");
		}

		FormEncodingBuilder builder = new FormEncodingBuilder();
		for (Element input : inputs) {
			String name = input.attr("name");
			String value = input.attr("value");

			if (!name.isEmpty()) {
				if (name.equalsIgnoreCase("Email")) {
					builder.add(name, username);
				} else if (name.equalsIgnoreCase("Passwd")) {
					builder.add(name, password);
				} else {
					builder.add(name, value);
				}
			}
		}
		RequestBody requestBody = builder.build();

		Request request = new Request.Builder()
				.url(action)
				.post(requestBody)
				.build();

		Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) {
			throw new IOException("Unexpected response: " + response);
		}

		/*
		 * Confirm we are logged in by checking for the presence of the 'gvx'
		 * cookie.
		 */
		HttpCookie gvx = findCookieByName("gvx");
		if (gvx == null) {
			throw new IOException("Missing gvx cookie.");
		}
	}

	/**
	 * Fetches GV settings.
	 *
	 * @return
	 * @throws IOException
	 */
	public Settings fetchSettings() throws IOException {
		GVJson json = fetchSettingsJson();
		return Settings.valueOf(json);
	}

	private GVJson fetchSettingsJson() throws IOException {
		HttpCookie gvx = findCookieByName("gvx");
		checkState(gvx != null, "Missing gvx cookie.");

		MediaType contentType = MediaType.parse("text/plain; charset=UTF-8");
		String content = "{gvx: \"" + gvx.getValue() + "\"}";
		RequestBody body = RequestBody.create(contentType, content);

		Request request = new Request.Builder()
				.url("https://www.google.com/voice/m/x?m=set&v=" + API_VERSION)
				.post(body)
				.build();

		Response response = client.newCall(request).execute();
		if (!response.isSuccessful()) {
			throw new IOException("Unexpected response: " + response);
		}

		String json = response.body().string().substring(")]}',".length()).trim();
		return gson.fromJson(json, GVJson.class);
	}

	/**
	 * Enables the specified phone.
	 *
	 * Phone objects can be acquired from a Settings object using the
	 * fetchSettings() method.
	 *
	 * @param phone
	 * @throws IOException
	 */
	public void enablePhone(Phone phone) throws IOException {
		togglePhone(phone, true);
	}

	/**
	 * Disables the specified phone.
	 *
	 * Phone objects can be acquired from a Settings object using the
	 * fetchSettings() method.
	 *
	 * @param phone
	 * @throws IOException
	 */
	public void disablePhone(Phone phone) throws IOException {
		togglePhone(phone, false);
	}

	private void togglePhone(Phone phone, boolean enable) throws IOException {
		checkNotNull(phone);

		HttpCookie gvx = findCookieByName("gvx");
		if (gvx == null) {
			throw new IllegalStateException("Missing gvx cookie.");
		}

		MediaType contentType = MediaType.parse("text/plain; charset=UTF-8");
		String content = "{gvx: \"" + gvx.getValue() + "\"}";
		RequestBody body = RequestBody.create(contentType, content);

		StringBuilder builder = new StringBuilder();
		builder.append("m=set");
		builder.append("&fp_id0=" + phone.getId());
		builder.append("&fp_name0=" + phone.getName());
		builder.append("&fp_num0=" + phone.getPhoneNumber());
		builder.append("&fp_type0=" + phone.getType());
		builder.append("&fp_pol0=" + phone.getPolicyBitmask());
		builder.append("&fp_sen0=" + phone.isSmsEnabled());
		builder.append("&fp_red0=" + phone.getBehaviorOnRedirect());
		builder.append("&fp_en0=" + enable);
		builder.append("&v=" + API_VERSION);
		String params = builder.toString();

		Request request = new Request.Builder()
				.url("https://www.google.com/voice/m/x?" + params)
				.post(body)
				.build();

		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			phone.isEnabled = enable;
		} else {
			throw new IOException("Unexpected response: " + response);
		}
	}

	private HttpCookie findCookieByName(String name) {
		List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
		for (HttpCookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				return cookie;
			}
		}
		return null;
	}

	private static Element getLoginFormElement(Document document) {
		return document.select("form#gaia_loginform").first();
	}

}
