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

import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonMapper;

@Provider
public class ObjectNotFoundExceptionMapper extends ExceptionMapperAbstract<ObjectNotFoundException> {

    @Override
    public Response toResponse(final ObjectNotFoundException ex) {


        final HttpStatusCode statusCode = HttpStatusCode.NOT_FOUND;
        final ResponseBuilder builder =
                Response.status(statusCode.getJaxrsStatusType());

        final String message = ex.getMessage();
        if (message != null) {
            builder.header(RestfulResponse.Header.WARNING.getName(), RestfulResponse.Header.WARNING.render(message));
        }

        setContentTypeOn(builder);

        String body = toBody(ex);

        if(body != null) {
            builder.entity(body);
        }

        return builder.build();
    }

    protected String toBody(final ObjectNotFoundException ex) {
        final boolean xml = isXmlButNotHtml();
        if (!xml) {
            String body;
            try {
                body = JsonMapper.instance().write(new ExceptionPojo(ex));
            } catch (final Exception e) {
                // fallback
                body = "{ \"message\": \"" + ex.getMessage() + "\" }";
            }
            return body;
        }
        return null;
    }
}
