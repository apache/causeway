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
package org.apache.causeway.applib.services.urlencoding;

import java.nio.charset.StandardCharsets;

import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.memento._Mementos.EncoderDecoder;

/**
 * Defines a consistent way to convert strings to/from a form safe for use
 * within a URL.
 *
 * <p>
 *     The service is used by the framework to map mementos (derived from the
 *     state of the view model itself) into a form that can be used as a view
 *     model. When the framework needs to recreate the view model (for example
 *     to invoke an action on it), this URL is converted back into a view model
 *     memento, from which the view model can be hydrated.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface UrlEncodingService extends EncoderDecoder {

    /**
     * Converts the string (eg view model memento) into a string safe for use
     * within an URL
     */
    @Override
    String encode(final byte[] bytes);

    /**
     * Unconverts the string from its URL form into its original form URL.
     *
     * <p>
     *     Reciprocal of {@link #encode(byte[])}.
     * </p>
     */
    @Override
    byte[] decode(String str);

    default String encodeString(final String str) {
        return encode(_Strings.toBytes(str, StandardCharsets.UTF_8));
    }

    default String decodeToString(final String str) {
        return _Strings.ofBytes(decode(str), StandardCharsets.UTF_8);
    }

    // -- FACTORIES

    /**
     * Uses base64 with compression.
     */
    public static UrlEncodingService forTesting() {
        return new UrlEncodingService() {

            @Override
            public String encode(final byte[] bytes) {
                return _Strings.ofBytes(_Bytes.asCompressedUrlBase64.apply(bytes), StandardCharsets.UTF_8);
            }

            @Override
            public byte[] decode(final String str) {
                return _Bytes.ofCompressedUrlBase64.apply(_Strings.toBytes(str, StandardCharsets.UTF_8));
            }
        };
    }

    /**
     * Uses base64 without compression.
     */
    public static UrlEncodingService forTestingNoCompression() {
        return new UrlEncodingService() {

            @Override
            public String encode(final byte[] bytes) {
                return _Strings.ofBytes(_Bytes.asUrlBase64.apply(bytes), StandardCharsets.UTF_8);
            }

            @Override
            public byte[] decode(final String str) {
                return _Bytes.ofUrlBase64.apply(_Strings.toBytes(str, StandardCharsets.UTF_8));
            }
        };

    }

}
