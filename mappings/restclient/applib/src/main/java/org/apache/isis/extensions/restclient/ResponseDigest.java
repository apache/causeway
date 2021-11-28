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
package org.apache.isis.extensions.restclient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Function;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.client.RepresentationTypeSimplifiedV2;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.viewer.restfulobjects.applib.dtos.ScalarValueDtoV2;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Log4j2
public class ResponseDigest<T> {

    /**
     * synchronous response processing (single entity)
     * @param <T>
     * @param response
     * @param entityType
     */
    public static <T> ResponseDigest<T> of(
            final @NonNull Response response,
            final @NonNull Class<T> entityType) {

        return new ResponseDigest<>(response, entityType, null).digest();
    }

    /**
     * synchronous response processing (list of entities)
     * @param <T>
     * @param response
     * @param entityType
     * @param genericType
     */
    public static <T> ResponseDigest<T> ofList(
            final @NonNull Response response,
            final @NonNull Class<T> entityType,
            final @NonNull GenericType<List<T>> genericType) {

        return new ResponseDigest<>(response, entityType, genericType).digest();
    }

    /** a-synchronous response failure processing */
    public static <T> ResponseDigest<T> ofAsyncFailure(
            final Future<Response> asyncResponse,
            final Class<T> entityType,
            final Exception failure) {

        Response response;
        try {
            response = asyncResponse.isDone() ? asyncResponse.get() : null;
        } catch (Exception e) {
            response = null;
        }

        final ResponseDigest<T> failureDigest = new ResponseDigest<>(response, entityType, null);
        return failureDigest.digestAsyncFailure(asyncResponse.isCancelled(), failure);
    }

    private final Response response;
    private final Class<T> entityType;
    private final GenericType<List<T>> genericType;

    private Can<T> entities;
    private Exception failureCause;


    protected ResponseDigest(final Response response, final Class<T> entityType, final GenericType<List<T>> genericType) {
        this.response = response;
        this.entityType = entityType;
        this.genericType = genericType;
    }

    /**
     * @return whether the REST endpoint replied with a success status code.
     */
    public boolean isSuccess() {
        return !isFailure();
    }

    /**
     * @return whether the REST endpoint replied with a failure status code.
     */
    public boolean isFailure() {
        return failureCause!=null;
    }

    /**
     * @return (non-null), optionally the result if cardinality is exactly ONE
     */
    public Optional<T> getEntity(){
        return getEntities().getSingleton();
    }

    /**
     * @return (non-null), the entities replied by the REST endpoint supporting any cardinality ZERO, ONE or more.
     */
    public Can<T> getEntities(){
        return entities;
    }

    /**
     * @return (nullable), the failure case (if any), when the REST endpoint replied with a failure status code
     */
    public @Nullable Exception getFailureCause(){
        return failureCause;
    }

    /**
     * @param failureMapper - fallback, to calculate a result from given failure exception
     * @return the result if cardinality is exactly ONE, otherwise the result of applying the failure to the {@code failureMapper}
     */
    public T singletonOrElseMapFailure(final Function<Exception, T> failureMapper) {
        return isSuccess()
                ? getEntity().orElseGet(()->failureMapper.apply(new NoSuchElementException()))
                : failureMapper.apply(getFailureCause());
    }

    /**
     * @param failureMapper - fallback, to calculate a result from given failure exception
     * @return the result of any cardinality, otherwise the result of applying the failure to the {@code failureMapper}
     */
    public Can<T> multipleOrElseMapFailure(final Function<Exception, Can<T>> failureMapper) {
        return isSuccess()
                ? getEntities()
                : failureMapper.apply(getFailureCause());
    }

    // -- HELPER

