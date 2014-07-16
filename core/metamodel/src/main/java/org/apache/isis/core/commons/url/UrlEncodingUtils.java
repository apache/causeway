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
package org.apache.isis.core.commons.url;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.google.common.base.Charsets;
import com.google.common.base.Function;

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

    public final static Function<String, String> FUNCTION_NULLSAFE = new Function<String, String>() {

        @Override
        public String apply(final String input) {
            if (input == null) {
                return null;
            }
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

    public static String urlDecodeNullSafe(final String string) {
        return FUNCTION_NULLSAFE.apply(string);
    }

    public static String urlEncode(final String str) {
        try {
            return URLEncoder.encode(str, Charsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        }
    }


}
