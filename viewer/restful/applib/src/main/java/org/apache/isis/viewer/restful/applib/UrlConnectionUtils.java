package org.apache.isis.viewer.restful.applib;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;


/**
 * Not API, so intentionally not visible outside this package.
 */
final class UrlConnectionUtils {
	
	private UrlConnectionUtils() {}

	static void writeMapToConnectionOutputStream(Map<String,String> formArgumentsByParameter,
			HttpURLConnection connection) throws IOException {
		OutputStream os = connection.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
		StringUtils.writeMap(formArgumentsByParameter, writer);
	}

	static Document readDocFromConnectionInputStream(
			HttpURLConnection connection) throws ParsingException,
			ValidityException, IOException {
		InputStream stream = connection.getInputStream();
		return new Builder().build(stream);
	}

	static HttpURLConnection createConnection(String uri)
	throws MalformedURLException, IOException {
		URL url = new URL(uri);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setInstanceFollowRedirects(false);
		connection.setRequestProperty("Content-Type", AbstractRestfulClient.MIME_TYPE);
		return connection;
	}

	static HttpURLConnection createPostConnection(String uri) throws IOException,
			ProtocolException {
		HttpURLConnection connection = createConnection(uri);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		return connection;
	}

	static HttpURLConnection createGetConnection(String uri) throws IOException,
			ProtocolException {
		HttpURLConnection connection = createConnection(uri);
		connection.setRequestMethod("GET");
		return connection;
	}

}
