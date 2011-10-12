package org.apache.isis.viewer.json.applib.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.util.URLUtils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class UrlDecodeUtils {

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

    private UrlDecodeUtils() {}

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

}
