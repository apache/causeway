/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.restfulobjects.applib.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

public final class UrlEncodingUtils {

    public final static Function<String, String> FUNCTION = new Function<String, String>() {

        @Override
        public String apply(final String input) {
            try {
                return URLDecoder.decode(input, "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                return "";
            }
        }
    };

    private UrlEncodingUtils() {
    }

    public static String urlDecode(final String string) {
        return FUNCTION.apply(string);
    }

    public static List<String> urlDecode(final List<String> values) {
        return _Lists.map(values, FUNCTION);
    }

    public static String[] urlDecode(final String[] values) {
        final List<String> asList = Arrays.asList(values);
        return urlDecode(asList).toArray(new String[] {});
    }

    public static String urlEncode(final JsonNode jsonNode) {
        return urlEncode(jsonNode.toString());
    }

    public static String urlEncode(final JsonRepresentation jsonRepresentation ) {
        return urlEncode(jsonRepresentation.toString());
    }

    public static String urlEncode(final String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        }
    }

}
