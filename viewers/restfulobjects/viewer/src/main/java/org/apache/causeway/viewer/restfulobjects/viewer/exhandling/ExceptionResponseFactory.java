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
package org.apache.causeway.viewer.restfulobjects.viewer.exhandling;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.exceprecog.RootCauseFinder;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.rendering.ExceptionWithBody;
import org.apache.causeway.viewer.restfulobjects.rendering.ExceptionWithHttpStatusCode;
import org.apache.causeway.viewer.restfulobjects.viewer.exhandling.entity.ExceptionDetail;
import org.apache.causeway.viewer.restfulobjects.viewer.exhandling.entity.ExceptionPojo;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.serialization.SerializationStrategy;

import static org.apache.causeway.commons.internal.base._NullSafe.stream;

public record ExceptionResponseFactory(
    List<RootCauseFinder> rootCauseFinders) {

    public ResponseEntity<Object> buildResponse(final Exception ex, final HttpHeaders httpHeaders) {
        return buildResponse(ex, determineStatusCode(ex), httpHeaders);
    }

    ResponseEntity<Object> buildResponse(final Exception ex, final HttpStatus httpStatus, final HttpHeaders httpHeaders) {
        final String message = messageFor(ex);

        if(ex instanceof ExceptionWithBody exceptionWithBody
            && exceptionWithBody.body()!=null) {
            return buildResponse(httpStatus, message, exceptionWithBody.body());
        }

        var exceptionPojo = new ExceptionPojo(
                httpStatus.value(),
                message,
                detailIfRequired(httpStatus, ex));

        return buildResponse(httpStatus, exceptionPojo, httpHeaders);
    }

    // -- HELPER

    private ResponseEntity<Object> buildResponse(
            final HttpStatus httpStatus,
            final String message,
            final JsonRepresentation body) {

        var builder = ResponseEntity.status(httpStatus);

        if (message != null) {
            builder = builder.header(RestfulResponse.Header.WARNING.getName(), RestfulResponse.Header.WARNING.render(message));
        }

        // hmm; the mediaType doesn't seem to be specified in the RO spec
        builder.contentType(SerializationStrategy.JSON.type(RepresentationType.GENERIC));
        return builder.body((Object)body.toString());
    }

    private HttpStatus determineStatusCode(final Exception ex) {
        var statusCode = FailureUtil.getFailureStatusCodeIfAny(ex);
        if(statusCode!=null) return statusCode;

        if(recoverableFor(ex).isPresent()) {
            return HttpStatus.OK;
        } else if(ex instanceof ExceptionWithHttpStatusCode exceptionWithHttpStatusCode) {
            return exceptionWithHttpStatusCode.httpStatus();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static String messageFor(final Throwable ex) {

        final Optional<RecoverableException> recoverableIfAny = recoverableFor(ex);

        return (recoverableIfAny.isPresent() ? recoverableIfAny.get() : ex).getMessage();
    }

    private static Optional<RecoverableException> recoverableFor(final Throwable ex) {
        final List<Throwable> chain = _Exceptions.getCausalChain(ex);

        final Optional<RecoverableException> recoverableIfAny = stream(chain)
                .filter(t->t instanceof RecoverableException)
                .map(t->(RecoverableException)t)
                .findFirst();

        return recoverableIfAny;
    }

    private ExceptionDetail detailIfRequired(
            final HttpStatus httpStatus,
            final Throwable ex) {

        return httpStatus == HttpStatus.NOT_FOUND
                || httpStatus == HttpStatus.OK
            ? null
            : new ExceptionDetail(ex, rootCauseFinders);
    }

    private ResponseEntity<Object> buildResponse(
            final HttpStatus httpStatus,
            final ExceptionPojo exceptionPojo,
            final HttpHeaders httpHeaders) {

        var builder = ResponseEntity.status(httpStatus);

        var acceptableMediaTypes = Try.call(()->httpHeaders.getAccept())
            .getValue()
            .orElseGet(List::of);
        var serializationStrategy = acceptableMediaTypes.contains(MediaType.APPLICATION_XML)
                || acceptableMediaTypes.contains(RepresentationType.OBJECT_LAYOUT.getXmlMediaType())
            ? SerializationStrategy.XML
            : SerializationStrategy.JSON;

        final String message = exceptionPojo.getMessage();
        if (message != null) {
            builder = builder.header(RestfulResponse.Header.WARNING.getName(), RestfulResponse.Header.WARNING.render(message));
        }

        return builder
            .contentType(serializationStrategy.type(RepresentationType.ERROR))
            .body(serializationStrategy.entity(exceptionPojo));
    }

}
