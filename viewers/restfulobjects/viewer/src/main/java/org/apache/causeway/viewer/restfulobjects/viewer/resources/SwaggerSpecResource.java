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

import java.util.concurrent.Callable;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.swagger.Format;
import org.apache.causeway.applib.services.swagger.SwaggerService;
import org.apache.causeway.applib.services.swagger.Visibility;

import lombok.extern.slf4j.Slf4j;

@Component
@Path("/swagger")
@Slf4j
public class SwaggerSpecResource {

    private final SwaggerService swaggerService;
    private final InteractionService interactionService;

    @Autowired HttpHeaders httpHeaders;
    @Autowired HttpServletRequest httpServletRequest;

    @Inject
    public SwaggerSpecResource(
            final @Context SwaggerService swaggerService,
            final @Context InteractionService interactionService) {
        this.swaggerService = swaggerService;
        this.interactionService = interactionService;
        log.debug("<init>");
    }

    @Path("/private")
    @GET
    //@Consumes({ MediaType.WILDCARD, MediaType.APPLICATION_JSON, "text/yaml" })
    @Produces({
        MediaType.APPLICATION_JSON_VALUE, "text/yaml"
    })
    public String swaggerPrivate() {
        return _EndpointLogging.stringResponse(log, "GET /swagger/private",
                swagger(Visibility.PRIVATE));
    }

    @Path("/prototyping")
    @GET
    //@Consumes({ MediaType.WILDCARD, MediaType.APPLICATION_JSON_VALUE, "text/yaml" })
    @Produces({
        MediaType.APPLICATION_JSON_VALUE, "text/yaml"
    })
    public String swaggerPrototyping() {
        return _EndpointLogging.stringResponse(log, "GET /swagger/prototyping",
                swagger(Visibility.PRIVATE_WITH_PROTOTYPING));
    }

    @Path("/public")
    @GET
    //@Consumes({ MediaType.WILDCARD, MediaType.APPLICATION_JSON_VALUE, "text/yaml" })
    @Produces({
        MediaType.APPLICATION_JSON_VALUE, "text/yaml"
    })
    public String swaggerPublic() {
        return _EndpointLogging.stringResponse(log, "GET /swagger/public",
                swagger(Visibility.PUBLIC));
    }

    // -- HELPER

    private String swagger(final Visibility visibility) {

        var format = deriveFrom(httpHeaders);
        var callable = new MyCallable(swaggerService, visibility, format);

        var spec = interactionService.callAnonymous(callable);
        return spec;
    }

    private Format deriveFrom(final HttpHeaders httpHeaders) {
        var acceptableMediaTypes = httpHeaders.getAccept();
        for (MediaType acceptableMediaType : acceptableMediaTypes) {
            if(acceptableMediaType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                return Format.JSON;
            }
        }
        final MediaType applYaml = new MediaType("application", "yaml");
        final MediaType textYaml = new MediaType("text", "yaml");
        for (MediaType acceptableMediaType : acceptableMediaTypes) {
            if (acceptableMediaType.isCompatibleWith(applYaml) ||
                    acceptableMediaType.isCompatibleWith(textYaml)) {
                return Format.YAML;
            }
        }
        return Format.JSON;
    }

    record MyCallable(
        SwaggerService swaggerService,
        Visibility visibility,
        Format format) implements Callable<String> {

        @Override
        public String call() throws Exception {
//            return format==Format.YAML
//                    ? _Strings.readFromResource(SwaggerSpecGenerator.class, "openapi-sample.yaml", StandardCharsets.UTF_8)
//                    : _Strings.readFromResource(SwaggerSpecGenerator.class, "openapi-sample.json", StandardCharsets.UTF_8);
            return swaggerService.generateSwaggerSpec(visibility, format);
        }

    }

}
