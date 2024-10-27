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
package org.apache.causeway.viewer.restfulobjects.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.client.Entity;

import org.apache.causeway.applib.services.bookmark.Bookmark;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.schema.common.v2.BlobDto;
import org.apache.causeway.schema.common.v2.ClobDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import lombok.Getter;
import lombok.NonNull;

/**
 * @since 2.0 {@index}
 */
public class ActionParameterListBuilder {

    private final Map<String, String> actionParameters = new LinkedHashMap<>();

    @Getter
    private final Map<String, Class<?>> actionParameterTypes = new LinkedHashMap<>();

    private final RestfulClient restfulClient;

    /**
     * @deprecated  - use {@link RestfulClient#arguments()}
     */
    @Deprecated
    public ActionParameterListBuilder() {
        this(null);
    }
    public ActionParameterListBuilder(RestfulClient restfulClient) {
        this.restfulClient = restfulClient;
    }

    public ActionParameterListBuilder addActionParameter(final String parameterName, final String parameterValue) {
        actionParameters.put(parameterName, parameterValue != null
                ? value("\"" + parameterValue + "\"")
                : value(JSON_NULL_LITERAL));
        actionParameterTypes.put(parameterName, String.class);
        return this;
    }

    public ActionParameterListBuilder addActionParameter(final String parameterName, final int parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        actionParameterTypes.put(parameterName, int.class);
        return this;
    }

    public ActionParameterListBuilder addActionParameter(final String parameterName, final long parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        actionParameterTypes.put(parameterName, long.class);
        return this;
    }

    public ActionParameterListBuilder addActionParameter(final String parameterName, final byte parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        actionParameterTypes.put(parameterName, byte.class);
        return this;
    }

    public ActionParameterListBuilder addActionParameter(final String parameterName, final short parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        actionParameterTypes.put(parameterName, short.class);
        return this;
    }

    public ActionParameterListBuilder addActionParameter(final String parameterName, final double parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        actionParameterTypes.put(parameterName, double.class);
        return this;
    }

    public ActionParameterListBuilder addActionParameter(final String parameterName, final float parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        actionParameterTypes.put(parameterName, float.class);
        return this;
    }

    public ActionParameterListBuilder addActionParameter(final String parameterName, final boolean parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        actionParameterTypes.put(parameterName, boolean.class);
        return this;
    }

    public ActionParameterListBuilder addActionParameter(final String parameterName, final Blob blob) {
        var blobDto = new BlobDto();
        blobDto.setName(blob.getName());
        blobDto.setMimeType(blob.getMimeType().getBaseType());
        blobDto.setBytes(blob.getBytes());
        var fundamentalTypeDto = new ValueWithTypeDto();
        fundamentalTypeDto.setType(ValueType.BLOB);
        fundamentalTypeDto.setBlob(blobDto);
        actionParameters.put(parameterName, value(CommonDtoUtils.getFundamentalValueAsJson(fundamentalTypeDto)));
        actionParameterTypes.put(parameterName, Blob.class);
        return this;
    }

    public ActionParameterListBuilder addActionParameter(final String parameterName, final Clob clob) {
        var clobDto = new ClobDto();
        clobDto.setName(clob.getName());
        clobDto.setMimeType(clob.getMimeType().getBaseType());
        clobDto.setChars(clob.asString());
        var fundamentalTypeDto = new ValueWithTypeDto();
        fundamentalTypeDto.setType(ValueType.CLOB);
        fundamentalTypeDto.setClob(clobDto);
        actionParameters.put(parameterName, value(CommonDtoUtils.getFundamentalValueAsJson(fundamentalTypeDto)));
        actionParameterTypes.put(parameterName, Blob.class);
        return this;
    }

    public ActionParameterListBuilder addActionParameter(final String parameterName,
            final @NonNull Map<String, Object> map) {
        var nestedJson = JsonUtils.toStringUtf8(map);
        actionParameters.put(parameterName, value(nestedJson));
        actionParameterTypes.put(parameterName, Map.class);
        return this;
    }

    public ActionParameterListBuilder addActionParameter(
            final @NonNull String parameterName,
            final @NonNull Bookmark bookmark) {
        if (this.restfulClient == null) {
            throw new IllegalStateException("Use RestfulClient#arguments() to create this builder");
        }
        actionParameters.put(parameterName, valueHref( bookmark) );
        actionParameterTypes.put(parameterName, Map.class);
        return this;
    }

    private String valueHref(Bookmark bookmark) {
        String hrefValue  = asAbsoluteHref(bookmark);
//        String hrefValue  = "\"" + asAbsoluteHref(bookmark) + "\"";
        Map<String, String> map = Map.of("href", hrefValue);
        return value(JsonUtils.toStringUtf8(map));
    }

    private String asAbsoluteHref(Bookmark bookmark) {
        return String.format("%s%s", restfulClient.getConfig().getRestfulBaseUrl(), asRelativeHref(bookmark));
    }

    private String asRelativeHref(Bookmark bookmark) {
        return String.format("objects/%s/%s", bookmark.getLogicalTypeName(), bookmark.getIdentifier());
    }

    public <T> ActionParameterListBuilder addActionParameter(
            final String parameterName,
            final @NonNull Class<T> type,
            final @Nullable T object) {
        var nestedJson = object!=null
            ? JsonUtils.toStringUtf8(object)
            : "NULL"; // see ValueSerializerDefault.ENCODED_NULL
        actionParameters.put(parameterName, value(nestedJson));
        actionParameterTypes.put(parameterName, type);
        return this;
    }

    /**
     * For transport of {@link ValueDecomposition} over REST.
     * @see RestfulClient#digestValue(javax.ws.rs.core.Response, org.apache.causeway.applib.value.semantics.ValueSemanticsProvider)
     */
    public ActionParameterListBuilder addActionParameter(final String parameterName, final ValueDecomposition decomposition) {
        return addActionParameter(parameterName, decomposition.stringify());
    }

    public Entity<String> build() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{\n")
        .append(actionParameters.entrySet().stream()
                .map(this::toJson)
                .collect(Collectors.joining(",\n")))
        .append("\n}");

        return Entity.json(sb.toString());
    }

    // -- HELPER

    private static final String JSON_NULL_LITERAL = "null";

    private String value(final String valueLiteral) {
        return "{\"value\" : " + valueLiteral + "}";
    }

    private String toJson(final Map.Entry<String, String> entry) {
        return "   \""+entry.getKey()+"\": "+entry.getValue();
    }

}
