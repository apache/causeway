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
package org.apache.causeway.viewer.restfulobjects.rendering.service.conneg;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.domain.DomainObjectList;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.restfulobjects.applib.CausewayModuleViewerRestfulObjectsApplib;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.rendering.Caching;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.Responses;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ActionResultReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectActionReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectCollectionReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectPropertyReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;

import lombok.NonNull;
import lombok.val;

/**
 * Returns representations according to the
 * <a href="https://restfulobjects.org">Restful Objects</a> spec.
 *
 * @since 1.x {@index}
 */
@Service
@Named(CausewayModuleViewerRestfulObjectsApplib.NAMESPACE + ".ContentNegotiationServiceForRestfulObjectsV1_0")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("RestfulObjectsV1_0")
public class ContentNegotiationServiceForRestfulObjectsV1_0
implements ContentNegotiationService {


    protected final CausewayConfiguration configuration;
    protected final SpecificationLoader specificationLoader;

    private final AcceptChecking acceptChecking;

    @Inject
    public ContentNegotiationServiceForRestfulObjectsV1_0(
            final CausewayConfiguration configuration,
            final SpecificationLoader specificationLoader) {
        this.configuration = configuration;
        this.specificationLoader = specificationLoader;
        this.acceptChecking = AcceptChecking.fromConfig(configuration);
    }

    @Override
    public ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter) {

        ensureCompatibleAcceptHeader(RepresentationType.DOMAIN_OBJECT, resourceContext);

        return responseBuilder(buildResponseTo(
                resourceContext, objectAdapter, JsonRepresentation.newMap(), null));
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
                new DomainObjectReprRenderer(resourceContext, null, representationIfAnyRequired)
                .with(objectAdapter)
                .includesSelf();

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE, rootRepresentation);

        if(resourceContext.getIntent() == RepresentationService.Intent.JUST_CREATED) {
            responseBuilder.status(Response.Status.CREATED);
        }

        return responseBuilder;
    }

    @Override
    public ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedProperty objectAndProperty) {

        ensureCompatibleAcceptHeader(RepresentationType.OBJECT_PROPERTY, resourceContext);

        val renderer =
                new ObjectPropertyReprRenderer(resourceContext)
                .with(objectAndProperty)
                .usingLinkTo(resourceContext.getObjectAdapterLinkTo());

        val repMode = objectAndProperty.getRepresentationMode();
        if(repMode.isExplicit()) {
            renderer.withMemberMode(repMode);
        }

        return Responses.ofOk(renderer, Caching.NONE);
    }

    @Override
    public ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedCollection objectAndCollection) {

        ensureCompatibleAcceptHeader(RepresentationType.OBJECT_COLLECTION, resourceContext);

        return responseBuilder(buildResponseTo(
                resourceContext, objectAndCollection, JsonRepresentation.newMap(), null));
    }

    /**
     * Not API
     */
    ResponseBuilder buildResponseTo(
            final IResourceContext resourceContext,
            final ManagedCollection objectAndCollection,
            final JsonRepresentation representation,
            final JsonRepresentation rootRepresentation) {
        final ObjectCollectionReprRenderer renderer =
                new ObjectCollectionReprRenderer(resourceContext, null, null, representation);
        renderer.with(objectAndCollection)
        .usingLinkTo(resourceContext.getObjectAdapterLinkTo());

        if(objectAndCollection.getRepresentationMode().isExplicit()) {
            renderer.withMemberMode(objectAndCollection.getRepresentationMode());
        }

        return Responses.ofOk(renderer, Caching.NONE, rootRepresentation);
    }

    @Override
    public ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedAction objectAndAction) {

        ensureCompatibleAcceptHeader(RepresentationType.OBJECT_ACTION, resourceContext);

        val renderer =
                new ObjectActionReprRenderer(resourceContext)
                .with(objectAndAction)
                .usingLinkTo(resourceContext.getObjectAdapterLinkTo())
                .asStandalone();

        return responseBuilder(Responses.ofOk(renderer, Caching.NONE));
    }

    @Override
    public ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ObjectAndActionInvocation objectAndActionInvocation) {

        final List<MediaType> acceptableMediaTypes = resourceContext.getAcceptableMediaTypes();

        val returnTypeCompileTimeSpecification = objectAndActionInvocation.getReturnTypeSpecification();

        val isDomainObjectOrCollection = returnTypeCompileTimeSpecification.isEntityOrViewModelOrAbstract()
                || returnTypeCompileTimeSpecification.isPlural();

        if(isDomainObjectOrCollection
                && isAccepted(RepresentationType.DOMAIN_OBJECT, acceptableMediaTypes)) {

            return objectAndActionInvocation.asEitherSingularOrPlural()
            .fold(singularActionResult->{
                return responseBuilder(
                        buildResponse(
                                resourceContext,
                                singularActionResult));
            }, pluralActionResult->{
                final ObjectSpecification elementSpec =
                        objectAndActionInvocation.getAction().getElementType();
                final ObjectSpecification actionOwnerSpec = actionOwnerSpecFrom(objectAndActionInvocation);
                final String actionId = actionIdFrom(objectAndActionInvocation);
                final String actionArguments = actionArgumentsFrom(objectAndActionInvocation);
                final DomainObjectList listAsViewmodel = domainObjectListFrom(
                        pluralActionResult, elementSpec, actionOwnerSpec, actionId, actionArguments);

                val domainObjectListSpec = resourceContext.getMetaModelContext().getSpecificationLoader()
                    .specForType(DomainObjectList.class)
                    .filter(ObjectSpecification::isViewModel)
                    .orElseThrow(()->_Exceptions.unrecoverable(
                            "framework bug: DomainObjectList should be recognized as viewmodel"));

                val listAdapter = ManagedObject.viewmodel(domainObjectListSpec, listAsViewmodel, Optional.empty());
                return responseBuilder(
                        buildResponse(
                                resourceContext,
                                listAdapter));
            });

        }

        if(isAccepted(RepresentationType.ACTION_RESULT, acceptableMediaTypes)) {

            return responseBuilder(
                    buildResponseTo(
                            resourceContext,
                            objectAndActionInvocation,
                            JsonRepresentation.newMap(),
                            /*rootRepr*/null));
        }

        throw RestfulObjectsApplicationException.create(RestfulResponse.HttpStatusCode.NOT_ACCEPTABLE);

    }

    /**
     * For easy sub-classing to further customize, eg additional headers
     */
    protected ResponseBuilder responseBuilder(final ResponseBuilder responseBuilder) {
        return responseBuilder;
    }

    // -- HELPER

    private static ObjectSpecification actionOwnerSpecFrom(final ObjectAndActionInvocation objectAndActionInvocation) {
        return objectAndActionInvocation.getAction().getDeclaringType();
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
                val param = parameters.getElseFail(paramIndex);
                val argAdapter = argAdapters.getElseFail(paramIndex);

                if(buf.length() > 0) {
                    buf.append(",");
                }
                buf
                .append(param.getStaticFriendlyName()
                        .orElseThrow(_Exceptions::unexpectedCodeReach))
                .append("=")
                .append(abbreviated(titleOf(argAdapter), 8));
            }
        }

        return buf.toString();
    }

    private static String titleOf(final ManagedObject argumentAdapter) {
        return argumentAdapter!=null?argumentAdapter.getTitle():"";
    }

    private static String abbreviated(final String str, final int maxLength) {
        return str.length() < maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }

    private static DomainObjectList domainObjectListFrom(
            final Collection<ManagedObject> collectionAdapters,
            final ObjectSpecification elementSpec,
            final ObjectSpecification actionOwnerSpec,
            final String actionId,
            final String actionArguments) {

        final String title = titleFrom(collectionAdapters, elementSpec);

        final DomainObjectList list = new DomainObjectList(
                title, elementSpec.fqcn(), actionOwnerSpec.fqcn(), actionId, actionArguments);
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

    private static enum AcceptChecking {
        RELAXED,
        /**
         * Any unrecognized Accept headers will result in an HTTP Not Acceptable Response code (406).
         */
        STRICT;
        static AcceptChecking fromConfig(final CausewayConfiguration configuration) {
            return configuration.getViewer().getRestfulobjects().isStrictAcceptChecking()
                    ? AcceptChecking.STRICT
                    : AcceptChecking.RELAXED;
        }
        boolean isStrict() { return this == STRICT; }
        boolean isRelaxed() { return this == RELAXED; }
    }

    private void ensureCompatibleAcceptHeader(
            final RepresentationType representationType,
            final IResourceContext resourceContext) {
        if(acceptChecking.isRelaxed()) {
            return;
        }
        if (representationType == null) {
            return;
        }

        // RestEasy will check the basic media types...
        // ... so we just need to check the profile parameter
        final String producedProfile = representationType.getMediaTypeProfile();
        if (producedProfile == null) {
            return;
        }
        if(!isAccepted(producedProfile, resourceContext.getAcceptableMediaTypes())) {
            throw RestfulObjectsApplicationException.create(RestfulResponse.HttpStatusCode.NOT_ACCEPTABLE);
        }
    }

    private boolean isAccepted(
            final RepresentationType representationType,
            final List<MediaType> acceptableMediaTypes) {

        final String producedProfile = representationType.getMediaTypeProfile();
        if (producedProfile != null) {
            return isAccepted(producedProfile, acceptableMediaTypes);
        }
        if(acceptChecking.isStrict()) {
            throw new IllegalArgumentException("RepresentationType " + representationType
                    + " does not specify a 'profile' parameter");
        }
        return false;
    }

    private static boolean isAccepted(
            final @NonNull String producedProfile,
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


}