    private ResponseDigest<T> digest() {

        if(response==null) {
            entities = Can.empty();
            failureCause = new NoSuchElementException();
            return this;
        }

        // a valid result corresponding to object not found, which is not an error per se
        if(response.getStatusInfo().getStatusCode() == 404) {
            entities = Can.empty();
            return this;
        }

        if(!response.hasEntity()) {
            entities = Can.empty();
            failureCause = new NoSuchElementException(defaultFailureMessage(response));
            return this;
        }

        if(response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
            entities = Can.empty();
            failureCause = new RestfulClientException(defaultFailureMessage(response));
            return this;
        }

        // see if we can extract the returned representation type (repr-type) from the header
        val contentTypeHeaderString = response.getHeaderString("Content-Type");
        val reprType = RepresentationTypeSimplifiedV2.parseContentTypeHeaderString(contentTypeHeaderString)
                .orElse(null);
        if(reprType==null) {
            entities = Can.empty();
            failureCause = new RestfulClientException(String.format(
                    "Invalid REST response, cannot parse header's Content-Type '%s' for the repr-type to use",
                    contentTypeHeaderString));
            return this;
        }

        try {

            if(genericType==null) {
                // when response is a singleton
                val singleton = readSingle(reprType);
                entities = singleton==null
                        ? Can.empty()
                        : Can.ofSingleton(singleton);
            } else {
                // when response is a list
                entities = Can.ofCollection(readList(reprType));
            }

        } catch (Exception e) {
            entities = Can.empty();
            failureCause = new RestfulClientException("failed to read JAX-RS response content", e);
        }

        return this;
    }

    private T readSingle(final RepresentationTypeSimplifiedV2 reprType)
            throws JsonParseException, JsonMappingException, IOException {

        log.debug("readSingle({})", reprType);

        if(reprType.isValue()
                || reprType.isValues()) {
            val mapper = new ObjectMapper();
            val jsonInput = response.readEntity(String.class);
            val scalarValueDto = mapper.readValue(jsonInput, ScalarValueDtoV2.class);
            return extractValue(scalarValueDto);
        }
        return response.<T>readEntity(entityType);
    }

    private List<T> readList(final RepresentationTypeSimplifiedV2 reprType)
            throws JsonParseException, JsonMappingException, IOException {

        log.debug("readList({})", reprType);

        if(reprType.isValues()
                || reprType.isValue()) {
            val mapper = new ObjectMapper();
            val jsonInput = response.readEntity(String.class);
            final List<ScalarValueDtoV2> scalarValueDtoList =
                    mapper.readValue(
                            jsonInput,
                            mapper.getTypeFactory().constructCollectionType(List.class, ScalarValueDtoV2.class));

            final List<T> resultList = new ArrayList<>(scalarValueDtoList.size());
            for(val valueBody : scalarValueDtoList) {
                // explicit loop, for simpler exception propagation
                resultList.add(extractValue(valueBody));
            }
            return resultList;

        }
        return response.readEntity(genericType);
    }

    private ResponseDigest<T> digestAsyncFailure(final boolean isCancelled, final Exception failure) {

        entities = Can.empty();

        if(isCancelled) {
            failureCause = new RestfulClientException("Async JAX-RS request was canceled", failure);
            return this;
        }

        if(response==null) {
            failureCause = new RestfulClientException("Async JAX-RS request failed", failure);
            return this;
        }

        failureCause = new RestfulClientException("Async JAX-RS request failed "
                + defaultFailureMessage(response), failure);
        return this;

    }

    private String defaultFailureMessage(final Response response) {
        String failureMessage = "non-successful JAX-RS response: " +
                String.format("%s (Http-Status-Code: %d)",
                        response.getStatusInfo().getReasonPhrase(),
                        response.getStatus());

        if(response.hasEntity()) {
            try {
                String jsonContent = _Strings.read((InputStream) response.getEntity(), StandardCharsets.UTF_8);
                return failureMessage + "\nContent:\n" + jsonContent;
            } catch (Exception e) {
                // ignore
            }
        }

        return failureMessage;
    }

    // -- VALUE TYPE HANDLING

    private T extractValue(final ScalarValueDtoV2 scalarValueDto)
            throws JsonParseException, JsonMappingException, IOException {
        return _Casts.uncheckedCast(scalarValueDto.getValue());
    }


}
