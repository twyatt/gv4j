package com.brewengine.gv4j;

import com.google.gson.Gson;
import com.squareup.okhttp.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    /**
     * Determines if we are logged in by checking for the presence of the 'gvx' cookie.
     *
     * @return
     */
    public boolean isLoggedIn() {
        return findCookieByName("gvx") != null;
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

        // 1st login request we send username
        response = login(loginForm, username, password);
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected response: " + response);
        }
//        System.out.println("response="+response.body().string());
        body = response.body().string();
        document = Jsoup.parse(body);
        loginForm = getLoginFormElement(document);
        if (loginForm == null) {
            throw new IOException("Failed to find login form element (2).");
        }

        // 2nd login request we send password
        response = login(loginForm, username, password);
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected response: " + response + "(2)");
        }
//        System.out.println("response="+response.body().string());

        if (!isLoggedIn()) {
            throw new IOException("Missing gvx cookie.");
        }
    }

    private Response login(Element form, String username, String password) throws IOException {
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
                if (name.equalsIgnoreCase("Email") && value.isEmpty()) {
                    builder.add(name, username);
                } else if (name.equalsIgnoreCase("Passwd") && value.isEmpty()) {
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

        return client.newCall(request).execute();
    }

    /**
     * Requests to be logged out.
     *
     * Optionally, for safe measure, after performing the logout you can clear
     * the cookies:
     * <code>
     *     getCookieManager().getCookieStore().removeAll();
     * </code>
     *
     * @throws IOException
     */
    public void logout() throws IOException {
        Request request = new Request.Builder()
                .url("https://www.google.com/voice/m/logout")
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected response: " + response);
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
        builder.append("&fp_id0=").append(phone.getId());
        builder.append("&fp_name0=").append(phone.getName());
        builder.append("&fp_num0=").append(phone.getPhoneNumber());
        builder.append("&fp_type0=").append(phone.getType());
        builder.append("&fp_pol0=").append(phone.getPolicyBitmask());
        builder.append("&fp_sen0=").append(phone.isSmsEnabled());
        builder.append("&fp_red0=").append(phone.getBehaviorOnRedirect());
        builder.append("&fp_en0=").append(enable);
        builder.append("&v=").append(API_VERSION);
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
