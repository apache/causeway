/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.htmx;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
public final class HtmxRouteCodec {

    static final int MAX_DECODED_LENGTH = 1024;
    static final int MAX_ENCODED_LENGTH = 4096;

    private final String basePath;

    public HtmxRouteCodec(final String basePath) {
        this.basePath = normalizeBasePath(basePath);
    }

    public String basePath() {
        return basePath;
    }

    public String rootPath() {
        return basePath;
    }

    public boolean isRootPath(final String rawPath) {
        return basePath.equals(rawPath) || (basePath + "/").equals(rawPath);
    }

    public String objectPath(final HtmxObjectRoute route) {
        return basePath + "/object/" + encodeSegment(route.logicalTypeName()) + "/" + encodeSegment(route.objectId());
    }

    public HtmxObjectRoute parseObjectPath(final String rawPath) {
        if (rawPath == null || rawPath.length() > basePath.length() + (MAX_ENCODED_LENGTH * 2) + 16) {
            throw invalid();
        }
        final var prefix = basePath + "/object/";
        if (!rawPath.startsWith(prefix)) {
            throw invalid();
        }
        final var remainder = rawPath.substring(prefix.length());
        final var separator = remainder.indexOf('/');
        if (separator <= 0 || separator != remainder.lastIndexOf('/') || separator == remainder.length() - 1) {
            throw invalid();
        }
        final var route = new HtmxObjectRoute(
                decodeSegment(remainder.substring(0, separator)),
                decodeSegment(remainder.substring(separator + 1)));
        if (!objectPath(route).equals(rawPath)) {
            throw invalid();
        }
        return route;
    }

    public static String encodeSegment(final String value) {
        validateDecoded(value);
        final var bytes = value.getBytes(StandardCharsets.UTF_8);
        final var encoded = new StringBuilder(bytes.length);
        for (byte valueByte : bytes) {
            final int unsigned = valueByte & 0xff;
            if (isUnreserved(unsigned)) {
                encoded.append((char) unsigned);
            } else {
                encoded.append('%');
                encoded.append(Character.toUpperCase(Character.forDigit((unsigned >>> 4) & 0xf, 16)));
                encoded.append(Character.toUpperCase(Character.forDigit(unsigned & 0xf, 16)));
            }
        }
        if (encoded.length() > MAX_ENCODED_LENGTH) {
            throw invalid();
        }
        return encoded.toString();
    }

    public static String decodeSegment(final String encoded) {
        if (encoded == null || encoded.isEmpty() || encoded.length() > MAX_ENCODED_LENGTH) {
            throw invalid();
        }
        final var bytes = new ByteArrayOutputStream(encoded.length());
        for (int index = 0; index < encoded.length();) {
            final char current = encoded.charAt(index);
            if (current == '%') {
                if (index + 2 >= encoded.length()) {
                    throw invalid();
                }
                final int high = Character.digit(encoded.charAt(index + 1), 16);
                final int low = Character.digit(encoded.charAt(index + 2), 16);
                if (high < 0 || low < 0) {
                    throw invalid();
                }
                bytes.write((high << 4) | low);
                index += 3;
            } else {
                if (current > 0x7f || !isUnreserved(current)) {
                    throw invalid();
                }
                bytes.write(current);
                index++;
            }
        }
        final String decoded;
        try {
            decoded = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes.toByteArray()))
                    .toString();
        } catch (CharacterCodingException ex) {
            throw invalid();
        }
        validateDecoded(decoded);
        if (!encodeSegment(decoded).equals(encoded)) {
            throw invalid();
        }
        return decoded;
    }

    public static String normalizeBasePath(final String configured) {
        if (configured == null) {
            throw new IllegalArgumentException("HTMX viewer base path is required.");
        }
        var value = configured.trim();
        if (!value.startsWith("/") || value.contains("//") || value.indexOf('?') >= 0 || value.indexOf('#') >= 0) {
            throw new IllegalArgumentException("HTMX viewer base path must be an absolute path without query or fragment.");
        }
        while (value.length() > 1 && value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        if ("/".equals(value) || value.length() > 200) {
            throw new IllegalArgumentException("HTMX viewer base path must name a bounded non-root path.");
        }
        for (String segment : value.substring(1).split("/")) {
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
                throw new IllegalArgumentException("HTMX viewer base path contains an invalid segment.");
            }
            for (int index = 0; index < segment.length(); index++) {
                final char character = segment.charAt(index);
                if (!(isUnreserved(character) || character == '-')) {
                    throw new IllegalArgumentException("HTMX viewer base path contains an invalid character.");
                }
            }
        }
        return value;
    }

    private static void validateDecoded(final String value) {
        if (value == null || value.isEmpty() || value.length() > MAX_DECODED_LENGTH
                || ".".equals(value) || "..".equals(value)) {
            throw invalid();
        }
        for (int offset = 0; offset < value.length();) {
            final int codePoint = value.codePointAt(offset);
            if (Character.isISOControl(codePoint) || Character.isSurrogate((char) codePoint)
                    || codePoint == '/' || codePoint == '\\') {
                throw invalid();
            }
            offset += Character.charCount(codePoint);
        }
    }

    private static boolean isUnreserved(final int value) {
        return value >= 'a' && value <= 'z'
                || value >= 'A' && value <= 'Z'
                || value >= '0' && value <= '9'
                || value == '-' || value == '.' || value == '_' || value == '~';
    }

    private static InvalidHtmxRouteException invalid() {
        return new InvalidHtmxRouteException("The requested application route is invalid.");
    }
}
