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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import org.apache.isis.core.commons.exceptions.ExceptionUtils;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonMapper;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

//@Path("/") // FIXME: workaround for TomEE ... but breaks the RestEasy TCK tests so commented out:-(
@Provider
public class RestfulObjectsApplicationExceptionMapper extends ExceptionMapperAbstract<RestfulObjectsApplicationException> {

    @Override
    public Response toResponse(final RestfulObjectsApplicationException ex) {
        final ResponseBuilder builder = Response.status(ex.getHttpStatusCode().getJaxrsStatusType());

        final String message = ex.getMessage();
        if (message != null) {
            builder.header(RestfulResponse.Header.WARNING.getName(), RestfulResponse.Header.WARNING.render(message));
        }

        setContentTypeOn(builder);

        final boolean xml = isXmlButNotHtml();
        String body = null;
        if(!xml) {
            final JsonRepresentation bodyRepr = ex.getBody();
            if (bodyRepr != null) {
                body = bodyRepr.toString();
            }
        }
        if (body == null) {
            body = toBody(ex);
        }

        if(body != null) {
            builder.entity(body);
        }

        return builder.build();
    }

    protected String toBody(final RestfulObjectsApplicationException ex) {
        if(ex.getHttpStatusCode() == RestfulResponse.HttpStatusCode.NOT_FOUND) {
            final ExceptionPojo exceptionPojo = new ExceptionPojo(ex);
            try {
                return JsonMapper.instance().write(exceptionPojo);
            } catch (final Exception e) {
                // fallback
                return null;
            }
        }

        final boolean xml = isXmlButNotHtml();
        final ExceptionPojoWithDetail exceptionPojo = new ExceptionPojoWithDetail(ex);
        if (!xml) {
            try {
                return JsonMapper.instance().write(exceptionPojo);
            } catch (final Exception e) {
                // fallback
                return "{ \"exception\": \"" + ExceptionUtils.getFullStackTrace(ex) + "\" }";
            }
        } else {
            final StringBuilder buf = new StringBuilder();
            buf.append("<exception>\n");
            buf.append("  <httpStatusCode>").append(exceptionPojo.getHttpStatusCode())
                    .append("</httpStatusCode>/n");
            buf.append("  <message>").append(exceptionPojo.getMessage()).append("</message>/n");
            buf.append("  <stackTrace>/n");
            for (String line : exceptionPojo.getStackTrace()) {
                buf.append("    <stackTraceElement>").append(line).append("    </stackTraceElement>/n");
            }
            buf.append("  </stackTrace>/n");
            buf.append("</exception>");

            return buf.toString();
        }
    }
}
