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
package org.apache.isis.viewer.restfulobjects.rendering.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.rendering.Caching;
import org.apache.isis.viewer.restfulobjects.rendering.Responses;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ActionResultReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectActionReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndAction;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndCollection;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndCollection2;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndProperty;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndProperty2;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectCollectionReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectPropertyReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationService;

@DomainService(
        nature = NatureOfService.DOMAIN
)
@DomainServiceLayout(
        menuOrder = "" + Integer.MAX_VALUE // default
)
public class ContentNegotiationServiceForRestfulObjectsV1_0 implements ContentNegotiationService {

    private static final DateFormat ETAG_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @PostConstruct
    public void init(final Map<String, String> properties) {

    }

    @PreDestroy
    public void shutdown() {
    }

    @Override
    public ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final ObjectAdapter objectAdapter) {

        final DomainObjectReprRenderer renderer =
                new DomainObjectReprRenderer(renderContext2, null, JsonRepresentation.newMap());
        renderer.with(objectAdapter).includesSelf();

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE);

        if(renderContext2 instanceof RepresentationService.Context6) {
            final RepresentationService.Context6 context6 = (RepresentationService.Context6) renderContext2;
            final RepresentationService.Intent intent = context6.getIntent();
            if(intent == RepresentationService.Intent.JUST_CREATED) {
                responseBuilder.status(Response.Status.CREATED);
            }
        }

        final Version version = objectAdapter.getVersion();
        if (version != null && version.getTime() != null) {
            responseBuilder.tag(ETAG_FORMAT.format(version.getTime()));
        }

        return responseBuilder(responseBuilder);
    }

    @Override
    public ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final ObjectAndProperty objectAndProperty) {

        final ObjectPropertyReprRenderer renderer = new ObjectPropertyReprRenderer(renderContext2);
        renderer.with(objectAndProperty)
                .usingLinkTo(renderContext2.getAdapterLinkTo());

        if(objectAndProperty instanceof ObjectAndProperty2) {
            final ObjectAndProperty2 objectAndProperty2 = (ObjectAndProperty2) objectAndProperty;
            renderer
                .withMemberMode(objectAndProperty2.getMemberReprMode());

        }

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE);
        return responseBuilder;
    }

    @Override
    public ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final ObjectAndCollection objectAndCollection) {

        final ObjectCollectionReprRenderer renderer = new ObjectCollectionReprRenderer(renderContext2);
        renderer.with(objectAndCollection)
                .usingLinkTo(renderContext2.getAdapterLinkTo());

        if(objectAndCollection instanceof ObjectAndCollection2) {
            final ObjectAndCollection2 objectAndCollection2 = (ObjectAndCollection2) objectAndCollection;

            renderer.withMemberMode(objectAndCollection2.getMemberReprMode());
        }

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE);

        return responseBuilder(responseBuilder);
    }

    @Override
    public ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final ObjectAndAction objectAndAction) {

        final ObjectActionReprRenderer renderer = new ObjectActionReprRenderer(renderContext2);
        renderer.with(objectAndAction)
                .usingLinkTo(renderContext2.getAdapterLinkTo())
                .asStandalone();

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE);

        return responseBuilder(responseBuilder);
    }

    @Override
    public ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final ObjectAndActionInvocation objectAndActionInvocation) {

        final ActionResultReprRenderer renderer = new ActionResultReprRenderer(renderContext2, objectAndActionInvocation.getSelfLink());
        renderer.with(objectAndActionInvocation)
                .using(renderContext2.getAdapterLinkTo());

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE);
        Responses.addLastModifiedAndETagIfAvailable(responseBuilder, objectAndActionInvocation.getObjectAdapter().getVersion());

        return responseBuilder(responseBuilder);
    }

    /**
     * For easy subclassing to further customize, eg additional headers
     */
    protected ResponseBuilder responseBuilder(final ResponseBuilder responseBuilder) {
        return responseBuilder;
    }


}
