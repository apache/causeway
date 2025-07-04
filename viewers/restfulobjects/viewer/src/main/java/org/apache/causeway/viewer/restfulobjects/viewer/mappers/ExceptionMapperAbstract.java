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
package org.apache.causeway.viewer.restfulobjects.viewer.mappers;

import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.exceprecog.RootCauseFinder;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.applib.util.MediaTypes;
import org.apache.causeway.viewer.restfulobjects.rendering.ExceptionWithBody;
import org.apache.causeway.viewer.restfulobjects.rendering.ExceptionWithHttpStatusCode;
import org.apache.causeway.viewer.restfulobjects.viewer.mappers.entity.ExceptionDetail;
import org.apache.causeway.viewer.restfulobjects.viewer.mappers.entity.ExceptionPojo;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.serialization.SerializationStrategy;

import static org.apache.causeway.commons.internal.base._NullSafe.stream;

public abstract class ExceptionMapperAbstract<T extends Throwable> implements ExceptionMapper<T> {

    @Context protected HttpHeaders httpHeaders;
    @Autowired(required = false) protected List<RootCauseFinder> rootCauseFinders;

    Response buildResponse(final T ex) {
        return buildResponse(ex, determineStatusCode(ex));
    }

    Response buildResponse(final T ex, final RestfulResponse.HttpStatusCode httpStatusCode) {
        final String message = messageFor(ex);

        if(ex instanceof ExceptionWithBody) {
            final ExceptionWithBody exceptionWithBody = (ExceptionWithBody) ex;
            final JsonRepresentation body = exceptionWithBody.getBody();
            if(body != null) {
                return buildResponse(httpStatusCode, message, body);
            }
        }

        final ExceptionPojo exceptionPojo =
                new ExceptionPojo(
                        httpStatusCode.getStatusCode(), message,
                        detailIfRequired(httpStatusCode, ex)
                        );

        return buildResponse(httpStatusCode, exceptionPojo);
    }

    private Response buildResponse(
            final RestfulResponse.HttpStatusCode httpStatusCode,
            final String message,
            final JsonRepresentation body) {
        final ResponseBuilder builder = Response.status(httpStatusCode.getJaxrsStatusType());
        if (message != null) {
            builder.header(RestfulResponse.Header.WARNING.getName(), RestfulResponse.Header.WARNING.render(message));
        }

        final SerializationStrategy serializationStrategy = SerializationStrategy.JSON;

        // hmm; the mediaType doesn't seem to be specified in the RO spec
        builder.type(MediaTypes.toJakarta(serializationStrategy.type(RepresentationType.GENERIC)));
        builder.entity(body.toString());

        return builder.build();
    }

    private RestfulResponse.HttpStatusCode determineStatusCode(final T ex) {

        RestfulResponse.HttpStatusCode statusCode;

        statusCode = FailureUtil.getFailureStatusCodeIfAny(ex);
        if(statusCode!=null) {
            return statusCode;
        }

        final Optional<RecoverableException> recoverableIfAny = recoverableFor(ex);

        if(recoverableIfAny.isPresent()) {
            statusCode = RestfulResponse.HttpStatusCode.OK;
        } else if(ex instanceof ExceptionWithHttpStatusCode) {
            ExceptionWithHttpStatusCode exceptionWithHttpStatusCode = (ExceptionWithHttpStatusCode) ex;
            statusCode = exceptionWithHttpStatusCode.getHttpStatusCode();
        } else {
            statusCode = RestfulResponse.HttpStatusCode.INTERNAL_SERVER_ERROR;
        }
        return statusCode;
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
            final RestfulResponse.HttpStatusCode httpStatusCode,
            final Throwable ex) {

        return httpStatusCode == RestfulResponse.HttpStatusCode.NOT_FOUND ||
                httpStatusCode == RestfulResponse.HttpStatusCode.OK
                ? null
                : new ExceptionDetail(ex, rootCauseFinders);
    }

    private Response buildResponse(
            final RestfulResponse.HttpStatusCode httpStatusCode,
            final ExceptionPojo exceptionPojo) {
        final ResponseBuilder builder = Response.status(httpStatusCode.getJaxrsStatusType());

        var acceptableMediaTypes = httpHeaders.getAcceptableMediaTypes()
            .stream()
            .map(MediaTypes::fromJakarta)
            .toList();
        var serializationStrategy = acceptableMediaTypes.contains(MediaType.APPLICATION_XML)
            || acceptableMediaTypes.contains(RepresentationType.OBJECT_LAYOUT.getXmlMediaType())
                ? SerializationStrategy.XML
                : SerializationStrategy.JSON;

        final String message = exceptionPojo.getMessage();
        if (message != null) {
            builder.header(RestfulResponse.Header.WARNING.getName(), RestfulResponse.Header.WARNING.render(message));
        }

        builder.type(MediaTypes.toJakarta(serializationStrategy.type(RepresentationType.ERROR)));
        builder.entity(serializationStrategy.entity(exceptionPojo));

        return builder.build();
    }

}
