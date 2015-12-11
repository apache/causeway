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
package org.apache.isis.viewer.restfulobjects.server;

import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.isis.core.commons.exceptions.ExceptionUtils;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonMapper;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

//@Path("/") // FIXME: workaround for TomEE ... but breaks the RestEasy TCK tests so commented out:-(
@Provider
public class RestfulObjectsApplicationExceptionMapper implements ExceptionMapper<RestfulObjectsApplicationException> {

    @Context
    HttpHeaders httpHeaders;

    @Override
    public Response toResponse(final RestfulObjectsApplicationException ex) {
        final ResponseBuilder builder = Response.status(ex.getHttpStatusCode().getJaxrsStatusType());

        final String message = ex.getMessage();
        if (message != null) {
            builder.header(RestfulResponse.Header.WARNING.getName(), RestfulResponse.Header.WARNING.render(message));
        }

        // xml handling (only if also not text/html, ie what browsers would send).
        boolean html = false;
        final List<MediaType> acceptableMediaTypes = httpHeaders.getAcceptableMediaTypes();
        for (MediaType acceptableMediaType : acceptableMediaTypes) {
            html = html || (acceptableMediaType.getType().equals("text") && acceptableMediaType.getSubtype().equals("html"));
        }
        boolean xml = false;
        if(!html) {
            for (MediaType acceptableMediaType : acceptableMediaTypes) {
                xml = xml || acceptableMediaType.getSubtype().equals("xml");
            }
        }

        // body and content-type
        final JsonRepresentation bodyRepr = ex.getBody();
        final Throwable cause = ex.getCause();
        if (bodyRepr != null) {
            if(!xml) {
                final String body = bodyRepr.toString();
                builder.entity(body);
                builder.type(MediaType.APPLICATION_JSON); // generic; the spec doesn't define what the media type should be
            } else {
                builder.type(MediaType.APPLICATION_XML);
            }
        } else if(cause == null) {
            if(!xml) {
                builder.type(MediaType.APPLICATION_JSON); // generic; the spec doesn't define what the media type should be
            } else {
                builder.type(MediaType.APPLICATION_XML);
            }
        } else {
            if(!xml) {
                String body;
                try {
                    body = JsonMapper.instance().write(RestfulObjectsApplicationExceptionPojo.create(cause));
                } catch (final Exception e) {
                    // fallback
                    body = "{ \"exception\": \"" + ExceptionUtils.getFullStackTrace(cause) + "\" }";
                }
                builder.entity(body);
                builder.type(RestfulMediaType.APPLICATION_JSON_ERROR);
            } else {
                final RestfulObjectsApplicationExceptionPojo exceptionPojo = RestfulObjectsApplicationExceptionPojo.create(cause);
                final StringBuilder buf = new StringBuilder();
                buf.append("<exception>\n");
                buf.append("  <httpStatusCode>").append(exceptionPojo.getHttpStatusCode()).append("</httpStatusCode>/n");
                buf.append("  <message>").append(exceptionPojo.getMessage()).append("</message>/n");
                buf.append("  <stackTrace>/n");
                for (String line : exceptionPojo.getStackTrace()) {
                    buf.append("    <stackTraceElement>").append(line).append("    </stackTraceElement>/n");
                }
                buf.append("  </stackTrace>/n");
                buf.append("</exception>");
                builder.entity(buf.toString());
                builder.type(RestfulMediaType.APPLICATION_XML_ERROR);
            }
        }

        return builder.build();
    }


}
