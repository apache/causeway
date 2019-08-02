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
package org.apache.isis.applib.client;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.concurrent.Future;
import java.util.function.Function;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.isis.commons.internal.base._Strings;

/**
 * 
 * @since 2.0
 */
public class ResponseDigest<T> {

    /** synchronous response processing */
    public static <T> ResponseDigest<T> of(Response response, Class<T> entityType) {
        return new ResponseDigest<>(response, entityType).digest();
    }

    /** a-synchronous response failure processing */
    public static <T> ResponseDigest<T> ofAsyncFailure(
            Future<Response> asyncResponse, 
            Class<T> entityType, 
            Exception failure) {

        Response response;
        try {
            response = asyncResponse.isDone() ? asyncResponse.get() : null;
        } catch (Exception e) {
            response = null;
        }

        final ResponseDigest<T> failureDigest = new ResponseDigest<>(response, entityType);
        return failureDigest.digestAsyncFailure(asyncResponse.isCancelled(), failure);
    }

    private final Response response;
    private final Class<T> entityType;

    private T entity;
    private Exception failureCause;


    protected ResponseDigest(Response response, Class<T> entityType) {
        this.response = response;
        this.entityType = entityType;
    }

    public boolean isSuccess() {
        return !isFailure();
    }

    public boolean isFailure() {
        return failureCause!=null;
    }

    public T get(){
        return entity;
    }

    public Exception getFailureCause(){
        return failureCause;
    }

    public T ifSuccessGetOrElseMap(Function<Exception, T> failureMapper) {
        return isSuccess() 
                ? get()
                        : failureMapper.apply(getFailureCause());
    }

    public <X> X ifSuccessMapOrElseMap(Function<T, X> successMapper, Function<Exception, X> failureMapper) {
        return isSuccess() 
                ? successMapper.apply(get())
                        : failureMapper.apply(getFailureCause());
    }

    // -- HELPER

    private ResponseDigest<T> digest() {

        if(response==null) {
            entity = null;
            failureCause = new NoSuchElementException();
            return this;
        }

        if(!response.hasEntity()) {
            entity = null;
            failureCause = new NoSuchElementException(defaultFailureMessage(response));
            return this;
        }

        if(response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
            entity = null;
            failureCause = new RestfulClientException(defaultFailureMessage(response));
            return this;
        }

        try {
            entity = response.readEntity(entityType);
        } catch (Exception e) {
            entity = null;
            failureCause = new RestfulClientException("failed to read JAX-RS response content", e);
        }

        return this;
    }

    private ResponseDigest<T> digestAsyncFailure(boolean isCancelled, Exception failure) {

        entity = null;


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

    private String defaultFailureMessage(Response response) {
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




}
