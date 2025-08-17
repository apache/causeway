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
package org.apache.causeway.applib.value;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

import lombok.SneakyThrows;

/**
 * The data URI scheme is a uniform resource identifier (URI) scheme that provides
 * a way to include data in-line in Web pages as if they were external resources.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Data_URI_scheme">wikipedia</a>
 * @since 4.0
 */
public record DataUri(
        String mediaType,
        List<String> parameters,
        Encoding encoding,
        byte[] data) {

    public enum Encoding {
        NONE,
        BASE64
    }

    @SneakyThrows
    public static DataUri parse(String dataURI) {
        var uri = new URI(dataURI);
        if(!"data".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Invalid Data URI format");
        }
        String[] parts = uri.getSchemeSpecificPart().split(",", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid Data URI format");
        }

        String metadata = parts[0];
        String dataPart = parts[1];

        // Extract media type and encoding
        String[] metadataParts = metadata.split(";");
        var mediaType = metadataParts[0];
        var encoding =  metadataParts.length > 1
            ?  metadataParts[metadataParts.length - 1].equals("base64")
                ? Encoding.BASE64
                : Encoding.NONE
            : Encoding.NONE;

        var parameters = IntStream.range(1, metadataParts.length - (encoding == Encoding.BASE64 ? 1 : 0))
            .mapToObj(i->metadataParts[i])
            .toList();

        return new DataUri(mediaType, parameters, encoding, decodeData(encoding, dataPart));
    }

    // canonical constructor
    @SneakyThrows
    public DataUri(
            @Nullable String mediaType,
            @Nullable List<String> parameters,
            @Nullable Encoding encoding,
            @Nullable byte[] data) {
        this.mediaType = StringUtils.hasLength(mediaType) ? mediaType : "text/plain;charset=US-ASCII";
        this.parameters = parameters!=null ? List.copyOf(parameters) : List.of();
        this.encoding = encoding!=null ? encoding : Encoding.NONE;
        this.data = data!=null ? data : new byte[0];
        // validate
        new URI(toExternalForm());
    }

    @Override
    public String toString() {
        return toExternalForm();
    }

    /**
     * Constructs a string representation of this {@code DataUri}, that
     * can be parsed via {@link DataUri#parse(String)}.
     */
    public String toExternalForm() {
        var sb = new StringBuilder("data:")
            .append(mediaType);
        if(!parameters.isEmpty()) {
            parameters.forEach(param->sb.append(";").append(param));
        }
        if (encoding == Encoding.BASE64) {
            sb.append(";base64");
        }
        sb.append(",").append(encodeData());
        return sb.toString();
    }

    /**
     * Equality by value.
     * @implNote override needed, otherwise the data array would be compared by reference
     */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        return o instanceof DataUri other
            ? Objects.equals(this.mediaType, other.mediaType)
                    && Objects.equals(this.parameters, other.parameters)
                    && Objects.equals(this.encoding, other.encoding)
                    && Arrays.equals(this.data, other.data)
            : false;
    }

    // -- HELPER

    private String encodeData() {
        return encoding == Encoding.BASE64
            ? Base64.getEncoder().encodeToString(data)
            : URLEncoder.encode(new String(data, StandardCharsets.UTF_8), StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static byte[] decodeData(Encoding encoding, String dataPart) {
        return encoding == Encoding.BASE64
                ? Base64.getDecoder().decode(dataPart)
                : dataPart.getBytes(StandardCharsets.UTF_8);
    }

}

