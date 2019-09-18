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
package org.apache.isis.viewer.restfulobjects.server.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.health.Health;
import org.apache.isis.applib.services.health.HealthCheckService;
import org.apache.isis.runtime.sessiontemplate.AbstractIsisSessionTemplate;
import org.apache.isis.security.authentication.health.HealthAuthSession;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.health.HealthResource;
import org.apache.isis.viewer.restfulobjects.rendering.Caching;
import org.apache.isis.viewer.restfulobjects.rendering.Responses;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.rendering.util.JsonWriterUtil;

/**
 * Implementation note: it seems to be necessary to annotate the implementation
 * with {@link Path} rather than the interface (at least under RestEasy 1.0.2
 * and 1.1-RC2).
 */
//@Path("/health")
public class HealthResourceServerside extends ResourceAbstract implements HealthResource {

    @Override
    @GET
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_HEALTH })
    public Response health() {

        final Response[] responseHolder = new Response[1];
        final AbstractIsisSessionTemplate template = new HealthServiceSession();

        final HealthAuthSession authSession = new HealthAuthSession();
        template.execute(authSession, responseHolder);

        return responseHolder[0];
    }

    @Override
    public Response deleteHealthNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting the health resource is not allowed.");
    }

    @Override
    public Response putHealthNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Putting to the health resource is not allowed.");
    }

    @Override
    public Response postHealthNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Posting to the version resource is not allowed.");
    }

    private class HealthServiceSession extends AbstractIsisSessionTemplate {

        @Override
        protected void doExecuteWithTransaction(final Object context) {
            super.doExecuteWithTransaction(context);
            init(RepresentationType.HEALTH, Where.NOWHERE, RepresentationService.Intent.NOT_APPLICABLE);

            final HealthReprRenderer renderer = new HealthReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
            final Health health;
            if(healthService != null) {
                health = healthService.check();
                renderer.with(health);
            } else {
                health = Health.ok();
            }
            renderer.includesSelf();

            final Response.ResponseBuilder responseBuilder = health.getResult()
                    ? Responses.ofOk(renderer, Caching.NONE)
                            : Response.serverError()
                            .entity(JsonWriterUtil.jsonFor(renderer.render()))
                            .cacheControl(Caching.NONE.getCacheControl());

                    final Response[] responseHolder = (Response[]) context;
                    responseHolder[0] = responseBuilder.build();
        }

        @Inject
        HealthCheckService healthService;

    }
}