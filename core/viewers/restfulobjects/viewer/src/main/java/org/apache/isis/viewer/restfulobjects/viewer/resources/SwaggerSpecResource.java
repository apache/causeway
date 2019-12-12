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
package org.apache.isis.viewer.restfulobjects.viewer.resources;

import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.runtime.session.IsisSessionFactory;
import org.apache.isis.webapp.util.IsisWebAppUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Path("/swagger")
public class SwaggerSpecResource {

    @Context HttpHeaders httpHeaders;
    @Context HttpServletRequest httpServletRequest;

    @Path("/private")
    @GET
    @Consumes({ MediaType.WILDCARD, MediaType.APPLICATION_JSON, "text/yaml" })
    @Produces({
        MediaType.APPLICATION_JSON, "text/yaml"
    })
    public String swaggerPrivate() {
        return swagger(SwaggerService.Visibility.PRIVATE);
    }

    @Path("/prototyping")
    @GET
    @Consumes({ MediaType.WILDCARD, MediaType.APPLICATION_JSON, "text/yaml" })
    @Produces({
        MediaType.APPLICATION_JSON, "text/yaml"
    })
    public String swaggerPrototyping() {
        return swagger(SwaggerService.Visibility.PRIVATE_WITH_PROTOTYPING);
    }

    @Path("/public")
    @GET
    @Consumes({ MediaType.WILDCARD, MediaType.APPLICATION_JSON, "text/yaml" })
    @Produces({
        MediaType.APPLICATION_JSON, "text/yaml"
    })
    public String swaggerPublic() {
        return swagger(SwaggerService.Visibility.PUBLIC);
    }

    private String swagger(final SwaggerService.Visibility visibility) {
        
        val servletContext = httpServletRequest.getServletContext();
        
        val swaggerService = IsisWebAppUtils.getManagedBean(SwaggerService.class, servletContext);
        val isisSessionFactory = IsisWebAppUtils.getManagedBean(IsisSessionFactory.class, servletContext);
        
        val format = deriveFrom(httpHeaders);
        val callable = new MyCallable(swaggerService, visibility, format);
        
        val spec = isisSessionFactory.doInSession(callable);
        return spec;
    }

    private SwaggerService.Format deriveFrom(final HttpHeaders httpHeaders) {
        final List<MediaType> acceptableMediaTypes = httpHeaders.getAcceptableMediaTypes();
        for (MediaType acceptableMediaType : acceptableMediaTypes) {
            if(acceptableMediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                return SwaggerService.Format.JSON;
            }
        }
        final MediaType applYaml = new MediaType("application", "yaml");
        final MediaType textYaml = new MediaType("text", "yaml");
        for (MediaType acceptableMediaType : acceptableMediaTypes) {
            if (acceptableMediaType.isCompatible(applYaml) ||
                    acceptableMediaType.isCompatible(textYaml)) {
                return SwaggerService.Format.YAML;
            }
        }
        return SwaggerService.Format.JSON;
    }

    @RequiredArgsConstructor
    static class MyCallable implements Callable<String> {

        private final SwaggerService swaggerService;
        private final SwaggerService.Visibility visibility;
        private final SwaggerService.Format format;

        @Override
        public String call() throws Exception {
            return swaggerService.generateSwaggerSpec(visibility, format);
        }

    }

}
