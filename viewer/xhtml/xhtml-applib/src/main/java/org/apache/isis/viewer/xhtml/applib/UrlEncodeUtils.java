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
package org.apache.isis.viewer.xhtml.applib;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

/**
 * Not API, so intentionally not visible outside this package.
 */
final class UrlEncodeUtils {

    private UrlEncodeUtils() {
    }

    static String encode(final String value) {
        try {
            return URLEncoder.encode(value, Constants.URL_ENCODING_CHAR_SET);
        } catch (final UnsupportedEncodingException e) {
            throw new WebApplicationException(e);
        }
    }

    static Map<String, String> urlEncode(final Map<String, String> asMap) {
        final Map<String, String> encodedMap = new HashMap<String, String>();
        final Set<Entry<String, String>> entrySet = asMap.entrySet();
        for (final Entry<String, String> entry : entrySet) {
            final String value = entry.getValue();
            encodedMap.put(entry.getKey(), encode(value));
        }
        return encodedMap;
    }

}
