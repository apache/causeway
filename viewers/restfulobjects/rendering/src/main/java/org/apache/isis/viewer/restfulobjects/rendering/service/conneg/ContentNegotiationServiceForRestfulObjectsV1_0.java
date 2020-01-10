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
package org.apache.isis.viewer.restfulobjects.rendering.service.conneg;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.domain.DomainObjectList;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.Caching;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.Responses;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
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
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;

import lombok.val;

@Service
@Named("isisRoRendering.ContentNegotiationServiceForRestfulObjectsV1_0")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("RestfulObjectsV1_0")
public class ContentNegotiationServiceForRestfulObjectsV1_0 implements ContentNegotiationService {

    private boolean strictAcceptChecking;

    @PostConstruct
    public void init() {
        this.strictAcceptChecking = configuration.getViewer().getRestfulobjects().isStrictAcceptChecking();
    }

    @Override
    public ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter) {

        final List<MediaType> list = resourceContext.getAcceptableMediaTypes();
        ensureCompatibleAcceptHeader(RepresentationType.DOMAIN_OBJECT, list);

        final ResponseBuilder responseBuilder = buildResponseTo(
                resourceContext, objectAdapter, JsonRepresentation.newMap(), null);

        return responseBuilder(responseBuilder);
    }

    /**
     * Not API
     */
    ResponseBuilder buildResponseTo(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter,
            final JsonRepresentation representationIfAnyRequired,
            final JsonRepresentation rootRepresentation) {

        final DomainObjectReprRenderer renderer =
                new DomainObjectReprRenderer(resourceContext, null, representationIfAnyRequired);
        renderer.with(objectAdapter).includesSelf();

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE, rootRepresentation);

        {
            final RepresentationService.Intent intent = resourceContext.getIntent();
            if(intent == RepresentationService.Intent.JUST_CREATED) {
                responseBuilder.status(Response.Status.CREATED);
            }
        }
        
        return responseBuilder;
    }

    @Override
    public ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ObjectAndProperty objectAndProperty) {

        final List<MediaType> list = resourceContext.getAcceptableMediaTypes();
        ensureCompatibleAcceptHeader(RepresentationType.OBJECT_PROPERTY, list);

        final ObjectPropertyReprRenderer renderer = new ObjectPropertyReprRenderer(resourceContext);
        renderer.with(objectAndProperty)
        .usingLinkTo(resourceContext.getObjectAdapterLinkTo());

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
            final IResourceContext resourceContext,
            final ObjectAndCollection objectAndCollection) {

        final List<MediaType> list = resourceContext.getAcceptableMediaTypes();
        ensureCompatibleAcceptHeader(RepresentationType.OBJECT_COLLECTION, list);

        final ResponseBuilder responseBuilder =
                buildResponseTo(resourceContext, objectAndCollection, JsonRepresentation.newMap(), null);

        return responseBuilder(responseBuilder);
    }

    /**
     * Not API
     */
    ResponseBuilder buildResponseTo(
            final IResourceContext resourceContext,
            final ObjectAndCollection objectAndCollection,
            final JsonRepresentation representation,
            final JsonRepresentation rootRepresentation) {
        final ObjectCollectionReprRenderer renderer =
                new ObjectCollectionReprRenderer(resourceContext, null, null, representation);
        renderer.with(objectAndCollection)
        .usingLinkTo(resourceContext.getObjectAdapterLinkTo());

        if(objectAndCollection instanceof ObjectAndCollection2) {
            final ObjectAndCollection2 objectAndCollection2 = (ObjectAndCollection2) objectAndCollection;

            renderer.withMemberMode(objectAndCollection2.getMemberReprMode());
        }

        return Responses.ofOk(renderer, Caching.NONE, rootRepresentation);
    }

    @Override
    public ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ObjectAndAction objectAndAction) {

        final List<MediaType> list = resourceContext.getAcceptableMediaTypes();
        ensureCompatibleAcceptHeader(RepresentationType.OBJECT_ACTION, list);

        final ObjectActionReprRenderer renderer = new ObjectActionReprRenderer(resourceContext);
        renderer.with(objectAndAction)
        .usingLinkTo(resourceContext.getObjectAdapterLinkTo())
        .asStandalone();

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE);

        return responseBuilder(responseBuilder);
    }

    @Override
    public ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ObjectAndActionInvocation objectAndActionInvocation) {

        final ResponseBuilder responseBuilder;

        final List<MediaType> acceptableMediaTypes = resourceContext.getAcceptableMediaTypes();
        if(isAccepted(RepresentationType.DOMAIN_OBJECT, acceptableMediaTypes, true)) {

            final ManagedObject adapter;
            final Collection<ManagedObject> collectionAdapters = objectAdaptersFrom(objectAndActionInvocation);

            if(collectionAdapters != null) {
                final ObjectSpecification elementSpec = elementSpecFrom(objectAndActionInvocation);
                final String actionOwningType = actionOwningTypeFrom(objectAndActionInvocation);
                final String actionId = actionIdFrom(objectAndActionInvocation);
                final String actionArguments = actionArgumentsFrom(objectAndActionInvocation);
                final DomainObjectList list = domainObjectListFrom(collectionAdapters, elementSpec, actionOwningType, actionId, actionArguments);

                adapter = ManagedObject._adapterOfList(resourceContext.getSpecificationLoader(), list);

            } else {
                adapter = objectAndActionInvocation.getReturnedAdapter();
            }
            responseBuilder = buildResponse(resourceContext, adapter);

        } else if(isAccepted(RepresentationType.ACTION_RESULT, acceptableMediaTypes)) {
            responseBuilder = buildResponseTo(resourceContext, objectAndActionInvocation, JsonRepresentation.newMap(), null);
        } else {
            throw RestfulObjectsApplicationException.create(RestfulResponse.HttpStatusCode.NOT_ACCEPTABLE);
        }

        return responseBuilder(responseBuilder);
    }

    private static String actionOwningTypeFrom(final ObjectAndActionInvocation objectAndActionInvocation) {
        return objectAndActionInvocation.getAction().getOnType().getSpecId().asString();
    }

    private static String actionIdFrom(final ObjectAndActionInvocation objectAndActionInvocation) {
        return objectAndActionInvocation.getAction().getId();
    }

    private static String actionArgumentsFrom(final ObjectAndActionInvocation objectAndActionInvocation) {
        final StringBuilder buf = new StringBuilder();
        val parameters = objectAndActionInvocation.getAction().getParameters();
        val argAdapters = objectAndActionInvocation.getArgAdapters();
        if(parameters.size() == argAdapters.size()) {
            for (int i = 0; i < parameters.size(); i++) {
                
                val paramIndex = i;
                val param = parameters.getOrThrow(paramIndex);
                val argAdapter = argAdapters.get(paramIndex);

                if(buf.length() > 0) {
                    buf.append(",");
                }
                buf.append(param.getName()).append("=");
                buf.append(abbreviated(titleOf(argAdapter), 8));
            }
        }

        return buf.toString();
    }

    private static String titleOf(final ManagedObject argumentAdapter) {
        return argumentAdapter!=null?argumentAdapter.titleString(null):"";
    }

    private static String abbreviated(final String str, final int maxLength) {
        return str.length() < maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }

    private static DomainObjectList domainObjectListFrom(
            final Collection<ManagedObject> collectionAdapters,
            final ObjectSpecification elementSpec,
            final String actionOwningType,
            final String actionId,
            final String actionArguments) {

        final String title = titleFrom(collectionAdapters, elementSpec);

        final DomainObjectList list = new DomainObjectList(
                title, elementSpec.getSpecId().asString(), actionOwningType, actionId, actionArguments);
        for (val adapter : collectionAdapters) {
            list.getObjects().add(adapter.getPojo());
        }
        return list;
    }

    private static String titleFrom(
            final Collection<ManagedObject> collectionAdapters,
            final ObjectSpecification elementSpec) {
        final String singularName = elementSpec.getSingularName();
        final String pluralName = elementSpec.getPluralName();
        int size = collectionAdapters.size();
        final String title;
        switch (size) {
        case 0:
            title = "0 " + pluralName;
            break;
        case 1:
            title = "1 " + singularName;
            break;
        default:
            title = size + " " + pluralName;
            break;
        }
        return title;
    }

    private ObjectSpecification elementSpecFrom(final ObjectAndActionInvocation objectAndActionInvocation) {
        final TypeOfFacet typeOfFacet = objectAndActionInvocation.getAction().getFacet(TypeOfFacet.class);
        return typeOfFacet != null 
                ? typeOfFacet.valueSpec() 
                        : specificationLoader.loadSpecification(Object.class) ;
    }

    private Collection<ManagedObject> objectAdaptersFrom(final ObjectAndActionInvocation objectAndActionInvocation) {
        final ManagedObject returnedAdapter = objectAndActionInvocation.getReturnedAdapter();
        final ObjectSpecification returnType = objectAndActionInvocation.getAction().getReturnType();

        final CollectionFacet collectionFacet = returnType.getFacet(CollectionFacet.class);
        return collectionFacet != null
                ? collectionFacet.stream(returnedAdapter).collect(Collectors.toList())
                        : null;
    }

    /**
     * Not API
     */
    ResponseBuilder buildResponseTo(
            final IResourceContext resourceContext,
            final ObjectAndActionInvocation objectAndActionInvocation,
            final JsonRepresentation representation,
            final JsonRepresentation rootRepresentation) {
        final ActionResultReprRenderer renderer =
                new ActionResultReprRenderer(resourceContext, null, objectAndActionInvocation.getSelfLink(), representation);
        renderer.with(objectAndActionInvocation)
        .using(resourceContext.getObjectAdapterLinkTo());

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE, rootRepresentation);
        
        return responseBuilder;
    }

    /**
     * For easy subclassing to further customize, eg additional headers
     */
    protected ResponseBuilder responseBuilder(final ResponseBuilder responseBuilder) {
        return responseBuilder;
    }



    private void ensureCompatibleAcceptHeader(
            final RepresentationType representationType,
            final List<MediaType> acceptableMediaTypes) {
        if(!strictAcceptChecking) {
            return;
        }
        if (representationType == null) {
            return;
        }

        // RestEasy will check the basic media types...
        // ... so we just need to check the profile paramter
        final String producedProfile = representationType.getMediaTypeProfile();
        if (producedProfile == null) {
            return;
        }
        boolean accepted = isAccepted(producedProfile, acceptableMediaTypes);
        if(!accepted) {
            throw RestfulObjectsApplicationException.create(RestfulResponse.HttpStatusCode.NOT_ACCEPTABLE);
        }
    }

    private boolean isAccepted(
            final RepresentationType representationType,
            final List<MediaType> acceptableMediaTypes) {
        return isAccepted(representationType, acceptableMediaTypes, strictAcceptChecking);
    }

    private boolean isAccepted(
            final RepresentationType representationType,
            final List<MediaType> acceptableMediaTypes,
            final boolean strictAcceptChecking) {
        if(!strictAcceptChecking) {
            return true;
        }
        final String producedProfile = representationType.getMediaTypeProfile();
        if (producedProfile == null) {
            throw new IllegalArgumentException("RepresentationType " + representationType + " does not specify a 'profile' parameter");
        }
        return isAccepted(producedProfile, acceptableMediaTypes);
    }

    private static boolean isAccepted(
            final String producedProfile,
            final List<MediaType> acceptableMediaTypes) {
        for (MediaType mediaType : acceptableMediaTypes ) {
            String acceptedProfileValue = mediaType.getParameters().get("profile");
            if(acceptedProfileValue == null) {
                continue;
            }
            if(!producedProfile.equals(acceptedProfileValue)) {
                return false;
            }
        }
        return true;
    }

    @Inject protected IsisConfiguration configuration;
    @Inject protected SpecificationLoader specificationLoader;

}
