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
package org.apache.isis.viewer.restfulobjects.rendering;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonMapper;
import org.apache.isis.viewer.restfulobjects.rendering.util.JsonWriterUtil;

import lombok.val;

public final class Responses {

    private Responses(){}

    public static Response.ResponseBuilder ofNoContent() {
        return of(RestfulResponse.HttpStatusCode.NO_CONTENT);
    }

    public static Response.ResponseBuilder ofNotFound() {
        return of(RestfulResponse.HttpStatusCode.NOT_FOUND);
    }

    public static Response.ResponseBuilder ofNotImplemented() {
        return of(RestfulResponse.HttpStatusCode.NOT_IMPLEMENTED);
    }

    public static Response.ResponseBuilder ofOk(
            final ReprRenderer<?> renderer,
            final Caching caching) {
        return ofOk(renderer, caching, null);
    }

    /**
     * @param rootRepresentationIfAny - if specified, is used for entity; otherwise the renderer is used.  The idea is that the renderer will be set up to render to some sub-node of root representation
     */
    public static Response.ResponseBuilder ofOk(
            final ReprRenderer<?> renderer,
            final Caching caching,
            final JsonRepresentation rootRepresentationIfAny) {

        final JsonRepresentation representation = renderer.render();
        // if a rootRepresentation is provided, then the assumption is that the rendered
        // will be rendering to some submap of the rootRepresentation
        final JsonRepresentation entityRepresentation =
                rootRepresentationIfAny != null? rootRepresentationIfAny : representation;

        final MediaType mediaType = renderer.getMediaType();

        final Date now = now(renderer);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        final Response.ResponseBuilder response =
                of(RestfulResponse.HttpStatusCode.OK)
                .header("Date", dateFormat.format(now))
                .type(mediaType)
                .cacheControl(caching.getCacheControl())
                .entity(JsonWriterUtil.jsonFor(entityRepresentation, inferPrettyPrinting(renderer)));

        return response;
    }

    private static Date now(final ReprRenderer<?> renderer) {
        if(renderer instanceof ReprRendererAbstract) {
            ((ReprRendererAbstract<?>)renderer).getResourceContext().getMetaModelContext().getServiceRegistry()
            .lookupServiceElseFail(ClockService.class).getClock().nowAsJavaUtilDate();
        }
        return new Date();
    }

    protected static Response.ResponseBuilder of(final RestfulResponse.HttpStatusCode httpStatusCode) {
        return Response.status(httpStatusCode.getJaxrsStatusType()).type(MediaType.APPLICATION_JSON_TYPE);
    }

    public static Response.ResponseBuilder mediaType(
            final Response.ResponseBuilder responseBuilder,
            final MediaType mediaType) {
        responseBuilder.type(mediaType);
        return responseBuilder;
    }

    public static JsonMapper.PrettyPrinting inferPrettyPrinting(final ReprRenderer<?> renderer) {

        if(renderer instanceof ReprRendererAbstract) {
            val systemEnvironment =  ((ReprRendererAbstract<?>) renderer).getResourceContext()
                    .getMetaModelContext().getSystemEnvironment();
            return systemEnvironment.isPrototyping()
                    ? JsonMapper.PrettyPrinting.ENABLE
                    : JsonMapper.PrettyPrinting.DISABLE;
        }

        return JsonMapper.PrettyPrinting.DISABLE;

    }

}
