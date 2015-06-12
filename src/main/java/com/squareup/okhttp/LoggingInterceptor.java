package com.squareup.okhttp;

import java.io.IOException;

// https://github.com/square/okhttp/wiki/Interceptors
public class LoggingInterceptor implements Interceptor {

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();

		long t1 = System.nanoTime();
		System.out.println(String.format("Sending request %s on %s%n%s",
				request.url(), chain.connection(), request.headers()));

		Response response = chain.proceed(request);

		long t2 = System.nanoTime();
		System.out.println(String.format("Received response for %s in %.1fms%n%s",
				response.request().url(), (t2 - t1) / 1e6d, response.headers()));

		return response;
	}

}