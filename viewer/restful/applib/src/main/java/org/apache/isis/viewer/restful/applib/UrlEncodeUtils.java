package org.apache.isis.viewer.restful.applib;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.ws.rs.WebApplicationException;


/**
 * Not API, so intentionally not visible outside this package.
 */
final class UrlEncodeUtils {
	
	private UrlEncodeUtils() {}

	static String encode(String value) {
		try {
			return URLEncoder.encode(value, Constants.URL_ENCODING_CHAR_SET);
		} catch (UnsupportedEncodingException e) {
			throw new WebApplicationException(e);
		}
	}

	static Map<String, String> urlEncode(Map<String, String> asMap) {
		Map<String,String> encodedMap = new HashMap<String, String>();
		Set<Entry<String, String>> entrySet = asMap.entrySet();
		for (Entry<String, String> entry : entrySet) {
			String value = entry.getValue();
			encodedMap.put(entry.getKey(), encode(value));
		}
		return encodedMap;
	}

}
