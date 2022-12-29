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
package org.apache.causeway.viewer.restfulobjects.viewer.resources;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.swagger.Format;
import org.apache.causeway.applib.services.swagger.SwaggerService;
import org.apache.causeway.applib.services.swagger.Visibility;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Component
@Path("/swagger")
@Log4j2
public class SwaggerSpecResource {

    private final SwaggerService swaggerService;
    private final InteractionService interactionService;

    @Context HttpHeaders httpHeaders;
    @Context HttpServletRequest httpServletRequest;

    @Inject
    public SwaggerSpecResource(
            final SwaggerService swaggerService,
            final InteractionService interactionService) {
        this.swaggerService = swaggerService;
        this.interactionService = interactionService;
        log.debug("<init>");
    }

    @Path("/private")
    @GET
    @Consumes({ MediaType.WILDCARD, MediaType.APPLICATION_JSON, "text/yaml" })
    @Produces({
        MediaType.APPLICATION_JSON, "text/yaml"
    })
    public String swaggerPrivate() {
        return _EndpointLogging.stringResponse(log, "GET /swagger/private",
                swagger(Visibility.PRIVATE));
    }

    @Path("/prototyping")
    @GET
    @Consumes({ MediaType.WILDCARD, MediaType.APPLICATION_JSON, "text/yaml" })
    @Produces({
        MediaType.APPLICATION_JSON, "text/yaml"
    })
    public String swaggerPrototyping() {
        return _EndpointLogging.stringResponse(log, "GET /swagger/prototyping",
                swagger(Visibility.PRIVATE_WITH_PROTOTYPING));
    }

    @Path("/public")
    @GET
    @Consumes({ MediaType.WILDCARD, MediaType.APPLICATION_JSON, "text/yaml" })
    @Produces({
        MediaType.APPLICATION_JSON, "text/yaml"
    })
    public String swaggerPublic() {
        return _EndpointLogging.stringResponse(log, "GET /swagger/public",
                swagger(Visibility.PUBLIC));
    }

    // -- HELPER

    private String swagger(final Visibility visibility) {

        val format = deriveFrom(httpHeaders);
        val callable = new MyCallable(swaggerService, visibility, format);

        val spec = interactionService.callAnonymous(callable);
        return spec;
    }

    private Format deriveFrom(final HttpHeaders httpHeaders) {
        final List<MediaType> acceptableMediaTypes = httpHeaders.getAcceptableMediaTypes();
        for (MediaType acceptableMediaType : acceptableMediaTypes) {
            if(acceptableMediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                return Format.JSON;
            }
        }
        final MediaType applYaml = new MediaType("application", "yaml");
        final MediaType textYaml = new MediaType("text", "yaml");
        for (MediaType acceptableMediaType : acceptableMediaTypes) {
            if (acceptableMediaType.isCompatible(applYaml) ||
                    acceptableMediaType.isCompatible(textYaml)) {
                return Format.YAML;
            }
        }
        return Format.JSON;
    }

    @RequiredArgsConstructor
    static class MyCallable implements Callable<String> {

        private final SwaggerService swaggerService;
        private final Visibility visibility;
        private final Format format;

        @Override
        public String call() throws Exception {

//            return format==Format.YAML
//                    ? _Strings.readFromResource(SwaggerSpecGenerator.class, "openapi-sample.yaml", StandardCharsets.UTF_8)
//                    : _Strings.readFromResource(SwaggerSpecGenerator.class, "openapi-sample.json", StandardCharsets.UTF_8);

            return swaggerService.generateSwaggerSpec(visibility, format);
        }

    }

}
