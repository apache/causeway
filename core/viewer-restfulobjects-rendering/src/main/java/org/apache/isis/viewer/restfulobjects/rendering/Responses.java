/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.restfulobjects.rendering;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.util.JsonWriterUtil;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public final class Responses {

    private Responses(){}

    public static Response.ResponseBuilder ofNoContent() {
        return of(RestfulResponse.HttpStatusCode.NO_CONTENT);
    }

    public static Response.ResponseBuilder ofNotFound() {
        return of(RestfulResponse.HttpStatusCode.NOT_FOUND);
    }

    public static Response.ResponseBuilder ofOk(
            final ReprRenderer<?, ?> renderer,
            final Caching caching) {
        return ofOk(renderer, caching, null, null);
    }

    /**
     * @param rootRepresentationIfAny - if specified, is used for entity; otherwise the renderer is used.  The idea is that the renderer will be set up to render to some sub-node of root representation
     */
    public static Response.ResponseBuilder ofOk(
            final ReprRenderer<?, ?> renderer,
            final Caching caching,
            final JsonRepresentation rootRepresentationIfAny) {
        return ofOk(renderer, caching, null, rootRepresentationIfAny);
    }

    public static Response.ResponseBuilder ofOk(
            final ReprRenderer<?, ?> renderer,
            final Caching caching,
            final Version version) {
        return ofOk(renderer, caching, version, null);
    }

    /**
     * @param rootRepresentationIfAny - if specified, is used for entity; otherwise the renderer is used.  The idea is that the renderer will be set up to render to some sub-node of root representation
     */
    public static Response.ResponseBuilder ofOk(
            final ReprRenderer<?, ?> renderer,
            final Caching caching,
            final Version version,
            final JsonRepresentation rootRepresentationIfAny) {

        final JsonRepresentation representation = renderer.render();
        // if a rootRepresentation is provided, then the assumption is that the rendered
        // will be rendering to some submap of the rootRepresentation
        final JsonRepresentation entityRepresentation =
                rootRepresentationIfAny != null? rootRepresentationIfAny : representation;

        final MediaType mediaType = renderer.getMediaType();

        final Date now = IsisContext.getServiceRegistry()
                .lookupServiceElseFail(ClockService.class).nowAsDateTime().toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        final Response.ResponseBuilder response =
                of(RestfulResponse.HttpStatusCode.OK)
                .header("Date", dateFormat.format(now))
                .type(mediaType)
                .cacheControl(caching.getCacheControl())
                .entity(JsonWriterUtil.jsonFor(entityRepresentation));
        return addLastModifiedAndETagIfAvailable(response, version);
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

    public static Response.ResponseBuilder addLastModifiedAndETagIfAvailable(
            final Response.ResponseBuilder responseBuilder,
            final Version version) {
        if (version != null && version.getTime() != null) {
            final Date time = version.getTime();
            responseBuilder.lastModified(time);
            responseBuilder.tag(asETag(time));
        }
        return responseBuilder;
    }

    private static EntityTag asETag(final Date time) {
        final String utcTime = ISODateTimeFormat.basicDateTime().print(new DateTime(time));
        return new EntityTag(utcTime, true);
    }
}
