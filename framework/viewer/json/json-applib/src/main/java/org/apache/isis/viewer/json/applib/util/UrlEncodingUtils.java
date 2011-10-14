package org.apache.isis.viewer.json.applib.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

public final class UrlEncodingUtils {

    public final static Function<String, String> FUNCTION = new Function<String, String>() {
        
        @Override
        public String apply(String input) {
            try {
                return URLDecoder.decode(input, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return "";
            }
        }
    };

    private UrlEncodingUtils() {}

    public static String urlDecode(String string) {
        return FUNCTION.apply(string);
    }

    public static List<String> urlDecode(List<String> values) {
        return Lists.transform(values, FUNCTION);
    }

    public static String[] urlDecode(String[] values) {
        final List<String> asList = Arrays.asList(values);
        return urlDecode(asList).toArray(new String[]{});
    }

    public static String asUrlEncoded(JsonNode jsonNode) {
        return UrlEncodingUtils.asUrlEncoded(jsonNode.toString());
    }

    public static String asUrlEncoded(final String str) {
        try {
            return URLEncoder.encode(str, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        }
    }

}
