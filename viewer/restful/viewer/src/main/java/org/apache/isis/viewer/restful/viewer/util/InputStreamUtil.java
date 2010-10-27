package org.apache.isis.viewer.restful.viewer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public final class InputStreamUtil {

    private InputStreamUtil() {}

	public static List<String> getArgs(final InputStream body) {
		// will be sorted by arg
	    final Map<String,String> args = new TreeMap<String,String>();
	    if (body == null) {
	        return listOfValues(args);
	    }
	    try {
	        final InputStreamReader inputStreamReader = new InputStreamReader(body);
	        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	        String readLine = bufferedReader.readLine();
	        while (readLine != null) {
		        final String[] keyValuePairs = readLine.split("&");
		        for (final String keyValuePair : keyValuePairs) {
		            final String[] keyThenValue = keyValuePair.split("=");
		            args.put(keyThenValue[0], keyThenValue[1]);
		        }
		        readLine = bufferedReader.readLine();
	        }
	        return listOfValues(args);
	    } catch (final IOException ex) {
	        throw new RuntimeException(ex);
	    }
	}

	private static ArrayList<String> listOfValues(final Map<String, String> args) {
		// returns the values in the order of the keys
		return new ArrayList<String>(args.values());
	}


}
