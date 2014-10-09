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

import java.util.Date;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.util.JsonWriterUtil;

public final class Responses {

    private Responses(){}

    public static Response.ResponseBuilder ofNoContent() {
        return of(RestfulResponse.HttpStatusCode.NO_CONTENT);
    }

    public static Response.ResponseBuilder ofOk(final ReprRenderer<?, ?> renderer, final Caching caching) {
        return ofOk(renderer, caching, null);
    }

    public static Response.ResponseBuilder ofOk(final ReprRenderer<?, ?> renderer, final Caching caching, final Version version) {
        final MediaType mediaType = renderer.getMediaType();
        final Response.ResponseBuilder response = of(RestfulResponse.HttpStatusCode.OK).type(mediaType).cacheControl(caching.getCacheControl()).entity(JsonWriterUtil.jsonFor(renderer.render()));
        return addLastModifiedAndETagIfAvailable(response, version);
    }

    protected static Response.ResponseBuilder of(final RestfulResponse.HttpStatusCode httpStatusCode) {
        return Response.status(httpStatusCode.getJaxrsStatusType()).type(MediaType.APPLICATION_JSON_TYPE);
    }

    public static Response.ResponseBuilder addLastModifiedAndETagIfAvailable(final Response.ResponseBuilder responseBuilder, final Version version) {
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
