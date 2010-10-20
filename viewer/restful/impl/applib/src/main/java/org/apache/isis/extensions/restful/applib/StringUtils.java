package org.apache.isis.extensions.restful.applib;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * Not API, so intentionally not visible outside this package.
 */
final class StringUtils {
	
	private StringUtils() {}

	/**
	 * Converts a list of objects [a, 1, b, 2] into a map {a -> 1; b -> 2}
	 */
	static Map<String, String> asMap(String... paramArgs) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		boolean param = true;
		String paramStr = null;
		for (String paramArg : paramArgs) {
			if (param) {
				paramStr = paramArg;
			} else {
				String arg = paramArg;
				map.put(paramStr, arg);
				paramStr = null;
			}
			param = !param;
		}
		if (paramStr != null) {
			throw new IllegalArgumentException(
					"Must have equal number of parameters and arguments");
		}
		return map;
	}

	static void writeMap(Map<String, String> formArgumentsByParameter,
			Writer writer) {
		try {
			Set<Map.Entry<String,String>> parameterArguments = formArgumentsByParameter.entrySet();
			for (Map.Entry<String, String> parameterArgument : parameterArguments) {
				writer.write(parameterArgument.getKey());
				writer.write("=");
				writer.write(parameterArgument.getValue());
				writer.write("\n");
			}
			writer.flush();
		} catch (IOException e) {
			throw new RestfulClientException(e);
		}
	}

	static String asString(Map<String, String> formArgumentsByParameter)  {
		StringWriter sw = new StringWriter();
		writeMap(formArgumentsByParameter, sw);
		return sw.toString();
	}


}
